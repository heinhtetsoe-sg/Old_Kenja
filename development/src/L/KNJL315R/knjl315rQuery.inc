<?php

require_once('for_php7.php');

class knjl315rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    function getNameMstValue($year, $namecd1, $namecd2, $field)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //コース取得
    function getExamCourseMst($year, $model)
    {
        $query = "";
        $query .= " SELECT ";
        $query .= "     T1.DESIREDIV, T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD, T1.WISHNO, T2.EXAMCOURSE_MARK ";
        $query .= " FROM ENTEXAM_WISHDIV_MST T1 ";
        $query .= " LEFT JOIN ENTEXAM_COURSE_MST T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "     AND T2.COURSECD = T1.COURSECD ";
        $query .= "     AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "     AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.DESIREDIV, T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD, T1.WISHNO ";

        return $query;
    }


    //コースごとの満点取得
    function getCoursePefect($year, $model, $coursecode)
    {
        $query = "";
        $query .= " SELECT ";
        $query .= "     SUM(T2.PERFECT) AS PERFECT ";
        $query .= " FROM V_NAME_MST T1 ";
        $query .= " INNER JOIN ENTEXAM_PERFECT_MST T2 ON T2.ENTEXAMYEAR = T1.YEAR ";
        $query .= "     AND T2.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T2.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND T1.NAMECD2 = T2.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.NAMECD1 = 'L009' ";
        if ("1" == $model->field["TESTDIV"]) {
            $query .= "     AND NAMESPARE2 = '1' ";
        } else if ("2" == $model->field["TESTDIV"]) {
            $query .= "     AND NAMESPARE3 = '1' ";
        }
        $query .= "     AND T2.COURSECD || T2.MAJORCD || T2.EXAMCOURSECD = '".$coursecode."' ";

        return $query;
    }

    //得点分布リスト
    function getScoreDistList($year, $model, $desireDiv, $naidaku, $perfect, $scores)
    {
        $list = array();
        if (empty($scores)) {
            return $list;
        } 
        $max = 0;
        $min = perfect;
        foreach ($scores as $score) {
            $max = $score > $max ? $score : $max;
            $min = $score < $min ? $score : $min;
        }
        $max = $max + 10 < $perfect ? $max + 10 : $perfect;
        $min = $min - 10 > 0 ? $min - 10 : 0;
        $max = floor($max / 5) * 5;
        $min = floor($min / 5) * 5;

        $kizami = 5;
        for ($s = $max; $s >= $min; $s -= $kizami) {
            $dist = array();
            $dist["UPPER"] = $s + 4;
            $dist["LOWER"] = $s;
            $dist["COUNT"] = 0;
            foreach ($scores as $score) {
                if ($dist["LOWER"] <= $score && $score <= $dist["UPPER"]) {
                    $dist["COUNT"] += 1;
                }
            }
            $list[] = $dist;
        }
        return $list;

    }

    //得点
    function getScoreList($year, $model, $desireDiv, $naidaku, $juken)
    {
        $query  = " SELECT ";
        $query .= "     T1.TOTAL4 AS SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND BASE.EXAMNO = T1.EXAMNO ";
        $query .= "         AND BASE.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV = T1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND T2.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND T2.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND T2.SEQ          = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT BEF ON BEF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND BEF.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND BEF.TESTDIV = BASE.TESTDIV ";
        $query .= "         AND BEF.BEFORE_PAGE = T2.REMARK1 ";
        $query .= "         AND BEF.BEFORE_SEQ = T2.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T14 ON T14.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND T14.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND T14.TESTDIV = BASE.TESTDIV ";
        $query .= "         AND T14.COURSECD = BEF.BEFORE_COURSECD ";
        $query .= "         AND T14.MAJORCD = BEF.BEFORE_MAJORCD ";
        $query .= "         AND T14.EXAMCOURSECD = BEF.BEFORE_EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        if ($juken) {
            $query .= "     AND T1.TOTAL4 IS NOT NULL ";
        } else {
            $query .= "     AND T1.TOTAL4 IS NULL ";
        }
        $query .= "     AND BASE.DESIREDIV = '".$desireDiv."' ";
        if ($naidaku) {
            $query .= "     AND BEF.BEFORE_EXAMCOURSECD IS NOT NULL ";
        } else {
            $query .= "     AND BEF.BEFORE_EXAMCOURSECD IS NULL ";
        }
        return $query;
    }
}
?>
