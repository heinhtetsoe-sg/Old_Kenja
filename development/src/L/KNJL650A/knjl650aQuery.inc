<?php

require_once('for_php7.php');
class knjl650aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験科目
    public function getTestSubclasscd($model, $testsubclasscd = "")
    {
        $query  = " SELECT ";
        if ($model->applicantdiv == "1") {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME2 AS LABEL ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        if ($model->applicantdiv == "1") {
            $query .= "     NAME2 IS NOT NULL ";
        } else {
            $query .= "     NAME1 IS NOT NULL ";
        }
        //満点マスタに存在する科目
        $query .= "     AND NAMECD2 IN (SELECT ";
        $query .= "                         TESTSUBCLASSCD ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_PERFECT_MST ";
        $query .= "                     WHERE ";
        $query .= "                         ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                         APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                         TESTDIV         = '{$model->testdiv}' ";
        $query .= "                    ) ";
        if ($model->applicantdiv == "1") {
            if ($model->testdiv == "1" || $model->testdiv == "2") {
                $query .= "     AND VALUE(NAMESPARE3,0) <> '1' ";
            }
        }
        if ($testsubclasscd) {
            $query .= " AND NAMECD2     = '{$testsubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場
    public function getHallcd($model, $hallcd = "")
    {
        $query  = " SELECT ";
        if ($hallcd) {
            $query .= "     EXAMHALL_NAME AS VALUE ";
        } else {
            $query .= "     EXAMHALLCD AS VALUE, ";
            $query .= "     EXAMHALLCD AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->examtype}' ";
        if ($hallcd) {
            $query .= " AND EXAMHALLCD = '{$hallcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $stNo)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.ENTEXAMYEAR, ";
        $query .= "         R1.APPLICANTDIV, ";
        $query .= "         N1.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "         R1.TESTDIV, ";
        $query .= "         N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "         '{$model->testsubclasscd}' AS TESTSUBCLASSCD, ";
        if ($model->applicantdiv == "1") {
            $query .= "         N3.NAME2 AS TESTSUBCLASSCD_NAME, ";
        } else {
            $query .= "         N3.NAME1 AS TESTSUBCLASSCD_NAME, ";
        }
        $query .= "         DT010.REMARK1 || DT010.REMARK2 AS SEATNO, ";
        $query .= "         R1.RECEPTNO, ";
        $query .= "         R1.EXAMNO, ";
        $query .= "         B1.NAME, ";
        $query .= "         B1.NAME_KANA, ";
        $query .= "         S1.ATTEND_FLG, ";
        $query .= "         S1.SCORE, ";
        //満点(初期値)　一律100点
        $perfect = 100;
        $query .= "         VALUE(P1.PERFECT, {$perfect}) AS PERFECT, ";
        $query .= "         VALUE(P1.MAGNIFYING, 1) AS MAGNIFYING, ";
        $query .= "         '".$model->lastColumn."' as ".$model->lastColumn." ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT007 ";
        $query .= "              ON DT007.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT007.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT007.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT007.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT007.SEQ          = '007' ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT010 ";
        $query .= "              ON DT010.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT010.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT010.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT010.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT010.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT010.SEQ          = '010' ";
        if ($model->hallcd !== "ALL") {
            $query .= "             AND DT010.REMARK1      = '{$model->hallcd}'  ";
        }
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "              ON B1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND B1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND B1.EXAMNO           = R1.EXAMNO ";
        $query .= "         INNER JOIN ENTEXAM_PERFECT_MST P1 ";
        $query .= "              ON P1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND P1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND P1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND P1.EXAMCOURSECD     = DT007.REMARK1 ";
        $query .= "             AND P1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND S1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND S1.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "             AND S1.RECEPTNO         = R1.RECEPTNO ";
        $query .= "             AND S1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR    = R1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1 = 'L003' ";
        $query .= "             AND N1.NAMECD2 = R1.APPLICANTDIV ";
        $namecd1 = $model->applicantdiv == "1" ? "L024" : "L004";
        $query .= "         LEFT JOIN V_NAME_MST N2 ";
        $query .= "              ON N2.YEAR    = R1.ENTEXAMYEAR ";
        $query .= "             AND N2.NAMECD1 = '{$namecd1}' ";
        $query .= "             AND N2.NAMECD2 = R1.TESTDIV ";
        $query .= "         LEFT JOIN V_NAME_MST N3 ";
        $query .= "              ON N3.YEAR    = R1.ENTEXAMYEAR ";
        $query .= "             AND N3.NAMECD1 = 'L009' ";
        $query .= "             AND N3.NAMECD2 = S1.TESTSUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND VALUE(R1.JUDGEDIV,'0') != '5' "; //受験不可
        $query .= " ORDER BY ";
        if ($model->sort == "1") {
            $query .= "     SEATNO, ";
        }
        $query .= "     EXAMNO ";
        $query .= " ), MAIN_T2 AS ( ";
        $query .= " SELECT ";
        $query .= "     row_number() over() AS NO,* ";
        $query .= " FROM ";
        $query .= "     MAIN_T";
        $query .= " ) SELECT";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T2";
        $query .= " WHERE ";
        $query .= "             NO BETWEEN {$stNo} AND {$stNo} + 49 ";
        return $query;
    }

    //面接の得点を取得
    public function getInterviewScore($model, $examno, $receptno)
    {
        $query .= " SELECT ";
        $query .= "   S1.SCORE ";
        $query .= " FROM  ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "   INNER JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "           ON R1.ENTEXAMYEAR      =  B1.ENTEXAMYEAR ";
        $query .= "          AND R1.APPLICANTDIV     =  B1.APPLICANTDIV ";
        $query .= "          AND R1.TESTDIV          <> '{$model->testdiv}' ";
        $query .= "          AND R1.EXAMNO           =  B1.EXAMNO ";
        $query .= "          AND R1.RECEPTNO         <> '{$receptno}' ";
        $query .= "   INNER JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "          ON S1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "         AND S1.TESTDIV          = R1.TESTDIV ";
        $query .= "         AND S1.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "         AND S1.RECEPTNO         = R1.RECEPTNO ";
        $query .= "         AND S1.TESTSUBCLASSCD   = 'A' ";
        $query .= "         AND S1.SCORE IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "       B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "   AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND B1.EXAMNO       = '{$examno}' ";
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
        $query .= "   AND EXAM_TYPE      = '".$model->examtype."' ";
        $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "   AND RECEPTNO IN ('".implode("','", array_keys($model->arr_score))."')";
        $db->query($query);

        //追加
        foreach ($model->arr_score as $receptno => $score) {
            $magnifying = $model->arr_magnifying[$receptno];
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->examtype;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            if ($score === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
                $data["SCORE2"][NUMBER]     = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
                $data["SCORE2"][NUMBER]     = !strlen($score) ? "" : (($magnifying == "0") ? $score : round($score * $magnifying));
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if (strlen($score)) {
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //件数取得 -- ENTEXAM_SCORE_DAT
    public function cntEntexamScoreDat($year, $applicantdiv, $testdiv, $examtype, $receptno, $testsubclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$year}' ";
        $query .= "     AND APPLICANTDIV    = '{$applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$testdiv}' ";
        $query .= "     AND EXAM_TYPE       = '{$examtype}' ";
        $query .= "     AND RECEPTNO        = '{$receptno}' ";
        $query .= "     AND TESTSUBCLASSCD  = '{$testsubclasscd}' ";

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
            $applicantdiv   = $data_arr[$i]["APPLICANTDIV"];
            $testdiv        = $data_arr[$i]["TESTDIV"];
            $examtype       = "1";
            $receptno       = $db->getOne(knjl650aQuery::getReceptNo($data_arr[$i]));
            $testsubclasscd = $data_arr[$i]["TESTSUBCLASSCD"];
            $score          = $data_arr[$i]["SCORE"];
            $magnifying     = $db->getOne(knjl650aQuery::getPerfectMst($data_arr[$i], "MAGNIFYING"));

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $year;
            $data["APPLICANTDIV"][TEXT]     = $applicantdiv;
            $data["TESTDIV"][TEXT]          = $testdiv;
            $data["EXAM_TYPE"][TEXT]        = $examtype;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $testsubclasscd;
            if ($score === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
                $data["SCORE2"][NUMBER]     = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
                $data["SCORE2"][NUMBER]     = !strlen($score) ? "" : (($magnifying == "0") ? $score : round($score * $magnifying));
            }


            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR    = '{$year}' ";
            $where .= "   AND APPLICANTDIV   = '{$applicantdiv}' ";
            $where .= "   AND TESTDIV        = '{$testdiv}' ";
            $where .= "   AND EXAM_TYPE      = '{$examtype}' ";
            $where .= "   AND RECEPTNO       = '{$receptno}' ";
            $where .= "   AND TESTSUBCLASSCD = '{$testsubclasscd}' ";

            //削除
            $query  = " DELETE FROM ENTEXAM_SCORE_DAT ".$where;
            $db->query($query);

            if (strlen($score)) {
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
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

    //受験番号の取得
    public function getReceptNo($data, $cnt = "")
    {
        $year           = $data["ENTEXAMYEAR"];
        $applicantdiv   = $data["APPLICANTDIV"];
        $testdiv        = $data["TESTDIV"];
        $examtype       = "1";
        $seatno         = $data["SEATNO"];
        $examno         = $data["EXAMNO"];
        $testsubclasscd = $data["TESTSUBCLASSCD"];

        $query .= "     SELECT ";
        if ($cnt) {
            $query .= "         COUNT(*) ";
        } else {
            $query .= "         R1.RECEPTNO ";
        }
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT007 ";
        $query .= "              ON DT007.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT007.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT007.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT007.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT007.SEQ          = '007' ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT010 ";
        $query .= "              ON DT010.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT010.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT010.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT010.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT010.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT010.SEQ          = '010' ";
        $query .= "             AND DT010.REMARK1 || DT010.REMARK2 = '{$seatno}'  ";
        $query .= "         INNER JOIN ENTEXAM_PERFECT_MST P1 ";
        $query .= "              ON P1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND P1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND P1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND P1.EXAMCOURSECD     = DT007.REMARK1 ";
        $query .= "             AND P1.TESTSUBCLASSCD   = '{$testsubclasscd}' ";
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$testdiv}' ";
        $query .= "         AND R1.EXAM_TYPE    = '{$examtype}' ";
        $query .= "         AND R1.EXAMNO       = '{$examno}' ";

        return $query;
    }

    //ENTEXAM_PERFECT_MSTを取得
    public function getPerfectMst($data, $field)
    {
        $year           = $data["ENTEXAMYEAR"];
        $applicantdiv   = $data["APPLICANTDIV"];
        $testdiv        = $data["TESTDIV"];
        $examtype       = "1";
        $seatno         = $data["SEATNO"];
        $examno         = $data["EXAMNO"];
        $testsubclasscd = $data["TESTSUBCLASSCD"];

        $query .= "     SELECT ";
        if ($field == "MAGNIFYING") {
            $query .= "         VALUE(P1.MAGNIFYING, 1) AS MAGNIFYING ";
        } else {
            $query .= "         P1.{$field} ";
        }

        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT007 ";
        $query .= "              ON DT007.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT007.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT007.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT007.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT007.SEQ          = '007' ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT010 ";
        $query .= "              ON DT010.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT010.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT010.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT010.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT010.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT010.SEQ          = '010' ";
        $query .= "             AND DT010.REMARK1 || DT010.REMARK2 = '{$seatno}'  ";
        $query .= "         INNER JOIN ENTEXAM_PERFECT_MST P1 ";
        $query .= "              ON P1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND P1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND P1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND P1.EXAMCOURSECD     = DT007.REMARK1 ";
        $query .= "             AND P1.TESTSUBCLASSCD   = '{$testsubclasscd}' ";
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$testdiv}' ";
        $query .= "         AND R1.EXAM_TYPE    = '{$examtype}' ";
        $query .= "         AND R1.EXAMNO       = '{$examno}' ";
        return $query;
    }

    //件数カウント
    public function selectQueryCsv($model)
    {
        $query .= "     SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT007 ";
        $query .= "              ON DT007.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT007.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT007.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT007.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT007.SEQ          = '007' ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT010 ";
        $query .= "              ON DT010.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT010.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT010.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT010.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT010.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT010.SEQ          = '010' ";
        if ($model->hallcd !== "ALL") {
            $query .= "             AND DT010.REMARK1      = '{$model->hallcd}'  ";
        }
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "              ON B1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND B1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND B1.EXAMNO           = R1.EXAMNO ";
        $query .= "         INNER JOIN ENTEXAM_PERFECT_MST P1 ";
        $query .= "              ON P1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND P1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND P1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND P1.EXAMCOURSECD     = DT007.REMARK1 ";
        $query .= "             AND P1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND S1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND S1.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "             AND S1.RECEPTNO         = R1.RECEPTNO ";
        $query .= "             AND S1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND VALUE(R1.JUDGEDIV,'0') != '5' "; //受験不可

        return $query;
    }

    //CSV出力
    public function selectQueryCsv2($model, $stNo)
    {
        $query  = "";
        $query .= "     SELECT ";
        $query .= "         R1.ENTEXAMYEAR, ";
        $query .= "         R1.APPLICANTDIV, ";
        $query .= "         N1.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "         R1.TESTDIV, ";
        $query .= "         N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "         P1.TESTSUBCLASSCD AS TESTSUBCLASSCD, ";
        if ($model->applicantdiv == "1") {
            $query .= "         N3.NAME2 AS TESTSUBCLASSCD_NAME, ";
        } else {
            $query .= "         N3.NAME1 AS TESTSUBCLASSCD_NAME, ";
        }
        $query .= "         DT010.REMARK1 || DT010.REMARK2 AS SEATNO, ";
        $query .= "         R1.RECEPTNO, ";
        $query .= "         R1.EXAMNO, ";
        $query .= "         B1.NAME, ";
        $query .= "         B1.NAME_KANA, ";
        $query .= "         S1.ATTEND_FLG, ";
        $query .= "         S1.SCORE, ";
        //満点(初期値)　一律100点
        $perfect = 100;
        $query .= "         VALUE(P1.PERFECT, {$perfect}) AS PERFECT, ";
        $query .= "         VALUE(P1.MAGNIFYING, 1) AS MAGNIFYING, ";
        $query .= "         '".$model->lastColumn."' as ".$model->lastColumn." ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT007 ";
        $query .= "              ON DT007.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT007.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT007.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT007.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT007.SEQ          = '007' ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT DT010 ";
        $query .= "              ON DT010.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "             AND DT010.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "             AND DT010.TESTDIV      = R1.TESTDIV ";
        $query .= "             AND DT010.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "             AND DT010.RECEPTNO     = R1.RECEPTNO ";
        $query .= "             AND DT010.SEQ          = '010' ";
        if ($model->hallcd !== "ALL" && $model->hallcd != "") {
            $query .= "             AND DT010.REMARK1      = '{$model->hallcd}'  ";
        }
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "              ON B1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND B1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND B1.EXAMNO           = R1.EXAMNO ";
        $query .= "         INNER JOIN ENTEXAM_PERFECT_MST P1 ";
        $query .= "              ON P1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND P1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND P1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND P1.EXAMCOURSECD     = DT007.REMARK1 ";
        if ($model->testsubclasscd != "") {
            $query .= "             AND P1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        }
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "             AND S1.TESTDIV          = R1.TESTDIV ";
        $query .= "             AND S1.EXAM_TYPE        = R1.EXAM_TYPE ";
        $query .= "             AND S1.RECEPTNO         = R1.RECEPTNO ";
        $query .= "             AND S1.TESTSUBCLASSCD   = P1.TESTSUBCLASSCD ";
        $query .= "         LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR    = R1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1 = 'L003' ";
        $query .= "             AND N1.NAMECD2 = R1.APPLICANTDIV ";
        $namecd1 = $model->applicantdiv == "1" ? "L024" : "L004";
        $query .= "         LEFT JOIN V_NAME_MST N2 ";
        $query .= "              ON N2.YEAR    = R1.ENTEXAMYEAR ";
        $query .= "             AND N2.NAMECD1 = '{$namecd1}' ";
        $query .= "             AND N2.NAMECD2 = R1.TESTDIV ";
        $query .= "         LEFT JOIN V_NAME_MST N3 ";
        $query .= "              ON N3.YEAR    = R1.ENTEXAMYEAR ";
        $query .= "             AND N3.NAMECD1 = 'L009' ";
        $query .= "             AND N3.NAMECD2 = P1.TESTSUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "             R1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND VALUE(R1.JUDGEDIV,'0') != '5' "; //受験不可
        $query .= " ORDER BY ";
        $query .= "     TESTSUBCLASSCD, ";
        if ($model->sort == "1") {
            $query .= "     SEATNO, ";
        }
        $query .= "     EXAMNO ";
        return $query;
    }
}
