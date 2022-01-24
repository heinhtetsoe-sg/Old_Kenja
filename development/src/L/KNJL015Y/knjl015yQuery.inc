<?php
class knjl015yQuery extends Query
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

    //志望区分
    public function getDesirediv($year, $applicantDiv, $testDiv, $wishno, $desireDiv = "")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     L1.EXAMCOURSE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "          ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testDiv}' ";
        if (0 < strlen($desireDiv)) {
            $query .= " AND T1.DESIREDIV    = '{$desireDiv}' ";
        }
        $query .= "     AND T1.WISHNO       = '{$wishno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.DESIREDIV ";
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
            //決算金額がnullの生徒は、スルー
            if ($data_arr[$i]["SETTLE_MONEY"] == "") {
                continue;
            }

            //前処理
            knjl015yQuery::preprocess($db, $model, $data_arr[$i]);

            //行単位更新
            knjl015yQuery::updateRow($db, $model, $data_arr[$i]["EXAMNO"], $model->field["TESTDIV"], $data_arr[$i]);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //行単位更新
    public function updateRow($db, $model, $examno, $testDiv, &$data_arr_i)
    {
        
        //更新 ENTEXAM_APPLICANTBASE_DAT
        knjl015yQuery::getUpdateApplicantBaseQuery($db, $model, $examno, $testDiv, $data_arr_i);
    
        //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005
        knjl015yQuery::getUpdateApplicantBaseDeital005Query($db, $model, $examno, $data_arr_i);
    
        //更新 ENTEXAM_APPLICANTADDR_DAT
        knjl015yQuery::getUpdateApplicantAddrQuery($db, $model, $examno, $data_arr_i);
    
        //更新 ENTEXAM_APPLICANTDESIRE_DAT
        knjl015yQuery::getUpdateEntexamDesireDiv($db, $model, $examno, $testDiv, $data_arr_i);
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
                knjl015yQuery::deleteRow($db, $model, $examno1);
            }

            if ($model->field["APPLICANTDIV"] == "1") {
                $examno2 = $data_arr[$i]["EXAMNO2"];
                if ($examno2) {
                    knjl015yQuery::deleteRow($db, $model, $examno2);
                }
            }
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //行単位削除
    public function deleteRow($db, $model, $examno)
    {
        //削除 ENTEXAM_APPLICANTBASE_DAT
        knjl015yQuery::deleteEntexamApplicantBase($db, $model, $testDiv, $examno);

        //削除 ENTEXAM_APPLICANTBASEDETAIL_DAT SEQ005
        knjl015yQuery::deleteEntexamApplicantBaseDetail($db, $model, $examno, "005");

        //ENTEXAM_APPLICANTADDR_DAT
        knjl015yQuery::deleteEntexamApplicantAddr($db, $model, $examno);

        //ENTEXAM_APPLICANTADDR_DAT
        knjl015yQuery::deleteEntexamApplicantDesire($db, $model, $examno);
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
        //卒業区分
        //ＣSVの「在学中（2021年3月卒業見込み)」 or 「卒業（2020年3月）」という文字から取得する
        $data_arr_i["FS_GRDYEAR"] = "";
        $data_arr_i["FS_GRDDIV"] = "";
        if (preg_match("/（([0-9]{4})年([1-9]|1[0-2])月((?<=月).*(?=）))）/", $data_arr_i["MIRAI_GRD_DIV"], $matches)) {
            $data_arr_i["FS_GRDYEAR"] = $matches[1];
            $data_arr_i["GRADDAY"] = sprintf("%04d%02d%02d", $matches[1], $matches[2], "01");
            $data_arr_i["FS_GRDDIV"] = ($matches[3] == "卒業見込み") ? "1": "2";
        }

        //元号取得
        $result = $db->query(knjl015yQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //日月変換
        //分解する日付一覧
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
            $y = substr($data_arr_i["GRADDAY"], 0, 4);
            $m = substr($data_arr_i["GRADDAY"], 4, 2);
            $d = substr($data_arr_i["GRADDAY"], 6, 2);
            $data_arr_i["FS_DAY"]       = $y."-".$m."-".$d;
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

        //備考1
        $data_arr_i["REMARK1"] = knjl015yQuery::getByteLimitStr($data_arr_i["REMARK1"], 246);
        //備考2
        $data_arr_i["REMARK2"] = knjl015yQuery::getByteLimitStr($data_arr_i["REMARK2"], 122);

        //出身校名称
        $data_arr_i["FS_CD"] = $data_arr_i["MIRAI_FS_CD"];
        $query = knjl015yQuery::getFsName($data_arr_i["FS_CD"]);
        $data_arr_i["FS_NAME"] = $db->getOne($query);
    }

    public function getByteLimitStr($targetStr, $limitByte)
    {
        if ($limitByte < 0) {
            return "";
        }
        while (strlen($targetStr) > $limitByte) {
            $last = substr($targetStr, strlen($targetStr) - 1);
            $mb_last = mb_substr($targetStr, mb_strlen($targetStr) - 1);
    
            $spritByte = ($last == $mb_last) ? 1 : 3;
            $targetStr = substr($targetStr, 0, strlen($targetStr) - $spritByte);
        }
        return $targetStr;
    }

    public function getFsName($fsCd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$fsCd}' ";

        return $query;
    }

    public function getUpdateApplicantBaseQuery($db, $model, $examno, $testDiv, $data_arr_i)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]               = $examno;
        $data["TESTDIV"][TEXT]              = $testDiv;
        $data["SHDIV"][TEXT]                = $data_arr_i["SHDIV"];
        $data["DESIREDIV"][TEXT]            = $data_arr_i["DESIREDIV"];
        $data["NAME"][TEXT]                 = $data_arr_i["NAME"];
        $data["NAME_KANA"][TEXT]            = $data_arr_i["NAME_KANA"];
        $data["SEX"][TEXT]                  = $data_arr_i["SEX"];
        $data["ERACD"][TEXT]                = $data_arr_i["ERACD"];
        $data["BIRTH_Y"][TEXT]              = $data_arr_i["BIRTH_Y"];
        $data["BIRTH_M"][TEXT]              = $data_arr_i["BIRTH_M"];
        $data["BIRTH_D"][TEXT]              = $data_arr_i["BIRTH_D"];
        $data["BIRTHDAY"][DATE]             = $data_arr_i["BIRTHDAY"];
        $data["FS_CD"][TEXT]                = $data_arr_i["FS_CD"];
        $data["FS_NAME"][TEXT]              = $data_arr_i["FS_NAME"];
        $data["FS_GRDYEAR"][TEXT]           = $data_arr_i["FS_GRDYEAR"];
        $data["FS_ERACD"][TEXT]             = $data_arr_i["FS_ERACD"];
        $data["FS_Y"][TEXT]                 = $data_arr_i["FS_Y"];
        $data["FS_M"][TEXT]                 = $data_arr_i["FS_M"];
        $data["FS_DAY"][TEXT]               = $data_arr_i["FS_DAY"];
        $data["FS_GRDDIV"][TEXT]            = $data_arr_i["FS_GRDDIV"];
        $data["REMARK1"][TEXT]              = $data_arr_i["REMARK1"];
        $data["REMARK2"][TEXT]              = $data_arr_i["REMARK2"];
        $data["INTERVIEW_ATTEND_FLG"][TEXT] = $data_arr_i["INTERVIEW_ATTEND_FLG"];
        $data["SHIFT_DESIRE_FLG"][TEXT]     = $data_arr_i["SHIFT_DESIRE_FLG"];
        $data["SLIDE_FLG"][TEXT]            = $data_arr_i["SLIDE_FLG"];
        $data["SPORTS_FLG"][TEXT]           = $data_arr_i["SPORTS_FLG"];
        $data["SH_SCHOOLCD"][TEXT]          = $data_arr_i["SH_SCHOOLCD"];
        $data["RECOM_ITEM1"][TEXT]          = $data_arr_i["RECOM_ITEM1"];
        $data["RECOM_ITEM2"][TEXT]          = $data_arr_i["RECOM_ITEM2"];
        $data["RECOM_ITEM3"][TEXT]          = $data_arr_i["RECOM_ITEM3"];
        $data["RECOM_ITEM4"][TEXT]          = $data_arr_i["RECOM_ITEM4"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
        // $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        // $where .= "       TESTDIV       = '{$testDiv}' AND ";
        $where .= "       EXAMNO        = '{$examno}' ";

        $base_cnt = $db->getOne(knjl015yQuery::cntEntexamApplicantbaseDat($model, $examno));
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
        //$data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["SEQ"][TEXT]              = $seq;
        $data["EXAMNO"][TEXT]           = $examno;
        $data["REMARK1"][TEXT]          = $data_arr_i["SPORTS_FLG2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
        // $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$examno}' AND ";
        $where .= "       SEQ           = '{$seq}' ";

        $bd005_cnt = $db->getOne(knjl015yQuery::cntEntexamApplicantbaseDetailDat($model, $examno, $seq));
        if (0 == $bd005_cnt) {
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
        //$data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
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
        $data["RELATIONSHIP"][TEXT]     = $data_arr_i["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
        // $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
        $where .= "       EXAMNO        = '{$examno}' ";

        $addr_cnt = $db->getOne(knjl015yQuery::cntEntexamApplicantAddrDat($model, $examno));
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
       
        $recept_cnt = $db->getOne(knjl015yQuery::cntEntexamReceptDat($model, $testDiv, $examno));
        if (0 == $recept_cnt) {
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        }

        $db->query($query);
    }

    public function getUpdateEntexamDesireDiv($db, $model, $examno, $testDiv, $data_arr_i)
    {
        //削除 -- ENTEXAM_APPLICANTDESIRE_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTDESIRE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$examno}' ";
        $db->query($query);

        //更新 -- ENTEXAM_APPLICANTDESIRE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $data_arr_i["TESTDIV"];
        $data["EXAMNO"][TEXT]           = $examno;
        $data["DESIREDIV"][TEXT]        = $data_arr_i["DESIREDIV"];
        $data["RECOM_KIND"][TEXT]       = $data_arr_i["RECOM_KIND"];
        $data["SHDIV"][TEXT]            = $data_arr_i["SHDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTDESIRE_DAT");

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
        // $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        // $query .= "   AND TESTDIV       = '{$testDiv}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";

        $db->query($query);
    }
   
    //削除 ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function deleteEntexamApplicantBaseDetail($db, $model, $examno, $seq)
    {
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        // $query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";
        $query .= "   AND SEQ           = '{$seq}' ";

        $db->query($query);
    }

    //削除 ENTEXAM_APPLICANTADDR_DAT
    public function deleteEntexamApplicantAddr($db, $model, $examno)
    {
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        //$query .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";

        $db->query($query);
    }

    //削除 ENTEXAM_APPLICANTDESIRE_DAT
    public function deleteEntexamApplicantDesire($db, $model, $examno)
    {
        $query  = " DELETE FROM ENTEXAM_APPLICANTDESIRE_DAT ";
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
        // $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
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
        //$query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
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
        //$query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
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
}
