-- $Id: d55622add6a3401f741d39d4eedfaf45920382ee $

DROP TABLE PRGINFO_PROPERTIES_MST
CREATE TABLE PRGINFO_PROPERTIES_MST( \
    NAME       VARCHAR(50)   NOT NULL, \
    VALUE      VARCHAR(300), \
    SORT       SMALLINT, \
    REMARK     VARCHAR(300), \
    COMMENT_1  VARCHAR(300), \
    COMMENT_2  VARCHAR(300), \
    COMMENT_3  VARCHAR(300), \
    COMMENT_4  VARCHAR(300), \
    REGISTERCD VARCHAR(10), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE PRGINFO_PROPERTIES_PRGID ADD CONSTRAINT PRGINFO_PROPERTIES_MST PRIMARY KEY (NAME)
