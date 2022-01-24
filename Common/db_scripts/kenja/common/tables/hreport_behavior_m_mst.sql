-- $Id: 2ee14f3ec7f121847ca9e73ef53ad3dd3c059cdd $

drop table HREPORT_BEHAVIOR_M_MST
create table HREPORT_BEHAVIOR_M_MST( \
    YEAR        varchar(4)  not null, \
    SCHOOL_KIND varchar(2)  not null, \
    GRADE       varchar(2)  not null, \
    L_CD        varchar(2)  not null, \
    M_CD        varchar(2)  not null, \
    M_NAME      varchar(120) not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_BEHAVIOR_M_MST add constraint PK_HREPBEH_M_M primary key (YEAR, SCHOOL_KIND, GRADE, L_CD, M_CD)
