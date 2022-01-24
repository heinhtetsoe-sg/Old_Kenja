<?php
class knjlz02iQuery extends Query
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

    //名称マスタ取得
    public function getSettingMst($year, $applicantdiv, $namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE,";
        $query .= "     SEQ || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' AND ";
        $query .= "     APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     SETTING_CD  = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "    AND SEQ = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度科目一覧取得
    public function selectQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "    SEQ AS VALUE, ";
        $query .= "    SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "    TESTSUBCLASSCD ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_SETTING_MST T1 ";
        $query .= "    LEFT JOIN ENTEXAM_TESTSUBCLASSCD_DAT T2 ";
        $query .= "      ON T2.ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "      AND T2.TESTDIV        = '{$model->testdiv}' ";
        $query .= "      AND T2.EXAM_TYPE      = '1' "; //固定
        $query .= "      AND T2.TESTSUBCLASSCD = T1.SEQ ";
        $query .= "  WHERE ";
        $query .= "    T1.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "    AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "    AND T1.SETTING_CD   = 'L009' ";
        $query .= "    AND T1.NAME1 IS NOT NULL ";
        $query .= "  ORDER BY  ";
        $query .= "       CAST(VALUE AS INT) ";

        return $query;
    }

    //年度コンボ
    public function selectYearQuery($model)
    {
        $query  = "  WITH TMP(ENTEXAMYEAR) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      SMALLINT (ENTEXAMYEAR) AS ENTEXAMYEAR";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR <> '{$model->examyear}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examyear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      ENTEXAMYEAR";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      ENTEXAMYEAR DESC ";

        return $query;
    }

    //DELETE
    public function getDeleteAllQuery($model)
    {
        //削除
        $query .= " DELETE FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = {$model->examyear} ";
        $query .= "   AND APPLICANTDIV  = {$model->applicantdiv} ";
        $query .= "   AND TESTDIV       = {$model->testdiv} ";
        
        return $query;
    }

    //UPDATE
    public function &getUpdateRowQuery($model, $testSubCd)
    {
        //更新
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = '1';
        $data["TESTSUBCLASSCD"][TEXT]   = $testSubCd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTSUBCLASSCD_DAT");
        return $query;
    }
}
