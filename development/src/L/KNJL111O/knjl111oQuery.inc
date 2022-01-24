<?php

require_once('for_php7.php');

class knjl111oQuery extends Query 
{
    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV || '：' || NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$ObjYear}' ";
        $query .= "     AND SHOWDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     VALUE ";
        return $query;
    }

    //受付番号取得
    function getRecept($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND RECEPT.EXAMNO = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RECEPT.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND RECEPT.EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";
        $query .= "     AND RECEPT.EXAMNO = '{$model->field["EXAMNO"]}' ";
        return $query;
    }

    //データ取得
    function getScoreSpare($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_SPARE_DAT SCORE ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";
        $query .= "     AND RECEPTNO = '{$model->field["RECEPTNO"]}' ";
        $query .= "     AND SEQ = '001' ";
        $query .= " ORDER BY ";
        $query .= "     TESTSUBCLASSCD ";
        return $query;
    }

    //加点取得
    function getKaten($model)
    {
        $query  = " SELECT ";
        $query .= "     KATEN ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";
        $query .= "     AND RECEPTNO = '{$model->field["RECEPTNO"]}' ";
        return $query;
    }

    function getDeleteQuery($db, $model) {

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_SPARE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";
        $query .= "     AND RECEPTNO = '{$model->field["RECEPTNO"]}' ";
        $query .= "     AND SEQ = '001' ";

        $db->query($query);
    }

    function getUpdateQuery($db, $model) {
        $sougou = 0;
        $katen = $db->getOne(knjl111oQuery::getKaten($model));
        if (!strlen($katen)) $katen = 0;
        foreach ($model->kamoku as $key => $val) {
            if ($key != "A") {
                $sougou += $model->meisaiField["SCORE".$key];
            }

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $model->field["EXAM_TYPE"];
            $data["TESTSUBCLASSCD"][TEXT]   = $key;
            $data["RECEPTNO"][TEXT]         = $model->field["RECEPTNO"];
            $data["SEQ"][TEXT]              = "001";
            if ($key != "A") {
                $data["SCORE1"][NUMBER]         = $model->meisaiField["SCORE".$key];
            } else {
                $data["SCORE1"][NUMBER]         = $sougou + $katen;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_SPARE_DAT");
            $db->query($query);
        }
    }
}
?>