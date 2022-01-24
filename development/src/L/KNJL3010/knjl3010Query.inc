<?php
class knjl3010Query extends Query {
    
    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度コンボ
    function getYear($model) {
        $query  = "     SELECT DISTINCT ";
        $query .= "         ENTEXAMYEAR AS LABEL,";
        $query .= "         ENTEXAMYEAR AS VALUE";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_TESTDIV_MST ";
        $query .= "     ORDER BY ";
        $query .= "         ENTEXAMYEAR ";

        return $query;
    }

    //リスト
    function getList($model) {

        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "  ORDER BY ";
        $query .= "      TESTDIV ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $div = "") {
        $testdiv = ($div == "chk") ? $model->field["TESTDIV"] : $model->testdiv;

        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "      AND TESTDIV      = '{$testdiv}' ";

        return $query;
    }


/************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    function getCheckTestDivMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTDIV_MST)
    function &getCopyTestDivMstQuery($db, $model) {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_TESTDIV_MST(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,TESTDIV_NAME,TESTDIV_ABBV,CAPACITY,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV_ABBV, ";
        $query .= "     CAPACITY, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "        ON T2.YEAR    = {$nextYear} ";
        $query .= "       AND T2.NAMECD1 = 'L003' ";
        $query .= "       AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->leftYear."' ";

        return $query;
   }


/******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["CAPACITY"][TEXT]         = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $examhallcd = $model->groupdiv."0".$fields["GROUPCD"];

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["CAPACITY"][TEXT]         = $fields["CAPACITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_MST", $where);
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
