<?php

require_once('for_php7.php');

class knjz281Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz281index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz281Query::getRow($model->jobcd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //職名コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JOBCD"] = knjCreateTextBox($objForm, $Row["JOBCD"], "JOBCD", 4, 4, $extra);

        //職名名称
        $extra = "";
        $arg["data"]["JOBNAME"] = knjCreateTextBox($objForm, $Row["JOBNAME"], "JOBNAME", 40, 20, $extra);

        //学校基本調査名称
        $extra = "";
        $arg["data"]["BASE_JOBNAME"] = knjCreateTextBox($objForm, $Row["BASE_JOBNAME"], "BASE_JOBNAME", 40, 20, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjz281index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz281Form2.html", $arg); 
    }
}
?>
