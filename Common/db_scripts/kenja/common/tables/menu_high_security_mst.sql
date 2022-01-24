-- $Id: 9603a35b5e4969f257ee12558eadc19e2a74bb39 $

drop table MENU_HIGH_SECURITY_MST
create table MENU_HIGH_SECURITY_MST( \
    MENUID          varchar(11)    not null, \
    SUBMENUID       varchar(1), \
    PARENTMENUID    varchar(11), \
    MENUNAME        varchar(60), \
    PROGRAMID       varchar(20), \
    PROGRAMPATH     varchar(40), \
    PROCESSCD       varchar(1), \
    INVALID_FLG     varchar(1), \
    PROGRAMMEMO     varchar(3000), \
    SHOWORDER       smallint, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MENU_HIGH_SECURITY_MST add constraint PK_MENU_H_S_MST primary key (MENUID)

