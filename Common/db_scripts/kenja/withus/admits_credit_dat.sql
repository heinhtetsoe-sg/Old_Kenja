-- kanji=漢字
-- $Id: admits_credit_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ADMITS_CREDIT_DAT

create table ADMITS_CREDIT_DAT \
(  \
    GET_METHOD          varchar(1) not null, \
    CURRICULUM_CD       varchar(1) not null, \
    ADMITSCD            varchar(6) not null, \
    ADMITSNAME          varchar(60), \
    ADMITSABBV          varchar(15), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMITS_CREDIT_DAT  \
add constraint PK_ADMITS_CREDIT_D \
primary key  \
(GET_METHOD, CURRICULUM_CD, ADMITSCD)
