#!/usr/bin/env python3
"""Converte senhas SHA1 legadas (padrão 1234) para BCrypt no banco destino."""
import hashlib
import subprocess
import sys

import bcrypt

MYSQL = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
DB = "jubo_new"
USER = "root"
PASSWORD = "root"
DEFAULT_PLAIN = "1234"
LEGACY_SHA1 = hashlib.sha1(DEFAULT_PLAIN.encode()).hexdigest()


def main() -> int:
    hashed = bcrypt.hashpw(DEFAULT_PLAIN.encode(), bcrypt.gensalt(rounds=10)).decode()
    sql = (
        f"UPDATE usuarios SET senha = '{hashed}' "
        f"WHERE LENGTH(senha) = 40 AND admin = 0 AND senha = '{LEGACY_SHA1}'; "
        f"SELECT ROW_COUNT();"
    )
    result = subprocess.run(
        [MYSQL, f"-u{USER}", f"-p{PASSWORD}", DB, "-N", "-e", sql],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if result.returncode != 0:
        print(result.stderr, file=sys.stderr)
        return 1
    updated = result.stdout.strip().splitlines()[-1] if result.stdout.strip() else "?"
    print(f"Operadores atualizados para BCrypt (senha padrão '{DEFAULT_PLAIN}'): {updated}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
