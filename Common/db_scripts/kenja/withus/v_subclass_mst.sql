-- $Id: v_subclass_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop view V_SUBCLASS_MST

create view V_SUBCLASS_MST ( \
        YEAR, CLASSCD, CURRICULUM_CD, SUBCLASSCD, SUBCLASSNAME, SUBCLASSABBV, SUBCLASSNAME_ENG, \
        SUBCLASSABBV_ENG, SUBCLASSORDERNAME1, SUBCLASSORDERNAME2, SUBCLASSORDERNAME3, \
        SHOWORDER, SHOWORDER2, SHOWORDER3, SUBCLASSCD2, SUBCLASSCD3, INOUT_DIV, UPDATED \
    ) as select \
            t1.YEAR, t2.CLASSCD, t2.CURRICULUM_CD, t2.SUBCLASSCD, t2.SUBCLASSNAME, t2.SUBCLASSABBV, \
            t2.SUBCLASSNAME_ENG, t2.SUBCLASSABBV_ENG, SUBCLASSORDERNAME1, SUBCLASSORDERNAME2, SUBCLASSORDERNAME3, \
            t2.SHOWORDER, t2.SHOWORDER2, t2.SHOWORDER3, t2.SUBCLASSCD2, t2.SUBCLASSCD3, t2.INOUT_DIV, t2.UPDATED \
         from	SUBCLASS_YDAT t1 \
                inner join SUBCLASS_MST t2 on t1.CLASSCD = t2.CLASSCD AND t1.SUBCLASSCD = t2.SUBCLASSCD AND t1.CURRICULUM_CD = t2.CURRICULUM_CD
