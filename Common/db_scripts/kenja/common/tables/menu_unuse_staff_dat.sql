-- $Id: 7e905b32d84a4643990d3b1e8a9594aef335d3f3 $

drop table MENU_UNUSE_STAFF_DAT
create table MENU_UNUSE_STAFF_DAT( \
    YEAR        varchar(4)  not null, \
    STAFFCD     varchar(8)  not null, \
    MENUID      varchar(11) not null, \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MENU_UNUSE_STAFF_DAT add constraint PK_MENU_UNUSE primary key (YEAR, STAFFCD, MENUID)
