-- $Id: 9d21038d5efb008f66402710a2fdaafe55120207 $

drop   table SCHOOL_MST

create table SCHOOL_MST \
    (YEAR               VARCHAR (4) not null, \
     SCHOOLCD           VARCHAR(12) not null, \
     SCHOOL_KIND        VARCHAR(2)  not null, \
     FOUNDEDYEAR        VARCHAR (4), \
     PRESENT_EST        VARCHAR (3), \
     CLASSIFICATION     VARCHAR (6), \
     SCHOOLNAME1        VARCHAR (90), \
     SCHOOLNAME2        VARCHAR (90), \
     SCHOOLNAME3        VARCHAR (90), \
     SCHOOLNAME_ENG     VARCHAR (60), \
     SCHOOLZIPCD        VARCHAR (8), \
     SCHOOLADDR1        VARCHAR (150), \
     SCHOOLADDR2        VARCHAR (150), \
     SCHOOLADDR1_ENG    VARCHAR (150), \
     SCHOOLADDR2_ENG    VARCHAR (150), \
     SCHOOLTELNO        VARCHAR (14), \
     SCHOOLFAXNO        VARCHAR (14), \
     SCHOOLMAIL         VARCHAR (50), \
     SCHOOLURL          VARCHAR (50), \
     SCHOOLDIV          VARCHAR (1), \
     SEMESTERDIV        VARCHAR (1), \
     GRADE_HVAL         VARCHAR (2), \
     ENTRANCE_DATE      DATE, \
     GRADUATE_DATE      DATE, \
     GRAD_CREDITS       SMALLINT, \
     GRAD_COMP_CREDITS  SMALLINT, \
     SEMES_ASSESSCD     VARCHAR (1), \
     SEMES_FEARVAL      SMALLINT, \
     GRADE_FEARVAL      SMALLINT, \
     ABSENT_COV         VARCHAR (1), \
     ABSENT_COV_LATE    SMALLINT, \
     GVAL_CALC          VARCHAR (1), \
     SUB_OFFDAYS        VARCHAR (1), \
     SUB_ABSENT         VARCHAR (1), \
     SUB_SUSPEND        VARCHAR (1), \
     SUB_MOURNING       VARCHAR (1), \
     SUB_VIRUS          VARCHAR (1), \
     SEM_OFFDAYS        VARCHAR (1), \
     REGISTERCD         VARCHAR (10), \
     UPDATED timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHOOL_MST add constraint pk_school_mst primary key (YEAR, SCHOOLCD, SCHOOL_KIND)
