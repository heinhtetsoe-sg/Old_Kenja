-- $Id: v_class_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop view V_CLASS_MST

create view V_CLASS_MST ( \
        YEAR, CLASSCD, CLASSNAME, CLASSABBV, CLASSNAME_ENG, CLASSABBV_ENG, \
        CLASSORDERNAME1, CLASSORDERNAME2, CLASSORDERNAME3, \
        SHOWORDER, SHOWORDER2, SHOWORDER3, INOUT_DIV, UPDATED \
    ) as select \
            T1.YEAR, T2.CLASSCD, T2.CLASSNAME, T2.CLASSABBV, T2.CLASSNAME_ENG, T2.CLASSABBV_ENG, \
            T2.CLASSORDERNAME1, T2.CLASSORDERNAME2, T2.CLASSORDERNAME3, \
            T2.SHOWORDER, T2.SHOWORDER2, T2.SHOWORDER3, T2.INOUT_DIV, T2.UPDATED \
         from \
            CLASS_YDAT T1, CLASS_MST T2 \
         where \
            T1.CLASSCD = T2.CLASSCD