-- kanji=漢字
-- $Id: levy_s_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計細目マスタ
DROP TABLE LEVY_S_MST \

CREATE TABLE LEVY_S_MST \
( \
        "YEAR"                varchar(4)  not null, \
        "LEVY_L_CD"           VARCHAR(2) NOT NULL, \
        "LEVY_M_CD"           VARCHAR(2) NOT NULL, \
        "LEVY_S_CD"           VARCHAR(2) NOT NULL, \
        "LEVY_S_NAME"         VARCHAR(90), \
        "LEVY_S_ABBV"         VARCHAR(90), \
        "REPAY_DIV"           VARCHAR(1), \
        "REMARK"              VARCHAR(60), \
        "REGISTERCD"          VARCHAR(10), \
        "UPDATED"             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_S_MST \
ADD CONSTRAINT PK_LEVY_S_MST \
PRIMARY KEY \
(YEAR, LEVY_L_CD, LEVY_M_CD, LEVY_S_CD)
