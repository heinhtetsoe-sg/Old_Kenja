<?php

require_once('for_php7.php');

class knjl358cQuery extends Query {

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
        $query .= "                     ENTEXAM_RECEPT_DAT ";
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
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";	
        $query .= "                     ENTEXAMYEAR = '".$year."' )";	
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //専併区分取得
    function getSHDiv($namecd, $year, $div = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        if($div){
            $query .= " AND NAMECD2 = '1' ";
        }
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
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     CERTIF_KINDCD = '{$certif_kindcd}'";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' AND ";
        $query .= "     NAMECD2 = '{$namecd2}' ";
        return $query;
    }

    //テスト科目
    function getTestSubclass($year, $appdiv, $testdiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.TESTSUBCLASSCD, ";
        $query .= "     L1.NAME1 AS TESTSUBCLASSNAME, ";
        $query .= "     L1.NAMESPARE1 AS SELECT_SUBCLASSDIV, ";
        $query .= "     T2.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT T2 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L009' AND L1.NAMECD2 = T2.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T2.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T2.APPLICANTDIV = '{$appdiv}' AND ";
        $query .= "     T2.TESTDIV      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SHOWORDER ";
        return $query;
    }

    //ＣＳＶ出力
    function selectCsvQuery($year, $appdiv, $testdiv, $shdiv, $sortdiv, $target, $isGojou, $dispTestDiv, $isCollege)
    {
        $query  = "";
        $query .= " WITH T_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.TESTDIV, ";
        $query .= "         T1.EXAM_TYPE, ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.TESTSUBCLASSCD, ";
        $query .= "         T1.ATTEND_FLG, ";
        $query .= "         T1.SCORE, ";
        $query .= "         T2.SHOWORDER, ";
        $query .= "         L1.NAME1 AS TESTSUBCLASSNAME, ";
        $query .= "         L1.NAMESPARE1 AS SELECT_SUBCLASSDIV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT T2 ";
        $query .= "                 ON T2.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "                AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "                AND T2.TESTDIV        = T1.TESTDIV ";
        $query .= "                AND T2.EXAM_TYPE      = T1.EXAM_TYPE ";
        $query .= "                AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= "         LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L009' AND L1.NAMECD2 = T1.TESTSUBCLASSCD ";
        $query .= "     ) ";
        $query .= " , T_ZIPCD_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         NEW_ZIPCD, ";
        $query .= "         PREF ";
        $query .= "     FROM ";
        $query .= "         ZIPCD_MST ";
        $query .= "     WHERE ";
        $query .= "         NEW_ZIPCD IS NOT NULL AND ";
        $query .= "         PREF IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         NEW_ZIPCD, ";
        $query .= "         PREF ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     L1.ABBV1 AS SEX_NAME, ";
        $query .= "     L3.FINSCHOOL_ZIPCD, ";
        $query .= "     L4.PREF AS PREF_NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     VALUE(L3.FINSCHOOL_NAME_ABBV, L3.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ";
        $query .= "     T2.RECEPTNO, ";
        if ($isCollege && $appdiv == "1" && $testdiv == "1") {
            $query .= "     CASE WHEN T1.SUB_ORDER = '1' THEN 'Ⅰ型' ";
            $query .= "          WHEN T1.SUB_ORDER = '2' THEN 'Ⅱ型' ";
            $query .= "          ELSE '' END AS SUB_ORDER_NAME, ";
            $query .= "     T2.TOTAL2 AS TOTAL2, ";
            $query .= "     T2.AVARAGE2 AS AVERAGE2, ";
            $query .= "     T2.TOTAL4 AS TOTAL3, ";
            $query .= "     T2.AVARAGE4 AS AVERAGE3, ";
            $query .= "     T2.AVARAGE1 AS AVERAGE1, ";
            $query .= "     T2.TOTAL_RANK1 AS RANK1, ";
        } else {
            $query .= "     T2.TOTAL4 AS TOTAL, ";
            $query .= "     T2.AVARAGE4 AS AVERAGE, ";
            $query .= "     T2.TOTAL_RANK4 AS RANK, ";
        }
        $query .= "     T2.JUDGEDIV, ";
        $query .= "     T2.ATTEND_ALL_FLG, ";
        //対象者 1:受験者 2:合格者 3:入学者
//        if ($target == "1") {
            $query .= "     L2.NAME1 AS JUDGENAME, ";
//        } else {
//            $query .= "     L7.NAME1 AS JUDGENAME, ";
//        }
        $query .= "     CASE WHEN L6.ABBV1 IS NOT NULL THEN L6.ABBV1 || ' ' END AS RECOM_TESTDIVNAME, ";
        $query .= "     CASE WHEN T1.RECOM_EXAMNO IS NOT NULL THEN T1.RECOM_EXAMNO || ' ' END AS RECOM_EXAMNO, ";
        if ($isCollege) {
            $query .= "     CASE WHEN T1.SHIFT_DESIRE_FLG = '1' THEN '五併' || ' ' ";
            $query .= "          WHEN T1.SHIFT_DESIRE_FLG = '2' THEN '和併' || ' ' ";
            $query .= "          WHEN T1.SHIFT_DESIRE_FLG = '3' THEN '五併/和併' || ' ' ";
            $query .= "          ELSE '' END AS SHIFT_FLG, ";
        } else if ($isGojou) {
            $query .= "     CASE WHEN T1.SHIFT_DESIRE_FLG = '1' THEN 'カレッジ併願' || ' ' END AS SHIFT_FLG, ";
        } else {
            $query .= "     CASE WHEN T1.SHIFT_DESIRE_FLG = '1' THEN '移行希望' || ' ' END AS SHIFT_FLG, ";
        }
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.SELECT_SUBCLASS_DIV AS SELECT_SUBCLASSCD, ";
        $query .= "     T3.TESTSUBCLASSCD, ";
        $query .= "     T3.ATTEND_FLG, ";
        $query .= "     T3.SCORE, ";
        $query .= "     T3.SHOWORDER, ";
        $query .= "     T3.TESTSUBCLASSNAME, ";
        $query .= "     T3.SELECT_SUBCLASSDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "             ON T2.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "            AND T2.APPLICANTDIV  = T1.APPLICANTDIV ";
        $query .= "            AND T2.TESTDIV       = T1.TESTDIV ";
        $query .= "            AND T2.EXAMNO        = T1.EXAMNO ";
        $query .= "     LEFT JOIN T_SCORE T3 ";
        $query .= "             ON T3.ENTEXAMYEAR   = T2.ENTEXAMYEAR ";
        $query .= "            AND T3.APPLICANTDIV  = T2.APPLICANTDIV ";
        $query .= "            AND T3.TESTDIV       = T2.TESTDIV ";
        $query .= "            AND T3.EXAM_TYPE     = T2.EXAM_TYPE ";
        $query .= "            AND T3.RECEPTNO      = T2.RECEPTNO ";
        $query .= "     LEFT JOIN NAME_MST L7 ON L7.NAMECD1 = 'L013' AND L7.NAMECD2 = T1.JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L013' AND L2.NAMECD2 = T2.JUDGEDIV ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z002' AND L1.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L3 ON L3.FINSCHOOLCD = T1.FS_CD ";
        $query .= "     LEFT JOIN T_ZIPCD_MST L4 ON L4.NEW_ZIPCD = L3.FINSCHOOL_ZIPCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT L5 ";
        $query .= "             ON L5.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "            AND L5.APPLICANTDIV  = '1' "; //1:中学
        $query .= "            AND L5.TESTDIV       <> T1.TESTDIV ";
        $query .= "            AND L5.EXAMNO        = T1.RECOM_EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L6 ON L6.NAMECD1 = 'L004' AND L6.NAMECD2 = L5.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$appdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$testdiv}' ";
        if (!$isGojou || $appdiv != "1" && !$isCollege) {
            $query .= "     AND T1.SHDIV        = '{$shdiv}' ";
        }
        //対象者 1:受験者 2:合格者 3:入学者
        if ($target == "2") {
            $query .= " AND T1.JUDGEMENT in ('1','7') ";
        }
        if ($target == "3") {
            $query .= " AND T1.PROCEDUREDIV = '1' ";
            $query .= " AND T1.ENTDIV       = '1' ";
        }
        //ソート
        $query .= " ORDER BY ";
        if ($sortdiv == "1") {
            if ($isCollege && $appdiv == "1" && $testdiv == "1") {
                $query .= "     VALUE(T2.AVARAGE1,-1) DESC, ";
            } else {
                $query .= "     VALUE(T2.TOTAL4,-1) DESC, ";
            }
        }
        $query .= "     T1.EXAMNO, T3.SHOWORDER ";
        return $query;
    }
}
?>
