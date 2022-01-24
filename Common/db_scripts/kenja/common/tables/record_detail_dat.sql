-- $Id: e02a237ed2de91b2e60314d75fb0f40d37853696 $

drop table RECORD_DETAIL_DAT
create table RECORD_DETAIL_DAT  \
(  \
        YEAR                  VARCHAR(4)      NOT NULL, \
        CLASSCD               VARCHAR(2)      NOT NULL, \
        SCHOOL_KIND           VARCHAR(2)      NOT NULL, \
        CURRICULUM_CD         VARCHAR(2)      NOT NULL, \
        SUBCLASSCD            VARCHAR(6)      NOT NULL, \
        TAKESEMES             VARCHAR(1)      NOT NULL, \
        SCHREGNO              VARCHAR(8)      NOT NULL, \
        SEQ                   VARCHAR(3)      NOT NULL, \
        REMARK1               VARCHAR(90),  \
        REMARK2               VARCHAR(90),  \
        REMARK3               VARCHAR(90),  \
        REMARK4               VARCHAR(90),  \
        REMARK5               VARCHAR(90),  \
        REGISTERCD            VARCHAR(10),  \
        UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms


alter table RECORD_DETAIL_DAT  \
add constraint PK_RECORD_DETAIL  \
primary key  \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, TAKESEMES, SCHREGNO, SEQ)
