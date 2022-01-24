-- $Id: entexam_course_judgment_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table ENTEXAM_COURSE_JUDGMENT_MST

create table ENTEXAM_COURSE_JUDGMENT_MST \
( \
    ENTEXAMYEAR                     VARCHAR(4)  NOT NULL, \
    JUDGMENT_DIV                    VARCHAR(2)  NOT NULL, \
    PROMISE_COURSE_NAME             VARCHAR(45), \
    PROMISE_COURSE_ABBV             VARCHAR(30), \
    PROMISE_RECOMMEND_TEST_FLG      VARCHAR(1), \
    PROMISE_GENERAL_TEST_FLG        VARCHAR(1), \
    JUDGMENT_COURSE_NAME            VARCHAR(60), \
    JUDGMENT_COURSE_ABBV            VARCHAR(30), \
    TAKE_RECOMMEND_TEST_FLG         VARCHAR(1), \
    TAKE_GENERAL_TEST_FLG           VARCHAR(1), \
    CHANGE_SINGLE_TEST_FLG          VARCHAR(1), \
    NORMAL_PASSCOURSECD             VARCHAR(1), \
    NORMAL_PASSMAJORCD              VARCHAR(3), \
    NORMAL_PASSEXAMCOURSECD         VARCHAR(4), \
    EARLY_PASSCOURSECD              VARCHAR(1), \
    EARLY_PASSMAJORCD               VARCHAR(3), \
    EARLY_PASSEXAMCOURSECD          VARCHAR(4), \
    PASSCOURSE_DIV                  VARCHAR(1), \
    SCHOOLWORK_DIV                  VARCHAR(1), \
    SPECIAL_DIV                     VARCHAR(1), \
    REGISTERCD                      VARCHAR(8), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_COURSE_JUDGMENT_MST add constraint \
PK_ENTEXAM_CS_JUGE primary key (ENTEXAMYEAR, JUDGMENT_DIV)
