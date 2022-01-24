-- $Id: 1aec734df8435c7beb18a818fbb60f0d340f3f25 $

drop table CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT
create table CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT( \
    YEAR                                varchar(4)    not null, \
    KIND_NO                             varchar(4)    not null, \
    KIND_SEQ                            varchar(3)    not null, \
    KIND_NAME                           varchar(120), \
    STATUS_NAME1                        varchar(120), \
    STATUS_NAME2                        varchar(120), \
    STATUS_NAME3                        varchar(120), \
    STATUS_NAME4                        varchar(120), \
    REGISTERCD                          varchar(10), \
    UPDATED                             timestamp default current timestamp \ 
) in usr1dms index in idx1dms

alter table CHALLENGED_SUPPORTPLAN_KIND_NAME_DAT add constraint PK_CHA_SP_PL_KIND_NM_D primary key (YEAR, KIND_NO, KIND_SEQ)
