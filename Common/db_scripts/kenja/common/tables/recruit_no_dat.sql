-- $Id: e77f80f9dbc9b99d5623b339b4f2a6f09eb15b7e $

drop table RECRUIT_NO_DAT

create table RECRUIT_NO_DAT( \
    RECRUIT_NO  VARCHAR(8)   NOT NULL, \
    REGISTERCD  VARCHAR(10) , \
    UPDATED     TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table RECRUIT_NO_DAT add constraint PK_RECRUIT_NO_D primary key (RECRUIT_NO)

