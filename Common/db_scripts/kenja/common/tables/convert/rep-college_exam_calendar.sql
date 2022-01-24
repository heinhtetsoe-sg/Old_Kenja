-- $Id: a70fd8fe9beccace78bdfee4ccf28dd2c0c2bb2b $

DROP TABLE COLLEGE_EXAM_CALENDAR_OLD
RENAME TABLE COLLEGE_EXAM_CALENDAR TO COLLEGE_EXAM_CALENDAR_OLD
create table COLLEGE_EXAM_CALENDAR( \
    YEAR                    varchar(4) not null, \
    SCHOOL_CD               varchar(8) not null, \
    FACULTYCD               varchar(3) not null, \
    DEPARTMENTCD            varchar(3) not null, \
    PROGRAM_CD              varchar(2) not null, \
    FORM_CD                 varchar(1) not null, \
    S_CD                    varchar(3) not null, \
    SCHOOL_NAME             varchar(120), \
    FACULTYNAME             varchar(120), \
    DEPARTMENTNAME          varchar(120), \
    PROGRAM_NAME            varchar(75), \
    FORM_NAME               varchar(75), \
    S_NAME                  varchar(75), \
    ADVERTISE_FLG           varchar(2), \
    BACHELOR_DIV            varchar(1), \
    PREF_CD                 varchar(2), \
    CENTER_PARTICIPATE      varchar(1), \
    WANTED_STUDENT_CNT      smallint, \
    JUDGE_DATE              varchar(3), \
    CENTER_JUDGE_B          varchar(5), \
    L_CD1                   varchar(2), \
    L_CD2                   varchar(2), \
    L_CD3                   varchar(2), \
    L_CD4                   varchar(2), \
    L_CD5                   varchar(2), \
    S_CD1                   varchar(3), \
    S_CD2                   varchar(3), \
    S_CD3                   varchar(3), \
    S_CD4                   varchar(3), \
    S_CD5                   varchar(3), \
    S_CD6                   varchar(3), \
    S_CD7                   varchar(3), \
    S_CD8                   varchar(3), \
    S_CD9                   varchar(3), \
    S_CD10                  varchar(3), \
    ADVERTISE_DIV           varchar(2) not null, \
    LIMIT_DATE_WEB          varchar(4), \
    LIMIT_DATE_WINDOW       varchar(4), \
    LIMIT_DATE_MAIL         varchar(4), \
    LIMIT_MAIL_DIV          varchar(1), \
    EXAM_DATE               varchar(4), \
    EXAM_PASS_DATE          varchar(4), \
    PROCEDURE_LIMIT_DATE    varchar(4), \
    ENT_MONEY               integer, \
    PROCEDURE_MONEY         integer, \
    TOTAL_MONEY             integer, \
    ACCEPTANCE_CRITERION_A  varchar(3), \
    ACCEPTANCE_CRITERION_B  varchar(3), \
    ACCEPTANCE_CRITERION_C  varchar(3), \
    ACCEPTANCE_CRITERION_D  varchar(3), \
    DOCKING_CRITERION_A     varchar(3), \
    DOCKING_CRITERION_B     varchar(3), \
    DOCKING_CRITERION_C     varchar(3), \
    DOCKING_CRITERION_D     varchar(3), \
    CENTER_CRITERION_A      integer, \
    CENTER_CRITERION_B      integer, \
    CENTER_CRITERION_C      integer, \
    CENTER_CRITERION_D      integer, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO COLLEGE_EXAM_CALENDAR \
    SELECT \
        YEAR, \
        SCHOOL_CD, \
        FACULTYCD , \
        DEPARTMENTCD, \
        PROGRAM_CD, \
        FORM_CD, \
        S_CD , \
        SCHOOL_NAME , \
        FACULTYNAME  , \
        DEPARTMENTNAME , \
        PROGRAM_NAME  , \
        FORM_NAME  , \
        S_NAME , \
        ADVERTISE_FLG   , \
        BACHELOR_DIV , \
        PREF_CD  , \
        CENTER_PARTICIPATE  , \
        WANTED_STUDENT_CNT, \
        JUDGE_DATE , \
        CENTER_JUDGE_B , \
        L_CD1 , \
        L_CD2 , \
        L_CD3 , \
        L_CD4 , \
        L_CD5 , \
        S_CD1 , \
        S_CD2 , \
        S_CD3 , \
        S_CD4 , \
        S_CD5 , \
        S_CD6 , \
        S_CD7 , \
        S_CD8 , \
        S_CD9 , \
        S_CD10 , \
        ADVERTISE_DIV , \
        CAST(NULL AS VARCHAR(4)) AS LIMIT_DATE_WEB, \
        LIMIT_DATE_WINDOW, \
        LIMIT_DATE_MAIL, \
        LIMIT_MAIL_DIV  , \
        EXAM_DATE    , \
        EXAM_PASS_DATE  , \
        PROCEDURE_LIMIT_DATE  , \
        ENT_MONEY , \
        PROCEDURE_MONEY  , \
        TOTAL_MONEY , \
        ACCEPTANCE_CRITERION_A , \
        ACCEPTANCE_CRITERION_B , \
        ACCEPTANCE_CRITERION_C , \
        ACCEPTANCE_CRITERION_D , \
        DOCKING_CRITERION_A , \
        DOCKING_CRITERION_B , \
        DOCKING_CRITERION_C , \
        DOCKING_CRITERION_D , \
        CENTER_CRITERION_A , \
        CENTER_CRITERION_B , \
        CENTER_CRITERION_C , \
        CENTER_CRITERION_D , \
        REGISTERCD, \
        UPDATED \
    FROM \
        COLLEGE_EXAM_CALENDAR_OLD

alter table COLLEGE_EXAM_CALENDAR add constraint PK_COLLEGE_CAL primary key (YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD, S_CD)
