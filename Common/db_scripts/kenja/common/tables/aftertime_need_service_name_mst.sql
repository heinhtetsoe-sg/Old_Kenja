-- $Id: 3ab81a72c71f9065b3f0b1a2932f58e2259211f5 $

DROP TABLE AFTERTIME_NEED_SERVICE_NAME_MST
CREATE TABLE AFTERTIME_NEED_SERVICE_NAME_MST( \
    NAMECD                  VARCHAR(3)    NOT NULL, \
    NAME                    VARCHAR(150), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE AFTERTIME_NEED_SERVICE_NAME_MST ADD CONSTRAINT PK_A_N_S_NAME_MST PRIMARY KEY (NAMECD)