-- kanji=漢字
-- $Id: 051fbad2095675f011325da9479825e0bd251a71 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HOUSE_GROUP_MST

create table HOUSE_GROUP_MST \
(  \
    HOUSE_GROUP_CD      varchar(3) not null, \
    HOUSE_GROUP_NAME    varchar(60), \
    REMARK1             varchar(150), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HOUSE_GROUP_MST add constraint PK_HOUSE_GROUP_MST \
primary key (HOUSE_GROUP_CD)
