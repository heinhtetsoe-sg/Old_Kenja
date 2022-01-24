-- $Id: e2a2b8b2d6d5933dade44d814635789d78eb4559 $

drop table SPECIAL_ACTIVITY_DAT

create table SPECIAL_ACTIVITY_DAT( \
    YEAR        varchar(4)     not null , \ 
    SPECIALCD   varchar(3)     not null , \ 
    SCHREGNO    varchar(8)     not null , \ 
    SPECIAL_FLG varchar(1)              , \ 
    REGISTERCD  varchar(10)             , \ 
    UPDATED     timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table SPECIAL_ACTIVITY_DAT add constraint PK_SPECIAL_ACTIVITY_DAT primary key (YEAR, SPECIALCD, SCHREGNO)
