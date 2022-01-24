-- kanji=漢字
-- $Id: 8b1fc554efcb89074ebbfb4258df20d4bdec4b26 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table FACILITY_GATE_DAT

create table FACILITY_GATE_DAT ( \
     FACCD          varchar(4) not null, \
     GATENO         varchar(4) not null, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table FACILITY_GATE_DAT add constraint PK_FAC_GATE_DAT primary key (FACCD, GATENO)

