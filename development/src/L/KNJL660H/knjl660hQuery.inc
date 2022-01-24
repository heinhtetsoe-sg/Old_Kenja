<?php

class knjl660hquery extends Query
{

    //名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2="")
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

    //加点自動設定
    public function selectReceptDat($model)
    {
        $query  = "  SELECT ";
        $query .= "      RECEPT.ENTEXAMYEAR, ";
        $query .= "      RECEPT.APPLICANTDIV, ";
        $query .= "      RECEPT.TESTDIV, ";
        $query .= "      RECEPT.EXAM_TYPE, ";
        $query .= "      RECEPT.RECEPTNO, ";
        $query .= "      CLASSIFY.PLUS_POINT ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "      INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "          AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "      INNER JOIN ENTEXAM_CLASSIFY_MST CLASSIFY ";
        $query .= "          ON CLASSIFY.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "          AND CLASSIFY.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "          AND CLASSIFY.CLASSIFY_CD = BASE.TESTDIV1 ";
        $query .= "  WHERE ";
        $query .= "      RECEPT.ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "      AND RECEPT.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "      AND RECEPT.TESTDIV       = '{$model->testdiv}' ";
        $query .= "      AND RECEPT.EXAM_TYPE     = '1' ";
        $query .= "      AND VALUE(BASE.JUDGEMENT, '0') <> '3' ";

        return $query;
    }

    //メイン
    public function getSelectMainQuery($model)
    {
        $query .= "  WITH TESTSUB_INFO AS ( ";
        $query .= "      SELECT ";
        $query .= "          T1.ENTEXAMYEAR, ";
        $query .= "          T1.APPLICANTDIV, ";
        $query .= "          T1.TESTDIV, ";
        $query .= "          T1.EXAM_TYPE, ";
        $query .= "          T1.TESTSUBCLASSCD ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "          INNER JOIN ENTEXAM_PERFECT_EXAMTYPE_MST T2 ";
        $query .= "              ON T2.ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
        $query .= "              AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "              AND T2.TESTDIV        = T1.TESTDIV ";
        $query .= "              AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= "              AND T2.EXAM_TYPE      = T1.EXAM_TYPE ";
        $query .= "              AND T2.COURSECD       = '0' ";
        $query .= "              AND T2.MAJORCD        = '000' ";
        $query .= "              AND T2.EXAMCOURSECD   = '0000' ";
        $query .= "  ), SCORE_DATA AS ( ";
        $query .= "      SELECT ";
        $query .= "          T1.ENTEXAMYEAR, ";
        $query .= "          T1.APPLICANTDIV, ";
        $query .= "          T1.TESTDIV, ";
        $query .= "          T1.EXAM_TYPE, ";
        $query .= "          T1.RECEPTNO, ";
        $query .= "          T1.TESTSUBCLASSCD, ";
        $query .= "          T1.ATTEND_FLG, ";
        $query .= "          T1.SCORE ";
        $query .= "      FROM ";
        $query .= "          ENTEXAM_SCORE_DAT T1 ";
        $query .= "          INNER JOIN TESTSUB_INFO T2 ";
        $query .= "              ON T2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "              AND T2.APPLICANTDIV    = T1.APPLICANTDIV ";
        $query .= "              AND T2.TESTDIV         = T1.TESTDIV ";
        $query .= "              AND T2.EXAM_TYPE       = T1.EXAM_TYPE ";
        $query .= "              AND T2.TESTSUBCLASSCD  = T1.TESTSUBCLASSCD ";
        $query .= "          INNER JOIN ENTEXAM_RECEPT_DAT T3 ";
        $query .= "              ON T3.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "              AND T3.APPLICANTDIV    = T1.APPLICANTDIV ";
        $query .= "              AND T3.TESTDIV         = T1.TESTDIV ";
        $query .= "              AND T3.EXAM_TYPE       = T1.EXAM_TYPE ";
        $query .= "              AND T3.RECEPTNO        = T1.RECEPTNO ";
        $query .= "      WHERE ";
        $query .= "          T1.ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "          AND T1.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "          AND T1.TESTDIV       = '{$model->testdiv}' ";
        $query .= "          AND T1.EXAM_TYPE     = '1' ";
        $query .= "  ), SCORE_TOTAL AS ( ";
        $query .= "      SELECT ";
        $query .= "          T1.ENTEXAMYEAR, ";
        $query .= "          T1.APPLICANTDIV, ";
        $query .= "          T1.TESTDIV, ";
        $query .= "          T1.EXAM_TYPE, ";
        $query .= "          T1.RECEPTNO, ";
        $query .= "          T2.REMARK1 AS PLUS_POINT, ";
        $query .= "          SUM(T1.SCORE) AS TOTAL, ";
        $query .= "          VALUE(SUM(T1.SCORE), 0) + VALUE(T2.REMARK1, 0) AS TOTAL_P, ";
        $query .= "          SUM(CASE WHEN T1.SCORE IS NULL THEN 1 ELSE 0 END) AS SCORE_NULL_CNT ";
        $query .= "      FROM ";
        $query .= "          SCORE_DATA T1 ";
        $query .= "          LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "              AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "              AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "              AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "              AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= "              AND T2.SEQ          = '008' ";
        $query .= "      GROUP BY ";
        $query .= "          T1.ENTEXAMYEAR, ";
        $query .= "          T1.APPLICANTDIV, ";
        $query .= "          T1.TESTDIV, ";
        $query .= "          T1.EXAM_TYPE, ";
        $query .= "          T1.RECEPTNO, ";
        $query .= "          T2.REMARK1 ";
        $query .= "      HAVING SUM(T1.SCORE) IS NOT NULL ";
        $query .= "  ), SCORE_RANK AS (";
        $query .= "     SELECT ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.TESTDIV, ";
        $query .= "         T1.EXAM_TYPE, ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.TOTAL, ";
        $query .= "         T1.TOTAL_P, ";
        $query .= "         RANK() OVER (ORDER BY T1.TOTAL DESC) AS RANK, ";
        $query .= "         RANK() OVER (ORDER BY T1.TOTAL_P DESC) AS RANK_P ";
        $query .= "     FROM ";
        $query .= "         SCORE_TOTAL T1 ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      T1.ENTEXAMYEAR, ";
        $query .= "      T1.APPLICANTDIV, ";
        $query .= "      T1.TESTDIV, ";
        $query .= "      T1.EXAM_TYPE, ";
        $query .= "      T1.RECEPTNO, ";
        $query .= "      T1.TOTAL, ";
        $query .= "      T1.TOTAL_P, ";
        $query .= "      T2.RANK, ";
        $query .= "      T2.RANK_P ";
        $query .= "  FROM ";
        $query .= "      SCORE_TOTAL T1 ";
        $query .= "      LEFT JOIN SCORE_RANK T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "          AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "          AND T2.RECEPTNO = T1.RECEPTNO ";

        return $query;
    }

    //更新 -- ENTEXAM_RECEPT_DAT
    public function &getUpdateReceptDatQuery($model, $receptData)
    {
        $data = array();
        $data["TOTAL4"][TEXT]       = $receptData["TOTAL"];
        $data["TOTAL_RANK4"][TEXT]  = $receptData["RANK"];
        $data["TOTAL3"][TEXT]       = $receptData["TOTAL_P"];
        $data["TOTAL_RANK3"][TEXT]  = $receptData["RANK_P"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "   AND EXAM_TYPE      = '1' ";
        $where .= "   AND RECEPTNO       = '{$receptData["RECEPTNO"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        return $query;
    }

    //更新 -- ENTEXAM_RECEPT_DETAIL_DAT
    public function &getUpdateReceptDetailDatQuery($db, $model, $receptData)
    {
        $seq = "008";

        $data = array();
        $data["REMARK1"][TEXT]      = $receptData["PLUS_POINT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $cnt = $db->getOne(knjl660hQuery::getReceptDetailExistsCnt($model, $receptData["RECEPTNO"], $seq));
        if ($cnt > 0) {
            $where .= " WHERE ";
            $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
            $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
            $where .= "   AND EXAM_TYPE      = '1' ";
            $where .= "   AND RECEPTNO       = '{$receptData["RECEPTNO"]}' ";
            $where .= "   AND SEQ            = '{$seq}' ";

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
        } else {
            $data["ENTEXAMYEAR"][TEXT]   = $model->examyear;
            $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
            $data["TESTDIV"][TEXT]       = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]     = "1";
            $data["RECEPTNO"][TEXT]      = $receptData["RECEPTNO"];
            $data["SEQ"][TEXT]           = $seq;
    
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        }
        return $query;
    }

    //存在 -- ENTEXAM_RECEPT_DETAIL_DAT
    public function getReceptDetailExistsCnt($model, $receptNo, $seq)
    {
        $query  = "   SELECT ";
        $query .= "       COUNT(*) AS CNT";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= "   WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "       AND EXAM_TYPE     = '1' ";
        $query .= "       AND RECEPTNO      = '{$receptNo}' ";
        $query .= "       AND SEQ           = '{$seq}' ";

        return $query;
    }
}
