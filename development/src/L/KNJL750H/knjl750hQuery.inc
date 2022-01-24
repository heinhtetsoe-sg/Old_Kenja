<?php
class knjl750hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "", $nameSpare3 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($nameSpare3) {
            $query .= " AND NAMESPARE3 = '".$nameSpare3."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getTestsubclasscd($model, $testsubclasscd = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTSUBCLASSCD || ':' || TESTSUBCLASS_NAME AS LABEL, ";
        $query .= "     TESTSUBCLASSCD AS VALUE, ";
        $query .= "     PERFECT, ";
        $query .= "     REMARK1 AS SUBCLASS_REMARK1, "; // 1:氏名・性別非表示
        $query .= "     REMARK2 AS SUBCLASS_REMARK2 ";  // 科目種別 NULL:筆記 1:面接、2:作文
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT P1 ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        if ($testsubclasscd) {
            $query .= "     AND TESTSUBCLASSCD = '{$testsubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    public function getHallYdat($model, $subclass_remark2)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE || '-' || EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAM_TYPE || '-' || EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$model->testdiv}' ";
        if ($subclass_remark2 == "1") {
            $query .= "     AND EXAM_TYPE = '2' "; //面接
        } elseif ($subclass_remark2 == "2") {
            $query .= "     AND EXAM_TYPE = '3' "; //作文
        } else {
            $query .= "     AND EXAM_TYPE = '1' "; //筆記
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.NAME1 AS SEX_NAME, ";
        $query .= "     SCORE_D.SCORE, ";
        $query .= "     SCORE_D.ATTEND_FLG, ";
        $query .= "     RECEPT.JUDGEDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_HALL_LIST_YDAT HALL ";
        $query .= "          ON HALL.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND HALL.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND HALL.TESTDIV = RECEPT.TESTDIV ";
        $query .= "         AND HALL.EXAM_TYPE || '-' || HALL.EXAMHALLCD = '{$model->examhallcd}' ";
        $query .= "         AND HALL.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_D ";
        $query .= "          ON SCORE_D.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND SCORE_D.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND SCORE_D.TESTDIV = RECEPT.TESTDIV ";
        $query .= "         AND SCORE_D.EXAM_TYPE = RECEPT.EXAM_TYPE ";
        $query .= "         AND SCORE_D.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "         AND SCORE_D.TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "          ON Z002.YEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND Z002.NAMECD1 = 'Z002' ";
        $query .= "         AND Z002.NAMECD2 = BASE.SEX ";
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

    //更新(エンター押下時にひとりのデータを更新）
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $receptno => $value) {
            //存在チェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_SCORE_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $query .= "   AND EXAM_TYPE     = '{$model->exam_type}' ";
            $query .= "   AND RECEPTNO      = '{$receptno}' ";
            $query .= "   AND TESTSUBCLASSCD= '{$model->testsubclasscd}' ";
            $dataCnt = $db->getOne($query);
            //データセット
            $data = array();
            if ($value["JUDGEDIV"] == "5" || $value["SCORE"] == "*") {
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
}
