-- $Id: schreg_textbook_free_apply_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_TEXTBOOK_FREE_APPLY_DAT
create table SCHREG_TEXTBOOK_FREE_APPLY_DAT ( \
     SCHREGNO           varchar(8)  not null, \
     YEAR               varchar(4)  not null, \
     REGISTER_DATE      DATE        not null, \
     TOTAL_GK           INT, \
     TOTAL_COUNT        SMALLINT, \
     BOOKDIV1_GK        INT, \
     BOOKDIV1_COUNT     SMALLINT, \
     BOOKDIV2_GK        INT, \
     BOOKDIV2_COUNT     SMALLINT, \
     PROVIDE_REASON     VARCHAR(1), \
     ATTACH_DOCUMENTS   VARCHAR(75), \
     REMARK             VARCHAR(75), \
     JUDGE_RESULT       VARCHAR(1), \
     DECISION_DATE      DATE, \
     REGISTERCD         VARCHAR(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SCHREG_TEXTBOOK_FREE_APPLY_DAT add constraint PK_SCH_TEXT_FA_DAT primary key(SCHREGNO, YEAR, REGISTER_DATE)
