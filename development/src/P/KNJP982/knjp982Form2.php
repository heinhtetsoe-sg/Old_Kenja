<?php

require_once('for_php7.php');

class knjp982Form2
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp982index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjp982Query::getRow($model, 1);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            $sk = $db->getOne(knjp982Query::getSchkind($model));
            $model->schoolKind = (SCHOOLKIND) ? SCHOOLKIND : $sk;
        }
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);

        //会計科目・項目コード
        $opt = array();
        $value_flg = false;
        $query = knjp982Query::getLevyCd($model->year, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["LEVY_L_M_CD"] = knjCreateCombo($objForm, "LEVY_L_M_CD", $Row["LEVY_L_M_CD"], $opt, $extra, 1);

        //会計細目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["LEVY_S_CD"] = knjCreateTextBox($objForm, $Row["LEVY_S_CD"], "LEVY_S_CD", 3, 3, $extra);

        //会計細目名称
        $extra = "";
        $arg["data"]["LEVY_S_NAME"] = knjCreateTextBox($objForm, $Row["LEVY_S_NAME"], "LEVY_S_NAME", 40, 40, $extra);

        //会計項目略称
        $extra = "";
        $arg["data"]["LEVY_S_ABBV"] = knjCreateTextBox($objForm, $Row["LEVY_S_ABBV"], "LEVY_S_ABBV", 40, 60, $extra);

        //会計細目有無
        $opt = array(1, 2);
        $Row["REPAY_DIV"] = ($Row["REPAY_DIV"] == "") ? "2" : $Row["REPAY_DIV"];
        $extra = array("id=\"REPAY_DIV1\"", "id=\"REPAY_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "REPAY_DIV", $Row["REPAY_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //給付対象使用するか
        if ($model->Properties["useBenefit"] == "1") {
            $arg["useBenefit"] = "1";

            //給付対象checkbox
            $checked = ($Row["BENEFIT"] == "1") ? " checked": "";
            $extra = "id=\"BENEFIT\" onClick=\"clicKyuhu(this)\"";
            $arg["data"]["BENEFIT"] = knjCreateCheckBox($objForm, "BENEFIT", "1", $extra.$checked);

            if ($Row["BENEFIT"] == "1") {
                $disabled = "";
            } else {
                $disabled = " disabled";
            }

            //給付金上限値
            $extra  = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["MAX_BENEFIT"] = knjCreateTextBox($objForm, $Row["MAX_BENEFIT"], "MAX_BENEFIT", 8, 8, $extra.$disabled);
        }

        //SIGELシステム使用するか
        if ($model->Properties["useSIGELsystem"] == "1") {
            $arg["useSIGELsystem"] = 1;

            //種別
            $extra = "style=\"text-align:right\" onblur=\"this.value=toAlphaNumber(this.value);\"";
            $arg["data"]["KIND_CD"] = knjCreateTextBox($objForm, $Row["KIND_CD"], "KIND_CD", 3, 3, $extra);

            //種別名称
            $extra = "";
            $arg["data"]["KIND_NAME"] = knjCreateTextBox($objForm, $Row["KIND_NAME"], "KIND_NAME", 40, 60, $extra);

            //内訳費目コード
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["DEPOSIT_CD"] = knjCreateTextBox($objForm, $Row["DEPOSIT_CD"], "DEPOSIT_CD", 3, 3, $extra);
        } else {
            $arg["useSIGELsystem"] = '';
        }

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 60, $extra);

        //追加
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリア
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd" ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year ));

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjp982index.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp982Form2.html", $arg);
    }
}
