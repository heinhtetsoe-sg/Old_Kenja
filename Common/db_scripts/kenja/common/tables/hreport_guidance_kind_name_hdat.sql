-- $Id: 4f7dfc8d8ffe5f4a321f699d0c40e57979715f9e $

drop table HREPORT_GUIDANCE_KIND_NAME_HDAT

create table HREPORT_GUIDANCE_KIND_NAME_HDAT( \
    YEAR                varchar(4)   not null, \
    KIND_NO             varchar(4)   not null, \
    KIND_NAME           varchar(120) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_KIND_NAME_HDAT add constraint PK_HREP_G_K_N_HD primary key (YEAR, KIND_NO)
