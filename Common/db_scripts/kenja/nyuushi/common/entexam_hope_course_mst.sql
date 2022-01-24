-- $Id: 94a39ba8567a21728d526463e0369c2ca3a2ccbc $

drop table ENTEXAM_HOPE_COURSE_MST

create table ENTEXAM_HOPE_COURSE_MST( \
    HOPE_COURSECODE      varchar(4) NOT NULL, \
    COURSECODE           varchar(4) NOT NULL, \
    HOPE_NAME            varchar(60), \
    NOTICE_NAME          varchar(60), \
    PASS_NAME            varchar(60), \
    NOT_PASS_NAME        varchar(60), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_HOPE_COURSE_MST add constraint PK_EXAM_HOPE_MST primary key (HOPE_COURSECODE)
