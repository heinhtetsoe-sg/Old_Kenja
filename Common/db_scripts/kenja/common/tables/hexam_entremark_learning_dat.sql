-- $Id: 5179358accca646a8ba64db2bfec1a319a295b0f $

drop table HEXAM_ENTREMARK_LEARNING_DAT
create table HEXAM_ENTREMARK_LEARNING_DAT( \
    YEAR                    varchar(4)    not null, \
    SCHREGNO                varchar(8)    not null, \
    REMARK                  varchar(5500), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HEXAM_ENTREMARK_LEARNING_DAT add constraint PK_HEX_ENT_LEARNI primary key(YEAR, SCHREGNO)
