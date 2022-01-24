<?php

require_once('for_php7.php');

class knjh320Query extends Query {

    //欠課数換算用データ取得
    function getSchoolData($model)
    {
        $query  = "SELECT ";
        $query .= "    ABSENT_COV, ";
        $query .= "    VALUE(ABSENT_COV_LATE,0) AS ABSENT_COV_LATE ";
        $query .= "FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";

        return $query;
    }

    //去年度取得
    function getLastYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR < '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MAX(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //次年度取得
    function getNextYear($model)
    {
        $query  = "WITH YEART AS ( ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEART ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR > '".$model->year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    MIN(YEAR) AS YEAR, ";
        $query .= "    MAX(SEMESTER) AS SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = (SELECT YEART FROM YEART) ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //生徒情報取得
    function getName($model)
    {
        $query  = "SELECT ";
        $query .= "    L1.GRADE, ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    L1.ATTENDNO, ";
        $query .= "    T1.NAME, ";
        $query .= "    L1.HR_CLASS, ";
        $query .= "    L1.COURSECODE ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.SEMESTER = '".$model->semester."' ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = L1.YEAR ";
        $query .= "         AND L2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND L2.GRADE = L1.GRADE ";
        $query .= "         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //科目取得
    function getTestSubclass($model)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "     t1.proficiency_subclass_cd as VALUE, ";
        $query .= "     t2.SUBCLASS_ABBV as LABEL ";
        $query .= " FROM ";
        $query .= "     proficiency_dat t1 ";
        $query .= "     left join proficiency_subclass_mst t2 on t1.proficiency_subclass_cd = t2.proficiency_subclass_cd ";
        $query .= " WHERE ";
        $query .= "     schregno = '".$model->schregno."' AND ";
        $query .= "     year = '".$model->year."' ";
        
        
        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    SEMESTERNAME, ";
        $query .= "    SDATE, ";
        $query .= "    EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getTestKind($model, $semester)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "     t1.semester, ";
        $query .= "     t1.proficiencycd, ";
        $query .= "     t2.proficiencyname1 as TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     proficiency_dat t1 ";
        $query .= "     left join proficiency_mst t2 on t1.proficiencycd = t2.proficiencycd and t2.proficiencydiv = '02' ";
        $query .= " WHERE ";
        $query .= "     schregno = '".$model->schregno."' AND ";
        $query .= "     year = '".$model->year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //得点データ取得
    function getScore($semester, $proficiencycd, $subclasscd, $model)
    {
        $query  = " SELECT ";
        $query .= "    t1.YEAR, ";
        $query .= "    t1.SEMESTER, ";
        $query .= "    t1.PROFICIENCY_SUBCLASS_CD as SUBCLASSCD, ";
        $query .= "    SCHREGNO, ";
        $query .= "    t1.SCORE ";
        if($model->avg_div != 9){
            $query .= "    , T3.AVG ";
        }
        $query .= " FROM ";
        $query .= "    PROFICIENCY_SUBCLASS_MST t2  ";
        $query .= "    left join PROFICIENCY_DAT t1 on t1.proficiency_subclass_cd = t2.proficiency_subclass_cd ";
        if($model->avg_div != 9){
            $query .= "    left join PROFICIENCY_AVERAGE_DAT t3 on t1.year = t3.year and t1.semester = t3.semester and t1.proficiencydiv = t3.proficiencydiv and t1.proficiencycd = t3.proficiencycd and t1.proficiency_subclass_cd = t3.proficiency_subclass_cd and t3.avg_div = '".$model->avg_div."' ";
            if($model->avg_div != 1 ){
                $query .= "  and t3.grade = '".$model->grade."' and ";
                if($model->avg_div == 2){
                    $query .= "  t3.hr_class = '".$model->limit."' ";
                }else{
                    $query .= "  t3.coursecode = '".$model->limit."' ";
                }
            }
        }
        $query .= " WHERE ";
        $query .= "    t1.YEAR='".$model->year."' AND ";
        $query .= "    t1.SEMESTER='".$semester."' ";
        $query .= "    AND t1.proficiencydiv = '02' ";
        $query .= "    AND t1.proficiencycd = '".$proficiencycd."' ";
        $query .= "    AND t1.proficiency_subclass_cd IN (".($subclasscd == '' ? "''" : $subclasscd).") ";
        $query .= "    AND t1.SCHREGNO = '".$model->schregno."' ";
        $query .= " order by ";
        $query .= "    t1.semester,t1.proficiency_subclass_cd ";

        return $query;
    }

    //順位取得
    function getRank($semester, $proficiencycd, $subclasscd, $model, $grade, $div)
    {
        $field = $div;
        $rank  = "";
        if($model->avg_div == 9 || $model->avg_div == 1){
            $rank = "01";
        } else if($model->avg_div == 2){
            $rank = "02";
        } else {
            $rank = "03";
        }

        $query  = " SELECT ";
        $query .= "t1.proficiency_subclass_cd as SUBCLASSCD, ";
        $query .= $field." as SCORE ";
        $query .= "FROM ";
        $query .= "    PROFICIENCY_SUBCLASS_MST t2  ";
        $query .= "    left join PROFICIENCY_RANK_DAT t1 on t1.proficiency_subclass_cd = t2.proficiency_subclass_cd and t1.rank_data_div = '01' and t1.rank_div = '".$rank."' ";
        $query .= "WHERE ";
        $query .= "    t1.YEAR = '".$model->year."' ";
        $query .= "    AND t1.SEMESTER = '".$semester."' ";
        $query .= "    AND t1.proficiencydiv = '02' ";
        $query .= "    AND t1.proficiencycd = '".$proficiencycd."' ";
        $query .= "    AND t1.proficiency_subclass_cd IN (".($subclasscd == '' ? "''" : $subclasscd).") ";
        $query .= "    AND t1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学校マスタの取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //欠課数取得
    function getAbsent($semester, $subclasscd, $model, $knjSchoolMst)
    {
        $query  = "SELECT ";
        $query .= "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD as SUBCLASSCD, ";
        if ($model->absent_cov == 0 || $model->absent_cov_late == 0) {
            $query .= "    (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE) + SUM(NURSEOFF) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + SUM(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + SUM(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + SUM(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + SUM(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + SUM(ABSENT) ";
            }
            $query .= "    ) AS SCORE ";
        } else if ($model->absent_cov < 3) {
            $query .= "    (((SUM(LATE) + SUM(EARLY)) / ".$model->absent_cov_late.") + (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE) + SUM(NURSEOFF) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + SUM(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + SUM(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + SUM(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + SUM(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + SUM(ABSENT) ";
            }
            $query .= "    )) AS SCORE ";
        } else {
            $query .= "    DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / ".$model->absent_cov_late.") + (SUM(SICK) + SUM(NOTICE) + SUM(NONOTICE) + SUM(NURSEOFF) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + SUM(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + SUM(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + SUM(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + SUM(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + SUM(ABSENT) ";
            }
            $query .= "    ),4,1) AS SCORE ";
        }
        $query .= "FROM ";

        $query .= "    (SELECT ";
        $query .= "          COPYCD, ";
        $query .= "          YEAR, ";
        $query .= "          MONTH, ";
        $query .= "          SEMESTER, ";
        $query .= "          SCHREGNO, ";
        $query .= "          CLASSCD, ";
        $query .= "          SCHOOL_KIND, ";
        $query .= "          CURRICULUM_CD, ";
        $query .= "          SUBCLASSCD, ";
        $query .= "          APPOINTED_DAY, ";
        $query .= "          LESSON, ";
        $query .= "          case when OFFDAYS IS NULL then '0' else OFFDAYS end as OFFDAYS, ";
        $query .= "          case when ABSENT IS NULL then '0' else ABSENT end as ABSENT, ";
        $query .= "          case when SUSPEND IS NULL then '0' else SUSPEND end as SUSPEND, ";
        $query .= "          case when MOURNING IS NULL then '0' else MOURNING end as MOURNING, ";
        $query .= "          case when ABROAD IS NULL then '0' else ABROAD end as ABROAD, ";
        $query .= "          case when SICK IS NULL then '0' else SICK end as SICK, ";
        $query .= "          case when NOTICE IS NULL then '0' else NOTICE end as NOTICE, ";
        $query .= "          case when NONOTICE IS NULL then '0' else NONOTICE end as NONOTICE, ";
        $query .= "          case when NURSEOFF IS NULL then '0' else NURSEOFF end as NURSEOFF, ";
        $query .= "          case when LATE IS NULL then '0' else LATE end as LATE, ";
        $query .= "          case when EARLY IS NULL then '0' else EARLY end as EARLY, ";
        $query .= "          case when VIRUS IS NULL then '0' else VIRUS end as VIRUS, ";
        $query .= "          case when KOUDOME IS NULL then '0' else KOUDOME end as KOUDOME ";
        $query .= "     FROM ";
        $query .= "          ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "          COPYCD = '0' ";
        $query .= "     AND YEAR = '".$model->year."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN (".$subclasscd.") ";
        } else {
            $query .= "    AND SUBCLASSCD IN (".$subclasscd.") )";
        }

        $query .= "    ) ";
        $query .= "WHERE ";
        $query .= "    COPYCD = '0' ";
        $query .= "    AND YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$semester."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN (".$subclasscd.") ";
        } else {
            $query .= "    AND SUBCLASSCD IN (".$subclasscd.") ";
        }
        $query .= "GROUP BY ";
        $query .= "    CLASSCD, SCHOOL_KIND , CURRICULUM_CD, SUBCLASSCD ";

        return $query;
    }

}
?>
