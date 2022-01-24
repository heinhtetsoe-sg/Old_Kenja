-- kanji=漢字
-- $Id: 7284491770f55d8f0bffa80ae5299ed5438a93f0 $

-- テスト時間割から講座の実施日を格納（成績入力で使用）
-- 作成日: 2005/05/16 20:09:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

drop   table CHALLENGED_SUPPORT_BASE_INFO_YMST

create table CHALLENGED_SUPPORT_BASE_INFO_YMST ( \
    YEAR        varchar(4) not null, \
    SPRT_SEQ    varchar(2) not null, \
    BASE_TITLE  varchar(150) not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE CHALLENGED_SUPPORT_BASE_INFO_YMST ADD CONSTRAINT PK_CHAL_SUPBASE_YM PRIMARY KEY (YEAR, SPRT_SEQ)
