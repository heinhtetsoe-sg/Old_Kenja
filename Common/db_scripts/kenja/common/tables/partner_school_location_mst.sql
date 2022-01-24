-- $Id: ee717304d463c4b96e4d45fefec9837c762ac152 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table PARTNER_SCHOOL_LOCATION_MST

create table PARTNER_SCHOOL_LOCATION_MST( \
     DISTRICTCD          VARCHAR(5)     NOT NULL, \
     DISTRICT_NAME       VARCHAR(75)    NOT NULL, \
     DISTRICT_NAME_ABBV  VARCHAR(75), \
     REGISTERCD          VARCHAR(10), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table PARTNER_SCHOOL_LOCATION_MST add constraint pk_prtnsch_locat_m primary key \
    (DISTRICTCD)
