<?php

require_once('for_php7.php');

class knjl070aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "", $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2 != "") {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($testdiv) {
            $query .= " AND TESTDIV = '".$testdiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特退区分
    public function getHonor($model)
    {
        $query   = " SELECT ";
        $query  .= "   HONORDIV, ";
        $query  .= "   HONORDIV_NAME ";
        $query  .= " FROM ";
        $query  .= "   ENTEXAM_HONORDIV_MST ";
        $query  .= " WHERE ";
        $query  .= "   ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query  .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query  .= "   AND int(HONORDIV) >= 11 ";   //事前専願の区分は11以降のもの

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RCPT.ENTEXAMYEAR, ";
        $query .= "     RCPT.APPLICANTDIV, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     RD006.REMARK1 AS SHDIV, ";
        $query .= "     L006.NAME1 AS SHDIVNAME, ";
        $query .= "     AD030.REMARK3 AS INPUT_SHDIV, ";
        $query .= "     L058.NAME1 AS COURSENAME, ";
        $query .= "     RCPT.TOTAL1 AS SCORE, ";
        $query .= "     EXSC.SCORE AS SCORE_ENG, ";
        $query .= "     L055.NAMESPARE2 AS SCORE_ENG_MINASHI, ";
        $query .= "     RD006.REMARK8 AS COURSECD1, ";
        $query .= "     L013_8.NAME1 AS COURSENAME1, ";
        $query .= "     RD006.REMARK9 AS COURSECD2, ";
        $query .= "     L013_9.NAME1 AS COURSENAME2, ";
        $query  .= "    L057.NAME1 AS DESIREDIV, ";
        $query  .= "    L058_2.NAME1 AS PRE_SENGAN, ";
        $query  .= "    AD029.REMARK1 AS HONORDIV1, ";
        $query  .= "    AD029.REMARK2 AS HONORDIV2, ";
        $query  .= "    AD029.REMARK3 AS HONORDIV3, ";
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
        $query  .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD005 ";
        $query  .= "         ON AD005.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query  .= "        AND AD005.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query  .= "        AND AD005.EXAMNO        = RCPT.EXAMNO ";
        $query  .= "        AND AD005.SEQ = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD030 ";
        $query .= "          ON AD030.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND AD030.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND AD030.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND AD030.SEQ           = '030' ";
        $query  .= "    LEFT JOIN ENTEXAM_SCORE_DAT EXSC ";
        $query  .= "         ON EXSC.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query  .= "        AND EXSC.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query  .= "        AND EXSC.TESTDIV        = RCPT.TESTDIV ";
        $query  .= "        AND EXSC.EXAM_TYPE      = RCPT.EXAM_TYPE ";
        $query  .= "        AND EXSC.RECEPTNO       = RCPT.RECEPTNO ";
        $query  .= "        AND EXSC.TESTSUBCLASSCD = '5' ";
        $query  .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD029 ";
        $query  .= "         ON AD029.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query  .= "        AND AD029.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query  .= "        AND AD029.EXAMNO        = RCPT.EXAMNO ";
        $query  .= "        AND AD029.SEQ           = '029' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L006 ";
        $query .= "          ON L006.YEAR       = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L006.NAMECD1    = 'L006' ";
        $query .= "         AND L006.NAMECD2    = RD006.REMARK1 ";
        $query  .= "    LEFT JOIN V_NAME_MST L055 ";
        $query  .= "         ON L055.YEAR = RCPT.ENTEXAMYEAR ";
        $query  .= "        AND L055.NAMECD1 = 'L055' ";
        $query  .= "        AND L055.NAMECD2 = AD005.REMARK1 ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ";
        $query .= "          ON L058.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L058.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '58' ";
        $query .= "         AND L058.NAMECD2    = RD006.REMARK2 ";
        $query  .= "    LEFT JOIN V_NAME_MST L057 ";
        $query  .= "         ON L057.YEAR       = BASE.ENTEXAMYEAR ";
        $query  .= "        AND L057.NAMECD1    = 'L' || VALUE (L003.NAMESPARE3, 'H') || '57' ";
        $query  .= "        AND L057.NAMECD2    = AD029.REMARK6 ";
        $query  .= "    LEFT JOIN V_NAME_MST L058_2 ";
        $query  .= "         ON L058_2.YEAR     = BASE.ENTEXAMYEAR ";
        $query  .= "        AND L058_2.NAMECD1  = 'L' || VALUE (L003.NAMESPARE3, 'H') || '58' ";
        $query  .= "        AND L058_2.NAMECD2  = AD029.REMARK8 ";
        $query .= "     LEFT JOIN V_NAME_MST L013_8 ";
        $query .= "          ON L013_8.YEAR     = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L013_8.NAMECD1  = 'L' || VALUE(L003.NAMESPARE3,'H') || '13' ";
        $query .= "         AND L013_8.NAMECD2  = RD006.REMARK8 ";
        $query .= "     LEFT JOIN V_NAME_MST L013_9 ";
        $query .= "          ON L013_9.YEAR     = RCPT.ENTEXAMYEAR ";
        $query .= "         AND L013_9.NAMECD1  = 'L' || VALUE(L003.NAMESPARE3,'H') || '13' ";
        $query .= "         AND L013_9.NAMECD2  = RD006.REMARK9 ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND RCPT.TESTDIV        = '".$model->testdiv."' ";
        $query .= "     AND RCPT.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     AND RD006.REMARK1 IS NOT NULL ";
        if ($model->shdiv != "3") {
            $query .= "     AND RD006.REMARK1       = '".$model->shdiv."' ";
        }
        if ($model->shdiv == "1") {
            if ($model->pass_div == "NO_DATA") {
                $query .= "     AND RD006.REMARK8 IS NULL ";
            } elseif ($model->pass_div == "ALL") {
            } else {
                $query .= "     AND RD006.REMARK8 = '".$model->pass_div."' ";
            }
        } else {
            if ($model->pass_div == "NO_DATA") {
                $query .= "     AND (RD006.REMARK8 IS NULL OR RD006.REMARK9 IS NULL) ";
            } elseif ($model->pass_div == "ALL") {
            } else {
                $query .= "     AND (RD006.REMARK8 = '".$model->pass_div."' OR RD006.REMARK9 = '".$model->pass_div."') ";
            }
        }
        if ($model->wish_course != "ALL") {
            $query .= "     AND RD006.REMARK2 = '".$model->wish_course."' ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     RCPT.RECEPTNO ";
        } else {
            $query .= "     VALUE(RCPT.TOTAL1,-1) DESC, ";
            $query .= "     RCPT.RECEPTNO ";
        }

        return $query;
    }

    //存在チェック
    public function checkReceptDetail($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPTNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->examType."' AND ";
        $query .= "     SEQ             = '006' ";
        $query .= " GROUP BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //更新処理
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $detail = array();
        $result = $db->query(knjl070aQuery::checkReceptDetail($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $detail[$row["RECEPTNO"]] = $row["CNT"];
        }
        $result->free();

        //更新フィールド
        $field = ($model->upd_shdiv == "1") ? "REMARK8" : "REMARK9";
        foreach ($model->chkReceptNo as $ReceptNo) {
            //更新
            $data = array();
            $data[$field][TEXT]         = ($model->upd_course == "NO_DATA") ? "" : $model->upd_course;
            $data['REMARK7'][TEXT]      = $model->bikou[$ReceptNo];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            if ($detail[$ReceptNo] > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
                $where .= "   AND EXAM_TYPE     = '".$model->examType."' ";
                $where .= "   AND RECEPTNO      = '".$ReceptNo."' ";
                $where .= "   AND SEQ           = '006' ";

                $db->query(Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where));
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["TESTDIV"][TEXT]      = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]    = $model->examType;
                $data["RECEPTNO"][TEXT]     = $ReceptNo;
                $data["SEQ"][TEXT]          = '006';

                $db->query(Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /*************/
    /*  CSV取込  */
    /*************/

    //受験番号チェック
    public function checkReceptNo($model, $repNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     EXAM_TYPE       = '{$model->examType}' AND ";
        $query .= "     RECEPTNO        = '{$repNo}' ";

        return $query;
    }

    //専併区分取得
    public function getShdiv($model, $repNo)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "     EXAM_TYPE       = '{$model->examType}' AND ";
        $query .= "     RECEPTNO        = '{$repNo}' AND ";
        $query .= "     SEQ             = '006' ";

        return $query;
    }

    //データの追加（CSVデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $detail = array();
        $result = $db->query(knjl070aQuery::checkReceptDetail($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $detail[$row["RECEPTNO"]] = $row["CNT"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //受験番号
            $ReceptNo = $data_arr[$i]["RECEPTNO"];

            //更新フィールド
            $field = ($data_arr[$i]["UPD_SHDIV"] == "1") ? "REMARK8" : "REMARK9";

            //更新
            $data = array();
            $data[$field][TEXT]         = $data_arr[$i]["UPD_COURSE"];
            $data['REMARK7'][TEXT]      = $data_arr[$i]["BIKOU"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            if ($detail[$ReceptNo] > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
                $where .= "   AND EXAM_TYPE     = '".$model->examType."' ";
                $where .= "   AND RECEPTNO      = '".$ReceptNo."' ";
                $where .= "   AND SEQ           = '006' ";

                $db->query(Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where));
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["TESTDIV"][TEXT]      = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]    = $model->examType;
                $data["RECEPTNO"][TEXT]     = $ReceptNo;
                $data["SEQ"][TEXT]          = '006';

                $db->query(Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT"));
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
