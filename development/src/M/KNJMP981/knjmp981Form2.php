<?php

require_once('for_php7.php');

class knjmp981Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjmp981index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjmp981Query::getRow($model,1);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //会計科目コードコンボ
        $opt = array();
        $value_flg = false;
        $query = knjmp981Query::getLevyLcd($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["LEVY_L_CD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["LEVY_L_CD"] = ($Row["LEVY_L_CD"] && $value_flg) ? $Row["LEVY_L_CD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["LEVY_L_CD"] = knjCreateCombo($objForm, "LEVY_L_CD", $Row["LEVY_L_CD"], $opt, $extra, 1);

        //会計項目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["LEVY_M_CD"] = knjCreateTextBox($objForm, $Row["LEVY_M_CD"], "LEVY_M_CD", 2, 2, $extra);
        
        //収入支出区分
        $opt = array();
        $value_flg = false;
        $query = knjmp981Query::getLevyInOutDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["LEVY_IN_OUT_DIV"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["LEVY_IN_OUT_DIV"] = ($Row["LEVY_IN_OUT_DIV"] && $value_flg) ? $Row["LEVY_IN_OUT_DIV"] : $opt[0]["value"];
        $extra = "onchange=\"inoutChange();\"";
        $arg["data"]["LEVY_IN_OUT_DIV"] = knjCreateCombo($objForm, "LEVY_IN_OUT_DIV", $Row["LEVY_IN_OUT_DIV"], $opt, $extra, 1);

        //会計項目名称
        $extra = "";
        $arg["data"]["LEVY_M_NAME"] = knjCreateTextBox($objForm, $Row["LEVY_M_NAME"], "LEVY_M_NAME", 40, 60, $extra);
        
        //会計項目略称
        $extra = "";
        $arg["data"]["LEVY_M_ABBV"] = knjCreateTextBox($objForm, $Row["LEVY_M_ABBV"], "LEVY_M_ABBV", 40, 60, $extra);

        //会計細目有無
        $opt = array(1, 2);
        $Row["LEVY_S_EXIST_FLG"] = ($Row["LEVY_S_EXIST_FLG"] == "") ? "2" : $Row["LEVY_S_EXIST_FLG"];
        $extra = array("id=\"LEVY_S_EXIST_FLG1\"", "id=\"LEVY_S_EXIST_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "LEVY_S_EXIST_FLG", $Row["LEVY_S_EXIST_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //1:収入のみ
        //雑収入の設定
        if ($Row["LEVY_IN_OUT_DIV"] === '2') {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        $extra = "id=\"ZATU_FLG\"";
        if ($Row["ZATU_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["ZATU_FLG"] = knjCreateCheckBox($objForm, "ZATU_FLG", "1", $extra.$disabled);
        
        //2:支出のみ
        //予備費の設定
        if ($Row["LEVY_IN_OUT_DIV"] === '1') {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        $extra = "id=\"YOBI_FLG\"";
        if ($Row["YOBI_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["YOBI_FLG"] = knjCreateCheckBox($objForm, "YOBI_FLG", "1", $extra.$disabled);

        //1:収入のみ
        //繰越金の設定
        if ($Row["LEVY_IN_OUT_DIV"] === '2') {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        $extra = "id=\"KURIKOSI_FLG\"";
        if ($Row["KURIKOSI_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["KURIKOSI_FLG"] = knjCreateCheckBox($objForm, "KURIKOSI_FLG", "1", $extra.$disabled);
        

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 60, $extra);

        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリア
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjmp981index.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp981Form2.html", $arg);
    }
}
?>
