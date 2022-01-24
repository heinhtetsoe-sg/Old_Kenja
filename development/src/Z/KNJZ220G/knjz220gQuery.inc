<?php

require_once('for_php7.php');


class knjz220gQuery extends Query {

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 学校種別
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    //テスト種別取得（年度末）
    function getTestone($model, $semester, $schoolKind = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ";
        $query .= "    AND T11.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T11.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T11.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T11.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T11.CLASSCD      = '00' ";
        $query .= "    AND T11.CURRICULUM_CD  = '00' ";
        $query .= "    AND T11.SUBCLASSCD  = '000000' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T11.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //テスト取得
    function getTest($model, $schoolKind = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER   || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER   || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ";
        $query .= "    AND T11.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T11.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T11.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T11.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T11.CLASSCD      = '00' ";
        $query .= "    AND T11.CURRICULUM_CD  = '00' ";
        $query .= "    AND T11.SUBCLASSCD  = '000000' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T11.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND T11.TESTKINDCD = '99' ";
        $query .= "     AND T11.TESTITEMCD = '00' ";
        $query .= "     AND T11.SCORE_DIV  = '08' ";
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }
    
    //科目取得
    function getSubclass($model) {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '：' || T1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧情報取得
    function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '：' || T1.SUBCLASSNAME AS SUBCLASS, ";
        $query .= "     T2.ASSESS_TBL_DIV, ";
        $query .= "     T2.CALC_UNIT_DIV ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " LEFT JOIN ASSESS_AVG_CALC_MST T2 ON ";
        $query .= "    T2.CLASSCD = T1.CLASSCD ";
        $query .= "    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "    AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        if ($model->field["SUBCLASSCD"] != 'ALL') {
            $query .= "AND T1.SUBCLASSCD = '".SUBSTR($model->field["SUBCLASSCD"],7,6)."' ";
        }

        $query .= " ORDER BY ";
        $query .= "     SUBCLASS ";

        return $query;
    }

    //Insert作成
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $ii = $model->field["SUBCLASSCNT"];
        for($i=1; $i<=$ii; $i++)
        {

            //削除
            $query  =  " DELETE FROM ASSESS_AVG_CALC_MST ";
            $query .=  " WHERE YEAR          = '".$model->field["YEAR"]."' ";
            $query .=  "   AND GRADE         = '".$model->field["GRADE"]."' ";
            $query .=  "   AND SEMESTER      = '".SUBSTR($model->field["TESTCD"],0,1)."' ";
            $query .=  "   AND TESTKINDCD    = '".SUBSTR($model->field["TESTCD"],2,2)."' ";
            $query .=  "   AND TESTITEMCD    = '".SUBSTR($model->field["TESTCD"],4,2)."' ";
            $query .=  "   AND SCORE_DIV     = '".SUBSTR($model->field["TESTCD"],6,2)."' ";
            $query .=  "   AND CLASSCD       = '".SUBSTR($model->field["SUBCLASS".$i],0,2)."' ";
            $query .=  "   AND SCHOOL_KIND   = '".SUBSTR($model->field["SUBCLASS".$i],3,1)."' ";
            $query .=  "   AND CURRICULUM_CD = '".SUBSTR($model->field["SUBCLASS".$i],5,1)."' ";
            $query .=  "   AND SUBCLASSCD    = '".SUBSTR($model->field["SUBCLASS".$i],7,6)."' ";

            $db->query($query);

            //登録
            $data = array();
            $data["YEAR"][TEXT]             = $model->field["YEAR"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["SEMESTER"][TEXT]         = SUBSTR($model->field["TESTCD"],0,1);
            $data["TESTKINDCD"][TEXT]       = SUBSTR($model->field["TESTCD"],2,2);
            $data["TESTITEMCD"][TEXT]       = SUBSTR($model->field["TESTCD"],4,2);
            $data["SCORE_DIV"][TEXT]        = SUBSTR($model->field["TESTCD"],6,2);
            $data["CLASSCD"][TEXT]          = SUBSTR($model->field["SUBCLASS".$i],0,2);
            $data["SCHOOL_KIND"][TEXT]      = SUBSTR($model->field["SUBCLASS".$i],3,1);
            $data["CURRICULUM_CD"][TEXT]    = SUBSTR($model->field["SUBCLASS".$i],5,1);
            $data["SUBCLASSCD"][TEXT]       = SUBSTR($model->field["SUBCLASS".$i],7,6);
            $data["ASSESS_TBL_DIV"][TEXT]   = $model->field["ASSESS_TBL_DIV".$i];
            $data["CALC_UNIT_DIV"][TEXT]    = $model->field["CALC_UNIT_DIV".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ASSESS_AVG_CALC_MST");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //前年度データ取得
    function getCopyData($model, $year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .=  " FROM ";
        $query .=  "    ASSESS_AVG_CALC_MST ";
        $query .=  " WHERE YEAR          = '".$year."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery($model, $pre_year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ASSESS_AVG_CALC_MST ";
        $query .=  " WHERE YEAR          = '".$model->field["YEAR"]."' ";
        $db->query($query);

        //前年度データ取得
        $query = knjz220gQuery::getCopyData($model, $pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //登録
            $data = array();
            $data["YEAR"][TEXT]             = $model->field["YEAR"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["SEMESTER"][TEXT]         = $row["SEMESTER"];
            $data["TESTKINDCD"][TEXT]       = $row["TESTKINDCD"];
            $data["TESTITEMCD"][TEXT]       = $row["TESTITEMCD"];
            $data["SCORE_DIV"][TEXT]        = $row["SCORE_DIV"];
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
            $data["ASSESS_TBL_DIV"][TEXT]   = $row["ASSESS_TBL_DIV"];
            $data["CALC_UNIT_DIV"][TEXT]    = $row["CALC_UNIT_DIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ASSESS_AVG_CALC_MST");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
