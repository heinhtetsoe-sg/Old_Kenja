<?php

require_once('for_php7.php');

class knjl315cQuery extends Query
{

    //学校コードを取得：五条・和歌山を判断するため
    public function getNameMstZ010()
    {
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
    public function getApctDiv($namecd, $year)
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
    public function getTestDiv($namecd, $year)
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
    public function getSHDiv($namecd, $year, $div = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        if ($div) {
            $query .= " AND NAMECD2 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    public function getNameMst($namecd1, $namecd2)
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
    public function getTestSubclass($year, $appdiv, $testdiv, $sub2ka3kaDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.TESTSUBCLASSCD, ";
        $query .= "     L1.NAME1 AS TESTSUBCLASSNAME, ";
        $query .= "     T2.PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T2 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L009' AND L1.NAMECD2 = T2.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T2.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T2.APPLICANTDIV = '{$appdiv}' AND ";
        $query .= "     T2.TESTDIV      = '{$testdiv}' ";
        if ($sub2ka3kaDiv == "2") {
            $query .= "     AND T2.TESTSUBCLASSCD IN ('1','2') ";       //国算
        }
        if ($sub2ka3kaDiv == "3") {
            $query .= "     AND T2.TESTSUBCLASSCD IN ('1','2','3') ";   //国算理
        }
        $query .= " ORDER BY ";
        $query .= "     T2.TESTSUBCLASSCD ";
        return $query;
    }

    //合計
    public function selectCsvQueryTotal($year, $appdiv, $testdiv, $shdiv, $gojo, $sub2ka3kaDiv, $isCollege, $isWakayama)
    {
        $total = $sub2ka3kaDiv == "2" ? "TOTAL2" : "TOTAL4";
        $perfect = $gojo ? 400 : $isWakayama ? 300 : 500;
        $query  = "";
        $query .= " WITH T_TOTALSCORE AS( ";
        $query .= "     SELECT ";
        $query .= "         (CASE WHEN MOD({$perfect} - W1.{$total}, 10) = 0 THEN 0 ";
        $query .= "          ELSE 1 END) + ({$perfect} - W1.{$total})/10 AS DISTINDEX, ";
        $query .= "         W1.{$total} ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT W1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT L1 ON ";
        $query .= "             L1.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ";
        $query .= "             L1.APPLICANTDIV = W1.APPLICANTDIV AND ";
        $query .= "             L1.TESTDIV = W1.TESTDIV AND ";
        $query .= "             L1.EXAMNO = W1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         W1.ATTEND_ALL_FLG = '1' AND ";
        $query .= "         W1.{$total} IS NOT NULL AND ";
        $query .= "         W1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "         W1.APPLICANTDIV = '{$appdiv}' AND ";
        $query .= "         W1.TESTDIV      = '{$testdiv}' ";
        if (!$gojo || $appdiv != "1" && !$isCollege) {
            $query .= "         AND L1.SHDIV        = '{$shdiv}' ";
        }
        if ($isWakayama) {
            $query .= "         AND W1.{$total}     <= {$perfect} ";
        }
        if ($sub2ka3kaDiv == "2") {
            $query .= "         AND L1.SUB_ORDER IN ('1','2') ";//Ⅰ型＋Ⅱ型
        }
        if ($sub2ka3kaDiv == "3") {
            $query .= "         AND L1.SUB_ORDER IN ('1') ";    //Ⅰ型
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.DISTINDEX, ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_TOTALSCORE T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.DISTINDEX ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     777 AS DISTINDEX, ";
        $query .= "     COUNT(T2.{$total}) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_TOTALSCORE T2 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     888 AS DISTINDEX, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T3.{$total}))*10,0)/10,5,1) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_TOTALSCORE T3 ";
        $query .= " ORDER BY DISTINDEX ";
        return $query;
    }

    //科目
    public function selectCsvQuerySubclass($year, $appdiv, $testdiv, $shdiv, $gojo, $sub2ka3kaDiv, $isCollege)
    {
        $query  = "";
        $query .= " WITH T_ENTEXAM_SUBCLASS AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME1 AS SUBCLASSNAME, ";
        $query .= "     5 AS KIZAMI ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST T2 ON ";
        $query .= "             T2.NAMECD1 = 'L009' AND ";
        $query .= "             T2.NAMECD2 = T1.TESTSUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     PERFECT DESC, TESTSUBCLASSCD ";
        $query .= " ), T_ENTEXAM_SCORE AS( ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T2.PERFECT, ";
        $query .= "     T1.SCORE, ";
        $query .= "     (CASE WHEN MOD(T2.PERFECT - T1.SCORE, T2.KIZAMI) = 0 THEN 0 ";
        $query .= "      ELSE 1 END) + (T2.PERFECT - T1.SCORE)/T2.KIZAMI AS DISTINDEX ";
        $query .= " FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= "     LEFT JOIN T_ENTEXAM_SUBCLASS T2 ON ";
        $query .= "         T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "         T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "         T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "         T1.TESTSUBCLASSCD = T2.TESTSUBCLASSCD ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT L1 ON ";
        $query .= "         L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "         AND L1.RECEPTNO = T1.RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT L2 ON ";
        $query .= "         L2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ";
        $query .= "         L2.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         L2.TESTDIV = T1.TESTDIV AND ";
        $query .= "         L2.EXAMNO = L1.EXAMNO ";
        $query .= " WHERE  ";
        $query .= "     T1.ATTEND_FLG = '1' AND ";
        $query .= "     T1.ENTEXAMYEAR  = '{$year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$appdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$testdiv}' ";
        if (!$gojo || $appdiv != "1" && !$isCollege) {
            $query .= "     AND L2.SHDIV        = '{$shdiv}' ";
        }
        if ($sub2ka3kaDiv == "2") {
            $query .= "     AND L2.SUB_ORDER IN ('1','2') ";            //Ⅰ型＋Ⅱ型
            $query .= "     AND T1.TESTSUBCLASSCD IN ('1','2') ";       //国算
        }
        if ($sub2ka3kaDiv == "3") {
            $query .= "     AND L2.SUB_ORDER IN ('1') ";                //Ⅰ型
            $query .= "     AND T1.TESTSUBCLASSCD IN ('1','2','3') ";   //国算理
        }
        $query .= " ) ";
        $query .= " SELECT  ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.SUBCLASSNAME,  ";
        $query .= "     T1.DISTINDEX, ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_ENTEXAM_SCORE T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.SUBCLASSNAME,  ";
        $query .= "     T1.DISTINDEX ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD,  ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     777 AS DISTINDEX, ";
        $query .= "     COUNT(SCORE) AS COUNT   ";
        $query .= " FROM ";
        $query .= "     T_ENTEXAM_SCORE T1 ";
        $query .= " GROUP BY      ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.SUBCLASSNAME ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD,  ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     888 AS DISTINDEX, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS COUNT   ";
        $query .= " FROM ";
        $query .= "     T_ENTEXAM_SCORE T1 ";
        $query .= " GROUP BY      ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     DISTINDEX ";
        return $query;
    }
}
