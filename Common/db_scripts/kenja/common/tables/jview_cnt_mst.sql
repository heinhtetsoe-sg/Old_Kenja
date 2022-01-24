-- $Id: 160ef9faa9d132b657df16b1e5dd9040e430608f $

DROP TABLE JVIEW_CNT_MST

CREATE TABLE JVIEW_CNT_MST( \
    YEAR                VARCHAR(4) NOT NULL, \
    SCORE               SMALLINT NOT NULL, \
    JVIEW1              VARCHAR(1), \
    JVIEW2              VARCHAR(1), \
    JVIEW3              VARCHAR(1), \
    JVIEW4              VARCHAR(1), \
    JVIEW5              VARCHAR(1), \
    JVIEW6              VARCHAR(1), \
    JVIEW7              VARCHAR(1), \
    JVIEW8              VARCHAR(1), \
    JVIEW9              VARCHAR(1), \
    JVIEW10             VARCHAR(1), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JVIEW_CNT_MST ADD CONSTRAINT PK_JVIEW_CNT_MST PRIMARY KEY (YEAR, SCORE)
