<?php
class knjl084iQuery extends Query
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
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験班取得
    public function getHallYdat($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE = '{$model->exam_type}' ";
        $query .= "     AND EXAMHALLCD LIKE '1%' "; // 受験班：先頭1桁が'1'のデータ
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目取得
    public function getTestsubclasscd($model, $testsubclasscd = "")
    {
        $nameField = "NAME{$model->applicantdiv}";

        $query  = " SELECT ";
        $query .= "     P1.TESTSUBCLASSCD || ':' || ES1.NAME1 AS LABEL, ";
        $query .= "     P1.TESTSUBCLASSCD AS VALUE, ";
        $query .= "     P1.PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST P1 ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST ES1 ";
        $query .= "          ON ES1.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "         AND ES1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND ES1.SETTING_CD = 'L009' ";
        $query .= "         AND ES1.SEQ = P1.TESTSUBCLASSCD ";
        $query .= "         AND ES1.NAME1 IS NOT NULL ";
        $query .= "     INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "          ON T1.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV = P1.TESTDIV ";
        $query .= "         AND T1.EXAM_TYPE = P1.EXAM_TYPE ";
        $query .= "         AND T1.TESTSUBCLASSCD = P1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     P1.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND P1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND P1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND P1.COURSECD     = '0' ";
        $query .= "     AND P1.MAJORCD      = '000' ";
        $query .= "     AND P1.EXAMCOURSECD = '0000' ";
        $query .= "     AND P1.EXAM_TYPE    = '{$model->exam_type}' ";
        if ($testsubclasscd) {
            $query .= " AND P1.TESTSUBCLASSCD = '{$testsubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目取得
    public function getTestsubclasscdSaki($model)
    {
        $query  = " SELECT ";
        $query .= "     ES1.NAMESPARE2 AS SUBCLASSCD_SAKI ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ES1 ";
        $query .= "     INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "          ON T1.ENTEXAMYEAR = ES1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = ES1.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "         AND T1.EXAM_TYPE = '1' ";
        $query .= "         AND T1.TESTSUBCLASSCD = ES1.SEQ ";
        $query .= " WHERE ";
        $query .= "     ES1.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND ES1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND ES1.SETTING_CD   = 'L009' ";
        $query .= "     AND ES1.SEQ          = '{$model->testsubclasscd}' ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.RECEPTNO, "; // 「RECEPTNO」「EXAMNO」・・・同じ受験番号が入っている
        $query .= "     SCORE_D.SCORE, ";
        $query .= "     SCORE_D.ATTEND_FLG, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT "; // テーブル・・・「4:欠席」「5:未受験」が除かれている
        if ($model->examhallcd != "ALL") {
            $query .= "     INNER JOIN ENTEXAM_HALL_YDAT HALL ";
            $query .= "          ON HALL.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
            $query .= "         AND HALL.APPLICANTDIV = RECEPT.APPLICANTDIV ";
            $query .= "         AND HALL.TESTDIV = RECEPT.TESTDIV ";
            $query .= "         AND HALL.EXAM_TYPE = RECEPT.EXAM_TYPE ";
            $query .= "         AND HALL.EXAMHALLCD = '{$model->examhallcd}' ";
            $query .= "         AND RECEPT.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        }
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "         AND BASE.TESTDIV = RECEPT.TESTDIV ";
        $query .= "         AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "         AND VALUE(BASE.JUDGEMENT, '') != '4' "; // 欠席者は除く
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
            $data["SCORE"][NUMBER]          = !strlen($value["ATTEND_FLG"]) ? $value["SCORE"] : "";
            $data["ATTEND_FLG"][TEXT]       = !strlen($value["ATTEND_FLG"]) ? "0" : $value["ATTEND_FLG"]; // 「0:出席」「1:欠席」
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

    //親科目更新
    public function getUpdateSakiQuery($model, $receptnoArray)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($receptnoArray as $receptno) {
            $query  = " SELECT ";
            $query .= "     SUM(SCORE) AS SCORE ";
            $query .= "   , MAX(VALUE(ATTEND_FLG, '0')) AS ATTEND_FLG ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_SCORE_DAT T1 ";
            $query .= "     INNER JOIN ENTEXAM_SETTING_MST ES1 ";
            $query .= "          ON ES1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "         AND ES1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND ES1.SETTING_CD = 'L009' ";
            $query .= "         AND ES1.SEQ = T1.TESTSUBCLASSCD ";
            $query .= "         AND ES1.NAMESPARE2 = '{$model->testsubclasscdSaki}' ";
            $query .= " WHERE T1.ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND T1.APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND T1.TESTDIV       = '{$model->testdiv}' ";
            $query .= "   AND T1.EXAM_TYPE     = '{$model->exam_type}' ";
            $query .= "   AND T1.RECEPTNO      = '{$receptno}' ";
            $rowSaki = $db->getRow($query, DB_FETCHMODE_ASSOC);

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
            $query .= "   AND TESTSUBCLASSCD= '{$model->testsubclasscdSaki}' ";
            $dataCnt = $db->getOne($query);
            //データセット
            $data = array();
            $data["SCORE"][NUMBER]          = "0" == $rowSaki["ATTEND_FLG"] ? $rowSaki["SCORE"] : "";
            $data["ATTEND_FLG"][TEXT]       = "0" == $rowSaki["ATTEND_FLG"] ? "0" : $rowSaki["ATTEND_FLG"]; // 「0:出席」「1:欠席」
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
                $where .= "   AND EXAM_TYPE     = '{$model->exam_type}' ";
                $where .= "   AND RECEPTNO      = '{$receptno}' ";
                $where .= "   AND TESTSUBCLASSCD= '{$model->testsubclasscdSaki}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscdSaki;
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
