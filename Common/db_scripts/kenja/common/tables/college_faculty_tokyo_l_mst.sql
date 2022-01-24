-- $Id: 29e3844252788dca5ee87c83c4e76087ebf032c8 $

drop table COLLEGE_FACULTY_TOKYO_L_MST

create table COLLEGE_FACULTY_TOKYO_L_MST( \
    SCHOOL_CATEGORY_CD  varchar(1)  not null, \
    TOKYO_L_CD          varchar(2)  not null, \
    TOKYO_L_NAME        varchar(90), \
    TOKYO_L_NAME_ABBV   varchar(90), \
    SHOWORDER           smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_FACULTY_TOKYO_L_MST add constraint PK_COLL_FAC_TO_LM \
primary key (SCHOOL_CATEGORY_CD, TOKYO_L_CD)
