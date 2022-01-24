-- $Id: ce93bf0bce3990dcfa112651f3ab4da0b3eb614d $

drop table CERTIF_ISSUE_SUBCLASS_DAT

create table CERTIF_ISSUE_SUBCLASS_DAT \
    (YEAR           varchar(4) not null, \
     CERTIF_INDEX   varchar(5) not null, \
     CLASSCD        varchar(2) not null, \
     SCHOOL_KIND    varchar(2) not null, \
     CURRICULUM_CD  varchar(2) not null, \
     SUBCLASSCD     varchar(6) not null, \
     SCHREGNO       varchar(8), \
     CERTIF_KINDCD  varchar(3), \
     GRADUATE_FLG   varchar(1), \
     APPLYDATE      date, \
     ISSUERNAME     varchar(40), \
     ISSUECD        varchar(1), \
     CERTIF_NO      smallint, \
     ISSUEDATE      date, \
     CHARGE         varchar(1), \
     PRINTCD        varchar(1), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CERTIF_ISSUE_SUBCLASS_DAT add constraint PK_CERTISSUE_SD primary key \
    (YEAR, CERTIF_INDEX)
