<?php

require_once('for_php7.php');

class knjl508iQuery extends Query
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

    //校納金コード取得
    public function selectReductionSchoolMst($model)
    {
        $schoolcd = sprintf("%012d", SCHOOLCD);

        $query .= "    SELECT ";
        $query .= "        REDUCTION_DIV_CD AS VALUE, ";
        $query .= "        REDUCTION_DIV_CD || ':' || REDUCTION_DIV_NAME AS LABEL ";
        $query .= "    FROM ";
        $query .= "        REDUCTION_SCHOOL_MST ";
        $query .= "    WHERE ";
        $query .= "        SCHOOLCD         = '{$schoolcd}' ";
        $query .= "        AND SCHOOL_KIND  = 'H' "; //固定
        $query .= "        AND YEAR         = '{$model->examyear}' ";
        $query .= "    ORDER BY ";
        $query .= "        VALUE ";

        return $query;
    }

    //データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV      = '{$model->testdiv}' AND ";
        $query .= "     GENERAL_DIV  = '{$model->generalDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, 0) ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $generalCd)
    {
        $query  = " SELECT ";
        $query .= "     *, ";
        $query .= "     REMARK4 AS REDUCTION_DIV_CD ";
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
        $data["ENTEXAMYEAR"][TEXT]          = $model->year;
        $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
        $data["TESTDIV"][TEXT]              = $model->testdiv;
        $data["GENERAL_DIV"][TEXT]          = $model->generalDiv;
        $data["GENERAL_CD"][TEXT]           = $model->field["GENERAL_CD"];
        $data["GENERAL_NAME"][TEXT]         = $model->field["GENERAL_NAME"];
        $data["GENERAL_MARK"][TEXT]         = $model->field["GENERAL_MARK"];
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
        $data["REMARK4"][TEXT]              = $model->field["REDUCTION_DIV_CD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

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
        $data["GENERAL_MARK"][TEXT]  = $model->field["GENERAL_MARK"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REMARK4"][TEXT]       = $model->field["REDUCTION_DIV_CD"];
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
        $query .= "   AND GENERAL_DIV   = '{$model->generalDiv}' ";
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
