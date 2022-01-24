-- kanji=漢字
-- $Id: levy_m_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計項目マスタ
DROP TABLE LEVY_M_MST \

CREATE TABLE LEVY_M_MST \
( \
        "YEAR"                varchar(4)  not null, \
        "LEVY_L_CD"           VARCHAR(2) NOT NULL, \
        "LEVY_M_CD"           VARCHAR(2) NOT NULL, \
        "LEVY_IN_OUT_DIV"     VARCHAR(1) NOT NULL, \
        "LEVY_M_NAME"         VARCHAR(90), \
        "LEVY_M_ABBV"         VARCHAR(90), \
        "LEVY_S_EXIST_FLG"    VARCHAR(1), \
        "ZATU_FLG"            VARCHAR(1), \
        "YOBI_FLG"            VARCHAR(1), \
        "KURIKOSI_FLG"        VARCHAR(1), \
        "REMARK"              VARCHAR(60), \
        "REGISTERCD"          VARCHAR(10), \
        "UPDATED"             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_M_MST \
ADD CONSTRAINT PK_LEVY_M_MST \
PRIMARY KEY \
(YEAR, LEVY_L_CD, LEVY_M_CD)
