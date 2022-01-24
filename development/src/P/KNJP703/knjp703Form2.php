<?php

require_once('for_php7.php');

class knjp703Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp703index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjp703Query::getRow($model,1);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //会計科目・項目コード
        $opt = array();
        $value_flg = false;
        $query = knjp703Query::getColectCd($model->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["COLLECT_L_M_CD"] = knjCreateCombo($objForm, "COLLECT_L_M_CD", $Row["COLLECT_L_M_CD"], $opt, $extra, 1);

        //会計細目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COLLECT_S_CD"] = knjCreateTextBox($objForm, $Row["COLLECT_S_CD"], "COLLECT_S_CD", 2, 2, $extra);

        //会計細目名称
        $extra = "";
        $arg["data"]["COLLECT_S_NAME"] = knjCreateTextBox($objForm, $Row["COLLECT_S_NAME"], "COLLECT_S_NAME", 40, 40, $extra);

        //金額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COLLECT_S_MONEY"] = knjCreateTextBox($objForm, $Row["COLLECT_S_MONEY"], "COLLECT_S_MONEY", 10, 8, $extra);

        //詳細登録状況取得
        $query = knjp703Query::getRowShousai($model);
        $result = $db->query($query);
        while($rowshousai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($rowshousai["TOKUSYU_VAL"] === '1') {
                $rowshousai["TOKUSYU_VAL_SET"] = $rowshousai["TOKUSYU_VAL"].':'.$rowshousai["NAME2"];
            } else if ($rowshousai["TOKUSYU_VAL"] === '2') {
                $rowshousai["TOKUSYU_VAL_SET"] = $rowshousai["TOKUSYU_VAL"].':'.$rowshousai["NAME3"];
            }
            $arg["shousai"][] = $rowshousai;
        }
        $result->free();

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

        //詳細登録ボタン
        $model->prgId = 'KNJP703';
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP703_DETAIL/knjp703_detailindex.php?cmd=main";
        $subdata .= "&SEND_PRGRID={$model->prgId}&SEND_AUTH={$model->auth}&SEND_YEAR={$model->year}";
        $subdata .= "&SEND_COLLECT_L_M_CD={$model->exp_lmcd}&SEND_COLLECT_S_CD={$model->exp_scd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $arg["button"]["btn_shousai"] = knjCreateBtn($objForm, "btn_shousai", "詳細登録", "onclick=\"$subdata\"".$disBtn);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" || VARS::get("cmd") != "change2"){
            $arg["reload"]  = "window.open('knjp703index.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp703Form2.html", $arg);
    }
}
?>
