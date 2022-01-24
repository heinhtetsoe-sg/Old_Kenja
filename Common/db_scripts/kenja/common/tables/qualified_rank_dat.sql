-- $Id: ca939bec6f8cb2a5c05524b9039693eeac8a8b38 $

DROP TABLE QUALIFIED_RANK_DAT
CREATE TABLE QUALIFIED_RANK_DAT( \
    QUALIFIED_CD  VARCHAR(4)    NOT NULL, \
    RANK          VARCHAR(3)    NOT NULL, \
    NOT_PRINT     VARCHAR(1), \
    SCORE         SMALLINT, \
    REGISTERCD    VARCHAR(10), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE QUALIFIED_RANK_DAT ADD CONSTRAINT PK_QUALI_RANK_DAT PRIMARY KEY (QUALIFIED_CD,RANK)
