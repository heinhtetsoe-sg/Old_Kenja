<?php
class knjl052oquery extends Query
{
    //名称マスタ取得
    public function getNameMst($year, $nameCd1, $nameCd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$nameCd1}' ";
        if ($nameCd2 != "") {
            $query .= "     AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getReceptDat($model, $data, $receptno = "", $examno = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "          ON T1.ENTEXAMYEAR    = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO         = T2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR     = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV         = '{$data["TESTDIV"]}' ";
        $query .= "     AND T1.EXAM_TYPE       = '{$model->exam_type}' ";
        if ($receptno && !$examno) {
            $query .= "     AND T1.RECEPTNO        = '{$receptno}' ";
        } elseif ($examno && !$receptno) {
            $query .= "     AND T1.EXAMNO          = '{$examno}' ";
        } elseif ($receptno && $examno) {
            $query .= "     AND T1.RECEPTNO        = '{$receptno}' ";
            $query .= "     AND T1.EXAMNO          = '{$examno}' ";
        } else {
            $query .= "     AND T1.EXAMNO          = '{$data["EXAMNO"]}' ";
        }

        return $query;
    }

    //データ取込
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $query  = "DELETE FROM ENTEXAM_SCORE_DAT";
            $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."'";
            $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."'";
            $query .= "   AND TESTDIV        = '".$data_arr[$i]["TESTDIV"]."'";
            $query .= "   AND EXAM_TYPE      = '".$model->exam_type."'";
            $query .= "   AND TESTSUBCLASSCD = '".$data_arr[$i]["TESTSUBCLASSCD"]."'";
            $query .= "   AND RECEPTNO       = '".$data_arr[$i]["RECEPTNO"]."'";

            $db->query($query);

            $query  = "INSERT INTO ENTEXAM_SCORE_DAT";
            $query .= " (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO, TESTSUBCLASSCD, ATTEND_FLG, SCORE, REGISTERCD, UPDATED)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv."'";
            $query .= "        ,'".$data_arr[$i]["TESTDIV"]."'";
            $query .= "        ,'".$model->exam_type."'";
            $query .= "        ,'".$data_arr[$i]["RECEPTNO"]."'";
            $query .= "        ,'".$data_arr[$i]["TESTSUBCLASSCD"]."'";
            $query .= "        ,'".(!strlen($data_arr[$i]["SCORE"]) ? "0" : "1" )."'";
            $query .= "        ,".(!strlen($data_arr[$i]["SCORE"]) ? "NULL" : $data_arr[$i]["SCORE"]);
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //データ削除
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $query  = "DELETE FROM ENTEXAM_SCORE_DAT";
            $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."'";
            $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."'";
            $query .= "   AND TESTDIV        = '".$data_arr[$i]["TESTDIV"]."'";
            $query .= "   AND EXAM_TYPE      = '".$model->exam_type."'";
            $query .= "   AND TESTSUBCLASSCD = '".$data_arr[$i]["TESTSUBCLASSCD"]."'";
            $query .= "   AND RECEPTNO       = '".$data_arr[$i]["RECEPTNO"]."'";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  エラーチェック用  **/
    /**********************/

    //入試区分マスタ
    public function checkTestdiv($model, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND TESTDIV = '{$testdiv}' ";

        return $query;
    }

    //名称マスタ登録コードチェック
    public function nameCntSql($model, $namecd, $setcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";

        return $query;
    }

    //満点チェック
    public function checkPerfectMst($model, $testdiv, $testsubclasscd)
    {
        $query  = " SELECT ";
        $query .= "     PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR        = '{$model->ObjYear}' ";
        $query .= "     AND TESTDIV        = '{$testdiv}' ";
        $query .= "     AND TESTSUBCLASSCD = '{$testsubclasscd}' ";

        return $query;
    }
}
