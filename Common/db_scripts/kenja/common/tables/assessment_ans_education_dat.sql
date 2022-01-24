-- $Id: e26024900bece3bbf77f857020cee80708f52f51 $

drop table ASSESSMENT_ANS_EDUCATION_DAT
create table ASSESSMENT_ANS_EDUCATION_DAT( \
    SCHREGNO            varchar(8)  not null, \
    WRITING_DATE        DATE        not null, \
    P_S_YM              varchar(7), \
    P_E_YM              varchar(7), \
    P_PASSING_GRADE_FLG varchar(1), \
    P_SUPPORT_FLG       varchar(1), \
    P_ETC_FLG           varchar(1), \
    P_ETC               varchar(18), \
    P_DATE_S_YM         varchar(7), \
    P_DATE_E_YM         varchar(7), \
    J_S_YM              varchar(7), \
    J_E_YM              varchar(7), \
    J_PASSING_GRADE_FLG varchar(1), \
    J_SUPPORT_FLG       varchar(1), \
    J_ETC_FLG           varchar(1), \
    J_ETC               varchar(18), \
    J_DATE_S_YM         varchar(7), \
    J_DATE_E_YM         varchar(7), \
    EDUCATION_TEXT      varchar(750), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_ANS_EDUCATION_DAT add constraint PK_ASSESS_ANS_EDU primary key (SCHREGNO, WRITING_DATE)
