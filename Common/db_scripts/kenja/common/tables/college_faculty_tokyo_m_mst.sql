-- $Id: 4d5962d099bc5b1076dfe769351e68ec8428d207 $

drop table COLLEGE_FACULTY_TOKYO_M_MST

create table COLLEGE_FACULTY_TOKYO_M_MST( \
    SCHOOL_CATEGORY_CD  varchar(1)  not null, \
    TOKYO_L_CD          varchar(2)  not null, \
    TOKYO_M_CD          varchar(2)  not null, \
    TOKYO_M_NAME        varchar(90), \
    TOKYO_M_NAME_ABBV   varchar(90), \
    SHOWORDER           smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_FACULTY_TOKYO_M_MST add constraint PK_COLL_FAC_TO_MM \
primary key (SCHOOL_CATEGORY_CD, TOKYO_L_CD, TOKYO_M_CD)
