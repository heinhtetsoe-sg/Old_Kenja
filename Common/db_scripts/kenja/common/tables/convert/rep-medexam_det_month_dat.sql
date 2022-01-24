-- $Id: a5d86c6edbc0c3771ae3bb842790c61ccefc46ee $

drop table MEDEXAM_DET_MONTH_DAT_OLD
rename table MEDEXAM_DET_MONTH_DAT to MEDEXAM_DET_MONTH_DAT_OLD
create table MEDEXAM_DET_MONTH_DAT( \
    YEAR                    varchar(4)    not null, \
    SEMESTER                varchar(1)    not null, \
    MONTH                   varchar(2)    not null, \
    SCHREGNO                varchar(8)    not null, \
    HEIGHT                  decimal(4,1) , \
    WEIGHT                  decimal(4,1) , \
    R_BAREVISION            varchar(5), \
    R_BAREVISION_MARK       varchar(3), \
    L_BAREVISION            varchar(5), \
    L_BAREVISION_MARK       varchar(3), \
    R_VISION                varchar(5), \
    R_VISION_MARK           varchar(3), \
    L_VISION                varchar(5), \
    L_VISION_MARK           varchar(3), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp   default current timestamp \
) in usr1dms index in idx1dms

insert into MEDEXAM_DET_MONTH_DAT \
    SELECT \
        YEAR, \
        SEMESTER, \
        MONTH, \
        SCHREGNO, \
        HEIGHT, \
        WEIGHT, \
        cast(null as varchar(5)) as R_BAREVISION, \
        cast(null as varchar(3)) as R_BAREVISION_MARK, \
        cast(null as varchar(5)) as L_BAREVISION, \
        cast(null as varchar(3)) as L_BAREVISION_MARK, \
        cast(null as varchar(5)) as R_VISION, \
        cast(null as varchar(3)) as R_VISION_MARK, \
        cast(null as varchar(5)) as L_VISION, \
        cast(null as varchar(3)) as L_VISION_MARK, \
        REGISTERCD, \
        UPDATED \
    FROM \
        MEDEXAM_DET_MONTH_DAT_OLD

alter table MEDEXAM_DET_MONTH_DAT add constraint PK_MED_D_MNT_D primary key (YEAR,SEMESTER,MONTH,SCHREGNO)
