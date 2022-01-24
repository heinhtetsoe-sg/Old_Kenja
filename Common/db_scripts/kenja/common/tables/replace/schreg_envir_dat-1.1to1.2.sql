-- kanji=漢字
-- $Id: d20efb08bac67d15501d29e01e5729b0b40d03e2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop   table TMP_SCHREG_ENVIR_DAT
create table TMP_SCHREG_ENVIR_DAT \
        (SCHREGNO           varchar(8)      not null, \
         HOWTOCOMMUTECD     varchar(1), \
         COMMUTE_HOURS      varchar(2), \
         COMMUTE_MINUTES    varchar(2), \
         OTHERHOWTOCOMMUTE  varchar(30), \
         STATIONNAME        varchar(30), \
         CHANGETRAIN1       varchar(7), \
         CHANGETRAIN2       varchar(7), \
         CHANGETRAIN3       varchar(7), \
         CHANGETRAIN4       varchar(7), \
         CHANGETRAIN5       varchar(7), \
         CHANGETRAIN6       varchar(7), \
         CHANGETRAIN7       varchar(7), \
         CHANGETRAIN8       varchar(7), \
         BRO_SISCD          varchar(1), \
         RESIDENTCD         varchar(1), \
         DISEASE            varchar(30), \
         HEALTHCONDITION    varchar(30), \
         MERITS             varchar(63), \
         DEMERITS           varchar(63), \
         OLD_CRAM           varchar(63), \
         CUR_CRAMCD         varchar(1), \
         CUR_CRAM           varchar(30), \
         LESSONCD           varchar(1), \
         LESSON             varchar(30), \
         BEDTIME_HOURS      varchar(2), \
         BEDTIME_MINUTES    varchar(2), \
         RISINGTIME_HOURS   varchar(2), \
         RISINGTIME_MINUTES varchar(2), \
         STUDYTIME          varchar(1), \
         POCKETMONEYCD      varchar(1), \
         POCKETMONEY        smallint, \
         TVVIEWINGHOURSCD   varchar(1), \
         TVPROGRAM          varchar(30), \
         PC_HOURS           varchar(1), \
         GOOD_SUBJECT       varchar(63), \
         BAD_SUBJECT        varchar(63), \
         HOBBY              varchar(63), \
         PRIZES             varchar(129), \
         READING            varchar(63), \
         SPORTS             varchar(63), \
         FRIENDSHIP         varchar(63), \
         PLANUNIV           varchar(63), \
         PLANJOB            varchar(63), \
         ED_ACT             varchar(63), \
         REMARK             varchar(129), \
         REGISTERCD         varchar(8), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

insert into TMP_SCHREG_ENVIR_DAT \
  select \
         SCHREGNO, \
         HOWTOCOMMUTECD, \
         cast(null as char(2)), \
         cast(null as char(2)), \
         OTHERHOWTOCOMMUTE, \
         cast(null as char(30)), \
         cast(null as char(7)), \
         cast(null as char(7)), \
         cast(null as char(7)), \
         cast(null as char(7)), \
         cast(null as char(7)), \
         cast(null as char(7)), \
         cast(null as char(7)), \
         cast(null as char(7)), \
         BRO_SISCD, \
         RESIDENTCD, \
         DISEASE, \
         HEALTHCONDITION, \
         MERITS, \
         DEMERITS, \
         OLD_CRAM, \
         CUR_CRAMCD, \
         CUR_CRAM, \
         LESSONCD, \
         LESSON, \
         substr(cast(BEDTIME as char(8)),1,2) as BEDTIME_HOURS, \
         substr(cast(BEDTIME as char(8)),4,2) as BEDTIME_MINUTES, \
         substr(cast(RISINGTIME as char(8)),1,2) as RISINGTIME_HOURS, \
         substr(cast(RISINGTIME as char(8)),4,2) as RISINGTIME_MINUTES, \
         STUDYTIME, \
         POCKETMONEYCD, \
         POCKETMONEY, \
         TVVIEWINGHOURSCD, \
         TVPROGRAM, \
         PC_HOURS, \
         GOOD_SUBJECT, \
         BAD_SUBJECT, \
         HOBBY, \
         PRIZES, \
         READING, \
         SPORTS, \
         FRIENDSHIP, \
         PLANUNIV, \
         PLANJOB, \
         ED_ACT, \
         REMARK, \
         REGISTERCD, \
         UPDATED \
  from SCHREG_ENVIR_DAT

drop table SCHREG_ENVIR_DAT_OLD

rename table     SCHREG_ENVIR_DAT to SCHREG_ENVIR_DAT_OLD

rename table TMP_SCHREG_ENVIR_DAT to SCHREG_ENVIR_DAT

alter table SCHREG_ENVIR_DAT add constraint pk_sch_envir_dat primary key (SCHREGNO)

