-- $Id: 4801cf5a6cf882fbc170dd3d9cc5aa3c2dccfe97 $

DROP TABLE ENTEXAM_AID_RISSHI_DAT
CREATE TABLE ENTEXAM_AID_RISSHI_DAT( \
    ENTEXAMYEAR             VARCHAR(4)    NOT NULL, \
    APPLICANTDIV            VARCHAR(1)    NOT NULL, \
    AID_TESTDIV             VARCHAR(2)    NOT NULL, \
    AID_TESTDIV_NAME        VARCHAR(15)   , \
    EXAMCD                  VARCHAR(4)    , \
    EXAM_NAME               VARCHAR(250)  , \
    EXAMNO                  VARCHAR(10)   NOT NULL, \
    HOPECOURSE1             VARCHAR(2)    , \
    HOPECOURSE2             VARCHAR(2)    , \
    NAME_SEI                VARCHAR(60)   , \
    NAME_MEI                VARCHAR(60)   , \
    GAIJI_CD                VARCHAR(1)    , \
    NAME_KANA_SEI           VARCHAR(120)  , \
    NAME_KANA_MEI           VARCHAR(120)  , \
    SEX                     VARCHAR(1)    , \
    BIRTHDAY                VARCHAR(10)   , \
    ZIPCD1                  VARCHAR(3)    , \
    ZIPCD2                  VARCHAR(4)    , \
    PREF_CITY_BANCHI_NAME   VARCHAR(150)  , \
    ADDRESS2_1              VARCHAR(75)   , \
    ADDRESS2_2              VARCHAR(75)   , \
    PREF_CD                 VARCHAR(2)    , \
    TELNO                   VARCHAR(14)   , \
    EMAIL                   VARCHAR(256)  , \
    SHIGAN_DATE             VARCHAR(10)   , \
    GNAME_SEI               VARCHAR(60)   , \
    GNAME_MEI               VARCHAR(60)   , \
    GKANA_SEI               VARCHAR(120)  , \
    GKANA_MEI               VARCHAR(120)  , \
    RELATIONSHIP            VARCHAR(2)    , \
    GZIPCD1                 VARCHAR(3)    , \
    GZIPCD2                 VARCHAR(4)    , \
    GPREF_CITY_BANCHI_NAME  VARCHAR(150)  , \
    GADDRESS2_1             VARCHAR(75)   , \
    GADDRESS2_2             VARCHAR(75)   , \
    GPREF_CD                VARCHAR(2)    , \
    GTELNO                  VARCHAR(14)   , \
    EMERGENCYCALL           VARCHAR(14)   , \
    AID_FS_CD               VARCHAR(6)    , \
    AID_FS_NAME             VARCHAR(150)  , \
    FS_GRDYEAR              VARCHAR(4)    , \
    REASON                  VARCHAR(765)  , \
    SH_SCHOOL_NAME          VARCHAR(150)  , \
    SH_PASS_DATE            VARCHAR(10)   , \
    SCHOLARSHIP             VARCHAR(2)    , \
    SCHOLARSHIP_REASON      VARCHAR(765)  , \
    ENT_MONEY_EXEMPT        VARCHAR(1)    , \
    P_REGD_DIV              VARCHAR(2)    , \
    P_GRD_YEAR              VARCHAR(4)    , \
    P_GRD_COURSE            VARCHAR(2)    , \
    P_NAME                  VARCHAR(150)  , \
    P_RELATIONSHIP          VARCHAR(2)    , \
    PRISCHOOL_NAME          VARCHAR(150)  , \
    PRISCHOOL_CLASS_NAME    VARCHAR(150)  , \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    HOPE_REASON             VARCHAR(1500), \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_AID_RISSHI_DAT ADD CONSTRAINT PK_ENTE_AID_RISSI PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,AID_TESTDIV,EXAMNO)