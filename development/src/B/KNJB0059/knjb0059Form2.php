<?php

require_once('for_php7.php');

class knjb0059Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjb0059index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //extra
        $extraInteger = "onblur=\"this.value=toInteger(this.value)\"";
        $extraRight = "STYLE=\"text-align: right\"";

        if ($model->cmd == "reset") {
            $query = knjb0059Query::getPatternRow($model, $model->groupcd);
            $patternData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["PATTERN_CD"] = $patternData["PATTERN_CD"];
            $model->field["PATTERN_NAME"] = $patternData["PATTERN_NAME"];
        }

        //履修パターンコード
        if (VARS::post("cmd") == "edit") $model->field["PATTERN_CD"] = $model->patterncd;    //コンボ変更時所持データ設定
        $arg["PATTERN_CD"] = knjCreateTextBox($objForm, $model->field["PATTERN_CD"], "PATTERN_CD", 4, 2, $extraRight.$extraInteger);

        //履修パターン名称
        if (VARS::post("cmd") == "edit") $model->field["PATTERN_NAME"] = $model->patternname;   //コンボ変更時所持データ設定
        $arg["PATTERN_NAME"] = knjCreateTextBox($objForm, $model->field["PATTERN_NAME"], "PATTERN_NAME", 50, 50, "");

        //データ設定＆リストToリスト
        makeDataList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //履修リスト保持設定
        $Row = array();

        //hidden
        makeHidden($objForm, $Row, $model);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "add" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjb0059index.php?cmd=list','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjb0059Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {

    //追加ボタン
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    }
    //更新ボタン
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    }
    //削除ボタン
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    }
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//コンボDB作成
function makeCombo(&$objForm, &$arg, $db, $query, $value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array ("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//Hidden作成
function makeHidden(&$objForm, $Row, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata", $Row["SUBCLASS_CD"]);
    knjCreateHidden($objForm, "selectdata2");
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "GROUPCD", $model->groupcd);
}

//リストToリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model) {

    //履修パターン科目一覧
    $credits_sum = 0;
    $chaircd = $option = array();
    $chk = array();
    $result = $db->query(knjb0059Query::GetGroup($model, $model->groupcd));
    $model->subclassArray = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setCheckVal = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
        $setName = "PATTERN_CHK".$setCheckVal;
        $extra = $row["PATTERN_CD"] != '999' ? " checked " : "";
        $extra .= "onclick=\"credit_keisan(this, '{$row["CREDITS"]}');\"";
        $row["PATTERN_CHK"] = knjCreateCheckBox($objForm, "PATTERN_CHK".$setCheckVal, "1", $extra, "");

        if ($row["PATTERN_CD"] != '999') {
            $credits_sum += (int)$row["CREDITS"];
        }
        $model->subclassArray[] = $setCheckVal;
        $row["SUBCLASSNAME"] = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"];
        if ($row["REQUIRE_FLG"] === "3") {
            $row["REQUIRE_NAME"] = '選';
        } else if ($row["REQUIRE_FLG"] === "1" || $row["REQUIRE_FLG"] === "2") {
            $row["REQUIRE_NAME"] = '必';
        } else {
            $row["REQUIRE_NAME"] = '';
        }

        $arg["data"][] = $row;
    }
    $result->free();
    //単位計
    knjCreateHidden($objForm, "CREDITS_SUM", $credits_sum);
    $arg["CREDITS_SUM"] = $credits_sum;
}
?>
