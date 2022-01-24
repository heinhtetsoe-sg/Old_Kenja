-- kanji=漢字
-- $Id: rep-htrainremark_hdat.sql 56577 2017-10-22 11:35:50Z maeshiro $

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
     TOTALSTUDYACT        varchar(1000), \
     TOTALSTUDYVAL        varchar(1000), \
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
    REGISTERCD, \
    UPDATED \
 from HTRAINREMARK_HDAT_OLD)

