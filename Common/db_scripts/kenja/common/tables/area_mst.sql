-- kanji=漢字
-- $Id: 527e7b475755342384e40a71cb2a066310373505 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table AREA_MST

create table AREA_MST \
(  \
    AREA_CD           varchar(1)  not null, \
    AREA_NAME         varchar(45) not null, \
    REGISTERCD        varchar(8), \
    UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AREA_MST add constraint PK_AREA_MST \
primary key (AREA_CD)
