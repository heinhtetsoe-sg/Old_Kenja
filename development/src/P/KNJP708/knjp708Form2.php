<?php

require_once('for_php7.php');

class knjp708Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp708index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjp708Query::getRow($model, $model->reductionDivCd);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        $extraInt = " style=\"text-align:right\" ";

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjp708Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('list');\"";
            makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");
        }

        //コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["REDUCTION_DIV_CD"] = knjCreateTextBox($objForm, $Row["REDUCTION_DIV_CD"], "REDUCTION_DIV_CD", 2, 2, $extraInt.$extra);

        //名称
        $extra = "";
        $arg["data"]["REDUCTION_DIV_NAME"] = knjCreateTextBox($objForm, $Row["REDUCTION_DIV_NAME"], "REDUCTION_DIV_NAME", 30, 30, $extra);

        //特待区分コンボ
        $query = knjp708Query::getScholarshipMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SCHOLARSHIP"], "SCHOLARSHIP", $extra, 1, "BLANK");

        //期間区分
        $query = knjp708Query::getSemester($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SEMESTER_DIV"], "SEMESTER_DIV", $extra, 1, "BLANK");

        //減免種別
        $opt = array(1, 2);
        $Row["REDUCTION_DIV"] = ($Row["REDUCTION_DIV"] == "") ? "1" : $Row["REDUCTION_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"REDUCTION_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "REDUCTION_DIV", $Row["REDUCTION_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //減免割引タイミング
        $opt = array(1, 2);
        $Row["REDUCTION_TIMING"] = ($Row["REDUCTION_TIMING"] == "") ? "2" : $Row["REDUCTION_TIMING"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"REDUCTION_TIMING{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "REDUCTION_TIMING", $Row["REDUCTION_TIMING"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //追加
        $extra = "onclick=\"return btn_submit('add')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);


        /* 減免対象項目欄用データ取得 */
        $model->rowIdArr = array();
        $result = $db->query(knjp708Query::getReductionCollectM($model, $Row["REDUCTION_DIV_CD"]));
        while ($Row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $model->rowIdArr[] = $Row2["REDUCTION_TARGET"];

            //警告メッセージが出た場合はフォーム入力値を保持
            if (isset($model->warning)) {
                 $Row2["MONEY_DIV"]     = $model->field["MONEY_DIV".$Row2["REDUCTION_TARGET"]];
                 $Row2["NUMERATOR"]     = $model->field["NUMERATOR".$Row2["REDUCTION_TARGET"]];
                 $Row2["DENOMINATOR"]   = $model->field["DENOMINATOR".$Row2["REDUCTION_TARGET"]];
                 $Row2["MONEY"]         = $model->field["MONEY".$Row2["REDUCTION_TARGET"]];
            }

            //項目名
            $Row2["COLLECT_NAME"] = $Row2["REDUCTION_TARGET"].":".$Row2["COLLECT_NAME"];
            //金額区分
            $opt = array(1, 2);
            $Row2["MONEY_DIV"] = ($Row2["MONEY_DIV"] == "") ? "1" : $Row2["MONEY_DIV"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"MONEY_DIV".$Row2['REDUCTION_TARGET']."{$val}\" onClick=\"changeDisp('{$val}', '".$Row2["REDUCTION_TARGET"]."')\" ");
            }
            $radioArray = knjCreateRadio($objForm, "MONEY_DIV".$Row2["REDUCTION_TARGET"], $Row2["MONEY_DIV"], $extra, $opt, get_count($opt));
            $Row2["MONEY_DIV1"] = $radioArray["MONEY_DIV".$Row2["REDUCTION_TARGET"]."1"];
            $Row2["MONEY_DIV2"] = $radioArray["MONEY_DIV".$Row2["REDUCTION_TARGET"]."2"];

            $setNumerator = "";
            $setDenominator = "";
            if ($Row2["MONEY_DIV"] == "1") {
                $Row2["PERCENT_DISP"] = " style=\"display:none;\" ";
                $Row2["ZETTAI_DISP"] = " style=\"display:none;\" ";
                $setNumerator = $Row2["NUMERATOR"];
                $setDenominator = $Row2["DENOMINATOR"];
            } else if ($Row2["MONEY_DIV"] == "2") {
                $Row2["WARIAI_DISP"] = " style=\"display:none;\" ";
                $Row2["PERCENT_DISP"] = " style=\"display:none;\" ";
            }
            //割合金額
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $Row2["NUMERATOR"] = knjCreateTextBox($objForm, $setNumerator, "NUMERATOR".$Row2["REDUCTION_TARGET"], 3, 3, $extraInt.$extra);
            $Row2["DENOMINATOR"] = knjCreateTextBox($objForm, $setDenominator, "DENOMINATOR".$Row2["REDUCTION_TARGET"], 3, 3, $extra);
            //絶対額
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $Row2["MONEY"] = knjCreateTextBox($objForm, $Row2["MONEY"], "MONEY".$Row2["REDUCTION_TARGET"], 7, 7, $extraInt.$extra);

            $arg["mlist"][] = $Row2;
        }

        //hidden
        knjCreateHidden($objForm, "cmd");

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjp708index.php?cmd=list','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp708Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
