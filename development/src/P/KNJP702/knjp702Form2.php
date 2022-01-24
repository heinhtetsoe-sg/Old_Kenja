<?php

require_once('for_php7.php');
class knjp702Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp702index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjp702Query::getRow($model,1);
        }else{
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            $sk = $db->getOne(knjp702Query::getSchkind($model));
            $model->schoolKind = (SCHOOLKIND) ? SCHOOLKIND : $sk;
        }
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);

        //入金科目コードコンボ
        $query = knjp702Query::getCollectLcd($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["COLLECT_L_CD"], "COLLECT_L_CD", $extra, 1, "");

        //入金項目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COLLECT_M_CD"] = knjCreateTextBox($objForm, $Row["COLLECT_M_CD"], "COLLECT_M_CD", 2, 2, $extra);

        //入金項目名称
        $extra = "";
        $arg["data"]["COLLECT_M_NAME"] = knjCreateTextBox($objForm, $Row["COLLECT_M_NAME"], "COLLECT_M_NAME", 40, 60, $extra);

        //年額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COLLECT_M_MONEY"] = knjCreateTextBox($objForm, $Row["COLLECT_M_MONEY"], "COLLECT_M_MONEY", 8, 8, $extra);

        if ($model->Properties["useSIGELsystem"] == "1") {
            //checkbox
            $checked = $Row["SGL_ITEM"] == "1" ? " checked " : "";
            $extra = " id=\"SGL_ITEM\" ";
            $arg["data"]["SGL_ITEM"] = knjCreateCheckBox($objForm, "SGL_ITEM", "1", $checked.$extra);
        }

        //公/私費区分
        $opt = array(1, 2);
        $Row["KOUHI_SHIHI"] = ($Row["KOUHI_SHIHI"] == "") ? "1" : $Row["KOUHI_SHIHI"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"KOUHI_SHIHI{$val}\" onClick=\"disKouhiShihi({$val});\"");
        }
        $radioArray = knjCreateRadio($objForm, "KOUHI_SHIHI", $Row["KOUHI_SHIHI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //公/私費選択時disabled設定
        $kouhiDis = ($Row["KOUHI_SHIHI"] == "1") ? " disabled": "";
        $ShihiDis = ($Row["KOUHI_SHIHI"] == "2") ? " disabled": "";

        //学納金区分
        $query = knjp702Query::getGakunokinDiv($model);
        $extra = "".$ShihiDis;
        makeCmb($objForm, $arg, $db, $query, $Row["GAKUNOKIN_DIV"], "GAKUNOKIN_DIV", $extra, 1, "BLANK");

        //支援・補助区分
        $opt = array();
        $opt[] = array("label" => "",               "value" => "");
        $opt[] = array("label" => "支援金＋補助金", "value" => "1");
        $opt[] = array("label" => "補助金",         "value" => "2");
        $opt[] = array("label" => "入学金補助",     "value" => "3");
        $extra = "".$ShihiDis;
        $arg["data"]["REDUCTION_DIV"] = knjCreateCombo($objForm, "REDUCTION_DIV", $Row["REDUCTION_DIV"], $opt, $extra, 1);

        //学校減免FLG
        $checked = $Row["IS_REDUCTION_SCHOOL"] == "1" ? " checked " : "";
        $extra = " id=\"IS_REDUCTION_SCHOOL\" ".$ShihiDis;
        $arg["data"]["IS_REDUCTION_SCHOOL"] = knjCreateCheckBox($objForm, "IS_REDUCTION_SCHOOL", "1", $checked.$extra);

        //単位数での算出FLG
        $checked = $Row["IS_CREDITCNT"] == "1" ? " checked " : "";
        $extra = " id=\"IS_CREDITCNT\" ".$ShihiDis;
        $arg["data"]["IS_CREDITCNT"] = knjCreateCheckBox($objForm, "IS_CREDITCNT", "1", $checked.$extra);

        //返金FLG
        $checked = $Row["IS_REPAY"] == "1" ? " checked " : "";
        $extra = " id=\"IS_REPAY\" ".$kouhiDis;
        $arg["data"]["IS_REPAY"] = knjCreateCheckBox($objForm, "IS_REPAY", "1", $checked.$extra);

        //対象教科書区分コンボ
        $query = knjp702Query::getTextBookDiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["TEXTBOOKDIV"], "TEXTBOOKDIV", $extra, 1, "BLANK");

        //表示順
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SHOW_ORDER"] = knjCreateTextBox($objForm, $Row["SHOW_ORDER"], "SHOW_ORDER", 3, 2, $extra);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 40, $extra);

        //分割端数
        $opt = array(1, 2, 3);
        $Row["DIVIDE_PROCESS"] = ($Row["DIVIDE_PROCESS"] == "") ? "1" : $Row["DIVIDE_PROCESS"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"DIVIDE_PROCESS{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "DIVIDE_PROCESS", $Row["DIVIDE_PROCESS"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //入金額のまるめ
        $opt = array(1, 2, 3);
        $Row["ROUND_DIGIT"] = ($Row["ROUND_DIGIT"] == "") ? "1" : $Row["ROUND_DIGIT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"ROUND_DIGIT{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "ROUND_DIGIT", $Row["ROUND_DIGIT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //全てcheckbox
        $extra = " id=\"ALL_CHECK\" onClick=\"checkedMethod(this)\" ";
        $arg["data"]["ALL_CHECK"] = knjCreateCheckBox($objForm, "ALL_CHECK", "1", $extra);

        //入金計画
        foreach ($model->monthArray as $key => $val) {
            $setName = "COLLECT_MONTH_{$val}";
            $extra = " id=\"{$setName}\" ";
            $extraChecked = $Row[$setName] == "1" ? " checked " : "";
            $arg["data"][$setName] = knjCreateCheckBox($objForm, $setName, "1", $extra.$extraChecked);
        }

        //詳細登録状況取得
        $query = knjp702Query::getRowShousai($model);
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
        $model->prgId = 'KNJP702';
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP702_DETAIL/knjp702_detailindex.php?cmd=main";
        $subdata .= "&SEND_PRGRID={$model->prgId}&SEND_AUTH={$model->auth}&SEND_YEAR={$model->year}";
        $subdata .= "&SEND_COLLECT_L_CD={$model->exp_lcd}&SEND_COLLECT_M_CD={$model->exp_mcd}";
        $subdata .= "&SEND_SCHOOLKIND={$model->schoolKind}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $arg["button"]["btn_shousai"] = knjCreateBtn($objForm, "btn_shousai", "詳細登録", "onclick=\"$subdata\"".$disBtn);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjp702index.php?cmd=list&SCHOOL_KIND={$model->schoolKind}','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp702Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
