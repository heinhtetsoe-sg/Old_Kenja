-- kanji=漢字
-- $Id: 47b6898bf0b58155ff6a6cf2bab29bc81ebf46a9 $
-- 出欠届けヘッダデータ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_BATCH_INPUT_HDAT

create table ATTEND_BATCH_INPUT_HDAT ( \
    YEAR            varchar(4) not null, \
    SEQNO           integer not null, \
    INPUT_TYPE      varchar(1) not null, \
    FIRST_DATE       timestamp not null, \
    FIRST_REGISTER   varchar(10) not null, \
    FROM_DATE        date not null, \
    FROM_PERIOD      varchar(1), \
    TO_DATE          date not null, \
    TO_PERIOD        varchar(1), \
    DI_CD           varchar(2) not null, \
    DI_REMARK_CD    varchar(3), \
    DI_REMARK       varchar(60), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_BATCH_INPUT_HDAT add constraint PK_ATE_BINPUT_HDAT \
        primary key (YEAR, SEQNO)
