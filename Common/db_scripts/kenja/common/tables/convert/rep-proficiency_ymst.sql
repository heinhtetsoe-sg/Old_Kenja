-- $Id: b9ac26e0a53a6e6e2f9b3474f5abde7a9531377f $

drop table PROFICIENCY_YMST_OLD

rename table PROFICIENCY_YMST to PROFICIENCY_YMST_OLD

create table PROFICIENCY_YMST \
    (YEAR               varchar(4) not null, \
     SEMESTER           varchar(1) not null, \
     PROFICIENCYDIV     varchar(2) not null, \
     PROFICIENCYCD      varchar(4) not null, \
     GRADE              varchar(2) not null, \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

INSERT INTO PROFICIENCY_YMST \
    SELECT \
        YEAR, \
        SEMESTER, \
        PROFICIENCYDIV, \
        PROFICIENCYCD, \
        GRADE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        PROFICIENCY_YMST_OLD

alter table PROFICIENCY_YMST add constraint PK_PRO_YMST primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, GRADE)
