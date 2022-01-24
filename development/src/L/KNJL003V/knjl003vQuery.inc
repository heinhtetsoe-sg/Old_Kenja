<?php

require_once('for_php7.php');

class knjl003vQuery extends Query
{
    //年度コンボ
    public function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      YEAR AS LABEL,";
        $query .= "      YEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_STD_APPLICANT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR <> '{$model->examyear}' ";
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

    //データ取得
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     PLACE_ID, ";
        $query .= "     PLACE_NAME, ";
        $query .= "     SEATS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_HALL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$model->leftYear."' ";
        $query .= " ORDER BY ";
        $query .= "     PLACE_ID ";

        return $query;
    }

    //データ取得 -- ENTEXAM_STD_HALL_MST
    public function getRow($model, $div = "")
    {
        $query  = " SELECT ";
        $query .= "     PLACE_ID, ";
        $query .= "     PLACE_NAME, ";
        $query .= "     SEATS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_HALL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->leftYear."' ";
        if ($div == "chk") {
            $query .= "     AND PLACE_ID = '".$model->field["PLACE_ID"]."' ";
        } else {
            $query .= "     AND PLACE_ID = '".$model->placeId."' ";
        }

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        //追加 -- ENTEXAM_STD_HALL_MST
        $data = array();
        $data["YEAR"][TEXT]             = $model->leftYear;
        $data["PLACE_ID"][TEXT]         = $fields["PLACE_ID"];
        $data["PLACE_NAME"][TEXT]       = $fields["PLACE_NAME"];
        $data["SEATS"][TEXT]            = $fields["SEATS"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_STD_HALL_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        //更新 -- ENTEXAM_STD_HALL_MST
        $data = array();
        $data["PLACE_NAME"][TEXT]            = $fields["PLACE_NAME"];
        $data["SEATS"][TEXT]                 = $fields["SEATS"];
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][FUNC]               = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR                 = '".$model->leftYear."' ";
        $where .= "     AND PLACE_ID         = '".$fields["PLACE_ID"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_STD_HALL_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_STD_HALL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '".$model->leftYear."' ";
        $query .= "     AND PLACE_ID         = '".$fields["PLACE_ID"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    // //ある年度にデータが存在するのかチェック
    public function getCheckHallMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_HALL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_STD_HALL_MST)
    public function getCopyQuery($model, $prevYear)
    {
        //ENTEXAM_HALL_YDAT
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_STD_HALL_MST ";
        $query .= "         (  ";
        $query .= "          YEAR ";
        $query .= "          , PLACE_ID ";
        $query .= "          , PLACE_NAME ";
        $query .= "          , SEATS ";
        $query .= "          , REGISTERCD ";
        $query .= "          , UPDATED ";
        $query .= "         ) ";
        $query .= "     ( ";
        $query .= "      SELECT ";
        $query .= "          '".$model->leftYear."', ";
        $query .= "          PLACE_ID, ";
        $query .= "          PLACE_NAME, ";
        $query .= "          SEATS, ";
        $query .= "          '".STAFFCD."', ";
        $query .= "          sysdate() ";
        $query .= "      FROM";
        $query .= "          ENTEXAM_STD_HALL_MST ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '{$prevYear}' ";
        $query .= "     ) ";

        return $query;
    }
}
