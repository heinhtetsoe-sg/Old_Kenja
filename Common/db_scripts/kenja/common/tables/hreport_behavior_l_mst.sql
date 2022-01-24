-- $Id: 8bf1e62f4d699d6c8b7eef8bb4d90bc330bfbf35 $

drop table HREPORT_BEHAVIOR_L_MST
create table HREPORT_BEHAVIOR_L_MST( \
    YEAR        varchar(4)  not null, \
    SCHOOL_KIND varchar(2)  not null, \
    GRADE       varchar(2)  not null, \
    L_CD        varchar(2)  not null, \
    L_NAME      varchar(75) not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_BEHAVIOR_L_MST add constraint PK_HREPBEH_L_M primary key (YEAR, SCHOOL_KIND, GRADE, L_CD)
