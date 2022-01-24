<?php

require_once('for_php7.php');

class knjb0053Query extends Query {
    //年度を取得
    function getYear()
    {
        $query  = " SELECT ";
        $query .= "     '" .CTRL_YEAR ."' AS LABEL, ";
        $query .= "     '" .CTRL_YEAR ."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '" .(CTRL_YEAR + 1)."' AS LABEL, ";
        $query .= "     '" .(CTRL_YEAR + 1)."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
        
    //リスト内情報取得
    function getList($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  * ";
        $query .= "FROM ";
        $query .= "  STUDY_SELECT_DATE_YMST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .$model->year ."' ";
        $query .= "ORDER BY ";
        $query .= "  RIREKI_CODE ";

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
        $query .= "  STUDY_SELECT_DATE_YMST ";
        $query .= "WHERE ";
        $query .= "  YEAR = '" .$model->year ."' AND ";
        $query .= "  RIREKI_CODE = '" .$model->rirekiCode ."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //対象年度データチェック
    function getCountData($year)
    {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        $count = $db->getOne($query);
        
        Query::dbCheckIn($db);
       
        return $count;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "";
        $query .= "INSERT INTO STUDY_SELECT_DATE_YMST( ";
        $query .= "YEAR, ";
        $query .= "RIREKI_CODE, ";
        $query .= "SELECT_NAME, ";
        $query .= "SELECT_DATE, ";
        $query .= "REGISTERCD, ";
        $query .= "UPDATED ";
        $query .= ") ";
        $query .= " (SELECT ";
        $query .= "      '" . $model->year ."', ";
        $query .= "      RIREKI_CODE, ";
        $query .= "      SELECT_NAME, ";
        $query .= "      SELECT_DATE, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "  FROM ";
        $query .= "      STUDY_SELECT_DATE_YMST T0 ";
        $query .= "  WHERE YEAR = '" .($model->year - 1) ."' AND ";
        $query .= "      not exists ( ";
        $query .= "                  SELECT 'x' FROM STUDY_SELECT_DATE_YMST T1 ";
        $query .= "                  WHERE ";
        $query .= "                  T1.YEAR = '" . $model->year ."' AND ";
        $query .= "                  T1.RIREKI_CODE = T0.RIREKI_CODE ";
        $query .= "                  ) ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    // INSERT&UPDATE
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $query  = " DELETE FROM STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE YEAR = '" .$model->year."' ";
        $query .= " AND RIREKI_CODE = '" .$model->field["RIREKI_CODE"] ."' ";
        $db->query($query);

        $data["YEAR"][TEXT]            = $model->year;
        $data["RIREKI_CODE"][TEXT]     = $model->field["RIREKI_CODE"];
        $data["SELECT_NAME"][TEXT]     = $model->field["SELECT_NAME"];
        $data["SELECT_DATE"][TEXT]     = str_replace("/","-",$model->field["SELECT_DATE"]);
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][NUMBER]       = "SYSDATE()";

        $query = Query::insertSQL($data, "STUDY_SELECT_DATE_YMST");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE YEAR = '" .$model->year."' ";
        $query .= " AND RIREKI_CODE = '" .$model->rirekiCode ."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
?>
