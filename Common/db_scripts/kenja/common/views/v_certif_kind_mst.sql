-- $Id: ec9592aa1440ed6a798e76d3e663479c03881046 $

drop view v_certif_kind_mst

create view v_certif_kind_mst \
    (year, \
     certif_kindcd, \
     kindname, \
     issuecd, \
     studentcd, \
     graduatecd, \
     dropoutcd, \
     ELAPSED_YEARS, \
     CERTIF_DIV, \
     CERTIF_GRPCD, \
     CERTIF_SCHOOL_KIND, \
     certif_no, \
     syosyo_name, \
     school_name, \
     job_name, \
     principal_name, \
     remark1, \
     remark2, \
     remark3, \
     remark4, \
     remark5, \
     remark6, \
     updated) \
as select \
    t1.year, \
    t2.certif_kindcd, \
    t2.kindname, \
    t2.issuecd, \
    t2.studentcd, \
    t2.graduatecd, \
    t2.dropoutcd, \
    T2.ELAPSED_YEARS, \
    T2.CERTIF_DIV, \
    T2.CERTIF_GRPCD, \
    T2.CERTIF_SCHOOL_KIND, \
    t3.certif_no, \
    t3.syosyo_name, \
    t3.school_name, \
    t3.job_name, \
    t3.principal_name, \
    t3.remark1, \
    t3.remark2, \
    t3.remark3, \
    t3.remark4, \
    t3.remark5, \
    t3.remark6, \
    t3.updated \
from    CERTIF_KIND_YDAT t1 \
      left join CERTIF_SCHOOL_DAT t3 on t1.year = t3.year \
      and t1.certif_kindcd = t3.certif_kindcd, \
    certif_kind_mst t2 \
where   t1.certif_kindcd = t2.certif_kindcd
