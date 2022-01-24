-- $Id: 06773905e14937728470a78dc7cdb3b0d154b260 $

drop table ASSESSMENT_ANS_CONSULT_DAT
create table ASSESSMENT_ANS_CONSULT_DAT( \
    SCHREGNO            varchar(8)  not null, \
    WRITING_DATE        DATE        not null, \
    CONSULT_CD          varchar(1)  not null, \
    INSTITUTES_CD       varchar(5), \
    CONSULT_DATE        varchar(7), \
    CONSULT_TEXT        varchar(400), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_ANS_CONSULT_DAT add constraint PK_ASSESS_ANS_CON primary key (SCHREGNO, WRITING_DATE, CONSULT_CD)
