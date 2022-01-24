<?php

require_once('for_php7.php');

class knjl415Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl415index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->cmd == "reset" || $model->cmd == "linkClick") {
            $query = knjl415Query::getRecruitSendYmst($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //送付コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EVENT_CLASS_CD"] = knjCreateTextBox($objForm, $Row["EVENT_CLASS_CD"], "EVENT_CLASS_CD", 3, 3, $extra);

        //送付名称
        $extra = "";
        $arg["data"]["EVENT_CLASS_NAME"] = knjCreateTextBox($objForm, $Row["EVENT_CLASS_NAME"], "EVENT_CLASS_NAME", 50, 80, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "linkClick"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl415index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl415Form2.html", $arg); 
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
