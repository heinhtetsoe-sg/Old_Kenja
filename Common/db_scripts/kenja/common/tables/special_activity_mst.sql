-- $Id: 2615a18e1c30aca7f4f9269a87d12c74ed6a55c1 $

drop table SPECIAL_ACTIVITY_MST

create table SPECIAL_ACTIVITY_MST( \
    YEAR                 varchar(4)  not null , \ 
    SPECIALCD            varchar(3)  not null , \ 
    SPECIAL_SDATE        date                 , \ 
    SPECIAL_EDATE        date                 , \ 
    SPECIALACTIVITYNAME  varchar(90)          , \ 
    SPECIALACTIVITYTIME  varchar(3)           , \ 
    REGISTERCD           varchar(10)          , \ 
    UPDATED              timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table SPECIAL_ACTIVITY_MST add constraint PK_SPECIAL_ACTIVITY_MST primary key (YEAR, SPECIALCD)
