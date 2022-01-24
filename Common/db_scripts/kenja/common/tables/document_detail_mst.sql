-- $Id: 65dd2428aede3dc9ad23898337bbcb9083bfb22f $

drop table DOCUMENT_DETAIL_MST

create table DOCUMENT_DETAIL_MST \
    (DOCUMENTCD     varchar(2)  not null, \
     SEQ            varchar(3)  not null, \
     TITLE          varchar(120),  \
     TEXT           varchar(1518), \
     FOOTNOTE       varchar(1518), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table DOCUMENT_DETAIL_MST add constraint PK_DOCUMENT_D_MST primary key \
    (DOCUMENTCD, SEQ)
