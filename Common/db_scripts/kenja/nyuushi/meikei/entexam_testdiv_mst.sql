-- $Id: 595bc430742705a9c8c682b15bb8b8b570d8d974 $

drop table ENTEXAM_TESTDIV_MST

create table ENTEXAM_TESTDIV_MST( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    TESTDIV         varchar(1)   not null, \
    TESTDIV_NAME    varchar(30)  not null, \
    REGISTERCD      varchar(10),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_TESTDIV_MST \
add constraint PK_ENT_TESTDIV_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV)
