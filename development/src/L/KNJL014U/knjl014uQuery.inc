<?php

require_once('for_php7.php');

class knjl014uquery extends Query {
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
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //全件クリア（その回以降）
    function delAllData($db, $model, $testdiv) {
        $tableList1 = array("ENTEXAM_APPLICANTBASE_DAT",
                            "ENTEXAM_HALL_YDAT",
                            "ENTEXAM_JUDGE_AVARAGE_DAT",
                            "ENTEXAM_JUDGE_TMP",
                            "ENTEXAM_PASSINGMARK_MST",
                            "ENTEXAM_RECEPT_DAT",
                            "ENTEXAM_SCORE_DAT"
                            );
        $tableList2 = array("ENTEXAM_APPLICANTADDR_DAT",
                            "ENTEXAM_APPLICANTBASE_DETAIL_DAT",
                            "ENTEXAM_APPLICANTCONFRPT_DAT",
                            "ENTEXAM_MIRAICOMPASS_DAT",
                            );
        foreach ($tableList1 as $table) {
            $query  = " DELETE FROM ";
            $query .= "     {$table} ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
            $query .= "     AND TESTDIV >= '{$testdiv}' ";
            $db->query($query);
        }
        foreach ($tableList2 as $table) {
            $query  = " DELETE FROM ";
            $query .= "     {$table} ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
            $query .= "     AND EXAMNO >= '{$testdiv}0000' ";
            $db->query($query);
        }
    }

    //願書の追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr, &$data_arr_mirai) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //全件クリア（その回以降）
        if (0 < get_count($data_arr)) knjl014uQuery::delAllData($db, $model, $data_arr[0]["TESTDIV"]);

        //元号取得
        $result = $db->query(knjl014uQuery::getNameMstAll($model->ObjYear, "L007"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list ($y, $m, $d) = preg_split("/-/", $data_arr[$i][$dkey]);
                    //部品の変数名
                    list ($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

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
            $base_cnt       = $db->getOne(knjl014uQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl014uQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $mirai_cnt      = $db->getOne(knjl014uQuery::cntEntexamMiraicompassDat($model, $data_arr_mirai[$i]));
            $recept_cnt     = $db->getOne(knjl014uQuery::cntEntexamReceptDat($model, $data_arr[$i]));
            //データ件数取得　前回以前の合格者か？
            $judge_cnt      = 0;
            if (2 <= $data_arr[$i]["TESTDIV"]) {//２回目以降
                $judge_cnt  = $db->getOne(knjl014uQuery::cntJudge($model, $data_arr[$i]));
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]            = '1';
            $data["DESIREDIV"][TEXT]        = '1';
            $data["NAME"][TEXT]             = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]              = '1';
            $data["ERACD"][TEXT]            = $birth_eracd;
            $data["BIRTH_Y"][TEXT]          = $birth_y;
            $data["BIRTH_M"][TEXT]          = $birth_m;
            $data["BIRTH_D"][TEXT]          = $birth_d;
            $data["BIRTHDAY"][TEXT]         = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]            = $data_arr[$i]["MIRAI_FS_CD"];
            $data["RECOM_EXAMNO"][TEXT]     = $data_arr[$i]["RECOM_EXAMNO"];
            if (0 < $judge_cnt) {
                $data["JUDGEMENT"][TEXT]    = '4'; //「5:受験不可」を「4:欠席」に変更（要望：受験不可としないでほしい）（高倉：欠席としておく）
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
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
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
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
            $data["EMERGENCYTELNO"][TEXT]   = $data_arr[$i]["EMERGENCYTELNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ミライコンパス保持用　ENTEXAM_MIRAICOMPASS_DAT
            $data = array();
            foreach ($model->miraiTableInfo as $field) {
                $setValue = $data_arr_mirai[$i][$field["COLNAME"]];
                if (strlen($setValue) > $field["LENGTH"]) {
                    $setValue = mb_strcut($setValue,0,$field["LENGTH"]);
                }
                $data[$field["COLNAME"]][TEXT] = $setValue;
            }
            $data["TORIKOMI_DATE"][FUNC]    = "DATE(SYSDATE())";
            $data["TORIKOMI_TIME"][FUNC]    = "TIME(SYSDATE())";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
//            $where .= "       APPLICANTDIV  = '{$data_arr_mirai[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr_mirai[$i]["EXAMNO"]}'";

            if (0 == $mirai_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_MIRAICOMPASS_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_MIRAICOMPASS_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = '1';
            $data["RECEPTNO"][TEXT]         = $data_arr[$i]["EXAMNO"];//RECEPTNOとEXAMNOは同じ番号
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            if (0 < $judge_cnt) {
                $data["JUDGEDIV"][TEXT]     = '4'; //「5:受験不可」を「4:欠席」に変更（要望：受験不可としないでほしい）（高倉：欠席としておく）
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$data_arr[$i]["TESTDIV"]}' AND ";
            $where .= "       RECEPTNO      = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $recept_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //ミライコンパスCSVテーブル情報取得
    function getMiraiTableInfo($miraiDataCnt) {
        $query  = " SELECT * from (SELECT ";
        $query .= "     COLNAME, ";
        $query .= "     COLNO, ";
        $query .= "     LENGTH ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME='ENTEXAM_MIRAICOMPASS_DAT') t1 ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        //ミライコンパスCSVテーブル情報取得(全56項目)
        $db = Query::dbCheckOut();
        $rtnArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["COLNO"] >= $miraiDataCnt) break;
            $rtnArray[] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        return $rtnArray;
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

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    function nameCntSql($namecd, $setcd, $where="") {
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

    /********************/
    /**  更新時に使用  **/
    /********************/

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    function cntEntexamApplicantbaseDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    function cntEntexamApplicantaddrDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    function cntEntexamApplicantbaseDetailDat($model, $data_arr, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_MIRAICOMPASS_DAT
    function cntEntexamMiraicompassDat($model, $data_arr_mirai) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAICOMPASS_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
//        $query .= "     APPLICANTDIV    = '{$data_arr_mirai["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr_mirai["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_RECEPT_DAT
    function cntEntexamReceptDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$data_arr["TESTDIV"]}' AND ";
        $query .= "     RECEPTNO        = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- 前回以前の合格者か？
    //合格者は次回以降は受験不可のため、２回目以降のデータに受験不可フラグで更新する。
    //前回以前の合格者の志願者SEQが同じ志願者を検索する。
    //通常は合否シミュレーションで行う。
    function cntJudge($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     T1.RECOM_EXAMNO = '{$data_arr["RECOM_EXAMNO"]}' AND ";
        $query .= "     T1.TESTDIV      < '{$data_arr["TESTDIV"]}' AND ";
        $query .= "     N1.NAMESPARE1   = '1' ";//合格者

        return $query;
    }

    //志望コース取得　不要？　今のところ未使用
    function getExamcourse($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$data_arr["TESTDIV"]}' ";
        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

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
}
?>
