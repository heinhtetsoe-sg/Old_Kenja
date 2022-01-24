-- $Id: 1e04e4199904dd7bdeb8701dff5333cf0c81ae09 $

drop table SCHREG_TEXTBOOK_CHKFIN_DAT
create table SCHREG_TEXTBOOK_CHKFIN_DAT ( \
     SCHREGNO       varchar(8)  not null, \
     YEAR           varchar(4)  not null, \
     EXECUTED       varchar(1), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SCHREG_TEXTBOOK_CHKFIN_DAT add constraint pk_text_chkfin primary key(SCHREGNO, YEAR)
