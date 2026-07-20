#!/usr/bin/env python3
"""
Importa backup Railway (schema antigo) e migra para o schema Flyway atual (jubo_new).

Uso:
  python scripts/migrate_production_backup.py ^
    --backup "d:\\jubo\\jubo\\backups\\automaticos\\railway_20260716_172946.sql.gz"

Pré-requisitos:
  1. MySQL em execução
  2. Banco destino (jubo_new) já criado com migrations Flyway V1-V3 aplicadas
  3. cameriere.seed.enabled=false ao subir a API após a migração
"""
from __future__ import annotations

import argparse
import gzip
import hashlib
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TRANSFORM_SQL = ROOT / "scripts" / "sql" / "transform_staging_to_new.sql"

TABLES_WITH_AI = [
    "clientes",
    "produtos",
    "estantes",
    "estante_produto",
    "pedidos",
    "item_pedido",
    "usuarios",
]

# SHA1 legado observado no backup para operadores com senha padrão "1234"
LEGACY_SHA1_1234 = "7110eda4d09e062aa5e4a390b0a572ac0d2c0220"


def run_mysql(
    mysql_bin: str,
    host: str,
    port: int,
    user: str,
    password: str,
    database: str | None,
    sql: str | None = None,
    input_file: Path | None = None,
    capture: bool = False,
) -> subprocess.CompletedProcess:
    cmd = [
        mysql_bin,
        f"-h{host}",
        f"-P{port}",
        f"-u{user}",
        f"-p{password}",
        "--default-character-set=utf8mb4",
    ]
    if database:
        cmd.append(database)

    kwargs: dict = {"text": True, "encoding": "utf-8", "errors": "replace"}
    if capture:
        kwargs["stdout"] = subprocess.PIPE
        kwargs["stderr"] = subprocess.PIPE

    if input_file:
        with input_file.open("r", encoding="utf-8", errors="replace") as fh:
            return subprocess.run(cmd, stdin=fh, **kwargs)
    return subprocess.run(cmd, input=sql, **kwargs)


def mysql_query(
    mysql_bin: str,
    host: str,
    port: int,
    user: str,
    password: str,
    database: str,
    query: str,
) -> str:
    result = run_mysql(
        mysql_bin, host, port, user, password, database, sql=query, capture=True
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "Erro ao executar query MySQL")
    return result.stdout.strip()


def decompress_backup(backup: Path, workdir: Path) -> Path:
    if backup.suffix == ".gz":
        out = workdir / backup.with_suffix("").name
        with gzip.open(backup, "rb") as src, out.open("wb") as dst:
            shutil.copyfileobj(src, dst)
        return out
    return backup


def ensure_flyway(target_db: str, mysql_bin: str, host: str, port: int, user: str, password: str) -> None:
    rows = mysql_query(
        mysql_bin,
        host,
        port,
        user,
        password,
        target_db,
        "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank;",
    )
    versions = [line.strip() for line in rows.splitlines() if line.strip()]
    if not versions:
        raise RuntimeError(
            f"Banco '{target_db}' sem histórico Flyway. Suba a API uma vez ou rode as migrations antes."
        )
    print(f"Flyway aplicado em {target_db}: {', '.join(versions)}")


def reset_auto_increment(
    target_db: str, mysql_bin: str, host: str, port: int, user: str, password: str
) -> None:
    for table in TABLES_WITH_AI:
        max_id = mysql_query(
            mysql_bin,
            host,
            port,
            user,
            password,
            target_db,
            f"SELECT COALESCE(MAX(id), 0) FROM {table};",
        )
        next_id = int(max_id.splitlines()[-1]) + 1
        run_mysql(
            mysql_bin,
            host,
            port,
            user,
            password,
            target_db,
            sql=f"ALTER TABLE {table} AUTO_INCREMENT = {next_id};",
        )


def reencode_legacy_passwords(
    target_db: str,
    mysql_bin: str,
    host: str,
    port: int,
    user: str,
    password: str,
    default_password: str,
) -> None:
    try:
        import bcrypt
    except ImportError:
        print(
            "AVISO: pacote 'bcrypt' não instalado; senhas legadas (SHA1) não foram convertidas.\n"
            "       Instale com: pip install bcrypt\n"
            "       Operadores precisarão redefinir senha ou você pode rodar novamente com --reencode-passwords."
        )
        return

    bcrypt_hash = bcrypt.hashpw(default_password.encode(), bcrypt.gensalt(rounds=10)).decode()
    legacy_hash = hashlib.sha1(default_password.encode()).hexdigest()

    sql = f"""
UPDATE usuarios
SET senha = '{bcrypt_hash}'
WHERE LENGTH(senha) = 40
  AND admin = 0
  AND senha = '{legacy_hash}';
"""
    result = run_mysql(
        mysql_bin, host, port, user, password, target_db, sql=sql, capture=True
    )
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip())

    updated = mysql_query(
        mysql_bin,
        host,
        port,
        user,
        password,
        target_db,
        f"SELECT ROW_COUNT();",
    )
    print(
        f"Senhas BCrypt aplicadas para operadores com hash legado de '{default_password}' "
        f"(esperado SHA1={legacy_hash})."
    )
    print("Admins e usuários com senha customizada precisam redefinir senha manualmente.")


def main() -> int:
    parser = argparse.ArgumentParser(description="Migra backup Railway para schema Flyway atual")
    parser.add_argument(
        "--backup",
        default=r"d:\jubo\jubo\backups\automaticos\railway_20260716_172946.sql.gz",
        help="Caminho do .sql ou .sql.gz do backup",
    )
    parser.add_argument("--staging-db", default="jubo_staging")
    parser.add_argument("--target-db", default="jubo_new")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=3306)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", default="root")
    parser.add_argument(
        "--mysql-bin",
        default=r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    )
    parser.add_argument(
        "--reencode-passwords",
        action="store_true",
        help="Converte senhas SHA1 legadas (padrão '1234') para BCrypt nos operadores",
    )
    parser.add_argument("--default-password", default="1234")
    parser.add_argument(
        "--keep-staging",
        action="store_true",
        help="Não remove o banco staging ao final",
    )
    args = parser.parse_args()

    backup = Path(args.backup)
    if not backup.exists():
        print(f"Backup não encontrado: {backup}", file=sys.stderr)
        return 1
    if not TRANSFORM_SQL.exists():
        print(f"SQL de transformação não encontrado: {TRANSFORM_SQL}", file=sys.stderr)
        return 1
    if not Path(args.mysql_bin).exists():
        print(f"mysql.exe não encontrado: {args.mysql_bin}", file=sys.stderr)
        return 1

    with tempfile.TemporaryDirectory(prefix="cameriere_migrate_") as tmp:
        workdir = Path(tmp)
        sql_file = decompress_backup(backup, workdir)
        print(f"Backup preparado: {sql_file} ({sql_file.stat().st_size / 1024 / 1024:.1f} MB)")

        print(f"Recriando staging '{args.staging_db}'...")
        run_mysql(
            args.mysql_bin,
            args.host,
            args.port,
            args.user,
            args.password,
            None,
            sql=f"DROP DATABASE IF EXISTS `{args.staging_db}`; CREATE DATABASE `{args.staging_db}` "
            f"CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
        )

        print("Importando backup no staging (pode levar alguns minutos)...")
        result = run_mysql(
            args.mysql_bin,
            args.host,
            args.port,
            args.user,
            args.password,
            args.staging_db,
            input_file=sql_file,
            capture=True,
        )
        if result.returncode != 0:
            print(result.stderr, file=sys.stderr)
            return 1
        print("Import concluído.")

        ensure_flyway(args.target_db, args.mysql_bin, args.host, args.port, args.user, args.password)

        transform = TRANSFORM_SQL.read_text(encoding="utf-8")
        transform = transform.replace("jubo_staging", args.staging_db).replace("jubo_new", args.target_db)

        transform_file = workdir / "transform.sql"
        transform_file.write_text(transform, encoding="utf-8")

        print(f"Migrando staging -> {args.target_db}...")
        result = run_mysql(
            args.mysql_bin,
            args.host,
            args.port,
            args.user,
            args.password,
            args.target_db,
            input_file=transform_file,
            capture=True,
        )
        if result.returncode != 0:
            print(result.stderr, file=sys.stderr)
            return 1
        if result.stdout.strip():
            print(result.stdout)

        reset_auto_increment(
            args.target_db, args.mysql_bin, args.host, args.port, args.user, args.password
        )

        if args.reencode_passwords:
            reencode_legacy_passwords(
                args.target_db,
                args.mysql_bin,
                args.host,
                args.port,
                args.user,
                args.password,
                args.default_password,
            )
        else:
            print(
                "\nIMPORTANTE: senhas do backup antigo estão em SHA1; a API nova usa BCrypt.\n"
                "Rode com --reencode-passwords para operadores com senha padrão '1234',\n"
                "ou redefina senhas manualmente antes de usar o login."
            )

        if not args.keep_staging:
            run_mysql(
                args.mysql_bin,
                args.host,
                args.port,
                args.user,
                args.password,
                None,
                sql=f"DROP DATABASE IF EXISTS `{args.staging_db}`;",
            )
            print(f"Staging '{args.staging_db}' removido.")

    print("\nMigração concluída com sucesso.")
    print("Próximos passos:")
    print("  1. Defina cameriere.seed.enabled=false em application-dev.properties")
    print("  2. Suba a API e valide login de um operador")
    print("  3. Confira pedidos, estantes e clientes na aplicação")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
