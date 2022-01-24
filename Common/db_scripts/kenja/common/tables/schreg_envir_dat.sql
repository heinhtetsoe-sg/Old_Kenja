-- kanji=漢字
-- $Id: c24bd098922f5060c269bee14cc45e843202f489 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHREG_ENVIR_DAT

create table SCHREG_ENVIR_DAT \
   (SCHREGNO           varchar(8)  not null, \
    GO_HOME_GROUP_NO   varchar(2),   \
    RESPONSIBILITY     varchar(1),   \
    HOWTOCOMMUTECD     varchar(1),   \
    UP_DOWN            varchar(1),   \
    COMMUTE_HOURS      varchar(2),   \
    COMMUTE_MINUTES    varchar(2),   \
    OTHERHOWTOCOMMUTE  varchar(30),  \
    STATIONNAME        varchar(30),  \
    JOSYA_1            varchar(45),  \
    ROSEN_1            varchar(45),  \
    GESYA_1            varchar(45),  \
    FLG_1              varchar(1),   \
    JOSYA_2            varchar(45),  \
    ROSEN_2            varchar(45),  \
    GESYA_2            varchar(45),  \
    FLG_2              varchar(1),   \
    JOSYA_3            varchar(45),  \
    ROSEN_3            varchar(45),  \
    GESYA_3            varchar(45),  \
    FLG_3              varchar(1),   \
    JOSYA_4            varchar(45),  \
    ROSEN_4            varchar(45),  \
    GESYA_4            varchar(45),  \
    FLG_4              varchar(1),   \
    JOSYA_5            varchar(45),  \
    ROSEN_5            varchar(45),  \
    GESYA_5            varchar(45),  \
    FLG_5              varchar(1),   \
    JOSYA_6            varchar(45),  \
    ROSEN_6            varchar(45),  \
    GESYA_6            varchar(45),  \
    FLG_6              varchar(1),   \
    JOSYA_7            varchar(45),  \
    ROSEN_7            varchar(45),  \
    GESYA_7            varchar(45),  \
    FLG_7              varchar(1),   \
    BRO_SISCD          varchar(1),   \
    RESIDENTCD         varchar(1),   \
    ATTENTIONMATTERS   varchar(30),  \
    DISEASE            varchar(30),  \
    HEALTHCONDITION    varchar(30),  \
    MERITS             varchar(63),  \
    DEMERITS           varchar(63),  \
    OLD_CRAM           varchar(63),  \
    CUR_CRAMCD         varchar(1),   \
    CUR_CRAM           varchar(30),  \
    LESSONCD           varchar(1),   \
    LESSON             varchar(30),  \
    BEDTIME_HOURS      varchar(2),   \
    BEDTIME_MINUTES    varchar(2),   \
    RISINGTIME_HOURS   varchar(2),   \
    RISINGTIME_MINUTES varchar(2),   \
    STUDYTIME          varchar(1),   \
    POCKETMONEYCD      varchar(1),   \
    POCKETMONEY        smallint,     \
    TVVIEWINGHOURSCD   varchar(1),   \
    TVPROGRAM          varchar(30),  \
    PC_HOURS           varchar(1),   \
    GOOD_SUBJECT       varchar(63),  \
    BAD_SUBJECT        varchar(63),  \
    HOBBY              varchar(63),  \
    PRIZES             varchar(129), \
    READING            varchar(63),  \
    SPORTS             varchar(63),  \
    FRIENDSHIP         varchar(63),  \
    PLANUNIV           varchar(63),  \
    PLANJOB            varchar(63),  \
    ED_ACT             varchar(63),  \
    REMARK             varchar(129), \
    REMARK1            varchar(60),  \
    REMARK2            varchar(5),   \
    REGISTERCD         varchar(10),   \
    UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_ENVIR_DAT add constraint pk_sch_envir_dat primary key \
      (SCHREGNO)
