<?php

require_once('for_php7.php');

class knjz285_2Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz285_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz285_2Query::getRow($model->positioncd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //教務主任等コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["POSITIONCD"] = knjCreateTextBox($objForm, $Row["POSITIONCD"], "POSITIONCD", 4, 4, $extra);

        //教務主任等名称
        $extra = "";
        $arg["data"]["POSITIONNAME"] = knjCreateTextBox($objForm, $Row["POSITIONNAME"], "POSITIONNAME", 40, 40, $extra);

        //学校基本調査名称
        $extra = "";
        $arg["data"]["BASE_POSITIONNAME"] = knjCreateTextBox($objForm, $Row["BASE_POSITIONNAME"], "BASE_POSITIONNAME", 40, 20, $extra);

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

        //戻るボタン
        $link = REQUESTROOT."/Z/KNJZ285/knjz285index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjz285_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz285_2Form2.html", $arg); 
    }
}
?>
