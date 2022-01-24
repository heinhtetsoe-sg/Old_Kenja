<?php
class knjl550iQuery extends Query
{
    //入試区分取得
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $namecd1, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND SETTING_CD   = '".$namecd1."' ";
        if ($seq) {
            $query .= "     AND SEQ   = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //満点マスタ取得
    public function getPerfectExamtypeMst($model)
    {
        $query  = " SELECT ";
        $query .= "     PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR        = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV        = '0' ";
        $query .= "     AND COURSECD       = '0' ";
        $query .= "     AND MAJORCD        = '000' ";
        $query .= "     AND EXAMCOURSECD   = '0000' ";
        $query .= "     AND TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
        $query .= "     AND EXAM_TYPE      = '0' ";

        return $query;
    }

    //試験会場設定データ
    public function getExamHall($model, $examhallcd = "")
    {
        $query  = " SELECT ";
        $query .= "     *, ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '" . $model->ObjYear . "' ";
        $query .= "     AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "     AND TESTDIV      = '" . $model->testdiv . "' ";
        $query .= "     AND EXAM_TYPE    = '" . $model->exam_type . "' ";
        if ($examhallcd) {
            $query .= "     AND EXAMHALLCD    = '" . $examhallcd . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験会場設定データ
    public function getExamHallAllDetail($model)
    {
        $query  = " WITH T_SRECEPTNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         EXAM_TYPE, ";
        $query .= "         S_RECEPTNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_YDAT T1 ";
        $query .= "     WHERE ";
        $query .= "         EXAMHALLCD = ( ";
        $query .= "             SELECT ";
        $query .= "                 MIN(EXAMHALLCD) ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_HALL_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "                 AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                 AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                 AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         ) ";
        $query .= " ), T_ERECEPTNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         EXAM_TYPE, ";
        $query .= "         E_RECEPTNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_YDAT T1 ";
        $query .= "     WHERE ";
        $query .= "         EXAMHALLCD = ( ";
        $query .= "             SELECT ";
        $query .= "                 MAX(EXAMHALLCD) ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_HALL_YDAT T2 ";
        $query .= "             WHERE ";
        $query .= "                 T2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "                 AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                 AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "                 AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         ) ";
        $query .= " ), T_CAPA AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         EXAM_TYPE, ";
        $query .= "         SUM(CAPA_CNT) AS SUM_CAPA ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_YDAT ";
        $query .= "     GROUP BY ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         EXAM_TYPE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.E_RECEPTNO, ";
        $query .= "     T3.SUM_CAPA ";
        $query .= " FROM ";
        $query .= "     T_SRECEPTNO T1 ";
        $query .= "     INNER JOIN T_ERECEPTNO T2 ";
        $query .= "          ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "     INNER JOIN T_CAPA T3 ";
        $query .= "          ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T3.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '" . $model->ObjYear . "' ";
        $query .= "     AND T1.APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "     AND T1.TESTDIV      = '" . $model->testdiv . "' ";
        $query .= "     AND T1.EXAM_TYPE    = '" . $model->exam_type . "' ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        //学科名
        $gakkaName = $model->examTypeList[$model->exam_type];

        $query  = " SELECT ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.TESTDIV0, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     HALL.EXAMHALLCD, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     S1.SCORE, ";
        $query .= "     B1.JUDGEMENT ";
        if ($model->cmd == "csvOutput") {
            $query .= "     , '".$gakkaName."' AS GAKKA_NAME ";
            $query .= "     , SL009.SEQ AS TESTSUBCLASSCD ";
            $query .= "     , SL009.NAME1 AS TESTSUBCLASS_NAME ";
            $query .= "     , T1.TESTDIV_NAME ";
            $query .= "     , H1.EXAMHALL_NAME ";
        }
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_HALL_YDAT HALL ";
        $query .= "          ON HALL.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND HALL.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND HALL.TESTDIV      = B1.TESTDIV ";
        $query .= "         AND HALL.EXAM_TYPE    = B1.TESTDIV0 ";
        $query .= "         AND B1.EXAMNO  BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "          ON S1.ENTEXAMYEAR    = B1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV   = B1.APPLICANTDIV ";
        $query .= "         AND S1.TESTDIV        = B1.TESTDIV ";
        $query .= "         AND S1.EXAM_TYPE      = '1' ";
        $query .= "         AND S1.RECEPTNO       = B1.EXAMNO ";
        $query .= "         AND S1.TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
        if ($model->cmd == "csvOutput") {
            $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST T1 ";
            $query .= "             ON T1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "            AND T1.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "            AND T1.TESTDIV      = B1.TESTDIV ";
            $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT H1 ";
            $query .= "             ON H1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "            AND H1.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "            AND H1.TESTDIV      = B1.TESTDIV ";
            $query .= "            AND H1.EXAM_TYPE    = B1.TESTDIV0 ";
            $query .= "            AND H1.EXAMHALLCD   = HALL.EXAMHALLCD ";
            $query .= "     LEFT JOIN ENTEXAM_SETTING_MST SL009 ";
            $query .= "             ON SL009.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
            $query .= "            AND SL009.APPLICANTDIV = B1.APPLICANTDIV ";
            $query .= "            AND SL009.SETTING_CD   = 'L009' ";
            $query .= "            AND SL009.SEQ          = '{$model->testsubclasscd}' ";
        }
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV       = '{$model->testdiv}' ";    //入試区分
        $query .= "     AND B1.TESTDIV0      = '{$model->exam_type}' ";  //学科
        if ($model->examhallcd != "ALL") {
            $query .= "     AND HALL.EXAMHALLCD  = '{$model->examhallcd}' ";
        }
        //受験番号範囲
        if (!in_array($model->cmd, array("main"))) {
            if ($model->s_receptno) {
                $query .= "     AND B1.EXAMNO >= '{$model->s_receptno}' ";
            }
            if ($model->e_receptno) {
                $query .= "     AND B1.EXAMNO <= '{$model->e_receptno}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    public function getScoreDatCntSql($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND EXAM_TYPE      = '1' ";
        $query .= "   AND RECEPTNO       = '{$receptno}' ";
        $query .= "   AND TESTSUBCLASSCD = '{$model->testsubclasscd}' ";

        return $query;
    }

    public function getBaseJugement($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     B1.JUDGEMENT ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV       = '{$model->testdiv}' ";    //入試区分
        $query .= "     AND B1.TESTDIV0      = '{$model->exam_type}' ";  //学科
        $query .= "     AND B1.EXAMNO        = '{$examno}' ";

        return $query;
    }

    //更新(エンター押下時にひとりのデータを更新）
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $receptno => $value) {
            //存在チェック
            $query = knjl550iQuery::getScoreDatCntSql($model, $receptno);
            $dataCnt = $db->getOne($query);

            //データセット
            $data = array();
            if ($value["JUDGEMENT"] == "4") {
                $data["SCORE"][NUMBER]          = "";
            } else {
                $data["SCORE"][NUMBER]          = $value["SCORE"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
                $where .= "   AND EXAM_TYPE      = '1' ";
                $where .= "   AND RECEPTNO       = '{$receptno}' ";
                $where .= "   AND TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = '1';
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            //存在チェック
            $query = knjl550iQuery::getScoreDatCntSql($model, $data_arr[$i]["EXAMNO"]);
            $dataCnt = $db->getOne($query);

            //JUDGEMENT取得
            $query = knjl550iQuery::getBaseJugement($model, $data_arr[$i]["EXAMNO"]);
            $judgment = $db->getOne($query);

            //データセット
            $data = array();
            if ($judgment == "4") { //欠席
                $data["SCORE"][NUMBER]          = "";
            } else {
                $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
                $where .= "   AND EXAM_TYPE      = '1' ";
                $where .= "   AND RECEPTNO       = '{$data_arr[$i]["EXAMNO"]}' ";
                $where .= "   AND TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = '1';
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            }

            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //受験番号
    public function checkExamNo($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR       = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV       = '{$model->testdiv}' ";    //入試区分
        $query .= "     AND B1.TESTDIV0      = '{$model->exam_type}' ";  //学科
        $query .= "     AND B1.EXAMNO        = '{$examno}' ";

        return $query;
    }
}
