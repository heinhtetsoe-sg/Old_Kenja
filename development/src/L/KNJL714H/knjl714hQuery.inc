<?php
class knjl714hQuery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
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
    public function getTestDiv($year, $applicantDiv, $exceptTestDiv = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        if ($exceptTestDiv != "") {
            $query .= "     AND TESTDIV <> '{$exceptTestDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試日から入試種別を取得
    public function getTestDivFromDate($year, $applicantDiv, $testDate)
    {
        $query  = "     SELECT ";
        $query .= "         TESTDIV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_TESTDIV_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$year}' ";
        $query .= "         AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "         AND TEST_DATE = '{$testDate}' ";

        return $query;
    }

    //入試種別取得
    public function getKindDiv($year, $applicantDiv)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     KINDDIV || ':' || KINDDIV_NAME AS LABEL, ";
        $query .= "     KINDDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KINDDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //資格マスタ取得
    public function getQualifiedMst($year, $applicantDiv)
    {
        $query  = "     SELECT ";
        $query .= "       * ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_QUALIFIED_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";

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

        $cnt = 0;
        for ($i = 0; $i < count($data_arr); $i++) {
            if ($data_arr[$i]["SETTLE_MONEY"] == "") {
                continue;
            } //決算金額がnullの生徒は、受験番号は振らない

            //前処理
            knjl714hQuery::preprocess($db, $model, $data_arr[$i]);

            $concurrentFlg = false;

            //行単位更新
            $examno1  = is_numeric($data_arr[$i]["EXAMNO1"]) ? $data_arr[$i]["EXAMNO1"] : "";  //※受験番号の欄が"未発番"となってる場合を飛ばすために数値かを確認している
            if ($model->field["APPLICANTDIV"] == "1") {
                $examno2  = is_numeric($data_arr[$i]["EXAMNO2"]) ? $data_arr[$i]["EXAMNO2"] : "";
                $data_arr[$i]["CONCURRENT_APP_FLG"] = ($examno1 && $examno2) ? "1" : "";
            }

            if ($examno1) {
                knjl714hQuery::updateRow($db, $model, $examno1, $model->field["TESTDIV1"], $data_arr[$i]);
            }
            if ($model->field["APPLICANTDIV"] == "1") {
                if ($examno2) {
                    knjl714hQuery::updateRow($db, $model, $examno2, $model->field["TESTDIV2"], $data_arr[$i]);
                }
            }
            $cnt++;
        }

        if ($model->field["APPLICANTDIV"] == "1") {
            //行またぎの更新 (同時出願、複数出願処理)
            knjl714hQuery::updateMultiRow($db, $model);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //行単位更新
    public function updateRow($db, $model, $examno, $testDiv, &$data_arr_i)
    {
        
        //更新 ENTEXAM_APPLICANTBASE_DAT
        knjl714hQuery::getUpdateApplicantBaseQuery($db, $model, $examno, $testDiv, $data_arr_i);
    
        //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005
        knjl714hQuery::getUpdateApplicantBaseDeital005Query($db, $model, $examno, $data_arr_i);
    
        //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ009
        knjl714hQuery::getUpdateApplicantBaseDeital009Query($db, $model, $examno, $data_arr_i);

        if ($model->field["APPLICANTDIV"] == "2" &&  $model->field["TESTDIV1"] == 1) {
            //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ031
            knjl714hQuery::getUpdateApplicantBaseDeital031Query($db, $model, $examno, $data_arr_i);
        }

        //更新 ENTEXAM_APPLICANTADDR_DAT
        knjl714hQuery::getUpdateApplicantAddrQuery($db, $model, $examno, $data_arr_i);

        //更新 ENTEXAM_RECEPT_DAT
        knjl714hQuery::updateEntexamRecept($db, $model, $examno, $testDiv, $data_arr_i);
    }

    //行またぎ更新
    public function updateMultiRow($db, $model)
    {
        $query .= "     WITH UPD_DATA AS ( ";
        $query .= "         SELECT ";
        $query .= "             BASE.ENTEXAMYEAR, ";
        $query .= "             BASE.APPLICANTDIV, ";
        $query .= "             BD009.REMARK1 AS LOGIN_USER_SEQ, ";
        $query .= "             CASE WHEN COUNT(DISTINCT BASE.TESTDIV) >= 2 THEN '1' ELSE NULL END AS MULTI_APP_FLG "; //複数出願フラグ
        $query .= "         FROM ";
        $query .= "             ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "                  ON BD005.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "                 AND BD005.APPLICANTDIV  = BASE.APPLICANTDIV ";
        $query .= "                 AND BD005.EXAMNO        = BASE.EXAMNO ";
        $query .= "                 AND BD005.SEQ           = '005' ";
        $query .= "             INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD009 ";
        $query .= "                  ON BD009.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "                 AND BD009.APPLICANTDIV  = BASE.APPLICANTDIV ";
        $query .= "                 AND BD009.EXAMNO        = BASE.EXAMNO ";
        $query .= "                 AND BD009.SEQ           = '009' ";
        $query .= "         WHERE ";
        $query .= "             BASE.ENTEXAMYEAR        = '{$model->ObjYear}' ";
        $query .= "             AND BASE.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "             AND BD005.REMARK2 IS NULL ";     //同時出願フラグがNULL
        $query .= "             AND BD009.REMARK1 IS NOT NULL "; //ログインユーザSEQがNULLでない
        $query .= "         GROUP BY ";
        $query .= "             BASE.ENTEXAMYEAR, ";
        $query .= "             BASE.APPLICANTDIV, ";
        $query .= "             BD009.REMARK1 ";
        $query .= "     ) ";
        $query .= "     SELECT ";
        $query .= "         BD009.EXAMNO, ";
        $query .= "         UPD.MULTI_APP_FLG ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DETAIL_DAT BD009 ";
        $query .= "         INNER JOIN UPD_DATA UPD ";
        $query .= "             ON UPD.ENTEXAMYEAR      = BD009.ENTEXAMYEAR ";
        $query .= "             AND UPD.APPLICANTDIV    = BD009.APPLICANTDIV ";
        $query .= "             AND UPD.LOGIN_USER_SEQ  = BD009.REMARK1 ";
        $query .= "     WHERE ";
        $query .= "         BD009.SEQ = '009' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["SEQ"][TEXT]              = "005";
            $data["EXAMNO"][TEXT]           = $row["EXAMNO"];
            $data["REMARK3"][TEXT]          = $row["MULTI_APP_FLG"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$row["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '005' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            
            $db->query($query);
        }
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //行単位更新
        $cnt = 0;
        for ($i = 0; $i < count($data_arr); $i++) {
            //削除
            $examno1 = $data_arr[$i]["EXAMNO1"];
            if ($examno1) {
                knjl714hQuery::deleteRow($db, $model, $model->field["TESTDIV1"], $examno1);
            }

            if ($model->field["APPLICANTDIV"] == "1") {
                $examno2 = $data_arr[$i]["EXAMNO2"];
                if ($examno2) {
                    knjl714hQuery::deleteRow($db, $model, $model->field["TESTDIV2"], $examno2);
                }
            }
            $cnt++;
        }

        //行またぎの更新 (※削除によって出願情報が変更されるので、同時・複数出願フラグの振り直しを行なう)
        knjl714hQuery::updateMultiRow($db, $model);

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //行単位削除
    public function deleteRow($db, $model, $testDiv, $examno)
    {
        //削除 ENTEXAM_RECEPT_DAT
        knjl714hQuery::deleteEntexamRecept($db, $model, $testDiv, $examno);

        //削除 ENTEXAM_APPLICANTBASE_DAT
        knjl714hQuery::deleteEntexamApplicantBase($db, $model, $testDiv, $examno);

        //削除 ENTEXAM_APPLICANTBASEDETAIL_DAT SEQ005
        knjl714hQuery::deleteEntexamApplicantBaseDetail($db, $model, $examno, "005");

        //削除 ENTEXAM_APPLICANTBASEDETAIL_DAT SEQ009
        knjl714hQuery::deleteEntexamApplicantBaseDetail($db, $model, $examno, "009");

        if ($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV1"] == 1) {
            //削除 ENTEXAM_APPLICANTBASEDETAIL_DAT SEQ031
            knjl714hQuery::deleteEntexamApplicantBaseDetail($db, $model, $examno, "031");
        }
        //ENTEXAM_APPLICANTADDR_DAT
        knjl714hQuery::deleteEntexamApplicantAddr($db, $model, $examno);
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

    /********************/
    /**  更新時に使用  **/
    /********************/

    public function preprocess($db, $model, &$data_arr_i)
    {

        //年齢超過フラグ作成
        $baseDate = (CTRL_YEAR + 1)."0401"; //基準日(年度末)
        $age = floor(($baseDate - $data_arr_i["BIRTHDAY"]) / 10000);
        $thresholdAge = ($model->field["APPLICANTDIV"] == "1") ? 13 : 16;
        $data_arr_i["OVER_AGE_FLG"] = ($age >= $thresholdAge) ? "1" : "";

        //元号取得
        $result = $db->query(knjl714hQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //日月変換
        //分解する日付一覧
        $data_arr_i["GRADDAY"] = sprintf("%04d%02d%02d", $model->ObjYear, "03", "01");
        $date_array = array("BIRTHDAY" => "birth", "GRADDAY" => "grd");
        $parts = array("_eracd", "_y", "_m", "_d");
        foreach ($date_array as $dkey => $dval) {
            if (strlen($data_arr_i[$dkey])) {
                //日付を分解
                $y = substr($data_arr_i[$dkey], 0, 4);
                $m = substr($data_arr_i[$dkey], 4, 2);
                $d = substr($data_arr_i[$dkey], 6, 2);
                $formattedDate = $y."-".$m."-".$d;

                //部品の変数名
                list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                $$e_id = $$y_id = $$m_id = $$d_id = "";
                for ($ere_no=0; $ere_no < count($eracd); $ere_no++) {
                    if ($defsdate[$ere_no] <= $formattedDate && $formattedDate <= $defedate[$ere_no]) {
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
        if ($data_arr_i["BIRTHDAY"] != "") {
            $data_arr_i["ERACD"]        = $birth_eracd;
            $data_arr_i["BIRTH_Y"]      = $birth_y;
            $data_arr_i["BIRTH_M"]      = $birth_m;
            $data_arr_i["BIRTH_D"]      = $birth_d;
            $y = substr($data_arr_i["BIRTHDAY"], 0, 4);
            $m = substr($data_arr_i["BIRTHDAY"], 4, 2);
            $d = substr($data_arr_i["BIRTHDAY"], 6, 2);
            $data_arr_i["BIRTHDAY"]     = $y."-".$m."-".$d;
        }
        if ($data_arr_i["GRADDAY"] != "") {
            $data_arr_i["FS_ERACD"]     = $grd_eracd;
            $data_arr_i["FS_Y"]         = $grd_y;
            $data_arr_i["FS_M"]         = $grd_m;
            $data_arr_i["FS_DAY"]       = sprintf("%04d-%02d-%02d", $model->ObjYear, "03", "01");
            $data_arr_i["FS_GRDYEAR"]   = $model->ObjYear;
        }

        //変換用出身学校コード格納
        if ($data_arr_i["MIRAI_FS_CD"] != "") {
            $fscd = array();
            $query = knjl714hQuery::getFsCd();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $fscd[$row["MIRAI_FS_CD"]] = $row["FS_CD"];
            }
            $data_arr_i["FS_CD"] = $fscd[$data_arr_i["MIRAI_FS_CD"]];
        }

        //性別データ
        if ($data_arr_i["SEX"] != "") {
            $query = knjl714hQuery::getNameMst($model->ObjYear, "Z002");
            $result = $db->query($query);
            $sexArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sexArray[$row["NAME2"]] = $row["VALUE"];
            }
            $data_arr_i["SEX"] = $sexArray[$data_arr_i["SEX"]];
            $result->free();
        }
       
        // 郵便番号変換
        if ($data_arr_i["ZIPCD"] != "") {
            $setZipCd = str_replace("-", "", $data_arr_i["ZIPCD"]);
            $setZipCd = substr($setZipCd, 0, 3)."-".substr($setZipCd, 3, 4);
            $data_arr_i["ZIPCD"] = $setZipCd;
        }
        /**************/

        //志願者氏名
        if ($data_arr_i["NAME_SEI"] != "" && $data_arr_i["NAME_MEI"] != "") {
            $data_arr_i["NAME"] = $data_arr_i["NAME_SEI"]."　".$data_arr_i["NAME_MEI"];
        }
        //志願者氏名カナ
        if ($data_arr_i["NAME_KANA_SEI"] != "" && $data_arr_i["NAME_KANA_MEI"] != "") {
            $data_arr_i["NAME_KANA"] = $data_arr_i["NAME_KANA_SEI"]."　".$data_arr_i["NAME_KANA_MEI"];
        }
       
        //保護者氏名
        if ($data_arr_i["GNAME_SEI"] != "" && $data_arr_i["GNAME_MEI"] != "") {
            $data_arr_i["GNAME"] = $data_arr_i["GNAME_SEI"]."　".$data_arr_i["GNAME_MEI"];
        }
        //保護者氏名カナ
        if ($data_arr_i["GKANA_SEI"] != "" && $data_arr_i["GKANA_MEI"] != "") {
            $data_arr_i["GKANA"] = $data_arr_i["GKANA_SEI"]."　".$data_arr_i["GKANA_MEI"];
        }
        //住所1
        $data_arr_i["ADDRESS1"] = $data_arr_i["PREF_NAME"].$data_arr_i["CITY_NAME"].$data_arr_i["BANCHI_NAME"];

        //資格関連処理
        if ($model->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV1"] == 1) {
            $plusTotal = 0;
            $query = knjl714hQuery::getQualifiedMst($model->ObjYear, $model->field["APPLICANTDIV"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $targetPrefix = "";
                if ($row["QUALIFIED_CD"] == "01") {
                    $targetPrefix = "QUALIFIED_ENG";
                } elseif ($row["QUALIFIED_CD"] == "02") {
                    $targetPrefix = "QUALIFIED_KANJI";
                } elseif ($row["QUALIFIED_CD"] == "03") {
                    $targetPrefix = "QUALIFIED_MATH";
                } elseif ($row["QUALIFIED_CD"] == "04") {
                    $targetPrefix = "QUALIFIED_READING";
                } elseif ($row["QUALIFIED_CD"] == "05") {
                    $targetPrefix = "ATHLETIC_PRIZE";
                } elseif ($row["QUALIFIED_CD"] == "06") {
                    $targetPrefix = "CULTURE_PRIZE";
                } elseif ($row["QUALIFIED_CD"] == "07") {
                    $targetPrefix = "SCH_COUNCIL_PRIZE";
                }

                //名称が一致した場合、資格判定CDおよび加算点の取得
                if ($row["QUALIFIED_NAME"] == $data_arr_i["{$targetPrefix}_NAME"]) {
                    $data_arr_i["{$targetPrefix}_CD"] = $row["QUALIFIED_JUDGE_CD"];
                    $plusTotal += $row["PLUS_POINT"];
                }
            }
            $data_arr_i["PLUS_POINT_TOTAL"] = $plusTotal;
        }
    }

    public function getUpdateApplicantBaseQuery($db, $model, $examno, $testDiv, $data_arr_i)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $examno;
        $data["TESTDIV"][TEXT]          = $testDiv;
        $data["SHDIV"][TEXT]            = '1';
        $data["DESIREDIV"][TEXT]        = '1';
        $data["NAME"][TEXT]             = $data_arr_i["NAME"];
        $data["NAME_KANA"][TEXT]        = $data_arr_i["NAME_KANA"];
        $data["SEX"][TEXT]              = $data_arr_i["SEX"];
        $data["ERACD"][TEXT]            = $data_arr_i["ERACD"];
        $data["BIRTH_Y"][TEXT]          = $data_arr_i["BIRTH_Y"];
        $data["BIRTH_M"][TEXT]          = $data_arr_i["BIRTH_M"];
        $data["BIRTH_D"][TEXT]          = $data_arr_i["BIRTH_D"];
        $data["BIRTHDAY"][DATE]         = $data_arr_i["BIRTHDAY"];
        $data["FS_CD"][TEXT]            = $data_arr_i["FS_CD"];
        $data["FS_GRDYEAR"][TEXT]       = $data_arr_i["FS_GRDYEAR"];
        $data["FS_ERACD"][TEXT]         = $data_arr_i["FS_ERACD"];
        $data["FS_Y"][TEXT]             = $data_arr_i["FS_Y"];
        $data["FS_M"][TEXT]             = $data_arr_i["FS_M"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       TESTDIV       = '{$testDiv}' AND ";
        $where .= "       EXAMNO        = '{$examno}' ";

        $base_cnt = $db->getOne(knjl714hQuery::cntEntexamApplicantbaseDat($model, $examno));
        if (0 == $base_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        }

        $db->query($query);
    }

    public function getUpdateApplicantBaseDeital005Query($db, $model, $examno, $data_arr_i)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $seq = "005";
       
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["SEQ"][TEXT]              = $seq;
        $data["EXAMNO"][TEXT]           = $examno;
        $data["REMARK1"][TEXT]          = $model->field["KINDDIV"];
        if ($model->field["APPLICANTDIV"] == "1") {
            $data["REMARK2"][TEXT]          = $data_arr_i["CONCURRENT_APP_FLG"];
        }
        $data["REMARK4"][TEXT]          = $data_arr_i["OVER_AGE_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$examno}' AND ";
        $where .= "       SEQ           = '{$seq}' ";

        $bd005_cnt = $db->getOne(knjl714hQuery::cntEntexamApplicantbaseDetailDat($model, $examno, $seq));
        if (0 == $bd005_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }

        $db->query($query);
    }

    public function getUpdateApplicantBaseDeital009Query($db, $model, $examno, $data_arr_i)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $seq = "009";

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["SEQ"][TEXT]              = $seq;
        $data["EXAMNO"][TEXT]           = $examno;
        $data["REMARK1"][TEXT]          = $data_arr_i["LOGIN_USER_SEQ"];
        $data["REMARK2"][TEXT]          = $data_arr_i["MIRAI_FS_CD"];
        $data["REMARK3"][TEXT]          = $data_arr_i["MIRAI_FS_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$examno}' AND ";
        $where .= "       SEQ           = '{$seq}' ";

        $bd009_cnt = $db->getOne(knjl714hQuery::cntEntexamApplicantbaseDetailDat($model, $examno, $seq));
        if (0 == $bd009_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
    
        $db->query($query);
    }

    public function getUpdateApplicantBaseDeital031Query($db, $model, $examno, $data_arr_i)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $seq = "031";
        
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["SEQ"][TEXT]              = $seq;
        $data["EXAMNO"][TEXT]           = $examno;
        $data["REMARK1"][TEXT]          = $data_arr_i["QUALIFIED_ENG_CD"];
        $data["REMARK2"][TEXT]          = $data_arr_i["QUALIFIED_KANJI_CD"];
        $data["REMARK3"][TEXT]          = $data_arr_i["QUALIFIED_MATH_CD"];
        $data["REMARK4"][TEXT]          = $data_arr_i["QUALIFIED_READING_CD"];
        $data["REMARK5"][TEXT]          = $data_arr_i["ATHLETIC_PRIZE_CD"];
        $data["REMARK6"][TEXT]          = $data_arr_i["CULTURE_PRIZE_CD"];
        $data["REMARK7"][TEXT]          = $data_arr_i["SCH_COUNCIL_PRIZE_CD"];
        $data["REMARK10"][TEXT]         = $data_arr_i["PLUS_POINT_TOTAL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$examno}' AND ";
        $where .= "       SEQ           = '{$seq}' ";

        $bd009_cnt = $db->getOne(knjl714hQuery::cntEntexamApplicantbaseDetailDat($model, $examno, $seq));
        if (0 == $bd009_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }

        $db->query($query);
    }

    public function getUpdateApplicantAddrQuery($db, $model, $examno, $data_arr_i)
    {
        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $examno;
        $data["ZIPCD"][TEXT]            = $data_arr_i["ZIPCD"];
        $data["ADDRESS1"][TEXT]         = $data_arr_i["ADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $data_arr_i["ADDRESS2"];
        $data["TELNO"][TEXT]            = $data_arr_i["TELNO"];
        $data["GNAME"][TEXT]            = $data_arr_i["GNAME"];
        $data["GKANA"][TEXT]            = $data_arr_i["GKANA"];
        $data["GZIPCD"][TEXT]           = $data_arr_i["ZIPCD"];     //志願者と同じデータを入れる
        $data["GADDRESS1"][TEXT]        = $data_arr_i["ADDRESS1"];  //志願者と同じデータを入れる
        $data["GADDRESS2"][TEXT]        = $data_arr_i["ADDRESS2"];  //志願者と同じデータを入れる
        $data["GTELNO"][TEXT]           = $data_arr_i["GTELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$examno}' ";

        $addr_cnt = $db->getOne(knjl714hQuery::cntEntexamApplicantAddrDat($model, $examno));
        if (0 == $addr_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
        }

        $db->query($query);
    }

    public function updateEntexamRecept($db, $model, $examno, $testDiv, $data_arr_i)
    {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $testDiv;
        $data["EXAM_TYPE"][TEXT]        = $model->field["EXAM_TYPE"];
        $data["RECEPTNO"][TEXT]         = $examno;// RECEPTNOとEXAMNOは同じ番号
        $data["EXAMNO"][TEXT]           = $examno;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
        $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       TESTDIV       = '{$testDiv}' AND ";
        $where .= "       EXAM_TYPE     = '{$model->field["EXAM_TYPE"]}' AND ";
        $where .= "       RECEPTNO      = '{$examno}' ";
       
        $recept_cnt = $db->getOne(knjl714hQuery::cntEntexamReceptDat($model, $testDiv, $examno));
        if (0 == $recept_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        }

        $db->query($query);
    }

    /********************/
    /**  削除時に使用  **/
    /********************/

    //削除 ENTEXAM_RECEPT_DAT
    public function deleteEntexamRecept($db, $model, $testDiv, $examno)
    {
        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV       = '{$testDiv}' ";
        $query .= "   AND EXAM_TYPE     = '{$model->field["EXAM_TYPE"]}' ";
        $query .= "   AND RECEPTNO      = '{$examno}' ";// RECEPTNOとEXAMNOは同じ番号
       
        $db->query($query);
    }

    //削除 ENTEXAM_APPLICANTBASE_DAT
    public function deleteEntexamApplicantBase($db, $model, $testDiv, $examno)
    {
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV       = '{$testDiv}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";

        $db->query($query);
    }
   
    //削除 ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function deleteEntexamApplicantBaseDetail($db, $model, $examno, $seq)
    {
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";
        $query .= "   AND SEQ           = '{$seq}' ";

        $db->query($query);
    }

    //削除 ENTEXAM_APPLICANTADDR_DAT
    public function deleteEntexamApplicantAddr($db, $model, $examno)
    {
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";

        $db->query($query);
    }

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

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseDat($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$examno}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function cntEntexamApplicantbaseDetailDat($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$examno}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    public function cntEntexamApplicantAddrDat($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$examno}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_RECEPT_DAT
    public function cntEntexamReceptDat($model, $testDiv, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     TESTDIV         = '{$testDiv}' AND ";
        $query .= "     EXAM_TYPE       = '{$model->field["EXAM_TYPE"]}' AND ";
        $query .= "     RECEPTNO        = '{$examno}' ";

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
}
