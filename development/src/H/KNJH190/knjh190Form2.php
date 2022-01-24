<?php

require_once('for_php7.php');

class knjh190Form2
{
    function main(&$model)
    {
        $arg["reload"] = "";

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh190index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjh190Query::getRow($model->area_cd);
        } else {
            $Row =& $model->field;
        }
        //エリアコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["AREA_CD"] = knjCreateTextBox($objForm, $Row["AREA_CD"], "AREA_CD", 3, 2, $extra);

        //エリア名
        $arg["data"]["AREA_NAME"] = knjCreateTextBox($objForm, $Row["AREA_NAME"], "AREA_NAME", 40, 20, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjh190index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh190Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
}
?>
