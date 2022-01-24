-- kanji=漢字
-- $Id: rep-htrainremark_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


drop table TMP_HTRAINREMARK_DAT

create table TMP_HTRAINREMARK_DAT \
    (YEAR                 varchar(4) not null, \
     SCHREGNO             varchar(8) not null, \
     ANNUAL               varchar(2) not null, \
     TOTALSTUDYACT        varchar(802), \
     TOTALSTUDYVAL        varchar(802), \
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

insert into TMP_HTRAINREMARK_DAT \
select \
    YEAR, \
    SCHREGNO, \
    ANNUAL, \
    TOTALSTUDYACT, \
    TOTALSTUDYVAL, \
    SPECIALACTREMARK, \
    TOTALREMARK     , \
    ATTENDREC_REMARK, \
    VIEWREMARK, \
    BEHAVEREC_REMARK, \
    CLASSACT, \
    STUDENTACT, \
    CLUBACT, \
    SCHOOLEVENT, \
    REGISTERCD  , \
    UPDATED      \
from \
    HTRAINREMARK_DAT

drop table HTRAINREMARK_DAT_OLD

rename table HTRAINREMARK_DAT to HTRAINREMARK_DAT_OLD

rename table TMP_HTRAINREMARK_DAT to HTRAINREMARK_DAT

alter table HTRAINREMARK_DAT \
add constraint PK_HTRAINREMARK \
primary key \
(YEAR,SCHREGNO)


