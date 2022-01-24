-- kanji=漢字
-- $Id: a4998851220ba8e0b0d3057948212e4ae7f11297 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--入金予定中分類データ
drop table COLLECT_REPAY_SLIP_HIST_DAT

create table COLLECT_REPAY_SLIP_HIST_DAT \
( \
    SCHOOLCD              varchar(12) not null, \
    SCHOOL_KIND           varchar(2)  not null, \
    YEAR                  varchar(4)  not null, \
    REPAY_SLIP_NO         varchar(15) not null, \
    REPAY_SEQ             smallint not null, \
    SCHREGNO              varchar(8)  not null, \
    PRINT_STAFF           varchar(10), \
    PRINT_DATE            date, \
    REPAY_DATE            date, \
    REGISTERCD            varchar(10), \
    UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_REPAY_SLIP_HIST_DAT \
add constraint PK_COL_REPAY_HIST \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, REPAY_SLIP_NO, REPAY_SEQ)
