<?php

require_once('for_php7.php');

class knjl514jQuery extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
    function getTestDiv($year, $applicantDiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試方式取得
    function getExamType($year, $applicantDiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL, ";
        $query .= "     EXAM_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //願書の追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //データ件数取得
            $mirai_cnt      = $db->getOne(knjl514jQuery::cntEntexamMiraicompassDat($model, $data_arr[$i]));

            //ミライコンパス保持用　ENTEXAM_MIRAI_TAMASEI_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]              = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPNO"][TEXT]                    = $data_arr[$i]["APPNO"];
            $data["LOGIN_USER_SEQ"][TEXT]           = $data_arr[$i]["LOGIN_USER_SEQ"];
            $data["LOGIN_MAIL"][TEXT]               = $data_arr[$i]["LOGIN_MAIL"];
            $data["SHIGANSYA_SEQ"][TEXT]            = $data_arr[$i]["SHIGANSYA_SEQ"];
            $data["MIRAI_TESTDIV"][TEXT]            = $data_arr[$i]["MIRAI_TESTDIV"];
            $data["NAME_SEI"][TEXT]                 = $data_arr[$i]["NAME_SEI"];
            $data["NAME_MEI"][TEXT]                 = $data_arr[$i]["NAME_MEI"];
            $data["NAME_KANA_SEI"][TEXT]            = $data_arr[$i]["NAME_KANA_SEI"];
            $data["NAME_KANA_MEI"][TEXT]            = $data_arr[$i]["NAME_KANA_MEI"];
            $data["BIRTHDAY"][TEXT]                 = $data_arr[$i]["BIRTHDAY"];
            $data["ZIPCD"][TEXT]                    = $data_arr[$i]["ZIPCD"];
            $data["PREF_NAME"][TEXT]                = $data_arr[$i]["PREF_NAME"];
            $data["CITY_NAME"][TEXT]                = $data_arr[$i]["CITY_NAME"];
            $data["BANCHI_NAME"][TEXT]              = $data_arr[$i]["BANCHI_NAME"];
            $data["ADDRESS2"][TEXT]                 = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]                    = $data_arr[$i]["TELNO"];
            $data["MIRAI_FS_CD"][TEXT]              = $data_arr[$i]["MIRAI_FS_CD"];
            $data["MIRAI_FS_NAME"][TEXT]            = $data_arr[$i]["MIRAI_FS_NAME"];
            $data["MIRAI_FS_NAME_OTHER"][TEXT]      = $data_arr[$i]["MIRAI_FS_NAME_OTHER"];
            $data["MIRAI_SH_CD1"][TEXT]             = $data_arr[$i]["MIRAI_SH_CD1"];
            $data["MIRAI_SH_NAME1"][TEXT]           = $data_arr[$i]["MIRAI_SH_NAME1"];
            $data["MIRAI_SH_NAME_OTHER1"][TEXT]     = $data_arr[$i]["MIRAI_SH_NAME_OTHER1"];
            $data["MIRAI_SH_CD2"][TEXT]             = $data_arr[$i]["MIRAI_SH_CD2"];
            $data["MIRAI_SH_NAME2"][TEXT]           = $data_arr[$i]["MIRAI_SH_NAME2"];
            $data["MIRAI_SH_NAME_OTHER2"][TEXT]     = $data_arr[$i]["MIRAI_SH_NAME_OTHER2"];
            $data["MIRAI_SH_CD3"][TEXT]             = $data_arr[$i]["MIRAI_SH_CD3"];
            $data["MIRAI_SH_NAME3"][TEXT]           = $data_arr[$i]["MIRAI_SH_NAME3"];
            $data["MIRAI_SH_NAME_OTHER3"][TEXT]     = $data_arr[$i]["MIRAI_SH_NAME_OTHER3"];
            $data["MIRAI_PS_CD"][TEXT]              = $data_arr[$i]["MIRAI_PS_CD"];
            $data["MIRAI_PS_NAME"][TEXT]            = $data_arr[$i]["MIRAI_PS_NAME"];
            $data["MIRAI_PS_NAME_OTHER"][TEXT]      = $data_arr[$i]["MIRAI_PS_NAME_OTHER"];
            $data["GNAME_SEI"][TEXT]                = $data_arr[$i]["GNAME_SEI"];
            $data["GNAME_MEI"][TEXT]                = $data_arr[$i]["GNAME_MEI"];
            $data["GKANA_SEI"][TEXT]                = $data_arr[$i]["GKANA_SEI"];
            $data["GKANA_MEI"][TEXT]                = $data_arr[$i]["GKANA_MEI"];
            $data["GTELNO"][TEXT]                   = $data_arr[$i]["GTELNO"];
            $data["EXPLAIN"][TEXT]                  = $data_arr[$i]["EXPLAIN"];
            $data["APP_INFO_DIV"][TEXT]             = $data_arr[$i]["APP_INFO_DIV"];
            $data["SRV_RCPT_DIV"][TEXT]             = $data_arr[$i]["SRV_RCPT_DIV"];
            $data["COUNTER_RCPTNO"][TEXT]           = $data_arr[$i]["COUNTER_RCPTNO"];
            $data["REMARKS"][TEXT]                  = $data_arr[$i]["REMARKS"];
            $data["SETTLE_MONEY"][TEXT]             = $data_arr[$i]["SETTLE_MONEY"];
            $data["EXAM_MONEY"][TEXT]               = $data_arr[$i]["EXAM_MONEY"];
            $data["FEES"][TEXT]                     = $data_arr[$i]["FEES"];
            $data["PAY_TYPE_CD"][TEXT]              = $data_arr[$i]["PAY_TYPE_CD"];
            $data["PAY_DUE_DATE"][TEXT]             = $data_arr[$i]["PAY_DUE_DATE"];
            $data["PAY_DAY"][TEXT]                  = $data_arr[$i]["PAY_DAY"];
            $data["PAY_INFO_DIV"][TEXT]             = $data_arr[$i]["PAY_INFO_DIV"];
            $data["APP_COMP_DAY"][TEXT]             = $data_arr[$i]["APP_COMP_DAY"];
            $data["CANCEL_FLG"][TEXT]               = $data_arr[$i]["CANCEL_FLG"];
            $data["CANCEL_DATE"][TEXT]              = $data_arr[$i]["CANCEL_DATE"];
            $data["TEST_NO"][TEXT]                  = $data_arr[$i]["TEST_NO"];//add
            $data["TEST_DATE"][TEXT]                = $data_arr[$i]["TEST_DATE"];
            $data["TEST_NAME"][TEXT]                = $data_arr[$i]["TEST_NAME"];
            $data["TEST_NAME_ABBV"][TEXT]           = $data_arr[$i]["TEST_NAME_ABBV"];
            $data["EXAMNO"][TEXT]                   = $data_arr[$i]["EXAMNO"];
            $data["EXAMNO_MANUAL"][TEXT]            = $data_arr[$i]["EXAMNO_MANUAL"];
            $data["STATUS_CD"][TEXT]                = $data_arr[$i]["STATUS_CD"];//add
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $model->field["EXAM_TYPE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$model->field["TESTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}'";

            if (0 == $mirai_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_MIRAI_TAMASEI_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MIRAI_TAMASEI_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }

        for ($i = 0; $i < get_count($data_arr); $i++) {
            // mirai → 賢者
            knjl514jQuery::getUpdateMiraiToKenja($db, $model, $data_arr[$i]);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function deleteQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ENTEXAM_MIRAI_TAMASEI_DAT
            $query  = " DELETE FROM ENTEXAM_MIRAI_TAMASEI_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "   AND TESTDIV       = '{$model->field["TESTDIV"]}' ";
            $query .= "   AND EXAM_TYPE     = '{$model->field["EXAM_TYPE"]}' ";// keyではないので不要？
            $query .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "   AND TESTDIV       = '{$model->field["TESTDIV"]}' ";
            $query .= "   AND EXAM_TYPE     = '{$model->field["EXAM_TYPE"]}' ";
            $query .= "   AND RECEPTNO      = '{$data_arr[$i]["EXAMNO"]}' ";// RECEPTNOとEXAMNOは同じ番号
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DAT
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $query .= "   AND EXAMNO NOT IN ( ";
            $query .= "         SELECT EXAMNO FROM ENTEXAM_RECEPT_DAT ";
            $query .= "         WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "           AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "           AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $query .= "         GROUP BY EXAMNO ";
            $query .= "   ) ";
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $query .= "   AND EXAMNO NOT IN ( ";
            $query .= "         SELECT EXAMNO FROM ENTEXAM_RECEPT_DAT ";
            $query .= "         WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "           AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "           AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            $query .= "         GROUP BY EXAMNO ";
            $query .= "   ) ";
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
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    /********************/
    /**  更新時に使用  **/
    /********************/
    //件数取得 -- ENTEXAM_MIRAI_TAMASEI_DAT
    function cntEntexamMiraicompassDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_TAMASEI_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV= '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV     = '{$model->field["TESTDIV"]}' AND ";
        $query .= "     EXAMNO      = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //UPDATE(mirai → 賢者)
    function &getUpdateMiraiToKenja($db, $model, $data_arr) {

        //元号取得
        $result = $db->query(knjl514jQuery::getNameMstAll($model->ObjYear, "L007"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //変換用出身学校コード格納
        $fscd = array();
        $query = knjl514jQuery::getFsCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fscd[$row["MIRAI_FS_CD"]] = $row["FS_CD"];
        }

        $query  = knjl514jQuery::getMiraiTamaseiDatAll($model, $data_arr);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            /**************/
            /** 部品変換 **/
            /**************/
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
            // 郵便番号変換
            $setZipCd = str_replace("-", "", $row["ZIPCD"]);
            $setZipCd = substr($setZipCd, 0, 3)."-".substr($setZipCd, 3, 4);
            /**************/

            //データ件数取得
            $base_cnt       = $db->getOne(knjl514jQuery::cntEntexamApplicantbaseDat($model, $row));
            $addr_cnt       = $db->getOne(knjl514jQuery::cntEntexamApplicantaddrDat($model, $row));
            $recept_cnt     = $db->getOne(knjl514jQuery::cntEntexamReceptDat($model, $row));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["TESTDIV"][TEXT]          = '1';// 1固定 RECEPTで区別する
            $data["SHDIV"][TEXT]            = '1';
            $data["DESIREDIV"][TEXT]        = '1';
            $data["NAME"][TEXT]             = $row["NAME_SEI"]."　".$row["NAME_MEI"];
            $data["NAME_KANA"][TEXT]        = $row["NAME_KANA_SEI"]."　".$row["NAME_KANA_MEI"];
            $data["SEX"][TEXT]              = '2';// 2固定 女子校のため
            $data["ERACD"][TEXT]            = $birth_eracd;
            $data["BIRTH_Y"][TEXT]          = $birth_y;
            $data["BIRTH_M"][TEXT]          = $birth_m;
            $data["BIRTH_D"][TEXT]          = $birth_d;
            $data["BIRTHDAY"][DATE]         = $setBirth;
            $data["FS_CD"][TEXT]            = $fscd[$row["MIRAI_FS_CD"]];
            $data["RECOM_EXAMNO"][TEXT]     = $row["SHIGANSYA_SEQ"];// 志願者SEQ
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
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
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $setZipCd;
            $data["ADDRESS1"][TEXT]         = $row["PREF_NAME"].$row["CITY_NAME"].$row["BANCHI_NAME"];
            $data["ADDRESS2"][TEXT]         = $row["ADDRESS2"];
            $data["TELNO"][TEXT]            = $row["TELNO"];
            $data["GNAME"][TEXT]            = $row["GNAME_SEI"]."　".$row["GNAME_MEI"];
            $data["GKANA"][TEXT]            = $row["GKANA_SEI"]."　".$row["GKANA_MEI"];
            $data["GTELNO"][TEXT]           = $row["GTELNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
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
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $row["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $row["EXAM_TYPE"];
            $data["RECEPTNO"][TEXT]         = $row["EXAMNO"];// RECEPTNOとEXAMNOは同じ番号
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$row["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$row["TESTDIV"]}' AND ";
            $where .= "       EXAM_TYPE     = '{$row["EXAM_TYPE"]}' AND ";
            $where .= "       RECEPTNO      = '{$row["EXAMNO"]}' ";

            if (0 == $recept_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            }
            $db->query($query);
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

    //ENTEXAM_MIRAI_TAMASEI_DAT情報取得
    function getMiraiTamaseiDatAll($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_TAMASEI_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
        $query .= "     AND EXAMNO       = '{$data_arr["EXAMNO"]}' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    function cntEntexamApplicantbaseDat($model, $row) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$row["APPLICANTDIV"]}' AND ";
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
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$row["APPLICANTDIV"]}' AND ";
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
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$row["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$row["TESTDIV"]}' AND ";
        $query .= "     EXAM_TYPE       = '{$row["EXAM_TYPE"]}' AND ";
        $query .= "     RECEPTNO        = '{$row["EXAMNO"]}' ";

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
}
?>
