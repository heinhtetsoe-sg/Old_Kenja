-- $Id: 3928cdfc37f8777f5d109451191eb844f6fb35ed $

DROP TABLE CHALLENGED_TABLE_UPDATE_LOG
CREATE TABLE CHALLENGED_TABLE_UPDATE_LOG( \
    UPDATED                             TIMESTAMP NOT NULL,  \
    USERID                              VARCHAR(60) NOT NULL, \
    STAFFCD                             VARCHAR(10), \
    DATA_TYPE                           VARCHAR(1), \
    YEAR                                VARCHAR(4), \
    SCHREGNO                            VARCHAR(8), \
    RECORD_DATE                         VARCHAR(10) \
    //CMD                                 VARCHAR(10), \
    //TABLE                               VARCHAR(100) \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CHALLENGED_TABLE_UPDATE_LOG ADD CONSTRAINT PK_CHA_TABLE_UP PRIMARY KEY (UPDATED, USERID)