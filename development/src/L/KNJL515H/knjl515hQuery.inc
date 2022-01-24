<?php

require_once('for_php7.php');

class knjl515hQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " WITH T_ALL (VALUE, LABEL) AS ( ";
        $query .= "      VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS VALUE, ";
        $query .= "     ENTEXAMYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($model, $namecd1, $nameCd2="") {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //リスト取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     DIST.ENTEXAMYEAR, ";
        $query .= "     DIST.APPLICANTDIV, ";
        $query .= "     DIST.DISTINCT_ID, ";
        $query .= "     DIST.DISTINCT_ID || ':' || DIST.DISTINCT_NAME AS DISTINCT_NAME, ";
        $query .= "     DIST.TESTDIV, ";
        $query .= "     DIST.TESTDIV || ':' || TEST.TESTDIV_NAME AS TESTDIV_NAME, ";
        $query .= "     DIST.EXAM_TYPE, ";
        $query .= "     DIST.EXAM_TYPE || ':' || TYPE.EXAMTYPE_NAME AS EXAMTYPE_NAME, ";
        $query .= "     DIST.TEST_DATE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST DIST ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TEST ON DIST.ENTEXAMYEAR  = TEST.ENTEXAMYEAR ";
        $query .= "                                       AND DIST.APPLICANTDIV = TEST.APPLICANTDIV ";
        $query .= "                                       AND DIST.TESTDIV      = TEST.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_MST TYPE ON DIST.ENTEXAMYEAR  = TYPE.ENTEXAMYEAR ";
        $query .= "                                        AND DIST.APPLICANTDIV = TYPE.APPLICANTDIV ";
        $query .= "                                        AND DIST.EXAM_TYPE    = TYPE.EXAM_TYPE ";
        $query .= " WHERE ";
        $query .= "         DIST.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND DIST.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     DIST.DISTINCT_ID ";

        return $query;
    }

    /**********/
    /**右画面**/
    /**********/
    //１レコード取得
    function getRow($year, $applicantdiv, $distinctId) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND DISTINCT_ID  = '".$distinctId."' ";

        return $query;
    }

    //入試種別取得
    function getEntexamTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試方式取得
    function getEntexamExamtypeMst($model) {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE AS VALUE, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験日
    function getTestDate($model) {
        $testDiv  = ($model->cmd == "sendLink") ? $model->testDiv:  $model->field["TESTDIV"];
        $examType = ($model->cmd == "sendLink") ? $model->examType: $model->field["EXAM_TYPE"];

        $query  = " SELECT ";
        $query .= "     TEST_DATE AS VALUE, ";
        $query .= "     TEST_DATE AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MEIKEI_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$testDiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$examType."' ";
        $query .= " GROUP BY ";
        $query .= "     TEST_DATE ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト取得
    function getMiraiMeikeiDat($model) {
        $testDiv  = ($model->cmd == "sendLink") ? $model->testDiv : $model->field["TESTDIV"];
        $examType = ($model->cmd == "sendLink") ? $model->examType: $model->field["EXAM_TYPE"];
        $testDate = ($model->cmd == "sendLink") ? $model->testDate: $model->field["TEST_DATE"];
        $testDate = str_replace("-", "/", $testDate);

        $query  = " SELECT ";
        $query .= "     DISTINCT_ID, ";
        $query .= "     TEST_CD AS VALUE, ";
        $query .= "     TEST_NAME_ABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MEIKEI_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$testDiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$examType."' ";
        $query .= "     AND TEST_DATE    = '".$testDate."' ";
        $query .= " GROUP BY ";
        $query .= "     DISTINCT_ID, ";
        $query .= "     TEST_CD, ";
        $query .= "     TEST_NAME_ABBV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**************/
    /* 更新前確認 */
    /**************/
    function getEntexamScoreDatCnt($model) {
        $setTestCdIn = "'".str_replace(",", "','", $model->selectRightdata)."'";

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT SCRE ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RCPT ON SCRE.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                      AND SCRE.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                      AND SCRE.TESTDIV      = RCPT.TESTDIV ";
        $query .= "                                      AND SCRE.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "                                      AND SCRE.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "         SCRE.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND SCRE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND SCRE.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND SCRE.EXAM_TYPE    = '{$model->field["EXAM_TYPE"]}' ";
        $query .= "     AND RCPT.DISTINCT_ID  = '{$model->field["DISTINCT_ID"]}' ";
        if ($model->cmd == "update") {
            $query .= "     AND RCPT.RECEPTNO IN ( ";
            $query .= "                           SELECT ";
            $query .= "                               REPLACE(REPLACE(TEST_DATE, '-', ''), '/', '') || EXAMNO ";
            $query .= "                           FROM ";
            $query .= "                               ENTEXAM_MIRAI_MEIKEI_DAT ";
            $query .= "                           WHERE ";
            $query .= "                                   ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "                               AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $query .= "                               AND TESTDIV      = '{$model->field["TESTDIV"]}' ";
            $query .= "                               AND EXAM_TYPE    = '{$model->field["EXAM_TYPE"]}' ";
            $query .= "                               AND TEST_DATE    = '{$model->field["TEST_DATE"]}' ";
            $query .= "                               AND TEST_CD     IN ({$setTestCdIn}) ";
            $query .= "                           ) ";
        }

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["DISTINCT_ID"][TEXT]      = $fields["DISTINCT_ID"];
        $data["DISTINCT_NAME"][TEXT]    = $fields["DISTINCT_NAME"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = $fields["EXAM_TYPE"];
        $data["TEST_DATE"][DATE]        = str_replace("/", "-", $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_DISTINCTION_MST");
        $db->query($query);

        // ENTEXAM_MIRAI_MEIKEI_DAT
        knjl515hQuery::getUpdateEntexamMiraiMeikeiDat($db, $model, $fields, "Insert");

        // mirai → 賢者
        knjl515hQuery::getUpdateMiraiToKenja($db, $model, $fields);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["DISTINCT_NAME"][TEXT]    = $fields["DISTINCT_NAME"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = $fields["EXAM_TYPE"];
        $data["TEST_DATE"][DATE]        = str_replace("/", "-", $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '{$fields["ENTEXAMYEAR"]}' ";
        $where .= "   AND APPLICANTDIV      = '{$fields["APPLICANTDIV"]}' ";
        $where .= "   AND DISTINCT_ID       = '{$fields["DISTINCT_ID"]}' ";
        $query = Query::updateSQL($data, "ENTEXAM_DISTINCTION_MST", $where);
        $db->query($query);

        // ENTEXAM_MIRAI_MEIKEI_DAT
        knjl515hQuery::getUpdateEntexamMiraiMeikeiDat($db, $model, $fields, "Update");

        // mirai → 賢者
        knjl515hQuery::getUpdateMiraiToKenja($db, $model, $fields);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_DISTINCTION_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '{$fields["ENTEXAMYEAR"]}' ";
        $query .= "         AND APPLICANTDIV    = '{$fields["APPLICANTDIV"]}' ";
        $query .= "         AND DISTINCT_ID     = '{$fields["DISTINCT_ID"]}' ";
        $db->query($query);

        // ENTEXAM_RECEPT_DAT
        $queryR = knjl515hQuery::getMiraiMeikeiDatAll($model);
        $result = $db->query($queryR);
        while ($recept = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '{$recept["ENTEXAMYEAR"]}' ";
            $query .= "         AND APPLICANTDIV    = '{$recept["APPLICANTDIV"]}' ";
            $query .= "         AND TESTDIV         = '{$recept["TESTDIV"]}' ";
            $query .= "         AND EXAM_TYPE       = '{$recept["EXAM_TYPE"]}' ";
            $query .= "         AND RECEPTNO        = '".str_replace(array("/", "-"), "", $recept["TEST_DATE"]).$recept["EXAMNO"]."' ";
            $query .= "         AND EXAMNO          = '{$recept["EXAMNO"]}' ";
            $db->query($query);
        }

        // ENTEXAM_MIRAI_MEIKEI_DAT
        knjl515hQuery::getUpdateEntexamMiraiMeikeiDat($db, $model, $fields, "Delete");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $result;
    }

    //UPDATE(ENTEXAM_MIRAI_MEIKEI_DAT)
    function &getUpdateEntexamMiraiMeikeiDat($db, $model, $fields, $kind) {
        // 更新
        if ($kind == "Update") {
            $setTestCdArr = array();
            $setTestCdArr = explode(",", $model->selectdata);

            $query  = knjl515hQuery::getTestCdMiraiMeikeiDat($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // 左リストに無いものは入試判別IDをクリアする。
                if (!in_array($row["TEST_CD"], $setTestCdArr)) {
                    $data = array();
                    $data["DISTINCT_ID"][TEXT]      = "";
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $where  = " WHERE ENTEXAMYEAR       = '{$fields["ENTEXAMYEAR"]}'";
                    $where .= "   AND APPLICANTDIV      = '{$fields["APPLICANTDIV"]}'";
                    $where .= "   AND TESTDIV           = '{$fields["TESTDIV"]}' ";
                    $where .= "   AND TEST_DATE         = '{$fields["TEST_DATE"]}' ";
                    $where .= "   AND TEST_CD           = '{$row["TEST_CD"]}' ";

                    $query = Query::updateSQL($data, "ENTEXAM_MIRAI_MEIKEI_DAT", $where);
                    $db->query($query);
                }
            }
        }

        // 追加、更新
        if ($kind != "Delete") {
            $setTestCdIn = "'".str_replace(",", "','", $model->selectdata)."'";

            $data = array();
            $data["DISTINCT_ID"][TEXT]      = $fields["DISTINCT_ID"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR       = '{$fields["ENTEXAMYEAR"]}' ";
            $where .= "   AND APPLICANTDIV      = '{$fields["APPLICANTDIV"]}' ";
            $where .= "   AND TESTDIV           = '{$fields["TESTDIV"]}' ";
            $where .= "   AND TEST_DATE         = '{$fields["TEST_DATE"]}' ";
            $where .= "   AND TEST_CD          IN ({$setTestCdIn}) ";

            $query = Query::updateSQL($data, "ENTEXAM_MIRAI_MEIKEI_DAT", $where);
            $db->query($query);
        }

        //削除
        if ($kind == "Delete") {
            $data = array();
            $data["DISTINCT_ID"][TEXT]      = "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR       = '{$fields["ENTEXAMYEAR"]}' ";
            $where .= "   AND APPLICANTDIV      = '{$fields["APPLICANTDIV"]}' ";
            $where .= "   AND TESTDIV           = '{$fields["TESTDIV"]}' ";
            $where .= "   AND TEST_DATE         = '{$fields["TEST_DATE"]}' ";
            $where .= "   AND DISTINCT_ID       = '{$fields["DISTINCT_ID"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_MIRAI_MEIKEI_DAT", $where);
            $db->query($query);
        }
        return;
    }

    //テストCD取得
    function getTestCdMiraiMeikeiDat($model) {
        $query  = " SELECT ";
        $query .= "     TEST_CD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MEIKEI_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND TEST_DATE    = '".$model->field["TEST_DATE"]."' ";
        $query .= "     AND DISTINCT_ID  = '".$model->field["DISTINCT_ID"]."' ";
        $query .= " GROUP BY ";
        $query .= "     TEST_CD ";

        return $query;
    }

    //UPDATE(mirai → 賢者)
    function &getUpdateMiraiToKenja($db, $model, $fields) {

        //元号取得
        $result = $db->query(knjl515hQuery::getNameMstAll($model->year, "L007"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //続柄コード格納
        $rShipCd = array();
        $query = knjl515hQuery::getNameMstAll($model->year, "H201");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rShipCd[$row["NAME1"]] = $row["NAMECD2"];
        }

        //変換用出身学校コード格納
        $fscd = array();
        $query = knjl515hQuery::getFsCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fscd[$row["MIRAI_FS_CD"]] = $row["FS_CD"];
        }

        //変換用塾コード格納
        $pscd = array();
        $query = knjl515hQuery::getPsCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $pscd[$row["MIRAI_PS_CD"]] = $row["PS_CD"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        $query  = knjl515hQuery::getMiraiMeikeiDatAll($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            /**************/
            /** 部品変換 **/
            /**************/
            //氏名かな
            $setNameKana = ($row["NAME_KANA_SEI"] != "") ? $row["NAME_KANA_SEI"]."　".$row["NAME_KANA_MEI"]: "";
            //誕生日変換
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth");
            $parts = array("_eracd", "_y", "_m", "_d");
            foreach ($date_array as $dkey => $dval) {
                if (strlen($row[$dkey])) {
                    //日付を分解
                    $y = substr($row[$dkey], 0, 4);
                    $m = substr($row[$dkey], 4, 2);
                    $d = substr($row[$dkey], 6, 2);
                    $setBirth = $y."-".$m."-".$d;

                    //部品の変数名
                    list ($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                    $$e_id = $$y_id = $$m_id = $$d_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $row[$dkey] && $row[$dkey] <= $defedate[$ere_no]) {
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
            // 性別変換
            if ($row["SEX"] == "男") {
                $sex = "1";
            } else if ($row["SEX"] == "女") {
                $sex = "2";
            } else {
                $sex = "";
            }
            //卒業年月
            list($fsY, $fsM) = explode("年", $row["FS_GRDYEAR"]);
            $fsY = substr(mb_convert_kana($fsY, 'n'), 0, 4);
            $fsY = !is_numeric($fsY) ? "": $fsY;
            $fsM = substr(mb_convert_kana($fsM, 'n'), 0, 1);
            $fsM = !is_numeric($fsM) ? "": $fsM;
            // 入寮希望
            $setDomi = substr($row["DORMITORY_FLG"], 0, 1);
            if ($setDomi == "" || $setDomi == "1") {
                $domiFlg = "";
            } else {
                $domiFlg = "1";
            }
            // 郵便番号変換
            $setZipCd = str_replace("-", "", $row["ZIPCD"]);
            $setZipCd = substr($setZipCd, 0, 3)."-".substr($setZipCd, 3, 4);
            /**************/

            //データ件数取得
            $base_cnt       = $db->getOne(knjl515hQuery::cntEntexamApplicantbaseDat($model, $row));
            $addr_cnt       = $db->getOne(knjl515hQuery::cntEntexamApplicantaddrDat($model, $row));
            $recept_cnt     = $db->getOne(knjl515hQuery::cntEntexamReceptDat($model, $row));
            $receptD007_cnt = $db->getOne(knjl515hQuery::cntEntexamReceptDetailDat($model, $row, "007"));
            $receptD008_cnt = $db->getOne(knjl515hQuery::cntEntexamReceptDetailDat($model, $row, "008"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["TESTDIV"][TEXT]          = '1';// 1固定 RECEPTで区別する
            $data["SHDIV"][TEXT]            = '1';
            $data["DESIREDIV"][TEXT]        = '1';
            $data["NAME"][TEXT]             = $row["NAME_SEI"]."　".$row["NAME_MEI"];
            $data["NAME_KANA"][TEXT]        = $setNameKana;
            $data["SEX"][TEXT]              = $sex;
            $data["ERACD"][TEXT]            = $birth_eracd;
            $data["BIRTH_Y"][TEXT]          = $birth_y;
            $data["BIRTH_M"][TEXT]          = $birth_m;
            $data["BIRTH_D"][TEXT]          = $birth_d;
            $data["BIRTHDAY"][DATE]         = $setBirth;
            $data["FS_CD"][TEXT]            = $fscd[$row["MIRAI_FS_CD"]];
            $data["FS_GRDYEAR"][TEXT]       = $fsY;
            $data["FS_M"][TEXT]             = $fsM;
            $data["PRISCHOOLCD"][TEXT]      = $pscd[$row["MIRAI_PS_CD"]];
            $data["DORMITORY_FLG"][TEXT]    = $domiFlg;
            $data["REMARK1"][TEXT]          = $row["GRADE"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' AND ";
            $where .= "       APPLICANTDIV  = '{$row["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$row["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $setZipCd;
            $data["ADDRESS1"][TEXT]         = $row["PREF_NAME"].$row["CITY_NAME"].$row["BANCHI_NAME"];
            $data["ADDRESS2"][TEXT]         = $row["ADDRESS2"];
            $data["TELNO"][TEXT]            = $row["TELNO"];
            $data["GNAME"][TEXT]            = $row["GNAME_SEI"]."　".$row["GNAME_MEI"];
            $data["GKANA"][TEXT]            = $row["GKANA_SEI"]."　".$row["GKANA_MEI"];
            $data["GTELNO"][TEXT]           = $row["GTELNO"];
            $data["RELATIONSHIP"][TEXT]     = $rShipCd[$row["RELATIONSHIP"]];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' AND";
            $where .= "       APPLICANTDIV  = '{$row["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$row["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $row["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $row["EXAM_TYPE"];
            $data["RECEPTNO"][TEXT]         = str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"];
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["DISTINCT_ID"][TEXT]      = $fields["DISTINCT_ID"];
            $data["TEST_NAME_ABBV"][TEXT]   = $row["TEST_NAME_ABBV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' AND ";
            $where .= "       APPLICANTDIV  = '{$row["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$row["TESTDIV"]}' AND ";
            $where .= "       EXAM_TYPE     = '{$row["EXAM_TYPE"]}' AND ";
            $where .= "       RECEPTNO      = '".str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"]."' AND ";//試験日+受験番号
            $where .= "       EXAMNO        = '{$row["EXAMNO"]}' ";

            if (0 == $recept_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DETAIL_DAT(007)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $row["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $row["EXAM_TYPE"];
            $data["RECEPTNO"][TEXT]         = str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"];
            $data["SEQ"][TEXT]              = "007";
            $data["REMARK1"][TEXT]          = $row["SHIGANSYA_SEQ"];        // 志願者SEQ
            $data["REMARK2"][TEXT]          = $row["MIRAI_TESTDIV"];        // 入試区分（miraicomas）
            $data["REMARK3"][TEXT]          = $row["NOT_PC_KANJI_NAME"];    // パソコン等で表示できない氏名の漢字
            $data["REMARK4"][TEXT]          = $row["FS_GRDYEAR"];           // 出身校卒業年月
            $data["REMARK5"][TEXT]          = $row["DORMITORY_FLG"];        // 入寮希望
            $data["REMARK6"][TEXT]          = $row["RELATIONSHIP_OTHER"];   // 保護者の本人との続柄（その他）
            $data["REMARK7"][TEXT]          = $row["TEST_CD"];              // 試験番号
            $data["REMARK8"][TEXT]          = $row["TEST_NAME_ABBV"];       // 試験名略称
            $data["REMARK9"][TEXT]          = $row["SH_FLG"];               // 普通コースとの併願希望の有無
            $data["REMARK10"][TEXT]         = $row["MIRAI_FS_ADDR"];        // 出身校所在地
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' AND ";
            $where .= "       APPLICANTDIV  = '{$row["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$row["TESTDIV"]}' AND ";
            $where .= "       EXAM_TYPE     = '{$row["EXAM_TYPE"]}' AND ";
            $where .= "       RECEPTNO      = '".str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"]."' AND ";//頭2桁+受験番号
            $where .= "       SEQ           = '007' ";

            if (0 == $receptD007_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DETAIL_DAT(008)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $row["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $row["EXAM_TYPE"];
            $data["RECEPTNO"][TEXT]         = str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"];
            $data["SEQ"][TEXT]              = "008";
            $data["REMARK1"][TEXT]          = $row["MIRAI_FS_CD"];          // 出身校コード（miraicomas）
            $data["REMARK2"][TEXT]          = $row["MIRAI_FS_NAME"];        // 出身校名称（miraicomas）
            $data["REMARK3"][TEXT]          = $row["MIRAI_PS_CD"];          // 塾コード（miraicomas）
            $data["REMARK4"][TEXT]          = $row["MIRAI_PS_NAME"];        // 塾名称（miraicomas）
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' AND ";
            $where .= "       APPLICANTDIV  = '{$row["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$row["TESTDIV"]}' AND ";
            $where .= "       EXAM_TYPE     = '{$row["EXAM_TYPE"]}' AND ";
            $where .= "       RECEPTNO      = '".str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"]."' AND ";//頭2桁+受験番号
            $where .= "       SEQ           = '008' ";

            if (0 == $receptD008_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_FAMILY_DAT
            $query  = " DELETE FROM ENTEXAM_FAMILY_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '{$model->year}' ";
            $query .= "         AND APPLICANTDIV    = '{$row["APPLICANTDIV"]}' ";
            $query .= "         AND EXAMNO          = '{$row["EXAMNO"]}' ";
            $db->query($query);

            for ($i=1; $i <= 5; $i++) {
                if ($row["RELANAME".$i] == "") {
                    continue;
                }

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
                $data["RELANO"][TEXT]           = sprintf("%02d", $i);
                $data["RELANAME"][TEXT]         = $row["RELANAME".$i];
                $data["RELATIONSHIP"][TEXT]     = $rShipCd[$row["RELATIONSHIP".$i]];
                $data["RELA_AGE"][TEXT]         = mb_convert_kana($row["RELA_AGE".$i], 'n');
                $data["WORKPLACE"][TEXT]        = $row["RELA_JOB".$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_FAMILY_DAT");
                $db->query($query);
            }
        }

        return;
    }

    //名称マスタより取得
    function getNameMstAll($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //ENTEXAM_MIRAI_MEIKEI_DAT情報取得
    function getMiraiMeikeiDatAll($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MEIKEI_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "     AND TEST_DATE    = '".$model->field["TEST_DATE"]."' ";
        $query .= "     AND DISTINCT_ID  = '".$model->field["DISTINCT_ID"]."' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    function cntEntexamApplicantbaseDat($model, $row) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     EXAMNO          = '{$row["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    function cntEntexamApplicantaddrDat($model, $row) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     EXAMNO          = '{$row["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_RECEPT_DAT
    function cntEntexamReceptDat($model, $row) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$row["TESTDIV"]}' AND ";
        $query .= "     EXAM_TYPE       = '{$row["EXAM_TYPE"]}' AND ";
        $query .= "     RECEPTNO        = '".str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"]."' AND ";
        $query .= "     EXAMNO          = '{$row["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_RECEPT_DETAIL_DAT
    function cntEntexamReceptDetailDat($model, $row, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '{$row["TESTDIV"]}' AND ";
        $query .= "     EXAM_TYPE       = '{$row["EXAM_TYPE"]}' AND ";
        $query .= "     RECEPTNO        = '".str_replace(array("/", "-"), "", $row["TEST_DATE"]).$row["EXAMNO"]."' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    /********************/
    /**  データ変換用  **/
    /********************/

    //賢者の出身学校コード取得
    function getFsCd() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_FS_REP_DAT ";

        return $query;
    }

    //賢者の塾コード取得
    function getPsCd() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_PS_REP_DAT ";

        return $query;
    }
}
?>
