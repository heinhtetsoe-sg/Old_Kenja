-- $Id: d8885b1fe5121c7bda723b289ff233f34fea8dae $

drop table TEXTBOOK_MST_OLD
create table TEXTBOOK_MST_OLD like TEXTBOOK_MST
insert into TEXTBOOK_MST_OLD select * from TEXTBOOK_MST

drop table TEXTBOOK_MST

create table TEXTBOOK_MST \
    (TEXTBOOKCD          varchar(12) not null, \
     TEXTBOOKDIV         varchar(1), \
     TEXTBOOKNAME        varchar(90), \
     TEXTBOOKABBV        varchar(15), \
     TEXTBOOKMK          varchar(9), \
     TEXTBOOKMS          varchar(3), \
     TEXTBOOKWRITINGNAME varchar(60), \
     TEXTBOOKPRICE       smallint, \
     TEXTBOOKUNITPRICE   smallint, \
     ISSUECOMPANYCD      varchar(4), \
     ISSUECOMPANY        varchar(45), \
     CONTRACTORNAME      varchar(45), \
     REMARK              varchar(60), \
     REGISTERCD          varchar(8), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table TEXTBOOK_MST add constraint PK_TEXTBOOK_MST primary key (textbookcd)

insert into TEXTBOOK_MST \
select \
    right(rtrim('000000000000'||TEXTBOOKCD),12) as TEXTBOOKCD, \
    TEXTBOOKDIV, \
    TEXTBOOKNAME, \
    TEXTBOOKABBV, \
    TEXTBOOKMK, \
    TEXTBOOKMS, \
    TEXTBOOKWRITINGNAME, \
    TEXTBOOKPRICE, \
    TEXTBOOKUNITPRICE, \
    ISSUECOMPANYCD, \
    cast(null as varchar(45)), \
    cast(null as varchar(45)), \
    REMARK, \
    REGISTERCD, \
    UPDATED \
from TEXTBOOK_MST_OLD
