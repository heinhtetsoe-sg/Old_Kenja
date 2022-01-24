<?php

require_once('for_php7.php');

class knjz380qQuery extends Query {

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '" .CTRL_YEAR ."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";

        return $query;
    }

    //リスト内情報取得
    function getListdata($model)
    {
        $query = "";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.SCHOOLCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     CASE WHEN T1.TESTKINDCD = '99' AND T1.SEMESTER <> '9' THEN '学期末' ";
        $query .= "          WHEN T1.TESTKINDCD = '99' AND T1.SEMESTER = '9' THEN '学年末' ";
        $query .= "          ELSE T4.TESTKINDNAME ";
        $query .= "     END AS TESTKINDNAME, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.TESTITEMNAME, ";
        $query .= "     T1.TESTITEMABBV1, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COUNTFLG, ";
        $query .= "     T1.TEST_START_DATE, ";
        $query .= "     T1.TEST_END_DATE, ";
        $query .= "     CASE WHEN T1.COUNTFLG = '1' THEN '集計する' ";
        $query .= "          WHEN T1.COUNTFLG = '0' THEN '集計しない' ";
        $query .= "          ELSE NULL END AS COUNTFLG_SHOW, ";
        $query .= "     T3.SEMESTER_DETAIL || '：' || T3.SEMESTERNAME AS DETAIL_NAME, ";
        $query .= "     CASE WHEN T1.SIDOU_INPUT = '1' THEN 'あり' ";
        $query .= "          ELSE NULL END AS SIDOU_INPUT_SHOW, ";
        $query .= "     CASE WHEN T1.SIDOU_INPUT_INF = '1' THEN '：記号' ";
        $query .= "          WHEN T1.SIDOU_INPUT_INF = '2' THEN '：得点' ";
        $query .= "          ELSE NULL END AS SIDOU_INPUT_INF_SHOW, ";
        $query .= "     CASE WHEN T1.JYORETSU_FLG = '1' THEN 'あり' ";
        $query .= "          ELSE NULL END AS JYORETSU_FLG_SHOW, ";
        $query .= "     CASE WHEN T1.NOT_USE_CSV_FLG = '1' THEN '不可' ";
        $query .= "          ELSE NULL END AS NOT_USE_CSV_FLG_SHOW, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        $query .= "     LEFT JOIN TESTKIND_MST T4 ON T4.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "          AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN SEMESTER_DETAIL_MST T3 ON T3.YEAR = T1.YEAR ";
        $query .= "          AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T3.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ";
        $query .= " WHERE ";
        //権限による絞込み
        $query .= "     T1.YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND T1.GRADE = '00' ";
        $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->leftCourseMajor}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV ";

        return $query;
    }

    //１レコード取得
    function getRightData($model)
    {

        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' ";
        $query .= "  AND SEMESTER = '" .$model->semester ."' ";
        $query .= "  AND SCHOOLCD = '" .$model->schoolCd ."' ";
        $query .= "  AND SCHOOL_KIND = '" .$model->schoolKind ."' ";
        $query .= "  AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= "  AND TESTITEMCD = '" .$model->testitemcd ."' ";
        $query .= "  AND SCORE_DIV = '" .$model->score_div ."' ";
        $query .= "  AND GRADE = '" .$model->grade ."' ";
        $query .= "  AND COURSECD = '" .$model->courseCd ."' ";
        $query .= "  AND MAJORCD = '" .$model->majorCd ."' ";

        return $query;
    }

    //考査種別（大分類）取得
    function getTestKindName($model)
    {
        if ($model->field["SEMESTER"] === '9') {
            $query   = " SELECT DISTINCT ";
            $query  .= "    '99' || ' ' || '学年末' AS LABEL, ";
            $query  .= "    '99' AS VALUE ";
        } else {
            $query   = " SELECT ";
            $query  .= "    TESTKINDCD || ' ' || TESTKINDNAME AS LABEL, ";
            $query  .= "    TESTKINDCD AS VALUE ";
            $query  .= " FROM";
            $query  .= "    TESTKIND_MST ";
            $query  .= " UNION ";
            $query  .= " SELECT ";
            $query  .= "    '99' || ' ' || '学期末' AS LABEL, ";
            $query  .= "    '99' AS VALUE ";
        }
        $query  .= " FROM";
        $query  .= "    TESTKIND_MST ";
        $query  .= " ORDER BY ";
        $query  .= "    VALUE";

        return $query;
    }
    
    //考査種別（小分類）取得
    function getScoreDiv($model)
    {
        $query   = " SELECT ";
        if ($model->field["SEMESTER"] !== '9') {
            if ($model->field["TESTKINDCD"] === '99') {
                $query  .= "    NAMECD2 || ' ' || NAME2 AS LABEL, ";
                $query  .= "    NAMECD2 AS VALUE ";
            } else {
                $query  .= "    NAMECD2 || ' ' || NAME1 AS LABEL, ";
                $query  .= "    NAMECD2 AS VALUE ";
            }
        } else {
            $query  .= "    NAMECD2 || ' ' || NAME3 AS LABEL, ";
            $query  .= "    NAMECD2 AS VALUE ";
        }
        $query  .= " FROM ";
        $query  .= "     V_NAME_MST ";
        $query  .= " WHERE ";
        $query  .= "     YEAR    = '" .CTRL_YEAR ."' ";
        $query  .= " AND NAMECD1 = 'D053' ";
        if ($model->field["SEMESTER"] === '9') {
            $query  .= "  AND  NAMECD2 NOT IN ('01', '02') ";
        }
        $query  .= " ORDER BY ";
        $query  .= "    VALUE ";

        return $query;
    }

    //学期名の取得
    function getSemester($model)
    {
        $query   = " SELECT ";
        $query  .= "    SEMESTERNAME AS LABEL, ";
        $query  .= "    SEMESTER AS VALUE ";
        $query  .= " FROM ";
        $query  .= "    SEMESTER_MST ";
        $query  .= " WHERE ";
        $query  .= "    YEAR = '" .CTRL_YEAR ."' ";
        $query  .= " ORDER BY ";
        $query  .= "    VALUE ";

        return $query;
    }

    //学期詳細名の取得
    function getSemesterDetail($Row)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER = '{$Row["SEMESTER"]}' ";

        return $query;
    }

    //学期詳細の日付を取得してくる
    function getSemesterDetail_sdate_edate($Row) {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '" .CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER        = '{$Row["SEMESTER"]}' ";
        $query .= "     AND SEMESTER_DETAIL = '{$Row["SEMESTER_DETAIL"]}' ";

        return $query;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "";
        $query .= " INSERT INTO TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV( ";
        $query .= "     YEAR, ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     TESTITEMNAME, ";
        $query .= "     TESTITEMABBV1, ";
        $query .= "     COUNTFLG, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SIDOU_INPUT, ";
        $query .= "     SIDOU_INPUT_INF, ";
        $query .= "     JYORETSU_FLG, ";
        $query .= "     NOT_USE_CSV_FLG, ";
        $query .= "     TEST_START_DATE, ";
        $query .= "     TEST_END_DATE, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      SCHOOLCD, ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      SEMESTER, ";
        $query .= "      TESTKINDCD, ";
        $query .= "      TESTITEMCD, ";
        $query .= "      SCORE_DIV, ";
        $query .= "      GRADE, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      TESTITEMNAME, ";
        $query .= "      TESTITEMABBV1, ";
        $query .= "      COUNTFLG, ";
        $query .= "      SEMESTER_DETAIL, ";
        $query .= "      SIDOU_INPUT, ";
        $query .= "      SIDOU_INPUT_INF, ";
        $query .= "      JYORETSU_FLG, ";
        $query .= "      NOT_USE_CSV_FLG, ";
        $query .= "      TEST_START_DATE, ";
        $query .= "      TEST_END_DATE, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' ";
        $query .= "      AND SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "      AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "      AND GRADE = '00' ";
        $query .= "      AND not exists ( ";
        $query .= "                  SELECT 'x' FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        $query .= "                  WHERE ";
        $query .= "                  T1.YEAR = '" . CTRL_YEAR ."' ";
        $query .= "                  AND T1.SCHOOLCD = T0.SCHOOLCD ";
        $query .= "                  AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ";
        $query .= "                  AND T1.SEMESTER = T0.SEMESTER ";
        $query .= "                  AND T1.TESTKINDCD = T0.TESTKINDCD ";
        $query .= "                  AND T1.TESTITEMCD = T0.TESTITEMCD ";
        $query .= "                  AND T1.SCORE_DIV = T0.SCORE_DIV ";
        $query .= "                  AND T1.GRADE = T0.GRADE ";
        $query .= "                  AND T1.COURSECD = T0.COURSECD ";
        $query .= "                  AND T1.MAJORCD = T0.MAJORCD ";
        $query .= "                  ) ";
        $query .= "      AND (TESTKINDCD IN (SELECT TESTKINDCD FROM TESTKIND_MST) OR TESTKINDCD = '99') ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //課程学科からのコピー
    function &getCopyQuery2($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);
        $query = "";
        $query .= " INSERT INTO TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV( ";
        $query .= "     YEAR, ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     SCORE_DIV, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     TESTITEMNAME, ";
        $query .= "     TESTITEMABBV1, ";
        $query .= "     COUNTFLG, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SIDOU_INPUT, ";
        $query .= "     SIDOU_INPUT_INF, ";
        $query .= "     JYORETSU_FLG, ";
        $query .= "     NOT_USE_CSV_FLG, ";
        $query .= "     TEST_START_DATE, ";
        $query .= "     TEST_END_DATE, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "      YEAR, ";
        $query .= "      SCHOOLCD, ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      SEMESTER, ";
        $query .= "      TESTKINDCD, ";
        $query .= "      TESTITEMCD, ";
        $query .= "      SCORE_DIV, ";
        $query .= "      GRADE, ";
        $query .= "      '{$coursecd}', ";
        $query .= "      '{$majorcd}', ";
        $query .= "      TESTITEMNAME, ";
        $query .= "      TESTITEMABBV1, ";
        $query .= "      COUNTFLG, ";
        $query .= "      SEMESTER_DETAIL, ";
        $query .= "      SIDOU_INPUT, ";
        $query .= "      SIDOU_INPUT_INF, ";
        $query .= "      JYORETSU_FLG, ";
        $query .= "      NOT_USE_CSV_FLG, ";
        $query .= "      TEST_START_DATE, ";
        $query .= "      TEST_END_DATE, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T0 ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "      AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "      AND GRADE = '00' ";
        $query .= "      AND COURSECD || '-' || MAJORCD = '{$model->field["MOTO_COURSE_MAJOR"]}' ";
        $query .= "      AND not exists ( ";
        $query .= "                  SELECT 'x' FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        $query .= "                  WHERE ";
        $query .= "                  T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND T1.SCHOOLCD = T0.SCHOOLCD ";
        $query .= "                  AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ";
        $query .= "                  AND T1.SEMESTER = T0.SEMESTER ";
        $query .= "                  AND T1.TESTKINDCD = T0.TESTKINDCD ";
        $query .= "                  AND T1.TESTITEMCD = T0.TESTITEMCD ";
        $query .= "                  AND T1.SCORE_DIV = T0.SCORE_DIV ";
        $query .= "                  AND T1.GRADE = T0.GRADE ";
        $query .= "                  AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= "                  ) ";
        $query .= "      AND (TESTKINDCD IN (SELECT TESTKINDCD FROM TESTKIND_MST) OR TESTKINDCD = '99') ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //存在チェック
    function getExists($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND SCHOOLCD = '" .$model->field["SCHOOLCD"] ."' ";
        $query .= "     AND SCHOOL_KIND = '" .$model->field["SCHOOL_KIND"] ."' ";
        $query .= "     AND SEMESTER = '" .$model->field["SEMESTER"] ."' ";
        $query .= "     AND TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' ";
        $query .= "     AND TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' ";
        $query .= "     AND SCORE_DIV  = '" .$model->field["SCORE_DIV"] ."' ";
        $query .= "     AND GRADE  = '" .$model->field["GRADE"] ."' ";
        $query .= "     AND COURSECD || '-' || MAJORCD  = '" .$model->field["RIGHT_COURSE_MAJOR"] ."' ";

        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $sem_detail = ($model->Properties["Semester_Detail_Hyouji"] == "1") ? $model->field["SEMESTER_DETAIL"] : $db->getOne(knjz380qQuery::getSemDetail($model));

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["RIGHT_COURSE_MAJOR"]);

        $db->autoCommit(false);
        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOLCD = '{$model->field["SCHOOLCD"]}' ";
        $query .= " AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= " AND TESTKINDCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= " AND TESTITEMCD = '{$model->field["TESTITEMCD"]}' ";
        $query .= " AND SCORE_DIV  = '{$model->field["SCORE_DIV"]}' ";
        $query .= " AND GRADE  = '{$model->field["GRADE"]}' ";
        $query .= " AND COURSECD  = '{$coursecd}' ";
        $query .= " AND MAJORCD  = '{$majorcd}' ";
        $db->query($query);

        $query = "";
        $data = array();

        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["SCHOOLCD"][TEXT]        = $model->field["SCHOOLCD"];
        $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHOOL_KIND"];
        $data["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]      = $model->field["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]      = $model->field["TESTITEMCD"];
        $data["SCORE_DIV"][TEXT]       = $model->field["SCORE_DIV"];
        $data["GRADE"][TEXT]           = $model->field["GRADE"];
        $data["COURSECD"][TEXT]        = $coursecd;
        $data["MAJORCD"][TEXT]         = $majorcd;
        $data["TESTITEMNAME"][TEXT]    = $model->field["TESTITEMNAME"];
        $data["TESTITEMABBV1"][TEXT]   = $model->field["TESTITEMABBV1"];
        $data["COUNTFLG"][TEXT]        = ($model->field["COUNTFLG"] == "1") ? "1" : "0" ;
        $data["SEMESTER_DETAIL"][TEXT] = $sem_detail;
        $data["SIDOU_INPUT"][TEXT]     = ($model->field["SIDOU_INPUT"] == "1") ? "1" : "0" ;
        $data["SIDOU_INPUT_INF"][TEXT] = $model->field["SIDOU_INPUT_INF"];
        $data["JYORETSU_FLG"][TEXT]    = ($model->field["JYORETSU_FLG"] == "1") ? "1" : "0" ;
        $data["NOT_USE_CSV_FLG"][TEXT] = ($model->field["NOT_USE_CSV_FLG"] == "1") ? "1" : "0" ;
        $data["TEST_START_DATE"][TEXT] = str_replace("/", "-",  $model->field["TEST_START_DATE"]);
        $data["TEST_END_DATE"][TEXT]   = str_replace("/", "-",  $model->field["TEST_END_DATE"]);
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][NUMBER]       = "SYSDATE()";

        $query = Query::insertSQL($data, "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["RIGHT_COURSE_MAJOR"]);
        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SCHOOLCD = '{$model->field["SCHOOLCD"]}' ";
        $query .= " AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " AND SEMESTER = '" .$model->semester ."' ";
        $query .= " AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= " AND TESTITEMCD = '" .$model->testitemcd ."' ";
        $query .= " AND SCORE_DIV  = '" .$model->score_div ."' ";
        $query .= " AND GRADE  = '{$model->field["GRADE"]}' ";
        $query .= " AND COURSECD  = '{$coursecd}' ";
        $query .= " AND MAJORCD  = '{$majorcd}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //出欠集計範囲取得
    function getSemDetail($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLCD    = '{$model->field["SCHOOLCD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "     AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND TESTKINDCD  = '".$model->field["TESTKINDCD"]."' ";
        $query .= "     AND TESTITEMCD  = '".$model->field["TESTITEMCD"]."' ";
        $query .= "     AND SCORE_DIV   = '".$model->field["SCORE_DIV"]."' ";
        $query .= "     AND GRADE       = '{$model->field["GRADE"]}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD  = '{$model->field["RIGHT_COURSE_MAJOR"]}' ";

        return $query;
    }
}
?>
