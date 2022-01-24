-- kanji=漢字
-- $Id: 1efc76b99f6d27a22d7fc3bbd6aa7c8083155b7d $
-- 出欠けデータ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_DAY_SUBM_DAT

create table ATTEND_DAY_SUBM_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    DI_CD           varchar(2)  not null, \
    SUBL_CD         varchar(3) not null, \
    SUBM_CD         varchar(3) not null, \
    YEAR            varchar(4) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_DAY_SUBM_DAT add constraint PK_AT_DAY_SUBM_DAT \
        primary key (SCHREGNO, ATTENDDATE)
