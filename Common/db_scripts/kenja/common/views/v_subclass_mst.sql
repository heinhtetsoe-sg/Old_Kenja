-- $Id: 02470624a80c92c28eada12725080a3ba5046698 $

drop view V_SUBCLASS_MST

create view V_SUBCLASS_MST ( \
        YEAR, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        SUBCLASSNAME, \
        SUBCLASSABBV, \
        SUBCLASSNAME_ENG, \
        SUBCLASSABBV_ENG, \
        SUBCLASSORDERNAME1, \
        SUBCLASSORDERNAME2, \
        SUBCLASSORDERNAME3, \
        SHOWORDER, \
        SHOWORDER2, \
        SHOWORDER3, \
        SUBCLASSCD2, \
        SUBCLASSCD3, \
        ELECTDIV, \
        UPDATED \
    ) as select \
            t1.YEAR, \
            t2.CLASSCD, \
            t2.SCHOOL_KIND, \
            t2.CURRICULUM_CD, \
            t2.SUBCLASSCD, \
            t2.SUBCLASSNAME, \
            t2.SUBCLASSABBV, \
            t2.SUBCLASSNAME_ENG, \
            t2.SUBCLASSABBV_ENG, \
            SUBCLASSORDERNAME1, \
            SUBCLASSORDERNAME2, \
            SUBCLASSORDERNAME3, \
            t2.SHOWORDER, \
            t2.SHOWORDER2, \
            t2.SHOWORDER3, \
            t2.SUBCLASSCD2, \
            t2.SUBCLASSCD3, \
            t2.ELECTDIV, \
            t2.UPDATED \
         from   SUBCLASS_YDAT t1 \
                inner join SUBCLASS_MST t2 on t1.SUBCLASSCD = t2.SUBCLASSCD \
                                          and t1.CLASSCD = t2.CLASSCD \
                                          and t1.SCHOOL_KIND = t2.SCHOOL_KIND \
                                          and t1.CURRICULUM_CD = t2.CURRICULUM_CD 
