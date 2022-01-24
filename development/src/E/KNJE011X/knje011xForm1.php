<?php

require_once('for_php7.php');

class knje011xForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knje011xindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度学期
        $query = knje011xQuery::getYearSeme();
        $model->field["YEAR_SEME"] = $model->field["YEAR_SEME"] ? $model->field["YEAR_SEME"] : CTRL_YEAR."-".CTRL_SEMESTER;
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR_SEME"], "YEAR_SEME", $extra, 1);

        //年組
        $query = knje011xQuery::getHrClass($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1);

        //入力項目
        $query = knje011xQuery::getInputVal($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["INPUT_VAL"], "INPUT_VAL", $extra, 1);

        $tableNm = $model->inputValArray[$model->field["INPUT_VAL"]]["TABLE_NM"];
        $fieldNm = $model->inputValArray[$model->field["INPUT_VAL"]]["FIELD_NM"];
        $moji_su = $model->inputValArray[$model->field["INPUT_VAL"]]["MOJI_SU"];
        $gyou_su = $model->inputValArray[$model->field["INPUT_VAL"]]["GYOU_SU"];
        $textarea_width  = (int)$moji_su * 2 + 1;
        $textarea_height = (int)$gyou_su * 15 + ((int)$gyou_su-1) * 1 + 2;

        //タイトル
        $arg["head"]["TITLE"] = $model->inputValArray[$model->field["INPUT_VAL"]]["LABEL"];
        $arg["head"]["TITLE_TYUI"] = "(全角{$moji_su}文字X{$gyou_su}行まで)";

        $query = knje011xQuery::getStudentsVal($model, $tableNm, $fieldNm);

        $schregNos = "";
        $sep = "";
        $result = $db->query($query);
        $model->schregInfo = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setData["STUDENTINFO"] = $row["ATTENDNO"]."番　".$row["NAME"];
            $model->schregInfo[$row["SCHREGNO"]] = $setData["STUDENTINFO"];
            $extraShow = " onPaste=\"return showPaste(this, '".$fieldNm."');\" ";
            $extra = "";

            if (isset($model->warning)) {
                $row[$fieldNm] = $model->setFieldData[$row["SCHREGNO"]];
            }

            $extra = "style=\"height:{$textarea_height}px;\"";
            $setData[$fieldNm] = knjCreateTextArea($objForm, $fieldNm."-".$row["SCHREGNO"], $moji_su, $textarea_width, "soft", $extra.$extraShow, $row[$fieldNm]);

            $arg["data"][] = $setData;
            $schregNos .= $sep.$row["SCHREGNO"];
            $sep = ",";
        }

        //ボタン作成
        makeButton($objForm, $arg);
        //Hidden作成
        makeHidden($objForm, $schregNos);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje011xForm1.html", $arg);
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
    $arg["head"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン
function makeButton(&$objForm, &$arg) {
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");

    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden
function makeHidden(&$objForm, $schregNos) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "schregNos", $schregNos);
}
?>
