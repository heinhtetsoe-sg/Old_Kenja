<?php

require_once('for_php7.php');

class knjl610iQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAME2, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

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
    public function getGeneralMst($model, $generalCd, $remark1)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_CD AS VALUE, ";
        $query .= "     GENERAL_CD || ':' || GENERAL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '2' AND "; //固定
        $query .= "     GENERAL_DIV = '{$generalCd}' AND ";
        $query .= "     TESTDIV = '0' AND "; //固定
        $query .= "     REMARK1 = '{$remark1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    public function getHallMst($model)
    {
        $query  = " SELECT ";
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

    public function getApplicantListCheck($model)
    {
        $query  = " SELECT ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY BASE.EXAMNO, BASE.NAME_KANA) AS NO, ";
        } else {
            $query .= "   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY BASE.NAME_KANA, BASE.EXAMNO) AS NO, ";
        }
        $query .= "   CASE WHEN BASE.TESTDIV0 = '1' THEN '普通科' ELSE '工業科' END AS GAKKA, ";
        $query .= "   BASE.EXAMNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   NAME.NAME2 AS SEX, ";
        $query .= "   REPLACE(BASE.BIRTHDAY, '-' ,'') AS BIRTHDAY, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME_ABBV, ";
        $query .= "   HALL.S_RECEPTNO, ";
        $query .= "   HALL.E_RECEPTNO, ";
        $query .= "   HALL.EXAMHALLCD, ";
        $query .= "   HALL.EXAMHALL_NAME, ";
        $query .= "   (SELECT ";
        $query .= "       COUNT(T2.EXAMHALLCD) ";
        $query .= "     FROM ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT T2 ";
        $query .= "        ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "       AND T1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "       AND T1.TESTDIV = T1.TESTDIV ";
        $query .= "       AND T1.EXAMNO BETWEEN T2.S_RECEPTNO AND T2.E_RECEPTNO ";
        $query .= "     WHERE ";
        $query .= "       T1.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "       T1.APPLICANTDIV = '2' AND ";
        $query .= "       T1.TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "       T1.TESTDIV0 = '{$model->field["GAKKA"]}' AND ";
        $query .= "       HALL.EXAMHALLCD = T2.EXAMHALLCD ";
        $query .= "     GROUP BY ";
        $query .= "       T2.EXAMHALLCD) AS NUM, ";
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
        $query .= "   ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND HALL.APPLICANTDIV = BASE.APPLICANTDIV AND HALL.TESTDIV = BASE.TESTDIV AND HALL.EXAM_TYPE = BASE.TESTDIV0 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "   BASE.TESTDIV0 = '{$model->field["GAKKA"]}' ";
        if ($model->field["RUIBETSU"]) {
            $query .= "   AND BD031.REMARK7 = '{$model->field["RUIBETSU"]}' ";
        }
        if ($model->field["COURSE"]) {
            $query .= "   AND BD031.REMARK1 = '{$model->field["COURSE"]}' ";
        }
        if ($model->field["HALL"]) {
            $query .= "   AND HALL.EXAMHALLCD = '{$model->field["HALL"]}' ";
        }
        if ($model->field["SEX"] != '3') {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        $query .= "   AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        
        return $query;
    }

    public function getApplicantList($model)
    {
        $query  = " SELECT ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   ROW_NUMBER() OVER(ORDER BY BASE.EXAMNO, BASE.NAME_KANA) AS NO, ";
        } else {
            $query .= "   ROW_NUMBER() OVER(ORDER BY BASE.NAME_KANA, BASE.EXAMNO) AS NO, ";
        }
        $query .= "     TEST.TESTDIV_NAME, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BD031.REMARK1 AS ASPIRING1, ";
        $query .= "     VALUE(BD031.REMARK2, '0') AS ASPIRING2, ";
        $query .= "     VALUE(BD031.REMARK3, '0') AS ASPIRING3, ";
        $query .= "     VALUE(BD031.REMARK4, '0') AS ASPIRING4, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     '' AS NAME_HIRA, "; //枠だけ
        $query .= "     NAME.NAME2 AS SEX, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     SCHOOL.FINSCHOOL_NAME_ABBV, ";
        $query .= "     CD009.REMARK2, ";
        $query .= "     GENE04.GENERAL_MARK, ";
        $query .= "     GENE05.GENERAL_MARK AS SP_REASON, ";
        $query .= "     CASE WHEN BD031.REMARK6 = '1' THEN '有' END AS SHOGAKU, ";
        $query .= "     CASE WHEN BASE.DORMITORY_FLG = '1' THEN SET042.NAME1 END AS RYO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ";
        $query .= "          WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ";
        $query .= "          WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ";
        $query .= "          ELSE NULL ";
        $query .= "     END AS DUPLICATE_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " LEFT JOIN  ";
        $query .= "     V_NAME_MST NAME ON NAME.YEAR = BASE.ENTEXAMYEAR AND NAMECD1 = 'Z002' AND NAMECD2 = BASE.SEX  ";
        $query .= " LEFT JOIN ";
        $query .= "     FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD  ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND HALL.APPLICANTDIV = BASE.APPLICANTDIV AND HALL.TESTDIV = BASE.TESTDIV AND HALL.EXAM_TYPE = BASE.TESTDIV0 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_TESTDIV_MST TEST ON TEST.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND TEST.APPLICANTDIV = BASE.APPLICANTDIV AND TEST.TESTDIV = BASE.TESTDIV ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD009 ON CD009.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CD009.APPLICANTDIV = BASE.APPLICANTDIV AND CD009.EXAMNO = BASE.EXAMNO AND CD009.SEQ = '009' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV AND ADDR.EXAMNO = BASE.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_SETTING_MST SET042 ON SET042.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND SET042.APPLICANTDIV = BASE.APPLICANTDIV AND SET042.SETTING_CD = 'L042' AND SET042.SEQ = BASE.SEX ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_GENERAL_MST GENE04 ON GENE04.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE04.APPLICANTDIV = BASE.APPLICANTDIV AND GENE04.TESTDIV = '0' AND GENE04.GENERAL_DIV = '04' AND GENE04.GENERAL_CD = CD009.REMARK2 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_GENERAL_MST GENE05 ON GENE05.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND GENE05.APPLICANTDIV = BASE.APPLICANTDIV AND GENE05.TESTDIV = '0' AND GENE05.GENERAL_DIV = '05' AND GENE05.GENERAL_CD = CD009.REMARK3 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ";
        $query .= " WHERE ";
        $query .= "   BASE.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   BASE.APPLICANTDIV = '2' AND ";
        $query .= "   BASE.TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "   BASE.TESTDIV0 = '{$model->field["GAKKA"]}' ";
        if ($model->field["RUIBETSU"]) {
            $query .= "   AND BD031.REMARK7 = '{$model->field["RUIBETSU"]}' ";
        }
        if ($model->field["COURSE"]) {
            $query .= "   AND BD031.REMARK1 = '{$model->field["COURSE"]}' ";
        }
        if ($model->field["HALL"]) {
            $query .= "   AND HALL.EXAMHALLCD = '{$model->field["HALL"]}' ";
        }
        if ($model->field["SEX"] != '3') {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        if ($model->field["SPECIAL"] == '2') {
            $query .= "   AND CD009.REMARK3 = '01' "; //特待生・学業
        } elseif ($model->field["SPECIAL"] == '3') {
            $query .= "   AND CD009.REMARK3 != '01' "; //特待生・部活動
        }

        $query .= "   AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";

        return $query;
    }

    public function getAttendance($model)
    {
        $query  = " SELECT ";
        if ("1" == $model->field["SORT"]) {
            if ($model->field["TESTSUBCLASSCD_CHECK"] == "1") {
                $query .= "   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, L009.SEQ ORDER BY HALL.EXAMHALLCD, L009.SEQ, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ";
            } else {
                $query .= "   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY HALL.EXAMHALLCD, BASE.EXAMNO, BASE.NAME_KANA) AS NO, ";
            }
        } else {
            if ($model->field["TESTSUBCLASSCD_CHECK"] == "1") {
                $query .= "   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, L009.SEQ ORDER BY HALL.EXAMHALLCD, L009.SEQ, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ";
            } else {
                $query .= "   ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD ORDER BY HALL.EXAMHALLCD, BASE.NAME_KANA, BASE.EXAMNO) AS NO, ";
            }
        }
        $query .= "   HALL.EXAMHALL_NAME, ";
        if ($model->field["TESTSUBCLASSCD_CHECK"] == "1") {
            $query .= "   L009.SETTING_CD AS SUBCLASSCD, ";
            $query .= "   L009.NAME1 AS SUBCLASS_NAME, ";
        }
        $query .= "   BASE.EXAMNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME_ABBV, ";
        $query .= "   HALL.EXAMHALLCD, ";
        $query .= "     CASE WHEN BASE_D012.REMARK1 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK1 THEN '1' ";
        $query .= "          WHEN BASE_D012.REMARK2 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK2 THEN '1' ";
        $query .= "          WHEN BASE_D012.REMARK3 IS NOT NULL AND BASE.EXAMNO <> BASE_D012.REMARK3 THEN '1' ";
        $query .= "          ELSE NULL ";
        $query .= "   END AS DUPLICATE_FLG ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HALL_YDAT HALL ";
        if ($model->field["TESTSUBCLASSCD_CHECK"] == "1") {
            $query .= " INNER JOIN ( ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK1 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK2 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK3 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK4 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK5 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK6 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK7 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK8 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= "   UNION ALL ";
            $query .= "   SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, SEQ, REMARK9 AS REMARK FROM ENTEXAM_TESTDIV_DETAIL_SEQ_MST ";
            $query .= " ) TD002 ON TD002.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND TD002.APPLICANTDIV = HALL.APPLICANTDIV AND TD002.TESTDIV = HALL.TESTDIV AND TD002.SEQ = '002' AND TD002.REMARK IS NOT NULL ";
            $query .= " INNER JOIN ";
            $query .= "   ENTEXAM_SETTING_MST L009 ON L009.ENTEXAMYEAR = TD002.ENTEXAMYEAR AND L009.APPLICANTDIV = TD002.APPLICANTDIV AND L009.SETTING_CD = 'L009' AND L009.SEQ = TD002.REMARK ";
        }
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND BASE.APPLICANTDIV = HALL.APPLICANTDIV AND BASE.TESTDIV = HALL.TESTDIV AND BASE.TESTDIV0 = HALL.EXAM_TYPE AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= " LEFT JOIN ";
        $query .= "     FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD  ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON BASE_D012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR AND BASE_D012.APPLICANTDIV = BASE.APPLICANTDIV AND BASE_D012.EXAMNO = BASE.EXAMNO AND BASE_D012.SEQ = '012' ";
        $query .= " WHERE ";
        $query .= "   HALL.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   HALL.APPLICANTDIV = '2' AND ";
        $query .= "   HALL.TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "   HALL.EXAM_TYPE = '{$model->field["GAKKA"]}' AND ";
        $query .= "   BASE.EXAMNO IS NOT NULL ";
        if ($model->field["HALL"]) {
            $query .= "   AND HALL.EXAMHALLCD = '{$model->field["HALL"]}' ";
        }
        if ($model->field["SEX"] != '3') {
            $query .= "   AND BASE.SEX = '{$model->field["SEX"]}' ";
        }
        return $query;
    }

    public function getScore($model)
    {
        $query  = " SELECT ";
        $query .= "   HALL.EXAMHALL_NAME, ";
        $query .= "   SET009.NAME1 AS SUBCLASS_NAME, ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   CASE WHEN BASE.EXAMNO IS NOT NULL THEN ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, SET009.SEQ ORDER BY HALL.EXAMHALLCD, SET009.SEQ, BASE.EXAMNO, BASE.NAME_KANA) END AS NO, ";
        } else {
            $query .= "   CASE WHEN BASE.EXAMNO IS NOT NULL THEN ROW_NUMBER() OVER(PARTITION BY HALL.EXAMHALLCD, SET009.SEQ ORDER BY HALL.EXAMHALLCD, SET009.SEQ, BASE.NAME_KANA, BASE.EXAMNO) END AS NO, ";
        }
        $query .= "   BASE.EXAMNO, ";
        $query .= "   HALL.S_RECEPTNO, ";
        $query .= "   HALL.E_RECEPTNO, ";
        $query .= "   HALL.EXAMHALLCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT HALL  ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND BASE.APPLICANTDIV = HALL.APPLICANTDIV AND BASE.TESTDIV = HALL.TESTDIV AND BASE.TESTDIV0 = HALL.EXAM_TYPE AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_SETTING_MST SET009 ON SET009.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND SET009.APPLICANTDIV = HALL.APPLICANTDIV AND SET009.SETTING_CD = 'L009' ";
        $query .= " WHERE ";
        $query .= "   HALL.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   HALL.APPLICANTDIV = '2' AND ";
        $query .= "   HALL.TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "   HALL.EXAM_TYPE = '{$model->field["GAKKA"]}' AND ";
        $query .= "   BASE.EXAMNO IS NOT NULL ";
        if ($model->field["HALL"]) {
            $query .= "   AND HALL.EXAMHALLCD = '{$model->field["HALL"]}' ";
        }

        return $query;
    }

    public function getInterView($model)
    {
        $query  = " SELECT ";
        $query .= "   HALL.EXAMHALL_NAME, ";
        $query .= "   BASE.EXAMNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   BASE.NAME_KANA, ";
        $query .= "   SCHOOL.FINSCHOOL_NAME_ABBV, ";
        $query .= "   HALL.EXAMHALLCD, ";
        $query .= "   BD031.REMARK1, ";
        $query .= "   GDIV02.GENERAL_ABBV, ";
        $query .= "   '□説明会・体験学習' AS MOTIVE1, ";
        $query .= "   '□部活と勉強の両立' AS MOTIVE2, ";
        $query .= "   CONF.ABSENCE_DAYS, ";
        $query .= "   CONF.ABSENCE_DAYS2, ";
        $query .= "   CONF.ABSENCE_DAYS3, ";
        $query .= "   CASE WHEN BASE.DORMITORY_FLG = '1' THEN '有' END AS RYO, ";
        $query .= "   'A' AS YOBI2, ";
        $query .= "   'B' AS YOBI3, ";
        $query .= "   'C' AS YOBI4 ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HALL_YDAT HALL ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = HALL.ENTEXAMYEAR AND BASE.APPLICANTDIV = HALL.APPLICANTDIV AND BASE.TESTDIV = HALL.TESTDIV AND BASE.TESTDIV0 = HALL.EXAM_TYPE AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= " LEFT JOIN ";
        $query .= "     FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD  ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ON BD031.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND BD031.APPLICANTDIV = BASE.APPLICANTDIV AND BD031.EXAMNO = BASE.EXAMNO AND BD031.SEQ = '031' ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_GENERAL_MST GDIV02 ON GDIV02.ENTEXAMYEAR = BD031.ENTEXAMYEAR AND GDIV02.APPLICANTDIV = BD031.APPLICANTDIV AND GDIV02.GENERAL_DIV = '02' AND GDIV02.GENERAL_CD = BD031.REMARK1 ";
        $query .= " LEFT JOIN ";
        $query .= "   ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONF.APPLICANTDIV = BASE.APPLICANTDIV AND CONF.EXAMNO = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "   HALL.ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "   HALL.APPLICANTDIV = '2' AND ";
        $query .= "   HALL.TESTDIV = '{$model->field["TESTDIV"]}' AND ";
        $query .= "   HALL.EXAM_TYPE = '{$model->field["GAKKA"]}' AND ";
        $query .= "   BASE.EXAMNO IS NOT NULL ";
        if ($model->field["HALL"]) {
            $query .= "   AND HALL.EXAMHALLCD = '{$model->field["HALL"]}' ";
        }
        $query .= " ORDER BY ";
        if ("1" == $model->field["SORT"]) {
            $query .= "   HALL.EXAMHALLCD, BASE.EXAMNO, BASE.NAME_KANA ";
        } else {
            $query .= "   HALL.EXAMHALLCD, BASE.NAME_KANA, BASE.EXAMNO ";
        }
        
        return $query;
    }
}
