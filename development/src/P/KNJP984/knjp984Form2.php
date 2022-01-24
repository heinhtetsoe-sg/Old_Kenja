<?php

require_once('for_php7.php');

class knjp984Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp984index.php", "", "edit");

        $db = Query::dbCheckOut();
        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            $sk = $db->getOne(knjp984Query::getSchkind($model));
            $model->schoolKind = (SCHOOLKIND) ? SCHOOLKIND : $sk;
        }
        Query::dbCheckIn($db);

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->levygroupcd)){
            $Row = knjp984Query::getRow($model, $model->levygroupcd);
            $temp_cd = $Row["LEVY_GROUP_CD"];
        }else{
            $Row =& $model->field;
            $temp_cd = "";
        }

        //グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["LEVY_GROUP_CD"] = knjCreateTextBox($objForm, $Row["LEVY_GROUP_CD"], "LEVY_GROUP_CD", 4, 4, $extra);

        //グループ名
        $extra = "";
        $arg["data"]["LEVY_GROUP_NAME"] = knjCreateTextBox($objForm, $Row["LEVY_GROUP_NAME"], "LEVY_GROUP_NAME", 40, 60, $extra);

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

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        if ($temp_cd=="" && isset($model->field["temp_cd"])) $temp_cd = $model->field["temp_cd"];
        knjCreateHidden($objForm, "temp_cd", $temp_cd);

        $cd_change = false;                                                                               
        if ($temp_cd==$Row["LEVY_GROUP_CD"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != "1")){
            $arg["reload"]  = "parent.left_frame.location.href='knjp984index.php?cmd=list&SCHOOL_KIND={$model->schoolKind}';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp984Form2.html", $arg);
    }
} 
?>
