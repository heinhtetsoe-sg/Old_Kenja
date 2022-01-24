-- kanji=漢字
-- $Id: a504055542081ee4e1727e10747b5fc31c8f933f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ENTEXAM_QUESTION_POINT_YMST_OLD

RENAME TABLE ENTEXAM_QUESTION_POINT_YMST TO ENTEXAM_QUESTION_POINT_YMST_OLD

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

INSERT INTO ENTEXAM_QUESTION_POINT_YMST \
    SELECT \
        ENTEXAMYEAR, \
        SUBCLASS_CD, \
        LARGE_QUESTION, \
        QUESTION, \
        QUESTION_ORDER, \
        PATTERN_CD, \
        ANSWER1, \
        POINT1, \
        ANSWER2, \
        POINT2, \
        CAST(NULL AS VARCHAR(2)) AS ANSWER3, \
        CAST(NULL AS VARCHAR(3)) AS POINT3, \
        QUEST_FLAG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_QUESTION_POINT_YMST_OLD
