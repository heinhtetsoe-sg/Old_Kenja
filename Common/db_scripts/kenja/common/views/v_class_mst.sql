-- $Id: 149e39ce983a95ad9f199e661eaa4beedb0049b4 $

drop view V_CLASS_MST

create view V_CLASS_MST ( \
        YEAR, \
        CLASSCD, \
        SCHOOL_KIND, \
        CLASSNAME, \
        CLASSABBV, \
        CLASSNAME_ENG, \
        CLASSABBV_ENG, \
        CLASSORDERNAME1, \
        CLASSORDERNAME2, \
        CLASSORDERNAME3, \
        SUBCLASSES, \
        SHOWORDER, \
        SHOWORDER2, \
        SHOWORDER3, \
        SHOWORDER4, \
        ELECTDIV, \
        SPECIALDIV, \
        UPDATED \
    ) as select \
            T1.YEAR, \
            T2.CLASSCD, \
            T2.SCHOOL_KIND, \
            T2.CLASSNAME, \
            T2.CLASSABBV, \
            T2.CLASSNAME_ENG, \
            T2.CLASSABBV_ENG, \
            T2.CLASSORDERNAME1, \
            T2.CLASSORDERNAME2, \
            T2.CLASSORDERNAME3, \
            T2.SUBCLASSES, \
            T2.SHOWORDER, \
            T2.SHOWORDER2, \
            T2.SHOWORDER3, \
            T2.SHOWORDER4, \
            T2.ELECTDIV, \
            T2.SPECIALDIV, \
            T2.UPDATED \
         from \
            CLASS_YDAT T1, \
            CLASS_MST T2 \
         where \
            T1.CLASSCD = T2.CLASSCD AND \
            T1.SCHOOL_KIND = T2.SCHOOL_KIND
