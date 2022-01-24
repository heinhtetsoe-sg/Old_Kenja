<?php

require_once('for_php7.php');

class knjl503iQuery extends Query
{
    //年度コンボ
    public function getYearQuery($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR <> '{$model->leftYear}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->leftYear}', '{$model->leftYear}')";
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

    //試験科目
    public function getTestSubclassCdQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND SETTING_CD   = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //指定年度における科目と満点数の一覧を取得
    //※入試制度と入試区分は固定
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.TESTSUBCLASSCD || ':' || T2.NAME1 AS TESTSUBCLASSNAME, ";
        $query .= "     T1.PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ";
        $query .= " INNER JOIN ENTEXAM_SETTING_MST T2 ";
        $query .= "     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T2.SETTING_CD = 'L009' ";
        $query .= "     AND T2.SEQ = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.COURSECD     = '0' ";
        $query .= "     AND T1.MAJORCD      = '000' ";
        $query .= "     AND T1.EXAMCOURSECD = '0000' ";
        $query .= "     AND T1.EXAM_TYPE    = '0' ";
        $query .= "     AND T2.NAME1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.TESTSUBCLASSCD ";

        return $query;
    }

    //指定年度と試験科目における満点数を含むマスタレコード取得クエリを取得
    public function getRowQuery($model, $div = "")
    {
        $testSubCd = ($div == "chk") ? $model->field["TESTSUBCLASSCD"] : $model->testSubCd;

        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR           = '{$model->leftYear}' ";    // 年度
        $query .= "      AND APPLICANTDIV      = '{$model->applicantdiv}' ";// 入試制度
        $query .= "      AND TESTDIV           = '{$model->testdiv}' ";     // 入試区分
        $query .= "      AND TESTSUBCLASSCD    = '{$testSubCd}' ";          // 試験科目コード

        return $query;
    }

    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckPerfectMstSql($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= "      AND APPLICANTDIV      = '{$model->applicantdiv}' ";// 入試制度
        $query .= "      AND TESTDIV           = '{$model->testdiv}' ";     // 入試区分
        $query .= "      AND COURSECD     = '0' ";
        $query .= "      AND MAJORCD      = '000' ";
        $query .= "      AND EXAMCOURSECD = '0000' ";
        $query .= "      AND EXAM_TYPE    = '0' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTDIV_MST)
    public function getCopyPerfectMstQuery($db, $model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " SELECT ";
        $query .= "     '{$model->nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '0', ";
        $query .= "     '000', ";
        $query .= "     '0000', ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     '0', "; // EXAM_TYPE（受験型）
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.RATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST T2 ";
        $query .= "         ON T2.ENTEXAMYEAR     = '{$model->nextYear}' "; //次年度の科目コードと一致したもののみをコピー
        $query .= "         AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "         AND T2.SETTING_CD     = 'L009' ";
        $query .= "         AND T2.SEQ            = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->leftYear}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " AND TESTDIV         = '{$model->testdiv}' ";

        return $query;
    }

    /******** 追加更新処理  ********/
    //試験別科目別満点マスタレコードを追加
    public function insertExtmPrfctExtypMst($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["COURSECD"][TEXT]         = "0";
        $data["MAJORCD"][TEXT]          = "000";
        $data["EXAMCOURSECD"][TEXT]     = "0000";
        $data["TESTSUBCLASSCD"][TEXT]   = $fields["TESTSUBCLASSCD"];
        $data["EXAM_TYPE"][TEXT]        = "0";
        $data["PERFECT"][TEXT]          = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_EXAMTYPE_MST");
        $db->query($query);

        Query::dbCheckIn($db);
    }

    //試験別科目別満点マスタレコードを更新
    public function updateExtmPrfctExtypMst($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["COURSECD"][TEXT]         = "0";
        $data["MAJORCD"][TEXT]          = "000";
        $data["EXAMCOURSECD"][TEXT]     = "0000";
        $data["TESTSUBCLASSCD"][TEXT]   = $fields["TESTSUBCLASSCD"];
        $data["EXAM_TYPE"][TEXT]        = "0";
        $data["PERFECT"][TEXT]          = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}'";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "   AND TESTSUBCLASSCD = '{$fields["TESTSUBCLASSCD"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_PERFECT_EXAMTYPE_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
    }

    //試験別科目別満点マスタレコードを削除
    public function deleteExtmPrfctExtypMst($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR           = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV      = '{$model->applicantdiv}'";
        $query .= "   AND TESTDIV           = '{$model->testdiv}' ";
        $query .= "   AND TESTSUBCLASSCD    = '{$fields["TESTSUBCLASSCD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
