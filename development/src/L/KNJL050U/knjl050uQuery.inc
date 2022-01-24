<?php

require_once('for_php7.php');
class knjl050uQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    public function getTestSubclasscd($model, $testsubclasscd = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME1 IS NOT NULL AND ";
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                     TESTDIV         = '{$model->testdiv}' ";
        $query .= "                 ) ";
        if ($testsubclasscd) {
            $query .= " AND NAMECD2     = '{$testsubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $force_next = false, $getalldat = false)
    {
        $getRowVal = 100;
        $query  = " WITH PERFECT_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         P1.* ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PERFECT_MST P1 ";
        $query .= "         INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 TESTSUBCLASSCD, ";
        $query .= "                 min(COURSECD||MAJORCD||EXAMCOURSECD) as COURSE ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_PERFECT_MST ";
        $query .= "             WHERE ";
        $query .= "                 ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                 APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                 TESTDIV         = '{$model->testdiv}' ";
        $query .= "             GROUP BY ";
        $query .= "                 TESTSUBCLASSCD ";
        $query .= "         ) P2 ON P2.TESTSUBCLASSCD   = P1.TESTSUBCLASSCD ";
        $query .= "             AND P2.COURSE           = P1.COURSECD||P1.MAJORCD||P1.EXAMCOURSECD ";
        $query .= "     WHERE ";
        $query .= "         P1.ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "         P1.APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "         P1.TESTDIV         = '{$model->testdiv}' ";
        $query .= "     ) ";
        //前検索の場合は、前の件数で先頭となる番号を求める(件数に満たない場合や、検索0件の場合、先頭から表示する処理が必要となるため)
        if (!$force_next && $model->cmd == "back") {
            $query .= " , SEARCH_FST_RECORD AS ( ";
            $query .= " SELECT RECEPTNO FROM ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "    ENTEXAMYEAR  = '{$model->ObjYear}' ";
            $query .= "    AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "    AND TESTDIV      = '{$model->testdiv}' ";
            $query .= "    AND '{$model->s_receptno}' > RECEPTNO ";
            $query .= " ORDER BY ";
            $query .= "    RECEPTNO DESC ";
            $query .= "    FETCH FIRST {$getRowVal} ROWS ONLY ";
            $query .= " ), GET_MINRECEPT AS ( ";
            $query .= " SELECT ";
            $query .= "   CASE WHEN MIN(SC1.RECEPTNO) IS NULL THEN '{$model->s_receptno}' ELSE MIN(SC1.RECEPTNO) END AS MIN_RCPT ";
            $query .= " FROM ";
            $query .= "   SEARCH_FST_RECORD SC1 ";
            $query .= "     ) ";
        }
        $query .= " , MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.ENTEXAMYEAR, ";
        $query .= "         R1.APPLICANTDIV, ";
        $query .= "         R1.TESTDIV, ";
        $query .= "         '{$model->testsubclasscd}' as TESTSUBCLASSCD, ";
        $query .= "         R1.RECEPTNO, ";
        $query .= "         R1.EXAMNO, ";
        $query .= "         B1.NAME, ";
        $query .= "         B1.NAME_KANA, ";
        $query .= "         S1.ATTEND_FLG, ";
        $query .= "         S1.SCORE, ";
        //満点(初期値)　国語100点、算数100点、社会60点、理科60点
        $perfect = ($model->testsubclasscd == "3" || $model->testsubclasscd == "4") ? 60 : 100;
        $query .= "         VALUE(P1.PERFECT, {$perfect}) AS PERFECT, ";
        $query .= "         '".$model->lastColumn."' as ".$model->lastColumn." ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "              ON B1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND B1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND B1.EXAMNO           = R1.EXAMNO ";
        $query .= "         INNER JOIN PERFECT_T P1 ";
        $query .= "              ON P1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND P1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND P1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND P1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND S1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND S1.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "             AND S1.RECEPTNO         = R1.RECEPTNO ";
        $query .= "             AND S1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        if (!$force_next && $model->cmd == "back") {
            $query .= " , GET_MINRECEPT GM1 ";
        }
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$model->testdiv}' ";
        if ($force_next || $model->cmd == "next") {
            if ($model->chg_srchnoflg) {
                $query .= "         AND R1.RECEPTNO >= '{$model->s_receptno}' ";
            } else {
                $query .= "         AND R1.RECEPTNO > '{$model->e_receptno}' ";
            }
        }
        if (!$force_next && $model->cmd == "back") {
            //前のデータの場合は、"前の先頭データ"を利用するので、必ず"それ"以上を検索する。
            $query .= "         AND R1.RECEPTNO >= GM1.MIN_RCPT ";
        }
        $query .= "         AND VALUE(R1.JUDGEDIV,'0') != '5' "; //受験不可
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";
        if ($getalldat === false) {
            $query .= "     FETCH FIRST {$getRowVal} ROWS ONLY ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";
        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAM_TYPE      = '".$model->exam_type."' ";
        $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "   AND RECEPTNO IN ('".implode("','", array_keys($model->score))."')";
        $db->query($query);

        //追加
        foreach ($model->score as $receptno => $score) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            if ($score === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //件数取得 -- ENTEXAM_SCORE_DAT
    public function cntEntexamScoreDat($year, $applicantDiv, $testDiv, $examType, $receptNo, $testsubclassCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$year}' ";
        $query .= "     AND APPLICANTDIV    = '{$applicantDiv}' ";
        $query .= "     AND TESTDIV         = '{$testDiv}' ";
        $query .= "     AND EXAM_TYPE       = '{$examType}' ";
        $query .= "     AND RECEPTNO        = '{$receptNo}' ";
        $query .= "     AND TESTSUBCLASSCD  = '{$testsubclassCd}' ";

        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $year           = $data_arr[$i]["ENTEXAMYEAR"];
            $applicantDiv   = $data_arr[$i]["APPLICANTDIV"];
            $testDiv        = $data_arr[$i]["TESTDIV"];
            $receptNo       = $data_arr[$i]["RECEPTNO"];
            $testsubclassCd = $data_arr[$i]["TESTSUBCLASSCD"];
            $score          = $data_arr[$i]["SCORE"];

            // 件数取得
            $score_cnt = $db->getOne(knjl050uQuery::cntEntexamScoreDat($year, $applicantDiv, $testDiv, $model->exam_type, $receptNo, $testsubclassCd));

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $year;
            $data["APPLICANTDIV"][TEXT]     = $applicantDiv;
            $data["TESTDIV"][TEXT]          = $testDiv;
            $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
            $data["RECEPTNO"][TEXT]         = $receptNo;
            $data["TESTSUBCLASSCD"][TEXT]   = $testsubclassCd;

            if ($score === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR    = '{$year}' ";
            $where .= "   AND APPLICANTDIV   = '{$applicantDiv}' ";
            $where .= "   AND TESTDIV        = '{$testDiv}' ";
            $where .= "   AND EXAM_TYPE      = '{$model->exam_type}' ";
            $where .= "   AND RECEPTNO       = '{$receptNo}' ";
            $where .= "   AND TESTSUBCLASSCD = '{$testsubclassCd}' ";

            if (0 == $score_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
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

    //受験番号
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
}
