-- kanji=$B4A;z(B
-- $Id: e600290dff7f625cebd307f0d6f146e5d1b66e0d $

-- $BCm0U(B:$B$3$N%U%!%$%k$O(B EUC/LF$B$N$_(B $B$G$J$1$l$P$J$i$J$$!#(B
-- $BE,MQJ}K!(B:
--    1.$B%G!<%?%Y!<%9@\B3(B
--    2.db2 +c -f <$B$3$N%U%!%$%k(B>
--    3.$B%3%_%C%H$9$k$J$i!"(Bdb2 +c commit$B!#$d$jD>$9$J$i!"(Bdb2 +c rollback

drop table SCHREG_ENT_GRD_HIST_DAT_OLD
create table SCHREG_ENT_GRD_HIST_DAT_OLD like SCHREG_ENT_GRD_HIST_DAT
insert into SCHREG_ENT_GRD_HIST_DAT_OLD select * from SCHREG_ENT_GRD_HIST_DAT

drop table SCHREG_ENT_GRD_HIST_DAT

create table SCHREG_ENT_GRD_HIST_DAT \
(  \
    SCHREGNO                    varchar(8)    not null, \
    SCHOOL_KIND                 varchar(2)    not null, \
    FINSCHOOLCD                 varchar(12), \
    FINISH_DATE                 date, \
    CURRICULUM_YEAR             varchar(4), \
    ENT_DATE                    date, \
    ENT_DIV                     varchar(1), \
    ENT_REASON                  varchar(75), \
    ENT_SCHOOL                  varchar(75), \
    ENT_ADDR                    varchar(150), \
    ENT_ADDR2                   varchar(150), \
    GRD_DATE                    date, \
    GRD_DIV                     varchar(1), \
    GRD_REASON                  varchar(75), \
    GRD_SCHOOL                  varchar(75), \
    GRD_ADDR                    varchar(150), \
    GRD_ADDR2                   varchar(150), \
    GRD_NO                      varchar(8), \
    GRD_TERM                    varchar(4), \
    TENGAKU_SAKI_ZENJITU        date, \
    TENGAKU_SAKI_GRADE          VARCHAR(60)  , \
    NYUGAKUMAE_SYUSSIN_JOUHOU   varchar(768), \
    REGISTERCD                  varchar(10), \
    UPDATED                     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_ENT_GRD_HIST_DAT add constraint PK_ENT_GRD_HIST primary key \
    (SCHREGNO, SCHOOL_KIND)

insert into SCHREG_ENT_GRD_HIST_DAT \
select \
    * \
from SCHREG_ENT_GRD_HIST_DAT_OLD
