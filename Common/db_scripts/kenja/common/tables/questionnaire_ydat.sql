-- kanji=漢字
-- $Id: 7e8539238645648455dbd1534a87ec7a3a7c5feb $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE QUESTIONNAIRE_YDAT
CREATE TABLE QUESTIONNAIRE_YDAT( \
    YEAR            VARCHAR(4)    NOT NULL, \
    QUESTIONNAIRECD VARCHAR(2)    NOT NULL, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE QUESTIONNAIRE_YDAT ADD CONSTRAINT PK_QUESTIONNAIRE_Y PRIMARY KEY (YEAR,QUESTIONNAIRECD)