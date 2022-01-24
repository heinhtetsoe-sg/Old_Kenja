-- kanji=����
-- $Id: rep-fin_junior_highschool_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop   table FIN_JUNIOR_HIGHSCHOOL_MST_OLD
create table FIN_JUNIOR_HIGHSCHOOL_MST_OLD like FIN_JUNIOR_HIGHSCHOOL_MST
insert into FIN_JUNIOR_HIGHSCHOOL_MST_OLD select * from FIN_JUNIOR_HIGHSCHOOL_MST

drop   table FIN_JUNIOR_HIGHSCHOOL_MST

create table FIN_JUNIOR_HIGHSCHOOL_MST \
   (SCHOOL_CD       varchar(11) not null, \
    FINSCOOL_DISTCD varchar(3), \
    NAME            varchar(120), \
    GAKUBU_KANJI    varchar(30), \
    NAME_KANA       varchar(150), \
    GAKUBU_KANA     varchar(30), \
    MW_DIV          varchar(1), \
    DN_DIV          varchar(1), \
    ZIPCD           varchar(8), \
    PREF            varchar(75), \
    CITY            varchar(120), \
    TOWN            varchar(150), \
    ADDR1           varchar(75), \
    ADDR2           varchar(75), \
    ADDR3           varchar(75), \
    PREF_CD         varchar(2), \
    CITY_CD         varchar(3), \
    URL             varchar(80), \
    TELLNO          varchar(14), \
    FAXNO           varchar(14), \
    COMPENDIUM_YEAR varchar(4), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table FIN_JUNIOR_HIGHSCHOOL_MST add constraint PK_FIN_JUNIOR_MST primary key (SCHOOL_CD)

insert into FIN_JUNIOR_HIGHSCHOOL_MST \
select \
    SCHOOL_CD, \
    cast(null as varchar(3)), \
    NAME, \
    GAKUBU_KANJI, \
    NAME_KANA, \
    GAKUBU_KANA, \
    MW_DIV, \
    DN_DIV, \
    ZIPCD, \
    PREF, \
    CITY, \
    TOWN, \
    ADDR1, \
    ADDR2, \
    ADDR3, \
    PREF_CD, \
    CITY_CD, \
    URL, \
    TELLNO, \
    FAXNO, \
    COMPENDIUM_YEAR, \
    REGISTERCD, \
    UPDATED \
from FIN_JUNIOR_HIGHSCHOOL_MST_OLD
