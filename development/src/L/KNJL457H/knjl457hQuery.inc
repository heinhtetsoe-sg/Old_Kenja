<?php

require_once('for_php7.php');

class knjl457hquery extends Query
{
    public function getNameMst($model, $namecd1, $namecd2 = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->examYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $settingCd, $seq = '')
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND SETTING_CD   = '{$settingCd}' ";
        if ($seq != '') {
            $query .= "     AND SEQ = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    public function selectCsvQuery($model)
    {
        $query  = " WITH RENZOKU_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         EXAM_TYPE, ";
        $query .= "         RECEPTNO, ";
        $query .= "         (SELECT ";
        $query .= "              RECEPTNO ";
        $query .= "          FROM ";
        $query .= "              ENTEXAM_RECEPT_DAT R2 ";
        $query .= "          WHERE ";
        $query .= "              R2.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "              AND R2.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "              AND R2.EXAMNO       = R1.EXAMNO ";
        $query .= "              AND R2.TESTDIV      > R1.TESTDIV ";
        $query .= "          ORDER BY ";
        $query .= "              R2.TESTDIV ";
        $query .= "          FETCH FIRST 1 ROWS ONLY ";
        $query .= "         ) AS NEXT_RECEPTNO, ";
        $query .= "         (SELECT ";
        $query .= "              RECEPTNO ";
        $query .= "          FROM ";
        $query .= "              ENTEXAM_RECEPT_DAT R3 ";
        $query .= "          WHERE ";
        $query .= "              R3.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "              AND R3.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "              AND R3.EXAMNO       = R1.EXAMNO ";
        $query .= "          ORDER BY ";
        $query .= "              R3.TESTDIV ";
        $query .= "          FETCH FIRST 1 ROWS ONLY ";
        $query .= "         ) AS FIRST_RECEPTNO, ";
        $query .= "         (SELECT ";
        $query .= "              COUNT(RECEPTNO) ";
        $query .= "          FROM ";
        $query .= "              ENTEXAM_RECEPT_DAT R4 ";
        $query .= "          WHERE ";
        $query .= "              R4.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "              AND R4.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "              AND R4.EXAMNO       = R1.EXAMNO ";
        $query .= "         ) AS COUNT_RECEPTNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     WHERE ";
        $query .= "         R1.ENTEXAMYEAR      = '{$model->examYear}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY B1.EXAMNO,R1.TESTDIV) AS RENBAN, ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     FM1.FINSCHOOL_NAME, ";
        //奨学区分(SYOGAKUDIV) 1:成績優秀
        if ($model->field["SYOGAKUDIV"] == "1") {
            $query .= "     CASE ";
            $query .= "         WHEN RD019.REMARK7 = 1 THEN '全奨'  ";
            $query .= "         WHEN RD019.REMARK7 = 2 THEN '半奨' ";
            $query .= "         ELSE '' ";
            $query .= "     END AS SYOGAKU_HANTEI, ";
        //奨学区分(SYOGAKUDIV) 2:クラブ
        } else {
            $query .= "     CASE ";
            $query .= "         WHEN CD005.REMARK7 = 1 THEN '全奨'  ";
            $query .= "         WHEN CD005.REMARK7 = 2 THEN '半奨' ";
            $query .= "         ELSE '' ";
            $query .= "     END AS SYOGAKU_HANTEI, ";
        }
        $query .= "     CASE WHEN REN.COUNT_RECEPTNO = 1 ";
        $query .= "         THEN NULL ";
        $query .= "         ELSE ";
        $query .= "             CASE WHEN REN.NEXT_RECEPTNO IS NOT NULL ";
        $query .= "                 THEN REN.NEXT_RECEPTNO ";
        $query .= "                 ELSE REN.FIRST_RECEPTNO ";
        $query .= "             END ";
        $query .= "     END AS RENZOKU_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        //奨学区分(SYOGAKUDIV) 1:成績優秀
        if ($model->field["SYOGAKUDIV"] == "1") {
            $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD019 ";
            $query .= "          ON RD019.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
            $query .= "         AND RD019.APPLICANTDIV = R1.APPLICANTDIV ";
            $query .= "         AND RD019.TESTDIV      = R1.TESTDIV  ";
            $query .= "         AND RD019.EXAM_TYPE    = R1.EXAM_TYPE ";
            $query .= "         AND RD019.RECEPTNO     = R1.RECEPTNO ";
            $query .= "         AND RD019.SEQ          = '019' ";
            $query .= "         AND RD019.REMARK7 IN ('1','2') ";
        //奨学区分(SYOGAKUDIV) 2:クラブ
        } else {
            $query .= "     INNER JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD005 ";
            $query .= "          ON CD005.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "         AND CD005.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "         AND CD005.EXAMNO       = B1.EXAMNO ";
            $query .= "         AND CD005.SEQ          = '005' ";
            $query .= "         AND CD005.REMARK7 IN ('1','2') ";
        }
        $query .= "     LEFT JOIN NAME_MST Z002 ";
        $query .= "          ON Z002.NAMECD2 = B1.SEX ";
        $query .= "         AND Z002.NAMECD1 = 'Z002' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM1 ";
        $query .= "          ON FM1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN RENZOKU_DAT REN ";
        $query .= "          ON REN.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND REN.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND REN.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND REN.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND REN.RECEPTNO     = R1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["TESTDIV"] != "ALL") {
            $query .= "     AND R1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.RECEPTNO ";

        return $query;
    }
}
