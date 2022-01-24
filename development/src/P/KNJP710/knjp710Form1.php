<?php

require_once('for_php7.php');

class knjp710Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp710index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $opt = array();
        $value_flg = false;
        $query = knjp710Query::getYear();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->year == $row["VALUE"]) $value_flg = true;
        }
        $model->year = ($model->year && $value_flg) ? $model->year : CTRL_YEAR;
        $extra = "onChange=\"btn_submit('changeYear')\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt, $extra, 1);

        //校種コンボ
        $query = knjp710Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //補助区分ラジオ    1:授業料等 2:入学金
        $opt = array("1", "2");
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"REDUCTION_TARGET{$val}\" onClick=\"btn_submit('reductionTarget'); rightReload(".$val.");\"");
        }
        $radioArray = knjCreateRadio($objForm, "REDUCTION_TARGET", $model->reductionTarget, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //補助区分の値によって表示を変える
        if ($model->reductionTarget == "1") {
            $arg["HOZYOKIN"] = true;
        } else if ($model->reductionTarget == "2") {
            $arg["NYUUGAKUKIN"] = true;
        }

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $result = $db->query(knjp710Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["PREFECTURESCD"] = View::alink("knjp710index.php", $row["PREFECTURESCD"], "target=\"right_frame\"",
                                                array("cmd"              => "edit",
                                                      "PREFECTURESCD"    => $row["PREFECTURESCD"],
                                                      "GRADE"            => $row["GRADE"],
                                                      "YEAR"             => $model->year,
                                                      "REDUCTION_SEQ"    => $row["REDUCTION_SEQ"],
                                                      "REDUCTION_TARGET" => $model->reductionTarget
                                                      ));

            for ($i = 1; $i <= 2; $i++) {
                //金額区分によって表示を変える
                $row["REDUCTIONMONEY_".$i] = ($row["MONEY_DIV".$i] == "1") ? $row["NUMERATOR".$i]." / ".$row["DENOMINATOR".$i] : $row["REDUCTIONMONEY_".$i];

                //金額をカンマ区切りにする
                if ($row["MONEY_DIV".$i] == "2" || $row["MONEY_DIV".$i] == "3") {
                    //絶対額・保護者負担額
                    $row["REDUCTIONMONEY_".$i] = (strlen($row["REDUCTIONMONEY_".$i])) ? number_format($row["REDUCTIONMONEY_".$i]): "";
                }
                $row["INCOME_LOW".$i]  = (strlen($row["INCOME_LOW".$i]))  ? number_format($row["INCOME_LOW".$i]): "";
                $row["INCOME_HIGH".$i] = (strlen($row["INCOME_HIGH".$i])) ? number_format($row["INCOME_HIGH".$i]): "";
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && (VARS::post("cmd") == "copy" || VARS::post("cmd") == "changeYear")) {
            $arg["reload"] = "parent.right_frame.location.href='knjp710index.php?cmd=edit"
                           . "&year=".$model->year
                           . "&REDUCTION_TARGET=".$model->reductionTarget."';";
        }
        if ($model->cmd == "changeKind") {
            $arg["jscript"] = "window.open('knjp710index.php?cmd=edit&REDUCTION_TARGET=".$model->reductionTarget."','right_frame');";
        }
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp710Form1.html", $arg);
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
