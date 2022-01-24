-- kanji=漢字
-- $Id: ceeb2ddd76ea3356601a059dd26831575e4b39a6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金給付生徒データ

drop table LEVY_REQUEST_BENEFIT_SCHREG_DAT

create table LEVY_REQUEST_BENEFIT_SCHREG_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "BENEFIT_MONEY"         integer, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_BENEFIT_SCHREG_DAT add constraint PK_LEVY_BENE_SCH primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO)
