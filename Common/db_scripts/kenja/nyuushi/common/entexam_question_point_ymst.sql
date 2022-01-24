-- kanji=����
-- $Id: 48862044285268dfceff0fd0ee70470f897a7349 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ENTEXAM_QUESTION_POINT_YMST

create table ENTEXAM_QUESTION_POINT_YMST ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    SUBCLASS_CD             varchar(1)  not null, \
    LARGE_QUESTION          varchar(2)  not null, \
    QUESTION                varchar(2)  not null, \
    QUESTION_ORDER          varchar(3)  not null, \
    PATTERN_CD              varchar(1), \
    ANSWER1                 varchar(2), \
    POINT1                  varchar(3), \
    ANSWER2                 varchar(2), \
    POINT2                  varchar(3), \
    ANSWER3                 varchar(2), \
    POINT3                  varchar(3), \
    QUEST_FLAG              varchar(1)  not null, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_QUESTION_POINT_YMST add constraint PK_ENT_Q_POINT_YM \
      primary key (ENTEXAMYEAR, SUBCLASS_CD, LARGE_QUESTION, QUESTION)
