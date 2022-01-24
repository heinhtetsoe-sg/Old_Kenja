-- $Id: 3b93d9bc22ea376d000405a74c32e48e0d7ceae7 $

drop table DOCUMENT_MST

create table DOCUMENT_MST \
    (DOCUMENTCD     varchar(2)  not null, \
     TITLE          varchar(120), \
     CERTIF_NO      varchar(120),  \
     TEXT           varchar(1650), \
     FOOTNOTE       varchar(1518), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table DOCUMENT_MST add constraint PK_DOCUMENT_MST primary key \
    (DOCUMENTCD)


