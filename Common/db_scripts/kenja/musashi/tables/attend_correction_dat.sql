-- kanji=漢字
-- $Id: attend_correction_dat.sql 59752 2018-04-16 13:39:03Z yamashiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_CORRECTION_DAT

create table ATTEND_CORRECTION_DAT \
        (YEAR               varchar(4)      not null, \
         SEMESTER           varchar(1)      not null, \
         SCHREGNO           varchar(8)      not null, \
         LATEDETAIL         smallint, \
         EARLYDETAIL        smallint, \
         KEKKADETAIL        smallint, \
         REGISTERCD         varchar(10), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_CORRECTION_DAT add constraint pk_att_correc_dat primary key \
        (YEAR, SEMESTER, SCHREGNO)
