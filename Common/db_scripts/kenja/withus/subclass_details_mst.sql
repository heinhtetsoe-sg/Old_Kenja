-- kanji=漢字
-- $Id: subclass_details_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SUBCLASS_DETAILS_MST

create table SUBCLASS_DETAILS_MST \
    (YEAR           varchar(4) not null, \
     CLASSCD        varchar(2) not null, \
     CURRICULUM_CD  varchar(1) not null, \
     SUBCLASSCD     varchar(6) not null, \
     CREDITS        smallint, \
     INOUT_DIV      varchar(1) not null, \
     REQUIRE_FLG    varchar(1) not null, \
     SCHOOLING_SEQ  smallint, \
     REPORT_SEQ     smallint, \
     TEST_FLG       varchar(1), \
     REPORT_RATE    smallint, \
     SCORE_RATE     smallint, \
     HYOUJUN_RATE   smallint, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SUBCLASS_DETAILS_MST add constraint PK_SUB_DETAILS_MST primary key \
      (YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD)
