<?php

require_once('for_php7.php');

class knjz092Form2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->finschoolcd)) {
            $Row = $db->getRow(knjz092Query::getRow($model->finschoolcd), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", 7, 7, $extra);

        //校種コンボ
        $query = knjz092Query::getNameMst('L019', '4');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_TYPE", $Row["FINSCHOOL_TYPE"], "", 1);

        //学校立コードコンボ
        $query = knjz092Query::getNameMst('L001');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_DISTCD", $Row["FINSCHOOL_DISTCD"], "", 1);

        //学区コードコンボ
        $query = knjz092Query::getNameMst('Z015');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_DISTCD2", $Row["FINSCHOOL_DISTCD2"], "", 1);

        //学校種別コンボ
        $query = knjz092Query::getNameMst('L015');
        makeCmb($objForm, $arg, $db, $query, "FINSCHOOL_DIV", $Row["FINSCHOOL_DIV"], "", 1);

        //学校名
        $arg["data"]["FINSCHOOL_NAME"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_NAME"], "FINSCHOOL_NAME", 50, 50, "");

        //学校名かな
        $arg["data"]["FINSCHOOL_KANA"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_KANA"], "FINSCHOOL_KANA", 50, 50, "");

        //学校名略称
        $arg["data"]["FINSCHOOL_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_NAME_ABBV"], "FINSCHOOL_NAME_ABBV", 20, 20, "");

        //学校名かな略称
        $arg["data"]["FINSCHOOL_KANA_ABBV"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_KANA_ABBV"], "FINSCHOOL_KANA_ABBV", 50, 50, "");

        //学校長氏名
        $arg["data"]["PRINCNAME"] = knjCreateTextBox($objForm, $Row["PRINCNAME"], "PRINCNAME", 40, 40, "");

        //学校長氏名表示用
        $arg["data"]["PRINCNAME_SHOW"] = knjCreateTextBox($objForm, $Row["PRINCNAME_SHOW"], "PRINCNAME_SHOW", 20, 20, "");

        //学校長氏名かな
        $arg["data"]["PRINCKANA"] = knjCreateTextBox($objForm, $Row["PRINCKANA"], "PRINCKANA", 80, 80, "");

        //地区コードコンボ
        $query = knjz092Query::getNameMst('Z003');
        makeCmb($objForm, $arg, $db, $query, "DISTRICTCD", $Row["DISTRICTCD"], "", 1);

        //郵便番号
        $arg["data"]["FINSCHOOL_ZIPCD"] = View::popUpZipCode($objForm, "FINSCHOOL_ZIPCD", $Row["FINSCHOOL_ZIPCD"], "FINSCHOOL_ADDR1");

        //住所１
        $arg["data"]["FINSCHOOL_ADDR1"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_ADDR1"], "FINSCHOOL_ADDR1", 50, 50, "");

        //住所２
        $arg["data"]["FINSCHOOL_ADDR2"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_ADDR2"], "FINSCHOOL_ADDR2", 50, 50, "");

        //電話番号
        $arg["data"]["FINSCHOOL_TELNO"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_TELNO"], "FINSCHOOL_TELNO", 14, 14, "");

        //FAX番号
        $arg["data"]["FINSCHOOL_FAXNO"] = knjCreateTextBox($objForm, $Row["FINSCHOOL_FAXNO"], "FINSCHOOL_FAXNO", 14, 14, "");

        //教育委員会コードコンボ
        $query = knjz092Query::getEdboardMst();
        makeCmb($objForm, $arg, $db, $query, "EDBOARDCD", $Row["EDBOARDCD"], "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz092index.php", "", "edit");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz092index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz092Form2.html", $arg);
    }
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
//    //戻るボタンを作成する
//    $link = REQUESTROOT."/Z/KNJZ092/knjz092index.php";
//    $extra = "onclick=\"parent.location.href='$link';\"";
//    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "cmd");
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $extra = "onclick=\"return changeRadio(this);\"";
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
