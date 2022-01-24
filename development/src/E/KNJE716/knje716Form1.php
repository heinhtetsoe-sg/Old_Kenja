<?php

require_once('for_php7.php');

class knje716Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knje716index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組コンボボックス
        $extra = "onChange=\"return btn_submit('change_header');\"";
        $query = knje716Query::getGradeHrclassName();
        makeCmb($objForm, $arg, $db, $query, "GRADE_HRCLASS_NAME", $model->field["GRADE_HRCLASS_NAME"], $extra, 1);

        //出席番号・氏名・成績スリップ所見入力
        $query   = knje716Query::getStudentDate($model);
        $result  = $db->query($query);
        $rows    = "4";
        $cols    = "50";
        $extra   = "";
        $counter = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //生徒の所見欄の初期値設定
            $textareaValue = (!isset($model->warning) && $model->cmd != "value_set") ? $row["SLIPREMARK"] : $model->field["SLIPREMARK"][$counter];
            //生徒番号のHidden作成
            $arg["data"]["SCHREGNO" . "-" . $counter] = knjCreateHidden($objForm, "SCHREGNO" . "-" . $counter, $row["SCHREGNO"]);
            //所見欄のTextarea生成
            $row["item"] = KnjCreateTextArea($objForm, "SLIPREMARK" . "-" . $counter, $rows, $cols, "soft", $extra, $textareaValue);
            $arg["studentData"][] = $row;
            $counter++;
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        $arg["data"]["cmd"] = knjCreateHidden($objForm, "cmd");
        $arg["data"]["studentCount"] = knjCreateHidden($objForm, "studentCount", get_count($arg["studentData"]));

        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje716Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick =\" return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick =\" return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank != "") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
