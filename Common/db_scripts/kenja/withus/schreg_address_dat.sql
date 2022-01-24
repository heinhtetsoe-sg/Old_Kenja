-- $Id: schreg_address_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $


drop   table SCHREG_ADDRESS_DAT
create table SCHREG_ADDRESS_DAT \
        (SCHREGNO     varchar(8) not null, \
         ISSUEDATE    date       not null, \
         EXPIREDATE   date      , \
         AREACD       varchar(2), \
         ZIPCD        varchar(8), \
         PREF_CD      varchar(2), \
         ADDR1        varchar(75), \
         ADDR2        varchar(75), \
         ADDR3        varchar(75), \
         ADDR1_ENG    varchar(75), \
         ADDR2_ENG    varchar(75), \
         TELNO        varchar(14), \
         TELNO_SEARCH varchar(14), \
         FAXNO        varchar(14), \
         EMAIL        varchar(20), \
         REGISTERCD   varchar(8), \
         UPDATED      timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table SCHREG_ADDRESS_DAT add constraint PK_SCHREG_ADDRESS primary key (SCHREGNO, ISSUEDATE)


