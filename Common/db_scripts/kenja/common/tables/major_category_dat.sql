-- kanji=漢字
-- $Id: c19edaf31a4b70a7c91296a038a415555a66d6cc $

DROP TABLE MAJOR_CATEGORY_DAT
CREATE TABLE MAJOR_CATEGORY_DAT( \
    COURSECD    VARCHAR(1)    NOT NULL, \
    MAJORCD     VARCHAR(3)    NOT NULL, \
    CATEGORYCD  VARCHAR(4)    NOT NULL, \
    REGISTERCD  varchar(8), \
    UPDATED     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE MAJOR_CATEGORY_DAT ADD CONSTRAINT PK_MAJOR_CTR_DAT PRIMARY KEY (COURSECD,MAJORCD)