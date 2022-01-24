<?php

require_once('for_php7.php');
class knjl310fquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試区分大分類コンボ　1:帰国生入試 2:一般入試
    public function getTestLDiv($year, $namecd1, $test_l_div = "")
    {
        $query  = " WITH T_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV2, ";
        $query .= "         NAMESPARE2 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR    = '".$year."' AND ";
        $query .= "         NAMECD1 = '".$namecd1."' ";
        $query .= " ), T_TEST_L_DIV AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS VALUE, ";
        $query .= "         '1:帰国生入試' AS LABEL, ";
        $query .= "         '帰国生入試' AS NAME ";
        $query .= "     FROM ";
        $query .= "         T_COMMON ";
        $query .= "     WHERE ";
        $query .= "         ABBV2 IS NOT NULL ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         '2' AS VALUE, ";
        $query .= "         '2:一般入試' AS LABEL, ";
        $query .= "         '一般入試' AS NAME ";
        $query .= "     FROM ";
        $query .= "         T_COMMON ";
        $query .= "     WHERE ";
        $query .= "         ABBV2 IS NULL ";
        $query .= " ), T_DEF AS ( ";
        $query .= "     SELECT ";
        $query .= "         CASE WHEN ABBV2 IS NOT NULL THEN '1' ELSE '2' END AS VALUE, ";
        $query .= "         NAMESPARE2 ";
        $query .= "     FROM ";
        $query .= "         T_COMMON ";
        $query .= "     WHERE ";
        $query .= "         NAMECD2 IN ( ";
        $query .= "             SELECT ";
        $query .= "                 MIN(NAMECD2) AS MIN_NAMECD2 ";
        $query .= "             FROM ";
        $query .= "                 T_COMMON ";
        $query .= "             WHERE ";
        $query .= "                 NAMESPARE2 IS NOT NULL) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.LABEL, ";
        $query .= "     T1.VALUE, ";
        $query .= "     L1.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     T_TEST_L_DIV T1 ";
        $query .= "     LEFT JOIN T_DEF L1 ON L1.VALUE = T1.VALUE ";
        if ($test_l_div) {
            $query .= " WHERE ";
            $query .= "     T1.VALUE = '".$test_l_div."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //受験料取得
    public function getPayMoney($model, $data_arr)
    {
        //免除額コードを取得(BASE)
        $query  = " WITH V_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         EXEMPTION_CD ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_EXEMPTION_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "         APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "         EXAMNO          = '{$data_arr["EXAMNO"]}' ";
        //基本額を取得（費目マスタ）
        $query .= " ), T_ITEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         SUM(ITEM_MONEY) AS ITEM_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "         APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "         DIV             = '0' AND ";
        $query .= "         REMARK4         = '1' ";        //受験料
        $query .= " ) ";
        //基本額から免除額を引いた金額を取得
        $query .= " SELECT ";
        $query .= "     VALUE(L1.ITEM_MONEY, 0) AS MONEY ";
        $query .= " FROM ";
        $query .= "     V_COMMON T1 ";
        $query .= "     LEFT JOIN T_ITEM L1 ON L1.LINK = T1.LINK ";

        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元号取得
        $result = $db->query(knjl310fQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //入試区分取得
        $testdiv_array = array();
        $result = $db->query(knjl310fQuery::getNameMstAll($model->ObjYear, "L024"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["TEST_L_DIV"] == "1" && $row["ABBV2"] == "1") {
                $testdiv_array[] = $row["NAMECD2"];
            } elseif ($model->field["TEST_L_DIV"] == "2" && $row["ABBV2"] != "1") {
                $testdiv_array[] = $row["NAMECD2"];
            }
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth", "FS_DAY" => "fs");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list($y, $m, $d) = preg_split('/-/', $data_arr[$i][$dkey]);
                    //部品の変数名
                    list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                    $$e_id = $$y_id = $$m_id = $$d_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            break;
                        }
                    }
                }
            }

            //データ件数取得
            $base_cnt       = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl310fQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $money_cnt      = $db->getOne(knjl310fQuery::cntEntexamMoneyDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));
            $baseD002_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "002"));
            $baseD010_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "010"));
            $baseD011_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "011"));
            $baseD012_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "012"));
            $baseD013_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "013"));
            $baseD014_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "014"));
            $baseD015_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "015"));
            $baseD016_cnt   = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "016"));

            //受験料取得
            $exam_money = "";
            if (strlen($data_arr[$i]["EXAM_PAY_DATE"])) {
                $exam_money     = $db->getOne(knjl310fQuery::getPayMoney($model, $data_arr[$i]));
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            if ($model->field["APPLICANTDIV"] == "1") {
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["TESTDIV"][TEXT]              = $data_arr[$i]["TEST_L_DIV"];
                $data["SHDIV"][TEXT]                = '1';
                $data["DESIREDIV"][TEXT]            = '1';
                $data["RECEPTDATE"][TEXT]           = $data_arr[$i]["RECEPTDATE"];
                $data["TESTDIV1"][TEXT]             = $data_arr[$i]["GANSHO_YUUSOU"];
                $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
                $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
                $data["SEX"][TEXT]                  = '2';
                $data["ERACD"][TEXT]                = $birth_eracd;
                $data["BIRTH_Y"][TEXT]              = $birth_y;
                $data["BIRTH_M"][TEXT]              = $birth_m;
                $data["BIRTH_D"][TEXT]              = $birth_d;
                $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
                $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
                $data["FS_ERACD"][TEXT]             = $fs_eracd;
                $data["FS_Y"][TEXT]                 = $fs_y;
                $data["FS_M"][TEXT]                 = $fs_m;
                $data["FS_DAY"][TEXT]               = $data_arr[$i]["FS_DAY"];
                $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
            } elseif ($model->field["APPLICANTDIV"] == "2") {
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
                $data["SHDIV"][TEXT]                = $data_arr[$i]["SHDIV"];
                $data["DESIREDIV"][TEXT]            = '1';
                $data["TESTDIV0"][TEXT]             = $data_arr[$i]["TESTDIV0"];
                $data["RECEPTDATE"][TEXT]           = $data_arr[$i]["RECEPTDATE"];
                $data["TESTDIV1"][TEXT]             = $data_arr[$i]["GANSHO_YUUSOU"];
                $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
                $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
                $data["SEX"][TEXT]                  = '2';
                $data["ERACD"][TEXT]                = $birth_eracd;
                $data["BIRTH_Y"][TEXT]              = $birth_y;
                $data["BIRTH_M"][TEXT]              = $birth_m;
                $data["BIRTH_D"][TEXT]              = $birth_d;
                $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
                $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
                $data["FS_ERACD"][TEXT]             = $fs_eracd;
                $data["FS_Y"][TEXT]                 = $fs_y;
                $data["FS_M"][TEXT]                 = $fs_m;
                $data["FS_DAY"][TEXT]               = $data_arr[$i]["FS_DAY"];
                $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
                $data["SELECT_SUBCLASS_DIV"][TEXT]  = $data_arr[$i]["SELECT_SUBCLASS_DIV"];
                $data["SHIFT_DESIRE_FLG"][TEXT]     = $data_arr[$i]["SHIFT_DESIRE_FLG"];
                $data["SLIDE_FLG"][TEXT]            = $data_arr[$i]["SLIDE_FLG"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_MONEY_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["EXAM_PAY_DIV"][TEXT]         = !strlen($data_arr[$i]["EXAM_PAY_DATE"]) ? "" : $data_arr[$i]["EXAM_PAY_DIV"];
            $data["EXAM_PAY_DATE"][TEXT]        = !strlen($data_arr[$i]["EXAM_PAY_DATE"]) ? "" : $data_arr[$i]["EXAM_PAY_DATE"];
            $data["EXAM_PAY_CHAK_DATE"][TEXT]   = $data_arr[$i]["EXAM_PAY_CHAK_DATE"];
            $data["EXAM_PAY_MONEY"][NUMBER]     = !strlen($data_arr[$i]["EXAM_PAY_DATE"]) ? "" : $exam_money;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $money_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_MONEY_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "001";
            $data["REMARK8"][TEXT]       = $data_arr[$i]["COURSECD"];
            $data["REMARK9"][TEXT]       = $data_arr[$i]["MAJORCD"];
            $data["REMARK10"][TEXT]      = $data_arr[$i]["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $baseD001_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=002
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "002";
            $data["REMARK1"][TEXT]       = $data_arr[$i]["RECRUIT_NO"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '002' ";

            if (0 == $baseD002_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            if ($model->field["APPLICANTDIV"] == "1") {
                //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT SEQ=010
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
                $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]           = "010";
                foreach ($testdiv_array as $testdiv) {
                    $data["REMARK".$testdiv][TEXT] = ($data_arr[$i]["TESTDIV".$testdiv."_FLG"] == "1") ? $testdiv : "";
                }
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '010' ";

                if (0 == $baseD010_cnt) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);

                //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT SEQ=011
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
                $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]           = "011";
                foreach ($testdiv_array as $testdiv) {
                    $data["REMARK".$testdiv][TEXT] = $data_arr[$i]["TESTDIV".$testdiv."_EXAM_TYPE"];
                }
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '011' ";

                if (0 == $baseD011_cnt) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);

                //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT SEQ=012
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
                $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]           = "012";
                foreach ($testdiv_array as $testdiv) {
                    $data["REMARK".$testdiv][TEXT] = $data_arr[$i]["TESTDIV".$testdiv."_RECEPTNO"];
                }
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '012' ";

                if (0 == $baseD012_cnt) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                }
                $db->query($query);

                if ($model->field["TEST_L_DIV"] == "1") {
                    //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT SEQ=013
                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]   = $model->ObjYear;
                    $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
                    $data["SEQ"][TEXT]           = "013";
                    foreach ($testdiv_array as $testdiv) {
                        $data["REMARK".$testdiv][TEXT] = $data_arr[$i]["TESTDIV".$testdiv."_TESTSUBCLASSCD"];
                    }
                    $data["REGISTERCD"][TEXT]    = STAFFCD;
                    $data["UPDATED"][NUMBER]     = "SYSDATE()";

                    $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
                    $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                    $where .= "       SEQ           = '013' ";

                    if (0 == $baseD013_cnt) {
                        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
                    } else {
                        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
                    }
                    $db->query($query);
                }
            }

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=014
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "014";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["SIS_DIV"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["SIS_NAME"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["SIS_JH"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["SIS_HR_CLASS"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '014' ";

            if (0 == $baseD014_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=015
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "015";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["MOM_NAME"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["MOM_GRDYEAR"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["MOM_HR_CLASS"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["MOM_BIRTHDAY"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '015' ";

            if (0 == $baseD015_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            if ($model->field["APPLICANTDIV"] == "2") {
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=016
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = "016";
                $data["REMARK1"][TEXT]          = $data_arr[$i]["SH_SCHOOLNAME"];
                $data["REMARK2"][TEXT]          = $data_arr[$i]["SH_JUDGEMENT_DATE"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '016' ";

                if (0 == $baseD016_cnt) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //調査書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv2($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //教科取得
        $class_array = array();
        $result = $db->query(knjl310fQuery::getNameMstAll($model->ObjYear, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[] = $row["NAMECD2"];
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $confRpt_cnt = $db->getOne(knjl310fQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            foreach ($class_array as $class) {
                $data["CONFIDENTIAL_RPT".$class][NUMBER] = $data_arr[$i]["CLASSCD".$class];
            }
            $data["TOTAL3"][NUMBER]             = $data_arr[$i]["TOTAL3"];
            $data["TOTAL5"][NUMBER]             = $data_arr[$i]["TOTAL5"];
            $data["TOTAL_ALL"][NUMBER]          = $data_arr[$i]["TOTAL_ALL"];
            $data["KASANTEN_ALL"][NUMBER]       = $data_arr[$i]["KASANTEN_ALL"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' AND ";
        $query .= "     NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //志望コース
    public function examcoursecdCntSql($entexamyear, $applicantdiv, $coursecd, $majorcd, $examcoursecd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$entexamyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' AND ";
        $query .= "     TESTDIV         = '1' AND ";
        $query .= "     COURSECD        = '{$coursecd}' AND ";
        $query .= "     MAJORCD         = '{$majorcd}' AND ";
        $query .= "     EXAMCOURSECD    = '{$examcoursecd}' ";

        return $query;
    }

    //出身学校コード
    public function fsCdCntSql($fs_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$fs_cd}' ";

        return $query;
    }

    //姉妹中高区分
    public function sisJhCntSql($sis_jh)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = '{$sis_jh}' AND ";
        $query .= "     NAME1 IN ('J','H') ";

        return $query;
    }

    //姉妹クラス
    public function sisHrClassCntSql($sis_hr_class, $sis_jh)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "              ON T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.GRADE    = T1.GRADE ";
        $query .= "             AND T2.SCHOOL_KIND IN ('J','H') ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$sis_hr_class."' ";
        if ($sis_jh) {
            $query .= " AND T2.SCHOOL_KIND = '".$sis_jh."' ";
        }

        return $query;
    }

    /********************/
    /**  更新時に使用  **/
    /********************/

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    public function cntEntexamApplicantaddrDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_MONEY_DAT
    public function cntEntexamMoneyDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MONEY_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function cntEntexamApplicantbaseDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        if ($seq == '010' || $seq == '011' || $seq == '012' || $seq == '013') {
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT ";
        } else {
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ         = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DAT
    public function cntEntexamApplicantconfrptDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //名称マスタより取得
    public function getNameMstAll($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(NAMECD2) ";
        } else {
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //ヘッダ例用データを名称マスタより取得
    public function getHeaderReiNameMst($year, $namecd1, $name, $namecd2 = "", $where = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || {$name} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($where) {
            $query .= " AND ".$where;
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //(姉妹)姉妹中高区分名取得
    public function getSisterJHName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1 IN ('J','H') ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 DESC ";

        return $query;
    }

    //CSVデータ出力（中学・願書）
    public function selectMainQuery1($model, $testdiv_array)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV AS TEST_L_DIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.TESTDIV1 AS GANSHO_YUUSOU, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     BD_001.REMARK8 AS COURSECD, ";
        $query .= "     BD_001.REMARK9 AS MAJORCD, ";
        $query .= "     BD_001.REMARK10 AS EXAMCOURSECD, ";
        $query .= "     M1.EXAM_PAY_DIV, ";
        $query .= "     M1.EXAM_PAY_DATE, ";
        $query .= "     M1.EXAM_PAY_CHAK_DATE, ";
        $query .= "     BD_002.REMARK1 AS RECRUIT_NO, ";
        //010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
        foreach ($testdiv_array as $key => $label) {
            $query .= "     CASE WHEN BD_010.REMARK{$key} = '{$key}' THEN '1' ELSE '' END AS TESTDIV{$key}_FLG, ";
            $query .= "     BD_011.REMARK{$key} AS TESTDIV{$key}_EXAM_TYPE, ";
            if ($model->field["TEST_L_DIV"] == "1") {
                $query .= "     BD_013.REMARK{$key} AS TESTDIV{$key}_TESTSUBCLASSCD, ";
            }
            $query .= "     BD_012.REMARK{$key} AS TESTDIV{$key}_RECEPTNO, ";
        }
        //志願者情報
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        //保護者
        $query .= "     T2.GNAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T2.RELATIONSHIP, ";
        //姉妹
        $query .= "     BD_014.REMARK1 AS SIS_DIV, ";
        $query .= "     BD_014.REMARK2 AS SIS_NAME, ";
        $query .= "     BD_014.REMARK3 AS SIS_JH, ";
        $query .= "     BD_014.REMARK4 AS SIS_HR_CLASS, ";
        //母
        $query .= "     BD_015.REMARK1 AS MOM_NAME, ";
        $query .= "     BD_015.REMARK2 AS MOM_GRDYEAR, ";
        $query .= "     BD_015.REMARK3 AS MOM_HR_CLASS, ";
        $query .= "     BD_015.REMARK4 AS MOM_BIRTHDAY, ";
        //英検取得級
        $query .= "     NL055.NAME1 AS EIKEN_NAME, ";
        //特待生情報
        $query .= "     NL025.NAME1 AS JUDGE_KIND_NAME, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "          ON T1.ENTEXAMYEAR      = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO           = T2.EXAMNO ";
        //志望区分
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_001 ";
        $query .= "          ON BD_001.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_001.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_001.SEQ          = '001' ";
        //事前番号
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_002 ";
        $query .= "          ON BD_002.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_002.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_002.SEQ          = '002' ";
        //入試区分(TESTDIV)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_010 ";
        $query .= "          ON BD_010.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_010.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_010.SEQ          = '010' ";
        //受験型(EXAM_TYPE)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_011 ";
        $query .= "          ON BD_011.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_011.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_011.SEQ          = '011' ";
        //受験番号(RECEPTNO)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_012 ";
        $query .= "          ON BD_012.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_012.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_012.SEQ          = '012' ";
        //帰国生Ｂ方式選択科目(TESTSUBCLASSCD)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_013 ";
        $query .= "          ON BD_013.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_013.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_013.SEQ          = '013' ";
        //姉妹
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_014 ";
        $query .= "          ON BD_014.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_014.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_014.SEQ          = '014' ";
        //母
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_015 ";
        $query .= "          ON BD_015.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_015.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_015.SEQ          = '015' ";
        //受験料
        $query .= "     LEFT JOIN ENTEXAM_MONEY_DAT M1 ";
        $query .= "          ON M1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "         AND M1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "         AND M1.EXAMNO           = T1.EXAMNO ";
        //出身学校
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "          ON T1.FS_CD            = FIN.FINSCHOOLCD ";
        //英検取得級
        $query .= "     LEFT JOIN NAME_MST NL055 ON NL055.NAMECD1 = 'L055' AND NL055.NAMECD2 = BD_014.REMARK9 ";
        //特待生情報
        $query .= "     LEFT JOIN NAME_MST NL025 ";
        $query .= "          ON NL025.NAMECD1 = 'L025'";
        $query .= "         AND NL025.NAMECD2 = T1.JUDGE_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TEST_L_DIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（高校・願書）
    public function selectMainQuery2_1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.TESTDIV1 AS GANSHO_YUUSOU, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     BD_001.REMARK8 AS COURSECD, ";
        $query .= "     BD_001.REMARK9 AS MAJORCD, ";
        $query .= "     BD_001.REMARK10 AS EXAMCOURSECD, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV0, ";
        $query .= "     M1.EXAM_PAY_DIV, ";
        $query .= "     M1.EXAM_PAY_DATE, ";
        $query .= "     M1.EXAM_PAY_CHAK_DATE, ";
        $query .= "     BD_002.REMARK1 AS RECRUIT_NO, ";
        //併願
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.SHIFT_DESIRE_FLG, ";
        $query .= "     BD_016.REMARK1 AS SH_SCHOOLNAME, ";
        $query .= "     BD_016.REMARK2 AS SH_JUDGEMENT_DATE, ";
        $query .= "     T1.SLIDE_FLG, ";
        $query .= "     T1.SELECT_SUBCLASS_DIV, ";
        //志願者情報
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        //保護者
        $query .= "     T2.GNAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T2.RELATIONSHIP, ";
        //姉妹
        $query .= "     BD_014.REMARK1 AS SIS_DIV, ";
        $query .= "     BD_014.REMARK2 AS SIS_NAME, ";
        $query .= "     BD_014.REMARK3 AS SIS_JH, ";
        $query .= "     BD_014.REMARK4 AS SIS_HR_CLASS, ";
        //母
        $query .= "     BD_015.REMARK1 AS MOM_NAME, ";
        $query .= "     BD_015.REMARK2 AS MOM_GRDYEAR, ";
        $query .= "     BD_015.REMARK3 AS MOM_HR_CLASS, ";
        $query .= "     BD_015.REMARK4 AS MOM_BIRTHDAY, ";
        //特待生情報
        $query .= "     NL025.NAME2 AS JUDGE_KIND_NAME, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "          ON T1.ENTEXAMYEAR      = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO           = T2.EXAMNO ";
        //志望区分
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_001 ";
        $query .= "          ON BD_001.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_001.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_001.SEQ          = '001' ";
        //事前番号
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_002 ";
        $query .= "          ON BD_002.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_002.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_002.SEQ          = '002' ";
        //入試区分(TESTDIV)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_010 ";
        $query .= "          ON BD_010.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_010.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_010.SEQ          = '010' ";
        //受験型(EXAM_TYPE)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_011 ";
        $query .= "          ON BD_011.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_011.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_011.SEQ          = '011' ";
        //受験番号(RECEPTNO)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_012 ";
        $query .= "          ON BD_012.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_012.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_012.SEQ          = '012' ";
        //帰国生Ｂ方式選択科目(TESTSUBCLASSCD)
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD_013 ";
        $query .= "          ON BD_013.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_013.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_013.SEQ          = '013' ";
        //姉妹
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_014 ";
        $query .= "          ON BD_014.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_014.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_014.SEQ          = '014' ";
        //母
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_015 ";
        $query .= "          ON BD_015.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_015.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_015.SEQ          = '015' ";
        //併願校名、併願校合格発表日
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD_016 ";
        $query .= "          ON BD_016.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND BD_016.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND BD_016.SEQ          = '016' ";
        //受験料
        $query .= "     LEFT JOIN ENTEXAM_MONEY_DAT M1 ";
        $query .= "          ON M1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "         AND M1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "         AND M1.EXAMNO           = T1.EXAMNO ";
        //出身学校
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "          ON T1.FS_CD            = FIN.FINSCHOOLCD ";
        //特待生情報
        $query .= "     LEFT JOIN NAME_MST NL025 ";
        $query .= "          ON NL025.NAMECD1 = 'L025'";
        $query .= "         AND NL025.NAMECD2 = T1.JUDGE_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（高校・調査書）
    public function selectMainQuery2_2($model, $class_array)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        foreach ($class_array as $key => $label) {
            $query .= "     L1.CONFIDENTIAL_RPT{$key} AS CLASSCD{$key}, ";
        }
        $query .= "     L1.TOTAL3, ";
        $query .= "     L1.TOTAL5, ";
        $query .= "     L1.TOTAL_ALL, ";
        $query .= "     L1.KASANTEN_ALL, ";
        $query .= "     L1.REMARK1, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }
}
