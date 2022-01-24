<?php

require_once('for_php7.php');

class knjl001aQuery extends Query
{

    //入試年度一覧取得
    public function selectYearQuery()
    {
        $query  = " WITH T_ALL (VALUE, LABEL) AS ( ";
        $query .= "      VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS VALUE, ";
        $query .= "     ENTEXAMYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $nameCd2 = "")
    {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //リスト取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST TEST ";
        $query .= " WHERE ";
        $query .= "         TEST.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND TEST.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     TEST.TESTDIV ";

        return $query;
    }

    //１レコード取得
    public function getRow($year, $applicantdiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$testdiv."' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    public function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["TESTDIV_NAME"][TEXT]             = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]             = $fields["TESTDIV_ABBV"];
        $data["TEST_DATE1"][TEXT]       = $fields["TEST_DATE1"];
        $data["PRETEST_FLG"][TEXT]      = $fields["PRETEST_FLG"];
        $data["DEFAULT_FLG"][TEXT]      = $fields["DEFAULT_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["TESTDIV_NAME"][TEXT]             = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]             = $fields["TESTDIV_ABBV"];
        $data["TEST_DATE1"][TEXT]       = $fields["TEST_DATE1"];
        $data["PRETEST_FLG"][TEXT]      = $fields["PRETEST_FLG"];
        $data["DEFAULT_FLG"][TEXT]      = $fields["DEFAULT_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND TESTDIV           = '".$fields["TESTDIV"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_TESTDIV_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND TESTDIV         = '".$fields["TESTDIV"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    /********************/
    /* 前年度コピー処理 */
    /********************/
    //ある年度のデータが存在するかチェック
    public function checkData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }
    //ある年度にデータが存在するのかチェック
    public function getCheckPerfectMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    public function getCopyQuery($thisYear, $nextYear)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV_ABBV, ";
        $query .= "     TEST_DATE1, ";
        $query .= "     PRETEST_FLG, ";
        $query .= "     DEFAULT_FLG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV_ABBV, ";
        $query .= "     TEST_DATE1, ";
        $query .= "     PRETEST_FLG, ";
        $query .= "     DEFAULT_FLG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
