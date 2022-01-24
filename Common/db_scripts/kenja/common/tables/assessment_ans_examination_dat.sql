-- $Id: f8a0e42e63cbbe9036c22fd9b63af58254829a7c $

drop table ASSESSMENT_ANS_EXAMINATION_DAT
create table ASSESSMENT_ANS_EXAMINATION_DAT( \
    SCHREGNO            varchar(8)  not null, \
    WRITING_DATE        DATE        not null, \
    EXAMINATION_CD      varchar(1)  not null, \
    EXAMINATION_DATE    varchar(7), \
    INSTITUTES_CD       varchar(5), \
    TESTER_NAME         varchar(30), \
    REMARK1             varchar(6), \
    REMARK2             varchar(6), \
    REMARK3             varchar(6), \
    REMARK4             varchar(6), \
    REMARK5             varchar(6), \
    REMARK6             varchar(6), \
    REMARK7             varchar(6), \
    OTHER_TEXT          varchar(320), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_ANS_EXAMINATION_DAT add constraint PK_ASSESS_ANS_EXAM primary key (SCHREGNO, WRITING_DATE, EXAMINATION_CD)
