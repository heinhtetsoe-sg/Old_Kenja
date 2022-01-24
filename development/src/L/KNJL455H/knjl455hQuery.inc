<?php

require_once('for_php7.php');

class knjl455hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE2 ";
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

    //ENTEXAM_SETTING_MSTより取得
    public function getEntexamSettingMst($model, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     *, ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND SETTING_CD   = '".$settingCd."' ";
        if ($seq) {
            $query .= "     AND SEQ = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //FINSCHOOL_MSTより取得
    public function getFinSchoolMst($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.FS_CD AS VALUE, ";
        $query .= "     T2.FS_CD || ':' || T3.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN FINSCHOOL_MST T3 ";
        $query .= "          ON T3.FINSCHOOLCD = T2.FS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
}
