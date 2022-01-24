-- kanji=´Á»ú
-- $Id: b15a5066011e45767bd2165dd188c0cd48e04a81 $

drop table KNJI100A_KAKIDASHI_LIST_OLD
create table KNJI100A_KAKIDASHI_LIST_OLD like KNJI100A_KAKIDASHI_LIST
insert into KNJI100A_KAKIDASHI_LIST_OLD select * from KNJI100A_KAKIDASHI_LIST

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

INSERT INTO KNJI100A_KAKIDASHI_LIST \
    SELECT \
        YEAR, \
        DATA_DIV, \
        SCHREGNO, \
        INOUTCD, \
        NAME, \
        NAME_SHOW, \
        NAME_KANA, \
        NAME_ENG, \
        REAL_NAME, \
        REAL_NAME_KANA, \
        BIRTHDAY, \
        SEX, \
        HANDICAP, \
        BLOODTYPE, \
        BLOOD_RH, \
        PRISCHOOLCD, \
        FINSCHOOLCD, \
        FINISH_DATE, \
        CURRICULUM_YEAR, \
        ENT_DATE, \
        ENT_DIV, \
        ENT_REASON, \
        ENT_SCHOOL, \
        ENT_ADDR, \
        ENT_ADDR2, \
        GRD_DATE, \
        GRD_DIV, \
        GRD_REASON, \
        GRD_SCHOOL, \
        GRD_ADDR, \
        GRD_ADDR2, \
        GRD_NO, \
        GRD_TERM, \
        REMARK1, \
        REMARK2, \
        REMARK3, \
        GRADE, \
        HR_CLASS, \
        ATTENDNO, \
        ANNUAL, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        STAFFNAME, \
        ZIPCD, \
        AREACD, \
        ADDR1, \
        ADDR2, \
        ADDR1_ENG, \
        ADDR2_ENG, \
        TELNO, \
        FAXNO, \
        EMAIL, \
        EMERGENCYCALL, \
        EMERGENCYNAME, \
        EMERGENCYRELA_NAME, \
        EMERGENCYTELNO, \
        EMERGENCYCALL2, \
        EMERGENCYNAME2, \
        EMERGENCYRELA_NAME2, \
        EMERGENCYTELNO2, \
        RELATIONSHIP, \
        GUARD_NAME, \
        GUARD_KANA, \
        GUARD_SEX, \
        GUARD_BIRTHDAY, \
        GUARD_ZIPCD, \
        GUARD_ADDR1, \
        GUARD_ADDR2, \
        GUARD_TELNO, \
        GUARD_FAXNO, \
        GUARD_E_MAIL, \
        GUARD_JOBCD, \
        GUARD_WORK_NAME, \
        GUARD_WORK_TELNO, \
        GUARANTOR_RELATIONSHIP, \
        GUARANTOR_NAME, \
        GUARANTOR_KANA, \
        GUARANTOR_SEX, \
        GUARANTOR_ZIPCD, \
        GUARANTOR_ADDR1, \
        GUARANTOR_ADDR2, \
        GUARANTOR_TELNO, \
        GUARANTOR_JOBCD, \
        PUBLIC_OFFICE, \
        REGISTERCD, \
        UPDATED, \
        CAST(NULL AS VARCHAR(1)) AS BIBOUROKU \
    FROM \
        KNJI100A_KAKIDASHI_LIST_OLD

ALTER TABLE KNJI100A_KAKIDASHI_LIST ADD CONSTRAINT PK_KNJI100A_K_L PRIMARY KEY (YEAR, DATA_DIV)
