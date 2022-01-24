-- kanji=漢字
-- $Id: 547a3731e9c12fb7f0cd5f1ee7ff1cec66e06474 $

-- テスト時間割から講座の実施日を格納（成績入力で使用）
-- 作成日: 2005/05/16 20:09:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

drop   table SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT

create table SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT ( \
    YEAR                varchar(4) not null, \
    SCHREGNO            varchar(8) not null, \
    SPRT_FACILITY_GRP   varchar(3) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT ADD CONSTRAINT PK_SCH_CHALSPRT_FACILITY_GRP_DAT PRIMARY KEY (YEAR, SCHREGNO)
