-- kanji=漢字
-- $Id: bf5000043039860c5c272cf6249eb912e4b3a824 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金支出伺い生徒データ

drop table LEVY_REQUEST_OUTGO_SCHREG_DAT

create table LEVY_REQUEST_OUTGO_SCHREG_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "LINE_NO"               smallint    not null, \
        "OUTGO_L_CD"            varchar(2)  not null, \
        "OUTGO_M_CD"            varchar(2)  not null, \
        "OUTGO_S_CD"            varchar(3)  not null, \
        "OUTGO_DATE"            date, \
        "OUTGO_NO"              varchar(10), \
        "OUTGO_MONEY"           integer, \
        "OUTGO_CERTIFICATE_CNT" varchar(10), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_OUTGO_SCHREG_DAT add constraint PK_LEVY_OUT_SCH primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REQUEST_NO, SCHREGNO, LINE_NO, OUTGO_L_CD, OUTGO_M_CD, OUTGO_S_CD)
