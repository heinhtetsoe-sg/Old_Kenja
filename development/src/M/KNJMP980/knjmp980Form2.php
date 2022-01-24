<?php

require_once('for_php7.php');

class knjmp980Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjmp980index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            if ($model->cmd === 'bank') {
                $Row =& $model->field;
            } else {
                $Row = knjmp980Query::getRow($model, $model->levy_l_cd);
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["LEVY_L_CD"] = knjCreateTextBox($objForm, $Row["LEVY_L_CD"], "LEVY_L_CD", 2, 2, $extra);

        //名称
        $extra = "";
        $arg["data"]["LEVY_L_NAME"] = knjCreateTextBox($objForm, $Row["LEVY_L_NAME"], "LEVY_L_NAME", 60, 60, $extra);

        //略称
        $extra = "";
        $arg["data"]["LEVY_L_ABBV"] = knjCreateTextBox($objForm, $Row["LEVY_L_ABBV"], "LEVY_L_ABBV", 60, 60, $extra);

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
            $arg["reload"]  = "window.open('knjmp980index.php?cmd=list','left_frame');";
        }
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp980Form2.html", $arg);
    }
}
?>
