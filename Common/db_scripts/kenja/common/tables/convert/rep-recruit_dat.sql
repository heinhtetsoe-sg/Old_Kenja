-- $Id: 86334de501d5f4864dee1a98d873dc262b068034 $
drop table RECRUIT_DAT_OLD
create table RECRUIT_DAT_OLD like RECRUIT_DAT
insert into RECRUIT_DAT_OLD select * from RECRUIT_DAT

drop table RECRUIT_DAT

create table RECRUIT_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    NAME                varchar(120) not null, \
    NAME_KANA           varchar(240) not null, \
    SCHOOL_KIND         varchar(2) not null, \
    SEX                 varchar(1), \
    BIRTHDAY            date, \
    FINSCHOOLCD         varchar(12), \
    GRADE               varchar(2), \
    SCHOOL_TEACHER      varchar(120), \
    PRISCHOOLCD         varchar(7), \
    PRISCHOOL_CLASS_CD  varchar(7), \
    PRISCHOOL_TEACHER   varchar(120), \
    GUARD_NAME          varchar(60), \
    GUARD_KANA          varchar(120), \
    ZIPCD               varchar(8), \
    ADDR1               varchar(150), \
    ADDR2               varchar(150), \
    TELNO               varchar(14), \
    TELNO2              varchar(14), \
    FAXNO               varchar(14), \
    EMAIL               varchar(120), \
    REMARK              varchar(250), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_DAT add constraint PK_RECRUIT_DAT primary key (YEAR, RECRUIT_NO)

INSERT INTO RECRUIT_DAT \
    SELECT \
        * \
    FROM \
        RECRUIT_DAT_OLD
