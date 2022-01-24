<?php

require_once('for_php7.php');

class knjl004aQuery extends Query
{

    //入試年度一覧取得
    function selectYearQuery()
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
        $query .= "     ENTEXAM_TRANSFER_FORMS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1, $nameCd2 = "")
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
    function selectQuery($model)
    {
        $query   = " SELECT ";
        $query  .= "     * ";
        $query  .= " FROM ";
        $query  .= "   ENTEXAM_TRANSFER_FORMS_MST ";
        $query  .= " WHERE ";
        $query  .= "        ENTEXAMYEAR  = '{$model->year}' ";
        $query  .= "    AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query  .= " ORDER BY ";
        $query  .= "     PATTERN_CD, ";
        $query  .= "     TOKUTAI_SELECT ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $patternCd, $tokutaiSelect)
    {
        $query   = " SELECT ";
        $query  .= "   * ";
        $query  .= " FROM ";
        $query  .= "   ENTEXAM_TRANSFER_FORMS_MST ";
        $query  .= " WHERE ";
        $query  .= "        ENTEXAMYEAR  = '".$year."' ";
        $query  .= "    AND APPLICANTDIV = '".$applicantdiv."' ";
        $query  .= "    AND PATTERN_CD   = '".$patternCd."' ";
        if ($patternCd == "001") {
            $query  .= "         AND TOKUTAI_SELECT  = '".$tokutaiSelect."'";
        } else {
            $query  .= "         AND TOKUTAI_SELECT  = '2'";
        }

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["PATTERN_CD"][TEXT]       = $fields["PATTERN_CD"];
        if ($fields["PATTERN_CD"] == "001") {
            $data["TOKUTAI_SELECT"][TEXT]   = $fields["TOKUTAI_SELECT"];
        } else {
            $data["TOKUTAI_SELECT"][TEXT]   = "2";
        }
        $data["PATTERN_NAME"][TEXT]     = $fields["PATTERN_NAME"];
        $data["PREF_NAME"][TEXT]        = $fields["PREF_NAME"];
        $data["ACCOUNT_NUMBER1"][TEXT]  = $fields["ACCOUNT_NUMBER1"];
        $data["ACCOUNT_NUMBER2"][TEXT]  = $fields["ACCOUNT_NUMBER2"];
        $data["ACCOUNT_NUMBER3"][TEXT]  = $fields["ACCOUNT_NUMBER3"];
        $data["MEMBER"][TEXT]           = $fields["MEMBER"];
        $data["SUBJECT"][TEXT]          = $fields["SUBJECT"];
        $data["COMMUNICATION"][TEXT]    = $fields["COMMUNICATION"];
        $data["TRANSFER_MONEY"][TEXT]   = $fields["TRANSFER_MONEY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TRANSFER_FORMS_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["PATTERN_NAME"][TEXT]     = $fields["PATTERN_NAME"];
        $data["PREF_NAME"][TEXT]        = $fields["PREF_NAME"];
        $data["ACCOUNT_NUMBER1"][TEXT]  = $fields["ACCOUNT_NUMBER1"];
        $data["ACCOUNT_NUMBER2"][TEXT]  = $fields["ACCOUNT_NUMBER2"];
        $data["ACCOUNT_NUMBER3"][TEXT]  = $fields["ACCOUNT_NUMBER3"];
        $data["MEMBER"][TEXT]           = $fields["MEMBER"];
        $data["SUBJECT"][TEXT]          = $fields["SUBJECT"];
        $data["COMMUNICATION"][TEXT]    = $fields["COMMUNICATION"];
        $data["TRANSFER_MONEY"][TEXT]   = $fields["TRANSFER_MONEY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND PATTERN_CD        = '".$fields["PATTERN_CD"]."' ";
        if ($fields["PATTERN_CD"] == "001") {
            $where .= "         AND TOKUTAI_SELECT  = '".$fields["TOKUTAI_SELECT"]."'";
        } else {
            $where .= "         AND TOKUTAI_SELECT  = '2'";
        }

        $query = Query::updateSQL($data, "ENTEXAM_TRANSFER_FORMS_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_TRANSFER_FORMS_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND PATTERN_CD      = '".$fields["PATTERN_CD"]."'";
        if ($fields["PATTERN_CD"] == "001") {
            $query .= "         AND TOKUTAI_SELECT  = '".$fields["TOKUTAI_SELECT"]."'";
        } else {
            $query .= "         AND TOKUTAI_SELECT  = '2'";
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    /********************/
    /* 前年度コピー処理 */
    /********************/
    //ある年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TRANSFER_FORMS_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }
    //ある年度にデータが存在するのかチェック
    function getCheckPerfectMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TRANSFER_FORMS_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_TRANSFER_FORMS_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     PATTERN_CD, ";
        $query .= "     TOKUTAI_SELECT, ";
        $query .= "     PATTERN_NAME, ";
        $query .= "     PREF_NAME, ";
        $query .= "     ACCOUNT_NUMBER1, ";
        $query .= "     ACCOUNT_NUMBER2, ";
        $query .= "     ACCOUNT_NUMBER3, ";
        $query .= "     MEMBER, ";
        $query .= "     SUBJECT, ";
        $query .= "     COMMUNICATION, ";
        $query .= "     TRANSFER_MONEY, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     PATTERN_CD, ";
        $query .= "     TOKUTAI_SELECT, ";
        $query .= "     PATTERN_NAME, ";
        $query .= "     PREF_NAME, ";
        $query .= "     ACCOUNT_NUMBER1, ";
        $query .= "     ACCOUNT_NUMBER2, ";
        $query .= "     ACCOUNT_NUMBER3, ";
        $query .= "     MEMBER, ";
        $query .= "     SUBJECT, ";
        $query .= "     COMMUNICATION, ";
        $query .= "     TRANSFER_MONEY, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_TRANSFER_FORMS_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
