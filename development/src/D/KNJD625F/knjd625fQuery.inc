<?php

require_once('for_php7.php');

class knjd625fQuery extends Query {

    //選択可能年度取得
    function getYearSelect() {
        $query  = " SELECT DISTINCT ";
        $query .= "   YEAR AS VALUE, ";
        $query .= "   YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "   AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR < '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "  YEAR DESC ";
        return $query;
    }

    //校種取得
    function getSchoolKind() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= "     , NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTER || ':' || SEMESTERNAME AS LABEL ";
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
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE || ':' || GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE";

        return $query;
    }

    //大学名称一覧取得
    function getCollege($model) {
        $query .= " WITH SET_NUM_TBL AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "   ROW_NUMBER() OVER(partition by T1.STAT_CD) AS ROWNUMBER, ";
        $query .= "   T1.STAT_CD AS VALUE, ";
        $query .= "   T1.STAT_CD || ':' || VALUE(T2.SCHOOL_NAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "   AFT_GRAD_COURSE_DAT T1 ";
        $query .= "   LEFT JOIN COLLEGE_MST T2 ";
        $query .= "     ON T2.SCHOOL_CD = T1.STAT_CD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR BETWEEN '".$model->field["STRT_YEAR"]."' AND '".$model->field["END_YEAR"]."' ";
        $query .= "   AND SENKOU_KIND = '0' ";
        $query .= "   AND T1.STAT_CD IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "   T1.STAT_CD ";
        $query .= " ) ";
        $query .= " SELECT * FROM SET_NUM_TBL WHERE ROWNUMBER = 1 ";  //重複コードの表示を無くす
        return $query;
    }

    //模擬試験一覧取得
    function getTestInfo($model) {
        if ($model->field["TESTTYPE"] == "2") {
            $query .= " SELECT ";
            $query .= "   T1.MOCKCD || ':' || T2.MOCKNAME1 AS LABEL, ";
            $query .= "   T1.MOCKCD AS VALUE, ";
            $query .= "   MAX(SUBSTR(T1.MOCKCD, 2, 4)) AS CHKMX ";
            $query .= " FROM ";
            $query .= "   MOCK_RANK_RANGE_DAT T1 ";
            $query .= "   LEFT JOIN MOCK_MST T2 ";
            $query .= "     ON T2.MOCKCD = T1.MOCKCD ";
            $query .= " GROUP BY ";
            $query .= "   T1.MOCKCD, ";
            $query .= "   T2.MOCKNAME1 ";
        } else {
            $query .= " WITH SET_CD_TBL AS ( ";
            $query .= " SELECT ";
            $query .= "   SEMESTER || '-' || TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
            $query .= "   MAX(YEAR) AS MXYEAR ";
            $query .= " FROM ";
            $query .= "   TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            $query .= " WHERE ";
            $query .= "   SCORE_DIV = '01' ";
            $query .= " GROUP BY ";
            $query .= "   SEMESTER, ";
            $query .= "   TESTKINDCD, ";
            $query .= "   TESTITEMCD, ";
            $query .= "   SCORE_DIV ";
            $query .= " ORDER BY ";
            $query .= "   SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "   T1.VALUE || ':' || T2.TESTITEMNAME AS LABEL, ";
            $query .= "   T1.VALUE ";
            $query .= " FROM ";
            $query .= "   SET_CD_TBL T1 ";
            $query .= "   LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
            $query .= "     ON T2.YEAR = T1.MXYEAR ";
            $query .= "    AND T2.SEMESTER || '-' || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = T1.VALUE ";
        }
        return $query;
    }

    //科目一覧取得
    function getSubclassCd($model)
    {
        if ($model->field["TESTTYPE"] == "2") {
            //過去全ての模試科目コードを選択できるようにする
            $query .= " SELECT ";
            $query .= "     T3.SUBCLASS_NAME AS SUBCLASSNAME, ";
            $query .= "     T1.MOCK_SUBCLASS_CD AS SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "     MOCK_RANK_RANGE_DAT T1 ";
            $query .= "     LEFT JOIN MOCK_SUBCLASS_MST T3 ";
            $query .= "       ON T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
            $query .= " GROUP BY ";
            $query .= "     T1.MOCK_SUBCLASS_CD, ";
            $query .= "     T3.SUBCLASS_NAME ";
            $query .= " ORDER BY ";
            $query .= "     T1.MOCK_SUBCLASS_CD ";
            $query .= "  ";
        } else {
            //すべての科目コードを選択できるようにする(ただし、YDATはチェック)
            $query .= " SELECT ";
            $query .= "   T1.SUBCLASSNAME, ";
            $query .= "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "   SUBCLASS_MST T1 ";
            $query .= "   INNER JOIN SUBCLASS_YDAT T2 ";
            $query .= "     ON T2.YEAR BETWEEN '".$model->field["STRT_YEAR"]."' AND '".$model->field["END_YEAR"]."' ";
            $query .= "    AND T2.CLASSCD = T1.CLASSCD";
            $query .= "    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "    AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " ORDER BY ";
            $query .= "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        }
        return $query;
    }

    // //先科目取得
    // function getCombinedSubclassCd($subclasscd)
    // {
    //     $query  = " SELECT ";
    //     $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
    //     $query .= "     , L1.SUBCLASSNAME ";
    //     $query .= " FROM ";
    //     $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
    //     $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
    //     $query .= "         ON L1.CLASSCD = T1.COMBINED_CLASSCD ";
    //     $query .= "         AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
    //     $query .= "         AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
    //     $query .= "         AND L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
    //     $query .= " WHERE ";
    //     $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
    //     $query .= "     AND T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD = '".$subclasscd."' ";
    //     $query .= " ORDER BY ";
    //     $query .= "     T1.COMBINED_SCHOOL_KIND ";
    //     $query .= "     , T1.COMBINED_CLASSCD ";
    //     $query .= "     , T1.COMBINED_CURRICULUM_CD ";
    //     $query .= "     , T1.COMBINED_SUBCLASSCD ";
    // 
    //     return $query;
    // }

}
?>
