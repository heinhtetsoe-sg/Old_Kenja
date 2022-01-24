-- kanji=漢字
-- $Id: jbank_class_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--JBANK_CLASS_MST      中学時銀行クラスマスタ

DROP TABLE JBANK_CLASS_MST

CREATE TABLE JBANK_CLASS_MST \
( \
        "YEAR"          VARCHAR(4)  NOT NULL, \
        "GRADE"         VARCHAR(2)  NOT NULL, \
        "HR_CLASS"      VARCHAR(3)  NOT NULL, \
        "BANK_MAJORCD"  VARCHAR(2), \
        "BANK_HR_CLASS" VARCHAR(2), \
        "REGISTERCD"    VARCHAR(8), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JBANK_CLASS_MST \
ADD CONSTRAINT PK_JBANK_CLASS_MST \
PRIMARY KEY \
(YEAR,GRADE,HR_CLASS)

