<?php
class knjl540iQuery extends Query
{
    //入試区分マスタ取得
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV      = '{$testdiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
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

    //一覧取得
    public function getSelectQuery($model)
    {
        //学科名
        $gakkaName = $model->examTypeList[$model->exam_type];

        $query  = " SELECT ";
        $query .= "     B1.EXAMNO AS VALUE, ";
        $query .= "     B1.EXAMNO || '：' ||  B1.NAME AS LABEL, ";
        $query .= "     B1.JUDGEMENT, ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.TESTDIV0, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     HALL.EXAMHALLCD, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME ";
        if ($model->cmd == "csvOutput") {
            $query .= "     , '".$gakkaName."' AS GAKKA_NAME ";
            $query .= "     , T1.TESTDIV_NAME ";
            $query .= "     , H1.EXAMHALL_NAME ";
        }
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_HALL_YDAT HALL ";
        $query .= "             ON HALL.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND HALL.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND HALL.TESTDIV      = B1.TESTDIV ";
        $query .= "            AND HALL.EXAM_TYPE    = B1.TESTDIV0 ";
        $query .= "            AND B1.EXAMNO  BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
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
        }
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR       = '" . $model->ObjYear . "' ";
        $query .= "     AND B1.APPLICANTDIV  = '" . $model->applicantdiv . "' ";
        $query .= "     AND B1.TESTDIV       = '" . $model->testdiv . "' ";    //入試区分
        $query .= "     AND B1.TESTDIV0      = '" . $model->exam_type . "' ";  //学科
        $query .= "     AND HALL.EXAMHALLCD  = '" . $model->examhallcd . "' ";
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model, $flg)
    {
        //リストが空の場合
        if ($flg == "left" && !$model->selectLeft) {
            return false;
        } elseif ($flg == "right" && !$model->selectRight) {
            return false;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $selectSchArr = array();

        if ($flg == "left") {
            //左側リスト
            $judge = "4"; //欠席
            $selectSchArr = explode(",", $model->selectLeft);
        } elseif ($flg == "right") {
            //右側リスト
            $judge = ""; //空白
            $selectSchArr = explode(",", $model->selectRight);
        }

        foreach ($selectSchArr as $examNo) {
            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["JUDGEMENT"][TEXT]    = $judge;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examNo}' ";
            if ($flg == "right") {
                $where .= "   AND (JUDGEMENT = '4' OR JUDGEMENT IS NULL) "; //JUDGEMENT '4':欠席 または NULL の場合、更新対象とする
            }
    
            //更新（ENTEXAM_APPLICANTBASE_DAT）
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
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
        $query = knjl540iQuery::getSelectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = array_search($row["EXAMNO"], array_column($data_arr, "EXAMNO"));

            if ($key !== false) {
                if ($data_arr[$key]["ERROR"] == "1") {
                    // エラーとなったレコードは処理しない
                    continue;
                }
                $judge = "4"; //欠席
            } else {
                $judge = "";
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["JUDGEMENT"][TEXT]    = $judge;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$row["EXAMNO"]}' ";
            if ($judge == "") {
                $where .= "   AND (JUDGEMENT = '4' OR JUDGEMENT IS NULL) "; //JUDGEMENT '4':欠席 または NULL の場合、更新対象とする
            }

            //更新（ENTEXAM_APPLICANTBASE_DAT）
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
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
    public function checkReceptNo($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_HALL_YDAT HALL ";
        $query .= "             ON HALL.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "            AND HALL.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "            AND HALL.TESTDIV      = B1.TESTDIV ";
        $query .= "            AND HALL.EXAM_TYPE    = B1.TESTDIV0 ";
        $query .= "            AND B1.EXAMNO  BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR       = '" . $model->ObjYear . "' ";
        $query .= "     AND B1.APPLICANTDIV  = '" . $model->applicantdiv . "' ";
        $query .= "     AND B1.TESTDIV       = '" . $model->testdiv . "' ";    //入試区分
        $query .= "     AND B1.TESTDIV0      = '" . $model->exam_type . "' ";  //学科
        $query .= "     AND HALL.EXAMHALLCD  = '" . $model->examhallcd . "' ";
        $query .= "     AND EXAMNO           = '{$examno}' ";

        return $query;
    }
}
