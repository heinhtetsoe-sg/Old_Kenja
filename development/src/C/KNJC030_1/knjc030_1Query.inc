<?php

require_once('for_php7.php');

class knjc030_1Query extends Query
{
    //Chair情報取得
    function getTargetChair(&$model)
    {
/*
        if($model->adminMode == "on"){
            $where = "AND T4.TRGTGRADE || T4.TRGTCLASS = '".$model->trgtClass."' ";
        }else if($model->adminMode == "off"){
            $where = "AND  T5.STAFFCD = '".$model->t_Staffcd."' ";
        }

        $query = "  SELECT
                        T1.CHAIRCD,
                        T3.CHAIRNAME
                    FROM
                        SCH_CHR_DAT T1 LEFT JOIN CHAIR_STF_DAT T2 ON  T1.YEAR     = T2.YEAR 
                                                                  AND T1.CHAIRCD  = T2.CHAIRCD 
                                       LEFT JOIN CHAIR_DAT T3     ON  T2.YEAR     = T3.YEAR 
                                                                  AND T2.CHAIRCD  = T3.CHAIRCD 
                                                                  AND T2.SEMESTER = T3.SEMESTER 
                                       LEFT JOIN CHAIR_CLS_DAT T4 ON  T2.YEAR     = T4.YEAR 
                                                                  AND T2.CHAIRCD  = T4.CHAIRCD 
                                                                  AND T2.SEMESTER = T4.SEMESTER
                                       LEFT JOIN V_STAFF_MST T5   ON  T2.YEAR     = T5.YEAR 
                                                                  AND T2.STAFFCD  = T5.STAFFCD 
                                       LEFT JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR   = T4.YEAR 
                                                                  AND T6.GRADE    = T4.TRGTGRADE 
                                                                  AND T6.HR_CLASS = T4.TRGTCLASS 
                    WHERE
                        T1.YEAR = '".CTRL_YEAR."'
                        AND  T1.EXECUTEDATE = DATE('".$model->executedate."')
                        AND  T6.SEMESTER  = '".$model->semester."'
                        ".$where."
                        AND  T1.PERIODCD  = '1'
                    GROUP BY
                        T1.CHAIRCD,
                        T3.CHAIRNAME
                    ORDER BY
                        T1.CHAIRCD ";
*/

#        if($model->adminMode == "on"){
#            $where = "AND T3.TRGTGRADE || T3.TRGTCLASS = '".$model->trgtClass."' ";
#        }else if($model->adminMode == "off"){
#            $where = "AND  T4.STAFFCD = '".$model->t_Staffcd."' ";
#        }
#  KNJC030から呼び出された時に複数あるSHRの講座をすべて取得するように変更
#        $query = "SELECT
#                    T1.chaircd, T3.chairname
#                  FROM (SELECT periodcd, chaircd, executed AS attendcd
#                          FROM sch_chr_dat
#                         WHERE executedate = DATE('".$model->executedate."')) T1
#                    LEFT JOIN (SELECT chaircd, staffcd
#                                 FROM chair_stf_dat
#                                WHERE year = '".CTRL_YEAR."'
#                                  AND semester = '".$model->semester."'
#                                  AND staffcd = '".$model->t_Staffcd."') T2
#                           ON T1.chaircd = T2.chaircd
#                    LEFT JOIN (SELECT k1.year, k1.semester, k2.chaircd, k2.chairname, k2.groupcd, k1.trgtgrade, k1.trgtclass
#                                 FROM chair_cls_dat k1, chair_dat k2
#                                WHERE k1.year       = k2.year
#                                  AND k1.semester   = k2.semester
#                                  AND k1.chaircd    = '0000000'
#                                  AND k1.year       = '".CTRL_YEAR."'
#                                  AND k1.semester   = '".$model->semester."'
#                                  AND k1.groupcd    = k2.groupcd
#                              UNION
#                               SELECT t1.year, t1.semester, t2.chaircd, t2.chairname, t2.groupcd, t1.trgtgrade, t1.trgtclass
#                                 FROM chair_cls_dat t1, chair_dat t2
#                                WHERE t1.year       = t2.year
#                                  AND t1.semester   = t2.semester
#                                  AND t1.groupcd    = t2.groupcd
#                                  AND t1.year       = '".CTRL_YEAR."'
#                                  AND t1.semester   = '".$model->semester."'
#                                  AND t1.chaircd    = t2.chaircd ) T3
#                           ON T2.chaircd = T3.chaircd
#                    LEFT JOIN (SELECT staffcd
#                                 FROM v_staff_mst
#                                WHERE year = '".CTRL_YEAR."') T4
#                           ON T2.staffcd = T4.staffcd
#                    LEFT JOIN schreg_regd_hdat T5
#                           ON T5.year      = T3.year
#                          AND T5.semester  = T3.semester
#                          AND T3.trgtgrade = T5.grade
#                          AND T3.trgtclass = T5.hr_class
#                  WHERE T1.PERIODCD  = '1'
#                    AND T3.TRGTGRADE || T3.TRGTCLASS = '".$model->trgtClass."'
#                  ORDER BY T1.chaircd, T3.chairname ";

        $query = "SELECT
                    T1.chaircd, T3.chairname, T1.periodcd
                  FROM (SELECT periodcd, chaircd, executed AS attendcd
                          FROM sch_chr_dat
                         WHERE executedate = DATE('".$model->executedate."')) T1
                    LEFT JOIN (SELECT k1.year, k1.semester, k2.chaircd, k2.chairname, k2.groupcd, k1.trgtgrade, k1.trgtclass
                                 FROM chair_cls_dat k1, chair_dat k2
                                WHERE k1.year       = k2.year
                                  AND k1.semester   = k2.semester
                                  AND k1.chaircd    = '0000000'
                                  AND k1.year       = '".CTRL_YEAR."'
                                  AND k1.semester   = '".$model->semester."'
                                  AND k1.groupcd    = k2.groupcd
                              UNION
                               SELECT t1.year, t1.semester, t2.chaircd, t2.chairname, t2.groupcd, t1.trgtgrade, t1.trgtclass
                                 FROM chair_cls_dat t1, chair_dat t2
                                WHERE t1.year       = t2.year
                                  AND t1.semester   = t2.semester
                                  AND t1.groupcd    = t2.groupcd
                                  AND t1.year       = '".CTRL_YEAR."'
                                  AND t1.semester   = '".$model->semester."'
                                  AND t1.chaircd    = t2.chaircd ) T3
                           ON T1.chaircd = T3.chaircd
                    LEFT JOIN schreg_regd_hdat T5
                           ON T5.year      = T3.year
                          AND T5.semester  = T3.semester
                          AND T3.trgtgrade = T5.grade
                          AND T3.trgtclass = T5.hr_class
                    LEFT JOIN V_NAME_MST T6
                           ON T6.YEAR= T3.YEAR AND T6.NAMECD1='B001' AND T6.NAMECD2=T1.PERIODCD
                  WHERE T6.NAMESPARE1 IS NOT NULL
                    AND T3.TRGTGRADE || T3.TRGTCLASS = '".$model->trgtClass."'
                  ORDER BY T1.chaircd, T3.chairname ";
                  
        #echo $query;
        return $query;
    }

    //時間割情報取得 2006.02.04 alp m-yama
    function getTimeTable($model)
    {

        $query =" SELECT
                    T1.PERIODCD,T1.CHAIRCD,T1.EXECUTED,T1.ATTESTOR,
                    T3.CHAIRNAME,T4.STAFFCD,T4.STAFFNAME_SHOW,T5.HR_NAMEABBV AS TARGETCLASS, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          T6.CLASSCD, T6.SCHOOL_KIND, T6.CURRICULUM_CD, ";
        }
        $query .=" T6.SUBCLASSCD,T6.SUBCLASSNAME,T6.SUBCLASSABBV,T7.FACCD,T8.FACILITYNAME,
                    T9.STAFFNAME_SHOW AS ATTESTOR_NAME, VALUE(N1.ABBV1, '　') AS LESSON_NAME
                FROM
                    SCH_CHR_DAT T1 LEFT JOIN chair_stf_dat T2 ON T1.year = T2.year
                                                             AND T1.semester = T2.semester
                                                             AND T1.chaircd  = T2.chaircd
                                   LEFT JOIN 
                                            (SELECT k1.year, k1.semester, k2.chaircd, k2.chairname, ";
                   if ($model->Properties["useCurriculumcd"] == "1") {
                       $query .= "          k2.CLASSCD, k2.SCHOOL_KIND, k2.CURRICULUM_CD, ";
                   }
                   $query .= "              k2.subclasscd, k2.groupcd, k1.trgtgrade, k1.trgtclass
                                               FROM chair_cls_dat k1, chair_dat k2
                                              WHERE k1.year       = k2.year
                                                AND k1.semester   = k2.semester
                                                AND k1.chaircd    = '0000000'
                                                AND k1.year       = '".CTRL_YEAR."'
                                                AND k1.semester   = '".$model->semester."'
                                                AND k1.groupcd    = k2.groupcd
                                            UNION
                                             SELECT t1.year, t1.semester, t2.chaircd, t2.chairname, ";
                   if ($model->Properties["useCurriculumcd"] == "1") {
                       $query .= "           t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ";
                   }
                   $query .= "              t2.subclasscd, t2.groupcd, t1.trgtgrade, t1.trgtclass
                                               FROM chair_cls_dat t1, chair_dat t2
                                              WHERE t1.year       = t2.year
                                                AND t1.semester   = t2.semester
                                                AND t1.groupcd    = t2.groupcd
                                                AND t1.year       = '".CTRL_YEAR."'
                                                AND t1.semester   = '".$model->semester."'
                                                AND t1.chaircd    = t2.chaircd ) T3
                                                              ON T1.year     = T3.year
                                                             AND T1.semester = T3.semester
                                                             AND T1.chaircd  = T3.chaircd
                                   LEFT JOIN v_staff_mst T4   ON T1.year     = T4.year
                                                             AND T2.staffcd  = T4.staffcd
                                   LEFT JOIN schreg_regd_hdat T5 ON T1.year      = T5.year
                                                                AND T1.semester  = T5.semester
                                                                AND T3.trgtgrade = T5.grade
                                                                AND T3.trgtclass = T5.hr_class
                                   LEFT JOIN subclass_mst T6  ON T3.subclasscd   = T6.subclasscd ";
                                     if ($model->Properties["useCurriculumcd"] == "1") {
                                         $query .= "          AND T3.CLASSCD = T6.CLASSCD ";
                                         $query .= "          AND T3.SCHOOL_KIND = T6.SCHOOL_KIND ";
                                         $query .= "          AND T3.CURRICULUM_CD = T6.CURRICULUM_CD ";
                                     }
        $query .="                 LEFT JOIN sch_fac_dat T7   ON T1.year        = T7.year
                                                             AND T1.executedate = T7.executedate
                                                             AND T1.chaircd     = T7.chaircd
                                   LEFT JOIN facility_mst T8  ON T7.faccd    = T8.faccd
                                   LEFT JOIN v_staff_mst T9   ON T1.year     = T9.year
                                                             AND T1.attestor = T9.staffcd
                                   LEFT JOIN V_NAME_MST T10
                                                              ON T10.YEAR= T1.YEAR
                                                             AND T10.NAMECD1='B001'
                                                             AND T10.NAMECD2=T1.PERIODCD
                                   LEFT JOIN SCH_CHR_COUNTFLG T11 ON T1.EXECUTEDATE = T11.EXECUTEDATE
                                        AND T1.PERIODCD = T11.PERIODCD
                                        AND T1.CHAIRCD = T11.CHAIRCD
                                        AND T3.TRGTGRADE = T11.GRADE
                                        AND T3.TRGTCLASS = T11.HR_CLASS
                                   LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B002'
                                        AND T11.LESSON_MODE = N1.NAMECD2
                WHERE
                        T1.year = '".CTRL_YEAR."'
                    AND T1.executedate = DATE('".$model->executedate."')
                    AND T1.chaircd   = '".$model->chaircd."'
                    AND T1.PERIODCD  = '".$model->pericd."'
                    AND T10.NAMESPARE1 IS NOT NULL
                ORDER BY T1.periodcd,TARGETCLASS ";
        #echo $query;
        return $query;
    }

    //学期取得
    function getTerm($day){
        return " VALUES TERM_GET('".CTRL_YEAR."',DATE('".$day."'),'N') ";
    }

    //担任コード１取得
    function getTr_cd1($model)
    {
        $query = "SELECT tr_cd1 FROM schreg_regd_hdat
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".$model->semester."'
                     AND grade || hr_class = '".$model->trgtClass."'";
        return $query;
    }
    
    //校時コード取得
    function getPericdcd($date,$cd){
        $query = "SELECT MIN(PERIODCD)
                    FROM SCH_CHR_DAT
                   WHERE EXECUTEDATE=date('".$date."')
                     AND CHAIRCD='".$cd."'";
        return $query;
    }


#    function getPath(){
#        return " SELECT PROGRAMPATH FROM MENU_MST WHERE PROGRAMID = 'KNJC010' ";
#    }
}
?>
