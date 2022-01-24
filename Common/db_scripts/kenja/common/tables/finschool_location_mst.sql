-- $Id: 291e957fa645b164acc615715901a072ffbf882c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table FINSCHOOL_LOCATION_MST

create table FINSCHOOL_LOCATION_MST( \
     DISTRICTCD          VARCHAR(5)     NOT NULL, \
     DISTRICT_NAME       VARCHAR(75)    NOT NULL, \
     DISTRICT_NAME_ABBV  VARCHAR(75), \
     REGISTERCD          VARCHAR(10), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table FINSCHOOL_LOCATION_MST add constraint pk_finsch_locat_m primary key \
    (DISTRICTCD)
