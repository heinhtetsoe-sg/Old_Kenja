<?php
class knjl630hQuery extends Query
{
    //入試区分取得
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->objYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号重複チェック
    public function isExistReceptno($model, $examno, $receptno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE  ";
        $query .= "     ENTEXAMYEAR       = '{$model->objYear}' ";
        $query .= "     AND RECEPTNO      = '{$receptno}' ";
        $query .= "     AND EXAMNO        <> '{$examno}' ";

        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0) ? false : true);
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     RECEPT.RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "          ON RECEPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND RECEPT.TESTDIV      = BASE.TESTDIV ";
        $query .= "         AND RECEPT.EXAMNO       = BASE.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR      = '{$model->objYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        if ($model->sort == "1") {
            $query .= "     BASE.NAME_KANA ";
        } else {
            $query .= "     BASE.EXAMNO ";
        }

        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->receptnoArray as $examno => $receptno) {
            //存在チェック
            $query  = " SELECT ";
            $query .= "     RECEPTNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR       = '{$model->objYear}' ";
            $query .= "     AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
            $query .= "     AND EXAM_TYPE    = '1' ";
            $query .= "     AND EXAMNO        = '{$examno}' ";
            $targetReceptno = $db->getOne($query);

            //ENTEXAM_RECEPT_DETAIL_DAT削除
            if ($targetReceptno != "") {
                //DELETE
                $query  = " DELETE FROM ";
                $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR      = '{$model->objYear}' ";
                $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
                $query .= "     AND EXAM_TYPE    = '1' ";
                $query .= "     AND RECEPTNO     = '{$targetReceptno}' ";
                $db->query($query);
            }

            //DELETE
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR      = '{$model->objYear}' ";
            $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
            $query .= "     AND EXAM_TYPE    = '1' ";
            $query .= "     AND EXAMNO       = '{$examno}' ";

            $db->query($query);

            //データセット
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->objYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = '1';
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
