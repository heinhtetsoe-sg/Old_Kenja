-- $Id: ff4b6bb2722076175fc48c3427d149b90bbc5bab $

DROP TABLE ENTEXAM_DOCUMENT_DAT
CREATE TABLE ENTEXAM_DOCUMENT_DAT( \
    ENTEXAMYEAR   VARCHAR(4)    NOT NULL, \
    APPLICANTDIV  VARCHAR(1)    NOT NULL, \
    DOCUMENT      VARCHAR(300), \
    REGISTERCD    VARCHAR(10), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_DOCUMENT_DAT ADD CONSTRAINT PK_ENTEXAM_DOC_D PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV)