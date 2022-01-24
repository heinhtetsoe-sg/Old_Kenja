-- $Id: 006ad3ca46c1384a48fa4735e078cee84f8564d7 $

drop table ASSESSMENT_ANS_DAT
create table ASSESSMENT_ANS_DAT( \
    SCHREGNO        varchar(8)  not null, \
    WRITING_DATE    DATE        not null, \
    ASSESS_DIV      varchar(2)  not null, \
    QUESTION1       varchar(1), \
    QUESTION2       varchar(1), \
    QUESTION3       varchar(1), \
    QUESTION4       varchar(1), \
    QUESTION5       varchar(1), \
    QUESTION6       varchar(1), \
    QUESTION7       varchar(1), \
    QUESTION8       varchar(1), \
    QUESTION9       varchar(1), \
    QUESTION10      varchar(1), \
    QUESTION11      varchar(1), \
    QUESTION12      varchar(1), \
    QUESTION13      varchar(1), \
    QUESTION14      varchar(1), \
    QUESTION15      varchar(1), \
    REMARK1         varchar(1500), \
    REMARK2         varchar(150), \
    REGISTERCD      varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_ANS_DAT add constraint PK_ASSESSMENT_ANS primary key (SCHREGNO, WRITING_DATE, ASSESS_DIV)
