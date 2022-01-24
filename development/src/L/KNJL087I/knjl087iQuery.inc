<?php

class knjl087iquery extends Query
{

    //名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2="")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        if ($nameCd1 != "") {
            $query .= "   AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    public function getEntexamTestDivMst($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "   AND TESTDIV IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //加算点数取得(B方式のみ)
    public function getKasanScore($model)
    {
        $query  = "     SELECT ";
        $query .= "         NAME1 AS KASAN_SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "         AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "         AND SETTING_CD    = 'L014' ";
        $query .= "         AND SEQ           = '01' ";

        return $query;
    }

    //入試科目を取得
    public function getTestSubclasscd($model)
    {
        $query  = "    SELECT ";
        $query .= "        TESTSUBCLASSCD ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "    WHERE ";
        $query .= "        ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "        AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "        AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "        AND EXAM_TYPE     = '1' ";

        return $query;
    }

    //志願者毎科目毎の情報
    public function getReceptSubclassScoreQuery($model, $div = "")
    {
        $kasanScore = $model->kasanScore;

        //処理対象のRECEPTNOの集団
        $query  = " WITH RECEPTNO_DATA AS ( ";
        $query .= "    SELECT ";
        $query .= "        RD.RECEPTNO ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_RECEPT_DAT RD ";
        $query .= "        INNER JOIN ENTEXAM_APPLICANTBASE_DAT AD ";
        $query .= "           ON AD.ENTEXAMYEAR  = RD.ENTEXAMYEAR ";
        $query .= "          AND AD.APPLICANTDIV = RD.APPLICANTDIV ";
        $query .= "          AND AD.EXAMNO       = RD.EXAMNO ";
        $query .= "    WHERE ";
        $query .= "        RD.ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "        AND RD.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "        AND RD.TESTDIV       = '{$model->testdiv}' ";
        $query .= "        AND RD.EXAM_TYPE     = '1' ";
        $query .= "        AND VALUE(RD.JUDGEDIV, '0') <> '4' "; //欠席以外が集計対象
        $query .= "        AND NOT EXISTS ( "; //得点未入力が1つもない
        $query .= "            SELECT ";
        $query .= "                 'X' ";
        $query .= "            FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT SD";
        $query .= "            WHERE ";
        $query .= "                 SD.ENTEXAMYEAR = RD.ENTEXAMYEAR ";
        $query .= "                 AND SD.APPLICANTDIV = RD.APPLICANTDIV ";
        $query .= "                 AND SD.TESTDIV = RD.TESTDIV ";
        $query .= "                 AND SD.EXAM_TYPE = RD.EXAM_TYPE ";
        $query .= "                 AND SD.RECEPTNO = RD.RECEPTNO ";
        $query .= "                 AND SD.SCORE IS NULL ";
        $query .= "        )";
        //生徒毎の全科目分の成績情報(合計、平均)
        $query .= "  ), RECEPT_TESTSUBCLASS_ALL_DATA AS (";
        $query .= "    SELECT ";
        $query .= "        SD.ENTEXAMYEAR, ";
        $query .= "        SD.APPLICANTDIV, ";
        $query .= "        SD.TESTDIV, ";
        $query .= "        SD.EXAM_TYPE, ";
        $query .= "        SD.RECEPTNO, ";
        $query .= "        SUM(SD.SCORE) AS ALL_TESTSUB_SUM, ";
        $query .= "        DECIMAL(ROUND(SUM(SD.SCORE) * 1.0 / COUNT(SD.TESTSUBCLASSCD), 1), 4, 1) AS ALL_TESTSUB_AVG, ";
        $query .= "        (SUM(SD.SCORE) + {$kasanScore}) AS ALL_TESTSUB_KASAN_SUM, ";
        $query .= "        DECIMAL(ROUND((SUM(SD.SCORE) + {$kasanScore}) * 1.0 / COUNT(SD.TESTSUBCLASSCD), 1), 4, 1) AS ALL_TESTSUB_KASAN_AVG ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_SCORE_DAT SD ";
        $query .= "        LEFT JOIN ENTEXAM_SETTING_MST ES "; // 元科目を除く
        $query .= "          ON ES.ENTEXAMYEAR = SD.ENTEXAMYEAR ";
        $query .= "         AND ES.APPLICANTDIV = SD.APPLICANTDIV ";
        $query .= "         AND ES.SETTING_CD = 'L009' ";
        $query .= "         AND ES.SEQ = SD.TESTSUBCLASSCD ";
        $query .= "    WHERE ";
        $query .= "        SD.ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "        AND SD.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "        AND SD.TESTDIV       = '{$model->testdiv}' ";
        $query .= "        AND SD.EXAM_TYPE     = '1' ";
        $query .= "        AND SD.RECEPTNO IN (SELECT RECEPTNO FROM RECEPTNO_DATA) ";
        $query .= "        AND VALUE(ES.NAMESPARE2, '') = '' ";
        $query .= "    GROUP BY ";
        $query .= "        SD.ENTEXAMYEAR, ";
        $query .= "        SD.APPLICANTDIV, ";
        $query .= "        SD.TESTDIV, ";
        $query .= "        SD.EXAM_TYPE, ";
        $query .= "        SD.RECEPTNO ";
        $query .= "  ), ";
        if ($model->applicantdiv == "1") {
            //加算条件判定用情報(中学)
            //※志願者のA日程方式での受験有無を取得、1科目でも得点が入力されていれば受験したとみなす
            $query .= "  PRETEST AS ( ";
            $query .= "   SELECT ";
            $query .= "       RECEPT.ENTEXAMYEAR, ";
            $query .= "       RECEPT.APPLICANTDIV, ";
            $query .= "       RECEPT.EXAMNO ";
            $query .= "    ";
            $query .= "   FROM ";
            $query .= "       ENTEXAM_SCORE_DAT SCORE ";
            $query .= "       INNER JOIN ENTEXAM_RECEPT_DAT RECEPT ";
            $query .= "           ON RECEPT.ENTEXAMYEAR = SCORE.ENTEXAMYEAR ";
            $query .= "           AND RECEPT.APPLICANTDIV = SCORE.APPLICANTDIV ";
            $query .= "           AND RECEPT.TESTDIV = SCORE.TESTDIV ";
            $query .= "           AND RECEPT.EXAM_TYPE = SCORE.EXAM_TYPE ";
            $query .= "           AND RECEPT.RECEPTNO = SCORE.RECEPTNO ";
            $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "           ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
            $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
            $query .= "           AND BASE.EXAMNO = RECEPT.EXAMNO ";
            $query .= "   WHERE ";
            $query .= "        SCORE.ENTEXAMYEAR       = '{$model->examyear}' ";
            $query .= "        AND SCORE.APPLICANTDIV  = '1' "; //固定 1:中学
            $query .= "        AND SCORE.TESTDIV       = '1' "; //固定 1:A日程入試
            $query .= "        AND SCORE.EXAM_TYPE     = '1' ";
            $query .= "        AND SCORE.SCORE         IS NOT NULL ";
            $query .= "   GROUP BY ";
            $query .= "       RECEPT.ENTEXAMYEAR, ";
            $query .= "       RECEPT.APPLICANTDIV, ";
            $query .= "       RECEPT.EXAMNO ";
            $query .= "  ), ";
        }
        $query .= "  RECEPT_TESTSUBCLASS_ALL_DATA1 AS (";
        $query .= "    SELECT ";
        $query .= "        RTAD.RECEPTNO, ";
        $query .= "        RTAD.ALL_TESTSUB_SUM, ";
        $query .= "        RTAD.ALL_TESTSUB_AVG, ";
        //以下の条件を全て満たす場合加算
        if ($model->applicantdiv == "1") { //中学
            //条件1 : B日程で成績確定処理した場合
            //条件2 : A日程を受験(少なくとも1科目で得点が入力されている)
            $query .= "        (CASE WHEN (RTAD.TESTDIV = '2' AND PRETEST.EXAMNO IS NOT NULL) THEN RTAD.ALL_TESTSUB_KASAN_SUM ELSE ALL_TESTSUB_SUM END) AS ALL_TESTSUB_KASAN_SUM, ";
            $query .= "        (CASE WHEN (RTAD.TESTDIV = '2' AND PRETEST.EXAMNO IS NOT NULL) THEN RTAD.ALL_TESTSUB_AVG ELSE ALL_TESTSUB_AVG END) AS ALL_TESTSUB_KASAN_AVG ";
        } else { //高校
            //条件1 : A方式で成績確定処理した場合
            //条件2 : 高等部が第一志望
            $query .= "        (CASE WHEN (RTAD.TESTDIV = '1' AND VALUE(ADD005.REMARK1, '1') <> '2') THEN RTAD.ALL_TESTSUB_KASAN_SUM ELSE ALL_TESTSUB_SUM END) AS ALL_TESTSUB_KASAN_SUM, ";
            $query .= "        (CASE WHEN (RTAD.TESTDIV = '1' AND VALUE(ADD005.REMARK1, '1') <> '2') THEN RTAD.ALL_TESTSUB_AVG ELSE ALL_TESTSUB_AVG END) AS ALL_TESTSUB_KASAN_AVG ";
        }
        $query .= "    FROM ";
        $query .= "        RECEPT_TESTSUBCLASS_ALL_DATA RTAD ";
        $query .= "        LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "             ON RECEPT.ENTEXAMYEAR  = RTAD.ENTEXAMYEAR ";
        $query .= "            AND RECEPT.APPLICANTDIV = RTAD.APPLICANTDIV ";
        $query .= "            AND RECEPT.TESTDIV      = RTAD.TESTDIV ";
        $query .= "            AND RECEPT.EXAM_TYPE    = RTAD.EXAM_TYPE ";
        $query .= "            AND RECEPT.RECEPTNO     = RTAD.RECEPTNO ";
        $query .= "        LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "            AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "            AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        if ($model->applicantdiv == "1") {
            $query .= "        LEFT JOIN PRETEST ";
            $query .= "             ON PRETEST.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
            $query .= "            AND PRETEST.APPLICANTDIV = BASE.APPLICANTDIV ";
            $query .= "            AND PRETEST.EXAMNO       = BASE.RECOM_EXAMNO ";
        } else {
            $query .= "        LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT ADD005 ";
            $query .= "             ON ADD005.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
            $query .= "            AND ADD005.APPLICANTDIV = BASE.APPLICANTDIV ";
            $query .= "            AND ADD005.EXAMNO       = BASE.EXAMNO ";
            $query .= "            AND ADD005.SEQ          = '005' ";
        }
        $query .= "  ) ";

        if ($div == "RECEPT") {
            $query .= " SELECT ";
            $query .= "     RD.RECEPTNO, ";
            $query .= "     RTAD1.ALL_TESTSUB_SUM, ";
            $query .= "     RTAD1.ALL_TESTSUB_AVG, ";
            $query .= "     RANK() OVER ( ";
            $query .= "         ORDER BY ";
            $query .= "             RTAD1.ALL_TESTSUB_SUM DESC ";
            $query .= "     ) AS ALL_TESTSUB_SUM_RANK, ";
            $query .= "     RANK() OVER ( ";
            $query .= "         PARTITION BY ";
            $query .= "             AD.SEX ";
            $query .= "         ORDER BY ";
            $query .= "             RTAD1.ALL_TESTSUB_SUM DESC ";
            $query .= "     ) AS ALL_TESTSUB_SUM_SEX_RANK, ";
            $query .= "     AD.RECOM_EXAMNO, ";
            $query .= "     RTAD1.ALL_TESTSUB_KASAN_SUM, ";
            $query .= "     RTAD1.ALL_TESTSUB_KASAN_AVG, ";
            $query .= "     RANK() OVER ( ";
            $query .= "         ORDER BY ";
            $query .= "             RTAD1.ALL_TESTSUB_KASAN_SUM DESC ";
            $query .= "     ) AS ALL_TESTSUB_KASAN_SUM_RANK, ";
            $query .= "     RANK() OVER ( ";
            $query .= "         PARTITION BY ";
            $query .= "             AD.SEX ";
            $query .= "         ORDER BY ";
            $query .= "             RTAD1.ALL_TESTSUB_KASAN_SUM DESC ";
            $query .= "     ) AS ALL_TESTSUB_KASAN_SUM_SEX_RANK ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT RD ";
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT AD ";
            $query .= "          ON AD.ENTEXAMYEAR  = RD.ENTEXAMYEAR ";
            $query .= "         AND AD.APPLICANTDIV = RD.APPLICANTDIV ";
            $query .= "         AND AD.EXAMNO       = RD.EXAMNO ";
            $query .= "     INNER JOIN RECEPT_TESTSUBCLASS_ALL_DATA1 RTAD1 ";
            $query .= "          ON RTAD1.RECEPTNO  = RD.RECEPTNO ";
            $query .= " WHERE ";
            $query .= "     RD.ENTEXAMYEAR       = '{$model->examyear}' ";
            $query .= "     AND RD.APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "     AND RD.TESTDIV       = '{$model->testdiv}' ";
            $query .= "     AND RD.EXAM_TYPE     = '1' ";
            $query .= "     AND RTAD1.ALL_TESTSUB_SUM IS NOT NULL ";
        } else {
            $query .= " SELECT ";
            $query .= "     SD.RECEPTNO, ";
            $query .= "     SD.TESTSUBCLASSCD, ";
            $query .= "     RANK() OVER ( ";
            $query .= "         PARTITION BY ";
            $query .= "             SD.TESTSUBCLASSCD ";
            $query .= "         ORDER BY ";
            $query .= "             SD.SCORE DESC ";
            $query .= "     ) AS TESTSUBCLASS_SCORE_RANK, ";
            $query .= "     RANK() OVER ( ";
            $query .= "         PARTITION BY ";
            $query .= "             SD.TESTSUBCLASSCD, ";
            $query .= "             AD.SEX ";
            $query .= "         ORDER BY ";
            $query .= "             SD.SCORE DESC ";
            $query .= "     ) AS TESTSUBCLASS_SCORE_SEX_RANK ";
            $query .= " FROM ";
            $query .= "       ENTEXAM_SCORE_DAT SD ";
            $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT AD ";
            $query .= "           ON AD.ENTEXAMYEAR = SD.ENTEXAMYEAR ";
            $query .= "           AND AD.APPLICANTDIV = SD.APPLICANTDIV ";
            $query .= "           AND AD.EXAMNO = SD.RECEPTNO ";
            $query .= " WHERE ";
            $query .= "     SD.ENTEXAMYEAR       = '{$model->examyear}' ";
            $query .= "     AND SD.APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "     AND SD.TESTDIV       = '{$model->testdiv}' ";
            $query .= "     AND SD.EXAM_TYPE     = '1' ";
            $query .= "     AND SD.RECEPTNO IN (SELECT RECEPTNO FROM RECEPTNO_DATA) ";
        }

        return $query;
    }

    //科目毎の情報
    public function getSubclassScoreQuery($model)
    {

        //処理対象のRECEPTNOの集団
        $query  = " WITH RECEPTNO_DATA AS ( ";
        $query .= "    SELECT ";
        $query .= "        RD.RECEPTNO ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_RECEPT_DAT RD ";
        $query .= "    WHERE ";
        $query .= "        RD.ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "        AND RD.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "        AND RD.TESTDIV       = '{$model->testdiv}' ";
        $query .= "        AND RD.EXAM_TYPE     = '1' ";
        $query .= "        AND VALUE(RD.JUDGEDIV, '0') <> '4' "; //欠席以外が集計対象
        $query .= "        AND NOT EXISTS ( "; //得点未入力が1つもない
        $query .= "            SELECT ";
        $query .= "                 'X' ";
        $query .= "            FROM ";
        $query .= "                 ENTEXAM_SCORE_DAT SD";
        $query .= "            WHERE ";
        $query .= "                 SD.ENTEXAMYEAR = RD.ENTEXAMYEAR ";
        $query .= "                 AND SD.APPLICANTDIV = RD.APPLICANTDIV ";
        $query .= "                 AND SD.TESTDIV = RD.TESTDIV ";
        $query .= "                 AND SD.EXAM_TYPE = RD.EXAM_TYPE ";
        $query .= "                 AND SD.RECEPTNO = RD.RECEPTNO ";
        $query .= "                 AND SD.SCORE IS NULL ";
        $query .= "        )";
        //科目毎の成績情報(合計、人数)
        $query .= " ), TESTSUBCLASS_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SD.TESTSUBCLASSCD, ";
        $query .= "         SUM(SD.SCORE) AS TESTSUB_SUM, ";
        $query .= "         SUM(CASE WHEN AD.SEX = '1' THEN SD.SCORE ELSE NULL END) AS TESTSUB_MEN_SUM, ";
        $query .= "         SUM(CASE WHEN AD.SEX = '2' THEN SD.SCORE ELSE NULL END) AS TESTSUB_WOMEN_SUM, ";
        $query .= "         COUNT(*) AS TESTSUB_CNT, ";
        $query .= "         COUNT(CASE WHEN AD.SEX = '1' THEN 1 ELSE NULL END) AS TESTSUB_MEN_CNT, ";
        $query .= "         COUNT(CASE WHEN AD.SEX = '2' THEN 1 ELSE NULL END) AS TESTSUB_WOMEN_CNT, ";
        $query .= "         MIN(SD.SCORE) AS TESTSUB_MIN, ";
        $query .= "         MAX(SD.SCORE) AS TESTSUB_MAX, ";
        $query .= "         DECIMAL(ROUND(SUM(SD.SCORE) * 1.0 / COUNT(*), 1), 4, 1) AS TESTSUB_AVG, ";
        $query .= "         DECIMAL(ROUND(SUM(CASE WHEN AD.SEX = '1' THEN SD.SCORE ELSE NULL END) * 1.0 / COUNT(CASE WHEN AD.SEX = '1' THEN 1 ELSE NULL END), 1), 4, 1) AS TESTSUB_MEN_AVG, ";
        $query .= "         DECIMAL(ROUND(SUM(CASE WHEN AD.SEX = '2' THEN SD.SCORE ELSE NULL END) * 1.0 / COUNT(CASE WHEN AD.SEX = '2' THEN 1 ELSE NULL END), 1), 4, 1) AS TESTSUB_WOMEN_AVG ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT SD ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DAT RD ";
        $query .= "              ON SD.ENTEXAMYEAR  = RD.ENTEXAMYEAR ";
        $query .= "             AND SD.APPLICANTDIV = RD.APPLICANTDIV ";
        $query .= "             AND SD.TESTDIV      = RD.TESTDIV ";
        $query .= "             AND SD.EXAM_TYPE    = RD.EXAM_TYPE ";
        $query .= "             AND SD.RECEPTNO     = RD.RECEPTNO ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT AD ";
        $query .= "              ON AD.ENTEXAMYEAR  = SD.ENTEXAMYEAR ";
        $query .= "             AND AD.APPLICANTDIV = SD.APPLICANTDIV ";
        $query .= "             AND AD.EXAMNO       = RD.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         SD.ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "         AND SD.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "         AND SD.TESTDIV       = '{$model->testdiv}' ";
        $query .= "         AND SD.EXAM_TYPE     = '1' ";
        $query .= "         AND SD.RECEPTNO IN (SELECT RECEPTNO FROM RECEPTNO_DATA) ";
        $query .= "     GROUP BY ";
        $query .= "         SD.TESTSUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     TSD.TESTSUBCLASSCD, ";
        $query .= "     TSD.TESTSUB_SUM, ";
        $query .= "     TSD.TESTSUB_CNT, ";
        $query .= "     TESTSUB_AVG, ";
        $query .= "     TSD.TESTSUB_MEN_SUM, ";
        $query .= "     TSD.TESTSUB_MEN_CNT, ";
        $query .= "     TESTSUB_MEN_AVG, ";
        $query .= "     TSD.TESTSUB_WOMEN_SUM, ";
        $query .= "     TSD.TESTSUB_WOMEN_CNT, ";
        $query .= "     TESTSUB_WOMEN_AVG, ";
        $query .= "     TSD.TESTSUB_MIN, ";
        $query .= "     TSD.TESTSUB_MAX ";
        $query .= " FROM ";
        $query .= "     TESTSUBCLASS_DATA TSD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'A', ";
        $query .= "     SUM(TSD.TESTSUB_SUM), ";
        $query .= "     SUM(TSD.TESTSUB_CNT), ";
        $query .= "     DECIMAL(ROUND(SUM(TSD.TESTSUB_SUM) * 1.0 / SUM(TSD.TESTSUB_CNT), 1), 4, 1) AS TESTSUB_AVG, ";
        $query .= "     SUM(TSD.TESTSUB_MEN_SUM), ";
        $query .= "     SUM(TSD.TESTSUB_MEN_CNT), ";
        $query .= "     DECIMAL(ROUND(SUM(TSD.TESTSUB_MEN_SUM) * 1.0 / SUM(TSD.TESTSUB_MEN_CNT), 1), 4, 1) AS TESTSUB_MEN_AVG, ";
        $query .= "     SUM(TSD.TESTSUB_WOMEN_SUM), ";
        $query .= "     SUM(TSD.TESTSUB_WOMEN_CNT), ";
        $query .= "     DECIMAL(ROUND(SUM(TSD.TESTSUB_WOMEN_SUM) * 1.0 / SUM(TSD.TESTSUB_WOMEN_CNT), 1), 4, 1) AS TESTSUB_WOMEN_AVG, ";
        $query .= "     MIN(TSD.TESTSUB_MIN), ";
        $query .= "     MAX(TSD.TESTSUB_MAX) ";
        $query .= " FROM ";
        $query .= "     TESTSUBCLASS_DATA TSD ";

        return $query;
    }


    public function getUpdateAttend($model, $db)
    {
        //初期化
        $data = array();
        $data["JUDGEDIV"][TEXT]         = null;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $where  = " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "   AND EXAM_TYPE      = '1' ";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        $data = array();
        $data["JUDGEMENT"][TEXT]         = null;
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";
        $where  = " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //RECEPT更新
        $query = knjl087iQuery::getUpdateAttendCommon($model, "RECEPTNO");
        $db->query($query);
        //BASE更新
        $query = knjl087iQuery::getUpdateAttendCommon($model, "EXAMNO");
        $db->query($query);
    }

    //欠席者更新
    public function &getUpdateAttendCommon($model, $fieldName)
    {
        $allSubCnt = count($model->testSubclassCdArray);
        $updFieldName = ($fieldName == "RECEPTNO") ? "JUDGEDIV" : "JUDGEMENT";
        $tableName = ($fieldName == "RECEPTNO") ? "ENTEXAM_RECEPT_DAT" : "ENTEXAM_APPLICANTBASE_DAT";
        
        $data = array();
        $data[$updFieldName][TEXT]     = '4';
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $where  = " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        if ($fieldName == "RECEPTNO") {
            $where .= "   AND EXAM_TYPE      = '1' ";
        }
        $where .= "   AND {$fieldName} IN ( ";
        $where .= "     SELECT RD.{$fieldName} ";
        $where .= "     FROM ";
        $where .= "         ENTEXAM_RECEPT_DAT RD ";
        $where .= "         LEFT JOIN ENTEXAM_SCORE_DAT SD ";
        $where .= "             ON SD.ENTEXAMYEAR = RD.ENTEXAMYEAR ";
        $where .= "             AND SD.APPLICANTDIV = RD.APPLICANTDIV ";
        $where .= "             AND SD.TESTDIV = RD.TESTDIV ";
        $where .= "             AND SD.EXAM_TYPE = RD.EXAM_TYPE ";
        $where .= "             AND SD.RECEPTNO = RD.RECEPTNO ";
        $where .= "     WHERE ";
        $where .= "        RD.ENTEXAMYEAR       = '{$model->examyear}' ";
        $where .= "        AND RD.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "        AND RD.TESTDIV       = '{$model->testdiv}' ";
        $where .= "        AND RD.EXAM_TYPE     = '1' ";
        $where .= "     GROUP BY RD.{$fieldName} ";
        $where .= "     HAVING SUM(SD.SCORE) IS NULL "; //合計がNULLの生徒を欠席とする
        $where .= "   ) ";

        $query = Query::updateSQL($data, $tableName, $where);
    
        return $query;
    }

    //更新 -- ENTEXAM_RECEPT_DAT
    public function &getUpdateReceptDatQuery($model, $receptData)
    {
        $data = array();
        $data["TOTAL1"][TEXT]       = $receptData["TOTAL1"];
        $data["AVARAGE1"][TEXT]     = $receptData["AVARAGE1"];
        $data["TOTAL_RANK1"][TEXT]  = $receptData["TOTAL_RANK1"];
        $data["SEX_RANK1"][TEXT]    = $receptData["SEX_RANK1"];
        $data["TOTAL4"][TEXT]       = $receptData["TOTAL4"];
        $data["AVARAGE4"][TEXT]     = $receptData["AVARAGE4"];
        $data["TOTAL_RANK4"][TEXT]  = $receptData["TOTAL_RANK4"];
        $data["SEX_RANK4"][TEXT]    = $receptData["SEX_RANK4"];
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

    //更新 -- ENTEXAM_SCORE_DAT
    public function &getUpdateEntexamScoreDat($model, $receptno, $testSubcd, $testSubData)
    {
        $data = array();
        $data["RANK"][TEXT]             = $testSubData["TESTSUBCLASS_SCORE_RANK"];
        $data["SEX_RANK"][TEXT]         = $testSubData["TESTSUBCLASS_SCORE_SEX_RANK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "   AND EXAM_TYPE      = '1' ";
        $where .= "   AND RECEPTNO       = '{$receptno}' ";
        $where .= "   AND TESTSUBCLASSCD = '{$testSubcd}' ";

        $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
    
        return $query;
    }

    //更新 -- ENTEXAM_JUDGE_AVARAGE_DAT
    public function &getUpdateJudgeAvgDatQuery($model, $db, $judgeAvgData)
    {
        
        //削除
        $query .= " DELETE FROM ENTEXAM_JUDGE_AVARAGE_DAT";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND EXAM_TYPE      = '1' ";
        $query .= "   AND TESTSUBCLASSCD = '{$judgeAvgData["TESTSUBCLASSCD"]}' ";
        $db->query($query);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = '1';
        $data["TESTSUBCLASSCD"][TEXT]   = $judgeAvgData["TESTSUBCLASSCD"];
        $data["TOTAL"][TEXT]            = $judgeAvgData["TOTAL"];
        $data["AVARAGE_MEN"][TEXT]      = $judgeAvgData["AVARAGE_MEN"];
        $data["AVARAGE_WOMEN"][TEXT]    = $judgeAvgData["AVARAGE_WOMEN"];
        $data["AVARAGE_TOTAL"][TEXT]    = $judgeAvgData["AVARAGE_TOTAL"];
        $data["MAX_SCORE"][TEXT]        = $judgeAvgData["MAX_SCORE"];
        $data["MIN_SCORE"][TEXT]        = $judgeAvgData["MIN_SCORE"];
        $data["COUNT"][TEXT]            = $judgeAvgData["COUNT"];
        $data["COUNT_MEN"][TEXT]        = $judgeAvgData["COUNT_MEN"];
        $data["COUNT_WOMEN"][TEXT]      = $judgeAvgData["COUNT_WOMEN"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_JUDGE_AVARAGE_DAT");
        $db->query($query);
    }
}
