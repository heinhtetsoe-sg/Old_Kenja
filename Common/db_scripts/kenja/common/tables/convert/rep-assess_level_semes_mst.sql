-- kanji=漢字
-- $Id: 2507dce9e8519e5fe0e4217b04bb4b23b70e188d $
-- テスト項目マスタ集計フラグ

DROP TABLE ASSESS_LEVEL_SEMES_MST_OLD
RENAME TABLE ASSESS_LEVEL_SEMES_MST TO ASSESS_LEVEL_SEMES_MST_OLD
create table ASSESS_LEVEL_SEMES_MST ( \
    YEAR            varchar(4)  not null, \
    SEMESTER        varchar(1)  not null, \
    TESTKINDCD      varchar(2)  not null, \
    TESTITEMCD      varchar(2)  not null, \
    CLASSCD         varchar(2)  not null, \
    SCHOOL_KIND     varchar(2)  not null, \
    CURRICULUM_CD   varchar(2)  not null, \
    SUBCLASSCD      varchar(6)  not null, \
    DIV             varchar(1)  not null, \
    GRADE           varchar(2)  not null, \
    HR_CLASS        varchar(3)  not null, \
    COURSECD        varchar(1)  not null, \
    MAJORCD         varchar(3)  not null, \
    COURSECODE      varchar(4)  not null, \
    ASSESSLEVEL     smallint not null, \
    ASSESSMARK      varchar(6), \
    ASSESSLOW       decimal, \
    ASSESSHIGH      decimal, \
    PERCENT         DECIMAL(4,1), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO ASSESS_LEVEL_SEMES_MST \
    SELECT \
        YEAR, \
        SEMESTER, \
        TESTKINDCD, \
        TESTITEMCD, \
        LEFT(SUBCLASSCD, 2) AS CLASSCD, \, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        DIV, \
        GRADE, \
        HR_CLASS, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        ASSESSLEVEL, \
        ASSESSMARK, \
        ASSESSLOW, \
        ASSESSHIGH, \
        PERCENT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ASSESS_LEVEL_SEMES_MST_OLD

alter table ASSESS_LEVEL_SEMES_MST add constraint pk_ass_l_s_mst \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, ASSESSLEVEL)
