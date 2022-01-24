-- kanji=漢字
-- $Id: b2d39f4ae2e7997d1ec48af88a86752be34e1459 $
-- 仮出欠データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND2_DAT

create table ATTEND2_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    PERIODCD        varchar(1) not null, \
    CHAIRCD         varchar(7), \
    DI_CD           varchar(2), \
    YEAR            varchar(4), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND2_DAT add constraint PK_ATTEND2_DAT \
        primary key (SCHREGNO, ATTENDDATE, PERIODCD)
