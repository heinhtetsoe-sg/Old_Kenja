-- kanji=漢字
-- $Id: belonging_staff_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table BELONGING_STAFF_DAT

create table BELONGING_STAFF_DAT \
      (YEAR             varchar(4)      not null, \
       BELONGING_DIV    varchar(3)      not null, \
       STAFFCD          varchar(8)      not null, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table BELONGING_STAFF_DAT add constraint PK_BELONGING_STAFF primary key \
      (YEAR, BELONGING_DIV, STAFFCD)


