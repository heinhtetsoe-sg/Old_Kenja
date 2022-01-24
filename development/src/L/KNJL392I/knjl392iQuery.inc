<?php
class knjl392iQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
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
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //名称マスタ取得
    function getEntexamTestDivMst($year, $applicantDiv) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$year}' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }


    function getEntexamTestSubcls($model) {
        $query  = " SELECT ";
        $query .= "  T1.TESTSUBCLASSCD AS VALUE, ";
        $query .= "  T1.TESTSUBCLASSCD || ':' || coalesce(N1.NAME1, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "  LEFT JOIN ENTEXAM_SETTING_MST N1  ";
        $query .= "    ON N1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "   AND N1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "   AND N1.SETTING_CD = 'L009' ";
        $query .= "   AND N1.SEQ = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND T1.EXAM_TYPE = '1' ";
        $query .= " ORDER BY ";
        $query .= "   CAST(VALUE AS INTEGER) ";
        return $query;
    }
}
?>
