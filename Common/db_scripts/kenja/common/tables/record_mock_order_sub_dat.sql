-- $Id: cb25116dbb0dd3ee1dd348d89c1df1d0f55b41aa $

drop table RECORD_MOCK_ORDER_SUB_DAT

create table RECORD_MOCK_ORDER_SUB_DAT ( \
    YEAR           VARCHAR(4)   NOT NULL, \
    GRADE          VARCHAR(2)   NOT NULL, \
    SEQ            SMALLINT     NOT NULL, \
    CLASSCD        VARCHAR(2), \
    SCHOOL_KIND    VARCHAR(2), \
    CURRICULUM_CD  VARCHAR(2), \
    SUBCLASSCD     VARCHAR(6), \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table RECORD_MOCK_ORDER_SUB_DAT add constraint PK_REC_MOCK_ORD_S primary key (YEAR,GRADE,SEQ)

