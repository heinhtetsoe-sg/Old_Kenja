-- $Id: a9452020dce824abc7c9774dc822e9e4b42f836c $

drop table LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT

create table LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT \
( \
SCHOOLCD    varchar(12) not null, \
SCHOOL_KIND varchar(2) not null, \
YEAR        varchar(4) not null, \
AUTO_NO     varchar(3) not null, \
SCHREGNO    varchar(8) not null, \
REGISTERCD  varchar(10), \
UPDATED     timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT add constraint PK_LEVY_INC_AT_S primary key (SCHOOLCD, SCHOOL_KIND, YEAR, AUTO_NO, SCHREGNO)
