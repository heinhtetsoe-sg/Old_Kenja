-- $Id: 7f788e922d001b5e403d15657f54e48d7910fa19 $

drop table ADMIN_CONTROL_MEDEXAM_PRINT_DAT
create table ADMIN_CONTROL_MEDEXAM_PRINT_DAT ( \
    "YEAR"          VARCHAR(4)      NOT NULL, \
    "MEDEXAM_DIV"   VARCHAR(1)      NOT NULL, \
    "PROGRAMID"     VARCHAR(10)     NOT NULL, \
    "MEDEXAM_ITEM"  VARCHAR(2)      NOT NULL, \
    "SHOWORDER"     VARCHAR(2), \
    "REGISTERCD"    VARCHAR(10), \
    "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMIN_CONTROL_MEDEXAM_PRINT_DAT add constraint PK_ADMIN_CTRL_MED \
primary key (YEAR, MEDEXAM_DIV, PROGRAMID, MEDEXAM_ITEM)
