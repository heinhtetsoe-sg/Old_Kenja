-- kanji=漢字
-- $Id: credit_subclass_combined_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CREDIT_SUBCLASS_COMBINED_DAT

create table CREDIT_SUBCLASS_COMBINED_DAT \
(  \
        GET_METHOD            varchar(1) not null, \
        CREDIT_CURRICULUM_CD  varchar(1) not null, \
        CREDIT_ADMITSCD       varchar(6) not null, \
        CLASSCD               varchar(2) not null, \
        CURRICULUM_CD         varchar(1) not null, \
        SUBCLASSCD            varchar(6) not null, \
        CREDIT                smallint, \
        REGISTERCD            varchar(8), \
        UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CREDIT_SUBCLASS_COMBINED_DAT  \
add constraint PK_CREDIT_SUB_COMB \
primary key  \
(GET_METHOD, CREDIT_CURRICULUM_CD, CREDIT_ADMITSCD)
