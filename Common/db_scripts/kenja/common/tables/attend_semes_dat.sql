-- kanji=漢字
-- $Id: 769fab5302d9a2c78c366fd51ce8f0630d746c4c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_SEMES_DAT

create table ATTEND_SEMES_DAT \
        (COPYCD             varchar(1)      not null, \
         YEAR               varchar(4)      not null, \
         MONTH              varchar(2)      not null, \
         SEMESTER           varchar(1)      not null, \
         SCHREGNO           varchar(8)      not null, \
         APPOINTED_DAY      varchar(2), \
         LESSON             smallint, \
         OFFDAYS            smallint, \
         ABSENT             smallint, \
         SUSPEND            smallint, \
         MOURNING           smallint, \
         ABROAD             smallint, \
         SICK               smallint, \
         NOTICE             smallint, \
         NONOTICE           smallint, \
         LATE               smallint, \
         EARLY              smallint, \
         KEKKA_JISU         smallint, \
         KEKKA              smallint, \
         LATEDETAIL         smallint, \
         VIRUS              smallint, \
         KOUDOME            SMALLINT, \
         REGISTERCD         varchar(10), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_SEMES_DAT add constraint pk_attsemes_dat primary key \
        (COPYCD, YEAR, SEMESTER,MONTH, SCHREGNO)


