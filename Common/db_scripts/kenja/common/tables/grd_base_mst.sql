-- $Id: 6cc57b7dbc94d2834fb7770b84b03f9d5bb863e4 $

DROP TABLE GRD_BASE_MST
CREATE TABLE GRD_BASE_MST( \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    INOUTCD                 VARCHAR(1), \
    NAME                    VARCHAR(120), \
    NAME_SHOW               VARCHAR(120), \
    NAME_KANA               VARCHAR(240), \
    NAME_ENG                VARCHAR(40), \
    REAL_NAME               VARCHAR(120), \
    REAL_NAME_KANA          VARCHAR(240), \
    OLD_NAME                VARCHAR(120), \
    OLD_NAME_SHOW           VARCHAR(120), \
    OLD_NAME_KANA           VARCHAR(240), \
    OLD_NAME_ENG            VARCHAR(40), \
    BIRTHDAY                DATE, \
    SEX                     VARCHAR(1), \
    BLOODTYPE               VARCHAR(2), \
    BLOOD_RH                VARCHAR(1), \
    HANDICAP                VARCHAR(3), \
    NATIONALITY             VARCHAR(3), \
    FINSCHOOLCD             VARCHAR(12), \
    FINISH_DATE             DATE, \
    PRISCHOOLCD             VARCHAR(7), \
    ENT_DATE                DATE, \
    ENT_DIV                 VARCHAR(1), \
    ENT_REASON              VARCHAR(75), \
    ENT_SCHOOL              VARCHAR(75), \
    ENT_ADDR                VARCHAR(150), \
    ENT_ADDR2               VARCHAR(150), \
    GRD_DATE                DATE, \
    GRD_DIV                 VARCHAR(1), \
    GRD_REASON              VARCHAR(75), \
    GRD_SCHOOL              VARCHAR(75), \
    GRD_ADDR                VARCHAR(150), \
    GRD_ADDR2               VARCHAR(150), \
    GRD_NO                  VARCHAR(8), \
    GRD_TERM                VARCHAR(4), \
    GRD_SEMESTER            VARCHAR(1), \
    GRD_GRADE               VARCHAR(2), \
    GRD_HR_CLASS            VARCHAR(3), \
    GRD_ATTENDNO            VARCHAR(3), \
    REMARK1                 VARCHAR(150), \
    REMARK2                 VARCHAR(150), \
    REMARK3                 VARCHAR(150), \
    CUR_EMERGENCYCALL       VARCHAR(60), \
    CUR_EMERGENCYNAME       VARCHAR(60), \
    CUR_EMERGENCYRELA_NAME  VARCHAR(30), \
    CUR_EMERGENCYTELNO      VARCHAR(14), \
    CUR_EMERGENCYCALL2      VARCHAR(60), \
    CUR_EMERGENCYNAME2      VARCHAR(60), \
    CUR_EMERGENCYRELA_NAME2 VARCHAR(30), \
    CUR_EMERGENCYTELNO2     VARCHAR(14), \
    CUR_ZIPCD               VARCHAR(8), \
    CUR_AREACD              VARCHAR(2), \
    CUR_ADDR1               VARCHAR(150), \
    CUR_ADDR2               VARCHAR(150), \
    CUR_ADDR_FLG            VARCHAR(1), \
    CUR_ADDR1_ENG           VARCHAR(150), \
    CUR_ADDR2_ENG           VARCHAR(150), \
    CUR_TELNO               VARCHAR(14), \
    CUR_FAXNO               VARCHAR(14), \
    CUR_EMAIL               VARCHAR(20), \
    ZIPCD                   VARCHAR(8), \
    AREACD                  VARCHAR(2), \
    ADDR1                   VARCHAR(150), \
    ADDR2                   VARCHAR(150), \
    TELNO                   VARCHAR(14), \
    FAXNO                   VARCHAR(14), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GRD_BASE_MST ADD CONSTRAINT PK_GRD_BASE PRIMARY KEY (SCHREGNO)