-- kanji=漢字
-- $Id: 79e405d6f1cc8c66f65a30dd799537dc2827e39a $
-- 一日欠データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_ABSENCE_DAT

create table ATTEND_ABSENCE_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ABSENCE_DATE    date not null, \
    YEAR            varchar(4), \
    SEMESTER        varchar(1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_ABSENCE_DAT add constraint PK_ATTEND_AB_DAT \
        primary key (SCHREGNO, ABSENCE_DATE)
