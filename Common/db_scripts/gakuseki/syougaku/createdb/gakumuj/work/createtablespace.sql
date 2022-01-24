connect to gakumuj

drop tablespace usr2dms

create regular tablespace usr2dms pagesize 16k \
managed by database using \
(file '/home/db2inst1/db2inst1/NODE0000/SQL00007/dms/usr2dms1' 6400) \
bufferpool bp16k1


