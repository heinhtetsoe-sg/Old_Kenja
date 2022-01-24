<?php

require_once('for_php7.php');

class knjl451hQuery extends Query
{

    //名称マスタよりデータを取得
    public function getName($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入製設定マスタよりデータを取得
    public function getEntexamSettingMst($model, $settingCd)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE , ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     ABBV3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND SETTING_CD   = '{$settingCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望コース取得
    public function getExamCourseMst($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験型取得
    public function getExamtypeMst($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE AS VALUE, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND REMARK1      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格者数(確認)
    public function getCntJudge($model)
    {
        $query  = " WITH T_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '1' THEN 1 ELSE 0 END) AS GOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN T1.JUDGEDIV = '2' THEN 1 ELSE 0 END) AS FUGOUKAKU_CNT, ";
        $query .= "         SUM(CASE WHEN T1.NAIDAKU_FLG = '1' THEN 1 ELSE 0 END) AS NAIDAKU_CNT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_JUDGE_TMP T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND T1.EXAM_TYPE    = '{$model->examtype}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GOUKAKU_CNT, ";
        $query .= "     FUGOUKAKU_CNT, ";
        $query .= "     NAIDAKU_CNT ";
        $query .= " FROM ";
        $query .= "     T_CNT ";

        return $query;
    }

    //合格者数(確定)
    public function getCntPassingmark($model)
    {
        $query  = " SELECT ";
        $query .= "     P1.BORDER_SCORE, ";
        $query .= "     P1.SUCCESS_CNT AS GOUKAKU_CNT, ";
        $query .= "     P1.SUCCESS_CNT_SPECIAL AS NAIDAKU_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PASSINGMARK_MST P1 ";
        $query .= " WHERE ";
        $query .= "         P1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND P1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND P1.TESTDIV = '{$model->testdiv}' ";
        $query .= "     AND P1.EXAM_TYPE = '{$model->examtype}' ";
        $query .= "     AND P1.SHDIV = '{$model->shdiv}' ";
        $query .= "     AND P1.EXAMCOURSECD = '{$model->coursecd}' ";

        return $query;
    }

    //確定結果一覧
    public function selectQueryPassingmark($model)
    {
        $query  = " SELECT ";
        $query .= "     P1.TESTDIV, ";
        $query .= "     T1.NAME1 AS TESTDIV_NAME, ";
        $query .= "     P1.EXAMCOURSECD, ";
        $query .= "     C1.EXAMCOURSE_NAME, ";
        $query .= "     P1.EXAM_TYPE, ";
        $query .= "     E1.EXAMTYPE_NAME, ";
        $query .= "     P1.SHDIV, ";
        $query .= "     S1.NAME1 AS SHDIV_NAME, ";
        $query .= "     P1.BORDER_SCORE, ";
        $query .= "     P1.SUCCESS_CNT AS GOUKAKU_CNT, ";
        $query .= "     P1.SUCCESS_CNT_SPECIAL AS NAIDAKU_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PASSINGMARK_MST P1 ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST T1 ON T1.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND T1.SETTING_CD   = 'L004' ";
        $query .= "         AND T1.SEQ          = P1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND C1.EXAMCOURSECD = P1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_MST E1 ON E1.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "         AND E1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND E1.EXAM_TYPE    = P1.EXAM_TYPE ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST S1 ON S1.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L006' ";
        $query .= "         AND S1.SEQ          = P1.SHDIV ";
        $query .= " WHERE ";
        $query .= "         P1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND P1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     P1.TESTDIV, ";
        $query .= "     P1.EXAMCOURSECD, ";
        $query .= "     P1.EXAM_TYPE, ";
        $query .= "     P1.SHDIV ";

        return $query;
    }

    //シミュレーションSQL
    public function simSql($model)
    {
        //志願者母集団
        $query  = " WITH RECEPT_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPT.ENTEXAMYEAR, ";
        $query .= "         RECEPT.APPLICANTDIV, ";
        $query .= "         RECEPT.TESTDIV, ";
        $query .= "         RECEPT.EXAM_TYPE, ";
        $query .= "         RECEPT.RECEPTNO, ";
        $query .= "         RECEPT.EXAMNO, ";
        $query .= "         RECEPT.TOTAL4 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR      = '{$model->year}' ";
        $query .= "         AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND RECEPT.EXAM_TYPE    = '{$model->examtype}' ";
        $query .= "         AND RECEPT.TOTAL4 IS NOT NULL "; //合計点がある人
        $query .= "         AND VALUE(RECEPT.JUDGEDIV, '') <> '1' "; //「1:合格」していない人
        $query .= "         AND EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_RECEPT_DETAIL_DAT RDETAIL ";
        $query .= "             WHERE ";
        $query .= "                     RDETAIL.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "                 AND RDETAIL.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "                 AND RDETAIL.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "                 AND RDETAIL.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
        $query .= "                 AND RDETAIL.RECEPTNO     = RECEPT.RECEPTNO ";
        $query .= "                 AND RDETAIL.SEQ          IN ('016','017','018') "; //第1～3志望
        $query .= "                 AND RDETAIL.REMARK1      = '{$model->coursecd}' ";
        $query .= "                 AND RDETAIL.REMARK2      = '{$model->shdiv}' ";
        $query .= "         ) ";
        //宗内生情報
        $query .= " ), BORDER_SCORE_CONDITION1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPT.RECEPTNO, ";
        $query .= "         BD005.REMARK1 AS SYUNAI_FLG ";
        $query .= "     FROM ";
        $query .= "         RECEPT_DATA RECEPT ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "             ON BD005.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND BD005.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND BD005.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "             AND BD005.SEQ          = '005' ";
        //第1回の試験の受験情報
        $query .= " ), BORDER_SCORE_CONDITION2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPT.RECEPTNO, ";
        $query .= "         '1' AS TESTDIV1_FLG ";
        $query .= "     FROM ";
        $query .= "         RECEPT_DATA RECEPT ";
        $query .= "     WHERE ";
        $query .= "         EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_RECEPT_DAT RECEPT2 ";
        $query .= "                 INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "                     ON BASE.ENTEXAMYEAR   = RECEPT2.ENTEXAMYEAR ";
        $query .= "                     AND BASE.APPLICANTDIV = RECEPT2.APPLICANTDIV ";
        $query .= "                     AND BASE.EXAMNO       = RECEPT2.EXAMNO ";
        $query .= "             WHERE ";
        $query .= "                 RECEPT2.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "                 AND RECEPT2.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "                 AND RECEPT2.EXAMNO        = RECEPT.EXAMNO ";
        $query .= "                 AND RECEPT2.TESTDIV       = '1' ";
        $query .= "         ) ";
        //内諾者情報
        $query .= " ), BORDER_SCORE_CONDITION3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         RECEPT.RECEPTNO, ";
        $query .= "         '1' AS NAIDAKU_FLG ";
        $query .= "     FROM ";
        $query .= "         RECEPT_DATA RECEPT ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD006 ";
        $query .= "              ON CD006.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND CD006.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND CD006.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "             AND CD006.SEQ          = '006' ";
        $query .= "             AND CD006.REMARK10     = '{$model->coursecd}' "; //最終内諾
        $query .= " ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     RECEPT.ENTEXAMYEAR, ";
        $query .= "     RECEPT.APPLICANTDIV, ";
        $query .= "     RECEPT.TESTDIV, ";
        $query .= "     RECEPT.EXAM_TYPE, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.TOTAL4, ";
        $query .= "     C1.SYUNAI_FLG, ";
        $query .= "     C2.TESTDIV1_FLG, ";
        $query .= "     C3.NAIDAKU_FLG ";
        $query .= " FROM ";
        $query .= "     RECEPT_DATA RECEPT ";
        $query .= "     LEFT JOIN BORDER_SCORE_CONDITION1 C1 ON C1.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "     LEFT JOIN BORDER_SCORE_CONDITION2 C2 ON C2.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "     LEFT JOIN BORDER_SCORE_CONDITION3 C3 ON C3.RECEPTNO = RECEPT.RECEPTNO ";

        return $query;
    }

    //シミュレーション
    public function sim($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //仮判定データすべて削除
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $db->query($query);

        //合格判定
        $query = knjl451hQuery::simSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //合格基準点算出(入力基準点に条件に応じた比率を乗算)
            $rate = ($row["SYUNAI_FLG"] == "1") ? 0.9 : ($row["APPLICANTDIV"] == "2" && $row["TESTDIV"] == "2" && $row["TESTDIV1_FLG"] == "1") ? 0.95 : 1.0;
            $borderScore = $model->field["BORDER_SCORE"] * $rate;

            //合否判定
            $row["JUDGEDIV"] = ($row["NAIDAKU_FLG"] == "1") ? "1" : ($row["TOTAL4"] >= $borderScore) ? "1" : "2";

            //更新
            $insQuery = knjl451hQuery::getUpdateJudgeTmp($row);
            $db->query($insQuery);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function getUpdateJudgeTmp($row)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $row["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT] = $row["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]      = $row["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]    = $row["EXAM_TYPE"];
        $data["RECEPTNO"][TEXT]     = $row["RECEPTNO"];
        $data["EXAMNO"][TEXT]       = $row["EXAMNO"];
        $data["JUDGEDIV"][TEXT]     = $row["JUDGEDIV"];
        $data["NAIDAKU_FLG"][TEXT]  = $row["NAIDAKU_FLG"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_JUDGE_TMP");

        return $query;
    }

    //確定
    public function decision(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受付データ（第1～3志望）
        //1:合格 2:不合格
        $judgeArray = array(1, 2);
        foreach ($judgeArray as $judge) {
            $query  = " UPDATE ";
            $query .= "     ENTEXAM_RECEPT_DETAIL_DAT T1 ";
            $query .= " SET ";
            $query .= "     (T1.REMARK3,UPDATED) = ";
            $query .= "     ( ";
            $query .= "     '{$judge}', ";
            $query .= "     SYSDATE() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
            $query .= "     AND T1.EXAM_TYPE    = '{$model->examtype}' ";
            $query .= "     AND T1.REMARK1      = '{$model->coursecd}' ";
            $query .= "     AND T1.REMARK2      = '{$model->shdiv}' ";
            $query .= "     AND T1.SEQ          IN ('016','017','018') ";
            $query .= "     AND T1.RECEPTNO     IN (SELECT RECEPTNO FROM ENTEXAM_JUDGE_TMP WHERE JUDGEDIV = '{$judge}' ) ";

            $db->query($query);
        }

        //受付データ
        //1:合格 2:不合格
        $judgeArray = array(1, 2);
        foreach ($judgeArray as $judge) {
            $query  = " UPDATE ";
            $query .= "     ENTEXAM_RECEPT_DAT T1 ";
            $query .= " SET ";
            $query .= "     (T1.JUDGEDIV,UPDATED) = ";
            $query .= "     ( ";
            $query .= "     '{$judge}', ";
            $query .= "     SYSDATE() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
            $query .= "     AND T1.EXAM_TYPE    = '{$model->examtype}' ";
            $query .= "     AND T1.RECEPTNO     IN (SELECT RECEPTNO FROM ENTEXAM_JUDGE_TMP WHERE JUDGEDIV = '{$judge}' ) ";

            $db->query($query);
        }

        //志願者基礎データ
        //1:合格 2:不合格
        foreach ($judgeArray as $judge) {
            $query  = " UPDATE ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " SET ";
            $query .= "     (T1.JUDGEMENT,UPDATED) = ";
            $query .= "     ( ";
            $query .= "     '{$judge}', ";
            $query .= "     sysdate() ";
            $query .= "     ) ";
            $query .= " WHERE ";
            $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND T1.EXAMNO       IN (SELECT EXAMNO FROM ENTEXAM_JUDGE_TMP WHERE JUDGEDIV = '{$judge}' ) ";
            //「1:合格」を「2:不合格」で上書きしない
            if ($judge == "2") {
                $query .= "     AND VALUE(T1.JUDGEMENT, '') <> '1' ";
            }

            $db->query($query);
        }

        //合格点マスタ
        $query  = " DELETE FROM ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->examtype}' ";
        $query .= "     AND SHDIV        = '{$model->shdiv}' ";
        $query .= "     AND COURSECD     = '0' ";
        $query .= "     AND MAJORCD      = '000' ";
        $query .= "     AND EXAMCOURSECD = '{$model->coursecd}' ";
        $db->query($query);

        //固定ALL0
        $course = "00000000";
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]       = $model->year;
        $data["APPLICANTDIV"][TEXT]      = $model->applicantdiv;
        $data["TESTDIV"][TEXT]           = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]         = $model->examtype;
        $data["SHDIV"][TEXT]             = $model->shdiv;
        $data["COURSECD"][TEXT]          = '0';
        $data["MAJORCD"][TEXT]           = '000';
        $data["EXAMCOURSECD"][TEXT]      = $model->coursecd;
        $data["BORDER_SCORE"][NUMBER]    = $model->field["BORDER_SCORE"];
        $data["SUCCESS_CNT"][NUMBER]     = $model->field["GOUKAKU_CNT"];
        $data["SUCCESS_CNT_SPECIAL"][NUMBER] = $model->field["NAIDAKU_CNT"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ENTEXAM_PASSINGMARK_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
