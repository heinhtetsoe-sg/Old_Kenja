-- kanji=漢字
-- $Id: 67f6c691ce6f75cdc7c2c01ec2f20faba1c51b23 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table GUARDIAN_COMMITTEE_YDAT

create table GUARDIAN_COMMITTEE_YDAT ( \
    YEAR                varchar(4) not null, \
    DIV                 varchar(1) not null, \
    EXECUTIVECD         varchar(2) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GUARDIAN_COMMITTEE_YDAT add constraint PK_G_COM_YDAT primary key (YEAR, DIV, EXECUTIVECD)
