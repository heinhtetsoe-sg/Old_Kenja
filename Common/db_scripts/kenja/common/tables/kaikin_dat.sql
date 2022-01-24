-- $Id: bc38c3ebaf27ddc3e0ce5fe3fd3153c4c261e3db $

drop table KAIKIN_DAT

create table KAIKIN_DAT( \
    YEAR                    VARCHAR(4)      not null    ,  \
    SCHREGNO                VARCHAR(8)      not null    ,  \
    KAIKIN_CD               VARCHAR(2)      not null    ,  \
    KAIKIN_FLG              VARCHAR(1)                  ,  \
    INVALID_FLG             VARCHAR(1)                  ,  \
    KAIKIN_AUTO_FLG         VARCHAR(1)                  ,  \
    REGISTERCD              VARCHAR(10)                 ,  \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table KAIKIN_DAT add constraint PK_KAIKIN_DAT primary key (YEAR, SCHREGNO, KAIKIN_CD)
