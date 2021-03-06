-- $Id: b8b87dd896df0ad3eea62afd2b25f894c24d6548 $

DROP TABLE IBSUBCLASS_UNITPLAN_DAT
CREATE TABLE IBSUBCLASS_UNITPLAN_DAT( \
    IBYEAR               VARCHAR(4)    NOT NULL, \
    IBGRADE              VARCHAR(2)    NOT NULL, \
    IBCLASSCD            VARCHAR(2)    NOT NULL, \
    IBPRG_COURSE         VARCHAR(2)    NOT NULL, \
    IBCURRICULUM_CD      VARCHAR(2)    NOT NULL, \
    IBSUBCLASSCD         VARCHAR(6)    NOT NULL, \
    IBSEQ                SMALLINT      NOT NULL, \
    IBEVAL_DIV1          VARCHAR(1)    NOT NULL, \
    IBEVAL_DIV2          VARCHAR(1)    NOT NULL, \
    IBEVAL_MARK          VARCHAR(2)    NOT NULL, \
    VIEWCD               VARCHAR(4)    NOT NULL, \
    REGISTERCD           VARCHAR(8), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE IBSUBCLASS_UNITPLAN_DAT ADD CONSTRAINT PK_IBSUB_UNITPLAN PRIMARY KEY (IBYEAR, IBGRADE, IBCLASSCD, IBPRG_COURSE, IBCURRICULUM_CD, IBSUBCLASSCD, IBSEQ, IBEVAL_DIV1, IBEVAL_DIV2, IBEVAL_MARK, VIEWCD)