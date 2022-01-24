-- kanji=漢字
-- $Id: 4982bcf0de73e071705d900626221d32dbe92ce2 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table HEALTH_SPORT_PRINT_HIST_DAT

create table HEALTH_SPORT_PRINT_HIST_DAT \
    (YEAR               varchar(4) not null, \
     SCHOOLCD           varchar(12) not null, \
     SCHOOL_KIND        varchar(2) not null, \
     SCHREGNO           varchar(8) not null, \
     SEQ                smallint not null, \
     SEND_TO1           varchar(120), \
     SEND_TO2           varchar(120), \
     SEND_DATE          date, \
     REMARK1            varchar(120), \
     REMARK2            varchar(120), \
     REMARK3            varchar(120), \
     REMARK4            varchar(120), \
     REMARK5            varchar(120), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEALTH_SPORT_PRINT_HIST_DAT add constraint PK_HEALTH_SPHIST primary key (YEAR, SCHOOLCD, SCHOOL_KIND, SCHREGNO, SEQ)
