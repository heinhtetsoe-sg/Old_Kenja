-- kanji=漢字
-- $Id: schreg_studyrec_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--


drop   table SCHREG_STUDYREC_DAT

create table SCHREG_STUDYREC_DAT \
        (SCHOOLCD         varchar(1) not null, \
         YEAR             varchar(4) not null, \
         SCHREGNO         varchar(8) not null, \
         ANNUAL           varchar(2) not null, \
         CLASSCD          varchar(2) not null, \
         SUBCLASSCD       varchar(6) not null, \
         CURRICULUM_CD    varchar(1) not null, \
         CLASSNAME        varchar(30), \
         CLASSABBV        varchar(15), \
         CLASSNAME_ENG    varchar(40), \
         CLASSABBV_ENG    varchar(30), \
         SUBCLASSES       smallint, \
         SUBCLASSNAME     varchar(60), \
         SUBCLASSABBV     varchar(15), \
         SUBCLASSNAME_ENG varchar(40), \
         SUBCLASSABBV_ENG varchar(20), \
         VALUATION        smallint, \
         GET_CREDIT       smallint, \
         ADD_CREDIT       smallint, \
         COMP_CREDIT      smallint, \
         REMARK           varchar(90), \
         REGISTERCD       varchar(8), \
         UPDATED          timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCHREG_STUDYREC_DAT add constraint PK_SCHREG_STUDYREC primary key (SCHOOLCD, YEAR, SCHREGNO, ANNUAL, CLASSCD, SUBCLASSCD, CURRICULUM_CD)

