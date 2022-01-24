<?php
class knjl051oQuery extends Query
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

    //受験科目
    public function getTestSubclasscd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        if ($model->exam_type == "1") {
            $query .= "     AND NAMESPARE1 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタ
    public function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV VALUE, ";
        $query .= "     TESTDIV || ':' || NAME LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= "     AND SHOWDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //試験会場設定データ
    public function getHallDat($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAMHALLCD VALUE, ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV       = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";
        return $query;
    }

    //一覧表示
    public function selectQuery($model, $setReceptno = "")
    {
        $query  = "SELECT T1.receptno ";
        $query .= "      ,T1.examno ";
        $query .= "      ,substr(T2.name,1,30)         AS name ";
        $query .= "      ,substr(T2.name_kana,1,60)    AS name_kana ";
        $query .= "      ,T2.sex                       AS sexcd ";
        $query .= "      ,MEISYOU_GET(T2.sex,'Z002',2) AS sex ";
        $query .= "      ,T3.score ";
        $query .= "      ,T2.desirediv ";
        $query .= "      ,T4.examcourse_name ";
        $query .= "      ,T5.perfect ";
        $query .= "      ,T1.ENTEXAMYEAR ";
        $query .= "      ,T1.APPLICANTDIV ";
        $query .= "      ,L003.NAME1 AS APPLICANTDIV_NAME ";
        $query .= "      ,T1.TESTDIV ";
        $query .= "      ,TEST_M.NAME AS TESTDIV_NAME ";
        $query .= "      ,T1.EXAM_TYPE ";
        $query .= "      ,L005.NAME1 AS EXAM_TYPE_NAME ";
        $query .= "      ,T3.TESTSUBCLASSCD ";
        $query .= "      ,L009.NAME1 AS TESTSUBCLASS_NAME ";
        $query .= "  FROM entexam_recept_dat T1 INNER JOIN entexam_applicantbase_dat T2 ";
        $query .= "                                     ON T1.entexamyear    = T2.entexamyear ";
        $query .= "                                    AND T1.examno         = T2.examno ";
        $query .= "                             INNER JOIN ENTEXAM_HALL_DAT HALL ";
        $query .= "                                     ON HALL.TESTDIV = T1.TESTDIV ";
        $query .= "                                    AND HALL.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "                                    AND HALL.EXAMHALLCD = '".$model->examhallcd."' ";
        $query .= "                                    AND T1.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= "                             LEFT OUTER JOIN entexam_score_dat T3 ";
        $query .= "                                     ON T1.entexamyear    = T3.entexamyear ";
        $query .= "                                    AND T1.applicantdiv   = T3.applicantdiv ";
        $query .= "                                    AND T1.testdiv        = T3.testdiv  ";
        $query .= "                                    AND T1.exam_type      = T3.exam_type ";
        $query .= "                                    AND T1.receptno       = T3.receptno ";
        $query .= "                                    AND T3.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "                             LEFT OUTER JOIN (SELECT T1.desirediv, T2.examcourse_name  ";
        $query .= "                                           FROM entexam_wishdiv_mst T1  ";
        $query .= "                                               ,entexam_course_mst T2 ";
        $query .= "                                          WHERE T1.entexamyear  = T2.entexamyear ";
        $query .= "                                            AND T1.examcoursecd = T2.examcoursecd  ";
        $query .= "                                            AND T1.coursecd     = T2.coursecd ";
        $query .= "                                            AND T1.majorcd      = T2.majorcd  ";
        $query .= "                                            AND T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "                                            AND T1.wishno       = '1') T4 ";
        $query .= "                                     ON T4.desirediv      = T2.desirediv ";
        $query .= "                             LEFT OUTER JOIN (SELECT T1.desirediv, T3.testdiv, T3.testsubclasscd, T3.perfect";
        $query .= "                                           FROM entexam_wishdiv_mst T1 ";
        $query .= "                                               ,entexam_perfect_mst T3 ";
        $query .= "                                          WHERE T1.entexamyear  = T3.entexamyear ";
        $query .= "                                            AND T1.coursecd     = T3.coursecd ";
        $query .= "                                            AND T1.majorcd      = T3.majorcd ";
        $query .= "                                            AND T1.examcoursecd = T3.examcoursecd ";
        $query .= "                                            AND T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "                                            AND T1.wishno       = '1') T5 ";
        $query .= "                                     ON T5.desirediv      = T2.desirediv ";
        $query .= "                                    AND T5.testdiv        = T1.testdiv ";
        $query .= "                                    AND T5.testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "                             LEFT JOIN V_NAME_MST L003 ";
        $query .= "                                     ON L003.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L003.NAMECD1 = 'L003' ";
        $query .= "                                    AND L003.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "                             LEFT JOIN ENTEXAM_TESTDIV_MST TEST_M ";
        $query .= "                                     ON TEST_M.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND TEST_M.TESTDIV     = T1.TESTDIV ";
        $query .= "                                    AND TEST_M.SHOWDIV     = '1' ";
        $query .= "                             LEFT JOIN V_NAME_MST L005 ";
        $query .= "                                     ON L005.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L005.NAMECD1 = 'L005' ";
        $query .= "                                    AND L005.NAMECD2 = T1.EXAM_TYPE ";
        $query .= "                             LEFT JOIN V_NAME_MST L009 ";
        $query .= "                                     ON L009.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L009.NAMECD1 = 'L009' ";
        $query .= "                                    AND L009.NAMECD2 = T3.TESTSUBCLASSCD ";
        $query .= " WHERE T1.entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND T1.applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.testdiv        = '".$model->testdiv."' ";
        $query .= "   AND T1.exam_type      = '".$model->exam_type."' ";
        $query .= "   AND EXISTS (";    //志願者数確定処理が実行していないと表示はしない。
        $query .= "                SELECT 'X' FROM entexam_desire_dat w1";
        $query .= "                 WHERE ";
        $query .= "                       w1.entexamyear    =T1.entexamyear AND";
        $query .= "                       w1.applicantdiv   =T1.applicantdiv AND";
        $query .= "                       w1.testdiv        =T1.testdiv AND";
        $query .= "                       w1.examno         =T1.examno";
        $query .= "              )";
        if ($setReceptno != "") {
            $query .= "  AND T1.RECEPTNO = '{$setReceptno}' ";
        }
        $query .= " ORDER BY T1.receptno";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //満点チェック
        $result = $db->query(knjl051oQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->score[$row["RECEPTNO"]] > (int)$row["PERFECT"]) {
                $model->setWarning("MSG901", "満点：".(int)$row["PERFECT"]."以下で入力してください\\n座席番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
        }

        $db->autoCommit(false);

        $query  = "DELETE FROM entexam_score_dat";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv   = '".$model->applicantdiv."'";
        $query .= "   AND testdiv        = '".$model->testdiv."'";
        $query .= "   AND exam_type      = '".$model->exam_type."'";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."'";
        $query .= "   AND receptno IN ('" . implode("', '", array_keys($model->score)) . "')";
        $db->query($query);

        foreach ($model->score as $receptno => $score) {
            $query  = "INSERT INTO entexam_score_dat";
            $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, testsubclasscd, attend_flg, score, registercd, updated)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'".$model->exam_type."'";
            $query .= "        ,'".$receptno."'";
            $query .= "        ,'".$model->testsubclasscd."'";
            $query .= "        ,'".(!strlen($score) ? "0" : "1" )."'";
            $query .= "        ,".(!strlen($score) ? "NULL" : $score);
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
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
        for ($i = 0; $i < count($data_arr); $i++) {
            $query  = "DELETE FROM ENTEXAM_SCORE_DAT";
            $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."'";
            $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."'";
            $query .= "   AND TESTDIV        = '".$model->testdiv."'";
            $query .= "   AND EXAM_TYPE      = '".$model->exam_type."'";
            $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."'";
            $query .= "   AND RECEPTNO       = '".$data_arr[$i]["RECEPTNO"]."'";
            $db->query($query);

            $query  = "INSERT INTO ENTEXAM_SCORE_DAT";
            $query .= " (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO, TESTSUBCLASSCD, ATTEND_FLG, SCORE, REGISTERCD, UPDATED)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'".$model->exam_type."'";
            $query .= "        ,'".$data_arr[$i]["RECEPTNO"]."'";
            $query .= "        ,'".$model->testsubclasscd."'";
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

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($model, $namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //座席番号
    public function checkReceptNo($model, $receptNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND RECEPTNO     = '{$receptNo}' ";

        return $query;
    }

    //受験番号
    public function checkExamNo($model, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       = '{$examNo}' ";

        return $query;
    }
}
?>
