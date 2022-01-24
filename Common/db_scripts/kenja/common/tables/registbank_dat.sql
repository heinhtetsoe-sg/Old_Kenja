-- kanji=漢字
-- $Id: 7f9a9b60f215659a7e0c17a6acc1e3a5a99f9656 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--登録銀行データ
drop table REGISTBANK_DAT

create table REGISTBANK_DAT \
( \
        "SCHOOLCD"       varchar(12) not null, \
        "SCHREGNO"       varchar(8)  not null, \
        "SEQ"            varchar(1)  not null, \
        "BANKCD"         varchar(4),  \
        "BRANCHCD"       varchar(3),  \
        "DEPOSIT_ITEM"   varchar(1),  \
        "ACCOUNTNO"      varchar(7),  \
        "ACCOUNTNAME"    varchar(120), \
        "RELATIONSHIP"   varchar(2),  \
        "PAID_INFO_CD"   varchar(2),  \
        "REGISTERCD"     varchar(10),  \
        "UPDATED"        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REGISTBANK_DAT add constraint PK_REGISTBANK_DAT primary key (SCHOOLCD, SCHREGNO, SEQ)
