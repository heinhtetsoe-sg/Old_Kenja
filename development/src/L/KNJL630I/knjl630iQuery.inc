<?php

require_once('for_php7.php');

class knjl630iQuery extends Query
{
    //入試区分マスタより取得
    public function getTestDivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV  = '2' "; //固定
        if ($testdiv != "") {
            $query .= "   AND TESTDIV = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //類別・コース別取得
    public function getGeneralMst($model, $generalCd, $remark1 = "")
    {
        $query .= " SELECT ";
        $query .= "     GENERAL_CD AS VALUE, ";
        $query .= "     GENERAL_CD || ':' || GENERAL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '2' AND "; //固定
        $query .= "     GENERAL_DIV = '{$generalCd}' AND ";
        $query .= "     TESTDIV = '0' "; //固定
        if ($generalCd == "02" && $remark1 == "") {
            $query .= " AND REMARK1 IN (";
            $query .= " SELECT ";
            $query .= " GENERAL_CD ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_GENERAL_MST ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
            $query .= "     APPLICANTDIV = '2' AND "; //固定
            
            if ($model->field["GAKKA"] != 'ALL') {
                $query .= "     REMARK1 = '{$model->field["GAKKA"]}' AND ";
            }
            $query .= "     GENERAL_DIV = '02') ";
        } else {
            if ($remark1 != 'ALL') {
                $query .= "   AND REMARK1 = '{$remark1}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特待・特待理由取得
    public function getGeneralMst2($model)
    {
        $query  = "   WITH SP_REASON_DATA AS ( ";
        $query .= "       SELECT ";
        $query .= "           ENTEXAMYEAR, ";
        $query .= "           APPLICANTDIV, ";
        $query .= "           TESTDIV, ";
        $query .= "           (CASE WHEN GENERAL_CD = '01' THEN '1' ELSE '2' END) AS SP_REASON_DIV "; //特待理由を1:学業、2:部活に類別する
        $query .= "       FROM ";
        $query .= "           ENTEXAM_GENERAL_MST ";
        $query .= "       WHERE ";
        $query .= "           ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "           AND APPLICANTDIV  = '2' "; //固定
        $query .= "           AND TESTDIV       = '0' "; //固定
        $query .= "           AND GENERAL_DIV   = '05' ";
        $query .= "   ) , SP_COMBO_DATA AS ( ";
        $query .= "   SELECT ";
        $query .= "       SP.SP_REASON_DIV , ";
        $query .= "       GEN.GENERAL_CD AS SP_CD, ";
        $query .= "       (CASE WHEN SP.SP_REASON_DIV = '1' THEN '学' ELSE '部' END) AS SP_REASON_DIV_NAME, ";
        $query .= "       GEN.GENERAL_NAME AS SP_CD_NAME ";
        $query .= "   FROM ";
        $query .= "       ( ";
        $query .= "           SELECT ";
        $query .= "               ENTEXAMYEAR, ";
        $query .= "               APPLICANTDIV, ";
        $query .= "               TESTDIV, ";
        $query .= "               SP_REASON_DIV ";
        $query .= "           FROM ";
        $query .= "               SP_REASON_DATA ";
        $query .= "           GROUP BY ";
        $query .= "               ENTEXAMYEAR, ";
        $query .= "               APPLICANTDIV, ";
        $query .= "               TESTDIV, ";
        $query .= "               SP_REASON_DIV ";
        $query .= "       ) SP ";
        $query .= "       LEFT JOIN ENTEXAM_GENERAL_MST GEN ";
        $query .= "           ON GEN.ENTEXAMYEAR    = SP.ENTEXAMYEAR ";
        $query .= "           AND GEN.APPLICANTDIV  = SP.APPLICANTDIV ";
        $query .= "           AND GEN.TESTDIV       = SP.TESTDIV ";
        $query .= "           AND GEN.GENERAL_DIV   = '04' ";
        $query .= "   ) ";
        $query .= "   SELECT ";
        $query .= "       SP_REASON_DIV || '-' || SP_CD AS VALUE, ";
        $query .= "       SP_REASON_DIV_NAME || SP_CD_NAME AS LABEL ";
        $query .= "   FROM ";
        $query .= "       SP_COMBO_DATA ";
        $query .= "   ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //会場取得
    public function getHallMst($model)
    {
        $query .= " SELECT ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '2' AND "; //固定
        $query .= "     TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "     EXAM_TYPE = '{$model->field["GAKKA"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $namecd1, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     SETTING_CD   = '{$namecd1}' ";
        if ($seq) {
            $query .= " AND SEQ = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    public function getPassList($model)
    {
        $query .= " SELECT ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   ROW_NUMBER() OVER(PARTITION BY BASE.TESTDIV0, BASE.TESTDIV ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ";
        } else {
            $query .= "   ROW_NUMBER() OVER(PARTITION BY BASE.TESTDIV0, BASE.TESTDIV ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ";
        }
        $query .= "   TEST.TESTDIV_NAME, ";
        $query .= "   CASE WHEN BASE.TESTDIV0 = '1' THEN '普通科' ELSE '工業科' END AS GAKKA, ";
        $query .= "   BASE.EXAMNO, ";
        $query .= "   GENE01.GENERAL_NAME AS PASSCOURSE, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   NAME.NAME2 AS SEX, ";
        $query .= "   BASE.FS_CD, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME_ABBV, ";
        $query .= "   GENE04.GENERAL_CD AS HONORDIV, ";
        $query .= "   GENE04.GENERAL_NAME AS SP_NAME, ";
        $query .= "   GENE05.GENERAL_MARK AS SP_REASON_MARK, ";
        $query .= "   CASE WHEN BASE.DORMITORY_FLG = '1' THEN SET042.NAME1 END AS RYO, ";
        $query .= "   ADDR.GNAME, ";
        $query .= "   ADDR.ZIPCD, ";
        $query .= "   ADDR.ADDRESS1 || ADDR.ADDRESS2 AS ADDRESS, ";
        $query .= "   ADDR.TELNO, ";
        $query .= "   CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ";
        $query .= "        WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ";
        $query .= "        WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ";
        $query .= "        ELSE NULL ";
        $query .= "   END AS DUPLICATE_FLG ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN ";
        $query .= "   V_NAME_MST NAME ON NAME.YEAR = BASE.ENTEXAMYEAR AND NAMECD1 = 'Z002' AND NAMECD2 = BASE.SEX ";
        $query .= " LEFT JOIN ";
        $query .= "   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV AND ADDR.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_SETTING_MST SET042 ON SET042.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND SET042.APPLICANTDIV = BASE.APPLICANTDIV AND SET042.SETTING_CD = 'L042' AND SET042.SEQ = BASE.SEX ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RD015.APPLICANTDIV = RPT.APPLICANTDIV AND RD015.TESTDIV = RPT.TESTDIV AND RD015.EXAM_TYPE = RPT.EXAM_TYPE AND RD015.RECEPTNO = RPT.RECEPTNO AND RD015.SEQ = '015' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE01 ON GENE01.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE01.APPLICANTDIV = BASE.APPLICANTDIV AND GENE01.TESTDIV = '0' AND GENE01.GENERAL_DIV = '01' AND GENE01.GENERAL_CD = RD015.REMARK3 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE04 ON GENE04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE04.APPLICANTDIV = BASE.APPLICANTDIV AND GENE04.TESTDIV = '0' AND GENE04.GENERAL_DIV = '04' AND GENE04.GENERAL_CD = RD015.REMARK4 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE05 ON GENE05.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE05.APPLICANTDIV = BASE.APPLICANTDIV AND GENE05.TESTDIV = '0' AND GENE05.GENERAL_DIV = '05' AND GENE05.GENERAL_CD = RD015.REMARK5 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["GAKKA"] != "ALL") {
            $query .= "   AND BASE.TESTDIV0 = '{$model->field["GAKKA"]}' ";
        }
        if ($model->field["RUIBETSU"]) {
            $query .= "   AND BD031.REMARK7 = '{$model->field["RUIBETSU"]}' ";
        }
        if ($model->field["COURSE"]) {
            $query .= "   AND BD031.REMARK1 = '{$model->field["COURSE"]}' ";
        }
        if ($model->s_examno && $model->e_examno) {
            $query .= "   AND BASE.EXAMNO BETWEEN $model->s_examno AND $model->e_examno ";
        } elseif ($model->s_examno) {
            $query .= "   AND BASE.EXAMNO >= $model->s_examno ";
        } elseif ($model->e_examno) {
            $query .= "   AND BASE.EXAMNO <= $model->e_examno ";
        }
        if ($model->field["SEX"] != '3') {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= "   AND RPT.JUDGEDIV = '1' ";
        
        return $query;
    }

    public function getfailList($model)
    {
        $query .= " SELECT ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   ROW_NUMBER() OVER(ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ";
        } else {
            $query .= "   ROW_NUMBER() OVER(ORDER BY BASE.TESTDIV0, BASE.TESTDIV, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ";
        }
        $query .= "   TEST.TESTDIV_NAME, ";
        $query .= "   CASE WHEN BASE.TESTDIV0 = '1' THEN '普通科' ELSE '工業科' END AS GAKKA, ";
        $query .= "   BASE.EXAMNO, ";
        $query .= "   BD031.REMARK1 AS ASPIRING1, ";
        $query .= "   VALUE(BD031.REMARK2, '0') AS ASPIRING2, ";
        $query .= "   VALUE(BD031.REMARK3, '0') AS ASPIRING3, ";
        $query .= "   VALUE(BD031.REMARK4, '0') AS ASPIRING4, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   NAME.NAME2 AS SEX, ";
        $query .= "   BASE.FS_CD, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME_ABBV, ";
        $query .= "   GENE04.GENERAL_CD AS HONORDIV, ";
        $query .= "   GENE04.GENERAL_NAME AS SP_NAME, ";
        $query .= "   GENE05.GENERAL_MARK AS SP_REASON_MARK, ";
        $query .= "   CASE WHEN BASE.DORMITORY_FLG = '1' THEN SET042.NAME1 END AS RYO, ";
        $query .= "   ADDR.GNAME, ";
        $query .= "   GENE03.GENERAL_MARK AS HANTEI, ";
        $query .= "   CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ";
        $query .= "        WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ";
        $query .= "        WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ";
        $query .= "        ELSE NULL ";
        $query .= "   END AS DUPLICATE_FLG ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN ";
        $query .= "   V_NAME_MST NAME ON NAME.YEAR = BASE.ENTEXAMYEAR AND NAMECD1 = 'Z002' AND NAMECD2 = BASE.SEX ";
        $query .= " LEFT JOIN ";
        $query .= "   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV AND ADDR.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_SETTING_MST SET042 ON SET042.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND SET042.APPLICANTDIV = BASE.APPLICANTDIV AND SET042.SETTING_CD = 'L042' AND SET042.SEQ = BASE.SEX ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RD015.APPLICANTDIV = RPT.APPLICANTDIV AND RD015.TESTDIV = RPT.TESTDIV AND RD015.EXAM_TYPE = RPT.EXAM_TYPE AND RD015.RECEPTNO = RPT.RECEPTNO AND RD015.SEQ = '015' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE03 ON GENE03.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE03.APPLICANTDIV = BASE.APPLICANTDIV AND GENE03.TESTDIV = '0' AND GENE03.GENERAL_DIV = '03' AND GENE03.GENERAL_CD = RD015.REMARK3 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE04 ON GENE04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE04.APPLICANTDIV = BASE.APPLICANTDIV AND GENE04.TESTDIV = '0' AND GENE04.GENERAL_DIV = '04' AND GENE04.GENERAL_CD = RD015.REMARK4 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE05 ON GENE05.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE05.APPLICANTDIV = BASE.APPLICANTDIV AND GENE05.TESTDIV = '0' AND GENE05.GENERAL_DIV = '05' AND GENE05.GENERAL_CD = RD015.REMARK5 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["GAKKA"] != "ALL") {
            $query .= "   AND BASE.TESTDIV0 = '{$model->field["GAKKA"]}' ";
        }
        if ($model->field["RUIBETSU"]) {
            $query .= "   AND BD031.REMARK7 = '{$model->field["RUIBETSU"]}' ";
        }
        if ($model->field["COURSE"]) {
            $query .= "   AND BD031.REMARK1 = '{$model->field["COURSE"]}' ";
        }
        if ($model->field["HALL"]) {
            $query .= "   AND HALL.EXAMHALLCD = '{$model->field["HALL"]}' ";
        }
        if ($model->s_examno && $model->e_examno) {
            $query .= "   AND BASE.EXAMNO BETWEEN $model->s_examno AND $model->e_examno ";
        } elseif ($model->s_examno) {
            $query .= "   AND BASE.EXAMNO >= $model->s_examno ";
        } elseif ($model->e_examno) {
            $query .= "   AND BASE.EXAMNO <= $model->e_examno ";
        }
        if ($model->field["SEX"] != '3') {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= "   AND (BASE.JUDGEMENT = '2' OR BASE.JUDGEMENT = '4')";
        
        return $query;
    }

    public function getPassNotice($model)
    {
        $query .= " SELECT ";
        $query .= "   BASE.EXAMNO, ";
        $query .= "   GENE02.REMARK3 AS PASSCOURSE, ";
        $query .= "   BASE.NAME, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN ";
        $query .= "   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RD015.APPLICANTDIV = RPT.APPLICANTDIV AND RD015.TESTDIV = RPT.TESTDIV AND RD015.EXAM_TYPE = RPT.EXAM_TYPE AND RD015.RECEPTNO = RPT.RECEPTNO AND RD015.SEQ = '015' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE03 ON GENE03.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE03.APPLICANTDIV = BASE.APPLICANTDIV AND GENE03.TESTDIV = '0' AND GENE03.GENERAL_DIV = '03' AND GENE03.GENERAL_CD = RD015.REMARK3 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GENE02 ON GENE02.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE02.APPLICANTDIV = BASE.APPLICANTDIV AND GENE02.TESTDIV = '0' AND GENE02.GENERAL_DIV = '02' AND GENE02.GENERAL_CD = GENE03.REMARK1 ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "   RPT.JUDGEDIV = '1' ";
        if ($model->field["GAKKA"] != "ALL") {
            $query .= "   AND BASE.TESTDIV0 = '{$model->field["GAKKA"]}' ";
        }
        if ($model->field["RUIBETSU"]) {
            $query .= "   AND BD031.REMARK7 = '{$model->field["RUIBETSU"]}' ";
        }
        if ($model->field["COURSE"]) {
            $query .= "   AND BD031.REMARK1 = '{$model->field["COURSE"]}' ";
        }
        if ($model->s_examno && $model->e_examno) {
            $query .= "   AND BASE.EXAMNO BETWEEN $model->s_examno AND $model->e_examno ";
        } elseif ($model->s_examno) {
            $query .= "   AND BASE.EXAMNO >= $model->s_examno ";
        } elseif ($model->e_examno) {
            $query .= "   AND BASE.EXAMNO <= $model->e_examno ";
        }
        if ($model->field["SEX"] != '3') {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        if ($model->field["SORT"] == '1') {
            $query .= "   ORDER BY BASE.EXAMNO, BASE.NAME_KANA ";
        } else {
            $query .= "   ORDER BY BASE.NAME_KANA, BASE.EXAMNO ";
        }
        return $query;
    }

    public function getSpecialNotice($model)
    {
        $query  = " WITH BASE_DATA AS ( ";
        $query .= "   SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     SCHOOL.FINSCHOOL_NAME, ";
        $query .= "     (CASE WHEN RD015.REMARK5 = '01' THEN '1' ELSE '2' END) AS SP_REASON_DIV, ";
        $query .= "     RD015.REMARK4 AS SP_CD ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "   LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= "   LEFT JOIN ";
        $query .= "     FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "   LEFT JOIN ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RD015.APPLICANTDIV = BASE.APPLICANTDIV AND RD015.RECEPTNO = BASE.EXAMNO AND RD015.SEQ = '015' ";
        $query .= "   WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     BASE.APPLICANTDIV = '2' AND ";
        $query .= "     BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->s_examno && $model->e_examno) {
            $query .= "   AND BASE.EXAMNO BETWEEN $model->s_examno AND $model->e_examno ";
        } elseif ($model->s_examno) {
            $query .= "   AND BASE.EXAMNO >= $model->s_examno ";
        } elseif ($model->e_examno) {
            $query .= "   AND BASE.EXAMNO <= $model->e_examno ";
        }
        $query .= " ) ";

        //メイン
        $query .= "   SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     NAME, ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= "   FROM ";
        $query .= "     BASE_DATA ";
        $query .= "   WHERE ";
        $query .= "     SP_REASON_DIV || '-' || SP_CD = '{$model->field["SPECIAL"]}' ";
        $query .= "   ORDER BY ";
        if ($model->field["SORT"] == '1') {
            $query .= "     EXAMNO, ";
            $query .= "     NAME_KANA ";
        } else {
            $query .= "     NAME_KANA, ";
            $query .= "     EXAMNO ";
        }

        return $query;
    }

    public function getExamStatus($model)
    {
        $query .= " WITH BASE AS (  ";
        $query .= " SELECT ";
        $query .= "   BASE.EXAMNO, ";
        $query .= "   BASE.TESTDIV, ";
        $query .= "   BASE.TESTDIV0, ";
        $query .= "   BD031.REMARK7, ";
        $query .= "   BD031.REMARK1, ";
        $query .= "   BASE.SEX, ";
        $query .= "   BASE.JUDGEMENT, ";
        $query .= "   RDT020.REMARK1 AS NYUGAKUKIN, ";
        $query .= "   RDT020.REMARK3 AS SETSUBIHI, ";
        $query .= "   BASE.PROCEDUREDIV,  ";
        $query .= "   BASE.ENTDIV  ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN  ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN  ";
        $query .= "   ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.RECEPTNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN  ";
        $query .= "   ENTEXAM_RECEPT_DETAIL_DAT RDT020 ON RDT020.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RDT020.APPLICANTDIV = RPT.APPLICANTDIV AND RDT020.TESTDIV = RPT.TESTDIV AND RDT020.RECEPTNO = RPT.RECEPTNO AND RDT020.SEQ = '020' ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV0 IS NOT NULL AND ";
        $query .= "   BD031.REMARK7 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "   BASE.EXAMNO ";
        $query .= " ), KEKKA AS ( ";
        if ($model->field["EXAM"] == "1") {
            $query .= " SELECT ";
            $query .= "   VALUE(TESTDIV0, '99') AS TESTDIV0, ";
            $query .= "   VALUE(TESTDIV, '99') AS TESTDIV, ";
            $query .= "   VALUE(SEX, '3') AS SEX, ";
            $query .= "   COUNT(EXAMNO) AS SHIGANSYA, ";
            $query .= "   COUNT(JUDGEMENT <> '4' OR JUDGEMENT IS NULL OR NULL) AS JUKENSYA, ";
            $query .= "   COUNT(JUDGEMENT = '1' OR NULL) AS PASS, ";
            $query .= "   COUNT(NYUGAKUKIN = '1' OR NULL) AS NYUGAKUKIN, ";
            $query .= "   COUNT(SETSUBIHI = '1' OR NULL) AS SETSUBIHI, ";
            $query .= "   COUNT(ENTDIV = '1' AND PROCEDUREDIV = '1' OR NULL) AS ENT ";
            $query .= " FROM BASE ";
            $query .= " GROUP BY ";
            $query .= "   GROUPING SETS ((TESTDIV0, TESTDIV, SEX),(TESTDIV0, TESTDIV), (TESTDIV, SEX), (TESTDIV0), (TESTDIV0, SEX), (TESTDIV), (SEX),()) ";
            $query .= " ORDER BY ";
            $query .= "   TESTDIV0,TESTDIV,SEX ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "   KEKKA.*, ";
            $query .= "   CASE WHEN KEKKA.TESTDIV0 = '1' THEN '普通科' WHEN KEKKA.TESTDIV0 = '2' THEN '工業科' ELSE '合計' END AS GAKKA, ";
            $query .= "   VALUE(TEST.TESTDIV_NAME, '計') AS TESTDIV_NAME, ";
            $query .= "   VALUE(NAME.NAME1, '計') AS SEX_NAME ";
            $query .= " FROM ";
            $query .= "   KEKKA ";
            $query .= " LEFT JOIN ";
            $query .= "   V_NAME_MST NAME ON NAME.YEAR = '{$model->ObjYear}' AND NAMECD1 = 'Z002' AND NAMECD2 = KEKKA.SEX ";
            $query .= " LEFT JOIN ";
            $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = '{$model->ObjYear}' AND TEST.APPLICANTDIV = '2' AND TEST.TESTDIV = KEKKA.TESTDIV ";
        } elseif ($model->field["EXAM"] == "2") {
            $query .= " SELECT ";
            $query .= "   VALUE(TESTDIV0, '99') AS TESTDIV0, ";
            $query .= "   VALUE(REMARK7, '99') AS REMARK7, ";
            $query .= "   VALUE(TESTDIV, '99') AS TESTDIV, ";
            $query .= "   VALUE(SEX, '3') AS SEX, ";
            $query .= "   COUNT(EXAMNO) AS SHIGANSYA, ";
            $query .= "   COUNT(JUDGEMENT <> '4' OR JUDGEMENT IS NULL OR NULL) AS JUKENSYA, ";
            $query .= "   COUNT(JUDGEMENT = '1' OR NULL) AS PASS, ";
            $query .= "   COUNT(NYUGAKUKIN = '1' OR NULL) AS NYUGAKUKIN, ";
            $query .= "   COUNT(SETSUBIHI = '1' OR NULL) AS SETSUBIHI, ";
            $query .= "   COUNT(ENTDIV = '1' AND PROCEDUREDIV = '1' OR NULL) AS ENT ";
            $query .= " FROM BASE ";
            $query .= " GROUP BY ";
            $query .= "   GROUPING SETS ((TESTDIV0,REMARK7, TESTDIV, SEX),(TESTDIV0,REMARK7, TESTDIV),(TESTDIV0,REMARK7),(TESTDIV0,REMARK7, SEX)) ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "   KEKKA.*, ";
            $query .= "   CASE WHEN KEKKA.TESTDIV0 = '1' THEN '普通科' WHEN KEKKA.TESTDIV0 = '2' THEN '工業科' ELSE '合計' END AS GAKKA, ";
            $query .= "   GENE01.GENERAL_NAME, ";
            $query .= "   VALUE(TEST.TESTDIV_NAME, '計') AS TESTDIV_NAME, ";
            $query .= "   VALUE(NAME.NAME1, '計') AS SEX_NAME ";
            $query .= " FROM ";
            $query .= "   KEKKA ";
            $query .= " LEFT JOIN ";
            $query .= "   V_NAME_MST NAME ON NAME.YEAR = '{$model->ObjYear}' AND NAMECD1 = 'Z002' AND NAMECD2 = KEKKA.SEX ";
            $query .= " LEFT JOIN ";
            $query .= "   ENTEXAM_GENERAL_MST GENE01 ON GENE01.ENTEXAMYEAR = '{$model->ObjYear}' AND GENE01.APPLICANTDIV = '2' AND GENE01.TESTDIV = '0' AND GENE01.GENERAL_DIV = '01' AND GENE01.GENERAL_CD = KEKKA.REMARK7 ";
            $query .= " LEFT JOIN ";
            $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = '{$model->ObjYear}' AND TEST.APPLICANTDIV = '2' AND TEST.TESTDIV = KEKKA.TESTDIV ";
            $query .= " ORDER BY ";
            $query .= "   TESTDIV0,REMARK7, TESTDIV,SEX ";
        } elseif ($model->field["EXAM"] == "3") {
            $query .= " SELECT ";
            $query .= "   VALUE(REMARK1, '99') AS REMARK1, ";
            $query .= "   VALUE(TESTDIV, '99') AS TESTDIV, ";
            $query .= "   VALUE(SEX, '3') AS SEX, ";
            $query .= "   COUNT(EXAMNO) AS SHIGANSYA, ";
            $query .= "   COUNT(JUDGEMENT <> '4' OR JUDGEMENT IS NULL OR NULL) AS JUKENSYA, ";
            $query .= "   COUNT(JUDGEMENT = '1' OR NULL) AS PASS, ";
            $query .= "   COUNT(NYUGAKUKIN = '1' OR NULL) AS NYUGAKUKIN, ";
            $query .= "   COUNT(SETSUBIHI = '1' OR NULL) AS SETSUBIHI, ";
            $query .= "   COUNT(ENTDIV = '1' AND PROCEDUREDIV = '1' OR NULL) AS ENT ";
            $query .= " FROM BASE ";
            $query .= " GROUP BY ";
            $query .= "   GROUPING SETS ((REMARK1, TESTDIV, SEX),(REMARK1, TESTDIV),(REMARK1),(REMARK1, SEX)) ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "   KEKKA.*, ";
            $query .= "   GENE02.GENERAL_ABBV, ";
            $query .= "   VALUE(TEST.TESTDIV_NAME, '計') AS TESTDIV_NAME, ";
            $query .= "   VALUE(NAME.NAME1, '計') AS SEX_NAME ";
            $query .= " FROM ";
            $query .= "   KEKKA ";
            $query .= " LEFT JOIN ";
            $query .= "   V_NAME_MST NAME ON NAME.YEAR = '{$model->ObjYear}' AND NAMECD1 = 'Z002' AND NAMECD2 = KEKKA.SEX ";
            $query .= " LEFT JOIN ";
            $query .= "   ENTEXAM_GENERAL_MST GENE02 ON GENE02.ENTEXAMYEAR = '{$model->ObjYear}' AND GENE02.APPLICANTDIV = '2' AND GENE02.TESTDIV = '0' AND GENE02.GENERAL_DIV = '02' AND GENE02.GENERAL_CD = KEKKA.REMARK1 ";
            $query .= " LEFT JOIN ";
            $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = '{$model->ObjYear}' AND TEST.APPLICANTDIV = '2' AND TEST.TESTDIV = KEKKA.TESTDIV ";
            $query .= " ORDER BY ";
            $query .= "   REMARK1, TESTDIV,SEX ";
        }

        return $query;
    }

    public function getNgList($model)
    {
        $query .= " SELECT ";
        $query .= "   BASE.TESTDIV, ";
        $query .= "   TEST.TESTDIV_NAME, ";
        $query .= "   BASE.FS_CD, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME, ";
        $query .= "   COUNT(BASE.JUDGEMENT <> '2' OR BASE.JUDGEMENT IS NULL OR NULL) AS JUKENSYA, ";
        $query .= "   COUNT(BASE.JUDGEMENT = '2' OR NULL) AS KESSEKI, ";
        $query .= "   COUNT(BASE.JUDGEMENT = '1' OR NULL) AS PASS ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " GROUP BY ";
        $query .= "   BASE.TESTDIV, TEST.TESTDIV_NAME, BASE.FS_CD, SCHOOL.FINSCHOOL_NAME ";
        $query .= " HAVING ";
        $query .= "   COUNT(BASE.JUDGEMENT = '1' OR NULL) = 0 ";

        return $query;
    }

    public function getResult($model)
    {
        $query .= " SELECT ";
        $query .= "   BASE.FS_CD, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME, ";
        $query .= "   BASE.TESTDIV, ";
        $query .= "   TEST.TESTDIV_NAME, ";
        $query .= "   TESTDT.REMARK2, ";
        $query .= "   SCHOOL.PRINCNAME, ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   ROW_NUMBER() OVER(PARTITION BY BASE.FS_CD, BASE.TESTDIV ORDER BY BASE.FS_CD, BASE.TESTDIV, BASE.EXAMNO) AS NO, ";
        } else {
            $query .= "   ROW_NUMBER() OVER(PARTITION BY BASE.FS_CD, BASE.TESTDIV ORDER BY BASE.FS_CD, BASE.TESTDIV, BASE.NAME_KANA) AS NO, ";
        }
        $query .= "   BASE.EXAMNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.JUDGEMENT, ";
        $query .= "   CASE ";
        $query .= "     WHEN BD031.REMARK1 = GEN03.REMARK1 THEN '第１志望' ";
        $query .= "     WHEN BD031.REMARK2 = GEN03.REMARK1 THEN '第２志望' ";
        $query .= "     WHEN BD031.REMARK3 = GEN03.REMARK1 THEN '第３志望' ";
        $query .= "     WHEN BD031.REMARK4 = GEN03.REMARK1 THEN '第４志望' ";
        $query .= "   ELSE '' END AS SHIBOU, ";
        $query .= "   CASE WHEN RPT.JUDGEDIV = '1' THEN '合格' ELSE '不合格' END AS GOUHI, ";
        $query .= "   CASE WHEN GEN01.REMARK1 = '1' THEN '普通科／' || GEN02.GENERAL_NAME  ELSE '工業科／' || GEN02.GENERAL_NAME END AS COURSE, ";
        $query .= "   GEN05.GENERAL_NAME || GEN04.GENERAL_MARK AS SPECIAL ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RD015.APPLICANTDIV = RPT.APPLICANTDIV AND RD015.TESTDIV = RPT.TESTDIV AND RD015.RECEPTNO = RPT.RECEPTNO AND RD015.SEQ = '015' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN03 ON GEN03.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GEN03.APPLICANTDIV = BASE.APPLICANTDIV AND GEN03.TESTDIV = '0' AND GEN03.GENERAL_DIV = '03' AND GEN03.GENERAL_CD = RD015.REMARK3 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN02 ON GEN02.ENTEXAMYEAR = GEN03.ENTEXAMYEAR AND GEN02.APPLICANTDIV = GEN03.APPLICANTDIV AND GEN02.TESTDIV = '0' AND GEN02.GENERAL_DIV = '02' AND GEN02.GENERAL_CD = GEN03.REMARK1 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN01 ON GEN01.ENTEXAMYEAR = GEN02.ENTEXAMYEAR AND GEN01.APPLICANTDIV = GEN02.APPLICANTDIV AND GEN01.TESTDIV = '0' AND GEN01.GENERAL_DIV = '01' AND GEN01.GENERAL_CD = GEN02.REMARK1 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN04 ON GEN04.ENTEXAMYEAR = RD015.ENTEXAMYEAR AND GEN04.APPLICANTDIV = RD015.APPLICANTDIV AND GEN04.TESTDIV = '0' AND GEN04.GENERAL_DIV = '04' AND GEN04.GENERAL_CD = RD015.REMARK4 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN05 ON GEN05.ENTEXAMYEAR = RD015.ENTEXAMYEAR AND GEN05.APPLICANTDIV = RD015.APPLICANTDIV AND GEN05.TESTDIV = '0' AND GEN05.GENERAL_DIV = '05' AND GEN05.GENERAL_CD = RD015.REMARK5 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST TESTDT ON TESTDT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TESTDT.APPLICANTDIV = BASE.APPLICANTDIV AND TESTDT.TESTDIV = BASE.TESTDIV AND TESTDT.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        
        return $query;
    }

    public function getResult2($model)
    {
        $query .= " WITH BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   BASE.FS_CD, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME, ";
        $query .= "   BASE.TESTDIV, ";
        $query .= "   TEST.TESTDIV_NAME, ";
        $query .= "   '1' AS KOTEI1, ";
        $query .= "   COUNT(RD015.REMARK3 IS NOT NULL OR NULL) AS SPECIAL, ";
        $query .= "   COUNT(BASE.JUDGEMENT = '1' OR NULL) AS PASS ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RD015.APPLICANTDIV = BASE.APPLICANTDIV AND RD015.TESTDIV = BASE.TESTDIV AND RD015.EXAM_TYPE = BASE.TESTDIV0 AND RD015.RECEPTNO = BASE.EXAMNO AND RD015.SEQ = '015'";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN01 ON GEN01.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GEN01.APPLICANTDIV = BASE.APPLICANTDIV AND GEN01.TESTDIV = '0' AND GEN01.GENERAL_DIV = '01' AND GEN01.GENERAL_CD = RD015.REMARK3 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN04 ON GEN04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GEN04.APPLICANTDIV = BASE.APPLICANTDIV AND GEN04.TESTDIV = '0' AND GEN04.GENERAL_DIV = '04' AND GEN04.GENERAL_CD = RD015.REMARK4 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " GROUP BY ";
        $query .= "   BASE.FS_CD, SCHOOL.FINSCHOOL_NAME, BASE.TESTDIV, TEST.TESTDIV_NAME ";
        $query .= " ORDER BY ";
        $query .= "   BASE.FS_CD, BASE.TESTDIV ";
        $query .= " )  ";
        $query .= " SELECT ";
        $query .= "   BASE.FS_CD, ";
        $query .= "   BASE.FINSCHOOL_NAME, ";
        $query .= "   TDL001.REMARK2, ";
        $query .= "   BASE.TESTDIV, ";
        $query .= "   BASE.TESTDIV_NAME, ";
        $query .= "   BASE.KOTEI1, ";
        $query .= "   CASE WHEN BASE.PASS > 0 THEN '1' ELSE '0' END AS SETSUBI, ";
        $query .= "   CASE WHEN BASE.SPECIAL > 0 THEN '1' ELSE '0' END AS SPECIAL, ";
        $query .= "   BASE.PASS, ";
        $query .= "   TDL003.REMARK1 AS SCHEDULE1_1, ";
        $query .= "   TDL003.REMARK2 AS SCHEDULE1_2, ";
        $query .= "   TDL003.REMARK3 AS SCHEDULE1_3, ";
        $query .= "   TDL003.REMARK4 AS SCHEDULE1_4, ";
        $query .= "   TDL003.REMARK5 AS SCHEDULE1_5, ";
        $query .= "   TDL003.REMARK6 AS SCHEDULE1_6, ";
        $query .= "   TDL004.REMARK1 AS SCHEDULE2_1, ";
        $query .= "   TDL004.REMARK2 AS SCHEDULE2_2, ";
        $query .= "   TDL004.REMARK3 AS SCHEDULE2_3, ";
        $query .= "   TDL004.REMARK4 AS SCHEDULE2_4, ";
        $query .= "   TDL004.REMARK5 AS SCHEDULE2_5, ";
        $query .= "   TDL004.REMARK6 AS SCHEDULE2_6, ";
        $query .= "   TDL005.REMARK1 AS SCHEDULE3_1, ";
        $query .= "   TDL005.REMARK2 AS SCHEDULE3_2, ";
        $query .= "   TDL005.REMARK3 AS SCHEDULE3_3, ";
        $query .= "   TDL005.REMARK4 AS SCHEDULE3_4, ";
        $query .= "   TDL005.REMARK5 AS SCHEDULE3_5, ";
        $query .= "   TDL005.REMARK6 AS SCHEDULE3_6, ";
        $query .= "   TDL006.REMARK1 AS SCHEDULE4_1, ";
        $query .= "   TDL006.REMARK2 AS SCHEDULE4_2, ";
        $query .= "   TDL006.REMARK3 AS SCHEDULE4_3, ";
        $query .= "   TDL006.REMARK4 AS SCHEDULE4_4, ";
        $query .= "   TDL006.REMARK5 AS SCHEDULE4_5, ";
        $query .= "   TDL006.REMARK6 AS SCHEDULE4_6 ";
        $query .= " FROM BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL001 ON TDL001.ENTEXAMYEAR = '{$model->ObjYear}' AND TDL001.APPLICANTDIV = '2' AND TDL001.TESTDIV = BASE.TESTDIV AND TDL001.SEQ = '001' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL003 ON TDL003.ENTEXAMYEAR = '{$model->ObjYear}' AND TDL003.APPLICANTDIV = '2' AND TDL003.TESTDIV = BASE.TESTDIV AND TDL003.SEQ = '003' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL004 ON TDL004.ENTEXAMYEAR = '{$model->ObjYear}' AND TDL004.APPLICANTDIV = '2' AND TDL004.TESTDIV = BASE.TESTDIV AND TDL004.SEQ = '004' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL005 ON TDL005.ENTEXAMYEAR = '{$model->ObjYear}' AND TDL005.APPLICANTDIV = '2' AND TDL005.TESTDIV = BASE.TESTDIV AND TDL005.SEQ = '005' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL006 ON TDL006.ENTEXAMYEAR = '{$model->ObjYear}' AND TDL006.APPLICANTDIV = '2' AND TDL006.TESTDIV = BASE.TESTDIV AND TDL006.SEQ = '006' ";

        return $query;
    }

    public function getFurikomi($model)
    {
        $query .= " SELECT ";
        $query .= "   BASE.TESTDIV, ";
        $query .= "   TEST.TESTDIV_NAME, ";
        $query .= "   BASE.EXAMNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   TDL001.REMARK4, ";
        $query .= "   TDL001.REMARK5, ";
        $query .= "   TDL001.REMARK7, ";
        $query .= "   TDL001.REMARK8, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME, ";
        $query .= "   CASE WHEN RD015.REMARK4 IS NULL THEN TDL001.REMARK3 ELSE GEN04.REMARK1 END AS NYUGAKU, ";
        $query .= "   CASE WHEN RD015.REMARK4 IS NULL THEN TDL001.REMARK6 ELSE GEN04.REMARK2 END AS SETSUBI, ";
        $query .= "   GEN04.GENERAL_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_DETAIL_SEQ_MST TDL001 ON TDL001.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TDL001.APPLICANTDIV = BASE.APPLICANTDIV AND TDL001.TESTDIV = BASE.TESTDIV AND TDL001.SEQ = '001' ";
        $query .= " LEFT JOIN ";
        $query .= "   FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = RPT.APPLICANTDIV AND RPT.TESTDIV = BASE.TESTDIV AND RPT.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_RECEPT_DETAIL_DAT RD015 ON RD015.ENTEXAMYEAR = RPT.ENTEXAMYEAR AND RD015.APPLICANTDIV = RPT.APPLICANTDIV AND RD015.TESTDIV = RPT.TESTDIV AND RD015.RECEPTNO = RPT.RECEPTNO AND RD015.SEQ = '015' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GEN04 ON GEN04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GEN04.APPLICANTDIV = BASE.APPLICANTDIV AND GEN04.TESTDIV = '0' AND GEN04.GENERAL_DIV = '04' AND GEN04.GENERAL_CD = RD015.REMARK4 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   RPT.JUDGEDIV = '1' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["GAKKA"] != "ALL") {
            $query .= "    AND BASE.TESTDIV0 = '{$model->field["GAKKA"]}'  ";
        }
        if ($model->s_examno && $model->e_examno) {
            $query .= "   AND BASE.EXAMNO BETWEEN $model->s_examno AND $model->e_examno ";
        } elseif ($model->s_examno) {
            $query .= "   AND BASE.EXAMNO >= $model->s_examno ";
        } elseif ($model->e_examno) {
            $query .= "   AND BASE.EXAMNO <= $model->e_examno ";
        }
        $query .= " ORDER BY ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   BASE.EXAMNO, BASE.NAME_KANA ";
        } else {
            $query .= "   BASE.NAME_KANA, BASE.EXAMNO ";
        }
        


        return $query;
    }

    //試験科目一覧取得クエリを取得
    public function getTestClassesListQuery()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L009' ";
        $query .= "     AND VALUE(NAME1,'') <> '' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
