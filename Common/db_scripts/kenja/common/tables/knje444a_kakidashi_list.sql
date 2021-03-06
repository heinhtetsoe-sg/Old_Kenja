-- kanji=漢字
-- $Id: d2695db253d7358ea64028f37bc4154e55ac4fd3 $

DROP TABLE KNJE444A_KAKIDASHI_LIST

CREATE TABLE KNJE444A_KAKIDASHI_LIST( \
    YEAR                VARCHAR(4)    NOT NULL, \
    DATA_DIV            VARCHAR(2)    NOT NULL, \
    SEQ                 SMALLINT      NOT NULL, \
    FIELD_NAME          VARCHAR(75), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KNJE444A_KAKIDASHI_LIST ADD CONSTRAINT PK_KNJE444A_KAKIDASHI_LIST PRIMARY KEY (YEAR, DATA_DIV, SEQ)