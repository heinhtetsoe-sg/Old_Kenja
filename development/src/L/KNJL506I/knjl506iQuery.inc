<?php

require_once('for_php7.php');

class knjl506iQuery extends Query
{
    //入試年度一覧取得
    public function selectYearQuery($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_GENERAL_MST ";
        $query .= "  WHERE ";
        $query .= "     GENERAL_DIV  = '{$model->generalDiv}' ";
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

    //データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     M1.GENERAL_NAME AS REMARK1_GENERAL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST T1 ";
        $query .= " LEFT JOIN ENTEXAM_GENERAL_MST M1 ";
        $query .= "     ON T1.ENTEXAMYEAR = M1.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = M1.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = M1.TESTDIV ";
        $query .= "     AND T1.REMARK1 = M1.GENERAL_CD ";
        $query .= "     AND M1.GENERAL_DIV = '01' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$model->testdiv}' AND ";
        $query .= "     T1.GENERAL_DIV  = '{$model->generalDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE(T1.GENERAL_CD, 0) ";

        return $query;
    }

    //類別一覧取得
    public function getRemark1($model)
    {
        $query .= "  SELECT  ";
        $query .= "      GENERAL_CD || ':' ||GENERAL_NAME AS LABEL,";
        $query .= "      GENERAL_CD AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_GENERAL_MST ";
        $query .= "  WHERE ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "    AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "    AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "    AND GENERAL_DIV  = '01' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE(GENERAL_CD, 0) ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $generalCd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->year}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "   AND GENERAL_DIV   = '{$model->generalDiv}' ";
        $query .= "   AND GENERAL_CD    = '{$generalCd}' ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["TESTDIV"][TEXT]       = $model->testdiv;
        $data["GENERAL_DIV"][TEXT]   = $model->generalDiv;
        $data["GENERAL_CD"][TEXT]    = $model->field["GENERAL_CD"];
        $data["GENERAL_NAME"][TEXT]  = $model->field["GENERAL_NAME"];
        $data["GENERAL_ABBV"][TEXT]  = $model->field["GENERAL_ABBV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]       = $model->field["REMARK3"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_GENERAL_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["GENERAL_NAME"][TEXT]  = $model->field["GENERAL_NAME"];
        $data["GENERAL_ABBV"][TEXT]  = $model->field["GENERAL_ABBV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]       = $model->field["REMARK3"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
        $where .= "   AND GENERAL_DIV   = '{$model->generalDiv}' ";
        $where .= "   AND GENERAL_CD    = '{$model->field["GENERAL_CD"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_GENERAL_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->year}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
        $where .= "   AND GENERAL_DIV   = '{$model->generalDiv}' ";
        $query .= "   AND GENERAL_CD    = '{$model->field["GENERAL_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }

    //ある年度にデータが存在するのかチェック
    public function getCheckGeneralMstSql($year, $generalDiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ENTEXAMYEAR  = '{$year}' ";
        $query .= "   AND GENERAL_DIV  = '{$generalDiv}' ";

        return $query;
    }

    //COPY
    public function getCopyQuery($model, $nextYear)
    {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     GENERAL_DIV, ";
        $query .= "     GENERAL_CD, ";
        $query .= "     GENERAL_NAME, ";
        $query .= "     GENERAL_ABBV, ";
        $query .= "     GENERAL_MARK,";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     REMARK7, ";
        $query .= "     REMARK8, ";
        $query .= "     REMARK9, ";
        $query .= "     REMARK10, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->year}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "   AND GENERAL_DIV   = '{$model->generalDiv}' ";

        return $query;
    }
}
