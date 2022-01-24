-- kanji=漢字
-- $Id: b31d8b6f9b028b2044c6ea1261be490e6dab1685 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table CHILDCARE_BUS_DAT

create table CHILDCARE_BUS_DAT \
    (YEAR           varchar(4) not null, \
     SCHREGNO       varchar(8) not null, \
     CARE_DATE      DATE, \
     SCHEDULE_CD    varchar(1), \
     COURSE_CD      varchar(2), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CHILDCARE_BUS_DAT add constraint PK_CHILDCARE_BUS primary key (YEAR, SCHREGNO)


