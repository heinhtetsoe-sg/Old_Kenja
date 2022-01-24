-- $Id: 5ff4a1471d4b81468c98813fb8c1540abd216404 $

drop table JVIEW_POINT_M_MST
create table JVIEW_POINT_M_MST( \
    POINT_DIV       varchar(2)  not null, \
    POINT_L_CD      varchar(2)  not null, \
    POINT_M_CD      varchar(2)  not null, \
    REMARK_M        varchar(210), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table JVIEW_POINT_M_MST add constraint PK_JVIEW_POINT_M primary key (POINT_DIV, POINT_L_CD, POINT_M_CD)
