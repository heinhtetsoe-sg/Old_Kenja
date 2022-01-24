create database gakumudb \
using codeset IBM-eucJP territory ja_JP

connect to gakumudb

create bufferpool bp8k1 size 10000 pagesize 8k
create bufferpool bp16k1 size 10000 pagesize 16k
