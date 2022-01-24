-- kanji=漢字
-- $Id: 22f91ac1a0a50f6b6429207d3179a4df8de1de27 $
-- 出欠届けヘッダデータ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ATTEND_BATCH_INPUT_HR_DAT

create table ATTEND_BATCH_INPUT_HR_DAT ( \
    YEAR            varchar(4) not null, \
    SEQNO           integer not null, \
    GRADE           varchar(2) not null, \
    HR_CLASS        varchar(3) not null, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_BATCH_INPUT_HR_DAT add constraint PK_ATE_BINPUT_HR \
        primary key (YEAR, SEQNO, GRADE, HR_CLASS)
