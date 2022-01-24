<?php
class knjlz01hQuery extends Query {
    
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
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR <> '{$model->examYear}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examYear}', '{$model->examYear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //リスト
    function getList($model) {

        $query  = "  SELECT ";
        $query .= "    T1.*, ";
        $query .= "    T2.REMARK5 AS CAPACITY_MALE, ";
        $query .= "    T2.REMARK6 AS CAPACITY_FEMALE ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST T1 ";
        $query .= "      LEFT JOIN ENTEXAM_TESTDIV_DETAIL_MST T2 ";
        $query .= "           ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "  WHERE ";
        $query .= "      T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "  ORDER BY ";
        $query .= "      T1.TESTDIV ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $div = "") {
        $testdiv = ($div == "chk") ? $model->field["TESTDIV"] : $model->testdiv;

        $query  = "  SELECT ";
        $query .= "    T1.*, ";
        $query .= "    T2.REMARK2 AS CONDITION_HYOTEI, ";
        $query .= "    T2.REMARK4 AS CONDITION_GOKYOKA, ";
        $query .= "    T2.REMARK5 AS CAPACITY_MALE, ";
        $query .= "    T2.REMARK6 AS CAPACITY_FEMALE ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST T1 ";
        $query .= "      LEFT JOIN ENTEXAM_TESTDIV_DETAIL_MST T2 ";
        $query .= "           ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "  WHERE ";
        $query .= "      T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "      AND T1.TESTDIV      = '{$testdiv}' ";

        return $query;
    }

    //データカウント(ENTEXAM_TESTDIV_DETAIL_MST)
    function getCountDetaildata($model, $testdiv) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_MST ";
        $query .= " WHERE ";
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

    //ある年度にデータが存在するのかチェック(ENTEXAM_TESTDIV_DETAIL_MST)
    function getCheckTestDivDetailMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTDIV_MST)
    function &getCopyTestDivMstQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $nextYear = $model->leftYear + 1;

        //ENTEXAM_TESTDIV_MST
        $query  = " INSERT INTO ENTEXAM_TESTDIV_MST(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,TESTDIV_NAME,TESTDIV_ABBV,CAPACITY,TEST_DATE,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV_ABBV, ";
        $query .= "     CAPACITY, ";
        $query .= "     TEST_DATE, ";
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
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_MST
        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = {$nextYear} ";
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_MST
        //INSERT
        $query  = " INSERT INTO ENTEXAM_TESTDIV_DETAIL_MST(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,REMARK2,REMARK4,REMARK5,REMARK6,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "        ON T2.YEAR    = {$nextYear} ";
        $query .= "       AND T2.NAMECD1 = 'L003' ";
        $query .= "       AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->leftYear."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }


/******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_TESTDIV_MST
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        // 募集人数 男子＋女子
        $data["CAPACITY"][TEXT]         = (int)$fields["CAPACITY_MALE"] + (int)$fields["CAPACITY_FEMALE"];
        $data["TEST_DATE"][TEXT]        = str_replace('/', '-', $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_MST");
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_MST
        $query = knjlz01hQuery::getDetailUpdateQuery($db, $model, $fields);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_TESTDIV_MST
        $data = array();
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        // 募集人数 男子＋女子
        $data["CAPACITY"][TEXT]         = (int)$fields["CAPACITY_MALE"] + (int)$fields["CAPACITY_FEMALE"];
        $data["TEST_DATE"][TEXT]        = str_replace('/', '-', $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_MST", $where);
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_MST
        $query = knjlz01hQuery::getDetailUpdateQuery($db, $model, $fields);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function &getDetailUpdateQuery($db, $model, $fields) {
        //ENTEXAM_TESTDIV_DETAIL_MST
        //cnt
        $getCount = $db->getOne(knjlz01hQuery::getCountDetaildata($model, $fields["TESTDIV"]));

        $data = array();
        $data["REMARK2"][TEXT]          = $fields["CONDITION_HYOTEI"];
        $data["REMARK4"][TEXT]          = $fields["CONDITION_GOKYOKA"];
        $data["REMARK5"][TEXT]          = $fields["CAPACITY_MALE"];
        $data["REMARK6"][TEXT]          = $fields["CAPACITY_FEMALE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]       = $model->leftYear;
            $data["APPLICANTDIV"][TEXT]      = $model->applicantdiv;
            $data["TESTDIV"][TEXT]           = $fields["TESTDIV"];
            $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_DETAIL_MST");
        } else {
            $where  = " WHERE ";
            $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";
            $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_DETAIL_MST", $where);
        }
        return $query;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_TESTDIV_MST
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";
        $db->query($query);

        //ENTEXAM_TESTDIV_DETAIL_MST
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTDIV_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
