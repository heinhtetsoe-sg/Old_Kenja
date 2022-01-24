-- $Id: fa7de3944c6fc2a91780060930f86a6064ca7f5f $

drop table RECRUIT_VISIT_DAT_OLD

RENAME TABLE RECRUIT_VISIT_DAT TO RECRUIT_VISIT_DAT_OLD

create table RECRUIT_VISIT_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    TOUROKU_DATE        date, \
    KAKUTEI_DATE        date, \
    HOPE_COURSECD       varchar(1), \
    HOPE_MAJORCD        varchar(3), \
    HOPE_COURSECODE     varchar(4), \
    STAFFCD             varchar(10), \
    TESTDIV             varchar(1), \
    JUDGE_KIND          varchar(1), \
    SCHOOL_DIV          varchar(1), \
    SCHOOL_NAME         varchar(75), \
    SCORE_CHK           varchar(1), \
    MOCK_CHK            varchar(1), \
    REMARK1             varchar(500), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO RECRUIT_VISIT_DAT \
    SELECT \
        YEAR, \
        RECRUIT_NO, \
        TOUROKU_DATE, \
        KAKUTEI_DATE, \
        HOPE_COURSECD, \
        HOPE_MAJORCD, \
        HOPE_COURSECODE, \
        STAFFCD, \
        TESTDIV, \
        JUDGE_KIND, \
        SCHOOL_DIV, \
        SCHOOL_NAME, \
        SCORE_CHK, \
        MOCK_CHK, \
        CAST(NULL AS VARCHAR(500)) AS REMARK1, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECRUIT_VISIT_DAT_OLD

alter table RECRUIT_VISIT_DAT add constraint PK_RECRUIT_VIS_DAT primary key (YEAR, RECRUIT_NO)
