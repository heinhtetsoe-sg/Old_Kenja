-- $Id: 667495142f331522de51c4277f81869f927c6490 $

drop table CLUB_DETAIL_DAT

create table CLUB_DETAIL_DAT \
(  \
        "YEAR"                  varchar(4)      not null, \
        "SCHOOLCD"              varchar(12)     not null, \
        "SCHOOL_KIND"           varchar(2)      not null, \
        "CLUBCD"                varchar(4)      not null, \
        "SEQ"                   varchar(3)      not null, \
        "REMARK1"               varchar(100), \
        "REMARK2"               varchar(100), \
        "REMARK3"               varchar(100), \
        "REMARK4"               varchar(100), \
        "REMARK5"               varchar(100), \
        "REMARK6"               varchar(100), \
        "REMARK7"               varchar(100), \
        "REMARK8"               varchar(100), \
        "REMARK9"               varchar(100), \
        "REMARK10"              varchar(100), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table CLUB_DETAIL_DAT  \
add constraint PK_CLUB_DETAIL_DAT  \
primary key  \
(YEAR, SCHOOLCD, SCHOOL_KIND, CLUBCD, SEQ)
