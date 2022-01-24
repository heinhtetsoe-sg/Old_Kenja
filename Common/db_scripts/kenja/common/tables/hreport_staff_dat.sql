-- kanji=漢字
-- $Id: ed6b48e6bbf52e43875357f2b7bda674c1f2759b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--登録銀行データ
drop table HREPORT_STAFF_DAT

create table HREPORT_STAFF_DAT \
( \
        "YEAR"          varchar(4)  not null, \
        "SEMESTER"      varchar(1)  not null, \
        "SCHREGNO"      varchar(8)  not null, \
        "SEQ"           varchar(3)  not null,  \
        "STAFFCD"       varchar(10),  \
        "REGISTERCD"    varchar(10), \
        "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_STAFF_DAT add constraint PK_HREPORT_STAFF_DAT primary key (YEAR, SEMESTER, SCHREGNO, SEQ)
