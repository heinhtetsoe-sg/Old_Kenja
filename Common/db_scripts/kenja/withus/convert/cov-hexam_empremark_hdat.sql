-- $Id: cov-hexam_empremark_hdat.sql 56577 2017-10-22 11:35:50Z maeshiro $

insert into HEXAM_EMPREMARK_HDAT \
select \
    SCHREGNO, \
    JOBHUNT_REC, \
    JOBHUNT_RECOMMEND, \
    JOBHUNT_ABSENCE, \
    JOBHUNT_HEALTHREMARK, \
    REGISTERCD, \
    UPDATED \
from \
    HEXAM_EMPREMARK_DAT
