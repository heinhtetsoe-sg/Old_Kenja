-- $Id: 7e9d2feab4f7dff052f187a3eda23232d7c8ea55 $

drop table SCHREG_BRANCH_DAT_OLD
create table SCHREG_BRANCH_DAT_OLD like SCHREG_BRANCH_DAT
insert into SCHREG_BRANCH_DAT_OLD select * from SCHREG_BRANCH_DAT

DROP TABLE SCHREG_BRANCH_DAT

CREATE TABLE SCHREG_BRANCH_DAT( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    YEAR                varchar(4)  not null, \
    SCHREGNO            varchar(8)  not null, \
    BRANCHCD            varchar(2), \
    BRANCH_POSITION     varchar(2), \
    GUARD_NAME          varchar(120), \
    GUARD_KANA          varchar(240), \
    GUARD_ZIPCD         varchar(8), \
    GUARD_ADDR1         varchar(150), \
    GUARD_ADDR2         varchar(150), \
    GUARD_TELNO         varchar(14), \
    GUARD_TELNO2        varchar(14), \
    SEND_NAME           varchar(120), \
    RESIDENTCD          varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO SCHREG_BRANCH_DAT \
    SELECT \
        SCHOOLCD, \
        SCHOOL_KIND, \
        YEAR, \
        SCHREGNO, \
        BRANCHCD, \
        BRANCH_POSITION, \
        GUARD_NAME, \
        GUARD_KANA, \
        GUARD_ZIPCD, \
        GUARD_ADDR1, \
        GUARD_ADDR2, \
        GUARD_TELNO, \
        GUARD_TELNO2, \
        SEND_NAME, \
        CAST(NULL AS VARCHAR(1)) AS RESIDENTCD, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SCHREG_BRANCH_DAT_OLD

alter table SCHREG_BRANCH_DAT add constraint PK_SCHREG_BRANCH primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO)
