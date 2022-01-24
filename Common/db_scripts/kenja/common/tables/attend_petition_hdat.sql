-- kanji=漢字
-- $Id: 3f9330f8a4a64299fc9d140a954e0b0e096c4416 $
-- 出欠届けヘッダデータ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_PETITION_HDAT

create table ATTEND_PETITION_HDAT ( \
    YEAR            varchar(4) not null, \
    SEQNO           integer not null, \
    SCHREGNO        varchar(8), \
    CONTACTERDIV    varchar(1), \
    CONTACTER       varchar(90), \
    CALLBACK        varchar(1), \
    FIRSTDATE       timestamp, \
    FIRSTREGISTER   varchar(10), \
    FROMDATE        date, \
    FROMPERIOD      varchar(1), \
    TODATE          date, \
    TOPERIOD        varchar(1), \
    DI_CD           varchar(2), \
    DI_REMARK_CD1   varchar(30), \
    DI_REMARK_CD2   varchar(30), \
    DI_REMARK_CD3   varchar(30), \
    DI_REMARK_CD4   varchar(30), \
    DI_REMARK_CD5   varchar(30), \
    DI_REMARK_CD6   varchar(30), \
    DI_REMARK_CD7   varchar(30), \
    DI_REMARK_CD8   varchar(30), \
    DI_REMARK_CD9   varchar(30), \
    DI_REMARK_CD10  varchar(30), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_PETITION_HDAT add constraint PK_ATTEND_P_HDAT \
        primary key (YEAR, SEQNO)
