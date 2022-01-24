-- $Id: 07ffd281fd8d17d6009d1de7ce023203051db0ca $

DROP TABLE CERTIF_SCH_PRINT_CNT_DAT
CREATE TABLE CERTIF_SCH_PRINT_CNT_DAT( \
    SCHREGNO      VARCHAR(8)    NOT NULL, \
    CERTIF_KINDCD VARCHAR(3)    NOT NULL, \
    PRINT_CNT     SMALLINT      NOT NULL, \
    REGISTERCD    VARCHAR(8), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CERTIF_SCH_PRINT_CNT_DAT ADD CONSTRAINT PK_CERTIF_SCH_PRT PRIMARY KEY (SCHREGNO,CERTIF_KINDCD)
