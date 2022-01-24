-- kanji=漢字
-- $Id: e7468992d8bd66358dc0b41f45fd37755e49abcc $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ENTEXAM_QUESTION_SCORE_DAT

create table ENTEXAM_QUESTION_SCORE_DAT ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    SUBCLASS_CD             varchar(1)  not null, \
    EXAMNO                  varchar(10) not null, \
    LARGE_QUESTION          varchar(2)  not null, \
    QUESTION                varchar(2)  not null, \
    QUESTION_ORDER          varchar(3)  not null, \
    PATTERN_CD              varchar(1)  not null, \
    SELECT1                 varchar(1), \
    SELECT2                 varchar(1), \
    SELECT3                 varchar(1), \
    SELECT4                 varchar(1), \
    SELECT5                 varchar(1), \
    SELECT6                 varchar(1), \
    SELECT7                 varchar(1), \
    SELECT8                 varchar(1), \
    SELECT9                 varchar(1), \
    SELECT10                varchar(1), \
    DOUBLE_MARK_FLG         varchar(1), \
    NO_MARK_FLG             varchar(1), \
    POINT                   smallint, \
    POINT_SYMBOL            varchar(3), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_QUESTION_SCORE_DAT add constraint PK_ENT_QU_SCORE_D \
      primary key (ENTEXAMYEAR, SUBCLASS_CD, EXAMNO, LARGE_QUESTION, QUESTION)
