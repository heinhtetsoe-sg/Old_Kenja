-- kanji=漢字
-- $Id: subclass_replace_combined_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_REPLACE_COMBINED_DAT

create table SUBCLASS_REPLACE_COMBINED_DAT \
    (REPLACECD                varchar(1) not null, \
     YEAR                     varchar(4) not null, \
     COMBINED_CLASSCD         varchar(2) not null, \
     COMBINED_CURRICULUM_CD   varchar(1) not null, \
     COMBINED_SUBCLASSCD      varchar(6) not null, \
     ATTEND_CLASSCD           varchar(2) not null, \
     ATTEND_CURRICULUM_CD     varchar(1) not null, \
     ATTEND_SUBCLASSCD        varchar(6) not null, \
     CALCULATE_CREDIT_FLG     varchar(1) , \
     STUDYREC_CREATE_FLG      varchar(1) , \
     PRINT_FLG1               varchar(1) , \
     PRINT_FLG2               varchar(1) , \
     PRINT_FLG3               varchar(1) , \
     REGISTERCD               varchar(8), \
     UPDATED                  timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SUBCLASS_REPLACE_COMBINED_DAT add constraint PK_SUBREPCOMB primary key (YEAR,COMBINED_CLASSCD,COMBINED_CURRICULUM_CD,COMBINED_SUBCLASSCD,ATTEND_CLASSCD,ATTEND_CURRICULUM_CD,ATTEND_SUBCLASSCD)
