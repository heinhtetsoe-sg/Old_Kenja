-- $Id: c24a54551aff749f85f1d681d07531eda38ba21f $

drop table KAIKIN_MST

create table KAIKIN_MST( \
    KAIKIN_CD               VARCHAR(2)      not null    ,  \
    KAIKIN_NAME             VARCHAR(45)     not null    ,  \
    KAIKIN_DIV              VARCHAR(1)      not null    ,  \
    REF_YEAR                INTEGER         not null    ,  \
    KESSEKI_CONDITION       SMALLINT        not null    ,  \
    TIKOKU_CONDITION        SMALLINT                    ,  \
    SOUTAI_CONDITION        SMALLINT                    ,  \
    KESSEKI_KANSAN          SMALLINT                    ,  \
    KEKKA_JISU_CONDITION    VARCHAR(3)                  ,  \
    PRIORITY                INTEGER         not null    ,  \
    KAIKIN_FLG              VARCHAR(1)                  ,  \
    REGISTERCD              VARCHAR(10)                 ,  \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table KAIKIN_MST add constraint PK_KAIKIN_MST primary key (KAIKIN_CD)
