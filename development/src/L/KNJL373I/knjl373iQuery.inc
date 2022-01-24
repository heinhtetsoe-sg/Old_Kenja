<?php

class knjl373iquery extends Query
{

    //名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        if ($nameCd1 != "") {
            $query .= "   AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    public function getEntexamTestDivMst($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    
    //地区選択取得
    public function getDistrictName()
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.DISTRICTCD VALUE, ";
        $query .= "   T2.NAME1 LABEL ";
        $query .= " FROM FINSCHOOL_MST T1 ";
        $query .= " INNER JOIN V_NAME_MST T2 ON T2.YEAR = '".(CTRL_YEAR + 1)."' AND NAMECD1 = 'Z003' AND T2.NAMECD2 = T1.DISTRICTCD ";
        $query .= " ORDER BY T1.DISTRICTCD ";

        return $query;
    }
}
