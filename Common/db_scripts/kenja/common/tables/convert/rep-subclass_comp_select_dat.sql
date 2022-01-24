-- kanji=漢字
-- $Id: 7279496c9f8b404b3a4d3d74a0a2a9405a3c6c35 $

DROP TABLE SUBCLASS_COMP_SELECT_DAT_OLD
RENAME TABLE SUBCLASS_COMP_SELECT_DAT TO SUBCLASS_COMP_SELECT_DAT_OLD
CREATE TABLE SUBCLASS_COMP_SELECT_DAT( \
    YEAR            varchar(4)  not null, \
    GRADE           varchar(2)  not null, \
    COURSECD        varchar(1)  not null, \
    MAJORCD         varchar(3)  not null, \
    COURSECODE      varchar(4)  not null, \
    GROUPCD         varchar(3)  not null, \
    CLASSCD         varchar(2)  not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    CURRICULUM_CD   varchar(2)  not null, \
    SUBCLASSCD      varchar(6)  not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms


INSERT INTO SUBCLASS_COMP_SELECT_DAT \
    SELECT \
        T1.YEAR, \
        T1.GRADE, \
        CM.COURSECD, \
        CM.MAJORCD, \
        CC.COURSECODE, \
        T1.GROUPCD, \
        T1.CLASSCD, \
        T1.SCHOOL_KIND, \
        T1.CURRICULUM_CD, \
        T1.SUBCLASSCD, \
        T1.REGISTERCD, \
        T1.UPDATED \
    FROM \
        SUBCLASS_COMP_SELECT_DAT_OLD T1 \
        LEFT JOIN V_COURSE_MAJOR_MST CM ON T1.YEAR = CM.YEAR \
        LEFT JOIN V_COURSECODE_MST CC ON T1.YEAR = CC.YEAR
        
alter table SUBCLASS_COMP_SELECT_DAT add constraint PK_SUBCLASS_CSD \
primary key (YEAR,GRADE, COURSECD, MAJORCD, COURSECODE,GROUPCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)
