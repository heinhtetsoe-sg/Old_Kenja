-- $Id: c50552998d14eb23cd778bd4359243a7066e2b2f $

drop table CHALLENGED_CARD_NAME_MST

create table CHALLENGED_CARD_NAME_MST ( \
    CARDNAME_CD   VARCHAR(2)    NOT NULL, \
    CARDNAME      VARCHAR(90) , \
    REGISTERCD    VARCHAR(10)  , \
    UPDATED       TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table CHALLENGED_CARD_NAME_MST add constraint PK_CHA_CANAME_MST primary key (CARDNAME_CD)

