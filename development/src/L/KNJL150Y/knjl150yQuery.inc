<?php

require_once('for_php7.php');

class knjl150yQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMstCsv($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //CSV出力(志願者情報)
    public function getCsvQuery($model)
    {
        $query  = "";
        $query .= " WITH T_COURSE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.DESIREDIV, ";
        $query .= "         T1.WISHNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.EXAMCOURSECD, ";
        $query .= "         T2.EXAMCOURSE_NAME, ";
        $query .= "         T2.EXAMCOURSE_ABBV, ";
        $query .= "         T2.EXAMCOURSE_MARK ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST T1 ";
        $query .= "         INNER JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "             ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND T2.COURSECD     = T1.COURSECD ";
        $query .= "             AND T2.MAJORCD      = T1.MAJORCD ";
        $query .= "             AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     WHERE ";
        $query .= "             T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "     ) ";
        $query .= " , T_COURSE3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.DESIREDIV, ";
        $query .= "         T1.WISHNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.EXAMCOURSECD, ";
        $query .= "         T2.EXAMCOURSE_NAME, ";
        $query .= "         T2.EXAMCOURSE_ABBV, ";
        $query .= "         T2.EXAMCOURSE_MARK ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST T1 ";
        $query .= "         INNER JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "             ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND T2.COURSECD     = T1.COURSECD ";
        $query .= "             AND T2.MAJORCD      = T1.MAJORCD ";
        $query .= "             AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     WHERE ";
        $query .= "             T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND T1.TESTDIV      = '3' "; //高校一般入試、中学第二回一般入試
        $query .= "     ) ";
        $query .= " , T_SCORE AS ( ";
        //配点ラジオボタン 1:傾斜配点 2:通常得点
        $score = ($model->field["HAITEN"] == "2") ? "SCORE" : "SCORE2";
        $query .= "     SELECT ";
        $query .= "         RECEPTNO, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '1' THEN {$score} END) AS KOKUGO, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN {$score} END) AS SUUGAKU, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '2' THEN SCORE3   END) AS KEISAN, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN {$score} END) AS TEKISEI, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '3' THEN {$score} END) AS SHAKAI, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '4' THEN {$score} END) AS RIKA, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '5' THEN {$score} END) AS EIGO, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '6' THEN {$score} END) AS LISNING, ";
        $query .= "         SUM(CASE WHEN TESTSUBCLASSCD = '9' THEN SCORE    END) AS KATEN ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT ";
        $query .= "     WHERE ";
        $query .= "             ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         RECEPTNO ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.RECOM_KIND, ";
        $query .= "     T1.EXAMNO, ";
        //志願者情報
        $query .= "     CASE WHEN T2.INTERVIEW_ATTEND_FLG = '1' THEN '帰' END AS INTERVIEW_ATTEND, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.SEX, ";
        $query .= "     N0.ABBV1 AS SEX_NAME, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T2.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME, ";
        $query .= "     T2.FS_GRDYEAR || '.' || T2.FS_M AS FS_GRDYEAR_M, "; //ADD
        $query .= "     T2.FS_GRDDIV, ";
        $query .= "     N3.ABBV1 AS FS_GRDDIV_NAME, ";
        $query .= "     W1.EXAMCOURSE_NAME  AS EXAMCOURSE_NAME1, ";
        $query .= "     CASE WHEN T2.TESTDIV = '".$model->field["TESTDIV"]."' AND T2.SLIDE_FLG = '1' THEN W2.EXAMCOURSE_NAME END AS EXAMCOURSE_NAME2, ";
        $query .= "     T3.TELNO, ";
        $query .= "     T3.ZIPCD, ";
        $query .= "     VALUE(T3.ADDRESS1,'') || VALUE(T3.ADDRESS2,'') AS ADDRESS, ";
        $query .= "     T3.GNAME, ";
        $query .= "     T3.GKANA, ";
        $query .= "     T3.RELATIONSHIP, ";
        $query .= "     N4.NAME1 AS RELATIONSHIP_NAME, ";
        $query .= "     T3.GTELNO, "; //ADD
        $query .= "     T3.GZIPCD, "; //ADD
        $query .= "     VALUE(T3.GADDRESS1,'') || VALUE(T3.GADDRESS2,'') AS GADDRESS, "; //ADD
        $query .= "     CASE WHEN T2.SHIFT_DESIRE_FLG = '1' THEN '有' END AS SHIFT_DESIRE, "; //ADD
        $query .= "     CASE WHEN BDET005.REMARK1  = '1' THEN '有' END AS TTOKU, ";
        $query .= "     CASE WHEN T2.SLIDE_FLG     = '1' THEN '有' END AS SLIDE, "; //ADD
        $query .= "     CASE WHEN T2.GENERAL_FLG   = '1' THEN '有' END AS GENERAL, ";
        $query .= "     CASE WHEN T2.GENERAL_FLG   = '1' THEN W3.EXAMCOURSE_NAME END AS GENERAL_DESIREDIV_NAME, "; //ADD
        $query .= "     CASE WHEN T2.GENERAL_FLG   = '1' THEN N5.NAME1           END AS GENERAL_SHDIV_NAME, "; //ADD
        $query .= "     CASE WHEN T2.SPORTS_FLG    = '1' THEN '有' END AS SPORTS, ";
        $query .= "     CASE WHEN T2.DORMITORY_FLG = '1' THEN '有' END AS DORMITORY, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     N2.NAME1 AS SHDIV_NAME, ";
        $query .= "     T2.SH_SCHOOLCD, ";
        $query .= "     F2.FINSCHOOL_NAME AS SH_SCHOOL_NAME, ";
        $query .= "     F2.FINSCHOOL_NAME_ABBV AS SH_SCHOOL_NAME_ABBV, ";
        $query .= "     CASE WHEN T2.RECOM_ITEM1 = '1' THEN '1' ELSE ' ' END AS RECOM_ITEM1, ";
        $query .= "     CASE WHEN T2.RECOM_ITEM2 = '1' THEN '2' ELSE ' ' END AS RECOM_ITEM2, ";
        $query .= "     CASE WHEN T2.RECOM_ITEM3 = '1' THEN '3' ELSE ' ' END AS RECOM_ITEM3, ";
        $query .= "     CASE WHEN T2.RECOM_ITEM4 = '1' THEN '4' ELSE ' ' END AS RECOM_ITEM4, ";
        $query .= "     T2.REMARK1, ";
        $query .= "     T2.REMARK2, ";
        $query .= "     W1.EXAMCOURSE_NAME  AS DESIREDIV_NAME, ";
        $query .= "     N1.NAME1 AS RECOM_KIND_NAME, ";
        $query .= "     SMALLINT(T4.AVERAGE_ALL) AS AVERAGE_ALL, ";
        $query .= "     T4.ABSENCE_DAYS  AS ABSENCE_DAYS1, ";
        $query .= "     T4.ABSENCE_DAYS2 AS ABSENCE_DAYS2, ";
        $query .= "     T4.ABSENCE_DAYS3 AS ABSENCE_DAYS3, ";
        $query .= "     T6.ACTIVITY, ";
        $query .= "     T6.SECTION, ";
        $query .= "     T6.RESULTS, ";
        //試験情報
        $query .= "     HALL.EXAMHALL_NAME, ";
        $query .= "     DETAIL.DETAIL_NO, ";
        $query .= "     SCORE.KOKUGO, ";
        $query .= "     SCORE.SUUGAKU, ";
        $query .= "     SCORE.KEISAN, ";
        $query .= "     SCORE.TEKISEI, ";
        $query .= "     SCORE.SHAKAI, ";
        $query .= "     SCORE.RIKA, ";
        $query .= "     SCORE.EIGO, ";
        $query .= "     SCORE.LISNING, ";
        $query .= "     SCORE.KATEN, ";
        $total = ""; //合計
        if ($model->field["APPLICANTDIV"] == "1") {
            $total = "RECEPT.TOTAL2"; //中学
        } elseif ($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV"] == "1") {
            $total = ($model->field["HAITEN"] == "2") ? "RECEPT.TOTAL1" : "RECEPT.TOTAL3"; //高校学特
        } elseif ($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV"] == "2") {
            $total = ($model->field["HAITEN"] == "2") ? "RECEPT.TOTAL2" : "RECEPT.TOTAL4"; //高校推薦
        } else {
            $total = ($model->field["HAITEN"] == "2") ? "RECEPT.TOTAL2" : "RECEPT.TOTAL4"; //高校一般
        }
        $query .= "     CASE WHEN RECEPT.ATTEND_ALL_FLG = '1' THEN {$total} END AS TOTAL, ";
        $query .= "     INTERVIEW.INTERVIEW_REMARK, ";
        $query .= "     INTERVIEW.INTERVIEW_VALUE, ";
        $query .= "     INTERVIEW.COMPOSITION_VALUE, ";
        $query .= "     T2.SUC_COURSECD, ";
        $query .= "     T2.SUC_MAJORCD, ";
        $query .= "     T2.SUC_COURSECODE, ";
        $query .= "     CASE WHEN N8.NAMESPARE1 = '1' THEN COURSE.EXAMCOURSE_NAME END AS SUC_COURSE_NAME, ";
        $query .= "     RECEPT.JUDGEDIV, ";
        $query .= "     N8.NAME1 AS JUDGEDIV_NAME, ";
        $query .= "     T2.JUDGE_KIND, ";
        $query .= "     N9.NAME1 AS JUDGE_KIND_NAME, ";
        $query .= "     T2.PROCEDUREDATE ";

        $query .= " FROM ";
        //志願者情報
        $query .= "     ENTEXAM_APPLICANTDESIRE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T4 ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T4.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT T5 ";
        $query .= "         ON  T5.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T5.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T5.TESTDIV      = '3' "; //高校一般入試、中学第二回一般入試
        $query .= "         AND T5.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTACTIVITY_DAT T6 ON T6.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T6.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN T_COURSE  W1 ON W1.WISHNO = '1' AND W1.DESIREDIV = T1.DESIREDIV ";
        $query .= "     LEFT JOIN T_COURSE  W2 ON W2.WISHNO = '2' AND W2.DESIREDIV = T1.DESIREDIV ";
        $query .= "     LEFT JOIN T_COURSE3 W3 ON W3.WISHNO = '1' AND W3.DESIREDIV = T5.DESIREDIV ";
        $query .= "     LEFT JOIN NAME_MST N0 ON N0.NAMECD1 = 'Z002' AND N0.NAMECD2 = T2.SEX ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L023' AND N1.NAMECD2 = T1.RECOM_KIND ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L006' AND N2.NAMECD2 = T1.SHDIV ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L016' AND N3.NAMECD2 = T2.FS_GRDDIV ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'H201' AND N4.NAMECD2 = T3.RELATIONSHIP ";
        $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'L006' AND N5.NAMECD2 = T5.SHDIV ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T2.FS_CD ";
        $query .= "     LEFT JOIN FINHIGHSCHOOL_MST F2 ON F2.FINSCHOOLCD = T2.SH_SCHOOLCD ";
        //試験情報
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "         ON  RECEPT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND RECEPT.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND RECEPT.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND RECEPT.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT HALL ";
        $query .= "         ON  HALL.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND HALL.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND HALL.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "         AND RECEPT.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_DETAIL_YDAT DETAIL ";
        $query .= "         ON  DETAIL.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND DETAIL.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND DETAIL.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "         AND RECEPT.RECEPTNO BETWEEN DETAIL.S_RECEPTNO AND DETAIL.E_RECEPTNO ";
        $query .= "     LEFT JOIN T_SCORE SCORE ON SCORE.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "     LEFT JOIN NAME_MST N8 ON N8.NAMECD1 = 'L013' AND N8.NAMECD2 = RECEPT.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "         ON  INTERVIEW.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND INTERVIEW.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "         AND INTERVIEW.TESTDIV       = RECEPT.TESTDIV ";
        $query .= "         AND INTERVIEW.EXAMNO        = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST COURSE ";
        $query .= "         ON  COURSE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND COURSE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND COURSE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "         AND COURSE.COURSECD     = T2.SUC_COURSECD ";
        $query .= "         AND COURSE.MAJORCD      = T2.SUC_MAJORCD ";
        $query .= "         AND COURSE.EXAMCOURSECD = T2.SUC_COURSECODE ";
        $query .= "     LEFT JOIN NAME_MST N9 ON N9.NAMECD1 = 'L025' AND N9.NAMECD2 = T2.JUDGE_KIND ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDET005 ";
        $query .= "         ON BDET005.ENTEXAMYEAR = T2.ENTEXAMYEAR AND BDET005.EXAMNO = T2.EXAMNO AND BDET005.SEQ = '005'";

        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        //過去の合格者を除く。(高校一般入試、中学第二回一般入試)
        if ($model->field["TESTDIV"] == "3") {
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT R1 ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT B2 ";
            $query .= "                ON  B2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
            $query .= "                AND B2.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "                AND B2.EXAMNO       = R1.EXAMNO ";
            $query .= "                AND VALUE(B2.SELECT_SUBCLASS_DIV,'0') <> '1' "; //1:特進チャレンジ受験者は対象外
            $query .= "             LEFT JOIN NAME_MST R2 ON R2.NAMECD1 = 'L013' AND R2.NAMECD2 = R1.JUDGEDIV ";
            $query .= "         WHERE ";
            $query .= "                 R1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
            $query .= "             AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "             AND R1.TESTDIV      < '{$model->field["TESTDIV"]}' ";
            $query .= "             AND R2.NAMESPARE1   = '1' ";
            $query .= "             AND R1.EXAMNO       = T1.EXAMNO ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.RECOM_KIND, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
