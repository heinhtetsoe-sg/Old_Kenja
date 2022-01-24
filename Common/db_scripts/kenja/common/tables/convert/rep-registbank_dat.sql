-- kanji=漢字
-- $Id: 22627338541f9ec7764bf805e0cf1f62c675a9ff $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--登録銀行データ

drop table REGISTBANK_DAT_OLD

create table REGISTBANK_DAT_OLD like REGISTBANK_DAT

insert into REGISTBANK_DAT_OLD select * from REGISTBANK_DAT

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

insert into REGISTBANK_DAT \
SELECT \
    REGI.SCHOOLCD, \
    REGI.SCHREGNO, \
    REGI.SEQ, \
    REGI.BANKCD, \
    REGI.BRANCHCD, \
    REGI.DEPOSIT_ITEM, \
    REGI.ACCOUNTNO, \
    REGI.ACCOUNTNAME, \
    REGI.RELATIONSHIP, \
    REGI.PAID_INFO_CD, \
    REGI.REGISTERCD, \
    REGI.UPDATED \
FROM \
   (SELECT \
        SCHOOLCD, \
        SCHREGNO, \
        SEQ, \
        max(NAMECD2) AS NAMECD2 \
    FROM \
        REGISTBANK_DAT_OLD REGI \
        LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' \
                               AND A023.NAME1   = REGI.SCHOOL_KIND \
    GROUP BY \
        SCHOOLCD, \
        SCHREGNO, \
        SEQ \
    ) as BASE \
    LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' \
                           AND A023.NAMECD2 = BASE.NAMECD2 \
    LEFT JOIN REGISTBANK_DAT_OLD REGI ON REGI.SCHOOLCD    = BASE.SCHOOLCD \
                                     AND REGI.SCHREGNO    = BASE.SCHREGNO \
                                     AND REGI.SCHOOL_KIND = A023.NAME1 \
                                     AND REGI.SEQ         = BASE.SEQ
