<?php
class knjl606hQuery extends Query
{
    
    //名称マスタ取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
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
    public function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_CLASSIFY_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR <> '{$model->examyear}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examyear}', '{$model->examyear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //リスト
    public function getList($model)
    {
        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_CLASSIFY_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "  ORDER BY ";
        $query .= "      CLASSIFY_CD ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $div = "")
    {
        $classifyCd = ($div == "chk") ? $model->field["CLASSIFY_CD"] : $model->classifyCd;

        $query  = "  SELECT ";
        $query  .= "     CLASSIFY_CD, ";
        $query  .= "     CLASSIFY_NAME, ";
        $query  .= "     MARK, ";
        $query  .= "     PLUS_POINT, ";
        $query  .= "     COMMITMENT_FLG, ";
        $query  .= "     CALC_FLG, ";
        $query  .= "     ORDER ";
        $query  .= " FROM ";
        $query  .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "      AND CLASSIFY_CD  = '{$classifyCd}' ";

        return $query;
    }

    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckClassIfyMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_CLASSIFY_MST)
    public function &getCopyClassIfyMstQuery($db, $model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_CLASSIFY_MST ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     CLASSIFY_CD, ";
        $query .= "     CLASSIFY_NAME, ";
        $query .= "     MARK, ";
        $query .= "     PLUS_POINT, ";
        $query .= "     COMMITMENT_FLG, ";
        $query .= "     CALC_FLG, ";
        $query .= "     ORDER, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     CLASSIFY_CD, ";
        $query .= "     CLASSIFY_NAME, ";
        $query .= "     MARK, ";
        $query .= "     PLUS_POINT, ";
        $query .= "     COMMITMENT_FLG, ";
        $query .= "     CALC_FLG, ";
        $query .= "     ORDER, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->leftYear."' ";

        return $query;
    }

    /******** 追加更新処理  ********/
    //--- INSERT
    public function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $classifyCd = sprintf("%02d", $fields["CLASSIFY_CD"]);
        $plusPoint = ($fields["PLUS_POINT"]) ? $fields["PLUS_POINT"] : "0";
        $commiementFlg = ($fields["COMMITMENT_FLG"]) ? "1" : "0";
        $calcFlg = ($fields["CALC_FLG"]) ? "1" : "0";

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["CLASSIFY_CD"][TEXT]      = $classifyCd;
        $data["CLASSIFY_NAME"][TEXT]    = $fields["CLASSIFY_NAME"];
        $data["MARK"][TEXT]             = $fields["MARK"];
        $data["PLUS_POINT"][TEXT]       = $plusPoint;
        $data["COMMITMENT_FLG"][TEXT]   = $commiementFlg;
        $data["CALC_FLG"][TEXT]         = $calcFlg;
        $data["ORDER"][TEXT]            = $fields["ORDER"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_CLASSIFY_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $classifyCd = sprintf("%02d", $fields["CLASSIFY_CD"]);
        $plusPoint = ($fields["PLUS_POINT"]) ? $fields["PLUS_POINT"] : "0";
        $commiementFlg = ($fields["COMMITMENT_FLG"]) ? "1" : "0";
        $calcFlg = ($fields["CALC_FLG"]) ? "1" : "0";

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["CLASSIFY_CD"][TEXT]      = $classifyCd;
        $data["CLASSIFY_NAME"][TEXT]    = $fields["CLASSIFY_NAME"];
        $data["MARK"][TEXT]             = $fields["MARK"];
        $data["PLUS_POINT"][TEXT]       = $plusPoint;
        $data["COMMITMENT_FLG"][TEXT]   = $commiementFlg;
        $data["CALC_FLG"][TEXT]         = $calcFlg;
        $data["ORDER"][TEXT]            = $fields["ORDER"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND CLASSIFY_CD   = '{$classifyCd}' ";

        $query = Query::updateSQL($data, "ENTEXAM_CLASSIFY_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $classifyCd = sprintf("%02d", $fields["CLASSIFY_CD"]);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND CLASSIFY_CD   = '{$classifyCd}' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
