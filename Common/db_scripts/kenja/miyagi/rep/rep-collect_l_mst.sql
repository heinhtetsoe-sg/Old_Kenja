-- kanji=´Á»ú
-- $Id: rep-collect_l_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table COLLECT_L_MST_OLD
create table COLLECT_L_MST_OLD like COLLECT_L_MST
insert into COLLECT_L_MST_OLD select * from COLLECT_L_MST

drop table COLLECT_L_MST

create table COLLECT_L_MST ( \
        "COLLECT_L_CD"    varchar(2) not null, \
        "COLLECT_L_NAME"  varchar(90), \
        "COLLECT_L_ABBV"  varchar(90), \
        "LEVY_FLG"        varchar(1), \
        "REGISTERCD"      varchar(8), \
        "UPDATED"         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_L_MST add constraint PK_COLLECT_L_MST primary key (COLLECT_L_CD)

insert into \
COLLECT_L_MST \
SELECT \
    T1.COLLECT_L_CD, \
    T1.COLLECT_L_NAME, \
    T1.COLLECT_L_ABBV, \
    cast(null as varchar(1)) as LEVY_FLG, \
    T1.REGISTERCD, \
    T1.UPDATED \
FROM \
    COLLECT_L_MST_OLD T1
