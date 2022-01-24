<?php
class knjl651hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

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
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //面接評価取得
    public function getInterview($year, $applicantdiv, $seq = "", $default = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND SETTING_CD   = 'L027' ";
        if ($seq) {
            $query .= " AND SEQ = '{$seq}' ";
        }
        if ($default) {
            $query .= " AND NAMESPARE2 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $setReceptno = "")
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     INTERVIEW.INTERVIEW_A, ";
        $query .= "     INTERVIEW.ATTEND_FLG, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     RECEPT.ENTEXAMYEAR, ";
        $query .= "     RECEPT.TESTDIV, ";
        $query .= "     L027.NAME1 AS HYOKA ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "          ON INTERVIEW.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND INTERVIEW.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND INTERVIEW.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "         AND INTERVIEW.EXAMNO       = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SETTING_MST L027 ";
        $query .= "          ON L027.ENTEXAMYEAR  = INTERVIEW.ENTEXAMYEAR ";
        $query .= "         AND L027.APPLICANTDIV = INTERVIEW.APPLICANTDIV ";
        $query .= "         AND L027.SETTING_CD   = 'L027' ";
        $query .= "         AND L027.SEQ          = INTERVIEW.INTERVIEW_A ";
        $query .= " WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND RECEPT.EXAM_TYPE    = '{$model->exam_type}' ";
        if ($setReceptno != "") {
            $query .= "  AND RECEPT.RECEPTNO = '{$setReceptno}' ";
        } else {
            //受験番号範囲
            if ($model->s_receptno) {
                $query .= "     AND RECEPT.RECEPTNO >= '{$model->s_receptno}' ";
            }
            if ($model->e_receptno) {
                $query .= "     AND RECEPT.RECEPTNO <= '{$model->e_receptno}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";

        return $query;
    }

    //面接データの存在チェック
    public function getExistsInterviewSql($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->interview as $receptno => $value) {
            //EXAMNO
            $examno = $value["EXAMNO"];

            //存在チェック
            $dataCnt = $db->getOne(knjl651hQuery::getExistsInterviewSql($model, $examno));

            //データセット
            $data = array();
            if ($value["JUDGEMENT"] == "3") {
                $data["INTERVIEW_A"][TEXT] = "";
                $data["ATTEND_FLG"][TEXT]  = "1"; // 「0:面接出席」「1:面接欠席」
            } else {
                $data["INTERVIEW_A"][TEXT] = !strlen($value["INTERVIEW_A"]) ? "" : $value["INTERVIEW_A"];
                $data["ATTEND_FLG"][TEXT]  = !strlen($value["ATTEND_FLG"]) ? "0" : $value["ATTEND_FLG"]; // 「0:面接出席」「1:面接欠席」
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $where .= "   AND EXAMNO        = '{$examno}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
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
            //１件取得
            $query = knjl651hQuery::selectQuery($model, $data_arr[$i]["RECEPTNO"]);
            $row = $db->getrow($query, DB_FETCHMODE_ASSOC);

            //EXAMNO
            $examno = $row["EXAMNO"];

            //存在チェック
            $dataCnt = $db->getOne(knjl651hQuery::getExistsInterviewSql($model, $examno));

            $intaviewA = "";
            $hyouka = $data_arr[$i]["HYOKA"];
            if ($hyouka != "" && $hyouka != "*") {
                $query = knjl651hQuery::getInterview($this->ObjYear, $this->applicantdiv);
                $result = $db->query($query);
                while ($rowIntaview = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($hyouka == $rowIntaview["LABEL"]) {
                        $intaviewA = $rowIntaview["VALUE"];
                        break;
                    }
                }
                $result->free();
            }

            //データセット
            $data = array();
            if ($row["JUDGEMENT"] == "3" || $hyouka == "*") {
                $data["INTERVIEW_A"][TEXT] = "";
                $data["ATTEND_FLG"][TEXT]  = "1"; // 「0:面接出席」「1:面接欠席」
            } else {
                $data["INTERVIEW_A"][TEXT] = !strlen($intaviewA) ? "" : $intaviewA;
                $data["ATTEND_FLG"][TEXT]  = "0"; // 「0:面接出席」「1:面接欠席」
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $where .= "   AND EXAMNO        = '{$examno}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
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
