-- $Id: 0edda2f5a83bf1afc1af8d3050d5d3ee3edf68a4 $

drop table COLLEGE_FACULTY_SYSTEM_MST

create table COLLEGE_FACULTY_SYSTEM_MST( \
    SCHOOL_CD           varchar(8)  not null, \
    FACULTYCD           varchar(3)  not null, \
    SYSTEM_CD           varchar(2) , \
    PROTECTION_FLG      varchar(1) , \
    SCHOOL_CATEGORY_CD  varchar(1) , \
    TOKYO_L_CD          varchar(2) , \
    TOKYO_M_CD          varchar(2) , \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_FACULTY_SYSTEM_MST add constraint PK_COLL_FAC_SYS_M \
primary key (SCHOOL_CD, FACULTYCD)
