-- kanji=漢字
-- $Id: 48862044285268dfceff0fd0ee70470f897a7349 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
