<?php
class knjlz04iQuery extends Query
{
    
    //名称マスタ取得
    public function getNameMst($year, $namecd1, $namecd2="")
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
        $query .= "      ENTEXAM_EXAMNO_DAT ";
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

    //入試区分取得
    public function getTestDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL,  ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->leftYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV || ':' || value(T2.TESTDIV_NAME, '') AS TESTDIV_NAME, ";
        $query .= "     T1.EXAMNO_FROM, ";
        $query .= "     T1.EXAMNO_TO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMNO_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_TESTDIV_MST T2 ";
        $query .= "         ON T2.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TESTDIV ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $div = "")
    {
        $testdiv = ($div == "chk") ? $model->field["TESTDIV"] : $model->testdiv;

        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMNO_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_TESTDIV_MST T2 ";
        $query .= "         ON T2.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV         = '{$testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '1' ";

        return $query;
    }

    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckExamnoDatSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMNO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$year}' ";
        $query .= "     AND EXAM_TYPE    = '1' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_EXAMNO_DAT)
    public function &getCopyExamnoDatQuery($db, $model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_EXAMNO_DAT ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.EXAMNO_FROM, ";
        $query .= "     T1.EXAMNO_TO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMNO_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_TESTDIV_MST T2 ";
        $query .= "         ON T2.ENTEXAMYEAR   = {$nextYear} "; //次年度に入試区分設定がされているものだけコピー
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND T1.EXAM_TYPE    = '1' ";

        return $query;
    }

    /******** 追加更新処理  ********/
    //--- INSERT
    public function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["EXAMNO_FROM"][TEXT]      = $fields["EXAMNO_FROM"];
        $data["EXAMNO_TO"][TEXT]        = $fields["EXAMNO_TO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_EXAMNO_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["EXAMNO_FROM"][TEXT]      = $fields["EXAMNO_FROM"];
        $data["EXAMNO_TO"][TEXT]        = $fields["EXAMNO_TO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV       = '{$model->testdiv}' ";

        $query = Query::updateSQL($data, "ENTEXAM_EXAMNO_DAT", $where);
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_EXAMNO_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR           = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV      = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV           = '{$model->testdiv}' ";
        $query .= "   AND EXAM_TYPE         = '1' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
