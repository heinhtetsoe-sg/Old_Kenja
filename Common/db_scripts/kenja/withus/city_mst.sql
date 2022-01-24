-- kanji=漢字
-- $Id: city_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table CITY_MST

create table CITY_MST \
	(PREF_CD        varchar(2)   not null, \
     CITY_CD        varchar(3)   not null, \
     CITY_NAME      varchar(120)  not null, \
     CITY_NAME_KANA varchar(120)  not null, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CITY_MST add constraint PK_CITY_MST primary key \
      (PREF_CD, CITY_CD)
