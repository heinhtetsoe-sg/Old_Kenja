-- kanji=漢字
-- $Id: appl_result_err_data.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--APPL_RESULT_ERR_DATA 申込入金処理結果エラーデータ

DROP TABLE APPL_RESULT_ERR_DATA

CREATE TABLE APPL_RESULT_ERR_DATA \
( \
        "PROCESSCD"             VARCHAR(1)      NOT NULL, \
        "FILE_LINE_NUMBER"      INTEGER         NOT NULL, \
        "COLNAME"               VARCHAR(128)    NOT NULL, \
        "ERR_LEVEL"             VARCHAR(1), \
        "ERR_MSG"               VARCHAR(150), \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE APPL_RESULT_ERR_DATA \
ADD CONSTRAINT PK_APPL_RES_ERR_DT \
PRIMARY KEY \
(PROCESSCD,FILE_LINE_NUMBER,COLNAME)

COMMENT ON TABLE APPL_RESULT_ERR_DATA IS '申込入金処理結果エラーデータ 2005/06/17'
