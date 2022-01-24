<?php

require_once('for_php7.php');

class knjz291_swForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz291_swindex.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->staffcd)) {
            if ($model->cmd === 'change') {
                $Row =& $model->field;
            } else {
                $query = knjz291_swQuery::getRow($model, $model->staffcd);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
        } else {
            $Row =& $model->field;
        }

        if ($model->staffcd) {
            $arg["update"] = 1;
        }

        /************/
        /* 編集項目 */
        /************/

        makeEditField($objForm, $arg, $db, $Row, $model);

        /************/
        /* 表示項目 */
        /************/

        //職員コード
        $arg["data"]["STAFFCD"] = $Row["STAFFCD"];
        //職員氏名
        $arg["data"]["STAFFNAME"] = $Row["STAFFNAME"];
        //職員氏名表示用
        $arg["data"]["STAFFNAME_SHOW"] = $Row["STAFFNAME_SHOW"];
        //職員氏名かな
        $arg["data"]["STAFFNAME_KANA"] = $Row["STAFFNAME_KANA"];
        //職員氏名英字
        $arg["data"]["STAFFNAME_ENG"] = $Row["STAFFNAME_ENG"];
        //戸籍氏名
        $arg["data"]["STAFFNAME_REAL"] = $Row["STAFFNAME_REAL"];
        //戸籍氏名かな
        $arg["data"]["STAFFNAME_KANA_REAL"] = $Row["STAFFNAME_KANA_REAL"];
        //性別
        $staffsex = $db->getOne(knjz291_swQuery::getNameMstLabel("Z002", $Row["STAFFSEX"]));
        $arg["data"]["STAFFSEX"] = (strlen($Row["STAFFSEX"])) ? $staffsex : "";
        //職員生年月日
        $arg["data"]["STAFFBIRTHDAY"] = str_replace("-","/",$Row["STAFFBIRTHDAY"]);
        //郵便番号
        $arg["data"]["STAFFZIPCD"] = $Row["STAFFZIPCD"];
        //住所１
        $arg["data"]["STAFFADDR1"] = $Row["STAFFADDR1"];
        //住所２
        $arg["data"]["STAFFADDR2"] = $Row["STAFFADDR2"];
        //電話番号
        $arg["data"]["STAFFTELNO"] = $Row["STAFFTELNO"];
        //FAX番号
        $arg["data"]["STAFFFAXNO"] = $Row["STAFFFAXNO"];
        //E-Mail
        $arg["data"]["STAFFE_MAIL"] = $Row["STAFFE_MAIL"];
        //赴任(入)日付
        $arg["data"]["FROM_DATE"] = str_replace("-", "/", $Row["FROM_DATE"]);
        //赴任(入)学校コード
        $from_schoolcd = $db->getOne(knjz291_swQuery::getEdSchool($Row["FROM_SCHOOLCD"]));
        $arg["data"]["FROM_SCHOOLCD"] = $from_schoolcd;
        //赴任(入)所属区分
        $from_div = $db->getOne(knjz291_swQuery::getNameMstLabel("Z041", $Row["FROM_DIV"]));
        $arg["data"]["FROM_DIV"] = $from_div;
        //赴任(入)課程
        $from_coursecd = $db->getOne(knjz291_swQuery::getCouse($Row["FROM_COURSECD"]));
        $arg["data"]["FROM_COURSECD"] = $from_coursecd;
        //赴任(出)日付
        $arg["data"]["TO_DATE"] = str_replace("-", "/", $Row["TO_DATE"]);
        //赴任(出)学校コード
        $to_schoolcd = $db->getOne(knjz291_swQuery::getEdSchool($Row["TO_SCHOOLCD"]));
        $arg["data"]["TO_SCHOOLCD"] = $to_schoolcd;
        //赴任(出)所属区分
        $to_div = $db->getOne(knjz291_swQuery::getNameMstLabel("Z041", $Row["TO_DIV"]));
        $arg["data"]["TO_DIV"] = $to_div;
        //赴任(出)課程
        $to_coursecd = $db->getOne(knjz291_swQuery::getCouse($Row["TO_COURSECD"]));
        $arg["data"]["TO_COURSECD"] = $to_coursecd;

        //資格教科登録
        makeShikaku($objForm, $arg, $db, $model);

        //届け登録
        makeTodoke($objForm, $arg, $db, $model);

        //免許・資格登録
        makeMenkyo($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz291_swindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz291_swForm2.html", $arg);
    }
}

//編集項目
function makeEditField(&$objForm, &$arg, $db, $Row, $model) {
    //画面表示セット
    $arg["data"]["SET_COLSPAN"] = "3";
    $arg["data"]["SET_BGCOLOR"] = 'bgcolor="#00BFFF"';
    $arg["data"]["SET_BGCOLOR2"] = 'bgcolor="#99eaff"';

    //担当保健室表示セット
    if ($model->Properties["useNurseoffRestrict"] == '1') {
        $arg["data"]["SET_COLWITH"] = 'width="30%"';
        $arg["useNurse"] = "1";
    } else {
        $arg["data"]["SET_COLWITH"] = 'colspan="3"';
    }

    //extra
    $disable = ($model->staffcd) ? "" : " disabled";

    //職名コンボボックス
    $query = knjz291_swQuery::getJOBNAME();
    makeCmb($objForm, $arg, $db, $query, "JOBCD", $Row["JOBCD"], $disable, 1, "", "BLANK");

    //所属コンボボックス
    $query = knjz291_swQuery::getSECTION();
    makeCmb($objForm, $arg, $db, $query, "SECTIONCD", $Row["SECTIONCD"], $disable, 1, "", "BLANK");

    //校務分掌部コンボボックス
    $query = knjz291_swQuery::getDUTYSHARE();
    $extra = " onchange=\"chkSelSameValue()\" ";
    makeCmb($objForm, $arg, $db, $query, "DUTYSHARECD", $Row["DUTYSHARECD"], $extra.$disable, 1, "", "BLANK");
    makeCmb($objForm, $arg, $db, $query, "DUTYSHARECD2", $Row["DUTYSHARECD2"], $extra.$disable, 1, "", "BLANK");

    //授業受持ちコンボボックス
    $opt   = array();
    $opt[] = array("label" => "", "value" => "");
    $opt[] = array("label" => "0 無し", "value" => "0");
    $opt[] = array("label" => "1 有り", "value" => "1");
    $arg["data"]["CHARGECLASSCD"] = knjCreateCombo($objForm, "CHARGECLASSCD", $Row["CHARGECLASSCD"], $opt, $disable, 1);

    //肩書き1
    $extra = "onchange=\"return btn_submit('change', 1);\"";
    $query = knjz291_swQuery::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD1", $Row["POSITIONCD1"], $extra.$disable, 1, "", "BLANK");
    //肩書き1の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 1, "005");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 1, "005");
    }

    //肩書き2
    $extra = "onchange=\"return btn_submit('change', 2);\"";
    $query = knjz291_swQuery::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD2", $Row["POSITIONCD2"], $extra.$disable, 1, "", "BLANK");
    //肩書き2の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 2, "006");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 2, "006");
    }

    //肩書き3
    $extra = "onchange=\"return btn_submit('change', 3);\"";
    $query = knjz291_swQuery::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD3", $Row["POSITIONCD3"], $extra.$disable, 1, "", "BLANK");
    //肩書き3の学年主任、教科主任
    if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
        setPositionExtCmb($objForm, $arg, $db, $model, $Row, 3, "007");
    } else {
        setPositionCmb($objForm, $arg, $db, $model, $Row, 3, "007");
    }

    //担当保健室コンボ
    $query = knjz291_swQuery::getVNameMst("Z043", $model);
    makeCmb($objForm, $arg, $db, $query, "CHARGENURSEOFF", $Row["CHARGENURSEOFF"], $disable, 1, "", "BLANK");
}

//肩書詳細
function setPositionCmb(&$objForm, &$arg, $db, $model, $Row, $positionNum, $staffSeq) {
    //肩書きの学年主任、教科主任
    if ($Row["POSITIONCD{$positionNum}"] === '0200' || $Row["POSITIONCD{$positionNum}"] === '1050') {
        $arg["MANAGER{$positionNum}_SET"] = "1";
        //学年主任　教科主任
        if ($Row["POSITIONCD{$positionNum}"] === '0200') {
            $query = knjz291_swQuery::getGrade($model);
        } else {
            $query = knjz291_swQuery::getClass($model);
        }
    } else {
        return true;
    }
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD{$positionNum}_MANAGER", $Row["POSITIONCD{$positionNum}_MANAGER"], "", 1, "", "BLANK");
}

//肩書詳細EXT
function setPositionExtCmb(&$objForm, &$arg, $db, $model, $Row, $positionNum, $staffSeq) {

    //肩書きの学年主任、教科主任
    $setClass = "";
    $setSize = "1";
    $positionNo = $Row["POSITIONCD{$positionNum}"];
    if ($positionNo === '0200' || $positionNo === '1050') {
        $arg["MANAGER{$positionNum}_SET"] = "1";
        //学年主任　教科主任
        if ($positionNo === '0200') {
            $query = knjz291_swQuery::getGrade($model);
        } else {
            if ($model->cmd == "change") {
                $Row["POSITIONCD{$positionNum}_MANAGER"] = array();
                $Row["POSITIONCD{$positionNum}_MANAGER"] = explode(",", $model->field["POSITION{$positionNum}_EXT"]);
            } else {
                $query = knjz291_swQuery::getStaffExt($model, $staffSeq);
                $result = $db->query($query);
                $Row["POSITIONCD{$positionNum}_MANAGER"] = array();
                while ($extRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $Row["POSITIONCD{$positionNum}_MANAGER"][] = $extRow["VALUE"];
                }
                $result->free();
            }

            $query = knjz291_swQuery::getClass($model);
            $arg["setBR{$positionNum}"] = "<BR>";
            $setMultiple = "1";
            $setSize = "10";
            $setBtnText = "教科登録";
            if ($model->field["POSITIONCD_NUM"] == $positionNum) {
                $setClass .= " style=\"width:300px\" ";
                $setBtnText = "閉じる";
            } else {
                $setClass .= " style=\"display:none; width:300px\" ";
            }

            //表示/非表示ボタン
            $extra = " id=\"BTN_POSITION{$positionNum}\" onclick=\"changeDisplay('{$positionNum}')\"";
            $arg["BTN_POSITION{$positionNum}"] = knjCreateBtn($objForm, "BTN_POSITION{$positionNum}", $setBtnText, $extra);
        }
    } else {
        return true;
    }
    $extra = $setClass." id=\"POSITIONCD{$positionNum}_MANAGER\" ";
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD{$positionNum}_MANAGER", $Row["POSITIONCD{$positionNum}_MANAGER"], $extra, $setSize, $setMultiple, "BLANK");
}

//資格教科登録
function makeShikaku(&$objForm, &$arg, $db, $model) {
    $disable = ($model->staffcd) ? "" : " disabled";

    //資格教科登録ボタン
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "資格教科登録", $extra.$disable);

    //資格科教科表示
    $query = knjz291_swQuery::getStaffClass($model);
    $arg["data"]["STF_SUBCLASS"] = implode(',',$db->getCol($query));

    if ($model->sendSubmit != "") $arg["shikaku"] = 1;
}

//届け登録
function makeTodoke(&$objForm, &$arg, $db, $model) {
    //届け登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_REQUESTFORM/knjz291_staff_requestformindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291_SW&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd) ? "" : " disabled";
    $arg["button"]["btn_requestform"] = knjCreateBtn($objForm, "btn_requestform", "届け登録", $extra);

    //届け表示
    $query = knjz291_swQuery::getStaffRequestform($model);
    $arg["data"]["STF_REQFORM"] = implode(',',$db->getCol($query));
}

//免許・資格登録
function makeMenkyo(&$objForm, &$arg, $db, $model) {
    //免許・資格登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_QUALIFIED/knjz291_staff_qualifiedindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291_SW&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd) ? "" : " disabled";
    $arg["button"]["btn_qualified"] = knjCreateBtn($objForm, "btn_qualified", "免許・資格登録", $extra);

    //免許・資格表示
    $data = array();
    $query = knjz291_swQuery::getStaffQualified($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $sep = "";
        if($row["QUALIFIED_CD"] != "" && $row["QUALIFIED_NAME"] != "") {
            $sep = "/";
        }

        $data[] = $row["QUALIFIED_CD"].$sep.$row["QUALIFIED_NAME"];
    }
    $arg["data"]["STF_QUALIFIED"] = implode(',',$data);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $disable = ($model->auth == DEF_UPDATABLE) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //戻るボタン
    $link = REQUESTROOT."/Z/KNJZ291A/knjz291aindex.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

    //赴任履歴修正ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_WORKHIST/knjz291_staff_workhistindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291_SW&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2') ? "" : " disabled";
    if ($model->Properties["knjz291NotShowWorkHistButton"] != '1') {
        $arg["button"]["btn_workhist"] = knjCreateBtn($objForm, "btn_workhist", "赴任履歴修正", $extra);
    }

    //校長履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ290S1/knjz290s1index.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ291_SW&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd) ? "" : " disabled";
    $arg["button"]["btn_prihist"] = knjCreateBtn($objForm, "btn_prihist", "校長履歴登録", $extra);
    if ($model->sendSubmit != "") $arg["koutyou"] = 1;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $multiple, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($multiple != "1") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size, $multiple);
}

//hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "POSITIONCD_NUM");
    knjCreateHidden($objForm, "POSITION1_EXT");
    knjCreateHidden($objForm, "POSITION2_EXT");
    knjCreateHidden($objForm, "POSITION3_EXT");
    knjCreateHidden($objForm, "HID_DUTYSHARECD", $Row["DUTYSHARECD"]);
    knjCreateHidden($objForm, "HID_DUTYSHARECD_2", $Row["DUTYSHARECD2"]);
}
?>
