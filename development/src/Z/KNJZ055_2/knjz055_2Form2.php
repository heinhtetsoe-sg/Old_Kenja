<?php

require_once('for_php7.php');


class knjz055_2Form2 {

    function main(&$model) {

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz055_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz055_2Query::getRow($model->coursecode);
        } else {
            $Row =& $model->field;
        }

        //コースコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COURSECODE"] = knjCreateTextBox($objForm, $Row["COURSECODE"], "COURSECODE", 4, 4, $extra);

        //コース名称
        $arg["data"]["COURSECODENAME"] = knjCreateTextBox($objForm, $Row["COURSECODENAME"], "COURSECODENAME", 22, 33, "");

        //コース略称
        $arg["data"]["COURSECODEABBV1"] = knjCreateTextBox($objForm, $Row["COURSECODEABBV1"], "COURSECODEABBV1", 20, 40, "");

        //コース略称２
        $arg["data"]["COURSECODEABBV2"] = knjCreateTextBox($objForm, $Row["COURSECODEABBV2"], "COURSECODEABBV2", 20, 40, "");

        //コース略称３
        $arg["data"]["COURSECODEABBV3"] = knjCreateTextBox($objForm, $Row["COURSECODEABBV3"], "COURSECODEABBV3", 20, 40, "");

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
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ055/knjz055index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz055_2Form2.html", $arg); 
    }
}
?>
