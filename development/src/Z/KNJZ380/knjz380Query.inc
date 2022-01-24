<?php

require_once('for_php7.php');

class knjz380Query extends Query {

    //リスト内情報取得
    function getListdata($model)
    {
        $query = "";
        $query .= "SELECT DISTINCT";
        $query .= "  T1.TESTKINDCD, ";
        $query .= "  T1.TESTKINDCD || ' ' || T4.TESTKINDNAME AS TESTKIND_SHOW, ";
        $query .= "  T1.TESTITEMCD, ";
        $query .= "  T1.TESTITEMNAME, ";
        $query .= "  T1.COUNTFLG, ";
        $query .= "  CASE WHEN T1.COUNTFLG = '1' THEN '集計する' ";
        $query .= "       WHEN T1.COUNTFLG = '0' THEN '集計しない' ";
        $query .= "       ELSE NULL END AS COUNTFLG_SHOW, ";
        $query .= "  T1.UPDATED ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG T1, ";
        $query .= "  TESTKIND_MST T4 ";
        $query .= "WHERE ";
        //権限による絞込み
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.TESTKINDCD = T4.TESTKINDCD ";
        $query .= "ORDER BY ";
        $query .= "  T1.TESTKINDCD, T1.TESTITEMCD ";
        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= "SELECT ";
        $query .= "  YEAR, ";
        $query .= "  TESTKINDCD, ";
        $query .= "  TESTITEMCD, ";
        $query .= "  TESTITEMNAME, ";
        $query .= "  COUNTFLG, ";
        $query .= "  UPDATED ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST_COUNTFLG ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  TESTKINDCD = '" .$model->testkindcd ."' AND ";
        $query .= "  TESTITEMCD = '" .$model->testitemcd ."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //テスト種別名の取得
    function getTestKindName($model)
    {
        $query   = " SELECT * FROM TESTKIND_MST ";
        $query  .= "ORDER BY INT(TESTKINDCD)";

        return $query;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "";
        $query .= "INSERT INTO TESTITEM_MST_COUNTFLG( ";
        $query .= "YEAR, ";
        $query .= "TESTKINDCD, ";
        $query .= "TESTITEMCD, ";
        $query .= "TESTITEMNAME, ";
        $query .= "COUNTFLG, ";
        $query .= "REGISTERCD, ";
        $query .= "UPDATED ";
        $query .= ") ";
        $query .= " (SELECT ";
        $query .= "      '" . CTRL_YEAR ."', ";
        $query .= "      TESTKINDCD, ";
        $query .= "      TESTITEMCD, ";
        $query .= "      TESTITEMNAME, ";
        $query .= "      COUNTFLG, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      TESTITEM_MST_COUNTFLG T0 ";
        $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "      not exists ( ";
        $query .= "                  SELECT 'x' FROM TESTITEM_MST_COUNTFLG T1 ";
        $query .= "                  WHERE ";
        $query .= "                  T1.YEAR = '" . CTRL_YEAR ."' AND ";
        $query .= "                  T1.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                  T1.TESTITEMCD = T0.TESTITEMCD ";
        $query .= "                  ) AND ";
        $query .= "      TESTKINDCD IN (SELECT TESTKINDCD FROM TESTKIND_MST) ";
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
        $query .= "  TESTITEM_MST_COUNTFLG ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
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

        $db->autoCommit(false);
        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' ";
        $query .= " AND TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' ";
        //echo $query;
        $db->query($query);
        
        $query = "";

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["TESTKINDCD"][TEXT]   = $model->field["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]   = $model->field["TESTITEMCD"];
        $data["TESTITEMNAME"][TEXT] = $model->field["TESTITEMNAME"];
        $data["COUNTFLG"][TEXT]     = ($model->field["COUNTFLG"] == "1") ? "1" : "0" ;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "testitem_mst_countflg");

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

        $query  = " DELETE FROM TESTITEM_MST_COUNTFLG ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= " AND TESTITEMCD = '" .$model->testitemcd ."' ";

        // echo $query;
        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }



}
?>
