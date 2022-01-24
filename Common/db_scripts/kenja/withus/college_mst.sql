-- kanji=漢字
-- $Id: college_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 大学・専門学校マスタ
-- 作成日: 2006/06/14 15:21:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

drop table COLLEGE_MST

create table COLLEGE_MST ( \
    SCHOOL_CD           char(11) not null, \
    SCHOOL_NAME         varchar(120), \
    BUNAME              varchar(120), \
    KANAME              varchar(120), \
    SCHOOL_SORT         char(2), \
    BUNYA               char(2), \
    AREA_NAME           varchar(30), \
    ZIPCD               varchar(8), \
    ADDR1               varchar(90), \
    ADDR2               varchar(90), \
    TELNO               varchar(16), \
    GREDES              varchar(120), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_MST add constraint PK_COLLEGE_MST primary key (SCHOOL_CD)
