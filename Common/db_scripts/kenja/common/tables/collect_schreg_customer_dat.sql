-- kanji=漢字
-- $Id: 8669a27e986f8fff8800eabfba348610a432f156 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table COLLECT_SCHREG_CUSTOMER_DAT

create table COLLECT_SCHREG_CUSTOMER_DAT \
( \
    SCHREGNO            varchar(8)  not null, \
    CUSTOMER_NUMBER     varchar(20), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SCHREG_CUSTOMER_DAT \
add constraint PK_COLL_SCH_CUS_D \
primary key \
(SCHREGNO)
