-- $Id: 1c24275ced8ccb90cc3dd04acc65c90c8ddbd5da $

DROP TABLE DELBK_SCHREG_BASE_MST
CREATE TABLE DELBK_SCHREG_BASE_MST ( \
     DEL_SEQ             SMALLINT      NOT NULL, \
     SCHREGNO            VARCHAR(8)    NOT NULL, \
     INOUTCD             VARCHAR(1), \
     NAME                VARCHAR(120), \
     NAME_SHOW           VARCHAR(120), \
     NAME_KANA           VARCHAR(240), \
     NAME_ENG            VARCHAR(40), \
     REAL_NAME           VARCHAR(120), \
     REAL_NAME_KANA      VARCHAR(240), \
     BIRTHDAY            DATE, \
     SEX                 VARCHAR(1), \
     BLOODTYPE           VARCHAR(2), \
     BLOOD_RH            VARCHAR(1), \
     HANDICAP            VARCHAR(3), \
     NATIONALITY         VARCHAR(3), \
     FINSCHOOLCD         VARCHAR(7), \
     FINISH_DATE         DATE, \
     PRISCHOOLCD         VARCHAR(7), \
     ENT_DATE            DATE, \
     ENT_DIV             VARCHAR(1), \
     ENT_REASON          VARCHAR(75), \
     ENT_SCHOOL          VARCHAR(75), \
     ENT_ADDR            VARCHAR(150), \
     ENT_ADDR2           VARCHAR(150), \
     GRD_DATE            DATE, \
     GRD_DIV             VARCHAR(1), \
     GRD_REASON          VARCHAR(75), \
     GRD_SCHOOL          VARCHAR(75), \
     GRD_ADDR            VARCHAR(150), \
     GRD_ADDR2           VARCHAR(150), \
     GRD_NO              VARCHAR(8), \
     GRD_TERM            VARCHAR(4), \
     REMARK1             VARCHAR(75), \
     REMARK2             VARCHAR(75), \
     REMARK3             VARCHAR(75), \
     EMERGENCYCALL       VARCHAR(60), \
     EMERGENCYNAME       VARCHAR(60), \
     EMERGENCYRELA_NAME  VARCHAR(30), \
     EMERGENCYTELNO      VARCHAR(14), \
     EMERGENCYCALL2      VARCHAR(60), \
     EMERGENCYNAME2      VARCHAR(60), \
     EMERGENCYRELA_NAME2 VARCHAR(30), \
     EMERGENCYTELNO2     VARCHAR(14), \
     REGISTERCD          VARCHAR(8), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
     DEL_REGISTERCD      VARCHAR(8), \
     DEL_UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DELBK_SCHREG_BASE_MST ADD CONSTRAINT PK_DELBK_SHG_BASE PRIMARY KEY (DEL_SEQ, SCHREGNO)
