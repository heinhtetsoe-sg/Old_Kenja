-- $Id: 042ffbd37dcd04e4caf436c18158b065031c8d99 $

DROP TABLE DOCUMENT_DETAIL_MST
CREATE TABLE DOCUMENT_DETAIL_MST( \
    DOCUMENTCD                VARCHAR(2)    NOT NULL, \
    SEQ                       VARCHAR(3)    NOT NULL, \
    TITLE                     VARCHAR(120)  , \
    TEXT                      VARCHAR(1600) , \
    FOOTNOTE                  VARCHAR(1600) , \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DOCUMENT_DETAIL_MST ADD CONSTRAINT PK_DOCUMENT_D_MST PRIMARY KEY (DOCUMENTCD, SEQ)