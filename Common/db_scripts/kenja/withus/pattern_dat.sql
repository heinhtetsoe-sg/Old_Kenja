-- kanji=漢字
-- $Id: pattern_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table PATTERN_DAT

create table PATTERN_DAT \
(  \
    PATTERN_CD     varchar(3) not null, \
    LINE_NO        varchar(2) not null, \
    COMMODITY_CD   varchar(5), \
    AMOUNT         varchar(2), \
    PRIORITY_LEVEL varchar(2), \
    AMOUNT_DIV     varchar(1), \
    REGISTERCD     varchar(8), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PATTERN_DAT  \
add constraint PK_PATTERN_DAT  \
primary key  \
(PATTERN_CD, LINE_NO)
