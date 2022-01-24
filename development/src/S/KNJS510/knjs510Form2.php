<?php

require_once('for_php7.php');

class knjs510form2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjs510index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //警告メッセージを表示しない場合
        if (isset($model->seq) && !isset($model->warning) && ($model->cmd != "change")){
            $Row = $db->getRow(knjs510Query::getPubHolidayData(CTRL_YEAR, $model->seq), DB_FETCHMODE_ASSOC);   
        } else {
            $Row =& $model->field;
        }

        //祝祭日区分ラジオボタン 1:日付指定 2:回数曜日指定
        $opt_div = array(1, 2);
        $Row["HOLIDAY_DIV"] = ($Row["HOLIDAY_DIV"] == "") ? "1" : $Row["HOLIDAY_DIV"];
        $click = " onclick=\"return btn_submit('change')\"";
        $extra = array("id=\"HOLIDAY_DIV1\"".$click, "id=\"HOLIDAY_DIV2\"".$click);
        $radioArray = knjCreateRadio($objForm, "HOLIDAY_DIV", $Row["HOLIDAY_DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //必須項目マーク表示
        $arg["data"]["MARK1"] = ($Row["HOLIDAY_DIV"] == "1") ? "※" : "";
        $arg["data"]["MARK2"] = ($Row["HOLIDAY_DIV"] == "2") ? "※" : "";

        //月コンボボックス
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        for($i = 1; $i <= 12; $i++) {
            $opt[] = array('label' => sprintf("%02d", $i).'月', 'value' => sprintf("%02d", $i));
        }
        $Row["HOLIDAY_MONTH"] = ($Row["HOLIDAY_MONTH"]) ? $Row["HOLIDAY_MONTH"] : "";
        $extra = ($Row["HOLIDAY_DIV"] == "1") ? "onchange=\"return btn_submit('change')\"" : "";
        $arg["data"]["HOLIDAY_MONTH"] = knjCreateCombo($objForm, "HOLIDAY_MONTH", $Row["HOLIDAY_MONTH"], $opt, $extra, 1);

        //日コンボボックス
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $lastday = ($Row["HOLIDAY_MONTH"]) ? date("t", mktime( 0, 0, 0, $Row["HOLIDAY_MONTH"], 1, CTRL_YEAR )) : "31";
        for($i = 1; $i <= $lastday; $i++) {
            $opt[] = array('label' => sprintf("%02d", $i).'日', 'value' => sprintf("%02d", $i));
        }
        $Row["HOLIDAY_DAY"] = ($Row["HOLIDAY_DAY"]) ? $Row["HOLIDAY_DAY"] : "";
        $extra = ($Row["HOLIDAY_DIV"] == "1") ? "" : "disabled";
        $arg["data"]["HOLIDAY_DAY"] = knjCreateCombo($objForm, "HOLIDAY_DAY", $Row["HOLIDAY_DAY"], $opt, $extra, 1);

        //週コンボボックス
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        for($i = 1; $i <= 5; $i++) {
            $opt[] = array('label' => $i.'週目', 'value' => $i);
        }
        $Row["HOLIDAY_WEEK_PERIOD"] = ($Row["HOLIDAY_WEEK_PERIOD"]) ? $Row["HOLIDAY_WEEK_PERIOD"] : "";
        $extra = ($Row["HOLIDAY_DIV"] == "2") ? "" : "disabled";
        $arg["data"]["HOLIDAY_WEEK_PERIOD"] = knjCreateCombo($objForm, "HOLIDAY_WEEK_PERIOD", $Row["HOLIDAY_WEEK_PERIOD"], $opt, $extra, 1);

        //曜日コンボボックス
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $week = array("1" => '日', "2" => '月', "3" => '火', "4" => '水', "5" => '木', "6" => '金', "7" => '土');
        foreach($week as $key => $val) {
            $opt[] = array('label' => $val.'曜日', 'value' => $key);
        }
        $Row["HOLIDAY_WEEKDAY"] = ($Row["HOLIDAY_WEEKDAY"]) ? $Row["HOLIDAY_WEEKDAY"] : "";
        $extra = ($Row["HOLIDAY_DIV"] == "2") ? "" : "disabled";
        $arg["data"]["HOLIDAY_WEEKDAY"] = knjCreateCombo($objForm, "HOLIDAY_WEEKDAY", $Row["HOLIDAY_WEEKDAY"], $opt, $extra, 1);

        //祝祭日名テキストボックス
        $extra = "onkeypress=\"btn_keypress();\"";
        $arg["data"]["HOLIDAY_NAME"] = knjCreateTextBox($objForm, $Row["HOLIDAY_NAME"], "HOLIDAY_NAME", 50, 50, $extra);

        //公休日区分(1:公休日, 2:学校設定公休日)
        if ($model->Properties["useHOLIDAY_KIND"] == "1") {
            $arg["useHOLIDAY_KIND"] = "1";
            $opt = array(1, 2);
            $Row["HOLIDAY_KIND"] = ($Row["HOLIDAY_KIND"] == "") ? "1" : $Row["HOLIDAY_KIND"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"HOLIDAY_KIND{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "HOLIDAY_KIND", $Row["HOLIDAY_KIND"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        } else {
            $arg["useHOLIDAY_KIND"] = "";
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEQ", $model->seq);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "reset" && $model->cmd != "change"){
            if (!isset($model->warning)) {
                $arg["reload"]  = "parent.left_frame.location.href='knjs510index.php?cmd=list';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjs510Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
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
