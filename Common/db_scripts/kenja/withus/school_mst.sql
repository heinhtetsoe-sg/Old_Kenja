-- $Id: school_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHOOL_MST
create table SCHOOL_MST \
    (YEAR               varchar(4) not null, \
     SCHOOLCD           varchar(11) not null, \
     FOUNDEDYEAR        varchar(4), \
     PRESENT_EST        varchar(3), \
     CLASSIFICATION     varchar(6), \
     SCHOOLNAME1        varchar(90), \
     SCHOOLNAME2        varchar(90), \
     SCHOOLNAME3        varchar(90), \
     SCHOOLNAME_ENG     varchar(60), \
     SCHOOLZIPCD        varchar(8), \
     SCHOOLADDR1        varchar(75), \
     SCHOOLADDR2        varchar(75), \
     SCHOOLADDR3        varchar(75), \
     SCHOOLADDR1_ENG    varchar(50), \
     SCHOOLADDR2_ENG    varchar(50), \
     SCHOOLTELNO        varchar(14), \
     SCHOOLTELNO_SEARCH varchar(14), \
     SCHOOLFAXNO        varchar(14), \
     SCHOOLMAIL         varchar(25), \
     SCHOOLURL          varchar(30), \
     SCHOOLDIV          varchar(1), \
     SEMESTERDIV        varchar(1), \
     GRADE_HVAL         varchar(2), \
     ENTRANCE_DATE      date, \
     GRADUATE_DATE      date, \
     GRAD_CREDITS       smallint, \
     SEMES_ASSESSCD     varchar(1), \
     SEMES_FEARVAL      smallint, \
     GRADE_FEARVAL      smallint, \
     ABSENT_COV         varchar(1), \
     ABSENT_COV_LATE    smallint, \
     SUB_OFFDAYS        varchar (1), \
     SUB_ABSENT         varchar (1), \
     SUB_SUSPEND        varchar (1), \
     SUB_MOURNING       varchar (1), \
     SUB_VIRUS          varchar (1), \
     SEM_OFFDAYS        varchar (1), \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SCHOOL_MST add constraint pk_school_mst primary key(YEAR)
