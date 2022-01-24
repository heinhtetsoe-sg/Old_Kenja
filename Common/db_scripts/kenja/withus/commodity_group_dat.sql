-- kanji=漢字
-- $Id: commodity_group_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table COMMODITY_GROUP_DAT

create table COMMODITY_GROUP_DAT \
(  \
    COMMODITY_CD    varchar(5) not null, \
    GROUP_CD        varchar(2) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMMODITY_GROUP_DAT \
add constraint PK_COMMODITY_G_DAT \
primary key  \
(COMMODITY_CD)
