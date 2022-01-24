<?php

require_once('for_php7.php');

class knjl014aquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //入試年度取得
    public function getYear()
    {
        $query  = " WITH T_ALL (VALUE, LABEL) AS ( ";
        $query .= "      VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2="")
    {
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

    //試験回取得
    public function getTestDiv($year, $applicantDiv)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目取得
    public function getMappingMst($year, $applicantDiv, $testDiv)
    {
        $query .= "   SELECT ";
        $query .= "       MAPPING.TESTDIV_NAME, ";
        $query .= "       MAPPING.REMARK1 AS SHDIV, ";
        $query .= "       MAPPING.REMARK2 AS COURSEDIV, ";
        $query .= "       MAPPING.REMARK3 AS EXAM_TYPE ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_MIRAI_MAPPING_MST MAPPING ";
        $query .= "   WHERE ";
        $query .= "       MAPPING.ENTEXAMYEAR       =  '{$year}' ";
        $query .= "       AND MAPPING.APPLICANTDIV  =  '{$applicantDiv}' ";
        $query .= "       AND MAPPING.TESTDIV       =  '{$testDiv}' ";
        $query .= "       AND EXISTS ( ";
        $query .= "           SELECT 'X' FROM V_NAME_MST T1 ";
        $query .= "           WHERE ";
        $query .= "               T1.YEAR         =  MAPPING.ENTEXAMYEAR ";
        $query .= "               AND T1.NAMECD1  =  'L006' ";
        $query .= "               AND T1.NAMECD2  =  MAPPING.REMARK1 ";
        $query .= "       ) ";
        $query .= "       AND EXISTS ( ";
        $query .= "           SELECT 'X' FROM V_NAME_MST T2 ";
        $query .= "           WHERE ";
        $query .= "               T2.YEAR         =  MAPPING.ENTEXAMYEAR ";
        $query .= "               AND T2.NAMECD1  =  (CASE WHEN MAPPING.APPLICANTDIV = '1' THEN 'LJ58' WHEN MAPPING.APPLICANTDIV = '2' THEN 'LH58' ELSE NULL END) ";
        $query .= "               AND T2.NAMECD2  =  MAPPING.REMARK2 ";
        $query .= "       ) ";
        $query .= "       AND EXISTS ( ";
        $query .= "           SELECT 'X' FROM ENTEXAM_EXAMTYPE_MST T3 ";
        $query .= "           WHERE ";
        $query .= "               T3.ENTEXAMYEAR       =  MAPPING.ENTEXAMYEAR ";
        $query .= "               AND T3.APPLICANTDIV  =   MAPPING.APPLICANTDIV ";
        $query .= "               AND T3.EXAM_TYPE     =  MAPPING.REMARK3 ";
        $query .= "       ) ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元号取得
        $result = $db->query(knjl014aQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //変換用出身学校コード格納
        $fscd = array();
        $result = $db->query(knjl014aQuery::getFsCd());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fscd[$row["MIRAI_FS_CD"]] = $row["FS_CD"];
        }
        $result->free();

        //変換用塾コード格納
        $pscd = array();
        $result = $db->query(knjl014aQuery::getPsCd());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $pscd[$row["MIRAI_PS_CD"]] = $row["PS_CD"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /**************/
            /** 部品変換 **/
            /**************/
            //誕生日変換
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth");
            $parts = array("_eracd", "_y", "_m", "_d");
            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    $y = substr($data_arr[$i][$dkey], 0, 4);
                    $m = substr($data_arr[$i][$dkey], 4, 2);
                    $d = substr($data_arr[$i][$dkey], 6, 2);
                    $setBirth = $y."-".$m."-".$d;

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
            // 性別変換
            if ($data_arr[$i]["SEX"] == "男") {
                $setSex = "1";
            } elseif ($data_arr[$i]["SEX"] == "女") {
                $setSex = "2";
            } else {
                $setSex = "";
            }
            // 郵便番号変換
            $setZipCd = str_replace("-", "", $data_arr[$i]["ZIPCD"]);
            $setZipCd = ($data_arr[$i]["ZIPCD"] != "") ? substr($setZipCd, 0, 3)."-".substr($setZipCd, 3, 4) : "";
            $setGZipCd = str_replace("-", "", $data_arr[$i]["GZIPCD"]);
            $setGZipCd = ($data_arr[$i]["GZIPCD"] != "") ? substr($setGZipCd, 0, 3)."-".substr($setGZipCd, 3, 4) : "";
            /**************/

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]          = '1';// 1固定 RECEPTで区別する
            $data["SHDIV"][TEXT]            = '1';// 1固定 RECEPTで区別する
            $data["DESIREDIV"][TEXT]        = '1';// 1固定 RECEPTで区別する
            $data["NAME"][TEXT]             = ($data_arr[$i]["NAME_SEI"] != "") ? $data_arr[$i]["NAME_SEI"]."　".$data_arr[$i]["NAME_MEI"] : "";
            $data["NAME_KANA"][TEXT]        = ($data_arr[$i]["NAME_KANA_SEI"] != "") ? $data_arr[$i]["NAME_KANA_SEI"]."　".$data_arr[$i]["NAME_KANA_MEI"] : "";
            $data["SEX"][TEXT]              = $setSex;
            $data["ERACD"][TEXT]            = $birth_eracd;
            $data["BIRTH_Y"][TEXT]          = $birth_y;
            $data["BIRTH_M"][TEXT]          = $birth_m;
            $data["BIRTH_D"][TEXT]          = $birth_d;
            $data["BIRTHDAY"][TEXT]         = $setBirth;
            $data["FS_CD"][TEXT]            = $fscd[$data_arr[$i]["MIRAI_FS_CD"]];
            $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //where
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            //cnt
            $getCount = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DAT ".$where);

            if (0 == $getCount) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = '008';
            $data["REMARK1"][TEXT]          = $pscd[$data_arr[$i]["MIRAI_PS_CD"]];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //where
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '008' ";
            //cnt
            $getCount = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);

            if (0 == $getCount) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $setZipCd;
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["PREF_NAME"].$data_arr[$i]["CITY_NAME"].$data_arr[$i]["BANCHI_NAME"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GNAME"][TEXT]            = ($data_arr[$i]["GNAME_SEI"] != "") ? $data_arr[$i]["GNAME_SEI"]."　".$data_arr[$i]["GNAME_MEI"] : "";
            $data["GKANA"][TEXT]            = ($data_arr[$i]["GKANA_SEI"] != "") ? $data_arr[$i]["GKANA_SEI"]."　".$data_arr[$i]["GKANA_MEI"] : "";
            /*** 松枝さんに確認する。志願者入力画面にはないので取込まないでいいですか？
                        $data["GZIPCD"][TEXT]           = $setGZipCd;
                        $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GPREF_NAME"].$data_arr[$i]["GCITY_NAME"].$data_arr[$i]["GBANCHI_NAME"];
                        $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            ***/
            $data["EMERGENCYTELNO"][TEXT]   = $data_arr[$i]["EMERGENCYTELNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //where
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
            //cnt
            $getCount = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_APPLICANTADDR_DAT ".$where);

            if (0 == $getCount) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = '1';
            $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //where
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$model->field["TESTDIV"]}' AND ";
            $where .= "       RECEPTNO      = '{$data_arr[$i]["RECEPTNO"]}' ";
            //cnt
            $getCount = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT ".$where);

            if (0 == $getCount) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_RECEPT_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = '1';
            $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
            $data["SEQ"][TEXT]              = '006';
            $data["REMARK1"][TEXT]          = $data_arr[$i]["SHDIV"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["COURSEDIV"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["EXAM_TYPE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //where
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       TESTDIV       = '{$model->field["TESTDIV"]}' AND ";
            $where .= "       RECEPTNO      = '{$data_arr[$i]["RECEPTNO"]}' AND ";
            $where .= "       SEQ           = '006' ";
            //cnt
            $getCount = $db->getOne("SELECT COUNT(*) FROM ENTEXAM_RECEPT_DETAIL_DAT ".$where);

            if (0 == $getCount) {
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function DeleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $tableList1 = array("ENTEXAM_RECEPT_DAT",
                            "ENTEXAM_RECEPT_DETAIL_DAT"
                            );
        $tableList2 = array("ENTEXAM_APPLICANTBASE_DAT",
                            "ENTEXAM_APPLICANTBASE_DETAIL_DAT",
                            "ENTEXAM_APPLICANTADDR_DAT"
                            );

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            foreach ($tableList1 as $table) {
                $query  = " DELETE ";
                $query .= " FROM ";
                $query .= "     {$table} ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
                $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
                $query .= "     TESTDIV         = '{$model->field["TESTDIV"]}' AND ";
                $query .= "     RECEPTNO        = '{$data_arr[$i]["RECEPTNO"]}' ";
                $db->query($query);
            }

            //ENTEXAM_RECEPT_DAT(cnt)
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_RECEPT_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
            $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "     EXAMNO          = '{$data_arr[$i]["EXAMNO"]}' ";
            $recept_cnt = $db->getOne($query);

            if ($recept_cnt == 0) {
                foreach ($tableList2 as $table) {
                    $query  = " DELETE ";
                    $query .= " FROM ";
                    $query .= "     {$table} ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
                    $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
                    $query .= "     EXAMNO          = '{$data_arr[$i]["EXAMNO"]}' ";
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  複数チェック用  **/
    /**********************/

    //件数取得 -- ENTEXAM_RECEPT_DAT
    //一つの志願者SEQが一つの試験に対して複数にならないように取り込みの段階でエラー処理する
    public function cntEntexamReceptDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$model->field["TESTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     RECEPTNO       != '{$data_arr["RECEPTNO"]}' ";

        return $query;
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
        $query .= "     NAMECD2 ";

        return $query;
    }

    //住所の最大文字サイズを取得
    public function getAddressMaxSize()
    {
        $query   = " SELECT ";
        $query  .= " MAX(CASE WHEN COLUMN_NAME = 'ADDRESS1' THEN CHARACTER_MAXIMUM_LENGTH ELSE NULL END) AS ADDRESS1_MAX_SIZE, ";
        $query  .= " MAX(CASE WHEN COLUMN_NAME = 'ADDRESS2' THEN CHARACTER_MAXIMUM_LENGTH ELSE NULL END) AS ADDRESS2_MAX_SIZE ";
        $query  .= " FROM ";
        $query  .= "   SYSIBM.COLUMNS ";
        $query  .= " WHERE ";
        $query  .= "   TABLE_NAME = 'ENTEXAM_APPLICANTADDR_DAT' ";
        $query  .= "   AND COLUMN_NAME IN ('ADDRESS1', 'ADDRESS2') ";
        $query  .= " GROUP BY ";
        $query  .= "   TABLE_NAME ";

        return $query;
    }
    /********************/
    /**  データ変換用  **/
    /********************/

    //賢者の出身学校コード取得
    public function getFsCd()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_FS_REP_DAT ";

        return $query;
    }

    //賢者の塾コード取得
    public function getPsCd()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_PS_REP_DAT ";

        return $query;
    }
}
