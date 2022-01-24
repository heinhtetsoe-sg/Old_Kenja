-- $Id: e15321918841efcb0c917bf012fb3db1fcd06594 $
drop table SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT_OLD
create table SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT_OLD like SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT
insert into SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT_OLD select * from SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT

DROP TABLE SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT
CREATE TABLE SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT( \
    SCHREGNO                            VARCHAR(8)    NOT NULL, \
    RECORD_DATE                         VARCHAR(10)   NOT NULL, \
    RECORD_DIV                          VARCHAR(1)    NOT NULL, \
    RECORD_NO                           VARCHAR(1)    NOT NULL, \
    RECORD_SEQ                          SMALLINT      NOT NULL, \
    SCHOOL_NAME                         VARCHAR(60), \
    P_J_SCHOOL_CD                       VARCHAR(12), \
    S_YEAR_MONTH                        VARCHAR(7), \
    E_YEAR_MONTH                        VARCHAR(7), \
    MAJOR_NAME                          VARCHAR(30), \
    GRADE                               VARCHAR(2), \
    HR_CLASS_NAME                       VARCHAR(30), \
    CLASS_SHUBETSU                      VARCHAR(2), \
    REMARK                              VARCHAR(75), \
    REGISTERCD                          VARCHAR(10), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT ADD CONSTRAINT PK_SCH_CHA_P_E_R_D PRIMARY KEY (SCHREGNO, RECORD_DATE, RECORD_DIV, RECORD_NO, RECORD_SEQ)

INSERT INTO SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT \
    SELECT \
        * \
    FROM \
        SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT_OLD
