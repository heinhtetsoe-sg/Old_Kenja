-- $Id: fe7c0a93aecf5a899f51dc07c1129415d11ebc5b $

drop table ENTEXAM_EXAMTYPE_MST

create table ENTEXAM_EXAMTYPE_MST( \
    ENTEXAMYEAR         varchar(4)   not null, \
    APPLICANTDIV        varchar(1)   not null, \
    EXAM_TYPE           varchar(2)   not null, \
    EXAMTYPE_NAME       varchar(60)  not null, \
    EXAMTYPE_NAME_ABBV  varchar(15)  not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp, \
    REMARK1             varchar(150), \
    REMARK2             varchar(150) \
) in usr1dms index in idx1dms

alter table ENTEXAM_EXAMTYPE_MST \
add constraint PK_ENT_EXAMTYPE_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, EXAM_TYPE)
