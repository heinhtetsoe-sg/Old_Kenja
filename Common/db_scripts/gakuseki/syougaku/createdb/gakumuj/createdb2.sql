connect to gakumuj

drop tablespace userspace1

drop tablespace usr1dms

create regular tablespace usr1dms pagesize 8k \
managed by database using \
(file '/home/db2inst1/db2inst1/NODE0000/SQL00007/dms/usr1dms1' 64000) \
bufferpool bp8k1

drop tablespace idx1dms

create regular tablespace idx1dms pagesize 4k \
managed by database using \
(file '/home/db2inst1/db2inst1/NODE0000/SQL00007/dms/idx1dms1' 50000)

