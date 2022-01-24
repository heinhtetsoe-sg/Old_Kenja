<?php

require_once('for_php7.php');

class knjl295yQuery extends Query
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
        $query .= " SELECT ";
        //共通
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     N1.ABBV1 AS SEX_NAME, ";
        if ($model->field["OUTPUT"] == "1") {
            $query .= "     T1.BIRTHDAY, ";
        }
        $query .= "     T1.FS_NAME, ";
        //志願者情報
        if ($model->field["OUTPUT"] == "1") {
            $query .= "     T3.TELNO, ";
            $query .= "     T3.ZIPCD, ";
            $query .= "     VALUE(T3.ADDRESS1,'') || VALUE(T3.ADDRESS2,'') AS ADDRESS, ";
            $query .= "     T3.GNAME, ";
            $query .= "     T3.GKANA, ";
            $query .= "     N2.NAME1 AS RELATIONSHIP_NAME, ";
            $query .= "     T11.REMARK1, ";
            $query .= "     T11.REMARK2, ";
            $query .= "     T11.REMARK3, ";
            $query .= "     T11.REMARK4, ";
        //試験情報
        } else {
            $query .= "     S11.SCORE AS SCORE1, ";
            $query .= "     S12.SCORE AS SCORE2, ";
            $query .= "     S13.SCORE AS SCORE3, ";
            $query .= "     S14.SCORE AS SCORE4, ";
            //傾斜得点
            $query .= "     R1.TOTAL2, ";
            $query .= "     R1.TOTAL_RANK2, ";
            $query .= "     R1.TOTAL4, ";
            $query .= "     R1.TOTAL_RANK4, ";
            //通常得点
            $query .= "     R1.TOTAL1, ";
            $query .= "     R1.TOTAL_RANK1, ";
            $query .= "     R1.TOTAL3, ";
            $query .= "     R1.TOTAL_RANK3, ";
            
            $query .= "     N4.NAMESPARE1 AS INTERVIEW_VALUE2_NAME, ";
            $query .= "     S2.INTERVIEW_REMARK2, ";
            $query .= "     S2.INTERVIEW_REMARK, ";
            $query .= "     S2.INTERVIEW_REMARK3, ";
            $query .= "     T1.JUDGEMENT, ";
            $query .= "     T1.PROCEDUREDATE, ";
        }
        $query .= "     N3.NAME1 AS TESTDIV_NAME ";//テスト名セット用
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        //志願者情報
        if ($model->field["OUTPUT"] == "1") {
            $query .= "     LEFT JOIN ENTEXAM_APPLICANT_CHARACTER_DAT T11 ON T11.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T11.EXAMNO = T1.EXAMNO ";
            $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ";
            $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'H201' AND N2.NAMECD2 = T3.RELATIONSHIP ";
        //試験情報
        } else {
            $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "                                    AND R1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                                    AND R1.TESTDIV = T1.TESTDIV ";
            $query .= "                                    AND R1.EXAM_TYPE = '1' ";
            $query .= "                                    AND R1.RECEPTNO = T1.EXAMNO ";
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S11 ON S11.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                    AND S11.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                                    AND S11.TESTDIV = T1.TESTDIV ";
            $query .= "                                    AND S11.EXAM_TYPE = '1' ";
            $query .= "                                    AND S11.RECEPTNO = T1.EXAMNO ";
            $query .= "                                    AND S11.TESTSUBCLASSCD = '1' ";
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S12 ON S12.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                    AND S12.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                                    AND S12.TESTDIV = T1.TESTDIV ";
            $query .= "                                    AND S12.EXAM_TYPE = '1' ";
            $query .= "                                    AND S12.RECEPTNO = T1.EXAMNO ";
            $query .= "                                    AND S12.TESTSUBCLASSCD = '2' ";
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S13 ON S13.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                    AND S13.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                                    AND S13.TESTDIV = T1.TESTDIV ";
            $query .= "                                    AND S13.EXAM_TYPE = '1' ";
            $query .= "                                    AND S13.RECEPTNO = T1.EXAMNO ";
            $query .= "                                    AND S13.TESTSUBCLASSCD = '3' ";
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S14 ON S14.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                    AND S14.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                                    AND S14.TESTDIV = T1.TESTDIV ";
            $query .= "                                    AND S14.EXAM_TYPE = '1' ";
            $query .= "                                    AND S14.RECEPTNO = T1.EXAMNO ";
            $query .= "                                    AND S14.TESTSUBCLASSCD = '4' ";
            $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT S2 ON S2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                       AND S2.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "                                       AND S2.TESTDIV = T1.TESTDIV ";
            $query .= "                                       AND S2.EXAMNO = T1.EXAMNO ";
            $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'L030' AND N4.NAMECD2 = S2.INTERVIEW_VALUE2 ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L004' AND N3.NAMECD2 = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["TESTDIV"] == "1") {
            $query .= "     AND T1.TESTDIV1      = '{$model->field["TESTDIV"]}' ";
        } elseif ($model->field["TESTDIV"] == "2") {
            $query .= "     AND T1.TESTDIV2      = '{$model->field["TESTDIV"]}' ";
        } else {
            $query .= "     AND T1.TESTDIV3      = '{$model->field["TESTDIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
    
    //家族・備考のデータ取得
    public function getFamilyCsvDataQuery($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAME AS FAMILY_NAME, ";
        $query .= "     T1.AGE, ";
        $query .= "     N2.NAME1 AS FAMILY_RELATIONSHIP_NAME, ";
        $query .= "     T1.REMARK AS FAMILY_REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_FAMILY_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'H201' AND N2.NAMECD2 = T1.RELATIONSHIP ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND T1.EXAMNO = '{$examno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
}
