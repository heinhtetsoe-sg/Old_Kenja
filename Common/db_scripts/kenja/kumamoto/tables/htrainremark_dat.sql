-- kanji=漢字
-- $Id: htrainremark_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


drop table HTRAINREMARK_DAT

create table HTRAINREMARK_DAT \
    (YEAR                 varchar(4) not null, \
     SCHREGNO             varchar(8) not null, \
     ANNUAL               varchar(2) not null, \
     TOTALSTUDYACT        varchar(266), \
     TOTALSTUDYVAL        varchar(460), \
     SPECIALACTREMARK     varchar(208), \
     TOTALREMARK          varchar(1361), \
     ATTENDREC_REMARK     varchar(122), \
     VIEWREMARK           varchar(226), \
     BEHAVEREC_REMARK     varchar(122), \
     CLASSACT             varchar(218), \
     STUDENTACT           varchar(218), \
     CLUBACT              varchar(218), \
     SCHOOLEVENT          varchar(218), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HTRAINREMARK_DAT \
add constraint PK_HTRAINREMARK \
primary key \
(YEAR,SCHREGNO)
