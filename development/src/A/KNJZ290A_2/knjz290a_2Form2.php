<?php

require_once('for_php7.php');

class knjz290a_2Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz290a_2index.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //新規
        if ($model->cmd == "new") {
            unset($model->staffcd);
            unset($model->field);
        }

        //学校名取得
        $model->getSchoolName = $db->getOne(knjz290a_2Query::getSchoolName());

        //警告メッセージを表示しない場合
        $model->iinkai = "";
        if (!isset($model->warning) && isset($model->staffcd) && ($model->cmd != "search")) {
            if ($model->cmd === 'change') {
                $Row =& $model->field;
            } else {
                $query = knjz290a_2Query::getRow($model, $model->staffcd);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
        } else if ($model->cmd == "search") {
            //職員マスタ存在チェック
            $query = knjz290a_2Query::getRow($model, $model->field["STAFFCD"]);
            $check = $db->getRow($query);
            if (is_array($check)){
                $model->setWarning("この職員コードは既に登録されています。");
                $Row["STAFFCD"] = $model->field["STAFFCD"];
            } else {
                //教育委員会から表示
                $query = knjz290a_2Query::getRow($model, $model->field["STAFFCD"]);
                $Row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                if ($Row["STAFFCD"]) {
                    $model->staffcd = $Row["STAFFCD"];
                    $model->iinkai = 1;
                } else {
                    $model->setWarning("MSG303", "　　（教育委員会）");
                    $Row["STAFFCD"] = $model->field["STAFFCD"];
                }
            }
        } else {
            $Row =& $model->field;
        }

        //教育委員会用学校コード取得
        $query = knjz290a_2Query::getEdboardSchoolcd();
        $model->edboard_schoolcd = $db->getOne($query);

        //職員区分取得
        $query = knjz290a_2Query::getStaffDiv($model->edboard_schoolcd, $model->staffcd);
        $model->staff_div = $db2->getOne($query);

        if ($model->iinkai) {
            $arg["insert"] = 1;
            $readonly = " readonly style=\"background-color:lightgray;\"";
        } else if ($model->staffcd) {
            $arg["update"] = 1;
            $readonly = " readonly style=\"background-color:lightgray;\"";
        } else {
            $arg["insert"] = 1;
            $readonly = "";
        }

        //職員コード
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($model->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }
        $extra = ($model->staffcd) ? " readonly style=\"background-color:lightgray;\"" : "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["STAFFCD"] = knjCreateTextBox($objForm, $Row["STAFFCD"], "STAFFCD", $setsize, $setsize, $extra.$readonly);

        //職員氏名
        $extra = "";
        $arg["data"]["STAFFNAME"] = knjCreateTextBox($objForm, $Row["STAFFNAME"], "STAFFNAME", 40, 60, $extra.$readonly);

        //職員氏名表示用
        $extra = "";
        $arg["data"]["STAFFNAME_SHOW"] = knjCreateTextBox($objForm, $Row["STAFFNAME_SHOW"], "STAFFNAME_SHOW", 10, 15, $extra.$readonly);

        //職員氏名かな
        $extra = "";
        $arg["data"]["STAFFNAME_KANA"] = knjCreateTextBox($objForm, $Row["STAFFNAME_KANA"], "STAFFNAME_KANA", 80, 120, $extra.$readonly);

        //職員氏名英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["STAFFNAME_ENG"] = knjCreateTextBox($objForm, $Row["STAFFNAME_ENG"], "STAFFNAME_ENG", 40, 60, $extra.$readonly);

        //戸籍氏名
        $extra = "";
        $arg["data"]["STAFFNAME_REAL"] = knjCreateTextBox($objForm, $Row["STAFFNAME_REAL"], "STAFFNAME_REAL", 80, 120, $extra.$readonly);

        //戸籍氏名かな
        $extra = "";
        $arg["data"]["STAFFNAME_KANA_REAL"] = knjCreateTextBox($objForm, $Row["STAFFNAME_KANA_REAL"], "STAFFNAME_KANA_REAL", 80, 240, $extra.$readonly);

        //学校側表示項目
        makeSchoolSide($objForm, $arg, $db, $Row, $model);

        //性別コンボボックス
        $query = knjz290a_2Query::getSTAFFSEX();
        makeCmb($objForm, $arg, $db, $query, "STAFFSEX", $Row["STAFFSEX"], "", 1);

        //職員生年月日
        $Row["STAFFBIRTHDAY"] = str_replace("-","/",$Row["STAFFBIRTHDAY"]);
        $arg["data"]["STAFFBIRTHDAY"] = View::popUpCalendar($objForm, "STAFFBIRTHDAY" ,$Row["STAFFBIRTHDAY"]);

        //郵便番号
        if ($model->staffcd) {
            $arg["data"]["STAFFZIPCD"] = knjCreateTextBox($objForm, $Row["STAFFZIPCD"], "STAFFZIPCD", 10, 10, $readonly);
        } else {
            $arg["data"]["STAFFZIPCD"] = View::popUpZipCode($objForm, "STAFFZIPCD", $Row["STAFFZIPCD"], "STAFFADDR1");
        }

        //住所１
        $extra = "";
        $arg["data"]["STAFFADDR1"] = knjCreateTextBox($objForm, $Row["STAFFADDR1"], "STAFFADDR1", 50, 90, $extra.$readonly);

        //住所２
        $extra = "";
        $arg["data"]["STAFFADDR2"] = knjCreateTextBox($objForm, $Row["STAFFADDR2"], "STAFFADDR2", 50, 90, $extra.$readonly);

        //電話番号
        $extra = "";
        $arg["data"]["STAFFTELNO"] = knjCreateTextBox($objForm, $Row["STAFFTELNO"], "STAFFTELNO", 14, 14, $extra.$readonly);

        //FAX番号
        $extra = "";
        $arg["data"]["STAFFFAXNO"] = knjCreateTextBox($objForm, $Row["STAFFFAXNO"], "STAFFFAXNO", 14, 14, $extra.$readonly);

        //E-Mail
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["STAFFE_MAIL"] = knjCreateTextBox($objForm, $Row["STAFFE_MAIL"], "STAFFE_MAIL", 25, 25, $extra.$readonly);

        //履歴有効開始日登録
        makeRirekiSdate($objForm, $arg, $db, $model);

        //資格教科登録
        makeShikaku($objForm, $arg, $db, $model);

        //届け登録
        makeTodoke($objForm, $arg, $db2, $model);

        //免許・資格登録
        makeMenkyo($objForm, $arg, $db2, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        
        $arg["show_csv"] = "ON";
        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model, $db);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz290a_2index.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz290a_2Form2.html", $arg);
    }
}

//学校側表示項目
function makeSchoolSide(&$objForm, &$arg, $db, $Row, $model) {

    //画面表示セット
    $arg["data"]["SET_COLSPAN"] = "3";
    $arg["data"]["SET_BGCOLOR"] = 'bgcolor="#00BFFF"';
    $arg["data"]["SET_BGCOLOR2"]   = 'bgcolor="#99eaff"';

    //職名コンボボックス
    $query = knjz290a_2Query::getJOBNAME();
    makeCmb($objForm, $arg, $db, $query, "JOBCD", $Row["JOBCD"], "", 1);

    //所属コンボボックス
    $query = knjz290a_2Query::getSECTION();
    makeCmb($objForm, $arg, $db, $query, "SECTIONCD", $Row["SECTIONCD"], "", 1);

    //校務分掌部コンボボックス
    $query = knjz290a_2Query::getDUTYSHARE();
    makeCmb($objForm, $arg, $db, $query, "DUTYSHARECD", $Row["DUTYSHARECD"], "", 1);

    //授業受持ちコンボボックス
    $opt   = array();
    $opt[] = array("label" => "", "value" => "");
    $opt[] = array("label" => "0 無し", "value" => "0");
    $opt[] = array("label" => "1 有り", "value" => "1");
    $extra = "";
    $arg["data"]["CHARGECLASSCD"] = knjCreateCombo($objForm, "CHARGECLASSCD", $Row["CHARGECLASSCD"], $opt, $extra, 1);

    //肩書き1
    $extra = "onchange=\"return btn_submit('change');\"";
    $query = knjz290a_2Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD1", $Row["POSITIONCD1"], $extra, 1);
    
    //肩書き2
    $extra = "onchange=\"return btn_submit('change');\"";
    $query = knjz290a_2Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD2", $Row["POSITIONCD2"], $extra, 1);
    
    //肩書き3
    $extra = "onchange=\"return btn_submit('change');\"";
    $query = knjz290a_2Query::getPositionCd($model);
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD3", $Row["POSITIONCD3"], $extra, 1);

    //肩書き1の学年主任、教科主任
    if ($Row["POSITIONCD1"] === '0200' || $Row["POSITIONCD1"] === '1050') {
        $arg["MANAGER1_SET"] = "1";
        //学年主任　教科主任
        if ($Row["POSITIONCD1"] === '0200') {
            $query = knjz290a_2Query::getGrade($model);
        } else {
            if ($model->getSchoolName != 'sapporo') {
                $query = knjz290a_2Query::getClass($model);
            } else {
                $query = knjz290a_2Query::getIbClass($model);
            }
        }
    }
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD1_MANAGER", $Row["POSITIONCD1_MANAGER"], "", 1);

    //肩書き2の学年主任、教科主任
    if ($Row["POSITIONCD2"] === '0200' || $Row["POSITIONCD2"] === '1050') {
        $arg["MANAGER2_SET"] = "1";
        //学年主任　教科主任
        if ($Row["POSITIONCD2"] === '0200') {
            $query = knjz290a_2Query::getGrade($model);
        } else {
            if ($model->getSchoolName != 'sapporo') {
                $query = knjz290a_2Query::getClass($model);
            } else {
                $query = knjz290a_2Query::getIbClass($model);
            }
        }
    }
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD2_MANAGER", $Row["POSITIONCD2_MANAGER"], "", 1);

    //肩書き3の学年主任、教科主任
    if ($Row["POSITIONCD3"] === '0200' || $Row["POSITIONCD3"] === '1050') {
        $arg["MANAGER3_SET"] = "1";
        //学年主任　教科主任
        if ($Row["POSITIONCD3"] === '0200') {
            $query = knjz290a_2Query::getGrade($model);
        } else {
            if ($model->getSchoolName != 'sapporo') {
                $query = knjz290a_2Query::getClass($model);
            } else {
                $query = knjz290a_2Query::getIbClass($model);
            }
        }
    }
    makeCmb($objForm, $arg, $db, $query, "POSITIONCD3_MANAGER", $Row["POSITIONCD3_MANAGER"], "", 1);

    if ($model->sendSubmit != "") $arg["school"] = 1;
}

//履歴有効開始日登録
function makeRirekiSdate(&$objForm, &$arg, $db, $model) {

    //氏名有効開始日
    $arg["data"]["NAME_SDATE"] = View::popUpCalendar($objForm, "NAME_SDATE" ,$model->field["NAME_SDATE"]);

    //住所有効開始日
    $arg["data"]["ADDRESS_SDATE"] = View::popUpCalendar($objForm, "ADDRESS_SDATE" ,$model->field["ADDRESS_SDATE"]);

    if (!$model->staffcd && !$model->iinkai) $arg["rireki"] = 1;
}

//資格教科登録
function makeShikaku(&$objForm, &$arg, $db, $model) {

    $disable = ($model->staffcd && !$model->iinkai) ? "" : " disabled";

    //資格教科登録ボタン
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "資格教科登録", $extra.$disable);

    //資格科教科表示
    $query = knjz290a_2Query::getStaffClass($model);
    $arg["data"]["STF_SUBCLASS"] = implode(',',$db->getCol($query));

    if ($model->sendSubmit != "") $arg["shikaku"] = 1;
}

//届け登録
function makeTodoke(&$objForm, &$arg, $db2, $model) {

    //届け登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_REQUESTFORM/knjz291_staff_requestformindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_requestform"] = knjCreateBtn($objForm, "btn_requestform", "届け登録", $extra);

    //届け表示
    $query = knjz290a_2Query::getStaffRequestform($model);
    $arg["data"]["STF_REQFORM"] = implode(',',$db2->getCol($query));
}

//免許・資格登録
function makeMenkyo(&$objForm, &$arg, $db2, $model) {

    //免許・資格登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_QUALIFIED/knjz291_staff_qualifiedindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_qualified"] = knjCreateBtn($objForm, "btn_qualified", "免許・資格登録", $extra);

    //免許・資格表示
    $data = array();
    $query = knjz290a_2Query::getStaffQualified($model);
    $result = $db2->query($query);
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

    //新規ボタン
    $extra = "onclick=\"return btn_submit('new');\"";
    $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disable);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    if ($model->sendSubmit == "1") {
        $link = REQUESTROOT."/Z/KNJZ290A/knjz290aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $name = "戻 る";
    } else {
        $extra = "onclick=\"return closeWin();\"";
        $name = "終 了";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", $name, $extra);

    //所属履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_WORKHIST/knjz291_staff_workhistindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && $model->staff_div != '2' && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_workhist"] = knjCreateBtn($objForm, "btn_workhist", "学校所属登録", $extra);

    //氏名履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_NAME_HIST/knjz291_staff_name_histindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd."&SEND_STAFF_DIV=".$model->staff_div;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_namehist"] = ($model->staffcd && !$model->iinkai) ? knjCreateBtn($objForm, "btn_namehist", "※職員氏名", $extra) : "※職員氏名";

    //住所履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ291_STAFF_ADDRESS/knjz291_staff_addressindex.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd."&SEND_STAFF_DIV=".$model->staff_div;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_address"] = ($model->staffcd && !$model->iinkai) ? knjCreateBtn($objForm, "btn_address", "住所1", $extra) : "住所1";

    //校長履歴登録ボタン
    $extra  = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ290S1/knjz290s1index.php?";
    $extra .= "&cmd=&SEND_PRGID=KNJZ290A_2&SEND_AUTH=".$model->auth."&SEND_STAFFCD=".$model->staffcd;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $extra .= ($model->staffcd && !$model->iinkai) ? "" : " disabled";
    $arg["button"]["btn_prihist"] = knjCreateBtn($objForm, "btn_prihist", "校長履歴登録", $extra);
    if ($model->sendSubmit != "") $arg["koutyou"] = 1;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model, $db) {
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["data"]["CSV_XLS_NAME"] = "ＣＳＶ出力<BR>／ＣＳＶ取込";

    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}

//hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJZ290A_2");
    knjCreateHidden($objForm, "TEMPLATE_PATH");
    
}
?>
