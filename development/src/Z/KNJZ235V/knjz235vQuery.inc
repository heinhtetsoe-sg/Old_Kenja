<?php

require_once('for_php7.php');

class knjz235vQuery extends Query {

    //学年取得（権限チェック）
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND <> 'P' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科取得
    function getCourseMajor() {
        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE, ";
        $query .= "     COURSECD || '-' || MAJORCD || ' ' || COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト一覧取得
    function getTestList($model) {
        $query  = " SELECT ";
        $query .= "     '1' || ':' || T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV AS VALUE, ";
        $query .= "     '考査：' || T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ' ' || S1.SEMESTERNAME || ' ' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST S1 ON T1.YEAR = S1.YEAR AND T1.SEMESTER = S1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //選択テスト取得
    function getSelectTest($model) {

        list($coursecd, $majorcd) = explode('-', $model->field["MAJORCD"]);

        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TEST_DIV || ':' || T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV AS VALUE, ";
        $query .= "     '考査：' || T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ' ' || S1.SEMESTERNAME || ' ' || T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST S1 ON T1.YEAR = S1.YEAR AND T1.SEMESTER = S1.SEMESTER, ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.TEST_DIV     = '1' AND ";
        $query .= "     T1.GRADE        = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.COURSECD     = '".$coursecd."' AND ";
        $query .= "     T1.MAJORCD      = '".$majorcd."' AND ";
        $query .= "     T1.SEMESTER     <> '9' AND ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "     T1.TESTKINDCD   = T2.TESTKINDCD AND ";
        $query .= "     T1.TESTITEMCD   = T2.TESTITEMCD AND ";
        $query .= "     T1.SCORE_DIV    = T2.SCORE_DIV ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //更新
    function &getUpdateQuery($model) {
        //配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time());     //更新日付

        list($coursecd, $majorcd) = explode('-', $model->field["MAJORCD"]);

        //RECORD_PROFICIENCY_ORDER_SDIV_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '" .CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     COURSECD    = '".$coursecd."' AND ";
        $query .= "     MAJORCD     = '".$majorcd."' ";
        $db->query($query);

        //考査順の登録
        if ($array[0] !="") {
            for ($i = 0; $i < get_count($array); $i++){
                //初期化
                $data       = array();
                $testdiv    = "";
                $semester   = "";
                $testkindcd = "";
                $testitemcd = "";
                $score_div  = "";

                list($testdiv, $semester, $testkindcd, $testitemcd, $score_div) = explode(":", $array[$i]);

                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["GRADE"][TEXT]                = $model->field["GRADE"];
                $data["COURSECD"][TEXT]             = $coursecd;
                $data["MAJORCD"][TEXT]              = $majorcd;
                $data["SEQ"][NUMBER]                = $i+1;
                $data["TEST_DIV"][TEXT]             = $testdiv;
                $data["SEMESTER"][TEXT]             = $semester;
                $data["TESTKINDCD"][TEXT]           = $testkindcd;
                $data["TESTITEMCD"][TEXT]           = $testitemcd;
                $data["SCORE_DIV"][TEXT]            = $score_div;
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $query = Query::insertSQL($data, "RECORD_PROFICIENCY_ORDER_SDIV_DAT");
                $db->query($query);
            }
        }
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //今年度のデータ件数取得
    function cntThisYearData($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GDAT.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     GDAT.YEAR = T1.YEAR AND ";
                $query .= "                     GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 UNION ";
                $query .= "                 SELECT '00' FROM SYSIBM.SYSDUMMY1) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GDAT.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     GDAT.YEAR = T1.YEAR AND ";
            $query .= "                     GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 UNION ";
            $query .= "                 SELECT '00' FROM SYSIBM.SYSDUMMY1) ";
        }

        $cntThis = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntThis;
    }

    //前年度のデータ件数取得
    function cntLastYearData($model) {
        $db = Query::dbCheckOut();
        $last_year = CTRL_YEAR - 1;
        $this_year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$last_year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GDAT.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     GDAT.YEAR = '".$this_year."' AND ";
                $query .= "                     GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 UNION ";
                $query .= "                 SELECT '00' FROM SYSIBM.SYSDUMMY1) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GDAT.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     GDAT.YEAR = '".$this_year."' AND ";
            $query .= "                     GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 UNION ";
            $query .= "                 SELECT '00' FROM SYSIBM.SYSDUMMY1) ";
        }

        $cntLast = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntLast;
    }

    //前年度からコピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $last_year = CTRL_YEAR - 1;
        $this_year = CTRL_YEAR;

        $query  = " INSERT INTO RECORD_PROFICIENCY_ORDER_SDIV_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD, ";
        $query .= "             SEQ, ";
        $query .= "             TEST_DIV, ";
        $query .= "             SEMESTER, ";
        $query .= "             TESTKINDCD, ";
        $query .= "             TESTITEMCD, ";
        $query .= "             SCORE_DIV, ";
        $query .= "             PROFICIENCYDIV, ";
        $query .= "             PROFICIENCYCD, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$this_year."', ";
        $query .= "         T1.GRADE, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         SEQ, ";
        $query .= "         TEST_DIV, ";
        $query .= "         SEMESTER, ";
        $query .= "         TESTKINDCD, ";
        $query .= "         TESTITEMCD, ";
        $query .= "         SCORE_DIV, ";
        $query .= "         PROFICIENCYDIV, ";
        $query .= "         PROFICIENCYCD, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         RECORD_PROFICIENCY_ORDER_SDIV_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$last_year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GDAT.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     GDAT.YEAR = '".$this_year."' AND ";
                $query .= "                     GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 UNION ";
                $query .= "                 SELECT '00' FROM SYSIBM.SYSDUMMY1) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GDAT.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     GDAT.YEAR = '".$this_year."' AND ";
            $query .= "                     GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                 UNION ";
            $query .= "                 SELECT '00' FROM SYSIBM.SYSDUMMY1) ";
        }
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
