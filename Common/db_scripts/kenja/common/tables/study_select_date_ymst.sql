-- kanji=漢字
-- $Id: 7b94ce40ca671599c9123ebfdd952e3738b73bcc $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE STUDY_SELECT_DATE_YMST
CREATE TABLE STUDY_SELECT_DATE_YMST( \
    YEAR            varchar(4)    not null, \
    RIREKI_CODE     varchar(2)    not null, \
    SELECT_NAME     varchar(60), \
    SELECT_DATE     DATE, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STUDY_SELECT_DATE_YMST ADD CONSTRAINT PK_STUDY_S_DATE_Y PRIMARY KEY (YEAR, RIREKI_CODE)