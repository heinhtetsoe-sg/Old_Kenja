<?php

require_once('for_php7.php');

class knjp707Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp707index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjp707Query::getRow($model, $model->collectPatternCd);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjp707Query::getSchkind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");
        }

        //入金パターンコード
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COLLECT_PATTERN_CD"] = knjCreateTextBox($objForm, $Row["COLLECT_PATTERN_CD"], "COLLECT_PATTERN_CD", 2, 2, $extra);

        //パターン名
        $extra = "";
        $arg["data"]["COLLECT_PATTERN_NAME"] = knjCreateTextBox($objForm, $Row["COLLECT_PATTERN_NAME"], "COLLECT_PATTERN_NAME", 62, 90, $extra);

        //引き落とし日
        $extra = "style=\"text-align:right\" onblur=\"checkDate(this);\"";
        $arg["data"]["DIRECT_DEBIT_DATE"] = knjCreateTextBox($objForm, $Row["DIRECT_DEBIT_DATE"], "DIRECT_DEBIT_DATE", 2, 2, $extra);

        //入金方法
        $opt = array(1, 2, 3, 4);
        $Row["PAY_DIV"] = ($Row["PAY_DIV"] == "") ? "1" : $Row["PAY_DIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"PAY_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "PAY_DIV", $Row["PAY_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //全て
        $opt   = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        foreach ($model->monthArray as $key2 => $val2) {
            $opt[] = array('label' => $val2,
                           'value' => $val2);
        }
        $extra = " onChange=\"checkedMethod(this)\"";
        $arg["data"]["ALL_CHECK"] = knjCreateCombo($objForm, "ALL_CHECK", "", $opt, $extra, 1);

        //入金月設定
        foreach ($model->monthArray as $key => $val) {
            $setName = "COLLECT_MONTH_{$val}";
            $opt   = array();
            $opt[] = array('label' => "", 'value' => "");
            $value_flg = false;
            foreach ($model->monthArray as $key2 => $val2) {
                $opt[] = array('label' => $val2,
                               'value' => $val2);
                if ($Row[$setName] == $val2) $value_flg = true;
            }
            $Row[$setName] = ($Row[$setName] && $value_flg) ? $Row[$setName] : $opt[0]["value"];
            $extra = "";
            $arg["data"][$setName] = knjCreateCombo($objForm, $setName, $Row[$setName], $opt, $extra, 1);
        }

        Query::dbCheckIn($db);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjp707index.php?cmd=list','left_frame');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp707Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
