<?php
class knjl401hQuery extends Query
{
    //入試年度一覧取得
    public function selectYearQuery($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_EXAMTYPE_MST ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examyear}', '{$model->examyear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    public function deleteAtExist($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '".$model->year."' AND ";
        $query .= "     TESTDIV      = '1' ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $default = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        if ($default) {
            $query .= "    ,NAMESPARE2 AS DEFAULT ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     SETTING_CD   = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //名称マスタよりNAMESPARE2取得(初期値を取得できていない場合)
    public function getNameSpare2($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMESPARE2  = '1' ";

        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    public function getNameMstSet($model, $namecd1, $namespare2)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namespare2) {
            $query .= " AND NAMESPARE2 = '".$namespare2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAM_TYPE ";

        return $query;
    }

    //１レコード取得
    public function getRow($year, $applicantdiv, $exam_type)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.REMARK1 AS TEST_DIV, ";
        $query .= "     T1.REMARK2 AS MAPPING_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$exam_type."' ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]        = $model->year;
        $data["APPLICANTDIV"][TEXT]       = $model->applicantdiv;
        $data["EXAM_TYPE"][TEXT]          = sprintf("%02d", $model->field["EXAM_TYPE"]);
        $data["EXAMTYPE_NAME"][TEXT]      = $model->field["EXAMTYPE_NAME"];
        $data["EXAMTYPE_NAME_ABBV"][TEXT] = "　";
        $data["REMARK1"][TEXT]            = $model->field["TEST_DIV"];
        $data["REMARK2"][TEXT]            = $model->field["MAPPING_NAME"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_EXAMTYPE_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]        = $model->year;
        $data["APPLICANTDIV"][TEXT]       = $model->applicantdiv;
        $data["EXAM_TYPE"][TEXT]          = sprintf("%02d", $model->field["EXAM_TYPE"]);
        $data["EXAMTYPE_NAME"][TEXT]      = $model->field["EXAMTYPE_NAME"];
        $data["EXAMTYPE_NAME_ABBV"][TEXT] = "　";
        $data["REMARK1"][TEXT]            = $model->field["TEST_DIV"];
        $data["REMARK2"][TEXT]            = $model->field["MAPPING_NAME"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV      = '".$model->applicantdiv."' ";
        $where .= "   AND EXAM_TYPE         = '".$model->field["EXAM_TYPE"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_EXAMTYPE_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_EXAMTYPE_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$model->year."' ";
        $query .= "         AND APPLICANTDIV    = '".$model->applicantdiv."' ";
        $query .= "         AND EXAM_TYPE       = '".$model->field["EXAM_TYPE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //ある年度にデータが存在するのかチェック
    public function getCheckExamTypeMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    public function getCopyQuery($thisYear, $nextYear)
    {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     EXAMTYPE_NAME, ";
        $query .= "     EXAMTYPE_NAME_ABBV, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate(), ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2 ";
        $query .= " FROM";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        return $query;
    }
}
