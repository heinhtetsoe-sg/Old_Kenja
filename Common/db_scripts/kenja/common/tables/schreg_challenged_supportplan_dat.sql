-- kanji=漢字
-- $Id: 0f909c7991b19d5131433623d529ea1251689573 $

-- テスト時間割から講座の実施日を格納（成績入力で使用）
-- 作成日: 2005/05/16 20:09:00 - JST
-- 作成者: tamura

-- スクリプトの使用方法: db2 +c -f <thisfile>
-- 注意:このファイルは EUC/LFのみ でなければならない。

drop   table SCHREG_CHALLENGED_SUPPORTPLAN_DAT

create table SCHREG_CHALLENGED_SUPPORTPLAN_DAT ( \
    YEAR        varchar(4) not null, \
    SCHREGNO    varchar(8) not null, \
    SPRT_DIV    varchar(2) not null, \
    SPRT_SEQ    varchar(2) not null, \
    REMARK      varchar(7200), \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

ALTER TABLE SCHREG_CHALLENGED_SUPPORTPLAN_DAT ADD CONSTRAINT PK_SCHCHAL_SPLAN PRIMARY KEY (YEAR, SCHREGNO, SPRT_DIV, SPRT_SEQ)
