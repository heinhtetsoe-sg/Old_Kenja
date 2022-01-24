<?php

require_once('for_php7.php');

class knjd619Query extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON ";
        $query .= "         T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND IN ('J','H') ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($semester)
    {
        $query  = " WITH PROV_RATE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= "     FROM ";
        $query .= "         RECORD_PROV_RATE_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$semester."' ";
        $query .= " ), COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "         TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "         TESTKINDCD AS KINDCD, ";
        $query .= "         TESTITEMCD AS ITEMCD ";
        $query .= "     FROM ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$semester."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.LABEL, ";
        $query .= "     T1.VALUE ";
        $query .= " FROM ";
        $query .= "     PROV_RATE T1 ";
        $query .= "     LEFT JOIN COUNTFLG T2 ON T1.VALUE = T2.VALUE ";
        $query .= " ORDER BY ";
        $query .= "     T2.KINDCD, ";
        $query .= "     T2.ITEMCD ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            if ($model->field["SEMESTER"] == "9") {
                $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            } else {
                $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            }
            $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        }

        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            } else {
                $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            }
            $query .= "     GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    //科目取得
    function getSubClass($model) 
    {
        //変数
        $year = CTRL_YEAR;
        $selectSeme = $model->field["SEMESTER"];
        $schregSeme = ($selectSeme == "9") ? CTRL_SEMESTER : $selectSeme;
        $testkindcd = $model->field["TESTKINDCD"];
        $arrData = explode(",", $model->selectdata);
        $gradeHrClassIn = implode($arrData, "','");

        //SQL
        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     value(T3.ELECTDIV,'0') as ELECTDIV, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     RECORD_PROV_RATE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '".$year."' ";
        $query .= "                                  AND T2.SEMESTER = '".$schregSeme."' ";
        $query .= "                                  AND T2.GRADE || T2.HR_CLASS IN ('".$gradeHrClassIn."') ";
        $query .= "                                  AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                              AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                              AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SEMESTER = '".$selectSeme."' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '".$testkindcd."' ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     value(T3.ELECTDIV,'0'), ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     value(T3.ELECTDIV,'0'), ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力
    function getCsvQuery($model)
    {
        //変数
        $year = CTRL_YEAR;
        $selectSeme = $model->field["SEMESTER"];
        $selectTest = $model->field["TESTKINDCD"];
        $schregSeme = ($selectSeme == "9") ? CTRL_SEMESTER : $selectSeme;
        $arrData = explode(",", $model->selectdata);
        $gradeHrClassIn = implode($arrData, "','");

        //対象生徒
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".$year."' AND ";
        $query .= "    SEMESTER = '".$schregSeme."' AND ";
        $query .= "    GRADE || HR_CLASS IN ('".$gradeHrClassIn."') ";
        $query .= ") ";

        //仮評価成績データ
        $query .= " , PROV_RATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        } 
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.VALUE ";
        $query .= "     FROM ";
        $query .= "         RECORD_PROV_RATE_DAT T1 ";
        $query .= "         INNER JOIN SCH_T T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$year."' AND ";
        $query .= "         T1.SEMESTER = '".$selectSeme."' AND ";
        $query .= "         T1.TESTKINDCD || T1.TESTITEMCD = '".$selectTest."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.VALUE ";
        $query .= "     ) ";

        //メインデータ
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T4.ABBV1 AS SEX, ";
        for ($i = 0; $i < get_count($model->arrSubclass); $i++) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     SUM( CASE T1.SUBCLASSCD WHEN '".substr($model->arrSubclass[$i], 7, 6)."' THEN T1.VALUE ELSE NULL END ) as SCORE" .$i .", ";
            } else {
                $query .= "     SUM( CASE T1.SUBCLASSCD WHEN '".$model->arrSubclass[$i]."' THEN T1.VALUE ELSE NULL END ) as SCORE" .$i .", ";
            }
        }
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*10,0)/10,5,1) AS AVERAGE ";
        $query .= " FROM ";
        $query .= "     PROV_RATE T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCH_T T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST T4 ON T2.SEX = T4.NAMECD2 AND T4.NAMECD1 = 'Z002' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T4.ABBV1 ";
        $query .= " ORDER BY ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO ";

        return $query;
    }
}
?>
