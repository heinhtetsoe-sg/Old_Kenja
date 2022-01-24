-- $Id: fc8de523f4339f803df073324d3d748b8774ff42 $

drop table ENTEXAM_TESTDIV_MST

create table ENTEXAM_TESTDIV_MST( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    TESTDIV         varchar(2)   not null, \
    TESTDIV_NAME    varchar(30)  not null, \
    TESTDIV_ABBV    varchar(10), \
    INTERVIEW_DIV   varchar(1), \
    CAPACITY        smallint, \
    TEST_DATE       date, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_TESTDIV_MST \
add constraint PK_ENT_TESTDIV_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV)
