-- $Id: 4a052d1794d2be6a0c31a88ff078768ea4178b21 $

drop table SCHREG_BRANCH_DAT
create table SCHREG_BRANCH_DAT( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    YEAR                varchar(4)  not null, \
    SCHREGNO            varchar(8)  not null, \
    BRANCHCD            varchar(2), \
    BRANCH_POSITION     varchar(2), \
    GUARD_NAME          varchar(120), \
    GUARD_KANA          varchar(240), \
    GUARD_ZIPCD         varchar(8), \
    GUARD_ADDR1         varchar(150), \
    GUARD_ADDR2         varchar(150), \
    GUARD_TELNO         varchar(14), \
    GUARD_TELNO2        varchar(14), \
    SEND_NAME           varchar(120), \
    RESIDENTCD          varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_BRANCH_DAT add constraint PK_SCHREG_BRANCH primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO)
