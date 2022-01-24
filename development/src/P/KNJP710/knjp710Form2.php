<?php

require_once('for_php7.php');

class knjp710Form2 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp710index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            if ($model->cmd == 'prefecturescd') {
                $Row =& $model->field;
            } else {
                $Row = knjp710Query::getRow($model, 1);
            }
        } else {
            $Row =& $model->field;
        }

        //補助区分の値によって表示を変える
        if ($model->reductionTarget == "1") {
            $arg["HOZYOKIN"] = true;
        }

        $db = Query::dbCheckOut();

        $model->year = $model->year ? $model->year : CTRL_YEAR;

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjp710Query::getSchkind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");
        }

        //都道府県コードコンボ
        $query = knjp710Query::getName($model->year, $model->schoolKind);
        $extra = "onChange=\"btn_submit('prefecturescd')\"";
        makeCmb($objForm, $arg, $db, $query, $Row["PREFECTURESCD"], "PREFECTURESCD", $extra, 1, "BLANK");

        //学年コンボ
        $result    = $db->query(knjp710Query::getGrade($model, $model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["GRADE"],
                           "value" => $row["GRADE"]);
        }
        $result->free();
        $extra = "onChange=\"btn_submit('prefecturescd')\"";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $Row["GRADE"], $opt, $extra, 1);

        //学年別都道府県補助金マスタ取得
        $reducPG = $db->getRow(knjp710Query::getReductionPrefGradeMst($model->year, $model->schoolKind, $Row["PREFECTURESCD"], $Row["GRADE"]), DB_FETCHMODE_ASSOC);

        //参照年度表示
        $arg["top"]["LAST_YEAR"] = ($reducPG["REFER_YEAR_DIV1_LABEL"]) ? $reducPG["REFER_YEAR_DIV1_LABEL"] : '未設定';
        $arg["top"]["THIS_YEAR"] = ($reducPG["REFER_YEAR_DIV2_LABEL"]) ? $reducPG["REFER_YEAR_DIV2_LABEL"] : '未設定';

        /********/
        /* 部品 */
        /********/
        for ($i = 1; $i <= 2; $i++) {
            //金額区分ラジオ    1:割合 2:絶対額 3:保護者の負担額
            $opt = array(1, 2, 3);
            $Row["MONEY_DIV".$i] = ($Row["MONEY_DIV".$i] == "") ? "1" : $Row["MONEY_DIV".$i];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"MONEY_DIV{$i}{$val}\" onClick=\"btn_submit('prefecturescd')\"");
            }
            $radioArray = knjCreateRadio($objForm, "MONEY_DIV".$i, $Row["MONEY_DIV".$i], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            $setNumerator = "";
            $setDenominator = "";
            if ($Row["MONEY_DIV".$i] == "1") {
                $arg["data"]["WARIAI_DISP".$i]  = "1";
                $setNumerator   = $Row["NUMERATOR".$i];
                $setDenominator = $Row["DENOMINATOR".$i];
            } else if ($Row["MONEY_DIV".$i] == "2") {
                $arg["data"]["ZETTAI_DISP".$i]  = "1";
            } else if ($Row["MONEY_DIV".$i] == "3") {
                $arg["data"]["HOGOSHA_DISP".$i]  = "1";
            }
            //補助額(割合金額)
            $extraInt = " style=\"text-align:right\" ";
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["NUMERATOR".$i]   = knjCreateTextBox($objForm, $setNumerator,   "NUMERATOR".$i,   3, 3, $extraInt.$extra);
            $arg["data"]["DENOMINATOR".$i] = knjCreateTextBox($objForm, $setDenominator, "DENOMINATOR".$i, 3, 3, $extra);

            //補助額(絶対額・保護者負担額)
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["REDUCTIONMONEY_".$i] = knjCreateTextBox($objForm, $Row["REDUCTIONMONEY_".$i], "REDUCTIONMONEY_".$i, 8, 8, $extra);

            //所得割下限額
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["INCOME_LOW".$i] = knjCreateTextBox($objForm, $Row["INCOME_LOW".$i], "INCOME_LOW".$i, 8, 8, $extra);

            //所得割上限額
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["INCOME_HIGH".$i] = knjCreateTextBox($objForm, $Row["INCOME_HIGH".$i], "INCOME_HIGH".$i, 8, 8, $extra);

            //学校負担金
            $extra = " id=\"BURDEN_CHARGE_FLG{$i}\" ";
            $checked = $Row["BURDEN_CHARGE_FLG".$i] == "1" ? " checked " : "";
            $arg["data"]["BURDEN_CHARGE_FLG".$i] = knjCreateCheckBox($objForm, "BURDEN_CHARGE_FLG".$i, "1", $extra.$checked);

            //ランク
            $query = knjp710Query::getNameMst($model->year, "P002", "");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $Row["INCOME_RANK".$i], "INCOME_RANK".$i, $extra, 1, "BLANK");
        }

        //備考
        $extra = "";
        $arg["data"]["REDUCTIONREMARK"] = knjCreateTextBox($objForm, $Row["REDUCTIONREMARK"], "REDUCTIONREMARK", 50, 50, $extra);

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
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "REDUCTION_TARGET", $model->reductionTarget);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjp710index.php?cmd=list&REDUCTION_TARGET=".$model->reductionTarget."','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp710Form2.html", $arg);
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
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
