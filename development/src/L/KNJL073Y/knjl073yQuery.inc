<?php

require_once('for_php7.php');

class knjl073yQuery extends Query
{
    public function getMenuName()
    {
        $query  = "   SELECT ";
        $query .= "       MENUNAME ";
        $query .= "   FROM ";
        $query .= "       MENU_MST ";
        $query .= "   WHERE ";
        $query .= "       PROGRAMID = 'KNJL073Y' ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
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
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //CSV出力(志願者情報)
    public function getCsvQuery_j($model)
    {
        $query = "";
        if ($model->field["TESTDIV"] == "ALL") {
            $query .= " WITH T_TESTDIVS AS ( ";
            $query .= "     SELECT EXAMNO ";
            $query .= "          , TESTDIV ";
            $query .= "          , CASE TESTDIV WHEN '1' THEN 1 "; // 推薦
            $query .= "                         WHEN '2' THEN 2 "; // 第1回
            $query .= "                         WHEN '5' THEN 3 "; // 適性検査型
            $query .= "                         WHEN '3' THEN 4 "; // 第2回
            $query .= "                         WHEN '4' THEN 5 "; // 第3回
            $query .= "            END AS ORDER ";
            $query .= "     FROM ENTEXAM_RECEPT_DAT ";
            $query .= "     WHERE ENTEXAMYEAR = '{$model->ObjYear}' ";
            $query .= "       AND APPLICANTDIV = '1' ";
            $query .= " ) ";
            $query .= " , T_TESTDIV AS ( ";
            $query .= "     SELECT T1.EXAMNO ";
            $query .= "          , T1.TESTDIV ";
            $query .= "     FROM T_TESTDIVS T1 ";
            $query .= "     INNER JOIN (SELECT EXAMNO, MAX(ORDER) AS ORDER FROM T_TESTDIVS GROUP BY EXAMNO) T2 ON ";
            $query .= "         T2.EXAMNO = T1.EXAMNO ";
            $query .= "     AND T2.ORDER = T1.ORDER ";
            $query .= " ) ";
        }
        $query .= "   SELECT ";
        $query .= "       BASE.EXAMNO, ";
        $query .= "       RECE.TESTDIV, ";
        $query .= "       BASE.SHDIV, ";
        $query .= "       BASE.BIRTHDAY, ";
        $query .= "       RECE.JUDGEDIV AS JUDGEMENT ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       LEFT JOIN ENTEXAM_RECEPT_DAT RECE ON ";
        $query .= "           RECE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "       AND RECE.APPLICANTDIV = BASE.APPLICANTDIV ";
        if ($model->field["TESTDIV"] != "ALL") {
            $query .= "       AND RECE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        }
        $query .= "       AND RECE.EXAM_TYPE = '1' ";
        $query .= "       AND RECE.EXAMNO = BASE.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "       BASE.ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "       AND BASE.APPLICANTDIV  = '1' ";
        if ($model->field["TESTDIV"] == "ALL") {
            $query .= "       AND (RECE.EXAMNO, RECE.TESTDIV) IN (SELECT EXAMNO, TESTDIV FROM T_TESTDIV) ";
        } else {
            $query .= "       AND (BASE.TESTDIV       = '{$model->field["TESTDIV"]}' ";
            $query .= "         OR RECE.EXAMNO IS NOT NULL ";
            $query .= "           ) ";
        }
        $query .= " ORDER BY ";
        $query .= "       EXAMNO ";

        return $query;
    }

    //CSV出力(志願者情報)
    public function getCsvQuery_h($model)
    {
        $query = "";
        if ($model->field["TESTDIV"] == "ALL") {
            $query .= " WITH T_TESTDIVS AS ( ";
            $query .= "     SELECT EXAMNO ";
            $query .= "          , TESTDIV ";
            $query .= "          , CASE TESTDIV WHEN '1' THEN 2 "; // 学業特別奨学生
            $query .= "                         WHEN '2' THEN 1 "; // 推薦
            $query .= "                         WHEN '3' THEN 3 "; // 一般
            $query .= "            END AS ORDER ";
            $query .= "     FROM ENTEXAM_RECEPT_DAT ";
            $query .= "     WHERE ENTEXAMYEAR = '{$model->ObjYear}' ";
            $query .= "       AND APPLICANTDIV = '2' ";
            $query .= " ) ";
            $query .= " , T_TESTDIV AS ( ";
            $query .= "     SELECT T1.EXAMNO ";
            $query .= "          , T1.TESTDIV ";
            $query .= "     FROM T_TESTDIVS T1 ";
            $query .= "     INNER JOIN (SELECT EXAMNO, MAX(ORDER) AS ORDER FROM T_TESTDIVS GROUP BY EXAMNO) T2 ON ";
            $query .= "         T2.EXAMNO = T1.EXAMNO ";
            $query .= "     AND T2.ORDER = T1.ORDER ";
            $query .= " ) ";
            $query .= " , T_DESIREDIVS AS ( ";
            $query .= "     SELECT EXAMNO ";
            $query .= "          , TESTDIV ";
            $query .= "          , CASE TESTDIV WHEN '1' THEN 2 "; // 学業特別奨学生
            $query .= "                         WHEN '2' THEN 1 "; // 推薦
            $query .= "                         WHEN '3' THEN 3 "; // 一般
            $query .= "            END AS ORDER ";
            $query .= "     FROM ENTEXAM_APPLICANTDESIRE_DAT ";
            $query .= "     WHERE ENTEXAMYEAR = '{$model->ObjYear}' ";
            $query .= "       AND APPLICANTDIV = '2' ";
            $query .= " ) ";
            $query .= " , T_DESIREDIV AS ( ";
            $query .= "     SELECT T1.EXAMNO ";
            $query .= "          , T1.TESTDIV ";
            $query .= "     FROM T_DESIREDIVS T1 ";
            $query .= "     INNER JOIN (SELECT EXAMNO, MAX(ORDER) AS ORDER FROM T_DESIREDIVS GROUP BY EXAMNO) T2 ON ";
            $query .= "         T2.EXAMNO = T1.EXAMNO ";
            $query .= "     AND T2.ORDER = T1.ORDER ";
            $query .= " ) ";
        }
        $query .= "   SELECT ";
        $query .= "       BASE.EXAMNO, ";
        $query .= "       RECE.TESTDIV, ";
        $query .= "       DESIRE.SHDIV, ";
        $query .= "       BASE.BIRTHDAY, ";
        $query .= "       RECE.JUDGEDIV AS JUDGEMENT, ";
        $query .= "       DESIRE.DESIREDIV, ";  //志望区分
        $query .= "       DESIRE.RECOM_KIND, "; //推薦区分
        $query .= "       BASE.JUDGE_KIND, ";   //特奨
        $query .= "       BASE.INTERVIEW_ATTEND_FLG, ";
        $query .= "       BASE.SPORTS_FLG, ";
        $query .= "       BASE.SUC_COURSECD, ";
        $query .= "       BASE.SUC_MAJORCD, ";
        $query .= "       BASE.SUC_COURSECODE, ";
        $query .= "       BD005.REMARK1 AS SPORTS_FLG2, ";
        $query .= "       BASE.SELECT_SUBCLASS_DIV, "; //特進チャレンジフラグ
        $query .= "       BASE.SLIDE_FLG "; //スライド判定フラグ
        $query .= "   FROM ";
        $query .= "       ENTEXAM_APPLICANTBASE_DAT BASE";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "        ON BD005.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "       AND BD005.EXAMNO       = BASE.EXAMNO ";
        $query .= "       AND BD005.SEQ          = '005' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT DESIRE ";
        $query .= "        ON DESIRE.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "       AND DESIRE.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "       AND DESIRE.EXAMNO       = BASE.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DAT RECE ON ";
        $query .= "           RECE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "       AND RECE.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "       AND RECE.EXAM_TYPE = '1' ";
        $query .= "       AND RECE.EXAMNO = BASE.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "       BASE.ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "       AND BASE.APPLICANTDIV  = '2' ";
        if ($model->field["TESTDIV"] == "ALL") {
            $query .= "       AND (DESIRE.EXAMNO, DESIRE.TESTDIV) IN (SELECT EXAMNO, TESTDIV FROM T_DESIREDIV) ";
            $query .= "       AND (RECE.EXAMNO, RECE.TESTDIV) IN (SELECT EXAMNO, TESTDIV FROM T_TESTDIV) ";
        } else {
            $query .= "       AND DESIRE.TESTDIV = '{$model->field["TESTDIV"]}' ";
            $query .= "       AND RECE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        }

        $query .= " ORDER BY ";
        $query .= "       BASE.EXAMNO ";

        return $query;
    }
}
