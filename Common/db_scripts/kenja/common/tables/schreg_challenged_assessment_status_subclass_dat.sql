-- $Id: 59b0f4b1dd02ec361c6080c21c9edfb9aa637d43 $

drop table SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT
create table SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT( \
    YEAR                                varchar(4)    not null, \
    SCHREGNO                            varchar(8)    not null, \
    RECORD_DATE                         varchar(10)   not null, \
    CLASSCD                             varchar(2)    not null, \
    SCHOOL_KIND                         varchar(2)    not null, \
    CURRICULUM_CD                       varchar(2)    not null, \
    SUBCLASSCD                          varchar(6)    not null, \
    STATUS                              varchar(4000), \
    FUTURE_CARE                         varchar(1800), \
    REGISTERCD                          varchar(10), \
    UPDATED                             timestamp default current timestamp \ 
) in usr16dms index in idx1dms

alter table SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT add constraint PK_SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT PRIMARY KEY (YEAR, SCHREGNO, RECORD_DATE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)