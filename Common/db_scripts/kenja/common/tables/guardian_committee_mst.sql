-- kanji=漢字
-- $Id: 0151c5621a55d7f745bd11eba53ca491cf4bce80 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table GUARDIAN_COMMITTEE_MST

create table GUARDIAN_COMMITTEE_MST ( \
    DIV                 varchar(1) not null, \
    EXECUTIVECD         varchar(2) not null, \
    NAME                varchar(75), \
    ABBV                varchar(75), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GUARDIAN_COMMITTEE_MST add constraint PK_G_COM_MST primary key (DIV, EXECUTIVECD)
