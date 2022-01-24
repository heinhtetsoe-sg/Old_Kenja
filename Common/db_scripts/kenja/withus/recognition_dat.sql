-- kanji=漢字
-- $Id: recognition_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
--drop table RECOGNITION_DAT

--create table RECOGNITION_DAT \
--    (APPLICANTNO              varchar(4) not null, \
--    RECOGNITION_CLASSCD       varchar(2) not null, \
--    RECOGNITION_SUBCLASSCD    varchar(6) not null, \
--    RECOGNITION_SUBCLASSNAME  varchar(60), \
--    RECOGNITION_SUBCLASSABBV  varchar(15), \
--    SUBCLASSCD                varchar(6), \
--    RECOGNITION_CREDIT        varchar(2), \
--    REGISTERCD                varchar(8), \
--    UPDATED                   timestamp default current timestamp \
--      ) in usr1dms index in idx1dms

--alter table RECOGNITION_DAT add constraint PK_RECOGNITION_DAT primary key \
--      (APPLICANTNO, RECOGNITION_CLASSCD)
