-- kanji=漢字
-- $Id: ba7f435f4386042d07b50a4c2d0206e7210951fc $

-- Revの1.1を1.5に変換する。
-- 作成日: 2007/03/30
-- 作成者: m-yama

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

DROP TABLE FRESHMAN_DAT_BK
CREATE TABLE FRESHMAN_DAT_BK LIKE FRESHMAN_DAT
insert into FRESHMAN_DAT_BK select * from FRESHMAN_DAT

drop table FRESHMAN_DAT
create table FRESHMAN_DAT \
      (ENTERYEAR           varchar(4)      not null, \
       SCHREGNO            varchar(8)      not null, \
       ENT_DIV             varchar(1), \
       HR_CLASS            varchar(3), \
       ATTENDNO            varchar(3), \
       INOUTCD             varchar(1), \
       COURSECD            varchar(1), \
       MAJORCD             varchar(3), \
       COURSECODE          varchar(4), \
       NAME                varchar(60), \
       NAME_KANA           varchar(120), \
       BIRTHDAY            date, \
       SEX                 varchar(1), \
       FINSCHOOLCD         varchar(7), \
       FINSCHOOLGRADDATE   date, \
       ZIPCD               varchar(8), \
       ADDR1               varchar(75), \
       ADDR2               varchar(75), \
       TELNO               varchar(14), \
       FAXNO               varchar(14), \
       EMAIL               varchar(20), \
       EMERGENCYCALL       varchar(60), \
       EMERGENCYTELNO      varchar(14), \
       SCALASHIPDIV        varchar(2), \
       REGISTERCD          varchar(8), \
       UPDATED             timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table FRESHMAN_DAT add constraint pk_freshman_dat primary key \
      (enteryear, schregno) 



insert into FRESHMAN_DAT \
select \
        ENTERYEAR, \
        SCHREGNO, \
        ENT_DIV, \
        HR_CLASS, \
        ATTENDNO, \
        INOUTCD, \
        COURSECD, \
        MAJORCD, \
        cast(null as varchar(4)), \
        NAME, \
        NAME_KANA, \
        BIRTHDAY, \
        SEX, \
        FINSCHOOLCD, \
        FINSCHOOLGRADDATE, \
        ZIPCD, \
        ADDR1, \
        ADDR2, \
        TELNO, \
        FAXNO, \
        EMAIL, \
        EMERGENCYCALL, \
        EMERGENCYTELNO, \
        cast(null as varchar(2)), \
        REGISTERCD, \
        UPDATED \
from FRESHMAN_DAT_BK
