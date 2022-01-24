-- kanji=漢字
-- $Id: 8a15952d3592babfdae636de211a17b7aea00ab4 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table HTRAINREMARK_HDAT_OLD
create table HTRAINREMARK_HDAT_OLD like HTRAINREMARK_HDAT
insert into  HTRAINREMARK_HDAT_OLD select * from HTRAINREMARK_HDAT

drop table HTRAINREMARK_HDAT

create table HTRAINREMARK_HDAT \
    (SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(534), \
     TOTALSTUDYVAL        varchar(802), \
     TOTALSTUDYACT2       varchar(534), \
     TOTALSTUDYVAL2       varchar(802), \
     CREDITREMARK         varchar(802), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HTRAINREMARK_HDAT \
add constraint PK_HTRAINREMARK_H \
primary key \
(SCHREGNO)

insert into  HTRAINREMARK_HDAT \
(select \
    SCHREGNO, \
    TOTALSTUDYACT, \
    TOTALSTUDYVAL, \
    TOTALSTUDYACT2, \
    TOTALSTUDYVAL2, \
    CAST(NULL AS VARCHAR(802)), \
    REGISTERCD, \
    UPDATED \
 from HTRAINREMARK_HDAT_OLD)

