-- kanji=´Á»ú
-- $Id: 8c9cb60801051b5528a7e8f383ce498b8e930e24 $

drop table SCHREG_ENT_GRD_HIST_COMEBACK_DAT_OLD
create table SCHREG_ENT_GRD_HIST_COMEBACK_DAT_OLD like SCHREG_ENT_GRD_HIST_COMEBACK_DAT
insert into SCHREG_ENT_GRD_HIST_COMEBACK_DAT_OLD select * from SCHREG_ENT_GRD_HIST_COMEBACK_DAT

drop table SCHREG_ENT_GRD_HIST_COMEBACK_DAT

create table SCHREG_ENT_GRD_HIST_COMEBACK_DAT \
 ( \
    SCHREGNO                   VARCHAR(8)    NOT NULL, \
    SCHOOL_KIND                VARCHAR(2)    NOT NULL, \
    COMEBACK_DATE              DATE          NOT NULL, \
    FINSCHOOLCD                VARCHAR(12)   , \
    FINISH_DATE                DATE         , \
    CURRICULUM_YEAR            VARCHAR(4)   , \
    ENT_DATE                   DATE         , \
    ENT_DIV                    VARCHAR(1)   , \
    ENT_REASON                 VARCHAR(75)  , \
    ENT_SCHOOL                 VARCHAR(75)  , \
    ENT_ADDR                   VARCHAR(150) , \
    ENT_ADDR2                  VARCHAR(150) , \
    GRD_DATE                   DATE         , \
    GRD_DIV                    VARCHAR(1)   , \
    GRD_REASON                 VARCHAR(75)  , \
    GRD_SCHOOL                 VARCHAR(75)  , \
    GRD_ADDR                   VARCHAR(150) , \
    GRD_ADDR2                  VARCHAR(150) , \
    GRD_NO                     VARCHAR(8)   , \
    GRD_TERM                   VARCHAR(4)   , \
    TENGAKU_SAKI_ZENJITU       DATE         , \
    TENGAKU_SAKI_GRADE         VARCHAR(60)  , \
    NYUGAKUMAE_SYUSSIN_JOUHOU  VARCHAR(768) , \
    EXAMNO                     VARCHAR(20)  , \
    REGISTERCD                 VARCHAR(10)   , \
    UPDATED                    TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table SCHREG_ENT_GRD_HIST_COMEBACK_DAT add constraint PK_ENT_GRD_H_COME primary key (SCHREGNO,SCHOOL_KIND,COMEBACK_DATE)

insert into SCHREG_ENT_GRD_HIST_COMEBACK_DAT \
select \
    * \
from SCHREG_ENT_GRD_HIST_COMEBACK_DAT_OLD

