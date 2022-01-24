<?php

require_once('for_php7.php');

class knjl760hquery extends Query
{

    //名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2 = "")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= "   , NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        if ($nameCd1 != "") {
            $query .= "   AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }

        return $query;
    }

    //入試区分取得
    public function getEntexamTestDivMst($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_NAME LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";

        return $query;
    }

    //メイン
    public function getReceptDataQuery($model)
    {
        $query  = " WITH RECEPT_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPT.ENTEXAMYEAR, ";
        $query .= "         RECEPT.APPLICANTDIV, ";
        $query .= "         RECEPT.TESTDIV, ";
        $query .= "         RECEPT.EXAM_TYPE, ";
        $query .= "         RECEPT.RECEPTNO, ";
        $query .= "         RECEPT.EXAMNO, ";
        $query .= "         BASE.RECOM_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND BASE.TESTDIV = RECEPT.TESTDIV ";
        $query .= "             AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "        RECEPT.ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "        AND RECEPT.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "        AND RECEPT.TESTDIV       = '{$model->testdiv}' ";
        $query .= "        AND RECEPT.EXAM_TYPE     = '1' ";
        $query .= "        AND VALUE(RECEPT.JUDGEDIV, '0') <> '5' ";
        $query .= " ), SCORE_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCORE.ENTEXAMYEAR, ";
        $query .= "         SCORE.APPLICANTDIV, ";
        $query .= "         SCORE.TESTDIV, ";
        $query .= "         SCORE.EXAM_TYPE, ";
        $query .= "         SCORE.TESTSUBCLASSCD, ";
        $query .= "         TESTSUB.REMARK2 AS SUB_TYPE, ";
        $query .= "         SCORE.RECEPTNO, ";
        $query .= "         SCORE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT SCORE ";
        $query .= "         INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT TESTSUB ";
        $query .= "             ON TESTSUB.ENTEXAMYEAR = SCORE.ENTEXAMYEAR ";
        $query .= "             AND TESTSUB.APPLICANTDIV = SCORE.APPLICANTDIV ";
        $query .= "             AND TESTSUB.TESTDIV = SCORE.TESTDIV ";
        $query .= "             AND TESTSUB.EXAM_TYPE = SCORE.EXAM_TYPE ";
        $query .= "             AND TESTSUB.TESTSUBCLASSCD = SCORE.TESTSUBCLASSCD ";
        $query .= "         INNER JOIN RECEPT_DATA RECEPT ";
        $query .= "             ON RECEPT.ENTEXAMYEAR = SCORE.ENTEXAMYEAR ";
        $query .= "             AND RECEPT.APPLICANTDIV = SCORE.APPLICANTDIV ";
        $query .= "             AND RECEPT.TESTDIV = SCORE.TESTDIV ";
        $query .= "             AND RECEPT.EXAM_TYPE = SCORE.EXAM_TYPE ";
        $query .= "             AND RECEPT.RECEPTNO = SCORE.RECEPTNO ";
        $query .= " ), ESSAY_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPTNO, ";
        $query .= "         SUM(SCORE) AS ESSAY_SCORE_SUM, ";
        $query .= "         DECIMAL (ROUND(SUM(SCORE) * 1.0 / COUNT(SCORE), 1), 4, 1) AS ESSAY_SCORE_AVG ";
        $query .= "     FROM ";
        $query .= "         SCORE_DATA ";
        $query .= "     WHERE ";
        $query .= "         SUB_TYPE = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         RECEPTNO ";
        $query .= " ), HIKKI_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPTNO, ";
        $query .= "         SUM(SCORE) AS HIKKI_SCORE_SUM, ";
        $query .= "         DECIMAL (ROUND(SUM(SCORE) * 1.0 / COUNT(SCORE), 1), 4, 1) AS HIKKI_SCORE_AVG ";
        $query .= "     FROM ";
        $query .= "         SCORE_DATA ";
        $query .= "     WHERE ";
        $query .= "         VALUE(SUB_TYPE, '0') NOT IN ('1', '2') ";
        $query .= "     GROUP BY ";
        $query .= "         RECEPTNO ";
        $query .= " ), PLUS_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         HIKKI.RECEPTNO, ";
        $query .= "         DECIMAL (ROUND(HIKKI_SCORE_AVG * 0.15, 1), 4, 1) AS RECOM_PLUS_POINT ";
        $query .= "     FROM ";
        $query .= "         HIKKI_DATA HIKKI ";
        $query .= "         INNER JOIN RECEPT_DATA RDATA ";
        $query .= "             ON RDATA.RECEPTNO = HIKKI.RECEPTNO ";
        $query .= "     WHERE ";
        $query .= "         EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             WHERE ";
        $query .= "                 BASE.ENTEXAMYEAR = RDATA.ENTEXAMYEAR ";
        $query .= "                 AND BASE.APPLICANTDIV = RDATA.APPLICANTDIV ";
        $query .= "                 AND BASE.EXAMNO = RDATA.RECOM_EXAMNO ";
        $query .= "                 AND VALUE (BASE.JUDGEMENT, '0') <> '5' ";
        $query .= "         ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN.ENTEXAMYEAR, ";
        $query .= "     MAIN.APPLICANTDIV, ";
        $query .= "     MAIN.TESTDIV, ";
        $query .= "     MAIN.EXAM_TYPE, ";
        $query .= "     MAIN.RECEPTNO, ";
        $query .= "     MAIN.EXAMNO, ";
        $query .= "     CONFRPT.TOTAL_ALL, ";
        $query .= "     BD031.REMARK10 AS QUALIFIED_PLUS_POINT, ";
        $query .= "     HIKKI.HIKKI_SCORE_SUM, ";
        $query .= "     HIKKI.HIKKI_SCORE_AVG, ";
        $query .= "     ESSAY.ESSAY_SCORE_SUM, ";
        $query .= "     ESSAY.ESSAY_SCORE_AVG, ";
        $query .= "     PLUS.RECOM_PLUS_POINT ";
        $query .= " FROM ";
        $query .= "     RECEPT_DATA MAIN ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ";
        $query .= "         ON CONFRPT.ENTEXAMYEAR = MAIN.ENTEXAMYEAR ";
        $query .= "         AND CONFRPT.APPLICANTDIV = MAIN.APPLICANTDIV ";
        $query .= "         AND CONFRPT.EXAMNO = MAIN.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "         ON BD031.ENTEXAMYEAR = MAIN.ENTEXAMYEAR ";
        $query .= "         AND BD031.APPLICANTDIV = MAIN.APPLICANTDIV ";
        $query .= "         AND BD031.EXAMNO = MAIN.EXAMNO ";
        $query .= "         AND BD031.SEQ = '031' ";
        $query .= "     LEFT JOIN ESSAY_DATA ESSAY ";
        $query .= "         ON ESSAY.RECEPTNO = MAIN.RECEPTNO ";
        $query .= "     LEFT JOIN HIKKI_DATA HIKKI ";
        $query .= "         ON HIKKI.RECEPTNO = MAIN.RECEPTNO ";
        $query .= "     LEFT JOIN PLUS_DATA PLUS ";
        $query .= "         ON PLUS.RECEPTNO = MAIN.RECEPTNO ";

        return $query;
    }

    //志願者を総合点に基づいて順位付け
    public function &getSelectReceptRankQuery($model)
    {
        $query  = "     SELECT ";
        $query .= "         RECEPT.RECEPTNO, ";
        $query .= "         RANK() OVER (ORDER BY FLOAT(RD009.REMARK6) DESC) AS TOTAL_RANK, ";
        $query .= "         RANK() OVER (PARTITION BY BASE.SEX ORDER BY FLOAT(RD009.REMARK6) DESC) AS TOTAL_SEX_RANK ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND BASE.TESTDIV = RECEPT.TESTDIV ";
        $query .= "             AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD009 ";
        $query .= "             ON RD009.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND RD009.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND RD009.TESTDIV = RECEPT.TESTDIV ";
        $query .= "             AND RD009.EXAM_TYPE = RECEPT.EXAM_TYPE ";
        $query .= "             AND RD009.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "             AND RD009.SEQ = '009' ";
        $query .= "     WHERE ";
        $query .= "             RECEPT.ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "             AND RECEPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "             AND RECEPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "             AND RECEPT.EXAM_TYPE      = '1' ";
        $query .= "             AND RD009.REMARK6 IS NOT NULL ";

        return $query;
    }
   
    //筆記試験の統計情報取得
    public function &getSelectToukeiQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN AD.SEX IS NULL THEN '9' ELSE AD.SEX END AS SEX, "; //1:男子, 2:女子, 9:共通
        $query .= "     CASE WHEN SD.TESTSUBCLASSCD IS NULL THEN '99' ELSE SD.TESTSUBCLASSCD END AS TESTSUBCLASSCD, ";
        $query .= "     MAX(SD.SCORE) AS HIGHSCORE, ";
        $query .= "     MIN(SD.SCORE) AS LOWSCORE, ";
        $query .= "     DECIMAL(ROUND(SUM(SD.SCORE) * 1.0 / COUNT(DISTINCT SD.RECEPTNO), 1), 4, 1) AS AVG, ";
        $query .= "     SUM(SD.SCORE) AS TOTAL, ";
        $query .= "     COUNT(DISTINCT SD.RECEPTNO) AS COUNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT SD ";
        $query .= "     INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT TD ";
        $query .= "         ON TD.ENTEXAMYEAR = SD.ENTEXAMYEAR ";
        $query .= "         AND TD.APPLICANTDIV = SD.APPLICANTDIV ";
        $query .= "         AND TD.TESTDIV = SD.TESTDIV ";
        $query .= "         AND TD.EXAM_TYPE = SD.EXAM_TYPE ";
        $query .= "         AND TD.TESTSUBCLASSCD = SD.TESTSUBCLASSCD ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT RD ";
        $query .= "         ON RD.ENTEXAMYEAR = SD.ENTEXAMYEAR ";
        $query .= "         AND RD.APPLICANTDIV = SD.APPLICANTDIV ";
        $query .= "         AND RD.TESTDIV = SD.TESTDIV ";
        $query .= "         AND RD.EXAM_TYPE = SD.EXAM_TYPE ";
        $query .= "         AND RD.RECEPTNO = SD.RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT AD ";
        $query .= "         ON AD.ENTEXAMYEAR = RD.ENTEXAMYEAR ";
        $query .= "         AND AD.APPLICANTDIV = RD.APPLICANTDIV ";
        $query .= "         AND AD.TESTDIV = RD.TESTDIV ";
        $query .= "         AND AD.EXAMNO = RD.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD009 ";
        $query .= "         ON RD009.ENTEXAMYEAR = SD.ENTEXAMYEAR ";
        $query .= "         AND RD009.APPLICANTDIV = SD.APPLICANTDIV ";
        $query .= "         AND RD009.TESTDIV = SD.TESTDIV ";
        $query .= "         AND RD009.EXAM_TYPE = SD.EXAM_TYPE ";
        $query .= "         AND RD009.RECEPTNO = SD.RECEPTNO ";
        $query .= "         AND RD009.SEQ = '009' ";
        $query .= " WHERE ";
        $query .= "     RD.ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "     AND RD.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND RD.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND RD.EXAM_TYPE      = '1' ";
        $query .= "     AND RD009.REMARK6 IS NOT NULL ";
        $query .= "     AND VALUE(TD.REMARK2, '0') NOT IN ('1', '2') ";
        $query .= " GROUP BY ";
        $query .= "     GROUPING SETS ((AD.SEX, SD.TESTSUBCLASSCD), (AD.SEX), (SD.TESTSUBCLASSCD), ()) ";

        return $query;
    }

    //データカウント(ENTEXAM_RECEPT_DETAIL_DAT)
    public function getCountReceptDetaildata($model, $receptNo, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE      = '1' ";
        $query .= "     AND RECEPTNO       = '{$receptNo}' ";
        $query .= "     AND SEQ            = '{$seq}' ";

        return $query;
    }

    //更新 -- ENTEXAM_RECEPT_DETAIL_DAT
    public function &getUpdateReceptDetailDatQuery($model, $receptData, $seq, $getCount)
    {
        $data = array();
        $data["REMARK1"][TEXT]      = $receptData["HIKKI_SCORE_SUM"];
        $data["REMARK2"][TEXT]      = $receptData["HIKKI_SCORE_AVG"];
        $data["REMARK3"][TEXT]      = $receptData["ESSAY_SCORE_SUM"];
        $data["REMARK4"][TEXT]      = $receptData["ESSAY_SCORE_AVG"];
        if ($model->applicantdiv == "2" && $model->testdiv == "01") {
            $data["REMARK5"][TEXT]      = $receptData["RECOM_PLUS_POINT"];
        }
        $data["REMARK6"][TEXT]      = $receptData["SOUGOU_POINT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = '1';
            $data["RECEPTNO"][TEXT]     = $receptData["RECEPTNO"];
            $data["SEQ"][TEXT]          = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
            $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
            $where .= "   AND EXAM_TYPE      = '1' ";
            $where .= "   AND RECEPTNO       = '{$receptData["RECEPTNO"]}' ";
            $where .= "   AND SEQ            = '{$seq}' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
        }

        return $query;
    }

    //更新 -- ENTEXAM_RECEPT_DAT ※順位用
    public function &getUpdateReceptRankQuery($model, $rankData)
    {
        $data = array();
        $data["TOTAL_RANK4"][TEXT]  = $rankData["TOTAL_RANK"];
        $data["SEX_RANK4"][TEXT]    = $rankData["TOTAL_SEX_RANK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "   AND EXAM_TYPE      = '1' ";
        $where .= "   AND RECEPTNO       = '{$rankData["RECEPTNO"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);

        return $query;
    }

    //更新 -- ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT
    public function &getDeleteAllToukeiQuery($model)
    {
        $query .= " DELETE FROM ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT ";
        $query .= " WHERE ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND SHDIV         = '1' ";
        $query .= "       AND COURSECODE    = '0000' ";

        return $query;
    }

    //更新 -- ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT
    public function &getUpdateToukeiQuery($model, $toukeiData)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["SHDIV"][TEXT]            = "1";
        $data["COURSECODE"][TEXT]       = "0000";
        $data["SEX"][TEXT]              = $toukeiData["SEX"];
        $data["TESTSUBCLASSCD"][TEXT]   = $toukeiData["TESTSUBCLASSCD"];
        $data["HIGHSCORE"][TEXT]        = $toukeiData["HIGHSCORE"];
        $data["LOWSCORE"][TEXT]         = $toukeiData["LOWSCORE"];
        $data["AVG"][FUNC]              = $toukeiData["AVG"];
        $data["TOTAL"][TEXT]            = $toukeiData["TOTAL"];
        $data["COUNT"][FUNC]            = $toukeiData["COUNT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT", $where);

        return $query;
    }
}
