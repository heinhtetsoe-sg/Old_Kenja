<?php

require_once('for_php7.php');

class knjl080aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格コース取得
    public function getPassCourseCmb($model)
    {
        $query   = " SELECT ";
        $query .= "     L013.NAMECD2 || ':' || L013.NAME1 AS LABEL, ";
        $query .= "     L013.NAMECD2 AS VALUE, ";
        $query .= "     L013.NAMESPARE2 ";
        $query  .= " FROM ";
        $query  .= "   V_NAME_MST L013 ";
        $query  .= "   INNER JOIN V_NAME_MST L003 ";
        $query  .= "     ON L003.YEAR = L013.YEAR ";
        $query  .= "     AND L003.NAMECD1 = 'L003' ";
        $query  .= "     AND L003.NAMECD2 = '".$model->applicantdiv."' ";
        $query  .= " WHERE ";
        $query  .= "   L013.YEAR = '".$model->ObjYear."' ";
        $query  .= "   AND L013.NAMECD1 = 'L' || L003.NAMESPARE3 || '13' ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     TDIV.TESTDIV_ABBV, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     RD006.REMARK1 AS SHDIV, ";
        $query .= "     RCPT.TOTAL1 AS TOTAL, ";
        $query .= "     L006.NAME1 AS SHDIVNAME, ";
        $query .= "     CASE WHEN (AD030.REMARK1 = RCPT.TESTDIV AND AD030.REMARK2 = RCPT.RECEPTNO) ";
        $query .= "          THEN AD030.REMARK3 ";
        $query .= "          ELSE '' ";
        $query .= "     END AS INPUT_SHDIV, ";
        $query .= "     CASE WHEN (AD030.REMARK1 = RCPT.TESTDIV AND AD030.REMARK2 = RCPT.RECEPTNO) ";
        $query .= "          THEN L012.NAME1 ";
        $query .= "          ELSE '' ";
        $query .= "     END AS COURSENAME, ";
        $query .= "     RD006.REMARK8 AS COURSECD1, ";
        $query .= "     L013_8.NAME1 AS COURSENAME1, ";
        $query .= "     L012_8.NAME1 AS COURSENAME1_2, ";
        $query .= "     RD006.REMARK9 AS COURSECD2, ";
        $query .= "     L013_9.NAME1 AS COURSENAME2, ";
        $query .= "     L012_9.NAME1 AS COURSENAME2_2, ";
        $query .= "     RD006.REMARK7 AS BIKOU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD006 ";
        $query .= "          ON RD006.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND RD006.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND RD006.TESTDIV       = RCPT.TESTDIV ";
        $query .= "         AND RD006.EXAM_TYPE     = RCPT.EXAM_TYPE ";
        $query .= "         AND RD006.RECEPTNO      = RCPT.RECEPTNO ";
        $query .= "         AND RD006.SEQ           = '006' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD030 ";
        $query .= "          ON AD030.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND AD030.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND AD030.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND AD030.SEQ           = '030' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L006 ";
        $query .= "          ON L006.YEAR       = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L006.NAMECD1    = 'L006' ";
        $query .= "         AND L006.NAMECD2    = RD006.REMARK1 ";
        $query .= "     LEFT JOIN V_NAME_MST L012 ";
        $query .= "          ON L012.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L012.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012.NAMECD2    = BASE.ENTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L012_8 ";
        $query .= "          ON L012_8.YEAR       = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L012_8.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012_8.NAMECD2    = RD006.REMARK8 ";
        $query .= "     LEFT JOIN V_NAME_MST L012_9 ";
        $query .= "          ON L012_9.YEAR       = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L012_9.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012_9.NAMECD2    = RD006.REMARK9 ";
        $query .= "     LEFT JOIN V_NAME_MST L013_8 ";
        $query .= "          ON L013_8.YEAR     = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L013_8.NAMECD1  = 'L' || VALUE(L003.NAMESPARE3,'H') || '13' ";
        $query .= "         AND L013_8.NAMECD2  = RD006.REMARK8 ";
        $query .= "     LEFT JOIN V_NAME_MST L013_9 ";
        $query .= "          ON L013_9.YEAR     = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L013_9.NAMECD1  = 'L' || VALUE(L003.NAMESPARE3,'H') || '13' ";
        $query .= "         AND L013_9.NAMECD2  = RD006.REMARK9 ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TDIV ";
        $query .= "          ON TDIV.ENTEXAMYEAR     = RCPT.ENTEXAMYEAR ";
        $query .= "         AND TDIV.APPLICANTDIV    = RCPT.APPLICANTDIV ";
        $query .= "         AND TDIV.TESTDIV         = RCPT.TESTDIV ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '".$model->applicantdiv."' ";
        if ($model->testdiv != "ALL") {
            $query .= "     AND RCPT.TESTDIV        = '".$model->testdiv."' ";
        }
        $query .= "     AND RCPT.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     AND (RD006.REMARK8 IS NOT NULL OR RD006.REMARK9 IS NOT NULL) ";
        if ($model->passCourse != "ALL") {
            $query .= "     AND (RD006.REMARK8 = '".$model->passCourse."' OR ";
            $query .= "          RD006.REMARK9 = '".$model->passCourse."' )";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "1") {
            $query .= "     RCPT.RECEPTNO, ";
        } elseif ($model->sort == "2") {
            $query .= "     CASE WHEN RCPT.TOTAL1 IS NULL THEN -1 ELSE RCPT.TOTAL1 END DESC, ";
        } else {
            $query .= "     BASE.NAME_KANA, ";
        }
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    //存在チェック
    public function checkExists($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     SEQ             = '".$seq."' ";
        $query .= " GROUP BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //更新処理
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $detail = array();
        $seqArray = array("030", "007");
        foreach ($seqArray as $seq) {
            $result = $db->query(knjl080aQuery::checkExists($model, $seq));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $detail[$seq][$row["EXAMNO"]] = $row["CNT"];
            }
            $result->free();
        }

        //入学コース
        $courseArray = array();
        $namecd1= 'L'.$model->skArray[$model->applicantdiv].'12';
        $result = $db->query(knjl080aQuery::getNameMst($model->ObjYear, $namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseArray[$row["VALUE"]] = array($row["ABBV3"], $row["NAMESPARE1"], $row["NAMESPARE2"]);
        }
        $result->free();

        //入学コースのクリア
        foreach ($model->setReceptNo as $key => $ReceptNo) {
            if ($model->arrInputShDiv[$ReceptNo] == $model->arrDefaultScore[$ReceptNo]) {
                continue;
            }
            $examno = $model->arrExamNo[$ReceptNo];

            // ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTDIV"][TEXT]       = "";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";

            $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where));
            $array = array();
            $array["030"] = array($model->arrTestDiv[$ReceptNo], $ReceptNo, $model->arrInputShDiv[$ReceptNo]);
            $array["007"] = $courseArray[$course];
            foreach ($array as $seq => $val) {
                $data = array();
                $data["REMARK2"][TEXT]      = "";
                $data["REMARK3"][TEXT]      = "";
                $data["REMARK1"][TEXT]      = "";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "SYSDATE()";

                if ($detail[$seq][$examno] > 0) {
                    $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                    $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                    $where .= "   AND EXAMNO        = '".$examno."' ";
                    $where .= "   AND SEQ           = '".$seq."' ";

                    $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where));
                } else {
                    $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                    $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                    $data["EXAMNO"][TEXT]       = $examno;
                    $data["SEQ"][TEXT]          = $seq;

                    $db->query(Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT"));
                }
                $detail[$seq][$examno] = 1;
            }
        }

        //更新
        foreach ($model->setReceptNo as $key => $ReceptNo) {
            if (!$model->arrInputShDiv[$ReceptNo]) {
                continue;
            }
            if ($model->arrInputShDiv[$ReceptNo] == $model->arrDefaultScore[$ReceptNo]) {
                continue;
            }
            $examno = $model->arrExamNo[$ReceptNo];
            $course = $model->arrCoursecd[$ReceptNo][$model->arrInputShDiv[$ReceptNo]];

            // ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTDIV"][TEXT]       = ($model->arrInputShDiv[$ReceptNo]) ? $course : "";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";

            $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where));

            // ENTEXAM_APPLICANTBASE_DETAIL_DAT
            $array = array();
            $array["030"] = array($model->arrTestDiv[$ReceptNo], $ReceptNo, $model->arrInputShDiv[$ReceptNo]);
            $array["007"] = $courseArray[$course];
            foreach ($array as $seq => $val) {
                $data = array();
                $data["REMARK1"][TEXT]      = ($model->arrInputShDiv[$ReceptNo]) ? $val[0] : "";
                $data["REMARK2"][TEXT]      = ($model->arrInputShDiv[$ReceptNo]) ? $val[1] : "";
                $data["REMARK3"][TEXT]      = ($model->arrInputShDiv[$ReceptNo]) ? $val[2] : "";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "SYSDATE()";

                if ($detail[$seq][$examno] > 0) {
                    $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                    $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                    $where .= "   AND EXAMNO        = '".$examno."' ";
                    $where .= "   AND SEQ           = '".$seq."' ";

                    $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where));
                } else {
                    $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                    $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                    $data["EXAMNO"][TEXT]       = $examno;
                    $data["SEQ"][TEXT]          = $seq;

                    $db->query(Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT"));
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
