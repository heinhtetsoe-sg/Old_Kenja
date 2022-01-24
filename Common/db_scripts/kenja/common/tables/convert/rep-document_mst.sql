-- $Id: 5a63cfac463704a0db401f1eb539f261330bcc85 $

drop table DOCUMENT_MST_OLD
create table DOCUMENT_MST_OLD like DOCUMENT_MST
insert into  DOCUMENT_MST_OLD select * from DOCUMENT_MST

drop   table DOCUMENT_MST
create table DOCUMENT_MST ( \
    DOCUMENTCD     varchar(2)  not null, \
    TITLE          varchar(120),  \
    CERTIF_NO      varchar(120),  \
    TEXT           varchar(1650), \
    FOOTNOTE       varchar(1518), \
    REGISTERCD     varchar(10), \
    UPDATED        timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DOCUMENT_MST ADD CONSTRAINT PK_DOCUMENT_MST PRIMARY KEY (DOCUMENTCD)

insert into DOCUMENT_MST \
    SELECT \
        DOCUMENTCD, \
        TITLE, \
        CERTIF_NO, \
        TEXT, \
        FOOTNOTE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        DOCUMENT_MST_OLD
