-- kanji=漢字
-- $Id: schooling_type_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHOOLING_TYPE_MST

create table SCHOOLING_TYPE_MST \
(  \
    SCHOOLING_TYPE  varchar(2) not null, \
    SCHOOLING_DIV   varchar(2) not null, \
    NAME            varchar(60) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHOOLING_TYPE_MST  \
add constraint PK_SCHOOLING_TYPE \
primary key  \
(SCHOOLING_TYPE)
