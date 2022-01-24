<?php

require_once('for_php7.php');

class knje031Form2
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje031index.php", "", "edit");

        if (VARS::get("cmd") == "from_right"){
            unset($model->field);
            unset($model->transfer_sdate);

            $arg["reload"] = "window.open('knje031index.php?cmd=edit','edit_frame');";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //異動データ取得
        if($model->schregno && $model->transfer_sdate && !$model->isWarning()) {
            $Row = $db->getRow(knje031Query::getTransferDat($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //異動区分コンボボックス
        $query = knje031Query::getTransfercd();
        $extra = "onChange=\"return check(this)\"";
        makeCmb($objForm, $arg, $db, $query, "TRANSFERCD", $Row["TRANSFERCD"], $extra, 1);

        //異動区分の初期値
        $transfercd = ($Row["TRANSFERCD"]) ? $Row["TRANSFERCD"] : "1";

        //異動期間（開始日付）
        $Row["TRANSFER_SDATE"] = str_replace("-", "/", $Row["TRANSFER_SDATE"]);
        $arg["data"]["TRANSFER_SDATE"] = View::popUpCalendar($objForm, "TRANSFER_SDATE", $Row["TRANSFER_SDATE"]);

        //異動期間（終了日付）
        $Row["TRANSFER_EDATE"] = str_replace("-", "/", $Row["TRANSFER_EDATE"]);
        $arg["data"]["TRANSFER_EDATE"] = View::popUpCalendar($objForm, "TRANSFER_EDATE", $Row["TRANSFER_EDATE"]);

        //事由
        $arg["data"]["TRANSFERREASON"] = knjCreateTextBox($objForm, $Row["TRANSFERREASON"], "TRANSFERREASON", 50, 75, "");

        //異動先名称
        $extra = ($transfercd == "1") ? "style=\"background-color:white;\"" : "style=\"background-color:darkgray;\" disabled";
        $arg["data"]["TRANSFERPLACE"] = knjCreateTextBox($objForm, $Row["TRANSFERPLACE"], "TRANSFERPLACE", 50, 60, $extra);

        //異動先住所
        $extra = ($transfercd == "1") ? "style=\"background-color:white;\"" : "style=\"background-color:darkgray;\" disabled";
        $arg["data"]["TRANSFERADDR"] = knjCreateTextBox($objForm, $Row["TRANSFERADDR"], "TRANSFERADDR", 50, 75, $extra);

        //授業日数
        $extra  = "onblur=\"this.value=toInteger(this.value)\"; onblur=\"return selcheck(this)\";";
        $extra .= ($transfercd == "1") ? " style=\"background-color:white;\"" : " style=\"background-color:darkgray;\" disabled";
        $arg["data"]["ABROAD_CLASSDAYS"] = knjCreateTextBox($objForm, $Row["ABROAD_CLASSDAYS"], "ABROAD_CLASSDAYS", 25, 3, $extra);

        //修得単位
        $extra  = "onblur=\"this.value=toInteger(this.value)\"; onblur=\"return selcheck(this)\";";
        $extra .= ($transfercd == "1") ? " style=\"background-color:white;\"" : " style=\"background-color:darkgray;\" disabled";
        $arg["data"]["ABROAD_CREDITS"] = knjCreateTextBox($objForm, $Row["ABROAD_CREDITS"], "ABROAD_CREDITS", 25, 2, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');parent.right_frame.btn_submit('right_list');";
        }

        if ($model->cmd == "edit2" && !$model->isWarning()){
            $arg["reload"] = "window.open('knje031index.php?cmd=from_edit','right_frame');";
            $model->cmd = "edit";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje031Form2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
