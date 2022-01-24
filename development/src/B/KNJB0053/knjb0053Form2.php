<?php

require_once('for_php7.php');

class knjb0053Form2
{
    function main(&$model) {
    
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjb0053index.php", "", "edit");

    //警告メッセージを表示しない場合
    if (!isset($model->warning) && $model->cmd != "knjb0053Form2") {
        $Row = knjb0053Query::getRow($model);
    } else {
        $Row =& $model->field;
    }
    $db = Query::dbCheckOut();
    
    //コード
    $extra = "onblur=\"this.value=toInteger(this.value)\";";
    $arg["data"]["RIREKI_CODE"] = knjCreateTextBox($objForm, $Row["RIREKI_CODE"], "RIREKI_CODE", 2, 2, $extra);

    //登録名称
    $extra = "";
    $arg["data"]["SELECT_NAME"] = knjCreateTextBox($objForm, $Row["SELECT_NAME"], "SELECT_NAME", 60, 30, $extra);

    //登録日付
    $arg["data"]["SELECT_DATE"] = View::popUpCalendar($objForm, "SELECT_DATE", str_replace("-","/",$Row["SELECT_DATE"]),"");

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

    //hiddenを作成する
    knjCreateHidden($objForm, "cmd");

    $arg["finish"]  = $objForm->get_finish();

    if (VARS::get("cmd") != "edit"){
        $arg["reload"]  = "window.open('knjb0053index.php?cmd=list','left_frame');";
    }

    Query::dbCheckIn($db);
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjb0053Form2.html", $arg);
    }
}
?>
