-- $Id: 4f2704994cedd5d929300c807c9af9fbbbf1fbc8 $
drop table PROFICIENCY_SUBCLASS_MST

create table PROFICIENCY_SUBCLASS_MST ( \
    PROFICIENCY_SUBCLASS_CD varchar(6) not null, \
    SUBCLASS_NAME           varchar(60), \
    SUBCLASS_ABBV           varchar(15), \
    CLASSCD                 varchar(2), \
    SCHOOL_KIND             varchar(2), \
    CURRICULUM_CD           varchar(2), \
    SUBCLASSCD              varchar(6), \
    PREF_SUBCLASSCD         varchar(6), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PROFICIENCY_SUBCLASS_MST add constraint PK_PRO_SUBCLASS_M \
        primary key (PROFICIENCY_SUBCLASS_CD)
