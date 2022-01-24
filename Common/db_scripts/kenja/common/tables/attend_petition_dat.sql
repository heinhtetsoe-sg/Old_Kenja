-- kanji=漢字
-- $Id: efb1201e6258e0a342c441e608333ca39ecaedf2 $
-- 出欠届けデータ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_PETITION_DAT

create table ATTEND_PETITION_DAT ( \
    YEAR            varchar(4) not null, \
    SEQNO           integer not null, \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    PERIODCD        varchar(1) not null, \
    DI_CD           varchar(2), \
    DI_REMARK_CD    varchar(3), \
    DI_REMARK       varchar(30), \
    INPUT_FLG       varchar(1), \
    EXECUTED        varchar(1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_PETITION_DAT add constraint PK_ATTEND_P_DAT \
        primary key (YEAR, SEQNO, SCHREGNO, ATTENDDATE, PERIODCD)
