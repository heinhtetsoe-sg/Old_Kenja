<?php

require_once('for_php7.php');

class knjm710Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm710index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjm710Query::getRow($model,1);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //会計科目コードコンボ
        $opt = array();
        $value_flg = false;
        $query = knjm710Query::getCollectLcd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["COLLECT_L_CD"] = knjCreateCombo($objForm, "COLLECT_L_CD", $Row["COLLECT_L_CD"], $opt, $extra, 1);

        //会計項目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COLLECT_M_CD"] = knjCreateTextBox($objForm, $Row["COLLECT_M_CD"], "COLLECT_M_CD", 2, 2, $extra);

        //会計項目名称
        $extra = "";
        $arg["data"]["COLLECT_M_NAME"] = knjCreateTextBox($objForm, $Row["COLLECT_M_NAME"], "COLLECT_M_NAME", 40, 60, $extra);

        //会計細目有無
        $opt = array(1, 2);
        $Row["COLLECT_S_EXIST_FLG"] = ($Row["COLLECT_S_EXIST_FLG"] == "") ? "2" : $Row["COLLECT_S_EXIST_FLG"];
        $extra = array("id=\"COLLECT_S_EXIST_FLG1\"", "id=\"COLLECT_S_EXIST_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "COLLECT_S_EXIST_FLG", $Row["COLLECT_S_EXIST_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //授業料FLG
        $checked = $Row["IS_JUGYOURYOU"] == "1" ? " checked " : "";
        $extra = " id=\"IS_JUGYOURYOU\" ";
        $arg["data"]["IS_JUGYOURYOU"] = knjCreateCheckBox($objForm, "IS_JUGYOURYOU", "1", $checked.$extra);

        //金額
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COLLECT_M_MONEY"] = knjCreateTextBox($objForm, $Row["COLLECT_M_MONEY"], "COLLECT_M_MONEY", 10, 8, $extra);

        //支払区分
        $opt = array(1, 2);
        $Row["PAY_DIV"] = ($Row["PAY_DIV"] == "") ? "1" : $Row["PAY_DIV"];
        $extra = array("id=\"PAY_DIV1\"", "id=\"PAY_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "PAY_DIV", $Row["PAY_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //日付（1:自動振替日　2:納入期限）
        $due_date = str_replace("-", "/", $Row["PAY_DATE"]);
        $arg["data"]["PAY_DATE"] = View::popUpCalendar($objForm, "PAY_DATE", $due_date);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 40, $extra);

        //詳細登録状況取得
        $query = knjm710Query::getRowShousai($model);
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
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリア
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //詳細登録ボタン
        $model->prgId = 'KNJM710';
        $subdata  = "wopen('".REQUESTROOT."/M/KNJM710_DETAIL/knjm710_detailindex.php?cmd=main";
        $subdata .= "&SEND_PRGRID={$model->prgId}&SEND_AUTH={$model->auth}&SEND_YEAR={$model->year}";
        $subdata .= "&SEND_COLLECT_L_CD={$model->exp_lcd}&SEND_COLLECT_M_CD={$model->exp_mcd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $arg["button"]["btn_shousai"] = knjCreateBtn($objForm, "btn_shousai", "詳細登録", "onclick=\"$subdata\"".$disBtn);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjm710index.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm710Form2.html", $arg);
    }
}
?>
