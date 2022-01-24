-- $Id: 4213f2bb9ae91ba3468ebcc192da3df168e776d3 $

drop table COLLEGE_SCHOOL_CATEGORY_MST

create table COLLEGE_SCHOOL_CATEGORY_MST( \
    SCHOOL_CATEGORY_CD  varchar(1)  not null, \
    CATEGORY_NAME       varchar(90), \
    CATEGORY_NAME_ABBV  varchar(90), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_SCHOOL_CATEGORY_MST add constraint PK_COLL_SCH_CAT_M \
primary key (SCHOOL_CATEGORY_CD)
