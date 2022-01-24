<?php
class knjl693hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    public function getEntexamTestDivMst($year, $applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_NAME LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$year}' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //類別マスタ取得
    public function getEntexamClassifyMst($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     CLASSIFY_CD || ':' || VALUE(CLASSIFY_NAME, '') AS LABEL,  ";
        $query .= "     CLASSIFY_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' ";
        $query .= " AND APPLICANTDIV = '".$applicantDiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試用受験番号用票の人数取得
    public function getReceptnoCnt($model)
    {
        $receptnoStart = sprintf("%04d", $model->field["RECEPTNO_START"]);
        $receptnoEnd = sprintf("%04d", $model->field["RECEPTNO_END"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(RECEPT.RECEPTNO) AS EXAMNO_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "      AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "      AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "      AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= " WHERE  ";
        $query .= "       RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "   AND RECEPT.APPLICANTDIV = '2' "; // 2:高校 固定
        $query .= "   AND RECEPT.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TESTDIV1"] <> "00") {
            $query .= "   AND BASE.TESTDIV1       = '{$model->field["TESTDIV1"]}' ";
        }
        if ($model->field["RECEPTNO_START"] <> "") {
            $query .= "   AND RECEPT.RECEPTNO >= '{$receptnoStart}' ";
        }
        if ($model->field["RECEPTNO_END"] <> "") {
            $query .= "   AND RECEPT.RECEPTNO <= '{$receptnoEnd}' ";
        }
        if ($model->field["SEX"] <> "3") {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= "   AND (BASE.JUDGEMENT <> '3' OR BASE.JUDGEMENT IS NULL) "; // 3:欠席 は除く
        return $query;
    }

    //入試用受験番号用票の人数取得
    public function getEntReceptnoCnt($model)
    {
        $receptnoStart = sprintf("%04d", $model->field["RECEPTNO_START"]);
        $receptnoEnd = sprintf("%04d", $model->field["RECEPTNO_END"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(RECEPT.RECEPTNO) AS EXAMNO_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "      AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "      AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "      AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= " WHERE  ";
        $query .= "       RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "   AND RECEPT.APPLICANTDIV = '2' "; // 2:高校 固定
        $query .= "   AND RECEPT.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TESTDIV1"] <> "00") {
            $query .= "   AND BASE.TESTDIV1       = '{$model->field["TESTDIV1"]}' ";
        }
        if ($model->field["RECEPTNO_START"] <> "") {
            $query .= "   AND RECEPT.RECEPTNO >= '{$receptnoStart}' ";
        }
        if ($model->field["RECEPTNO_END"] <> "") {
            $query .= "   AND RECEPT.RECEPTNO <= '{$receptnoEnd}' ";
        }
        if ($model->field["SEX"] <> "3") {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= "   AND BASE.JUDGEMENT = '1' "; // 1:合格
        $query .= "   AND BASE.ENTDIV    = '1' "; // 1:入学
        return $query;
    }
}
