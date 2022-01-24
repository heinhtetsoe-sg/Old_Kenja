-- $Id: bdeb3ba0a0510136b2043806225ee1fa330738b2 $

DROP TABLE ENTEXAM_MIRAICOMPASS_DAT

CREATE TABLE ENTEXAM_MIRAICOMPASS_DAT \
( \
    ENTEXAMYEAR         VARCHAR(4)  NOT NULL, \
    APPLICANTDIV        VARCHAR(30) NOT NULL, \
    ITEM3               VARCHAR(30), \
    ITEM4               VARCHAR(60), \
    ITEM5               VARCHAR(60), \
    EXAMNO              VARCHAR(10) NOT NULL, \
    ITEM7               VARCHAR(10), \
    ITEM8               VARCHAR(10), \
    ITEM9               VARCHAR(10), \
    ITEM10              VARCHAR(60), \
    ITEM11              VARCHAR(60), \
    RECOM_EXAMNO        VARCHAR(10) NOT NULL, \
    NAME_SEI            VARCHAR(60), \
    NAME_MEI            VARCHAR(60), \
    NAME_KANA_SEI       VARCHAR(120), \
    NAME_KANA_MEI       VARCHAR(120), \
    ITEM17              VARCHAR(60), \
    BIRTHDAY            VARCHAR(10), \
    ZIPCD               VARCHAR(8), \
    PREF_NAME           VARCHAR(30), \
    CITY_NAME           VARCHAR(90), \
    BANCHI_NAME         VARCHAR(90), \
    ADDRESS2            VARCHAR(150), \
    TELNO               VARCHAR(14), \
    MIRAI_FS_CD         VARCHAR(10), \
    ITEM26              VARCHAR(90), \
    ITEM27              VARCHAR(90), \
    ITEM28              VARCHAR(90), \
    MIRAI_FS_SHOZAI_CD  VARCHAR(10), \
    GNAME_SEI           VARCHAR(60), \
    GNAME_MEI           VARCHAR(60), \
    GKANA_SEI           VARCHAR(120), \
    GKANA_MEI           VARCHAR(120), \
    GZIPCD              VARCHAR(8), \
    GPREF_NAME          VARCHAR(30), \
    GCITY_NAME          VARCHAR(90), \
    GBANCHI_NAME        VARCHAR(90), \
    GADDRESS2           VARCHAR(150), \
    GTELNO              VARCHAR(14), \
    ITEM40              VARCHAR(15), \
    ITEM41              VARCHAR(2), \
    ITEM42              VARCHAR(2), \
    ITEM43              VARCHAR(30), \
    ITEM44              VARCHAR(10), \
    ITEM45              VARCHAR(90), \
    ITEM46              VARCHAR(10), \
    ITEM47              VARCHAR(10), \
    ITEM48              VARCHAR(10), \
    ITEM49              VARCHAR(2), \
    ITEM50              VARCHAR(30), \
    ITEM51              VARCHAR(30), \
    ITEM52              VARCHAR(30), \
    ITEM53              VARCHAR(2), \
    ITEM54              VARCHAR(30), \
    ITEM55              VARCHAR(1), \
    ITEM56              VARCHAR(30), \
    TORIKOMI_DATE       DATE, \
    TORIKOMI_TIME       TIME, \
    REGISTERCD          VARCHAR(10),  \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_MIRAICOMPASS_DAT ADD CONSTRAINT \
PK_ENTEXAM_MIRAI PRIMARY KEY (ENTEXAMYEAR, EXAMNO)
