-- $Id: entexam_applicant_before_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table ENTEXAM_APPLICANT_BEFORE_DAT
create table ENTEXAM_APPLICANT_BEFORE_DAT( \
    ENTEXAMYEAR          varchar(4)    not null, \
    APPLICANTDIV         varchar(1)    not null, \
    TESTDIV              varchar(1)    not null, \
    BEFORE_PAGE          varchar(3)    not null, \
    BEFORE_SEQ           varchar(3)    not null, \
    BEFORE_COURSECD      varchar(1), \
    BEFORE_MAJORCD       varchar(3), \
    BEFORE_EXAMCOURSECD  varchar(4), \
    FS_CD                varchar(7), \
    NAME                 varchar(60), \
    NAME_KANA            varchar(120), \
    SEX                  varchar(1), \
    NAISIN1              smallint, \
    NAISIN2              smallint, \
    NAISIN3              smallint, \
    ATTEND1              smallint, \
    ATTEND2              smallint, \
    ATTEND3              smallint, \
    ATTEND_TOTAL         smallint, \
    SENBATU1_SCHOOL      varchar(90), \
    SENBATU1_MAJOR       varchar(90), \
    SENBATU2_SCHOOL      varchar(90), \
    SENBATU2_MAJOR       varchar(90), \
    SCHOLARSHIP          varchar(1), \
    RECOM_FLG            varchar(1), \
    RECOM_REMARK         varchar(150), \
    REMARK               varchar(150), \
    REGISTERCD           varchar(8), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANT_BEFORE_DAT add constraint PK_ENTEXAM_BEF primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, BEFORE_PAGE, BEFORE_SEQ)
