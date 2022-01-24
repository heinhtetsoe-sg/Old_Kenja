-- $Id: rep-subclass_details_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SUBCLASS_DETAILS_MST_OLD
create table SUBCLASS_DETAILS_MST_OLD like SUBCLASS_DETAILS_MST
insert into  SUBCLASS_DETAILS_MST_OLD select * from SUBCLASS_DETAILS_MST

drop   table SUBCLASS_DETAILS_MST
create table SUBCLASS_DETAILS_MST ( \
    YEAR           varchar(4) not null, \
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

insert into SUBCLASS_DETAILS_MST \
  select \
     YEAR, \
     CLASSCD, \
     CURRICULUM_CD, \
     SUBCLASSCD, \
     CREDITS, \
     INOUT_DIV, \
     REQUIRE_FLG, \
     SCHOOLING_SEQ, \
     REPORT_SEQ, \
     TEST_FLG, \
     CASE WHEN YEAR < '2010' \
          THEN CASE WHEN VALUE(TEST_FLG, '0') = '1' \
                    THEN CASE WHEN VALUE(REPORT_SEQ, 0) > 0 \
                              THEN 50 \
                              ELSE 0 \
                         END \
                    ELSE CASE WHEN VALUE(REPORT_SEQ, 0) > 0 \
                              THEN 70 \
                              ELSE 0 \
                         END \
               END \
          ELSE CASE WHEN VALUE(TEST_FLG, '0') = '1' \
                    THEN CASE WHEN VALUE(REPORT_SEQ, 0) > 0 \
                              THEN 50 \
                              ELSE 0 \
                         END \
                    ELSE CASE WHEN VALUE(REPORT_SEQ, 0) > 0 \
                              THEN 100 \
                              ELSE 0 \
                         END \
               END \
     END AS REPORT_RATE, \
     CASE WHEN YEAR < '2010' \
          THEN CASE WHEN VALUE(TEST_FLG, '0') = '1' \
                    THEN CASE WHEN VALUE(REPORT_SEQ, 0) > 0 \
                              THEN 20 \
                              ELSE 70 \
                         END \
                    ELSE 0 \
               END \
          ELSE CASE WHEN VALUE(TEST_FLG, '0') = '1' \
                    THEN CASE WHEN VALUE(REPORT_SEQ, 0) > 0 \
                              THEN 50 \
                              ELSE 100 \
                         END \
                    ELSE 0 \
               END \
     END AS SCORE_RATE, \
     CASE WHEN YEAR < '2010' \
          THEN 30 \
          ELSE 0 \
     END AS HYOUJUN_RATE, \
     REGISTERCD, \
     UPDATED \
  from SUBCLASS_DETAILS_MST_OLD
