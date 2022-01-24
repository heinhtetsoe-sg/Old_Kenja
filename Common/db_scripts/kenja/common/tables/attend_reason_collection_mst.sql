-- kanji=漢字
-- $Id: af1e18d9d39e02936a2baf54aef757372ecda684 $
-- 一日欠データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_REASON_COLLECTION_MST

create table ATTEND_REASON_COLLECTION_MST ( \
    YEAR            varchar(4) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    COLLECTION_CD   varchar(2) not null, \
    COLLECTION_NAME varchar(30) not null, \
    FROM_DATE       date not null, \
    TO_DATE         date not null, \
    SEMESTER        varchar(1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_REASON_COLLECTION_MST add constraint PK_ATT_RESON_COL_M \
        primary key (YEAR, SCHOOL_KIND, COLLECTION_CD)
