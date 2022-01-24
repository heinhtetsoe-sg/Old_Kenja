-- kanji=漢字
-- $Id: 3116e482fac8ed1d46aad83d558b47e7d756ffd5 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_LESSON_MST

create table ATTEND_LESSON_MST \
        (YEAR               varchar(4)      not null, \
         MONTH              varchar(2)      not null, \
         SEMESTER           varchar(1)      not null, \
         GRADE              varchar(2)      not null, \
         COURSECD           varchar(1)      not null, \
         MAJORCD            varchar(3)      not null, \
         LESSON             smallint        not null, \
         REGISTERCD         varchar(10), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table ATTEND_LESSON_MST add constraint pk_attsemes_dat primary key \
        (YEAR, MONTH, SEMESTER, GRADE, COURSECD, MAJORCD)


