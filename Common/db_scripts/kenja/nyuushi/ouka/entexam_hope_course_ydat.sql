-- $Id: 5806b2eb31e7d8bb6e4e14e1ef7b73e0f59d8a94 $

drop table ENTEXAM_HOPE_COURSE_YDAT

create table ENTEXAM_HOPE_COURSE_YDAT( \
    ENTEXAMYEAR          varchar(4) NOT NULL, \
    HOPE_COURSECODE      varchar(4) NOT NULL, \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_HOPE_COURSE_YDAT add constraint PK_EXAM_HOPE_YDAT primary key (ENTEXAMYEAR, HOPE_COURSECODE)
