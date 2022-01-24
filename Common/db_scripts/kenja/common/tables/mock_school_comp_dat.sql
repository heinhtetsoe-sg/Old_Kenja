-- $Id: 11e17a282406117e4d4b794ae0b4d3d85c86f496 $

DROP TABLE MOCK_SCHOOL_COMP_DAT

CREATE TABLE MOCK_SCHOOL_COMP_DAT ( \
    IMPORT_NO       int             NOT NULL, \
    ROW_NO          int             NOT NULL, \
    YEAR            varchar(4)      NOT NULL, \
    GAKKONAME       varchar(120)    NOT NULL, \
    GAKKOCODE       varchar(6)      NOT NULL, \
    GRADE           varchar(30)     NOT NULL, \
    MOCKCD          varchar(9)      NOT NULL, \
    MOCKNAME        varchar(60)     NOT NULL, \
    SUBCLASS_CD     varchar(10), \
    SUBCLASS_NAME   varchar(45)     NOT NULL, \
    EXAM_COUNT      smallint, \
    AVERAGE         decimal(6, 2), \
    STD_DEVIATION   decimal(6, 2), \
    AVG_DEVIATION   decimal(6, 2), \
    PERFECT         smallint, \
    SIMPLE_80       smallint, \
    SIMPLE_75       smallint, \
    SIMPLE_70       smallint, \
    SIMPLE_65       smallint, \
    SIMPLE_60       smallint, \
    SIMPLE_55       smallint, \
    SIMPLE_50       smallint, \
    SIMPLE_45       smallint, \
    SIMPLE_40       smallint, \
    SIMPLE_35       smallint, \
    SIMPLE_30       smallint, \
    SIMPLE_LOW      smallint, \
    TOTAL_80        smallint, \
    TOTAL_75        smallint, \
    TOTAL_70        smallint, \
    TOTAL_65        smallint, \
    TOTAL_60        smallint, \
    TOTAL_55        smallint, \
    TOTAL_50        smallint, \
    TOTAL_45        smallint, \
    TOTAL_40        smallint, \
    TOTAL_35        smallint, \
    TOTAL_30        smallint, \
    TOTAL_LOW       smallint \
)

alter table MOCK_SCHOOL_COMP_DAT add constraint PK_MOCK_SCHOOL_COM \
primary key (YEAR, GAKKONAME, GAKKOCODE, GRADE, MOCKNAME, SUBCLASS_NAME)
