-- kanji=漢字
-- $Id: 53bdfde2456db3d9178844ee0dc7f6205a652082 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_ABSENCE_MONTH_REMARK_DAT

create table ATTEND_ABSENCE_MONTH_REMARK_DAT \
        (YEAR               varchar(4) not null, \
         MONTH              varchar(2) not null, \
         SEMESTER           varchar(1) not null, \
         SCHREGNO           varchar(8) not null, \
         SEQ                smallint   not null, \
         DI_CD              varchar(2),  \
         TOTAL_DAY          varchar(3),  \
         REMARK             varchar(60), \
         TREATMENT          varchar(90), \
         REGISTERCD         varchar(10), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_ABSENCE_MONTH_REMARK_DAT add constraint PK_AT_AB_MON_RE_D primary key \
        (YEAR, MONTH, SEMESTER, SCHREGNO, SEQ)
