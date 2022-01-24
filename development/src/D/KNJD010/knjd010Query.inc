<?php

require_once('for_php7.php');

class knjd010Query extends Query {

    //リスト内情報取得
    function getListdata($model)
    {
        $query = "";
        $query .= "SELECT DISTINCT";
        $query .= "  SUBSTR(T1.SUBCLASSCD,1,2) AS SUBCD_HEAD, ";
        $query .= "  SUBSTR(T1.SUBCLASSCD,1,2) || ' ' || T2.CLASSNAME AS CLASS_SHOW, ";
        $query .= "  T1.SUBCLASSCD, ";
        $query .= "  T1.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS SUBCLASS_SHOW, ";
        $query .= "  T1.TESTKINDCD, ";
        $query .= "  T1.TESTKINDCD || ' ' || T4.TESTKINDNAME AS TESTKIND_SHOW, ";
        $query .= "  T1.TESTITEMCD, ";
        $query .= "  T1.TESTITEMNAME, ";
        $query .= "  T1.UPDATED ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST T1, ";
        $query .= "  V_CLASS_MST T2, ";
        $query .= "  V_SUBCLASS_MST T3, ";
        $query .= "  TESTKIND_MST T4 ";
        $query .= "WHERE ";
        //権限による絞込み
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  SUBSTR(T1.SUBCLASSCD,1,2) = T2.CLASSCD AND ";
        //教科がALLのとき条件はいらない
        if($model->classcd != "00"){
            $query .= "  T2.CLASSCD = '" .$model->classcd ."' AND ";
        }
        $query .= "  T1.SUBCLASSCD = T3.SUBCLASSCD AND ";
        $query .= "  T1.TESTKINDCD = T4.TESTKINDCD ";
        $query .= "ORDER BY ";
        $query .= "  T1.SUBCLASSCD " .$model->subclass_sort .", ";
        $query .= "  T1.TESTKINDCD " .$model->testkind_sort .", ";
        $query .= "  T1.TESTITEMCD ASC ";
        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= "SELECT ";
        $query .= "  YEAR, ";
        $query .= "  SUBCLASSCD, ";
        $query .= "  TESTKINDCD, ";
        $query .= "  TESTITEMCD, ";
        $query .= "  TESTITEMNAME, ";
        $query .= "  UPDATED ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  SUBCLASSCD = '" .$model->subclasscd ."' AND ";
        $query .= "  TESTKINDCD = '" .$model->testkindcd ."' AND ";
        $query .= "  TESTITEMCD = '" .$model->testitemcd ."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //科目名の取得
    function getSubclassName($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  SUBCLASSCD, ";
        $query .= "  SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "  V_SUBCLASS_MST ";
        $query .= "WHERE ";
        $query .= "YEAR ='".CTRL_YEAR."'";
        //教科がALLのとき条件はいらない
        if($model->classcd != "00"){
            $query .= " AND SUBSTR(SUBCLASSCD,1,2) = '" .$model->classcd ."' ";
        }
        $query .= "ORDER BY ";
        $query .= "  SUBCLASSCD ";
        // echo $query;
        return $query;
    }
    //科目名の取得
    function getTestKindName($model)
    {
        //権限による絞込み
        $query   = " SELECT * FROM TESTKIND_MST ";
        $query  .= $permission;
        $query  .= "ORDER BY INT(TESTKINDCD)";

        return $query;
    }

    //テスト項目追加区分の値を取得
    function getTestItemAddCd($model)
    {
        $db = Query::dbCheckOut();

        $selectdata = str_replace(",","','",$model->selectdata);

        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST T1, ";
        $query .= "  TESTKIND_MST T2 ";
        $query .= "WHERE ";
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.SUBCLASSCD IN ('" .$selectdata ."') AND ";
        $query .= "  T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "  T1.TESTKINDCD = T2.TESTKINDCD AND ";
        $query .= "  T2.TESTITEMADDCD = '1' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //テスト項目追加区分の値を取得
    function CheckTestItemAdd($model)
    {
        $db = Query::dbCheckOut();

        $selectdata = str_replace(",","','",$model->selectdata);

        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST T1 ";
        $query .= "WHERE ";
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.SUBCLASSCD IN ('" .$selectdata ."') AND ";
        $query .= "  T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND";
        $query .= "  T1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //テスト項目追加区分チェック
    function chkTestItemAdd($model)
    {

        $selectdata = str_replace(",","','",$model->selectdata);

        $db = Query::dbCheckOut();

        $query   = " SELECT COUNT(*) AS COUNT_VAL FROM TESTITEM_MST";
        $query  .= " WHERE YEAR = '";
        $query  .= CTRL_YEAR;
        $query  .= "' AND SUBCLASSCD IN ('";
        $query  .= $selectdata;
        $query  .= "') AND TESTKINDCD = '";
        $query  .= $model->field["TESTKINDCD"];
        $query  .= "' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return (($row["COUNT_VAL"] == 0)? false : true );

    }

    //前年度からのコピー
  function &getCopyQuery($model)
  {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

       $query = "";
       $query .= "INSERT INTO TESTITEM_MST( ";
       $query .= "YEAR, ";
       $query .= "SUBCLASSCD, ";
       $query .= "TESTKINDCD, ";
       $query .= "TESTITEMCD, ";
       $query .= "TESTITEMNAME, ";
       $query .= "REGISTERCD, ";
       $query .= "UPDATED ";
       $query .= ") ";
       $query .= " (SELECT ";
       $query .= "      '" . CTRL_YEAR ."', ";
       $query .= "      SUBCLASSCD, ";
       $query .= "      TESTKINDCD, ";
       $query .= "      TESTITEMCD, ";
       $query .= "      TESTITEMNAME, ";
       $query .= "      '".STAFFCD."', ";
       $query .= "      SYSDATE() ";
       $query .= "  FROM ";
       $query .= "      TESTITEM_MST T0 ";
       $query .= "  WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
       $query .= "      SUBSTR(T0.SUBCLASSCD,1,2) = '" .$model->classcd ."' AND ";
       $query .= "      not exists ( ";
       $query .= "                  SELECT 'x' FROM TESTITEM_MST T1 ";
       $query .= "                  WHERE ";
       $query .= "                  T1.YEAR = '" . CTRL_YEAR ."' AND ";
       $query .= "                  T1.SUBCLASSCD = T0.SUBCLASSCD AND ";
       $query .= "                  T1.TESTKINDCD = T0.TESTKINDCD AND ";
       $query .= "                  T1.TESTITEMCD = T0.TESTITEMCD ";
       $query .= "                  ) AND ";
       $query .= "      T0.SUBCLASSCD in ( SELECT SUBCLASSCD ";
       $query .= "                         FROM V_SUBCLASS_MST ";
       $query .= "                         WHERE ";
       $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
       $query .= "                         SUBSTR(T0.SUBCLASSCD,1,2) = '" .$model->classcd ."')";
       $query .= "                       ) ";
       $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
       return true;
   }

    //テスト項目マスタの各科目の行数を取得
    function getCountTestItem($model)
    {
        $db = Query::dbCheckOut();

        $selectdata = str_replace(",","','",$model->selectdata);

        $query = "";
        $query .= "SELECT ";
        $query .= "  SUBCLASSCD, ";
        $query .= "  COUNT(SUBCLASSCD) AS COUNT_VAL ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  SUBCLASSCD IN ('" .$selectdata ."') ";
        $query .= "GROUP BY ";
        $query .= "  SUBCLASSCD ";
        $query .= "HAVING ";
        $query .= "  COUNT(SUBCLASSCD) > 30 ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //成績項目ヘッダーのデータチェック
    function chkTestScoreData($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  TESTSCORE_HDAT T1,";
        $query .= "  CHAIR_DAT      T2 ";
        $query .= "WHERE ";
        $query .= "  T1.YEAR       = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.TESTKINDCD = '" .$model->testkindcd ."' AND ";
        $query .= "  T1.TESTITEMCD = '" .$model->testitemcd ."' AND ";
        $query .= "  T2.YEAR       = '" .CTRL_YEAR ."' AND ";
        $query .= "  T2.SEMESTER   = T1.SEMESTER AND ";
        $query .= "  T2.SUBCLASSCD = '" .$model->subclasscd ."' AND ";
        $query .= "  T2.CHAIRCD    = T1.CHAIRCD ";
        //echo $query;
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //INSERT時の重複チェック
    function chkTestItemMst($model)
    {
        $db = Query::dbCheckOut();
        
        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  TESTITEM_MST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .CTRL_YEAR."' AND ";
        $query .= "  SUBCLASSCD = '" .$model->selectdata. "' AND ";
        $query .= "  TESTKINDCD = '" .sprintf("%02d", $model->field["TESTKINDCD"]) ."' AND ";
        $query .= "  TESTITEMCD = '" .sprintf("%02d", $model->field["TESTITEMCD"]) ."' ";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $selectdata = str_replace(",","','",$model->selectdata);

        $db->autoCommit(false);
        $query  = " DELETE FROM TESTITEM_MST ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SUBCLASSCD IN ('" .$selectdata ."') ";
        $query .= " AND TESTKINDCD = '" .sprintf("%02d", $model->field["TESTKINDCD"]) ."' ";
        $query .= " AND TESTITEMCD = '" .sprintf("%02d", $model->field["TESTITEMCD"]) ."'";
        //echo $query;
        $db->query($query);
        
        $query = "";
        $query .= "INSERT INTO TESTITEM_MST( ";
        $query .= "YEAR, ";
        $query .= "SUBCLASSCD, ";
        $query .= "TESTKINDCD, ";
        $query .= "TESTITEMCD, ";
        $query .= "TESTITEMNAME, ";
        $query .= "REGISTERCD, ";
        $query .= "UPDATED ";
        $query .= ") ";
        $query .= " (SELECT ";
        $query .= "    '" .CTRL_YEAR ."', ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    '" .sprintf("%02d", $model->field["TESTKINDCD"]) ."', ";
        $query .= "    '" .sprintf("%02d", $model->field["TESTITEMCD"]) ."', ";
        $query .= "    '" .Query::addQuote($model->field["TESTITEMNAME"]) ."', ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    sysdate() ";
        $query .= "  FROM ";
        $query .= "    V_SUBCLASS_MST ";
        $query .= "  WHERE ";
        $query .= " YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SUBCLASSCD IN ('" .$selectdata ."') ";
        $query .= "  ) ";
        //echo "<br>".$query;
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $selectdata = str_replace(",","','",$model->selectdata);

        $query = "";
        $query .= "UPDATE ";
        $query .= "  TESTITEM_MST ";
        $query .= "SET ";
        $query .= "  TESTITEMNAME = '" .Query::addQuote($model->field["TESTITEMNAME"]) ."', ";
        $query .= "  REGISTERCD = '".STAFFCD."', UPDATED = sysdate() ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SUBCLASSCD IN ('" .$selectdata ."') ";
        $query .= " AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= " AND TESTITEMCD = '" .$model->testitemcd ."' ";
        // echo $query;
        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $selectdata = str_replace(",","','",$model->selectdata);

        $query  = " DELETE FROM TESTITEM_MST ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SUBCLASSCD IN ('" .$selectdata ."') ";
        $query .= " AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= " AND TESTITEMCD = '" .$model->testitemcd ."' AND ";
        $query .= " NOT EXISTS(SELECT SUBCLASSCD,TESTKINDCD,TESTITEMCD ";
        $query .= " FROM TESTSCORE_HDAT T1,CHAIR_DAT T2 WHERE ";
        $query .= " T1.YEAR       = '" .CTRL_YEAR ."' AND ";               //2004/05/11 Y.ARAKAKI
        $query .= " T1.TESTKINDCD = '" .$model->testkindcd ."' AND ";
        $query .= " T1.TESTITEMCD = '" .$model->testitemcd ."' AND ";
        $query .= " T2.YEAR       = '" .CTRL_YEAR ."' AND ";               //2004/05/11 Y.ARAKAKI
        $query .= " T2.SEMESTER   = T1.SEMESTER AND ";                     //2004/05/11 Y.ARAKAKI
        $query .= " SUBCLASSCD IN ('" .$selectdata ."') AND ";
        $query .= " T2.CHAIRCD    = T1.CHAIRCD )";

/*
        $query  = " DELETE FROM TESTITEM_MST ";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND SUBCLASSCD IN ('" .$selectdata ."') ";
        $query .= " AND TESTKINDCD = '" .$model->testkindcd ."' ";
        $query .= " AND TESTITEMCD = '" .$model->testitemcd ."' AND ";
        $query .= " NOT EXISTS(SELECT SUBCLASSCD,TESTKINDCD,TESTITEMCD ";
        $query .= " FROM TESTSCORE_HDAT T1,CHAIR_DAT T2 WHERE ";
        $query .= " T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= " SUBCLASSCD IN ('" .$selectdata ."') AND ";
        $query .= " TESTKINDCD = '" .$model->testkindcd ."' AND ";
        $query .= " TESTITEMCD = '" .$model->testitemcd ."')";
*/
        // echo $query;
        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //年度データの確認
    function IsExisting($classcd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from classyear_dat where classcd = '".$classcd."'";
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }
    }

    function getClassName($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  CLASSCD, ";
        $query .= "  CLASSNAME ";
        $query .= "FROM ";
        $query .= "  V_CLASS_MST ";
        $query .= "WHERE ";
        $query .= " YEAR = '" .CTRL_YEAR ."' AND ";
        //更新可
        if($model->sec_competence == DEF_UPDATABLE){
            //$query .= "  INTEGER(CLASSCD) < 50 ";
            $query .= "  INTEGER(CLASSCD) < 90 ";
         //制限つき更新
        }else if($model->sec_competence == DEF_UPDATE_RESTRICT){
            //$query .= "  INTEGER(CLASSCD) < 50 AND ";
            $query .= "  INTEGER(CLASSCD) < 90 AND ";
            $query .= "  CLASSCD IN (SELECT DISTINCT ";
            $query .= "                  SUBSTR(SUBCLASSCD,1,2) ";
            $query .= "              FROM ";
            $query .= "                  CHAIR_DAT W1 ";
            $query .= "              WHERE ";
            $query .= "                  W1.YEAR       = '" .CTRL_YEAR ."' AND ";
            $query .= "                  W1.SUBCLASSCD IS NOT NULL AND " ;
            $query .= "                  EXISTS (SELECT ";
            $query .= "                              'X' ";
            $query .= "                          FROM ";
            $query .= "                              CHAIR_STF_DAT W2";
            $query .= "                          WHERE ";
            $query .= "                              W2.YEAR     = W1.YEAR AND ";
            $query .= "                              W2.SEMESTER = W1.SEMESTER AND ";
            $query .= "                              W2.CHAIRCD  = W1.CHAIRCD AND ";
            $query .= "                              W2.STAFFCD  = '" .STAFFCD ."' ";
            $query .= "                         ) ";
            $query .= "             ) ";

        }else{
            $query .= "  CLASSCD IS NULL";
        }
        $query .= " ORDER BY ";
        $query .= "  CLASSCD ";
        return $query;
    }

}
?>
