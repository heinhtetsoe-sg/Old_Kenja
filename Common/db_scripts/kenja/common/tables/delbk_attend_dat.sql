-- $Id: f0e83f578f8840381ae96d31974cbabe20ee2e21 $

DROP TABLE DELBK_ATTEND_DAT
CREATE TABLE DELBK_ATTEND_DAT ( \
    DEL_SEQ             SMALLINT   NOT NULL, \
    SCHREGNO            VARCHAR(8) NOT NULL, \
    ATTENDDATE          DATE NOT NULL, \
    PERIODCD            VARCHAR(1) NOT NULL, \
    CAHIRCD             VARCHAR(7), \
    DI_CD               VARCHAR(2), \
    DI_REMARK_CD        VARCHAR(3), \
    DI_REMARK           VARCHAR(60), \
    YEAR                VARCHAR(4), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             TIMESTAMP, \
    DEL_REGISTERCD      VARCHAR(8), \
    DEL_UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DELBK_ATTEND_DAT ADD CONSTRAINT PK_DLBK_ATTEND \
        PRIMARY KEY (DEL_SEQ, SCHREGNO, ATTENDDATE, PERIODCD)
