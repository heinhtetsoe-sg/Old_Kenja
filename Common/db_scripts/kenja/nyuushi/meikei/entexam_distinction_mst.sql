-- $Id: 373fcf2dd21250bacca85bec943c3a06123a06b6 $

drop table ENTEXAM_DISTINCTION_MST

create table ENTEXAM_DISTINCTION_MST( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    DISTINCT_ID     varchar(3)   not null, \
    DISTINCT_NAME   varchar(60)  not null, \
    TESTDIV         varchar(1)   not null, \
    EXAM_TYPE       varchar(2)   not null, \
    TEST_DATE       date         not null, \
    REGISTERCD      varchar(10),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_DISTINCTION_MST \
add constraint PK_ENT_DISTINCT_M \
primary key (ENTEXAMYEAR, APPLICANTDIV, DISTINCT_ID)
