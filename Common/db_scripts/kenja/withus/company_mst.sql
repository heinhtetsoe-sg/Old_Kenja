-- kanji=漢字
-- $Id: company_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 会社マスタ
-- 作成日: 2006/06/14 15:21:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

drop   table COMPANY_MST

create table COMPANY_MST ( \
    COMPANY_CD          char(11) not null, \
    COMPANY_NAME        varchar(120), \
    SHUSHOKU_ADDR       varchar(120), \
    SHIHONKIN           varchar(17), \
    SONINZU             int, \
    TONINZU             int, \
    COMPANY_SORT        char(2), \
    TARGET_SEX          char(1), \
    ZIPCD               varchar(8), \
    ADDR1               varchar(90), \
    ADDR2               varchar(90), \
    TELNO               varchar(16), \
    REMARK              varchar(120), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMPANY_MST add constraint PK_COMPANY_MST primary key (COMPANY_CD)
