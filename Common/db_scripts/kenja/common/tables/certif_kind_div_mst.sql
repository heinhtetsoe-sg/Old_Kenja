-- $Id: b6f301c39c8b2eb241b31667530702e8cb529298 $

DROP TABLE CERTIF_KIND_DIV_MST

CREATE TABLE CERTIF_KIND_DIV_MST ( \
    CERTIF_DIV      varchar(2) not null, \
    CERTIF_DIV_NAME varchar(60) not null, \
    REMARK1         varchar(30), \
    REMARK2         varchar(30), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
)

alter table CERTIF_KIND_DIV_MST add constraint PK_CERT_KIND_DM \
primary key (CERTIF_DIV)
