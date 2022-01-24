-- kanji=漢字
-- $Id: 85d83d2a9823cef9b6a187b5bda39ac29a72d08d $
-- 出欠けデータ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_CHAIR_STF_DAT

create table ATTEND_CHAIR_STF_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    PERIODCD        varchar(1) not null, \
    CHAIRCD         varchar(7), \
    DI_CD           varchar(2), \
    DI_REMARK_CD    varchar(3), \
    DI_REMARK       varchar(60), \
    YEAR            varchar(4), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_CHAIR_STF_DAT add constraint PK_ATTEND_CHAIR_STF_DAT \
        primary key (SCHREGNO, ATTENDDATE, PERIODCD)
