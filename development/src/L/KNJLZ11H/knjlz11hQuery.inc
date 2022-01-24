<?php
class knjlz11hQuery extends Query {
    
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

    //ENTEXAM_SETTING_MST取得
    function getEntexamSettingMst($model, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE,";
        $query .= "     SEQ || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR    = '{$model->leftYear}' AND ";
        $query .= "     APPLICANTDIV   = '{$model->applicantdiv}' AND ";
        $query .= "     SETTING_CD = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "    AND SEQ = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getYear($model) {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_QUALIFIED_MST ";
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

    function getList($model) {
        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_QUALIFIED_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "      AND QUALIFIED_CD = '{$model->qualifiedCd}' ";
        $query .= "  ORDER BY ";
        $query .= "      QUALIFIED_JUDGE_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $div = "") {
        $qualifiedJudgeCd = ($div == "chk") ? $model->field["QUALIFIED_JUDGE_CD"] : $model->qualifiedJudgeCd;

        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_QUALIFIED_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR            = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV       = '{$model->applicantdiv}' ";
        $query .= "      AND QUALIFIED_CD       = '{$model->qualifiedCd}' ";
        $query .= "      AND QUALIFIED_JUDGE_CD = '{$qualifiedJudgeCd}' ";

        return $query;
    }


    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    function getCheckQualifiedMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_QUALIFIED_MST)
    function &getCopyQualifiedMstQuery($model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_QUALIFIED_MST(ENTEXAMYEAR,APPLICANTDIV,QUALIFIED_CD,QUALIFIED_JUDGE_CD,QUALIFIED_NAME,QUALIFIED_ABBV,PLUS_POINT,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     QUALIFIED_CD, ";
        $query .= "     QUALIFIED_JUDGE_CD, ";
        $query .= "     QUALIFIED_NAME, ";
        $query .= "     QUALIFIED_ABBV, ";
        $query .= "     PLUS_POINT, ";
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_QUALIFIED_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "        ON T2.YEAR    = {$nextYear} ";
        $query .= "       AND T2.NAMECD1 = 'L003' ";
        $query .= "       AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" . $model->leftYear . "' ";

        return $query;
    }


/******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]        = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]       = $model->applicantdiv;
        $data["QUALIFIED_CD"][TEXT]       = $model->qualifiedCd;
        $data["QUALIFIED_JUDGE_CD"][TEXT] = $fields["QUALIFIED_JUDGE_CD"];
        $data["QUALIFIED_NAME"][TEXT]     = $fields["QUALIFIED_NAME"];
        $data["QUALIFIED_ABBV"][TEXT]     = $fields["QUALIFIED_ABBV"];
        $data["PLUS_POINT"][NUMBER]         = $fields["PLUS_POINT"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_QUALIFIED_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["QUALIFIED_NAME"][TEXT]     = $fields["QUALIFIED_NAME"];
        $data["QUALIFIED_ABBV"][TEXT]     = $fields["QUALIFIED_ABBV"];
        $data["PLUS_POINT"][NUMBER]         = $fields["PLUS_POINT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "   ENTEXAMYEAR            = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV       = '{$model->applicantdiv}' ";
        $where .= "   AND QUALIFIED_CD       = '{$model->qualifiedCd}' ";
        $where .= "   AND QUALIFIED_JUDGE_CD = '{$fields["QUALIFIED_JUDGE_CD"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_QUALIFIED_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR            = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV       = '{$model->applicantdiv}' ";
        $query .= "   AND QUALIFIED_CD       = '{$model->qualifiedCd}' ";
        $query .= "   AND QUALIFIED_JUDGE_CD = '{$fields["QUALIFIED_JUDGE_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
