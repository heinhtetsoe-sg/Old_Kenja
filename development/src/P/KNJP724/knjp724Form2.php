<?php

require_once('for_php7.php');

class knjp724Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp724index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)) {
            if ($model->cmd == 'prefecturescd') {
                $Row =& $model->field;
            } else {
                $Row = knjp724Query::getRow($model,1);
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        /******************/
        /* コンボボックス */
        /******************/
        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjp724Query::getSchkind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");
        }

        //都道府県コードコンボ
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        $query = knjp724Query::getNameMst($model->year, "G202");
        $extra = "onChange=\"btn_submit('prefecturescd')\"";
        makeCmb($objForm, $arg, $db, $query, "PREFECTURESCD", $Row["PREFECTURESCD"], $extra, 1, "");

        //学年コンボ
        $query = knjp724Query::getGrade($model, $model->year);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $Row["GRADE"], $extra, 1, "");

        for ($i = 1; $i <= 2; $i++) {
            //参照年度
            $query = knjp724Query::getNameMst($model->year, "P003");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "REFER_YEAR_DIV".$i, $Row["REFER_YEAR_DIV".$i], $extra, 1, "");
        }

        /********************/
        /* テキストボックス */
        /********************/
        //標準授業料
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["STANDARD_SCHOOL_FEE"] = knjCreateTextBox($objForm, $Row["STANDARD_SCHOOL_FEE"], "STANDARD_SCHOOL_FEE", 8, 8, $extra);

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

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjp724index.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp724Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
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
