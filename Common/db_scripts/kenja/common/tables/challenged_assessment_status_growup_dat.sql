-- $Id: c26f67e29975484d4ec7b8340edab893205c2c38 $

drop table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT
create table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT( \
    YEAR                    varchar(4)    not null, \
    DATA_DIV                varchar(2)    not null, \
    SHEET_PATTERN           varchar(1), \
    DATA_DIV_NAME           varchar(150), \
    STATUS_NAME             varchar(90), \
    GROWUP_NAME             varchar(90), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT add constraint PK_CHA_AS_SG_D primary key (YEAR, DATA_DIV)