-- $Id: 9be2f6015895ad61962bd957259711150334c139 $
drop table ENTEXAM_NO_DAT

create table ENTEXAM_NO_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    RECNO               smallint    not null, \
    EXAMNO_FROM         varchar(10), \
    EXAMNO_TO           varchar(10), \
    TESTDIV0            varchar(2), \
    TESTDIV             varchar(2), \
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
