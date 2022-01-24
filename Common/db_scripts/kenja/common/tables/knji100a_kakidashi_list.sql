-- kanji=Š¿Žš
-- $Id: 5a4765e1fdbc2796097b483a42c1554519e48b30 $

drop   table KNJI100A_KAKIDASHI_LIST

create table KNJI100A_KAKIDASHI_LIST( \
    YEAR                  VARCHAR(4) NOT NULL, \
    DATA_DIV              VARCHAR(2) NOT NULL, \
    SCHREGNO              VARCHAR(1), \
    INOUTCD               VARCHAR(1), \
    NAME                  VARCHAR(1), \
    NAME_SHOW             VARCHAR(1), \
    NAME_KANA             VARCHAR(1), \
    NAME_ENG              VARCHAR(1), \
    REAL_NAME             VARCHAR(1), \
    REAL_NAME_KANA        VARCHAR(1), \
    BIRTHDAY              VARCHAR(1), \
    SEX                   VARCHAR(1), \
    HANDICAP              VARCHAR(1), \
    BLOODTYPE             VARCHAR(1), \
    BLOOD_RH              VARCHAR(1), \
    PRISCHOOLCD           VARCHAR(1), \
    FINSCHOOLCD           VARCHAR(1), \
    FINISH_DATE           VARCHAR(1), \
    CURRICULUM_YEAR       VARCHAR(1), \
    ENT_DATE              VARCHAR(1), \
    ENT_DIV               VARCHAR(1), \
    ENT_REASON            VARCHAR(1), \
    ENT_SCHOOL            VARCHAR(1), \
    ENT_ADDR              VARCHAR(1), \
    ENT_ADDR2             VARCHAR(1), \
    GRD_DATE              VARCHAR(1), \
    GRD_DIV               VARCHAR(1), \
    GRD_REASON            VARCHAR(1), \
    GRD_SCHOOL            VARCHAR(1), \
    GRD_ADDR              VARCHAR(1), \
    GRD_ADDR2             VARCHAR(1), \
    GRD_NO                VARCHAR(1), \
    GRD_TERM              VARCHAR(1), \
    REMARK1               VARCHAR(1), \
    REMARK2               VARCHAR(1), \
    REMARK3               VARCHAR(1), \
    GRADE                 VARCHAR(1), \
    HR_CLASS              VARCHAR(1), \
    ATTENDNO              VARCHAR(1), \
    ANNUAL                VARCHAR(1), \
    COURSECD              VARCHAR(1), \
    MAJORCD               VARCHAR(1), \
    COURSECODE            VARCHAR(1), \
    STAFFNAME             VARCHAR(1), \
    ZIPCD                 VARCHAR(1), \
    AREACD                VARCHAR(1), \
    ADDR1                 VARCHAR(1), \
    ADDR2                 VARCHAR(1), \
    ADDR1_ENG             VARCHAR(1), \
    ADDR2_ENG             VARCHAR(1), \
    TELNO                 VARCHAR(1), \
    FAXNO                 VARCHAR(1), \
    EMAIL                 VARCHAR(1), \
    EMERGENCYCALL         VARCHAR(1), \
    EMERGENCYNAME         VARCHAR(1), \
    EMERGENCYRELA_NAME    VARCHAR(1), \
    EMERGENCYTELNO        VARCHAR(1), \
    EMERGENCYCALL2        VARCHAR(1), \
    EMERGENCYNAME2        VARCHAR(1), \
    EMERGENCYRELA_NAME2   VARCHAR(1), \
    EMERGENCYTELNO2       VARCHAR(1), \
    RELATIONSHIP          VARCHAR(1), \
    GUARD_NAME            VARCHAR(1), \
    GUARD_KANA            VARCHAR(1), \
    GUARD_SEX             VARCHAR(1), \
    GUARD_BIRTHDAY        VARCHAR(1), \
    GUARD_ZIPCD           VARCHAR(1), \
    GUARD_ADDR1           VARCHAR(1), \
    GUARD_ADDR2           VARCHAR(1), \
    GUARD_TELNO           VARCHAR(1), \
    GUARD_FAXNO           VARCHAR(1), \
    GUARD_E_MAIL          VARCHAR(1), \
    GUARD_JOBCD           VARCHAR(1), \
    GUARD_WORK_NAME       VARCHAR(1), \
    GUARD_WORK_TELNO      VARCHAR(1), \
    GUARANTOR_RELATIONSHIP VARCHAR(1), \
    GUARANTOR_NAME        VARCHAR(1), \
    GUARANTOR_KANA        VARCHAR(1), \
    GUARANTOR_SEX         VARCHAR(1), \
    GUARANTOR_ZIPCD       VARCHAR(1), \
    GUARANTOR_ADDR1       VARCHAR(1), \
    GUARANTOR_ADDR2       VARCHAR(1), \
    GUARANTOR_TELNO       VARCHAR(1), \
    GUARANTOR_JOBCD       VARCHAR(1), \
    PUBLIC_OFFICE         VARCHAR(1), \
    REGISTERCD            VARCHAR(10), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP, \ 
    BIBOUROKU             VARCHAR(1) \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KNJI100A_KAKIDASHI_LIST ADD CONSTRAINT PK_KNJI100A_K_L PRIMARY KEY (YEAR, DATA_DIV)

