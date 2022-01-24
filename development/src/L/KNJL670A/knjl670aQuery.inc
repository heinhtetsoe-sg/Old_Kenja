<?php

require_once('for_php7.php');

class knjl670aQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($namecd1, $year, $isShdiv = false)
    {
        $query  = " WITH T_SHDIV(VALUE, LABEL, NAMESPARE2) AS ( ";
        $query .= "     VALUES('9', '9' || ':' || '全員', '') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        //専併区分コンボボックス
        if ($isShdiv) {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     VALUE,";
            $query .= "     LABEL, ";
            $query .= "     NAMESPARE2 ";
            $query .= " FROM ";
            $query .= "     T_SHDIV ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより取得
    public function getNameMst2($namecd1, $year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //受験コース
    public function getCourseCode($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    public function SelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.EXAMNO, ";
        $query .= "   T1.RECEPTNO, ";
        $query .= "   T2.NAME, ";
        $query .= "   CASE WHEN '{$model->coursecode}' = T3.REMARK1 THEN ECM1.EXAMCOURSE_NAME ";
        $query .= "        WHEN '{$model->coursecode}' = T3.REMARK2 THEN ECM2.EXAMCOURSE_NAME ";
        $query .= "        ELSE '' ";
        $query .= "   END AS DESIRE_COURSE, ";
        $query .= "   CASE WHEN '{$model->coursecode}' = T3.REMARK1 THEN '1' ";
        $query .= "        WHEN '{$model->coursecode}' = T3.REMARK2 THEN '2' ";
        $query .= "        ELSE '' ";
        $query .= "   END AS DESIRE_RANK, ";
        //画面選択コースが第二志望なら、TOTAL1を。それ以外はTOTAL4を得点として取得
        if ($model->applicantdiv == "1" && $model->perfectKeishaCount == 0) {
            $query .= "   CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN VALUE(T1.TOTAL2, T1.TOTAL1) ELSE VALUE(TOTAL3, TOTAL4) END AS TOTAL4, ";
        } else {
            $query .= "   CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN T1.TOTAL1 ELSE TOTAL4 END AS TOTAL4, ";
        }
        //$query .= "   T1.JUDGEDIV, ";
        $query .= "  CASE WHEN '{$model->coursecode}' = T3.REMARK1 THEN T3.REMARK7 ELSE T3.REMARK8 END AS JUDGEDIV, ";
        $query .= "  CASE WHEN '{$model->coursecode}' = T3.REMARK1 THEN L013_1.NAME1 ELSE L013_2.NAME1 END AS JUDGEDIV_NAME, ";
        //コース併願&第2志望コースの表示で、合格コースが第1コースなら第1志望コース名、それ以外は空文字
        $query .= "   CASE WHEN ('{$model->coursecode}' = T3.REMARK2 AND T3.REMARK1 = T2.SUC_COURSECODE AND L013_JDG.NAMESPARE1 = '1') THEN ECM1.EXAMCOURSE_NAME ";
        $query .= "        ELSE '' ";
        $query .= "   END AS FIRST_PASSCOURSE ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3 ";
        $query .= "     ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND T3.TESTDIV = T1.TESTDIV ";
        $query .= "    AND T3.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "    AND T3.RECEPTNO = T1.RECEPTNO ";
        $query .= "    AND T3.SEQ = '007' ";
        $query .= "   LEFT JOIN ENTEXAM_COURSE_MST ECM1 ";
        $query .= "     ON ECM1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND ECM1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND ECM1.TESTDIV = T1.TESTDIV ";
        $query .= "    AND ECM1.EXAMCOURSECD = T3.REMARK1 ";
        $query .= "   LEFT JOIN ENTEXAM_COURSE_MST ECM2 ";
        $query .= "     ON ECM2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND ECM2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "    AND ECM2.TESTDIV = T1.TESTDIV ";
        $query .= "    AND ECM2.EXAMCOURSECD = T3.REMARK2 ";
        $query .= "   LEFT JOIN V_NAME_MST L013_1 ";
        $query .= "     ON L013_1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND L013_1.NAMECD1 = 'L013' ";
        $query .= "    AND L013_1.NAMECD2 = T3.REMARK7 ";
        $query .= "   LEFT JOIN V_NAME_MST L013_2 ";
        $query .= "     ON L013_2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND L013_2.NAMECD1 = 'L013' ";
        $query .= "    AND L013_2.NAMECD2 = T3.REMARK8 ";
        $query .= "   LEFT JOIN V_NAME_MST L013_JDG ";
        $query .= "     ON L013_JDG.YEAR = T1.ENTEXAMYEAR ";
        $query .= "    AND L013_JDG.NAMECD1 = 'L013' ";
        $query .= "    AND L013_JDG.NAMECD2 = T2.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "   AND (T3.REMARK1 = '{$model->coursecode}' OR T3.REMARK2 = '{$model->coursecode}') ";
        if ($model->leastScore != "") {
            if ($model->applicantdiv == "1" && $model->perfectKeishaCount == 0) {
                $query .= " AND CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN VALUE(T1.TOTAL2, T1.TOTAL1) ELSE VALUE(TOTAL3, TOTAL4) END >= ".$model->leastScore." ";
            } else {
                $query .= " AND CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN T1.TOTAL1 ELSE TOTAL4 END >= ".$model->leastScore." ";
            }
        }
        if ($model->topScore != "") {
            if ($model->applicantdiv == "1" && $model->perfectKeishaCount == 0) {
                $query .= " AND CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN VALUE(T1.TOTAL2, T1.TOTAL1) ELSE VALUE(TOTAL3, TOTAL4) END <= ".$model->topScore." ";
            } else {
                $query .= " AND CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN T1.TOTAL1 ELSE TOTAL4 END <= ".$model->topScore." ";
            }
        }
        if ($model->rejectPass != "") {
            $query .= " AND VALUE(L013_1.NAMESPARE1, '') <> '1' ";
        }
        if ($model->rejectUnPass != "") {
            $query .= " AND VALUE(T1.JUDGEDIV, '') <> '2' ";
        }
        $query .= " ORDER BY ";
        if ($model->applicantdiv == "1" && $model->perfectKeishaCount == 0) {
            if ($model->sort == "1") {
                $query .= " CASE WHEN CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN VALUE(T1.TOTAL2, T1.TOTAL1) ELSE VALUE(TOTAL3, TOTAL4) END IS NULL THEN 0 ELSE 1 END DESC, ";
                $query .= " CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN VALUE(T1.TOTAL2, T1.TOTAL1) ELSE VALUE(TOTAL3, TOTAL4) END DESC, ";
            } elseif ($model->sort == "2") {
                $query .= " CASE WHEN CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN VALUE(T1.TOTAL2, T1.TOTAL1) ELSE VALUE(TOTAL3, TOTAL4) END IS NULL THEN 0 ELSE 1 END DESC, ";
                $query .= " CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN VALUE(T1.TOTAL2, T1.TOTAL1) ELSE VALUE(TOTAL3, TOTAL4) END ASC, ";
            }
        } else {
            if ($model->sort == "1") {
                $query .= " CASE WHEN CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN T1.TOTAL1 ELSE TOTAL4 END IS NULL THEN 0 ELSE 1 END DESC, ";
                $query .= " CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN T1.TOTAL1 ELSE TOTAL4 END DESC, ";
            } elseif ($model->sort == "2") {
                $query .= " CASE WHEN CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN T1.TOTAL1 ELSE TOTAL4 END IS NULL THEN 0 ELSE 1 END DESC, ";
                $query .= " CASE WHEN '{$model->coursecode}' = T3.REMARK2 THEN T1.TOTAL1 ELSE TOTAL4 END ASC, ";
            }
        }
        $query .= " T1.RECEPTNO ASC ";

        return $query;
    }

    public function getCoursePriority($model)
    {
        $query .= " SELECT DISTINCT";
        $query .= "   T1.EXAMCOURSECD, ";
        $query .= "   T1.PRIORITY ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR    = '{$model->ObjYear}'";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$model->testdiv}' ";
        return $query;
    }

    public function getPerfectKeishaCount($model)
    {
        $query .= " SELECT ";
        $query .= "   COUNT(MAGNIFYING) ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_PERFECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR    = '{$model->ObjYear}'";
        $query .= "   AND T1.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV       = '{$model->testdiv}' ";
        $query .= "   AND T1.COURSECD || T1.MAJORCD = '0000' ";
        $query .= "   AND T1.EXAMCOURSECD = '{$model->coursecode}' ";
        if ($model->applicantdiv == '2') {
            $query .= "   AND T1.TESTSUBCLASSCD <> 'A' ";
        }
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $opt_priority = array();
        $result = $db->query(knjl670aQuery::getCoursePriority($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_priority[$row["EXAMCOURSECD"]] = $row["PRIORITY"];
        }

        foreach ($model->score as $receptno => $val) {
            //名称マスタの取得（判定コード）
            $opt_judge = array();
            $result = $db->query(knjl670aQuery::getNameMst2("L013", $model->ObjYear));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_judge[$row["NAMECD2"]] = $row["NAMESPARE1"];   //合格扱い
            }

            //RECEPT_DAT
            //更新前のデータを取得
            $cur_data = array();
            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "       AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "       AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "       AND EXAM_TYPE     = '".$model->exam_type."' ";
            $where .= "       AND RECEPTNO      = '".$receptno."' ";

            //ここのSQLでEXAMNOを取得しておいて、後で利用。
            $examno = "";
            $result    = $db->query(" SELECT * FROM ENTEXAM_RECEPT_DAT ".$where);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $examno = $row["EXAMNO"];
                //未設定または設定値
                $cur_data["JUDGEDIV"][TEXT] = $val["JUDGEDIV"];
                $cur_data["REGISTERCD"][TEXT]   = STAFFCD;
                $cur_data["UPDATED"][FUNC]      = "sysdate()";

                //画面表示データの母集団データであり、存在することが大前提のため、更新only
                $query = Query::updateSQL($cur_data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);
            }

            //RECEPT_DETAIL_DAT
            $where3 = $where;
            $where3 .= "       AND SEQ      = '007' ";
            $cur_data = array();
            $result    = $db->query(" SELECT * FROM ENTEXAM_RECEPT_DETAIL_DAT ".$where3);
            //データがあることが大前提のデータなので、update前提。
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //更新
                if ($model->coursecode == $row["REMARK2"]) {
                    $cur_data["REMARK8"][TEXT] = $val["JUDGEDIV"];
                } else {
                    $cur_data["REMARK7"][TEXT] = $val["JUDGEDIV"];
                    //合格なら第二志望の入力はクリア
                    if ($val["JUDGEDIV"] == "1") {
                        $cur_data["REMARK8"][TEXT] = "";
                    }
                }
                $cur_data["REGISTERCD"][TEXT]   = STAFFCD;
                $cur_data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::updateSQL($cur_data, "ENTEXAM_RECEPT_DETAIL_DAT", $where3);
            }
            $db->query($query);

            //RECEPT_DETAIL_DAT
            $where2 = $where;
            $where2 .= "       AND SEQ      = '001' ";
            $cur_data = array();
            $result    = $db->query(" SELECT * FROM ENTEXAM_RECEPT_DETAIL_DAT ".$where2);
            //恐らく、第一志望/第二志望が入っていることが前提になるはずだが、念のためチェックしておく
            if ($result->numRows() == 0) {
                //新規登録
                $cur_data["ENTEXAMYEAR"][TEXT] = $model->ObjYear;
                $cur_data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $cur_data["TESTDIV"][TEXT] = $model->testdiv;
                $cur_data["EXAM_TYPE"][TEXT] = $model->exam_type;
                $cur_data["RECEPTNO"][TEXT] = $receptno;
                $cur_data["SEQ"][TEXT] = "001";
                $cur_data["REMARK3"][TEXT] = $model->coursecode;
                $cur_data["REGISTERCD"][TEXT]   = STAFFCD;
                $cur_data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($cur_data, "ENTEXAM_RECEPT_DETAIL_DAT");
            } else {
                //更新
                $cur_data["REMARK3"][TEXT] = $model->coursecode;
                $cur_data["REGISTERCD"][TEXT]   = STAFFCD;
                $cur_data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::updateSQL($cur_data, "ENTEXAM_RECEPT_DETAIL_DAT", $where2);
            }
            $db->query($query);

            //APPLICANTBASE_DAT
            if ($examno != "") {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                $where .= "       AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "       AND EXAMNO      = '".$examno."' ";
                $cur_data = array();
                $result    = $db->query(" SELECT * FROM ENTEXAM_APPLICANTBASE_DAT ".$where);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //画面表示データの母集団データの上位データなので、存在することが大前提のため、更新only
                    //合否が一致しないまたは不合格->合格または合格コースが未設定または"優位コースの合格"であれば、登録
                    if (($row["JUDGEMENT"] != $val["JUDGEDIV"] && $row["SUC_COURSECODE"] == $model->coursecode)
                        || $row["SUC_COURSECODE"] == ""
                        || $row["JUDGEMENT"] > $val["JUDGEDIV"]
                        || ($row["JUDGEMENT"] == $val["JUDGEDIV"] && $row["SUC_COURSECODE"] != "" && ($opt_priority[$row["SUC_COURSECODE"]] > $opt_priority[$model->coursecode]) ) ) {
                        $cur_data["JUDGEMENT"][TEXT] = $val["JUDGEDIV"];
                        $cur_data["SUC_COURSECD"][TEXT] = $model->coursecd;
                        $cur_data["SUC_MAJORCD"][TEXT] = $model->majorcd;
                        $cur_data["SUC_COURSECODE"][TEXT] = $model->coursecode;
                        $cur_data["REGISTERCD"][TEXT]   = STAFFCD;
                        $cur_data["UPDATED"][FUNC]      = "sysdate()";
                        $query = Query::updateSQL($cur_data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                        $db->query($query);
                    }
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}