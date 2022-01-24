-- $Id: 05893c603e69ab560c156398ae2fcbdf71f37e2d $

drop table HREPORT_GUIDANCE_KIND_NAME_DAT

create table HREPORT_GUIDANCE_KIND_NAME_DAT( \
    YEAR                varchar(4)   not null, \
    KIND_NO             varchar(4)   not null, \
    KIND_SEQ            varchar(3)   not null, \
    KIND_REMARK         varchar(45), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_KIND_NAME_DAT add constraint PK_HREP_GU_K_N_D primary key (YEAR, KIND_NO, KIND_SEQ)
