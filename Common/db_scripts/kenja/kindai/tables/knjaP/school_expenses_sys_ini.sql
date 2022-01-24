-- kanji=漢字
-- $Id: school_expenses_sys_ini.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--SCHOOL_EXPENSES_SYS_INI	校納金システム設定データ

DROP TABLE SCHOOL_EXPENSES_SYS_INI

CREATE TABLE SCHOOL_EXPENSES_SYS_INI \
( \
        "PROGRAMID"     VARCHAR(10) NOT NULL, \
        "DIV"           VARCHAR(4)  NOT NULL, \
        "VAR1"          VARCHAR(8), \
        "VAR2"          VARCHAR(8), \
        "INT1"          INTEGER, \
        "INT2"          INTEGER, \
        "DATE1"         DATE, \
        "DATE2"         DATE, \
        "REMARK"        VARCHAR(75), \
        "REGISTERCD"    VARCHAR(8), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHOOL_EXPENSES_SYS_INI \
ADD CONSTRAINT PK_SCREG_GRANT_DAT \
PRIMARY KEY \
(PROGRAMID,DIV)

INSERT INTO SCHOOL_EXPENSES_SYS_INI \
 (PROGRAMID,DIV,VAR1,REMARK) VALUES \
 ('KNJP000K','0001','02456','銀行利用学校コード')

INSERT INTO SCHOOL_EXPENSES_SYS_INI \
 (PROGRAMID,DIV,VAR1,REMARK) VALUES \
 ('KNJP050K','0001','1','P050K費目グループ設定処理の更新機能の制御フラグ')
