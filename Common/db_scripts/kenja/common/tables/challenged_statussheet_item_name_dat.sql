-- $Id: cf7ab787e419fa07af1a740531236a8072c7f55b $

drop table CHALLENGED_STATUSSHEET_ITEM_NAME_DAT
create table CHALLENGED_STATUSSHEET_ITEM_NAME_DAT( \
    YEAR                    varchar(4)    not null, \
    DATA_DIV                varchar(2)    not null, \
    SHEET_PATTERN           varchar(1), \
    DATA_DIV_NAME           varchar(150), \
    STATUS_NAME             varchar(90), \
    GROWUP_NAME             varchar(90), \
    COMMENTS                varchar(1000), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table CHALLENGED_STATUSSHEET_ITEM_NAME_DAT add constraint PK_CHA_STATSHEET_ITEM_NM_D primary key (YEAR, DATA_DIV)