<?php

require_once('for_php7.php');

class knjz081Form2 {

    function main(&$model) {

        //教育委員会チェック
        if (knjz081Query::checkEdboard() != 1) {
            $arg["jscript"] = "OnAuthError();";
        }
        //権限チェック
        if ($model->auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz081index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->edboard_schoolcd) && !isset($model->warning)){
            $Row = knjz081Query::getRow($model->edboard_schoolcd);
        }else{
            $Row =& $model->field;
        }

        //学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EDBOARD_SCHOOLCD"] = knjCreateTextBox($objForm, $Row["EDBOARD_SCHOOLCD"], "EDBOARD_SCHOOLCD", 4, 4, $extra);

        //学校名
        $extra = "";
        $arg["data"]["EDBOARD_SCHOOLNAME"] = knjCreateTextBox($objForm, $Row["EDBOARD_SCHOOLNAME"], "EDBOARD_SCHOOLNAME", 40, 60, $extra);

        //アドレス
        $extra = "STYLE=\"background-color:silver;\" readonly";
        $Row["LINK_ADDR"] = $Row["LINK_ADDR"] != "" ? "******************" : "";
        $arg["data"]["LINK_ADDR"] = knjCreateTextBox($objForm, $Row["LINK_ADDR"], "LINK_ADDR", 30, 30, $extra);

        //DB名
        $extra = "STYLE=\"background-color:silver;\" readonly";
        $Row["DBNAME"] = $Row["DBNAME"] != "" ? "*****" : "";
        $arg["data"]["DBNAME"] = knjCreateTextBox($objForm, $Row["DBNAME"], "DBNAME", 10, 10, $extra);

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
        if ($model->prgid == PROGRAMID) {
            $label = "終 了";
            $extra = "onclick=\"closeWin();\"";
        } else {
            $label = "戻 る";
            $linkIdF = strtoupper(substr($model->prgid,3,1));
            $linkIdU = strtoupper($model->prgid);
            $linkIdL = strtolower($model->prgid);
            $link = REQUESTROOT."/".$linkIdF."/".$linkIdU."/".$linkIdL."index.php?cmd=";
            $extra = "onclick=\"parent.location.href='$link'\"";
        }
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", $label, $extra);
        
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz081index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz081Form2.html", $arg); 
    }
}
?>
