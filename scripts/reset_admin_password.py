#!/usr/bin/env python3
"""Redefine senha do admin kideliciafoods@outlook.com para BCrypt."""
import bcrypt
import subprocess
import sys

MYSQL = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
EMAIL = "kideliciafoods@outlook.com"
NEW_PASSWORD = "123456"


def main() -> int:
    hashed = bcrypt.hashpw(NEW_PASSWORD.encode(), bcrypt.gensalt(rounds=10)).decode()
    escaped = hashed.replace("\\", "\\\\").replace("'", "''")
    sql = (
        f"UPDATE usuarios SET senha = '{escaped}' WHERE email = '{EMAIL}'; "
        f"SELECT id, email, admin, LENGTH(senha) AS senha_len "
        f"FROM usuarios WHERE email = '{EMAIL}';"
    )
    result = subprocess.run(
        [MYSQL, "-uroot", "-proot", "jubo_new", "-e", sql],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if result.returncode != 0:
        print(result.stderr, file=sys.stderr)
        return 1
    print(result.stdout.strip())
    print(f"Senha redefinida para: {NEW_PASSWORD}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
