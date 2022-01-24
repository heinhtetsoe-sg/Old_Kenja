create bufferpool BP16K1 pagesize 16384
ALTER BUFFERPOOL BP16K1 SIZE 1000
create stogroup ibmstogroup on '/home/db2inst1'
create large tablespace "USR16DMS" in database partition group ibmdefaultgroup pagesize 16k managed by automatic storage using stogroup ibmstogroup autoresize yes bufferpool "BP16K1" file system caching dropped table recovery off

create bufferpool BP32K1 pagesize 32768
ALTER BUFFERPOOL BP32K1 SIZE 1000
create large tablespace "USR32DMS" in database partition group ibmdefaultgroup pagesize 32k managed by automatic storage using stogroup ibmstogroup autoresize yes bufferpool "BP32K1" file system caching dropped table recovery off
