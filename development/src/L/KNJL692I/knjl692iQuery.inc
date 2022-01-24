<?php

require_once('for_php7.php');

class knjl692iQuery extends Query
{
    //入試区分マスタより取得
    public function getTestDivMst($model, $testDiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV  = '".$model->applicantDiv."' ";
        if ($testDiv != "") {
            $query .= "     AND TESTDIV  = '".$testDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //類別を取得
    public function getRuibetsu($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_CD AS VALUE, ";
        $query .= "     GENERAL_CD || ':' || GENERAL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "     AND GENERAL_DIV = '01' ";
        $query .= "     AND TESTDIV = '0' "; //固定
        if ($model->field["GAKKA"] != "ALL") {
            $query .= "     AND REMARK1 = '".$model->field["GAKKA"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //相談コースを取得
    public function getConsulCourse($model, $gakka = "")
    {
        $query  = " SELECT ";
        $query .= "     G2.GENERAL_CD, ";
        $query .= "     G2.GENERAL_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST G1 ";
        $query .= "     INNER JOIN ENTEXAM_GENERAL_MST G2 ";
        $query .= "             ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ";
        $query .= "            AND G2.APPLICANTDIV = G1.APPLICANTDIV ";
        $query .= "            AND G2.GENERAL_DIV  = '02' ";
        $query .= "            AND G2.TESTDIV      = G1.TESTDIV ";
        $query .= "            AND G2.REMARK1      = G1.GENERAL_CD ";
        $query .= " WHERE ";
        $query .= "     G1.ENTEXAMYEAR      = '".$model->examyear."' ";
        $query .= "     AND G1.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "     AND G1.GENERAL_DIV  = '01' ";
        $query .= "     AND G1.TESTDIV      = '0' "; //固定
        $query .= "     AND G1.REMARK1      = '".$gakka."' ";
        $query .= " ORDER BY ";
        $query .= "     G2.GENERAL_CD ";

        return $query;
    }

    public function getApplicationList($model)
    {
        // 指示画面の類別コースで絞込むためのテーブル
        $query  = " WITH GENERAL_D2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         G2.ENTEXAMYEAR, ";
        $query .= "         G2.GENERAL_CD, ";
        $query .= "         G2.GENERAL_NAME, ";
        $query .= "         G2.REMARK1 AS RUIBETSU_COURSE, ";
        $query .= "         G1.REMARK1 AS GAKKA ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_GENERAL_MST G1 ";
        $query .= "         INNER JOIN ENTEXAM_GENERAL_MST G2 ";
        $query .= "                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ";
        $query .= "                AND G2.APPLICANTDIV = G1.APPLICANTDIV ";
        $query .= "                AND G2.TESTDIV      = G1.TESTDIV ";
        $query .= "                AND G2.GENERAL_DIV  = '02' ";
        $query .= "                AND G2.REMARK1      = G1.GENERAL_CD ";
        $query .= "     WHERE ";
        $query .= "         G1.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "     AND G1.TESTDIV      = '0' ";
        $query .= "     AND G1.GENERAL_DIV  = '01' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     CASE WHEN BASE.SEX = '1' THEN 'M' WHEN BASE.SEX = '2' THEN 'F' ELSE '' END AS SEX, ";
        $query .= "     DESIRE.GENERAL_CD AS DESIRE_COURSE_CD, ";
        $query .= "     DESIRE.GENERAL_NAME AS DESIRE_COURSE_NAME, ";
        $query .= "     CONSUL.GENERAL_CD AS CONSUL_COURSE_CD, ";
        $query .= "     CONSUL.GENERAL_NAME AS CONSUL_COURSE_NAME, ";
        $query .= "     ADV.FS_CD, ";
        $query .= "     FIN_MST.FINSCHOOL_NAME, ";
        $query .= "     ADV_D3.REMARK1 AS COMMON_TEST, ";
        $query .= "     ADV_D8.REMARK1 AS ABSENCE_DAYS, ";
        $query .= "     GENE05.GENERAL_NAME AS SP_REASON, ";
        $query .= "     ADV_D6.REMARK1 AS REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DAT ADV ";
        $query .= "            ON ADV.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND ADV.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D31 ";
        $query .= "            ON BASE_D31.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE_D31.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND BASE_D31.EXAMNO       = BASE.EXAMNO ";
        $query .= "           AND BASE_D31.SEQ          = '031' ";
        $query .= "     LEFT JOIN GENERAL_D2 DESIRE ";
        $query .= "            ON DESIRE.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ";
        $query .= "           AND DESIRE.GENERAL_CD   = BASE_D31.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D1 ";
        $query .= "            ON ADV_D1.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D1.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D1.SEQ         = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D2 ";
        $query .= "            ON ADV_D2.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D2.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D2.SEQ         = '002' ";
        $query .= "     LEFT JOIN GENERAL_D2 CONSUL ";
        $query .= "            ON CONSUL.ENTEXAMYEAR  = ADV_D2.ENTEXAMYEAR ";
        $query .= "           AND CONSUL.GENERAL_CD   = ADV_D2.REMARK1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN_MST ";
        $query .= "            ON FIN_MST.FINSCHOOLCD = ADV.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D3 ";
        $query .= "            ON ADV_D3.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D3.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D3.SEQ         = '003' ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D8 ";
        $query .= "            ON ADV_D8.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D8.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D8.SEQ         = '008' ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D4 ";
        $query .= "            ON ADV_D4.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D4.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D4.SEQ         = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GENE05 ";
        $query .= "            ON GENE05.ENTEXAMYEAR  = ADV_D4.ENTEXAMYEAR ";
        $query .= "           AND GENE05.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "           AND GENE05.TESTDIV      = '0' ";
        $query .= "           AND GENE05.GENERAL_DIV  = '05' ";
        $query .= "           AND GENE05.GENERAL_CD   = ADV_D4.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_RECRUIT_ADVICE_DETAIL_DAT ADV_D6 ";
        $query .= "            ON ADV_D6.ENTEXAMYEAR = ADV.ENTEXAMYEAR ";
        $query .= "           AND ADV_D6.EXAMNO      = ADV.EXAMNO ";
        $query .= "           AND ADV_D6.SEQ         = '006' ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= " AND BASE.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= " AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        if ($model->field["GAKKA"] != "ALL") {
            $query .= "     AND CONSUL.GAKKA = '".$model->field["GAKKA"]."' ";
        }
        if ($model->field["RUIBETSU"] != "ALL") {
            $query .= "     AND CONSUL.RUIBETSU_COURSE = '".$model->field["RUIBETSU"]."' ";
        }
        if ($model->field["EXAMNO_FROM"] != "") {
            if ($model->field["EXAMNO_TO"] != "") {
                $query .= "     AND (ADV.EXAMNO BETWEEN '".$model->field["EXAMNO_FROM"]."' AND '".$model->field["EXAMNO_TO"]."') ";
            } else {
                $query .= "     AND ADV.EXAMNO >= '".$model->field["EXAMNO_FROM"]."' ";
            }
        } else {
            if ($model->field["EXAMNO_TO"] != "") {
                $query .= "     AND ADV.EXAMNO <= '".$model->field["EXAMNO_TO"]."' ";
            }
        }
        // 3:全て 以外のときに条件に加える
        if ($model->field["SEX"] != "3") {
            $query .= "     AND BASE.SEX = '".$model->field["SEX"]."' ";
        }
        if ($model->field["OUTPUT_DIV"] == "2") {
            $query .= "     AND ADV_D4.REMARK2 = '01' "; // 01:学業
        } elseif ($model->field["OUTPUT_DIV"] == "3") {
            $query .= "     AND ADV_D4.REMARK2 <> '01' "; // 01:学業 以外
        }
        $query .= " ORDER BY ";
        if ($model->field["ORDER"] == "2") {
            $query .= "     BASE.NAME, ";
        }
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    public function getApplicationFinschoolList($model, $generalCourseList, $techinicalCourseList)
    {
        $query  = " WITH GENE_COSE AS ( ";
        $query .= "     SELECT ";
        $query .= "         G2.ENTEXAMYEAR, ";
        $query .= "         G2.GENERAL_CD ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_GENERAL_MST G1 ";
        $query .= "         INNER JOIN ENTEXAM_GENERAL_MST G2 ";
        $query .= "                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ";
        $query .= "                AND G2.APPLICANTDIV = G1.APPLICANTDIV ";
        $query .= "                AND G2.TESTDIV      = G1.TESTDIV ";
        $query .= "                AND G2.GENERAL_DIV  = '02' ";
        $query .= "                AND G2.REMARK1      = G1.GENERAL_CD ";
        $query .= "     WHERE ";
        $query .= "         G1.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "     AND G1.TESTDIV      = '0' ";
        $query .= "     AND G1.GENERAL_DIV  = '01' ";
        $query .= "     AND G1.REMARK1      = '1' "; // 1:普通科
        $query .= " ), ";
        $query .= " TECH_COSE AS ( ";
        $query .= "     SELECT ";
        $query .= "         G2.ENTEXAMYEAR, ";
        $query .= "         G2.GENERAL_CD ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_GENERAL_MST G1 ";
        $query .= "         INNER JOIN ENTEXAM_GENERAL_MST G2 ";
        $query .= "                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ";
        $query .= "                AND G2.APPLICANTDIV = G1.APPLICANTDIV ";
        $query .= "                AND G2.TESTDIV      = G1.TESTDIV ";
        $query .= "                AND G2.GENERAL_DIV  = '02' ";
        $query .= "                AND G2.REMARK1      = G1.GENERAL_CD ";
        $query .= "     WHERE ";
        $query .= "         G1.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "     AND G1.TESTDIV      = '0' ";
        $query .= "     AND G1.GENERAL_DIV  = '01' ";
        $query .= "     AND G1.REMARK1      = '2' "; // 2:工業科
        $query .= " ), ";
        // 指示画面の類別コースで絞込むためのテーブル
        $query .= " GENERAL_D2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         G2.ENTEXAMYEAR, ";
        $query .= "         G2.GENERAL_CD, ";
        $query .= "         G2.REMARK1 AS RUIBETSU_COURSE, ";
        $query .= "         G1.REMARK1 AS GAKKA ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_GENERAL_MST G1 ";
        $query .= "         INNER JOIN ENTEXAM_GENERAL_MST G2 ";
        $query .= "                 ON G2.ENTEXAMYEAR  = G1.ENTEXAMYEAR ";
        $query .= "                AND G2.APPLICANTDIV = G1.APPLICANTDIV ";
        $query .= "                AND G2.TESTDIV      = G1.TESTDIV ";
        $query .= "                AND G2.GENERAL_DIV  = '02' ";
        $query .= "                AND G2.REMARK1      = G1.GENERAL_CD ";
        $query .= "     WHERE ";
        $query .= "         G1.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= "     AND G1.TESTDIV      = '0' ";
        $query .= "     AND G1.GENERAL_DIV  = '01' ";
        $query .= " ), ";
        $query .= " MAIN AS ( ";
        $query .= " SELECT ";
        foreach ($generalCourseList as $val) {
            $query .= "     SUM(CASE WHEN GENE_COSE.GENERAL_CD = '".$val."' THEN 1 ELSE 0 END) AS GENERAL_COURSE".$val.", ";
        }
        foreach ($techinicalCourseList as $val) {
            $query .= "     SUM(CASE WHEN TECH_COSE.GENERAL_CD = '".$val."' THEN 1 ELSE 0 END) AS TECHNICAL_COURSE".$val.", ";
        }
        $query .= "     ROW_NUMBER () OVER () AS NO, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FIN_MST.FINSCHOOL_NAME_ABBV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D31 ";
        $query .= "            ON BASE_D31.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE_D31.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND BASE_D31.EXAMNO       = BASE.EXAMNO ";
        $query .= "           AND BASE_D31.SEQ          = '031' ";
        $query .= "     LEFT JOIN GENE_COSE ";
        $query .= "            ON GENE_COSE.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ";
        $query .= "           AND GENE_COSE.GENERAL_CD   = BASE_D31.REMARK1 ";
        $query .= "     LEFT JOIN TECH_COSE ";
        $query .= "            ON TECH_COSE.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ";
        $query .= "           AND TECH_COSE.GENERAL_CD   = BASE_D31.REMARK1 ";
        $query .= "     LEFT JOIN GENERAL_D2 ";
        $query .= "            ON GENERAL_D2.ENTEXAMYEAR  = BASE_D31.ENTEXAMYEAR ";
        $query .= "           AND GENERAL_D2.GENERAL_CD   = BASE_D31.REMARK1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN_MST ";
        $query .= "            ON FIN_MST.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= " AND BASE.APPLICANTDIV = '".$model->applicantDiv."' ";
        $query .= " AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        if ($model->field["GAKKA"] != "ALL") {
            $query .= "     AND GENERAL_D2.GAKKA = '".$model->field["GAKKA"]."' ";
        }
        if ($model->field["RUIBETSU"] != "ALL") {
            $query .= "     AND GENERAL_D2.RUIBETSU_COURSE = '".$model->field["RUIBETSU"]."' ";
        }
        if ($model->field["EXAMNO_FROM"] != "") {
            if ($model->field["EXAMNO_TO"] != "") {
                $query .= "     AND (BASE.EXAMNO BETWEEN '".$model->field["EXAMNO_FROM"]."' AND '".$model->field["EXAMNO_TO"]."') ";
            } else {
                $query .= "     AND BASE.EXAMNO >= '".$model->field["EXAMNO_FROM"]."' ";
            }
        } else {
            if ($model->field["EXAMNO_TO"] != "") {
                $query .= "     AND BASE.EXAMNO <= '".$model->field["EXAMNO_TO"]."' ";
            }
        }
        // 3:全て 以外のときに条件に加える
        if ($model->field["SEX"] != "3") {
            $query .= "     AND BASE.SEX = '".$model->field["SEX"]."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FIN_MST.FINSCHOOL_NAME_ABBV ";
        $query .= " ORDER BY ";
        $query .= "     BASE.FS_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        foreach ($generalCourseList as $val) {
            $query .= "     GENERAL_COURSE".$val.", ";
        }
        foreach ($techinicalCourseList as $val) {
            $query .= "     TECHNICAL_COURSE".$val.", ";
        }
        $plusMark = "";
        foreach ($generalCourseList as $val) {
            $query .= $plusMark."GENERAL_COURSE".$val;
            $plusMark = " + ";
        }
        foreach ($techinicalCourseList as $val) {
            $query .= " + TECHNICAL_COURSE".$val;
        }
        $query .= "     AS TOTAL, ";
        $query .= "     NO, ";
        $query .= "     FS_CD, ";
        $query .= "     FINSCHOOL_NAME_ABBV ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     FS_CD ";

        return $query;
    }
    public function getWarekiAbbv($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1, ";
        $query .= "     CHAR(".$model->examyear." + 1 - INT(NAMESPARE1)) AS YEAR ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->examyear."' AND ";
        $query .= "     NAMECD1 = 'L007' AND ";
        $query .= "     '".$model->examyear."' BETWEEN NAMESPARE1 AND ABBV3 ";

        return $query;
    }
}
