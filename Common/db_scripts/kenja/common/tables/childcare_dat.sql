-- kanji=漢字
-- $Id: afcdf0322075d16e0e2c04944e09f1e406a66042 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table CHILDCARE_DAT

create table CHILDCARE_DAT \
    (YEAR                   varchar(4) not null, \
     SCHREGNO               varchar(8) not null, \
     CARE_DATE              date not null, \
     FARE_CD                varchar(2), \
     PICK_UP                varchar(300), \
     REMARK                 varchar(300), \
     EXTRACURRICULAR_CLASS  varchar(300), \
     REGISTERCD             varchar(10), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CHILDCARE_DAT add constraint PK_CHILDCARE_DAT primary key (YEAR, SCHREGNO, CARE_DATE)


