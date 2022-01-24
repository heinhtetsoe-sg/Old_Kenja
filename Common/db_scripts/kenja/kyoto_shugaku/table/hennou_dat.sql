drop table HENNOU_DAT

create table HENNOU_DAT \
( \
    SHUUGAKU_NO     VARCHAR(7) NOT NULL, \
    HENNOU_GAKU     INTEGER NOT NULL, \
    INPUT_DATE      DATE, \
    HENNOU_DATE     DATE NOT NULL, \
    HENNOU_BANGO    VARCHAR(5) NOT NULL, \
    HENNOU_BIKOU    VARCHAR(120), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in USR1DMS INDEX in IDX1DMS 

alter table HENNOU_DAT add constraint PK_HENNOU_DAT \
    primary key (SHUUGAKU_NO, HENNOU_DATE, HENNOU_BANGO)