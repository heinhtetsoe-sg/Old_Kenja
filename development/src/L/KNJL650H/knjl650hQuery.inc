<?php
class knjl650hQuery extends Query
{
    //入試日程取得
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getTestsubclasscd($model, $testsubclasscd = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD || ':' || S1.NAME1 AS LABEL, ";
        $query .= "     T1.TESTSUBCLASSCD AS VALUE, ";
        $query .= "     S1.NAME1 AS TESTSUBCLASS_NAME, ";
        $query .= "     P1.PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_PERFECT_EXAMTYPE_MST P1 ";
        $query .= "          ON P1.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "         AND P1.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "         AND P1.TESTDIV        = T1.TESTDIV ";
        $query .= "         AND P1.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= "         AND P1.EXAM_TYPE      = T1.EXAM_TYPE ";
        $query .= "         AND P1.COURSECD       = '0' ";
        $query .= "         AND P1.MAJORCD        = '000' ";
        $query .= "         AND P1.EXAMCOURSECD   = '0000' ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST S1 ";
        $query .= "          ON S1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L009' ";
        $query .= "         AND S1.SEQ          = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";
        if ($testsubclasscd) {
            $query .= "     AND T1.TESTSUBCLASSCD = '{$testsubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CAST(VALUE AS INTEGER) ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     SCORE_D.SCORE, ";
        $query .= "     SCORE_D.ATTEND_FLG, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     RECEPT.ENTEXAMYEAR, ";
        $query .= "     RECEPT.TESTDIV, ";
        $query .= "     SCORE_D.TESTSUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_D ";
        $query .= "          ON SCORE_D.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND SCORE_D.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND SCORE_D.TESTDIV = RECEPT.TESTDIV ";
        $query .= "         AND SCORE_D.EXAM_TYPE = RECEPT.EXAM_TYPE ";
        $query .= "         AND SCORE_D.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "         AND SCORE_D.TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
        $query .= " WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND RECEPT.EXAM_TYPE    = '{$model->exam_type}' ";
        //受験番号範囲
        if ($model->s_receptno) {
            $query .= "     AND RECEPT.RECEPTNO >= '{$model->s_receptno}' ";
        }
        if ($model->e_receptno) {
            $query .= "     AND RECEPT.RECEPTNO <= '{$model->e_receptno}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    //得点データの存在チェック
    public function getExistsScoreSql($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $query .= "   AND EXAM_TYPE      = '{$model->exam_type}' ";
        $query .= "   AND RECEPTNO       = '{$receptno}' ";
        $query .= "   AND TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
        return $query;
    }

    //更新(エンター押下時にひとりのデータを更新）
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $receptno => $value) {
            //存在チェック
            $dataCnt = $db->getOne(knjl650hQuery::getExistsScoreSql($model, $receptno));

            //データセット
            $data = array();
            if ($value["JUDGEMENT"] == "3" || $value["SCORE"] == "*") {
                $data["SCORE"][NUMBER]          = "";
                $data["ATTEND_FLG"][TEXT]       = "1";
            } else {
                $data["SCORE"][NUMBER]          = $value["SCORE"];
                $data["ATTEND_FLG"][TEXT]       = "";
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $where .= "   AND EXAM_TYPE     = '{$model->exam_type}' ";
                $where .= "   AND RECEPTNO      = '{$receptno}' ";
                $where .= "   AND TESTSUBCLASSCD= '{$model->testsubclasscd}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
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
            $dataCnt = $db->getOne(knjl650hQuery::getExistsScoreSql($model, $data_arr[$i]["RECEPTNO"]));

            //データセット
            $data = array();
            if ($data_arr[$i]["SCORE"] == "*") {
                $data["SCORE"][NUMBER]          = "";
                $data["ATTEND_FLG"][TEXT]       = "1";
            } else {
                $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE"];
                $data["ATTEND_FLG"][TEXT]       = "";
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV        = '{$data_arr[$i]["TESTDIV"]}' ";
                $where .= "   AND EXAM_TYPE      = '{$model->exam_type}' ";
                $where .= "   AND RECEPTNO       = '{$data_arr[$i]["RECEPTNO"]}' ";
                $where .= "   AND TESTSUBCLASSCD = '{$data_arr[$i]["TESTSUBCLASSCD"]}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
                $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
                $data["TESTSUBCLASSCD"][TEXT]   = $data_arr[$i]["TESTSUBCLASSCD"];
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

    //設定マスタ登録コードチェック
    public function checkSettingMst($model, $settingcd, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND SETTING_CD   = '{$settingcd}' ";
        $query .= "     AND SEQ          = '{$seq}' ";

        return $query;
    }

    //受験番号
    public function checkReceptNo($model, $receptNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND RECEPTNO     = '{$receptNo}' ";

        return $query;
    }
}
?>
