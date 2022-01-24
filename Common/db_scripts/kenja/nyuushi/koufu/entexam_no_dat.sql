-- $Id: 0d1ca28096557f6f985736e918a5be2bfc55a8e6 $
drop table ENTEXAM_NO_DAT

create table ENTEXAM_NO_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    RECNO               smallint    not null, \
    EXAMNO_FROM         varchar(5), \
    EXAMNO_TO           varchar(5), \
    TESTDIV0            varchar(1), \
    TESTDIV             varchar(1), \
    COURSECD            varchar(1), \
    MAJORCD             varchar(3), \
    EXAMCOURSECD        varchar(4), \
    EXAMHALLCD          varchar(4), \
    REMARK_DIV          varchar(1), \
    MEMO_KISO           varchar(1), \
    MEMO_HIKKI          varchar(1), \
    MEMO_MENSETU        varchar(1), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_NO_DAT add constraint \
PK_ENTEXAM_NO_DAT primary key (ENTEXAMYEAR, APPLICANTDIV, RECNO)
