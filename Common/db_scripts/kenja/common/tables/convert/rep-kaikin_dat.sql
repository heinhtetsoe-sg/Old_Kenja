-- $Id: a550963fcccff7b170cea0d969634970e5ce9d2b $

DROP TABLE KAIKIN_DAT_OLD
RENAME TABLE KAIKIN_DAT TO KAIKIN_DAT_OLD

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

INSERT INTO KAIKIN_DAT \
    SELECT \
        YEAR             ,  \
        SCHREGNO         ,  \
        KAIKIN_CD        ,  \
        KAIKIN_FLG       ,  \
        INVALID_FLG      ,  \
        cast(null as varchar(1)) as KAIKIN_AUTO_FLG  ,  \
        REGISTERCD       ,  \
        UPDATED             \
    FROM                    \
        KAIKIN_DAT_OLD
