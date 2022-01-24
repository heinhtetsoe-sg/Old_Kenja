<?php

require_once('for_php7.php');

class knjz091a_2Form2
{
    public function main(&$model)
    {
        $objForm = new form();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->prischoolcd) && $model->cmd != "chgPref") {
            $Row = knjz091a_2Query::getRow($model->prischoolcd, $model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //出身塾コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, $extra);

        //出身塾名
        $extra = "";
        $arg["data"]["PRISCHOOL_NAME"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_NAME"], "PRISCHOOL_NAME", 50, 75, $extra);

        //出身塾カナ
        $extra = "";
        $arg["data"]["PRISCHOOL_KANA"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_KANA"], "PRISCHOOL_KANA", 50, 75, $extra);

        //出身塾長氏名
        $extra = "";
        $arg["data"]["PRINCNAME"] = knjCreateTextBox($objForm, $Row["PRINCNAME"], "PRINCNAME", 30, 60, $extra);

        //出身塾長氏名表示用
        $extra = "";
        $arg["data"]["PRINCNAME_SHOW"] = knjCreateTextBox($objForm, $Row["PRINCNAME_SHOW"], "PRINCNAME_SHOW", 20, 30, $extra);

        //出身塾長氏名かな
        $extra = "";
        $arg["data"]["PRINCKANA"] = knjCreateTextBox($objForm, $Row["PRINCKANA"], "PRINCKANA", 80, 120, $extra);

        //担当者コンボ
        $query = knjz091a_2Query::getStaffMst();
        makeCmb($objForm, $arg, $db, $query, "PRISCHOOL_STAFFCD", $Row["PRISCHOOL_STAFFCD"], "", 1);

        //地区コード
        $query = knjz091a_2Query::getDistinct();
        makeCmb($objForm, $arg, $db, $query, "DISTRICTCD", $Row["DISTRICTCD"], "", 1);

        $Row["PRISCHOOL_PREF_CD"] = ($model->field["PRISCHOOL_PREF_CD"] != "") ? $model->field["PRISCHOOL_PREF_CD"] : $Row["PRISCHOOL_PREF_CD"];

        //都道府県コンボ
        $extra = "onchange=\"return btn_submit('chgPref');\"";
        $query = knjz091a_2Query::getPrefMst();
        makeCmb($objForm, $arg, $db, $query, "PRISCHOOL_PREF_CD", $Row["PRISCHOOL_PREF_CD"], $extra, 1);

        //市区町村コンボ
        if ($model->cmd == "chgPref") {
            $Row["PRISCHOOL_CITY_CD"] = "";
        }
        $query = knjz091a_2Query::getCityMst($Row["PRISCHOOL_PREF_CD"]);
        makeCmb($objForm, $arg, $db, $query, "PRISCHOOL_CITY_CD", $Row["PRISCHOOL_CITY_CD"], "", 1);

        //出身塾郵便番号
        $arg["data"]["PRISCHOOL_ZIPCD"] = View::popUpZipCode($objForm, "PRISCHOOL_ZIPCD", $Row["PRISCHOOL_ZIPCD"], "PRISCHOOL_ADDR1");
        
        //出身塾住所１
        $extra = "";
        $arg["data"]["PRISCHOOL_ADDR1"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_ADDR1"], "PRISCHOOL_ADDR1", 50, 90, $extra);

        //出身塾住所２
        $extra = "";
        $arg["data"]["PRISCHOOL_ADDR2"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_ADDR2"], "PRISCHOOL_ADDR2", 50, 90, $extra);

        //出身塾電話番号
        $extra = "";
        $arg["data"]["PRISCHOOL_TELNO"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_TELNO"], "PRISCHOOL_TELNO", 14, 14, $extra);

        //出身塾FAX番号
        $extra = "";
        $arg["data"]["PRISCHOOL_FAXNO"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_FAXNO"], "PRISCHOOL_FAXNO", 14, 14, $extra);

        if ($model->Properties["useMiraicompass"] == '1') {
            $arg["useMiraicompass"] = 1;
            //ミライコンパス塾コード
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["MIRAI_PS_CD"] = knjCreateTextBox($objForm, $Row["MIRAI_PS_CD"], "MIRAI_PS_CD", 10, 10, $extra);
        }

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ091A/knjz091aindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //教室登録
        $extra  = " onClick=\" wopen('".REQUESTROOT."/Z/KNJZ091A_3/knjz091a_3index.php?";
        $extra .= "PRISCHOOLCD=".$Row["PRISCHOOLCD"]."&cmd=";
        $extra .= "&AUTH=".$model->auth;
        $extra .= "&CALLID=KNJZ091A_2";
        $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_z091a_3"] = knjCreateBtn($objForm, "btn_z091a_3", "教室登録", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //教室一覧
        $query = knjz091a_2Query::getPriClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subdata = "wopen('".REQUESTROOT."/Z/KNJZ091A_3/knjz091a_3index.php?cmd=&AUTH={$model->auth}&PRISCHOOLCD={$row["PRISCHOOLCD"]}&PRISCHOOL_CLASS_CD={$row["PRISCHOOL_CLASS_CD"]}&CALLID=KNJZ091A_2&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $row["PRISCHOOL_NAME"] = View::alink("#", htmlspecialchars($row["PRISCHOOL_NAME"]), "onclick=\"$subdata\"");
            $arg["data2"][] = $row;
        }
        $result->free();

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz091a_2index.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "chgPref") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz091a_2index.php?cmd=list';";
        }
                                    
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz091a_2Form2.html", $arg);
    }
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model, $db)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 5120000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["data"]["CSV_XLS_NAME"] = "ＣＳＶ出力<BR>／ＣＳＶ取込";

    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = (($value === '0' || $value) && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
