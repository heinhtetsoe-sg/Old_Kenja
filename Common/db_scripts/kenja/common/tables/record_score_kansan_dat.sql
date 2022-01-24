-- kanji=漢字
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table RECORD_SCORE_KANSAN_DAT

create table RECORD_SCORE_KANSAN_DAT ( \
    YEAR            varchar(4) not null, \
    GRADE           varchar(2) not null, \
    SEMESTER        varchar(1) not null, \
    SCHREGNO        varchar(8) not null, \
    KANSAN_SCORE    smallint, \
    RANK            smallint, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECORD_SCORE_KANSAN_DAT add constraint PK_RECORD_SCORE_KANSAN_DAT \
      primary key (YEAR, GRADE, SEMESTER, SCHREGNO)
