<?php
class knjlz05hQuery extends Query
{

    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2 = "")
    {
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
    function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_KINDDIV_MST ";
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
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.KINDDIV, ";
        $query .= "     T1.KINDDIV_NAME, ";
        $query .= "     T1.KINDDIV_ABBV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KINDDIV_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.KINDDIV ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $div = "")
    {
        $kinddiv = ($div == "chk") ? $model->field["KINDDIV"] : $model->kinddiv;

        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_KINDDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "      AND KINDDIV      = '{$kinddiv}' ";

        return $query;
    }


    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    function getCheckKindDivMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KINDDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_KINDDIV_MST)
    function &getCopyKindDivMstQuery($model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_KINDDIV_MST(ENTEXAMYEAR,APPLICANTDIV,KINDDIV,KINDDIV_NAME,KINDDIV_ABBV,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     KINDDIV, ";
        $query .= "     KINDDIV_NAME, ";
        $query .= "     KINDDIV_ABBV, ";
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KINDDIV_MST T1 ";
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
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["KINDDIV"][TEXT]          = $fields["KINDDIV"];
        $data["KINDDIV_NAME"][TEXT]     = $fields["KINDDIV_NAME"];
        $data["KINDDIV_ABBV"][TEXT]     = $fields["KINDDIV_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_KINDDIV_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["KINDDIV_NAME"][TEXT]     = $fields["KINDDIV_NAME"];
        $data["KINDDIV_ABBV"][TEXT]         = $fields["KINDDIV_ABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND KINDDIV       = '{$fields["KINDDIV"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_KINDDIV_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_KINDDIV_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND KINDDIV       = '{$fields["KINDDIV"]}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
