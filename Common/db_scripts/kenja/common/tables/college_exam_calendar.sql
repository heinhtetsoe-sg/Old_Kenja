-- $Id: 5bf59b91924e6a3516100b626dd20d506a41b5ac $

drop table COLLEGE_EXAM_CALENDAR
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
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_EXAM_CALENDAR add constraint PK_COLLEGE_CAL primary key (YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD, S_CD)
