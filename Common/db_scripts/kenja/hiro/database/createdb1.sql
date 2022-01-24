create database KNJ_UTF8 \
using codeset UTF-8 territory JA_JP \
collate using IDENTITY

connect to KNJ_UTF8

create bufferpool bp8k1 size 10000 pagesize 8k
create bufferpool bp16k1 size 10000 pagesize 16k
