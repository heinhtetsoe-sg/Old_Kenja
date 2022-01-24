<?php

require_once('for_php7.php');

class knjl220cQuery extends Query {

    //入試制度を取得
    function getApplicantdiv($model) {
        $query  = " WITH DATA(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1','1:中学') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DATA ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //プレテスト区分取得
    function getPreTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L104' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目を取得
    function getTestsubclasscd($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1 = 'L109' AND ";
        $query .= "     ABBV3   = '{$model->preTestdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "SELECT T1.pre_receptno ";
        $query .= "      ,T3.score ";
        $query .= "      ,value(T5.perfect, 100) as perfect ";
        $query .= "      ,'中学' AS APPLICANT_NAME ";
        $query .= "      ,L104.NAME1 AS PRE_TESTDIV_NAME ";
        $query .= "      ,L109.NAME1 AS TESTSUBCLASS_NAME ";
        $query .= "  FROM entexam_applicantbase_pre_dat T1 ";
        $query .= "       LEFT JOIN entexam_score_pre_dat T3 ";
        $query .= "              ON T3.entexamyear    = T1.entexamyear ";
        $query .= "             AND T3.applicantdiv   = T1.applicantdiv ";
        $query .= "             AND T3.pre_receptno   = T1.pre_receptno ";
        $query .= "             AND T3.testsubclasscd = '{$model->testsubclasscd}' ";
        $query .= "       INNER JOIN entexam_perfect_pre_mst T5 ";
        $query .= "              ON T5.entexamyear    = T1.entexamyear ";
        $query .= "             AND T5.applicantdiv   = T1.applicantdiv ";
        $query .= "             AND T5.pre_exam_type  = T1.pre_exam_type  ";
        $query .= "             AND T5.testsubclasscd = '{$model->testsubclasscd}' ";
        $query .= "       LEFT JOIN V_NAME_MST L104 ";
        $query .= "              ON L104.YEAR    = T1.entexamyear ";
        $query .= "             AND L104.NAMECD1 = 'L104'  ";
        $query .= "             AND L104.NAMECD2 = '{$model->preTestdiv}' ";
        $query .= "       LEFT JOIN V_NAME_MST L109 ";
        $query .= "              ON L109.YEAR    = T1.entexamyear ";
        $query .= "             AND L109.NAMECD1 = 'L109'  ";
        $query .= "             AND L109.NAMECD2 = '{$model->testsubclasscd}' ";
        $query .= " WHERE T1.entexamyear    = '{$model->ObjYear}' ";
        $query .= "   AND T1.applicantdiv   = '{$model->applicantdiv}' ";
        $query .= " ORDER BY T1.pre_receptno";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //満点チェック
        $result = $db->query(knjl220cQuery::SelectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($model->score[$row["PRE_RECEPTNO"]] > (int)$row["PERFECT"]) {
                $model->setWarning("MSG901","満点：".(int)$row["PERFECT"]."以下で入力してください\\n受験番号：".$row["PRE_RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
        }

        $db->autoCommit(false);

        foreach ($model->score as $receptno => $score) {
            $data = array();
            //セット
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["PRE_RECEPTNO"][TEXT]     = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            $data["ATTEND_FLG"][TEXT]       = strlen($score) ? "1" : "0";
            $data["SCORE"][NUMBER]          = $score;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //条件
            $where  = " WHERE ENTEXAMYEAR    = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $where .= "   AND PRE_RECEPTNO   = '{$receptno}' ";
            $where .= "   AND TESTSUBCLASSCD = '{$model->testsubclasscd}' ";
            //追加or更新
            $query  = "SELECT COUNT(*) FROM ENTEXAM_SCORE_PRE_DAT " .$where;
            if (0 < $db->getOne($query)) {
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_PRE_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_PRE_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>