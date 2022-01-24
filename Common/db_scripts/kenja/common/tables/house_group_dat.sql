-- kanji=漢字
-- $Id: 5480f18ea1a0fca5a8ee812425618742b97eedfb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HOUSE_GROUP_DAT

create table HOUSE_GROUP_DAT \
(  \
    YEAR                varchar(4) not null, \
    SCHREGNO            varchar(8) not null, \
    HOUSE_GROUP_CD      varchar(3) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HOUSE_GROUP_DAT add constraint PK_HOUSE_GROUP_DAT \
primary key (YEAR, SCHREGNO)
