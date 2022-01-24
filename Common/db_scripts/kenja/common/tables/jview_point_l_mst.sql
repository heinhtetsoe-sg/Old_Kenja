-- $Id: 5a4643ea67da922d608a6fd88428241eaddfc8b5 $

drop table JVIEW_POINT_L_MST
create table JVIEW_POINT_L_MST( \
    POINT_DIV       varchar(2)  not null, \
    POINT_L_CD      varchar(2)  not null, \
    REMARK_L        varchar(30), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table JVIEW_POINT_L_MST add constraint PK_JVIEW_POINT_L primary key (POINT_DIV, POINT_L_CD)
