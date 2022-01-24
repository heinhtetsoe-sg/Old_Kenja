-- kanji=漢字
-- $Id: 3fe45d3b4a7d6c081cf7bdc3a240491dce285da8 $
-- 一日欠データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_ABSENCE_REMARK_DAT

create table ATTEND_ABSENCE_REMARK_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ABSENCE_DATE    date not null, \
    REMARK          varchar(60), \
    YEAR            varchar(4), \
    SEMESTER        varchar(1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_ABSENCE_REMARK_DAT add constraint PK_ATTEND_AB_REM \
        primary key (SCHREGNO, ABSENCE_DATE)
