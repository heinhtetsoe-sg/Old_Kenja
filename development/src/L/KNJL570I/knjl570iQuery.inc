<?php
class knjl570iQuery extends Query
{
    //入試年度一覧取得
    public function selectYearQuery($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_RECEPT_DAT ";
        $query .= "  WHERE ";
        $query .= "     APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->entexamyear}', '{$model->entexamyear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $settngCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     SEQ AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->entexamyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND SETTING_CD = '".$settngCd."' ";
        if ($namecd2) {
            $query .= " AND SEQ = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタより取得
    public function getTestDivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '{$model->entexamyear}' ";
        $query .= "     AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        if ($testdiv != "") {
            $query .= "   AND TESTDIV = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース一覧取得
    public function getMajorCourcecd($model)
    {
        $query  = " SELECT ";
        $query .= "     MAJOR_COURCE.GENERAL_CD || ':' || MAJOR_COURCE.GENERAL_NAME AS LABEL, ";
        $query .= "     MAJOR_COURCE.GENERAL_CD   AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST MAJOR_COURCE ";
        $query .= "     INNER JOIN ENTEXAM_GENERAL_MST SUBJECT ON ";
        $query .= "         SUBJECT.ENTEXAMYEAR  = MAJOR_COURCE.ENTEXAMYEAR ";
        $query .= "     AND SUBJECT.APPLICANTDIV = MAJOR_COURCE.APPLICANTDIV ";
        $query .= "     AND SUBJECT.TESTDIV      = MAJOR_COURCE.TESTDIV  ";
        $query .= "     AND SUBJECT.GENERAL_DIV  = '01' "; // '01'：類 固定
        $query .= "     AND SUBJECT.GENERAL_CD   = MAJOR_COURCE.REMARK1 ";
        $query .= "     AND SUBJECT.REMARK1      = '{$model->majorcd}' ";
        $query .= " WHERE ";
        $query .= "         MAJOR_COURCE.ENTEXAMYEAR  = '{$model->entexamyear}' ";
        $query .= "     AND MAJOR_COURCE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND MAJOR_COURCE.TESTDIV      = '0' ";  // '0' 固定
        $query .= "     AND MAJOR_COURCE.GENERAL_DIV  = '02' "; // '02'：コース 固定
        $query .= " ORDER BY ";
        $query .= "     VALUE(MAJOR_COURCE.GENERAL_CD, 0) ";

        return $query;
    }

    //判定マーク一覧取得
    public function getJudgeCourceQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_CD || ':' || GENERAL_MARK AS LABEL, ";
        $query .= "     GENERAL_CD AS VALUE, ";
        $query .= "     REMARK1 AS PASS_COURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->entexamyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '0' ";  // '0' 固定
        $query .= "     AND GENERAL_DIV  = '03' "; // '03'：判定マーク 固定
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, '') ";

        return $query;
    }

    //特待コード一覧取得
    public function getHonordivQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_CD || ':' || GENERAL_NAME AS LABEL, ";
        $query .= "     GENERAL_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->entexamyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '0' ";  // '0' 固定
        $query .= "     AND GENERAL_DIV  = '04' "; // '04'：特待コード 固定
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, '') ";

        return $query;
    }

    //特待理由コード一覧取得
    public function getHonorReasondivQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_CD || ':' || GENERAL_NAME AS LABEL, ";
        $query .= "     GENERAL_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->entexamyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '0' ";  // '0' 固定
        $query .= "     AND GENERAL_DIV  = '05' "; // '05'：特待理由コード 固定
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, '') ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $getDefaultTokutaiFlg)
    {
        $query .= " WITH L009 AS ( ";
        $query .= "     SELECT ";
        $query .= "         ROWNUMBER() OVER() AS ROW_NUMBER, ";
        $query .= "         SEQ ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->entexamyear."' AND ";
        $query .= "         APPLICANTDIV = '".$model->applicantdiv."'    AND ";
        $query .= "         SETTING_CD   = 'L009' ";
        $query .= "     ORDER BY ";
        $query .= "         VALUE(SEQ, 0) ";
        $query .= " ), ";
        $query .= " SCORE1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCORE.ENTEXAMYEAR, ";
        $query .= "         SCORE.APPLICANTDIV, ";
        $query .= "         SCORE.TESTDIV, ";
        $query .= "         SCORE.RECEPTNO, ";
        $query .= "         SCORE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT AS SCORE ";
        $query .= "         INNER JOIN L009 ON ";
        $query .= "                    L009.ROW_NUMBER = 1 ";
        $query .= "                AND SEQ             = SCORE.TESTSUBCLASSCD ";
        $query .= " ), ";
        $query .= " SCORE2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCORE.ENTEXAMYEAR, ";
        $query .= "         SCORE.APPLICANTDIV, ";
        $query .= "         SCORE.TESTDIV, ";
        $query .= "         SCORE.RECEPTNO, ";
        $query .= "         SCORE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT AS SCORE ";
        $query .= "         INNER JOIN L009 ON ";
        $query .= "                    L009.ROW_NUMBER = 2 ";
        $query .= "                AND SEQ             = SCORE.TESTSUBCLASSCD ";
        $query .= " ), ";
        $query .= " SCORE3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCORE.ENTEXAMYEAR, ";
        $query .= "         SCORE.APPLICANTDIV, ";
        $query .= "         SCORE.TESTDIV, ";
        $query .= "         SCORE.RECEPTNO, ";
        $query .= "         SCORE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT AS SCORE ";
        $query .= "         INNER JOIN L009 ON ";
        $query .= "                    L009.ROW_NUMBER = 3 ";
        $query .= "                AND SEQ             = SCORE.TESTSUBCLASSCD ";
        $query .= " ), ";
        $query .= " SCORE4 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCORE.ENTEXAMYEAR, ";
        $query .= "         SCORE.APPLICANTDIV, ";
        $query .= "         SCORE.TESTDIV, ";
        $query .= "         SCORE.RECEPTNO, ";
        $query .= "         SCORE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT AS SCORE ";
        $query .= "         INNER JOIN L009 ON ";
        $query .= "                    L009.ROW_NUMBER = 4 ";
        $query .= "                AND SEQ             = SCORE.TESTSUBCLASSCD ";
        $query .= " ), ";
        $query .= " SCORE5 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCORE.ENTEXAMYEAR, ";
        $query .= "         SCORE.APPLICANTDIV, ";
        $query .= "         SCORE.TESTDIV, ";
        $query .= "         SCORE.RECEPTNO, ";
        $query .= "         SCORE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SCORE_DAT AS SCORE ";
        $query .= "         INNER JOIN L009 ON ";
        $query .= "                    L009.ROW_NUMBER = 5 ";
        $query .= "                AND SEQ             = SCORE.TESTSUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     FS.FINSCHOOL_NAME, ";
        $query .= "     VALUE(BASE_D031.REMARK1, '0') || VALUE(BASE_D031.REMARK2, '0') || VALUE(BASE_D031.REMARK3, '0') || VALUE(BASE_D031.REMARK4, '0') AS HOPE_COURCE, ";
        $query .= "     CONSULTATION_COURCE.GENERAL_ABBV AS CONSULTATION_COURCE, ";
        $query .= "     VALUE(HONOR_NAME1.GENERAL_MARK, '') || VALUE(HONOR_NAME2.GENERAL_MARK, '') AS HONOR_NAME, ";
        $query .= "     CASE BASE_D031.REMARK6 WHEN '1' THEN '●' ELSE NULL END AS SCHOLARSHIPS_DESIRE, ";
        $query .= "     CON.AVERAGE_ALL, ";
        $query .= "     CON_D009.REMARK4 AS COMMON_TEST, ";
        $query .= "     VALUE(CON.ABSENCE_DAYS, 0) + VALUE(CON.ABSENCE_DAYS2, 0) + VALUE(CON.ABSENCE_DAYS3, 0) AS ABSENCE_DAYS, ";
        $query .= "     BASE_D031.REMARK5 AS PAST, ";
        $query .= "     SCORE1.SCORE AS SCORE1, ";
        $query .= "     SCORE2.SCORE AS SCORE2, ";
        $query .= "     SCORE3.SCORE AS SCORE3, ";
        $query .= "     SCORE4.SCORE AS SCORE4, ";
        $query .= "     SCORE5.SCORE AS SCORE5, ";
        $query .= "     RECEPT.TOTAL4, ";
        $query .= "     RECEPT.TOTAL_RANK1, ";
        $query .= "     RECEPT.TOTAL_RANK3, ";
        $query .= "     RECEPT_D015.REMARK3 AS JUDGE_COURCE, ";
        $query .= "     RECEPT_D015.REMARK1 AS JUDGE_COURCE_KARI, ";
        if ($getDefaultTokutaiFlg) {
            //「特待参照」押下時、判定結果が合格以外の人は特待情報をコピーしない(未受験や不合格)
            $query .= "     (CASE WHEN JUDGE_MARK.REMARK1 IS NOT NULL THEN CON_D009.REMARK2 ELSE NULL END) AS HONORDIV, ";
            $query .= "     (CASE WHEN JUDGE_MARK.REMARK1 IS NOT NULL THEN CON_D009.REMARK3 ELSE NULL END) AS HONOR_REASONDIV ";
        } else {
            $query .= "     RECEPT_D015.REMARK4 AS HONORDIV, ";
            $query .= "     RECEPT_D015.REMARK5 AS HONOR_REASONDIV ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT AS RECEPT ";

        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT AS BASE ON ";
        $query .= "               BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND BASE.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND BASE.EXAMNO       = RECEPT.EXAMNO ";

        $query .= "     LEFT JOIN V_NAME_MST AS Z002 ON ";
        $query .= "               Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";

        $query .= "     LEFT JOIN FINSCHOOL_MST AS FS ON  ";
        $query .= "               FS.FINSCHOOLCD = BASE.FS_CD ";

        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AS BASE_D031 ON  ";
        $query .= "               BASE_D031.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE_D031.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND BASE_D031.EXAMNO       = BASE.EXAMNO ";
        $query .= "           AND BASE_D031.SEQ          = '031' ";

        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT AS CON ON ";
        $query .= "               CON.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "           AND CON.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND CON.EXAMNO       = BASE.EXAMNO ";

        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT AS CON_D009 ON ";
        $query .= "               CON_D009.ENTEXAMYEAR  = CON.ENTEXAMYEAR ";
        $query .= "           AND CON_D009.APPLICANTDIV = CON.APPLICANTDIV ";
        $query .= "           AND CON_D009.EXAMNO       = CON.EXAMNO ";
        $query .= "           AND CON_D009.SEQ          = '009' ";

        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST AS CONSULTATION_COURCE ON ";
        $query .= "               CONSULTATION_COURCE.ENTEXAMYEAR  = CON_D009.ENTEXAMYEAR ";
        $query .= "           AND CONSULTATION_COURCE.APPLICANTDIV = CON_D009.APPLICANTDIV ";
        $query .= "           AND CONSULTATION_COURCE.TESTDIV      = '0' ";  // '0' 固定
        $query .= "           AND CONSULTATION_COURCE.GENERAL_DIV  = '02' "; // '02':コース 固定
        $query .= "           AND CONSULTATION_COURCE.GENERAL_CD   = CON_D009.REMARK1 ";

        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST AS HONOR_NAME1 ON ";
        $query .= "               HONOR_NAME1.ENTEXAMYEAR  = CON_D009.ENTEXAMYEAR ";
        $query .= "           AND HONOR_NAME1.APPLICANTDIV = CON_D009.APPLICANTDIV ";
        $query .= "           AND HONOR_NAME1.TESTDIV      = '0' ";  // '0' 固定
        $query .= "           AND HONOR_NAME1.GENERAL_DIV  = '05' "; // '05':特待理由コード 固定
        $query .= "           AND HONOR_NAME1.GENERAL_CD   = CON_D009.REMARK3 ";

        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST AS HONOR_NAME2 ON ";
        $query .= "               HONOR_NAME2.ENTEXAMYEAR  = CON_D009.ENTEXAMYEAR ";
        $query .= "           AND HONOR_NAME2.APPLICANTDIV = CON_D009.APPLICANTDIV ";
        $query .= "           AND HONOR_NAME2.TESTDIV      = '0' ";  // '0' 固定
        $query .= "           AND HONOR_NAME2.GENERAL_DIV  = '04' "; // '04':特待コード 固定
        $query .= "           AND HONOR_NAME2.GENERAL_CD   = CON_D009.REMARK2 ";

        $query .= "     LEFT JOIN SCORE1 ON ";
        $query .= "               SCORE1.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE1.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE1.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND SCORE1.RECEPTNO     = RECEPT.RECEPTNO ";

        $query .= "     LEFT JOIN SCORE2 ON ";
        $query .= "               SCORE2.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE2.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE2.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND SCORE2.RECEPTNO     = RECEPT.RECEPTNO ";

        $query .= "     LEFT JOIN SCORE3 ON ";
        $query .= "               SCORE3.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE3.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE3.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND SCORE3.RECEPTNO     = RECEPT.RECEPTNO ";

        $query .= "     LEFT JOIN SCORE4 ON ";
        $query .= "               SCORE4.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE4.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE4.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND SCORE4.RECEPTNO     = RECEPT.RECEPTNO ";

        $query .= "     LEFT JOIN SCORE5 ON ";
        $query .= "               SCORE5.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND SCORE5.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND SCORE5.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND SCORE5.RECEPTNO     = RECEPT.RECEPTNO ";

        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT AS RECEPT_D015 ON ";
        $query .= "               RECEPT_D015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "           AND RECEPT_D015.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "           AND RECEPT_D015.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "           AND RECEPT_D015.SEQ          = '015' ";

        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST AS JUDGE_MARK ON ";
        $query .= "               JUDGE_MARK.ENTEXAMYEAR  = CON_D009.ENTEXAMYEAR ";
        $query .= "           AND JUDGE_MARK.APPLICANTDIV = CON_D009.APPLICANTDIV ";
        $query .= "           AND JUDGE_MARK.TESTDIV      = '0' ";  // '0' 固定
        $query .= "           AND JUDGE_MARK.GENERAL_DIV  = '03' "; // '03':判定マークコード 固定
        $query .= "           AND JUDGE_MARK.GENERAL_CD   = RECEPT_D015.REMARK3 ";
        $query .= " WHERE  ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '".$model->entexamyear."' ";
        $query .= " AND RECEPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND RECEPT.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " AND BASE.TESTDIV0       = '".$model->field["MAJORCD"]."' ";
        $query .= " AND BASE_D031.REMARK1   = '".$model->field["MAJOR_COURCECD"]."' ";
        if ($model->field["RECEPTNO"] != "") {
            $query .= " AND RECEPT.RECEPTNO     = '".$model->field["RECEPTNO"]."' ";
        }

        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    public function getUpdateQuery($model, $testdiv)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格扱いの判定マークコードを取得
        $passJudgeCourseArr = array();
        $query = knjl570iQuery::getJudgeCourceQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["PASS_COURSE"] != "") {
                $passJudgeCourseArr[] = $row["VALUE"];
            }
        }

        foreach ($model->updField as $receptno => $value) {
            //判定コード
            $judgeCourse = $model->updField[$receptno]["JUDGE_COURCE"];

            //合否区分
            $passDiv = "";
            if ($judgeCourse) {
                $passDiv = (in_array($judgeCourse, $passJudgeCourseArr)) ? "1" : "2";
            }

            //BASE更新
            $data = array();
            $data["JUDGEMENT"][TEXT]         = $passDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->entexamyear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$receptno}' "; //城東入試ではEXAMNO=RECEPTNO
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //RECEPT更新
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $passDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->entexamyear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$testdiv}' ";
            $where .= "   AND RECEPTNO      = '{$receptno}' ";
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);


            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->entexamyear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND TESTDIV       = '{$testdiv}' ";
            $query .= "   AND RECEPTNO      = '{$receptno}' ";
            $query .= "   AND SEQ           = '015' ";
            $dataCnt = $db->getOne($query);

            //データセット
            $data = array();

            if ($dataCnt > 0) {
                $data["REMARK3"][TEXT]          = $judgeCourse;
                $data["REMARK4"][TEXT]          = $model->updField[$receptno]["HONORDIV"];
                $data["REMARK5"][TEXT]          = $model->updField[$receptno]["HONOR_REASONDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";
                $where  = " WHERE ENTEXAMYEAR   = '{$model->entexamyear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$testdiv}' ";
                $where .= "   AND RECEPTNO      = '{$receptno}' ";
                $where .= "   AND SEQ           = '015' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->entexamyear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $testdiv;
                $data["EXAM_TYPE"][TEXT]        = '1';
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["SEQ"][TEXT]              = '015';
                $data["REMARK3"][TEXT]          = $judgeCourse;
                $data["REMARK4"][TEXT]          = $model->updField[$receptno]["HONORDIV"];
                $data["REMARK5"][TEXT]          = $model->updField[$receptno]["HONOR_REASONDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
