<?php

require_once('for_php7.php');

class knjm710lForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm710lindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            if ($model->cmd === 'bank') {
                $Row =& $model->field;
            } else {
                $Row = knjm710lQuery::getRow($model->collect_l_cd);
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COLLECT_L_CD"] = knjCreateTextBox($objForm, $Row["COLLECT_L_CD"], "COLLECT_L_CD", 2, 2, $extra);

        //名称
        $extra = "";
        $arg["data"]["COLLECT_L_NAME"] = knjCreateTextBox($objForm, $Row["COLLECT_L_NAME"], "COLLECT_L_NAME", 60, 60, $extra);

        //略称
        $extra = "";
        $arg["data"]["COLLECT_L_ABBV"] = knjCreateTextBox($objForm, $Row["COLLECT_L_ABBV"], "COLLECT_L_ABBV", 60, 60, $extra);
        
        //収入対象科目フラグ
        if ($Row["LEVY_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["LEVY_FLG"] = knjCreateCheckBox($objForm, "LEVY_FLG", "1", $extra);

        
        //追加
        $extra = "onclick=\"return btn_submit('add')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
        
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", CTRL_YEAR);
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjm710lindex.php?cmd=list','left_frame');";
        }
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm710lForm2.html", $arg);
    }
}
?>
