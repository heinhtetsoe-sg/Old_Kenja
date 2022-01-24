-- kanji=漢字
-- $Id: 0d66381442b0d1d7a6328c8f0e0db01960c621ef $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_SEMES_SUBM_DAT

create table ATTEND_SEMES_SUBM_DAT \
        (COPYCD             varchar(1)      not null, \
         YEAR               varchar(4)      not null, \
         MONTH              varchar(2)      not null, \
         SEMESTER           varchar(1)      not null, \
         SCHREGNO           varchar(8)      not null, \
         DI_CD              varchar(2)      not null, \
         SUBL_CD            varchar(3)      not null, \
         SUBM_CD            varchar(3)      not null, \
         CNT                smallint, \
         REGISTERCD         varchar(8), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_SEMES_SUBM_DAT add constraint pk_at_sem_subm_dat primary key \
        (COPYCD, YEAR, MONTH, SEMESTER, SCHREGNO, DI_CD, SUBL_CD, SUBM_CD)


