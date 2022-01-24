-- $Id: dae80fa857b7d93f9d5900aad91e272a81026d67 $

drop table RECRUIT_VISIT_DAT

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

alter table RECRUIT_VISIT_DAT add constraint PK_RECRUIT_VIS_DAT primary key (YEAR, RECRUIT_NO)
