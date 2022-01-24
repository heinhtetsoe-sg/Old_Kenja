-- $Id: b28c391294d67a0455c00b1e3ebf436a512123a3 $

drop table ENTEXAM_CONFRPT_BASE_MST

create table ENTEXAM_CONFRPT_BASE_MST( \
    ENTEXAMYEAR          varchar(4) NOT NULL, \
    TESTDIV              varchar(2) NOT NULL, \
    HOPE_COURSECODE      varchar(4) NOT NULL, \
    CLASS_SCORE          varchar(2), \
    SCORE5               smallint, \
    SCORE9               smallint, \
    HEALTH_PE_DISREGARD  varchar(1), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_CONFRPT_BASE_MST add constraint PK_EXAM_CONFRPT_BM primary key (ENTEXAMYEAR, TESTDIV, HOPE_COURSECODE)