-- $Id: 79865ebcbe3ea12f579acb80ef8ca9098729ba84 $

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
