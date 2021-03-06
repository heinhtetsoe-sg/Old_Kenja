-- kanji=漢字
-- $Id: levy_request_outgo_schreg_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金支出伺い生徒データ

drop table LEVY_REQUEST_OUTGO_SCHREG_DAT

create table LEVY_REQUEST_OUTGO_SCHREG_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "OUTGO_L_CD"            varchar(2)  not null, \
        "OUTGO_M_CD"            varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "OUTGO_S_CD"            varchar(2)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "LINE_NO"               smallint    not null, \
        "OUTGO_DATE"            date, \
        "OUTGO_NO"              varchar(10), \
        "OUTGO_MONEY"           integer, \
        "OUTGO_CERTIFICATE_CNT" varchar(10), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_OUTGO_SCHREG_DAT add constraint PK_LEVY_OUT_SCH primary key (YEAR, OUTGO_L_CD, OUTGO_M_CD, REQUEST_NO, OUTGO_S_CD, SCHREGNO)
