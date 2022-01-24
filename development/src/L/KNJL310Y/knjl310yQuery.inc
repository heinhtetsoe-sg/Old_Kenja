<?php

require_once('for_php7.php');

class knjl310yQuery extends Query
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

    //志望区分取得
    public function getDesireDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.DESIREDIV AS VALUE, ";
        $query .= "     T1.DESIREDIV || ':' || T2.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1, ";
        $query .= "     ENTEXAM_COURSE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV = T2.TESTDIV AND ";
        $query .= "     T1.TESTDIV = '".$model->field["TESTDIV"]."' AND ";
        $query .= "     T1.WISHNO = '1' AND ";
        $query .= "     T1.COURSECD = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD = T2.MAJORCD AND ";
        $query .= "     T1.EXAMCOURSECD = T2.EXAMCOURSECD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力
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

        $query .= " SELECT ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.RECOM_KIND, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     CASE WHEN T2.INTERVIEW_ATTEND_FLG = '1' THEN '帰' END AS INTERVIEW_ATTEND, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.SEX, ";
        $query .= "     N0.ABBV1 AS SEX_NAME, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     T2.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME, ";
        $query .= "     T2.FS_GRDDIV, ";
        $query .= "     N3.ABBV1 AS FS_GRDDIV_NAME, ";
        $query .= "     W1.EXAMCOURSE_NAME  AS EXAMCOURSE_NAME1, ";
        $query .= "     CASE WHEN T2.TESTDIV = '{$model->field["TESTDIV"]}' AND T2.SLIDE_FLG = '1' THEN W2.EXAMCOURSE_NAME END AS EXAMCOURSE_NAME2, ";
        $query .= "     T3.TELNO, ";
        $query .= "     T3.ZIPCD, ";
        $query .= "     VALUE(T3.ADDRESS1,'') || VALUE(T3.ADDRESS2,'') AS ADDRESS, ";
        $query .= "     T3.GNAME, ";
        $query .= "     T3.GKANA, ";
        $query .= "     T3.RELATIONSHIP, ";
        $query .= "     N4.NAME1 AS RELATIONSHIP_NAME, ";
        $query .= "     CASE WHEN T2.SHIFT_DESIRE_FLG   = '1' THEN '有' END AS SHIFT_DESIRE, ";
        $query .= "     CASE WHEN DT005.REMARK1    = '1' THEN '有' END AS GENERAL, ";
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
        $query .= "     N5.NAME1 AS TESTDIV_NAME, ";
        $query .= "     T4.AVERAGE5, ";
        $query .= "     SMALLINT(T4.AVERAGE_ALL) AS AVERAGE_ALL, ";
        $query .= "     T4.ABSENCE_DAYS AS ABSENCE_DAYS1, ";
        $query .= "     T4.ABSENCE_DAYS2, ";
        $query .= "     T4.ABSENCE_DAYS3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTDESIRE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T4 ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T4.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN T_COURSE W1 ON W1.WISHNO = '1' AND W1.DESIREDIV = T1.DESIREDIV ";
        $query .= "     LEFT JOIN T_COURSE W2 ON W2.WISHNO = '2' AND W2.DESIREDIV = T1.DESIREDIV ";
        $query .= "     LEFT JOIN NAME_MST N0 ON N0.NAMECD1 = 'Z002' AND N0.NAMECD2 = T2.SEX ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L023' AND N1.NAMECD2 = T1.RECOM_KIND ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L006' AND N2.NAMECD2 = T1.SHDIV ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L016' AND N3.NAMECD2 = T2.FS_GRDDIV ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'H201' AND N4.NAMECD2 = T3.RELATIONSHIP ";
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = '{$namecd1}' AND N5.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T2.FS_CD ";
        $query .= "     LEFT JOIN FINHIGHSCHOOL_MST F2 ON F2.FINSCHOOLCD = T2.SH_SCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DT005 ";
        $query .= "            ON DT005.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        //$query .= "           AND DT005.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "           AND DT005.EXAMNO       = T2.EXAMNO ";
        $query .= "           AND DT005.SEQ          = '005'  ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        //志望区分(9:全て)
        if ($model->field["DESIREDIV"] != "9") {
            $query .= "     AND T1.DESIREDIV    = '{$model->field["DESIREDIV"]}' ";
        }
        //過去の合格者を除く。(高校一般入試、中学第二回一般入試)
        if ($model->field["TESTDIV"] == "3") {
            $query .= "     AND NOT EXISTS ( ";
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             ENTEXAM_RECEPT_DAT R1 ";
            $query .= "             LEFT JOIN NAME_MST R2 ON R2.NAMECD1 = 'L013' AND R2.NAMECD2 = R1.JUDGEDIV ";
            $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DAT B2 ";
            $query .= "                 ON  B2.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
            $query .= "                 AND B2.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "                 AND B2.EXAMNO       = R1.EXAMNO ";
            $query .= "                 AND VALUE(B2.SELECT_SUBCLASS_DIV,'0') <> '1' "; //1:特進チャレンジ受験者は対象外
            $query .= "         WHERE ";
            $query .= "                 R1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
            $query .= "             AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "             AND R1.TESTDIV      < '{$model->field["TESTDIV"]}' ";
            $query .= "             AND R2.NAMESPARE1   = '1' ";
            $query .= "             AND R1.EXAMNO       = T1.EXAMNO ";
            $query .= "     ) ";
        }
        //特進再チャレンジ者のみ出力
        if ($model->field["OUTPUT_C"] == "1") {
            $query .= "     AND T2.SELECT_SUBCLASS_DIV = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.RECOM_KIND, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
