-- kanji=漢字
-- $Id: 0200b7dd091411a4c5c67040be8a44dc79038a53 $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table RECORD_MOCK_AVERAGE_DAT

create table RECORD_MOCK_AVERAGE_DAT ( \
    YEAR            varchar(4) not null, \
    SUBCLASSCD      varchar(6) not null, \
    AVG_DIV         varchar(1) not null, \
    GRADE           varchar(2) not null, \
    HR_CLASS        varchar(3) not null, \
    COURSECD        varchar(1) not null, \
    MAJORCD         varchar(3) not null, \
    COURSECODE      varchar(4) not null, \
    SCORE           integer, \
    HIGHSCORE       integer, \
    LOWSCORE        integer, \
    COUNT           smallint, \
    AVG             decimal (9,5), \
    STDDEV          decimal (5,1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECORD_MOCK_AVERAGE_DAT add constraint pk_rec_mock_avg_d \
      primary key (YEAR, SUBCLASSCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
