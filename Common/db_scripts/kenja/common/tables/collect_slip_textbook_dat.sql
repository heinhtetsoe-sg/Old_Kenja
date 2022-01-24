-- kanji=漢字
-- $Id: 9e614adb5e8c3d1433f15bfcb7a2146407d4f9d2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_SLIP_TEXTBOOK_DAT

create table COLLECT_SLIP_TEXTBOOK_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SLIP_NO"               varchar(15) not null, \
        "TEXTBOOKCD"            varchar(12) not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "TEXTBOOKUNITPRICE"     integer, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SLIP_TEXTBOOK_DAT \
add constraint PK_SLIP_TEXTBOOK \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, TEXTBOOKCD)
