-- $Id: 02653ff803044bfc9a11214d2002a466f5342471 $

drop table SCHREGNO_MAPPING_DAT

create table SCHREGNO_MAPPING_DAT( \
    SCHREGNO             varchar(8)  not null , \ 
    SCHREGNO_OLD         varchar(8)           , \ 
    REGISTERCD           varchar(10)          , \ 
    UPDATED              timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table SCHREGNO_MAPPING_DAT add constraint PK_SCH_MAP_D primary key (SCHREGNO)
