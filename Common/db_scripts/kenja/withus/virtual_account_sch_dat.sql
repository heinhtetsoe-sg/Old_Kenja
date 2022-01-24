-- kanji=漢字
-- $Id: virtual_account_sch_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table VIRTUAL_ACCOUNT_SCH_DAT

create table VIRTUAL_ACCOUNT_SCH_DAT \
(  \
        APPLICANTNO         varchar(7) not null, \
        ADJUST_SDATE        date not null, \
        ADJUST_EDATE        date, \
        VIRTUAL_BANK_CD     varchar(3), \
        VIRTUAL_ACCOUNT_NO  varchar(7), \
        REGISTERCD          varchar(8), \
        UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table VIRTUAL_ACCOUNT_SCH_DAT  \
add constraint PK_VIRTUAL_SCH \
primary key  \
(APPLICANTNO, ADJUST_SDATE)
