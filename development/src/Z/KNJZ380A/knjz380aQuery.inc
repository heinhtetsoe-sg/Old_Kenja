<?php

require_once('for_php7.php');
class knjz380aQuery extends Query {

    //リスト内情報取得
    function getListdata($model)
    {
        $query = "";
        $query .= "SELECT DISTINCT";
        $query .= "  T1.SEMESTER, ";
        $query .= "  T2.SEMESTERNAME, ";
        $query .= "  T1.TESTKINDCD, ";
        $query .= "  T1.TESTKINDCD || ' ' || T4.TESTKINDNAME AS TESTKIND_SHOW, ";
        $query .= "  T4.TESTKINDCD AS TESTKIND_MST_TESTKINDCD, ";
        $query .= "  T1.TESTITEMCD, ";
        $query .= "  T1.TESTITEMNAME, ";
        $query .= "  T1.COUNTFLG, ";
        $query .= "  CASE WHEN T1.COUNTFLG = '1' THEN '集計する' ";
        $query .= "       WHEN T1.COUNTFLG = '0' THEN '集計しない' ";
        $query .= "       ELSE NULL END AS COUNTFLG_SHOW, ";
        $query .= "  T3.SEMESTER_DETAIL || '：' || T3.SEMESTERNAME AS DETAIL_NAME, ";
        //テスト期間
        if ($model->Properties["Test_Period_Hyouji"] == "1") {
            $query .= " T1.TEST_START_DATE, ";
            $query .= " T1.TEST_END_DATE, ";
        }
        $query .= "  T1.UPDATED ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG_NEW T1 ";
        $query .= "LEFT JOIN ";
        $query .= "  TESTKIND_MST T4 ON T4.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "LEFT JOIN ";
        $query .= "  SEMESTER_MST T2 ON  T2.YEAR = T1.YEAR ";
        $query .= "                  AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "LEFT JOIN ";
        $query .= "  SEMESTER_DETAIL_MST T3 ON  T3.YEAR = T1.YEAR ";
        $query .= "                         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND T3.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ";
        $query .= "WHERE ";
        //権限による絞込み
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        //$query .= "  T1.TESTKINDCD = T4.TESTKINDCD AND ";
        $query .= "  T1.YEAR = T2.YEAR AND ";
        $query .= "  T1.SEMESTER = T2.SEMESTER ";
        $query .= "ORDER BY ";
        $query .= "  T1.SEMESTER,T1.TESTKINDCD, T1.TESTITEMCD ";
        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  SEMESTER = '" .$model->semester ."' AND ";
        $query .= "  TESTKINDCD = '" .$model->testkindcd ."' AND ";
        $query .= "  TESTITEMCD = '" .$model->testitemcd ."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        // echo $query;
        return $row;
    }

    //テスト種別名の取得
    function getTestKindName($model)
    {
        $query   = " SELECT * FROM TESTKIND_MST ";
        $query  .= "ORDER BY INT(TESTKINDCD)";

        return $query;
    }

    //学期名の取得
    function getSemester($model)
    {
        $query   = "SELECT * FROM SEMESTER_MST ";
        $query  .= "WHERE YEAR = '" .CTRL_YEAR ."' ";
        $query  .= "ORDER BY SEMESTER";

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
        $query .= "     YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$Row["SEMESTER"]}' ";

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
        $query .= "     YEAR            = '" .CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER        = '{$Row["SEMESTER"]}' AND ";
        $query .= "     SEMESTER_DETAIL = '{$Row["SEMESTER_DETAIL"]}' ";

        return $query;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "";
        $query .= "INSERT INTO TESTITEM_MST_COUNTFLG_NEW( ";
        $query .= "YEAR, ";
        $query .= "SEMESTER, ";
        $query .= "TESTKINDCD, ";
        $query .= "TESTITEMCD, ";
        $query .= "TESTITEMNAME, ";
        $query .= "COUNTFLG, ";
        $query .= "SEMESTER_DETAIL, ";
        //テスト期間
        if ($model->Properties["Test_Period_Hyouji"] == "1") {
            $query .= "TEST_START_DATE, ";
            $query .= "TEST_END_DATE, ";
        }
        $query .= "REGISTERCD, ";
        $query .= "UPDATED ";
        $query .= ") ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      SEMESTER, ";
        $query .= "      TESTKINDCD, ";
        $query .= "      TESTITEMCD, ";
        $query .= "      TESTITEMNAME, ";
        $query .= "      COUNTFLG, ";
        $query .= "      SEMESTER_DETAIL, ";
        //テスト期間
        if ($model->Properties["Test_Period_Hyouji"] == "1") {
            $query .= "TEST_START_DATE, ";
            $query .= "TEST_END_DATE, ";
        }
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      TESTITEM_MST_COUNTFLG_NEW T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "      not exists ( ";
        $query .= "                  SELECT 'x' FROM TESTITEM_MST_COUNTFLG_NEW T1 ";
        $query .= "                  WHERE ";
        $query .= "                  T1.YEAR = '" . CTRL_YEAR ."' AND ";
        $query .= "                  T1.SEMESTER = T0.SEMESTER AND ";
        $query .= "                  T1.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                  T1.TESTITEMCD = T0.TESTITEMCD ";
        $query .= "                  ) ";
        //$query .= "  AND TESTKINDCD IN (SELECT TESTKINDCD FROM TESTKIND_MST) ";
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
        $query .= "SELECT ";
        $query .= "  COUNT(*) ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "  TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "  TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' ";
        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $sem_detail = ($model->Properties["Semester_Detail_Hyouji"] == "1") ? $model->field["SEMESTER_DETAIL"] : $db->getOne(knjz380aQuery::getSemDetail($model));

        $db->autoCommit(false);
        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '" .$model->field["SEMESTER"] ."' ";
        $query .= " AND TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' ";
        $query .= " AND TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' ";
        //echo $query;
        $db->query($query);

        $query = "";

        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]      = $model->field["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]      = $model->field["TESTITEMCD"];
        $data["TESTITEMNAME"][TEXT]    = $model->field["TESTITEMNAME"];
        $data["COUNTFLG"][TEXT]        = ($model->field["COUNTFLG"] == "1") ? "1" : "0" ;
        $data["SEMESTER_DETAIL"][TEXT] = $sem_detail;
        //テスト期間
        if ($model->Properties["Test_Period_Hyouji"] == "1") {
            $data["TEST_START_DATE"][DATE]  = $model->field["TEST_START_DATE"];
            $data["TEST_END_DATE"][DATE]    = $model->field["TEST_END_DATE"];
        }
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][NUMBER]       = "SYSDATE()";

        $query = Query::insertSQL($data, "testitem_mst_countflg_new");

        //echo "<br>".$query;
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '" .$model->semester ."' ";
        $query .= " AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= " AND TESTITEMCD = '" .$model->testitemcd ."' ";

        // echo $query;
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
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD  = '".$model->field["TESTKINDCD"]."' AND ";
        $query .= "     TESTITEMCD  = '".$model->field["TESTITEMCD"]."' ";

        return $query;
    }
}
?>
