-- $Id: 47cd31d9de38c339d0ef013cc77b8f73d24f71ea $

drop view V_IBSUBCLASS_MST

create view V_IBSUBCLASS_MST ( \
        IBYEAR, \
        IBCLASSCD, \
        IBPRG_COURSE, \
        IBCURRICULUM_CD, \
        IBSUBCLASSCD, \
        IBSUBCLASSNAME, \
        IBSUBCLASSABBV, \
        IBSUBCLASSNAME_ENG, \
        IBSUBCLASSABBV_ENG, \
        IBSUBCLASSORDERNAME1, \
        IBSUBCLASSORDERNAME2, \
        IBSUBCLASSORDERNAME3, \
        IBSHOWORDER, \
        IBSHOWORDER2, \
        IBSHOWORDER3, \
        IBSUBCLASSCD2, \
        IBSUBCLASSCD3, \
        IBELECTDIV, \
        IBUPDATED \
    ) as select \
            t1.IBYEAR, \
            t2.IBCLASSCD, \
            t2.IBPRG_COURSE, \
            t2.IBCURRICULUM_CD, \
            t2.IBSUBCLASSCD, \
            t2.IBSUBCLASSNAME, \
            t2.IBSUBCLASSABBV, \
            t2.IBSUBCLASSNAME_ENG, \
            t2.IBSUBCLASSABBV_ENG, \
            IBSUBCLASSORDERNAME1, \
            IBSUBCLASSORDERNAME2, \
            IBSUBCLASSORDERNAME3, \
            t2.IBSHOWORDER, \
            t2.IBSHOWORDER2, \
            t2.IBSHOWORDER3, \
            t2.IBSUBCLASSCD2, \
            t2.IBSUBCLASSCD3, \
            t2.IBELECTDIV, \
            t2.UPDATED \
         from   IBSUBCLASS_YDAT t1 \
                inner join IBSUBCLASS_MST t2 on t1.IBSUBCLASSCD = t2.IBSUBCLASSCD \
                                          and t1.IBCLASSCD = t2.IBCLASSCD \
                                          and t1.IBPRG_COURSE = t2.IBPRG_COURSE \
                                          and t1.IBCURRICULUM_CD = t2.IBCURRICULUM_CD 
