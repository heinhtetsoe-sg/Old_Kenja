<?php

class knjl560iquery extends Query
{

    //名称マスタ取得
    public function getNameMst($model, $nameCd1, $nameCd2="")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
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
        $query .= "       SEQ || ':' || NAME1 AS LABEL ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_SETTING_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND SETTING_CD    = '{$settingCd}' ";

        return $query;
    }

    //入試科目数取得
    public function getTestSubclassNum($model)
    {
        $query  = "   SELECT ";
        $query .= "       COUNT(*) AS CNT ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_SETTING_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND SETTING_CD    = 'L009' ";

        return $query;
    }

    //入試区分マスタ取得
    public function getTestDivMst($model)
    {
        $query .= "   SELECT ";
        $query .= "       TESTDIV AS VALUE, ";
        $query .= "       TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_TESTDIV_MST ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //個人or全体の統計情報を取得
    public function getSelectQuery($model, $div = "")
    {
        $query  = "   WITH SCORE_DATA AS ( ";
        $query .= "       SELECT ";
        $query .= "           S1.ENTEXAMYEAR, ";
        $query .= "           S1.APPLICANTDIV, ";
        $query .= "           S1.TESTDIV, ";
        $query .= "           S1.EXAM_TYPE, ";
        $query .= "           S1.RECEPTNO, ";
        $query .= "           S1.TESTSUBCLASSCD, ";
        $query .= "           S1.SCORE ";
        $query .= "       FROM ";
        $query .= "           ENTEXAM_SCORE_DAT S1 ";
        $query .= "           INNER JOIN ENTEXAM_PERFECT_EXAMTYPE_MST P1 ";
        $query .= "               ON P1.ENTEXAMYEAR     = S1.ENTEXAMYEAR ";
        $query .= "               AND P1.APPLICANTDIV   = S1.APPLICANTDIV ";
        $query .= "               AND P1.TESTDIV        = '0' ";
        $query .= "               AND P1.COURSECD       = '0' ";
        $query .= "               AND P1.MAJORCD        = '000' ";
        $query .= "               AND P1.EXAMCOURSECD   = '0000' ";
        $query .= "               AND P1.TESTSUBCLASSCD = S1.TESTSUBCLASSCD ";
        $query .= "           INNER JOIN ENTEXAM_TESTDIV_MST T1 ";
        $query .= "               ON T1.ENTEXAMYEAR     = S1.ENTEXAMYEAR ";
        $query .= "               AND T1.APPLICANTDIV   = S1.APPLICANTDIV ";
        $query .= "               AND T1.TESTDIV        = S1.TESTDIV ";
        $query .= "           INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "               ON B1.ENTEXAMYEAR     = S1.ENTEXAMYEAR ";
        $query .= "               AND B1.APPLICANTDIV   = S1.APPLICANTDIV ";
        $query .= "               AND B1.TESTDIV        = S1.TESTDIV ";
        $query .= "               AND B1.EXAMNO         = S1.RECEPTNO ";
        $query .= "       WHERE ";
        $query .= "           S1.ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "           AND S1.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "           AND S1.TESTDIV        = '{$model->testdiv}' ";
        $query .= "           AND S1.EXAM_TYPE      = '1' ";
        $query .= "           AND S1.SCORE IS NOT NULL ";
        $query .= "           AND VALUE (B1.JUDGEMENT, '') <> '4' ";
        $query .= "   ), RECEPT_DATA0 AS ( ";
        $query .= "       SELECT ";
        $query .= "           ENTEXAMYEAR, ";
        $query .= "           APPLICANTDIV, ";
        $query .= "           TESTDIV, ";
        $query .= "           EXAM_TYPE, ";
        $query .= "           RECEPTNO, ";
        $query .= "           RECEPTNO AS EXAMNO, ";
        $query .= "           SUM(SCORE) AS TOTAL4, ";
        $query .= "           DECIMAL (ROUND(AVG(SCORE * 1.00), 1), 4, 1) AS AVERAGE4 ";
        $query .= "       FROM ";
        $query .= "           SCORE_DATA ";
        $query .= "       GROUP BY ";
        $query .= "           ENTEXAMYEAR, ";
        $query .= "           APPLICANTDIV, ";
        $query .= "           TESTDIV, ";
        $query .= "           EXAM_TYPE, ";
        $query .= "           RECEPTNO ";
        $query .= "       HAVING ";
        $query .= "           COUNT(TESTSUBCLASSCD) = '{$model->testsubNum}' "; //総科目数
        $query .= "   ), RECEPT_DATA1 AS ( ";
        $query .= "   SELECT ";
        $query .= "      RECEPT.*, ";
        $query .= "      BASE.TESTDIV0 AS MAJORCD, ";            //学科
        $query .= "      BD031.REMARK7 AS HOPE_COURSE1_TYPE, ";  //第一志望コースの類別
        $query .= "      BD031.REMARK1 AS HOPE_COURSE1 ";        //第一志望コース
        $query .= "   FROM ";
        $query .= "       RECEPT_DATA0 RECEPT ";
        $query .= "           INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON BASE.ENTEXAMYEAR     = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV   = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO         = RECEPT.EXAMNO ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "           ON BD031.ENTEXAMYEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND BD031.APPLICANTDIV  = BASE.APPLICANTDIV ";
        $query .= "           AND BD031.EXAMNO        = BASE.EXAMNO ";
        $query .= "           AND BD031.EXAMNO        = BASE.EXAMNO ";
        $query .= "           AND BD031.SEQ           = '031' ";
        $query .= "   WHERE ";
        $query .= "               BD031.REMARK1 IS NOT NULL ";
        $query .= "           AND BD031.REMARK7 IS NOT NULL ";
        $query .= "   ), RANK_DATA AS ( ";
        $query .= "   SELECT ";
        $query .= "       RECEPT.*, ";
        $query .= "       RANK() OVER (ORDER BY RECEPT.TOTAL4 DESC) AS TOTAL_RANK4, ";
        $query .= "       RANK() OVER (PARTITION BY HOPE_COURSE1  ORDER BY RECEPT.TOTAL4 DESC) AS TOTAL_RANK3 , ";
        $query .= "       RANK() OVER (PARTITION BY HOPE_COURSE1_TYPE  ORDER BY RECEPT.TOTAL4 DESC) AS TOTAL_RANK2 , ";
        $query .= "       RANK() OVER (PARTITION BY MAJORCD  ORDER BY RECEPT.TOTAL4 DESC) AS TOTAL_RANK1 ";
        $query .= "   FROM ";
        $query .= "       RECEPT_DATA1 RECEPT ";
        $query .= "   ), RECEPT_DATA AS ( ";
        $query .= "   SELECT ";
        $query .= "       RECEPT.ENTEXAMYEAR, ";
        $query .= "       RECEPT.APPLICANTDIV, ";
        $query .= "       RECEPT.TESTDIV, ";
        $query .= "       RECEPT.EXAM_TYPE, ";
        $query .= "       RECEPT.RECEPTNO, ";
        $query .= "       RECEPT.EXAMNO, ";
        $query .= "       RECEPT.TOTAL4, ";
        $query .= "       RECEPT.TOTAL4 AS TOTAL3, ";
        $query .= "       RECEPT.TOTAL4 AS TOTAL2, ";
        $query .= "       RECEPT.TOTAL4 AS TOTAL1, ";
        $query .= "       RECEPT.AVERAGE4, ";
        $query .= "       RANK.MAJORCD, ";
        $query .= "       RANK.HOPE_COURSE1_TYPE, ";
        $query .= "       RANK.HOPE_COURSE1, ";
        $query .= "       RANK.TOTAL_RANK4, ";
        $query .= "       RANK.TOTAL_RANK3, ";
        $query .= "       RANK.TOTAL_RANK2, ";
        $query .= "       RANK.TOTAL_RANK1 ";
        $query .= "   FROM ";
        $query .= "       RECEPT_DATA0 RECEPT ";
        $query .= "       INNER JOIN RANK_DATA RANK ";
        $query .= "           ON RECEPT.ENTEXAMYEAR   = RANK.ENTEXAMYEAR ";
        $query .= "           AND RECEPT.APPLICANTDIV = RANK.APPLICANTDIV ";
        $query .= "           AND RECEPT.TESTDIV      = RANK.TESTDIV ";
        $query .= "           AND RECEPT.EXAM_TYPE    = RANK.EXAM_TYPE ";
        $query .= "           AND RECEPT.RECEPTNO     = RANK.RECEPTNO ";
        $query .= "   ), TOKEI_DATA AS ( ";
        $query .= "   SELECT ";
        $query .= "       S1.ENTEXAMYEAR, ";
        $query .= "       S1.APPLICANTDIV, ";
        $query .= "       S1.TESTDIV, ";
        $query .= "       S1.EXAM_TYPE, ";
        $query .= "       VALUE (R1.HOPE_COURSE1_TYPE, '9') AS HOPE_COURSE1_TYPE, ";
        $query .= "       VALUE (R1.MAJORCD, '999') AS MAJORCD, ";
        $query .= "       VALUE (R1.HOPE_COURSE1, '9999') AS HOPE_COURSE1, ";
        $query .= "       '0' AS SEX, ";
        $query .= "       VALUE (S1.TESTSUBCLASSCD, '99') AS TESTSUBCLASSCD, ";
        $query .= "       MAX(S1.SCORE) AS HIGHSCORE, ";
        $query .= "       MIN(S1.SCORE) AS LOWSCORE, ";
        $query .= "       DECIMAL (ROUND(AVG(S1.SCORE * 1.0), 1), 4, 1) AS AVG, ";
        $query .= "       SUM(S1.SCORE) AS TOTAL, ";
        $query .= "       COUNT(DISTINCT R1.EXAMNO) AS COUNT ";
        $query .= "   FROM ";
        $query .= "       SCORE_DATA S1 ";
        $query .= "       INNER JOIN RECEPT_DATA R1 ";
        $query .= "           ON R1.ENTEXAMYEAR   = S1.ENTEXAMYEAR ";
        $query .= "           AND R1.APPLICANTDIV = S1.APPLICANTDIV ";
        $query .= "           AND R1.TESTDIV      = S1.TESTDIV ";
        $query .= "           AND R1.EXAM_TYPE    = S1.EXAM_TYPE ";
        $query .= "           AND R1.RECEPTNO     = S1.RECEPTNO ";
        $query .= "   GROUP BY ";
        $query .= "       S1.ENTEXAMYEAR, ";
        $query .= "       S1.APPLICANTDIV, ";
        $query .= "       S1.TESTDIV, ";
        $query .= "       S1.EXAM_TYPE, ";
        $query .= "       GROUPING SETS ( ";
        $query .= "               (R1.MAJORCD, R1.HOPE_COURSE1_TYPE, R1.HOPE_COURSE1, S1.TESTSUBCLASSCD), "; //コース毎科目毎
        $query .= "               (R1.MAJORCD, R1.HOPE_COURSE1_TYPE, R1.HOPE_COURSE1), "; //コース毎
        $query .= "               (R1.MAJORCD, R1.HOPE_COURSE1_TYPE, S1.TESTSUBCLASSCD), "; //類別枚科目毎
        $query .= "               (R1.MAJORCD, R1.HOPE_COURSE1_TYPE), "; //類別毎
        $query .= "               (R1.MAJORCD, S1.TESTSUBCLASSCD), "; //学科科目毎
        $query .= "               (R1.MAJORCD) "; //学科毎
        $query .= "       ) ";
        $query .= "   ) ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       * ";
        $query .= "   FROM ";
        if ($div == "TOKEI") {
            $query .= " TOKEI_DATA ";
        } else {
            $query .= " RECEPT_DATA ";
        }

        return $query;
    }

    //欠席した志願者を取得
    public function getSelectKessekiQuery($model)
    {
        $query  = "   SELECT ";
        $query .= "       ENTEXAMYEAR, ";
        $query .= "       APPLICANTDIV, ";
        $query .= "       EXAMNO, ";
        $query .= "       TESTDIV, ";
        $query .= "       EXAMNO AS RECEPTNO ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND JUDGEMENT     = '4' ";

        return $query;
    }

    //削除 -- ENTEXAM_RECEPT_DAT
    public function &getExistsReceptCnt($model, $receptno)
    {
        $query  = "   SELECT ";
        $query .= "       COUNT(*) AS CNT ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_RECEPT_DAT ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND EXAM_TYPE     = '1' ";
        $query .= "       AND RECEPTNO      = '{$receptno}' ";

        return $query;
    }

    //更新 -- ENTEXAM_RECEPT_DAT
    public function &getUpdateReceptDatQuery($db, $model, $receptData)
    {
        $data = array();
        $data["EXAMNO"][TEXT]       = $receptData["EXAMNO"];
        $data["TOTAL2"][TEXT]       = $receptData["TOTAL2"];
        $data["AVARAGE2"][TEXT]     = $receptData["AVARAGE2"];
        $data["TOTAL_RANK2"][TEXT]  = $receptData["TOTAL_RANK2"];
        $data["TOTAL4"][TEXT]       = $receptData["TOTAL4"];
        $data["AVARAGE4"][TEXT]     = $receptData["AVARAGE4"];
        $data["TOTAL_RANK4"][TEXT]  = $receptData["TOTAL_RANK4"];
        $data["TOTAL4"][TEXT]       = $receptData["TOTAL4"];
        $data["AVARAGE4"][TEXT]     = $receptData["AVARAGE4"];
        $data["TOTAL_RANK4"][TEXT]  = $receptData["TOTAL_RANK4"];
        $data["TOTAL1"][TEXT]       = $receptData["TOTAL1"];
        $data["AVARAGE1"][TEXT]     = $receptData["AVARAGE1"];
        $data["TOTAL_RANK1"][TEXT]  = $receptData["TOTAL_RANK1"];
        $data["TOTAL3"][TEXT]       = $receptData["TOTAL3"];
        $data["AVARAGE3"][TEXT]     = $receptData["AVARAGE3"];
        $data["TOTAL_RANK3"][TEXT]  = $receptData["TOTAL_RANK3"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = knjl560iQuery::getExistsReceptCnt($model, $receptData["RECEPTNO"]);
        $existsCnt = $db->getOne($query);
        if ($existsCnt == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = "1";
            $data["RECEPTNO"][TEXT]     = $receptData["RECEPTNO"];

            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
        } else {
            $where  = "   WHERE ";
            $where .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
            $where .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "       AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "       AND EXAM_TYPE     = '1' ";
            $where .= "       AND RECEPTNO      = '{$receptData["RECEPTNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        }

        return $query;
    }

    //更新 -- ENTEXAM_TOKEI_HIGH_LOW_DAT
    public function &getDeleteAllToukeiQuery($model)
    {
        $query .= " DELETE FROM ENTEXAM_TOKEI_HIGH_LOW_DAT ";
        $query .= " WHERE ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND EXAM_TYPE     = '1' ";

        return $query;
    }

    //更新 -- ENTEXAM_TOKEI_HIGH_LOW_DAT
    public function &getUpdateToukeiQuery($model, $toukeiData)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = "1";   //固定
        $data["SHDIV"][TEXT]            = $toukeiData["HOPE_COURSE1_TYPE"];
        $data["COURSECD"][TEXT]         = "0";   //固定
        $data["MAJORCD"][TEXT]          = $toukeiData["MAJORCD"];
        $data["EXAMCOURSECD"][TEXT]     = $toukeiData["HOPE_COURSE1"];
        $data["SEX"][TEXT]              = "9"; //固定
        $data["TESTSUBCLASSCD"][TEXT]   = $toukeiData["TESTSUBCLASSCD"];
        $data["HIGHSCORE"][TEXT]        = $toukeiData["HIGHSCORE"];
        $data["LOWSCORE"][TEXT]         = $toukeiData["LOWSCORE"];
        $data["AVG"][FUNC]              = $toukeiData["AVG"];
        $data["TOTAL"][TEXT]            = $toukeiData["TOTAL"];
        $data["COUNT"][FUNC]            = $toukeiData["COUNT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TOKEI_HIGH_LOW_DAT");

        return $query;
    }
}
