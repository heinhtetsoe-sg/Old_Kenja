-- $Id: 4ece681733ff5edcc68980b478f37422b92bb2cf $

drop table HREPORT_GUIDANCE_SELFRELIANCE_MST

create table HREPORT_GUIDANCE_SELFRELIANCE_MST( \
    YEAR                varchar(4) not null, \
    SELF_DIV            varchar(1) not null, \
    SELF_SEQ            varchar(2) not null, \
    SELF_TITLE          varchar(60) not null, \
    SELF_ITEM           varchar(3) not null, \
    SELF_CONTENT        varchar(150) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_SELFRELIANCE_MST add constraint PK_HREP_GUID_SELFRELIANCE_MST primary key (YEAR, SELF_DIV, SELF_SEQ)
