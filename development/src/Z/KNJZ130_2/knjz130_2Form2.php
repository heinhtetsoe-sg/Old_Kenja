<?php

require_once('for_php7.php');

class knjz130_2Form2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz130_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->namecd1) && isset($model->namecd2) && !isset($model->warning)) {
            $Row = knjz130_2Query::getRow($model->namecd1, $model->namecd2);
            $temp_cd = $Row["NAMECD1"].$Row["NAMECD2"];
        } else {
            $Row =& $model->field;
        }
        
        //名称コード
        $extra = "onblur=\"this.value=toAlphaNumber(this.value)\";";
        $arg["data"]["NAMECD1"] = knjCreateTextBox($objForm, $Row["NAMECD1"], "NAMECD1", 4, 4, $extra);

        //名称区分
        $extra = "onblur=\"this.value=toAlphaNumber(this.value)\";";
        $arg["data"]["NAMECD2"] = knjCreateTextBox($objForm, $Row["NAMECD2"], "NAMECD2", 4, 4, $extra);

        //名称1
        $extra = "";
        $arg["data"]["NAME1"] = knjCreateTextBox($objForm, $Row["NAME1"], "NAME1", 40, 60, $extra);

        //名称2
        $extra = "";
        $arg["data"]["NAME2"] = knjCreateTextBox($objForm, $Row["NAME2"], "NAME2", 40, 60, $extra);

        //名称3
        $extra = "";
        $arg["data"]["NAME3"] = knjCreateTextBox($objForm, $Row["NAME3"], "NAME3", 40, 60, $extra);

        //名称説明1
        $extra = "";
        $arg["data"]["NAME1MEMO"] = knjCreateTextBox($objForm, $Row["NAME1MEMO"], "NAME1MEMO", 40, 60, $extra);

        //名称説明2
        $extra = "";
        $arg["data"]["NAME2MEMO"] = knjCreateTextBox($objForm, $Row["NAME2MEMO"], "NAME2MEMO", 40, 60, $extra);

        //名称説明3
        $extra = "";
        $arg["data"]["NAME3MEMO"] = knjCreateTextBox($objForm, $Row["NAME3MEMO"], "NAME3MEMO", 40, 60, $extra);

        //略称1
        $extra = "";
        $arg["data"]["ABBV1"] = knjCreateTextBox($objForm, $Row["ABBV1"], "ABBV1", 40, 30, $extra);

        //略称2
        $extra = "";
        $arg["data"]["ABBV2"] = knjCreateTextBox($objForm, $Row["ABBV2"], "ABBV2", 40, 30, $extra);

        //略称3
        $extra = "";
        $arg["data"]["ABBV3"] = knjCreateTextBox($objForm, $Row["ABBV3"], "ABBV3", 40, 30, $extra);

        //略称説明1
        $extra = "";
        $arg["data"]["ABBV1MEMO"] = knjCreateTextBox($objForm, $Row["ABBV1MEMO"], "ABBV1MEMO", 40, 60, $extra);

        //略称説明2
        $extra = "";
        $arg["data"]["ABBV2MEMO"] = knjCreateTextBox($objForm, $Row["ABBV2MEMO"], "ABBV2MEMO", 40, 60, $extra);

        //略称説明3
        $extra = "";
        $arg["data"]["ABBV3MEMO"] = knjCreateTextBox($objForm, $Row["ABBV3MEMO"], "ABBV3MEMO", 40, 60, $extra);

        //名称予備1
        $extra = "";
        $arg["data"]["NAMESPARE1"] = knjCreateTextBox($objForm, $Row["NAMESPARE1"], "NAMESPARE1", 40, 30, $extra);

        //名称予備2
        $extra = "";
        $arg["data"]["NAMESPARE2"] = knjCreateTextBox($objForm, $Row["NAMESPARE2"], "NAMESPARE2", 40, 30, $extra);

        //名称予備3
        $extra = "";
        $arg["data"]["NAMESPARE3"] = knjCreateTextBox($objForm, $Row["NAMESPARE3"], "NAMESPARE3", 40, 30, $extra);

        //名称予備説明1
        $extra = "";
        $arg["data"]["NAMESPARE1MEMO"] = knjCreateTextBox($objForm, $Row["NAMESPARE1MEMO"], "NAMESPARE1MEMO", 40, 60, $extra);

        //名称予備説明2
        $extra = "";
        $arg["data"]["NAMESPARE2MEMO"] = knjCreateTextBox($objForm, $Row["NAMESPARE2MEMO"], "NAMESPARE2MEMO", 40, 60, $extra);

        //名称予備説明3
        $extra = "";
        $arg["data"]["NAMESPARE3MEMO"] = knjCreateTextBox($objForm, $Row["NAMESPARE3MEMO"], "NAMESPARE3MEMO", 40, 60, $extra);

        //区分説明
        $extra = "";
        $arg["data"]["CDMEMO"] = knjCreateTextBox($objForm, $Row["CDMEMO"], "CDMEMO", 45, 45, $extra);

        //学校編集可
        $extra = " id=\"MODIFY_FLG\" ";
        $checked = ($Row["MODIFY_FLG"] == "2") ? " checked " : "";
        $arg["data"]["MODIFY_FLG"] = knjCreateCheckBox($objForm, "MODIFY_FLG", "2", $checked.$extra);

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
        $extra = "onclick=\"return Btn_reset('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ130/knjz130index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        if ($temp_cd == "") {
            $temp_cd = $model->field["temp_cd"];
        }
        knjCreateHidden($objForm, "temp_cd", $temp_cd);

        $cd_change = false;
        if ($temp_cd == $Row["NAMECD1"].$Row["NAMECD2"]) {
            $cd_change = true;
        }

        $arg["finish"] = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && ($cd_change == true || $model->isload != "1")) {
            $arg["reload"]  = "window.open('knjz130_2index.php?cmd=list&NAMECD1=$model->namecd1','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz130_2Form2.html", $arg);
    }
}
