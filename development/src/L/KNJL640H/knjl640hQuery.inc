<?php
class knjl640hQuery extends Query
{
    //入試日程取得
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

    //一覧取得
    public function getSelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO AS VALUE, ";
        $query .= "     RECEPT.RECEPTNO || '：' ||  BASE.NAME AS LABEL, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     RECEPT.TESTDIV, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "            AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "            AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND RECEPT.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";
        return $query;
    }

    //対象EXAMNO取得
    public function getTargetExamno($model, $flg, $receptNo)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.EXAMNO ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
        $query .= "            AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "            AND BASE.EXAMNO       = RECEPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RECEPT.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND RECEPT.EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND RECEPTNO            = '{$receptNo}' ";
        if ($flg == "right") {
            $query .= "     AND (BASE.JUDGEMENT = '3' OR BASE.JUDGEMENT IS NULL) ";
        }
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";
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
            $judge = "3"; //欠席
            $selectSchArr = explode(",", $model->selectLeft);
        } elseif ($flg == "right") {
            //右側リスト
            $judge = ""; //空白
            $selectSchArr = explode(",", $model->selectRight);
        }

        foreach ($selectSchArr as $receptNo) {
            //ENTEXAM_RECEPT_DAT.EXAMNOを取得
            $row = $db->getRow(knjl640hQuery::getTargetExamno($model, $flg, $receptNo), DB_FETCHMODE_ASSOC);

            if (is_array($row)) {
                //ENTEXAM_APPLICANTBASE_DAT
                $data = array();
                $data["JUDGEMENT"][TEXT]    = $judge;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO        = '{$row["EXAMNO"]}' ";

                //更新（ENTEXAM_APPLICANTBASE_DAT）
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
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
        $flg = "left";
        $judge = "3"; //欠席

        for ($i = 0; $i < count($data_arr); $i++) {
            //ENTEXAM_RECEPT_DAT.EXAMNOを取得
            $row = $db->getRow(knjl640hQuery::getTargetExamno($model, $flg, $data_arr[$i]["RECEPTNO"]), DB_FETCHMODE_ASSOC);

            if (is_array($row)) {
                //ENTEXAM_APPLICANTBASE_DAT
                $data = array();
                $data["JUDGEMENT"][TEXT]    = $judge;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO        = '{$row["EXAMNO"]}' ";

                //更新（ENTEXAM_APPLICANTBASE_DAT）
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }

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
