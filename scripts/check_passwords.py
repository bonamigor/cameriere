import gzip, re, hashlib
from collections import Counter
path = r"d:\jubo\jubo\backups\automaticos\railway_20260716_172946.sql.gz"
sha1234 = hashlib.sha1(b'1234').hexdigest()
with gzip.open(path,'rt',encoding='utf-8',errors='replace') as f: t=f.read()
m = re.search(r"INSERT INTO `users`[^;]+;", t)
rows = re.findall(r"'([^']+)'\s*,\s*(\d)\s*,", m.group(0))
c = Counter(s for s,a in rows)
print('password hash counts:', c)
print('1234 hash users', c.get(sha1234,0), 'total', len(rows))
