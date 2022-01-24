-- kanji=漢字
-- $Id: da5bc969c83ea6360cd225b4078e1486233d8eed $

-- テスト時間割から講座の実施日を格納（成績入力で使用）
-- 作成日: 2005/05/16 20:09:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

drop   table CHALLENGED_SUPPORT_FACILITY_GRP_DAT

create table CHALLENGED_SUPPORT_FACILITY_GRP_DAT ( \
    YEAR                varchar(4) not null, \
    SPRT_FACILITY_GRP   varchar(3) not null, \
    SPRT_FACILITY_CD    varchar(3) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE CHALLENGED_SUPPORT_FACILITY_GRP_DAT ADD CONSTRAINT PK_CHALSPRT_FACGD PRIMARY KEY (YEAR, SPRT_FACILITY_GRP, SPRT_FACILITY_CD)
