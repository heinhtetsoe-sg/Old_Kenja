<?php

require_once('for_php7.php');

class knjp091kForm2 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->paid_money_date)){
            $Row = $db->getRow(knjp091kQuery::getRow($model->paid_money_date), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        $Row["PAID_MONEY_DATE"] = str_replace("-", "/", $Row["PAID_MONEY_DATE"]);
        $Row["REPLACE_MONEY_DATE"] = str_replace("-", "/", $Row["REPLACE_MONEY_DATE"]);

        //振替日
        $arg["data"]["PAID_MONEY_DATE"] = View::popUpCalendar($objForm, "PAID_MONEY_DATE", $Row["PAID_MONEY_DATE"]);
        //入金日
        $arg["data"]["REPLACE_MONEY_DATE"] = View::popUpCalendar($objForm, "REPLACE_MONEY_DATE", $Row["REPLACE_MONEY_DATE"]);
        //学校コード
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 20, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp091kindex.php", "", "edit");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjp091kindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp091kForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
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
    //戻るボタンを作成する
    $link = REQUESTROOT."/P/KNJP090K/knjp090kindex.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "cmd");
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model) {
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出
    $opt_shubetsu = array(1, 2, 3);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $extra = "onclick=\"return changeRadio(this);\"";
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

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
?>
