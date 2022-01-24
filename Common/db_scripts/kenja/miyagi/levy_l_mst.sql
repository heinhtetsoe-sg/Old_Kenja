-- kanji=漢字
-- $Id: levy_l_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計科目マスタ

DROP TABLE LEVY_L_MST

CREATE TABLE LEVY_L_MST ( \
        "YEAR"         varchar(4)  not null, \
        "LEVY_L_CD"    VARCHAR(2) NOT NULL, \
        "LEVY_L_NAME"  VARCHAR(90), \
        "LEVY_L_ABBV"  VARCHAR(90), \
        "REGISTERCD"   VARCHAR(10), \
        "UPDATED"      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_L_MST ADD CONSTRAINT PK_LEVY_L_MST PRIMARY KEY (YEAR, LEVY_L_CD)
