<?php

require_once('for_php7.php');

class knjl412Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl412index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->cmd == "reset" || $model->cmd == "linkClick") {
            $query = knjl412Query::getRecruitEventYmst($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //イベントコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EVENT_CD"] = knjCreateTextBox($objForm, $Row["EVENT_CD"], "EVENT_CD", 3, 3, $extra);

        //イベント名称
        $extra = "";
        $arg["data"]["EVENT_NAME"] = knjCreateTextBox($objForm, $Row["EVENT_NAME"], "EVENT_NAME", 50, 80, $extra);

        //イベント略称
        $extra = "";
        $arg["data"]["EVENT_ABBV"] = knjCreateTextBox($objForm, $Row["EVENT_ABBV"], "EVENT_ABBV", 30, 40, $extra);

        //イベント備考
        $extra = "onkeyup =\"charCount(this.value, 2, (40 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (40 * 2), true);\"";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "2", "80", "wrap", $extra, $Row["REMARK"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "linkClick"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl412index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl412Form2.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //修正ボタン
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
