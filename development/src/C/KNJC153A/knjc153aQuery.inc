<?php

require_once('for_php7.php');


class knjc153aQuery extends Query {

    function getSubclassSaki($model) {
        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= " COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //科目コンボ
    function selectSubclassQuery($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     subclass_mst T3, ";
        $query .= "     chair_stf_dat T4 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T1.year       = T4.year AND ";
        $query .= "     T1.semester   = T4.semester AND ";
        $query .= "     T1.chaircd    = T4.chaircd AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "    (T3.subclasscd NOT LIKE '9%' OR T3.subclasscd LIKE '90%' ) AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD  AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND  AND ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " T3.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD  AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }


    //備考考査
    function selectTestcdQuery($model, $subclasscd, $schoolKind = "", $remarkFlg = "") {
        $subclassArray = array();
        $subclassArray = explode("-", $subclasscd);

        $query  = " WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (";
        $query .= "   SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "       T1.SUBCLASSCD AS SUBCLASSCD ";
        }
        $query .= "   FROM ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "   WHERE T1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "   UNION ALL ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "   FROM ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "   WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-000000' ";
        if ($schoolKind != "") {
            $query .= "     AND T1.SCHOOL_KIND = '".$schoolKind."' ";
        } else {
            $query .= "     AND T1.SCHOOL_KIND = '".$subclassArray[1]."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     VALUE(T2.SEMESTERNAME, '') || '-' || VALUE(T1.TESTITEMNAME, '') AS TESTNAME ";
        $query .= " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ";
        $query .= "    AND T11.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T11.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T11.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T11.SCORE_DIV = T1.SCORE_DIV ";
        $query .= " INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "; // 指定の科目が登録されていれば登録された科目、登録されていなければ00-X-00-000000を使用する
        } else {
            $query .= "   AND T11.SUBCLASSCD IN "; // 指定の科目が登録されていれば登録された科目、登録されていなければ00-X-00-000000を使用する
        }
        $query .= "    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ";
        if ($model->Properties["useRemarkFlg"] == '1' && $remarkFlg == "REMARK_FLG") {
            $query .= "    AND T1.REMARK_FLG = '1' ";
        } else {
            $query .= "    AND T1.SIDOU_INPUT = '1' ";
        }
        $query .= " ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ";
        return $query;
    }

    //学期
    function getSemesterSdate() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '1' ";
        return $query;
    }

    //年度の日付範囲
    function getSemester9() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SDATE, EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '9' ";
        return $query;
    }

    //Z010
    function getZ010() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        return $query;
    }
}
?>
