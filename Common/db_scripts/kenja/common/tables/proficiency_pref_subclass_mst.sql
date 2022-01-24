-- $Id: 07d1a93be946f144434ae2dd91bfad333de754b0 $
drop table PROFICIENCY_PREF_SUBCLASS_MST

create table PROFICIENCY_PREF_SUBCLASS_MST ( \
    PREF_SUBCLASSCD    varchar(6) not null, \
    SUBCLASS_NAME      varchar(60), \
    SUBCLASS_ABBV      varchar(60), \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PROFICIENCY_PREF_SUBCLASS_MST add constraint PK_PRO_PREF_SUB_M \
        primary key (PREF_SUBCLASSCD)
