<?php

require_once('for_php7.php');
class knjl550fQuery extends Query {
    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     ABBV2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //推薦入試は表示しない
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //IEEA判断取得
    function getNameMstL024($year, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     ABBV2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L024' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //受験型
    function getExamType($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             EXAM_TYPE ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "             AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "             AND TESTDIV = '{$model->testdiv}' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //受験科目
    function getTestSubclasscd($model, $nameCd2="") {
        $name = ($model->applicantdiv == "1") ? "NAME1" : "NAME2";

        $query  = " SELECT ";
        $query .= "     ".$name." AS CSV_NAME, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || ".$name." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     ".$name." IS NOT NULL AND ";
        //満点マスタの設定が前提
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                     TESTDIV         = '{$model->testdiv}' ";

        if ($model->applicantdiv == "1" && (in_array($model->testdiv, array("9", "10", "11"))) && strlen($model->exam_type)) {
            $query .= "                 AND EXAM_TYPE = '{$model->exam_type}' ";
        }
        $query .= "                 ) ";
        if ($nameCd2) {
            $query .= " AND NAMECD2     = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg) {
        $namecd1 = ($model->applicantdiv == "1")  ? "L024" : "L004";

        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         L1.ATTEND_FLG, ";
        $query .= "         L1.SCORE, ";
        $query .= "         VALUE(S4.PERFECT, 100) AS PERFECT, ";
        $query .= "         N1.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "         N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "         CASE WHEN T1.APPLICANTDIV = '1' THEN N3.NAME1 ELSE N3.NAME2 END AS TESTSUBCLASSCD_NAME ";
        if ($model->applicantdiv == "1") {
            $query .= "        ,N5.NAME1 AS EXAM_TYPE_NAME ";
            $query .= "        ,INTV.INTERVIEW_REMARK2 ";
        }
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S2 ";
        $query .= "              ON S2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S2.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S2.EXAMNO           = T1.EXAMNO ";
        $query .= "             AND S2.SEQ              = '001' ";
        if ($model->applicantdiv == "1") {
            $query .= "         INNER JOIN (SELECT DISTINCT ";
            $query .= "                         SA1.RECEPTNO, ";
            $query .= "                         SA2.EXAM_TYPE, ";
            $query .= "                         SA3.SELECT_SUBCLASS ";
            $query .= "                     FROM ";
            $query .= "                         V_ENTEXAM_RECEPT_DAT SA1 ";
            $query .= "                         INNER JOIN ( SELECT ";
            $query .= "                                         EXAMNO, ";
            $query .= "                                         REMARK{$model->testdiv} AS EXAM_TYPE ";
            $query .= "                                     FROM ";
            $query .= "                                         ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= "                                     WHERE ";
            $query .= "                                         ENTEXAMYEAR = '{$model->ObjYear}' AND ";
            $query .= "                                         APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "                                         SEQ = '011' AND ";
            $query .= "                                         REMARK{$model->testdiv} IS NOT NULL ";
            $query .= "                                 ) SA2 ON SA1.EXAMNO = SA2.EXAMNO ";
            $query .= "                         LEFT JOIN ( SELECT ";
            $query .= "                                         EXAMNO, ";
            $query .= "                                         REMARK{$model->testdiv} AS SELECT_SUBCLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= "                                     WHERE ";
            $query .= "                                         ENTEXAMYEAR = '{$model->ObjYear}' AND ";
            $query .= "                                         APPLICANTDIV = '{$model->applicantdiv}' AND ";
            $query .= "                                         SEQ = '013' AND ";
            $query .= "                                         REMARK{$model->testdiv} IS NOT NULL ";
            $query .= "                                 ) SA3 ON SA1.EXAMNO = SA3.EXAMNO ";
            $query .= "                     WHERE ";
            $query .= "                         SA1.ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
            $query .= "                         SA1.APPLICANTDIV    = '{$model->applicantdiv}' AND ";
            $query .= "                         SA1.TESTDIV         = '{$model->testdiv}') A1 ";
            $query .= "              ON A1.RECEPTNO = T1.RECEPTNO ";
            if ($model->testsubclasscd == "2" || $model->testsubclasscd == "5") {
                $query .= "             AND (CASE WHEN A1.EXAM_TYPE = '5' THEN A1.SELECT_SUBCLASS ELSE '' END) = (CASE WHEN A1.EXAM_TYPE = '5' THEN '{$model->testsubclasscd}' ELSE '' END) ";
            }
        }
        $query .= "         INNER JOIN ENTEXAM_PERFECT_EXAMTYPE_MST S4 ";
        $query .= "              ON S4.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S4.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S4.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND S4.COURSECD         = S2.REMARK8 ";
        $query .= "             AND S4.MAJORCD          = S2.REMARK9 ";
        $query .= "             AND S4.EXAMCOURSECD     = S2.REMARK10 ";
        if ($model->applicantdiv == "1") {
            $query .= "             AND S4.EXAM_TYPE        = A1.EXAM_TYPE ";
        } else {
            $query .= "             AND S4.EXAM_TYPE        = '1'";
        }
        $query .= "             AND S4.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAM_TYPE        = T1.EXAM_TYPE ";
        $query .= "             AND L1.RECEPTNO         = T1.RECEPTNO ";
        $query .= "             AND L1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN NAME_MST N1 ";
        $query .= "              ON N1.NAMECD1  = 'L003' ";
        $query .= "             AND N1.NAMECD2  = T1.APPLICANTDIV ";
        $query .= "         LEFT JOIN NAME_MST N2 ";
        $query .= "              ON N2.NAMECD1  = '{$namecd1}' ";
        $query .= "             AND N2.NAMECD2  = T1.TESTDIV ";
        $query .= "         LEFT JOIN V_NAME_MST N3 ";
        $query .= "              ON N3.YEAR     = T1.ENTEXAMYEAR ";
        $query .= "             AND N3.NAMECD1  = 'L009' ";
        $query .= "             AND N3.NAMECD2  = '{$model->testsubclasscd}' ";
        if ($model->applicantdiv == "1") {
            $query .= "         LEFT JOIN V_NAME_MST N5 ";
            $query .= "              ON N5.YEAR     = T1.ENTEXAMYEAR ";
            $query .= "             AND N5.NAMECD1  = 'L005' ";
            $query .= "             AND N5.NAMECD2  = A1.EXAM_TYPE ";
            $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ";
            $query .= "              ON INTV.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "             AND INTV.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "             AND INTV.TESTDIV      = T1.TESTDIV ";
            $query .= "             AND INTV.EXAMNO       = T1.EXAMNO ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' ";
        if ($model->applicantdiv == "2") {
            if ($model->testsubclasscd == "2" || $model->testsubclasscd == "4") {
                $query .= "     AND T1.EXAM_TYPE = '4' "; // 5教科型
            }
        }

        if (strlen($model->special_reason_div)) {
            $query .= "     AND S1.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "     AND VALUE(T1.JUDGEDIV,'') <> '4' ";
        }
        if ($model->target == "2") {
            $query .= "     AND VALUE(S1.GENERAL_FLG,'') != '1' ";
        }
        if ($model->target == "3") {
            $query .= "     AND VALUE(S1.GENERAL_FLG,'') = '1' ";
        }
        if ($model->applicantdiv == "1" && (in_array($model->testdiv, array("9", "10", "11"))) && strlen($model->exam_type)) {
            $query .= "     AND A1.EXAM_TYPE = '{$model->exam_type}' ";
        }
        if ($flg == "") {
            if ($model->cmd == "back") {
                $query .= "   AND T1.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO DESC ";
            } else if ($model->cmd == "next") {
                $query .= "   AND T1.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($flg == "BAC_COUNT" || $flg == "NEXT_COUNT") {
            if ($flg == "BAC_COUNT") {
                $query .= "   AND T1.RECEPTNO < '".$model->s_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO DESC ";
            } else if ($flg == "NEXT_COUNT") {
                $query .= "   AND T1.RECEPTNO > '".$model->e_receptno."' ";
                $query .= " ORDER BY T1.RECEPTNO ";
            }
        } else if ($flg == "CHECK") {
                $query .= "   AND T1.RECEPTNO >= '".$model->s_receptno."' ";
        }
        $query .= "     FETCH FIRST 500 ROWS ONLY ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "BAC_COUNT" && $flg != "NEXT_COUNT" ) {
            $query .= " ORDER BY RECEPTNO";
        }

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();

        //満点チェック
        $result = $db->query(knjl550fQuery::SelectQuery($model, "CHECK"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkScore = $model->score[$row["RECEPTNO"]];
            if ($checkScore != "*" && !is_numeric($checkScore) && strlen($checkScore)) {
                $model->setWarning("MSG901","得点欄は数値又は、＊(小文字)のみ入力可能です。\\n受験番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
            if ($checkScore > (int)$row["PERFECT"] && strlen($checkScore)) {
                $model->setWarning("MSG901","満点：".(int)$row["PERFECT"]."以下で入力してください\\n受験番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
        }

        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM entexam_score_dat ";
        $query .= " WHERE entexamyear    = '".$model->ObjYear."' ";
        $query .= "   AND applicantdiv   = '".$model->applicantdiv."' ";
        $query .= "   AND testdiv        = '".$model->testdiv."' ";
        $query .= "   AND testsubclasscd = '".$model->testsubclasscd."' ";
        $query .= "   AND receptno IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        //追加
        foreach ($model->score as $receptno => $score) {
            //EXAM_TYPE
            $query  = " SELECT EXAM_TYPE FROM ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $query .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $query .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $query .= "   AND RECEPTNO      = '".$receptno."' ";
            $exam_type = $db->getOne($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $exam_type;
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

        //追加
        if ($model->applicantdiv == "1" && $model->ieeFlg) {
            foreach ($model->remark2 as $receptno_examno => $remark2) {
                list($receptno, $examno) = explode("-", $receptno_examno);

                //cnt
                $getCount = $db->getOne(knjl550fQuery::getCountInterview($model, $examno));

                $data = array();
                $data["INTERVIEW_REMARK2"][TEXT]    = $remark2;

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                if ($getCount == 0) {
                    $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                    $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                    $data["TESTDIV"][TEXT]          = $model->testdiv;
                    $data["EXAMNO"][TEXT]           = $examno;

                    $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
                } else {
                    $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
                    $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
                    $where .= "   AND TESTDIV      = '".$model->testdiv."' ";
                    $where .= "   AND EXAMNO       = '".$examno."' ";

                    $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //カウント
    function getCountInterview($model, $examno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $query .= "     AND EXAMNO          = '{$examno}' ";

        return $query;
    }
}
?>