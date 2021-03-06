-- $Id: 0d43d1f1177198b4404c05a18f6115662395718a $

drop table SCHOOL_DIARY_DETAIL_DAT_OLD
create table SCHOOL_DIARY_DETAIL_DAT_OLD like SCHOOL_DIARY_DETAIL_DAT
insert into SCHOOL_DIARY_DETAIL_DAT_OLD select * from SCHOOL_DIARY_DETAIL_DAT

drop table SCHOOL_DIARY_DETAIL_DAT

CREATE TABLE SCHOOL_DIARY_DETAIL_DAT( \
    SCHOOLCD          VARCHAR(12) NOT NULL, \
    SCHOOL_KIND       VARCHAR(2)  NOT NULL, \
    DIARY_DATE        DATE        NOT NULL, \
    STAFF_DIV         VARCHAR(2)  NOT NULL, \
    GRADE             VARCHAR(2)  NOT NULL, \
    HR_CLASS          VARCHAR(3)  NOT NULL, \
    STAFFCD           VARCHAR(10)  NOT NULL, \
    COUNT             SMALLINT, \
    REGISTERCD        VARCHAR(10), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO SCHOOL_DIARY_DETAIL_DAT \
    SELECT \
        '000000000000', \
        'H', \
        DIARY_DATE, \
        STAFF_DIV, \
        GRADE, \
        HR_CLASS, \
        STAFFCD, \
        COUNT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SCHOOL_DIARY_DETAIL_DAT_OLD

ALTER TABLE SCHOOL_DIARY_DETAIL_DAT ADD CONSTRAINT PK_SCHOOL_DIARY_DE PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, DIARY_DATE, STAFF_DIV, GRADE, HR_CLASS, STAFFCD)
