<?php

require_once('for_php7.php');

class knjl313cQuery extends Query {

    //学校コードを取得：五条・和歌山を判断するため
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //入試制度取得
    function getApctDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     APPLICANTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "                 WHERE ";	
        $query .= "                     ENTEXAMYEAR = '".$year."') ";	
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     TESTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "                 WHERE ";	
        $query .= "                     ENTEXAMYEAR = '".$year."' )";	
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得
    function getSchoolName($year, $certif_kindcd)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     CERTIF_KINDCD = '".$certif_kindcd."'";

        return $query;
    }

    //CVS作成用のQUERY
    function selectCsvQuery($model, $sex="")
    {
        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     N1.NAME1 AS APPDIV_NAME, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME, ";
        //カレッジ
        if ($model->isCollege) {
            $query .= "     CASE WHEN T1.SHDIV IN ('1','3','4','5','6','7','8') THEN '○' ELSE '' END AS SHDIV, ";
            $query .= "     CASE T1.SHDIV WHEN '3' THEN 'EA' WHEN '4' THEN 'ES' WHEN '5' THEN 'EA/ES' ";
            $query .= "                   WHEN '6' THEN 'G' WHEN '7' THEN 'S' WHEN '8' THEN 'S/G' ";
            $query .= "                   WHEN '9' THEN 'G' WHEN 'A' THEN 'S' WHEN 'B' THEN 'S/G' ";
            $query .= "                   ELSE '' END AS HOPE_COURSE, "; //志望コース
            $query .= "     CASE T1.SUB_ORDER WHEN '1' THEN 'Ⅰ型' WHEN '2' THEN 'Ⅱ型' ELSE '' END AS SUB_ORDER, "; //受験型
            $query .= "     CASE T1.SLIDE_FLG           WHEN '1' THEN '○' ELSE '' END AS SLIDE_FLG, ";
            $query .= "     CASE T1.SELECT_SUBCLASS_DIV WHEN '7' THEN '○' ELSE '' END AS SELECT_SUBCLASS_DIV7, ";
            $query .= "     CASE T1.SELECT_SUBCLASS_DIV WHEN '6' THEN '○' ELSE '' END AS SELECT_SUBCLASS_DIV6, ";
            $query .= "     CASE T1.DORMITORY_FLG       WHEN '1' THEN '○' ELSE '' END AS DORMITORY_FLG, ";
            $query .= "     CASE WHEN T1.TESTDIV = '8' AND value(T1.SPORTS_FLG,'0')  = '1' THEN '○' ELSE '' END AS SPORTS_FLG1, ";
            $query .= "     CASE WHEN T1.TESTDIV = '8' AND value(T1.SPORTS_FLG,'0') != '1' THEN '○' ELSE '' END AS SPORTS_FLG2, ";
        //五條
        } else if ($model->isGojou) {
            $query .= "     CASE T1.SHDIV WHEN '1' THEN '○' WHEN '6' THEN 'Ⅰ' WHEN '7' THEN 'Ⅱ' WHEN '8' THEN 'Ⅲ' ELSE '' END AS SHDIV, ";
            $query .= "     CASE T1.SLIDE_FLG           WHEN '1' THEN '○' ELSE '' END AS SLIDE_FLG, ";
            $query .= "     CASE T1.SELECT_SUBCLASS_DIV WHEN '7' THEN '○' ELSE '' END AS SELECT_SUBCLASS_DIV7, ";
            $query .= "     CASE T1.SELECT_SUBCLASS_DIV WHEN '6' THEN '○' ELSE '' END AS SELECT_SUBCLASS_DIV6, ";
            $query .= "     CASE T1.DORMITORY_FLG       WHEN '1' THEN '○' ELSE '' END AS DORMITORY_FLG, ";
            $query .= "     CASE WHEN T1.TESTDIV = '8' AND value(T1.SPORTS_FLG,'0')  = '1' THEN '○' ELSE '' END AS SPORTS_FLG1, ";
            $query .= "     CASE WHEN T1.TESTDIV = '8' AND value(T1.SPORTS_FLG,'0') != '1' THEN '○' ELSE '' END AS SPORTS_FLG2, ";
        //和歌山
        } else {
            $query .= "     CASE T1.SHDIV WHEN '1' THEN '専' WHEN '2' THEN '併' ELSE '' END AS SHDIV, ";
        }
        $query .= "     N3.ABBV1 AS SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T3.PREF_NAME, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T6.EDBOARDABBV AS EDBOARDNAME, ";
        $query .= "     T2.TELNO, ";
        $query .= "     N4.NAME1 AS DISTRICT_NAME, ";
        $query .= "     N5.NAME1 AS FS_DIST_NAME, ";
        $query .= "     T4.FINSCHOOL_NAME, ";
        $query .= "     P1.PRISCHOOL_NAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.GNAME, ";
        $query .= "     N6.NAME1 AS RELATIONSHIP, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T5.PREF_NAME AS GPREF_NAME, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T1.RECOM_EXAMNO, ";
        $query .= "     N8.ABBV1 AS OTHER_TESTDIV_NAME, ";
        $query .= "     T1.SHIFT_DESIRE_FLG, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1  ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T1.EXAMNO = T2.EXAMNO  ";
        $query .= "     LEFT JOIN PREF_MST T3 ON T2.PREF_CD = T3.PREF_CD  ";
        $query .= "     LEFT JOIN V_FINSCHOOL_MST T4 ON T1.ENTEXAMYEAR = T4.YEAR AND T1.FS_CD = T4.FINSCHOOLCD  ";
        $query .= "     LEFT JOIN PREF_MST T5 ON T2.GPREF_CD = T5.PREF_CD  ";
        $query .= "     LEFT JOIN EDBOARD_MST T6 ON T2.EDBOARDCD = T6.EDBOARDCD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON T1.ENTEXAMYEAR = N1.YEAR AND N1.NAMECD1 = 'L003' AND T1.APPLICANTDIV = N1.NAMECD2  ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON T1.ENTEXAMYEAR = N2.YEAR AND N2.NAMECD1 = 'L004' AND T1.TESTDIV = N2.NAMECD2  ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ON T1.ENTEXAMYEAR = N3.YEAR AND N3.NAMECD1 = 'Z002' AND T1.SEX = N3.NAMECD2  ";
        $query .= "     LEFT JOIN V_NAME_MST N4 ON T4.YEAR = N4.YEAR AND N4.NAMECD1 = 'Z003' AND T4.DISTRICTCD = N4.NAMECD2  ";
        $query .= "     LEFT JOIN V_NAME_MST N5 ON T4.YEAR = N5.YEAR AND N5.NAMECD1 = 'L001' AND T4.FINSCHOOL_DISTCD = N5.NAMECD2  ";
        $query .= "     LEFT JOIN V_NAME_MST N6 ON T2.ENTEXAMYEAR = N6.YEAR AND N6.NAMECD1 = 'H201' AND T2.RELATIONSHIP = N6.NAMECD2 ";
        $query .= "     LEFT JOIN V_NAME_MST N7 ON T1.ENTEXAMYEAR = N7.YEAR AND N7.NAMECD1 = 'L004' AND T1.TESTDIV1 = N7.NAMECD2  ";
        $query .= "     LEFT JOIN PRISCHOOL_MST P1 ON P1.PRISCHOOLCD = T1.PRISCHOOLCD  ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T8 ON T8.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T8.EXAMNO = T1.RECOM_EXAMNO AND T8.APPLICANTDIV = '1' AND T8.TESTDIV <> T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST N8 ON N8.NAMECD1 = 'L004' AND N8.NAMECD2 = T8.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        if($model->field["TESTDIV"] != "9"){
            $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        }
        if($sex){
            $query .= "     AND T1.SEX = '".$sex."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
