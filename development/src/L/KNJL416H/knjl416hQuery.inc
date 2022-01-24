<?php

class knjl416hquery extends Query
{

    //名称マスタ取得
    public function getNameMst($model, $nameCd1, $nameCd2="")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= "   , NAME1 ";
        $query .= "   , NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '{$model->examyear}' ";
        if ($nameCd1 != "") {
            $query .= "   AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }

        return $query;
    }

    //設定マスタ取得
    public function getSettingMst($model, $settingCd)
    {
        $query  = "   SELECT ";
        $query .= "       SEQ AS VALUE, ";
        $query .= "       SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "       SEQ AS KEY, ";
        if ($settingCd == "L006") {
            $query .= "       NAMESPARE1 AS VAL ";
        } elseif ($settingCd == "L100") {
            $query .= "       NAME1 AS VAL ";
        } else {
            $query .= "       NAME1, ";
            $query .= "       NAMESPARE1 ";
        }
        $query .= "   FROM ";
        $query .= "       ENTEXAM_SETTING_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND SETTING_CD    = '{$settingCd}' ";

        return $query;
    }

    //入試区分取得
    public function getEntexamExamTypeMst($model, $examType = "")
    {
        $query  = "   SELECT ";
        $query .= "       EXAM_TYPE AS VALUE, ";
        $query .= "       EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_EXAMTYPE_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        if ($examType != "") {
            $query .= "       AND EXAM_TYPE     = '{$examType}' ";
        }
        $query .= "       AND REMARK1       = '{$model->testdiv}' ";

        return $query;
    }

    //入試コース取得
    public function getEntexamCourseMst($model)
    {
        $query  = "   SELECT ";
        $query .= "       COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
        $query .= "       COURSECD || MAJORCD || EXAMCOURSECD || ':' || EXAMCOURSE_NAME  AS LABEL,";
        $query .= "       COURSECD || MAJORCD || EXAMCOURSECD AS KEY, ";
        $query .= "       EXAMCOURSE_ABBV AS VAL,";
        $query .= "       EXAMCOURSE_NAME ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_COURSE_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '0' ";

        return $query;
    }

    //塾名
    public function getPriSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOLCD AS KEY, ";
        $query .= "     PRISCHOOL_NAME AS VAL ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
    
        return $query;
    }

    //教室名
    public function getPriSchoolClassName()
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOLCD || '-' || PRISCHOOL_CLASS_CD AS KEY, ";
        $query .= "     PRISCHOOL_NAME AS VAL ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
    
        return $query;
    }

    //個人or全体の統計情報を取得
    public function getSelectQuery($model)
    {
        $query  = "   WITH ALL_TESTDIV_DATA AS ( ";
        $query .= "       SELECT ";
        $query .= "           RECEPT.ENTEXAMYEAR, ";
        $query .= "           RECEPT.APPLICANTDIV, ";
        $query .= "           RECEPT.EXAMNO, ";
        $query .= "           MAX(CASE WHEN RECEPT.TESTDIV ='1' THEN RECEPT.RECEPTNO ELSE NULL END) AS TESTDIV1_EXAMNO, ";
        $query .= "           MAX(CASE WHEN RECEPT.TESTDIV ='2' THEN RECEPT.RECEPTNO ELSE NULL END) AS TESTDIV2_EXAMNO, ";
        $query .= "           MAX(CASE WHEN RECEPT.TESTDIV ='3' THEN RECEPT.RECEPTNO ELSE NULL END) AS TESTDIV3_EXAMNO, ";
        $query .= "           MAX(CASE WHEN RECEPT.TESTDIV ='4' THEN RECEPT.RECEPTNO ELSE NULL END) AS TESTDIV4_EXAMNO, ";
        $query .= "           MAX(CASE WHEN RECEPT.TESTDIV ='5' THEN RECEPT.RECEPTNO ELSE NULL END) AS TESTDIV5_EXAMNO  ";
        $query .= "       FROM ";
        $query .= "           ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "       WHERE ";
        $query .= "           RECEPT.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "           AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       GROUP BY ";
        $query .= "           RECEPT.ENTEXAMYEAR, ";
        $query .= "           RECEPT.APPLICANTDIV, ";
        $query .= "           RECEPT.EXAMNO ";
        $query .= "   ) ";
        $query .= "   SELECT ";
        $query .= "       BASE.EXAMNO, ";
        $query .= "       BASE.NAME, ";
        $query .= "       BASE.NAME_KANA, ";
        $query .= "       BASE.SEX, ";
        $query .= "       BASE.BIRTHDAY AS HIHUN_BIRTHDAY, ";
        $query .= "       BD005.REMARK1 AS QUALIFIED_CHK, ";
        $query .= "       BD005.REMARK4 AS QUALIFIED_NAME, ";
        $query .= "       BD005.REMARK5 AS QUALIFIED_RANK, ";
        $query .= "       BD005.REMARK6 AS QUALIFIED_REMARK, ";
        $query .= "       CD002.REMARK1 AS NAISHIN_9, ";
        $query .= "       CD002.REMARK2 AS NAISHIN_5, ";
        $query .= "       CD002.REMARK3 AS NAISHIN_3YEARS, ";
        $query .= "       BD008.REMARK1 AS PS_CD, ";
        $query .= "       BD008.REMARK4 AS PS_CLS_CD, ";
        $query .= "       CD003.REMARK1 AS MOSHI_CD, ";
        $query .= "       CD003.REMARK2 AS MOSHI_SCORE, ";
        $query .= "       CD003.REMARK10 AS MOSHI_NAIDAKU_COURSE, ";
        $query .= "       CD003.REMARK11 AS MOSHI_NAIDAKU_SHDIV, ";
        $query .= "       CD003.REMARK12 AS MOSHI_SCHOLAR_DIV, ";
        $query .= "       CD004.REMARK1 AS OPT_A, ";
        $query .= "       CD004.REMARK2 AS OPT_B, ";
        $query .= "       CD004.REMARK3 AS OPT_C, ";
        $query .= "       CD004.REMARK10 AS OPT_NAIDAKU_COURSE, ";
        $query .= "       CD004.REMARK11 AS OPT_NAIDAKU_SHDIV, ";
        $query .= "       CD004.REMARK12 AS OPT_SCHOLAR_DIV, ";
        $query .= "       CD005.REMARK1 AS NAISHIN_NAIDAKU_COURSE, ";
        $query .= "       CD005.REMARK2 AS NAISHIN_NAIDAKU_SHDIV, ";
        $query .= "       CD005.REMARK3 AS NAISHIN_SCHOLAR_DIV, ";
        $query .= "       CD005.REMARK5 AS KYOUKA_RECOM_NAIDAKU_COURSE, ";
        $query .= "       CD005.REMARK6 AS KYOUKA_RECOM_NAIDAKU_SHDIV, ";
        $query .= "       RD015.REMARK1 AS HOPE_ORDER1_COURSE, ";
        $query .= "       RD015.REMARK2 AS HOPE_ORDER1_SHDIV, ";
        $query .= "       RD016.REMARK1 AS HOPE_ORDER2_COURSE, ";
        $query .= "       RD016.REMARK2 AS HOPE_ORDER2_SHDIV, ";
        $query .= "       RD017.REMARK1 AS HOPE_ORDER3_COURSE, ";
        $query .= "       RD017.REMARK2 AS HOPE_ORDER3_SHDIV, ";
        $query .= "       ATD.TESTDIV1_EXAMNO, ";
        $query .= "       ATD.TESTDIV2_EXAMNO, ";
        $query .= "       ATD.TESTDIV3_EXAMNO, ";
        $query .= "       ATD.TESTDIV4_EXAMNO, ";
        $query .= "       ATD.TESTDIV5_EXAMNO ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "           ON BD005.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "           AND BD005.APPLICANTDIV    = BASE.APPLICANTDIV ";
        $query .= "           AND BD005.EXAMNO          = BASE.EXAMNO ";
        $query .= "           AND BD005.SEQ             = '005' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD008 ";
        $query .= "           ON BD008.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "           AND BD008.APPLICANTDIV    = BASE.APPLICANTDIV ";
        $query .= "           AND BD008.EXAMNO          = BASE.EXAMNO ";
        $query .= "           AND BD008.SEQ             = '008' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ";
        $query .= "           ON CONF.ENTEXAMYEAR       = BASE.ENTEXAMYEAR ";
        $query .= "           AND CONF.APPLICANTDIV     = BASE.APPLICANTDIV ";
        $query .= "           AND CONF.EXAMNO           = BASE.EXAMNO ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD002 ";
        $query .= "           ON CD002.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "           AND CD002.APPLICANTDIV    = BASE.APPLICANTDIV ";
        $query .= "           AND CD002.EXAMNO          = BASE.EXAMNO ";
        $query .= "           AND CD002.SEQ             = '002' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD003 ";
        $query .= "           ON CD003.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "           AND CD003.APPLICANTDIV    = BASE.APPLICANTDIV ";
        $query .= "           AND CD003.EXAMNO          = BASE.EXAMNO ";
        $query .= "           AND CD003.SEQ             = '003' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD004 ";
        $query .= "           ON CD004.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "           AND CD004.APPLICANTDIV    = BASE.APPLICANTDIV ";
        $query .= "           AND CD004.EXAMNO          = BASE.EXAMNO ";
        $query .= "           AND CD004.SEQ             = '004' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD005 ";
        $query .= "           ON CD005.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "           AND CD005.APPLICANTDIV    = BASE.APPLICANTDIV ";
        $query .= "           AND CD005.EXAMNO          = BASE.EXAMNO ";
        $query .= "           AND CD005.SEQ             = '005' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD018 ";
        $query .= "           ON CD018.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "           AND CD018.APPLICANTDIV    = BASE.APPLICANTDIV ";
        $query .= "           AND CD018.EXAMNO          = BASE.EXAMNO ";
        $query .= "           AND CD018.SEQ             = '018' ";
        $query .= "       LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "           ON RECEPT.ENTEXAMYEAR     = BASE.ENTEXAMYEAR ";
        $query .= "           AND RECEPT.APPLICANTDIV   = BASE.APPLICANTDIV ";
        $query .= "           AND RECEPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "           AND RECEPT.EXAM_TYPE      = '{$model->examtype}' ";
        $query .= "           AND RECEPT.EXAMNO         = BASE.EXAMNO ";
        $query .= "       LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
        $query .= "           ON RD015.ENTEXAMYEAR      = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RD015.APPLICANTDIV    = RECEPT.APPLICANTDIV ";
        $query .= "           AND RD015.TESTDIV         = RECEPT.TESTDIV ";
        $query .= "           AND RD015.EXAM_TYPE       = RECEPT.EXAM_TYPE ";
        $query .= "           AND RD015.RECEPTNO        = RECEPT.RECEPTNO ";
        $query .= "           AND RD015.SEQ             = '015' ";
        $query .= "       LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD016 ";
        $query .= "           ON RD016.ENTEXAMYEAR      = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RD016.APPLICANTDIV    = RECEPT.APPLICANTDIV ";
        $query .= "           AND RD016.TESTDIV         = RECEPT.TESTDIV ";
        $query .= "           AND RD016.EXAM_TYPE       = RECEPT.EXAM_TYPE ";
        $query .= "           AND RD016.RECEPTNO        = RECEPT.RECEPTNO ";
        $query .= "           AND RD016.SEQ             = '016' ";
        $query .= "       LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD017 ";
        $query .= "           ON RD017.ENTEXAMYEAR      = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RD017.APPLICANTDIV    = RECEPT.APPLICANTDIV ";
        $query .= "           AND RD017.TESTDIV         = RECEPT.TESTDIV ";
        $query .= "           AND RD017.EXAM_TYPE       = RECEPT.EXAM_TYPE ";
        $query .= "           AND RD017.RECEPTNO        = RECEPT.RECEPTNO ";
        $query .= "           AND RD017.SEQ             = '017' ";
        $query .= "       LEFT JOIN ALL_TESTDIV_DATA ATD ";
        $query .= "           ON ATD.ENTEXAMYEAR        = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND ATD.APPLICANTDIV      = RECEPT.APPLICANTDIV ";
        $query .= "           AND ATD.EXAMNO            = RECEPT.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "       BASE.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "       AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND RD015.REMARK1     = '{$model->course}' ";
        $query .= "       AND RD015.REMARK2     = '{$model->shdiv}' ";
        $query .= "   ORDER BY ";
        $query .= "       BASE.EXAMNO ";

        return $query;
    }
}
