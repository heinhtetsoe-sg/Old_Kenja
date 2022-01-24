-- $Id: 0425beda16e2151e65560ea9d87b08927e5cff3c $

drop view V_CHAIR_STD_CLS_DAT

create view V_CHAIR_STD_CLS_DAT ( \
    YEAR, \
    SEMESTER, \
    CHAIRCD, \
    TRGTGRADE, \
    TRGTCLASS, \
    APPDATE, \
    APPENDDATE \
) as select distinct  \
    t1.YEAR, \
    t1.SEMESTER, \
    t1.CHAIRCD, \
    t2.GRADE as TRGTGRADE, \
    t2.HR_CLASS as TRGTCLASS, \
    t1.APPDATE, \
    t1.APPENDDATE \
from CHAIR_STD_DAT t1 \
inner join SCHREG_REGD_DAT t2 on t2.SCHREGNO = t1.SCHREGNO \
    and t2.YEAR = t1.YEAR \
    and t2.SEMESTER = t1.SEMESTER 

