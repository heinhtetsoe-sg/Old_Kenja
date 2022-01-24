<?php

require_once('for_php7.php');

class knjz452Query extends Query {
/************ 左画面 ***************/
    //年度コンボ
    function getYear($model) {
        $query .= " WITH T_ALL (LABEL, VALUE) AS ( ";
        $query .= "      VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      LABEL, ";
        $query .= "      VALUE ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_TEST_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //資格コンボ
    function getQualifiedMst() {
        $query  = " SELECT DISTINCT ";
        $query .= "     QUALIFIED_CD AS VALUE, ";
        $query .= "     QUALIFIED_CD || ':' || QUALIFIED_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     MANAGEMENT_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     QUALIFIED_CD ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        if ($model->div == "1") {
            $query .= "     QUALIFIED_RESULT_MST ";
        } else {
            $query .= "     QUALIFIED_TEST_MST ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->leftYear."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= " ORDER BY ";
        if ($model->div == "1") {
            $query .= "     RESULT_LEVEL, ";
            $query .= "     RESULT_CD ";
        } else {
            $query .= "     TEST_LEVEL, ";
            $query .= "     TEST_CD ";
        }

        return $query;
    }

/************ 右画面 ***************/
    //１レコード取得
    function getRow($model, $Cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        if ($model->div == "1") {
            $query .= "     QUALIFIED_RESULT_MST ";
        } else {
            $query .= "     QUALIFIED_TEST_MST ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->leftYear."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        if ($model->div == "1") {
            $query .= "     AND RESULT_CD = '".$Cd."' ";
        } else {
            $query .= "     AND TEST_CD   = '".$Cd."' ";
        }

        return $query;
    }

    //NOT_PRINTがテーブルにあるかどうか取得
    function getSyscatColumns($div) {

        if ($div == "1") {
            $tableName = "QUALIFIED_RESULT_MST";
        } else {
            $tableName = "QUALIFIED_TEST_MST";
        }

        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }

    //名称取得
    function getNameMst($model, $nameCd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL  ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$nameCd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

/************ コピー ***************/
    //指定年度のQUALIFIED_RESULT_MSTデータを消去
    function &getCopyDelResaltMstQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->leftYear + 1)."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //対象年度からのコピー(QUALIFIED_RESULT_MST)
    function &getCopyResaltMstQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO QUALIFIED_RESULT_MST ";
        $query .= " SELECT ";
        $query .= "     '".($model->leftYear + 1)."' AS YEAR, ";
        $query .= "     QUALIFIED_CD, ";
        $query .= "     RESULT_CD, ";
        $query .= "     RESULT_NAME, ";
        $query .= "     RESULT_NAME_ABBV, ";
        $query .= "     CERT_FLG, ";
        $query .= "     LIMITED_PERIOD, ";
        $query .= "     RESULT_LEVEL, ";
        if (0 < $db->getOne(knjz452Query::getSyscatColumns("1"))) {
            $query .= "     NOT_PRINT, ";
            $query .= "     SCORE, ";
        }
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->leftYear."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //今年度のQUALIFIED_TEST_MSTデータを消去
    function &getCopyDelTestMstQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     QUALIFIED_TEST_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->leftYear + 1)."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー(QUALIFIED_TEST_MST)
    function &getCopyTestMstQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO QUALIFIED_TEST_MST ";
        $query .= " SELECT ";
        $query .= "     '".($model->leftYear + 1)."' AS YEAR, ";
        $query .= "     QUALIFIED_CD, ";
        $query .= "     TEST_CD, ";
        $query .= "     TEST_NAME, ";
        $query .= "     TEST_NAME_ABBV, ";
        $query .= "     PREREQ_RESALT_CD, ";
        $query .= "     TEST_FEE, ";
        $query .= "     TEST_LEVEL, ";
        if (0 < $db->getOne(knjz452Query::getSyscatColumns("2"))) {
            $query .= "     NOT_PRINT, ";
            $query .= "     SCORE, ";
        }
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_TEST_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->leftYear."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

/******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]             = $model->leftYear;
        $data["QUALIFIED_CD"][TEXT]     = $model->sikakuCd;
        if ($model->div == "1") {
            $data["RESULT_CD"][TEXT]         = $fields["RESULT_CD"];
            $data["RESULT_NAME"][TEXT]       = $fields["RESULT_NAME"];
            $data["RESULT_NAME_ABBV"][TEXT]  = $fields["RESULT_NAME_ABBV"];
            $data["CERT_FLG"][TEXT]          = ($fields["CERT_FLG"] == "1") ? "T": "F";
            $data["LIMITED_PERIOD"][NUMBER]  = $fields["LIMITED_PERIOD"];
            $data["RESULT_LEVEL"][NUMBER]    = $fields["RESULT_LEVEL"];
            $tableName = "QUALIFIED_RESULT_MST";
        } else {
            $data["TEST_CD"][TEXT]           = $fields["TEST_CD"];
            $data["TEST_NAME"][TEXT]         = $fields["TEST_NAME"];
            $data["TEST_NAME_ABBV"][TEXT]    = $fields["TEST_NAME_ABBV"];
            $data["PREREQ_RESALT_CD"][TEXT]  = $fields["PREREQ_RESALT_CD"];
            $data["TEST_FEE"][NUMBER]        = $fields["TEST_FEE"];
            $data["TEST_LEVEL"][NUMBER]      = $fields["TEST_LEVEL"];
            $tableName = "QUALIFIED_TEST_MST";
        }
        if ($model->cntNotPrintColumn > 0) {
            $data["NOT_PRINT"][TEXT]       = $fields["NOT_PRINT"];
            $data["SCORE"][NUMBER]         = $fields["SCORE"];
        }
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($model->div == "1") {
            $data["RESULT_CD"][TEXT]         = $fields["RESULT_CD"];
            $data["RESULT_NAME"][TEXT]       = $fields["RESULT_NAME"];
            $data["RESULT_NAME_ABBV"][TEXT]  = $fields["RESULT_NAME_ABBV"];
            $data["CERT_FLG"][TEXT]          = ($fields["CERT_FLG"] == "1") ? "T": "F";
            $data["LIMITED_PERIOD"][NUMBER]  = $fields["LIMITED_PERIOD"];
            $data["RESULT_LEVEL"][NUMBER]    = $fields["RESULT_LEVEL"];
            $tableName = "QUALIFIED_RESULT_MST";
        } else {
            $data["TEST_CD"][TEXT]           = $fields["TEST_CD"];
            $data["TEST_NAME"][TEXT]         = $fields["TEST_NAME"];
            $data["TEST_NAME_ABBV"][TEXT]    = $fields["TEST_NAME_ABBV"];
            $data["PREREQ_RESALT_CD"][TEXT]  = $fields["PREREQ_RESALT_CD"];
            $data["TEST_FEE"][NUMBER]        = $fields["TEST_FEE"];
            $data["TEST_LEVEL"][NUMBER]      = $fields["TEST_LEVEL"];
            $tableName = "QUALIFIED_TEST_MST";
        }
        if ($model->cntNotPrintColumn > 0) {
            $data["NOT_PRINT"][TEXT]       = $fields["NOT_PRINT"];
            $data["SCORE"][NUMBER]         = $fields["SCORE"];
        }
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "         YEAR         = '".$model->leftYear."' ";
        $where .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        if ($model->div == "1") {
            $where .= "    AND RESULT_CD = '" .$fields["RESULT_CD"]."' ";
        } else {
            $where .= "    AND TEST_CD   = '" .$fields["TEST_CD"]."' ";
        }

        $query = Query::updateSQL($data, $tableName, $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        if ($model->div == "1") {
            $query .= "     QUALIFIED_RESULT_MST ";
        } else {
            $query .= "     QUALIFIED_TEST_MST ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR         = '".$model->leftYear."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        if ($model->div == "1") {
            $query .= "    AND RESULT_CD = '".$fields["RESULT_CD"]."' ";
        } else {
            $query .= "    AND TEST_CD   = '".$fields["TEST_CD"]."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
