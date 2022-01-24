<?php

require_once('for_php7.php');

class knje355Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje355index.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (($model->l_cd != "") && !isset($model->warning) && $model->cmd != 'chenge_cd'&& $model->cmd != 'search'){
            $query = knje355Query::getLmst($model, $model->l_cd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }

        //求人番号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["L_CD"] = knjCreateTextBox($objForm, $Row["L_CD"], "L_CD", 2, 2, $extra);

        //会社名
        $extra = "";
        $arg["data"]["L_NAME"] = knjCreateTextBox($objForm, $Row["L_NAME"], "L_NAME", 50, 50, $extra);

        //会社名かな
        $extra = "";
        $arg["data"]["L_ABBV"] = knjCreateTextBox($objForm, $Row["L_ABBV"], "L_ABBV", 50, 50, $extra);

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
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd'){
            $arg["reload"]  = "parent.left_frame.location.href='knje355index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje355Form2.html", $arg);
    }
}
?>
