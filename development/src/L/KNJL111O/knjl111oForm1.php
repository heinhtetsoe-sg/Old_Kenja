<?php

require_once('for_php7.php');

class knjl111oForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl111oindex.php", "", "main");
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $query = knjl111oQuery::GetName($model->year, "L003");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl111oQuery::getTestdivMst($model->year);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //受験型コンボ
        $query = knjl111oQuery::GetName($model->year, "L005", "2");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAM_TYPE"], "EXAM_TYPE", $extra, 1, "");

        //受験番号
        $extra = "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->field["EXAMNO"], "EXAMNO", 5, 5, $extra);

        //受付番号
        $query = knjl111oQuery::getRecept($model);
        $setInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->field["RECEPTNO"] = $setInfo["RECEPTNO"];
        knjCreateHidden($objForm, "RECEPTNO", $model->field["RECEPTNO"]);
        $arg["data"]["NAME"] = $setInfo["NAME"];

        $disabled = "";
        if ($model->cmd == "reference" && !$model->field["RECEPTNO"]) {
            $disabled = " disabled ";
            $arg["errAlert"] = "alert('存在しないデータです。');";
        }

        makeMeisai($objForm, $arg, $db, $model);

        //加点(表示のみ)
        $katen = $db->getOne(knjl111oQuery::getKaten($model));
        $arg["data"]["KATEN"] = $katen;

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //更新ボタン
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);

        //削除ボタン
        $extra = "onclick=\"btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disabled);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl111oForm1.html", $arg);
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function makeMeisai(&$objForm, &$arg, $db, $model)
{

    $query = knjl111oQuery::GetName($model->year, "L009");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["KAMOKU".$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();
    //クリア
    foreach ($model->kamoku as $key => $val) {
        $model->meisaiField["SCORE".$key] = "";
    }

    $query = knjl111oQuery::getScoreSpare($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->meisaiField["SCORE".$row["TESTSUBCLASSCD"]] = $row["SCORE1"];
    }
    $result->free();

    foreach ($model->meisaiField as $key => $val) {
        $pos = strpos($key, "AVG");
        if ($pos === false) {
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value);\"";
        }
        if ($key == "SCOREA") {
            $arg["data"][$key] = $val;
        } else {
            $arg["data"][$key] = knjCreateTextBox($objForm, $val, $key, 3, 3, $extra);
        }
    }
}
?>