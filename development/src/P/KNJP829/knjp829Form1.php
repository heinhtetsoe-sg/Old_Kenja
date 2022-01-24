<?php

require_once('for_php7.php');

class knjp829Form1 {
    function main(&$model) {

        $objForm = new form;

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."<br>奨学費等ＣＳＶ出力";

        $model->yearMonthArray = array();
        $model->yearMonthArray[] = array('label' => "", 'value' => "");
        for ($month = 4; $month <= 15; $month++) {
            $setMonth = $month <= 12 ? $month : ($month - 12);
            $model->yearMonthArray[] = array('label' => sprintf("%02d", $setMonth)."月", 'value' => $month);
        }

        //開始日付
        $model->field["FROM_MONTH"] = ($model->field["FROM_MONTH"] != "") ? $model->field["FROM_MONTH"] : $model->yearMonthArray[0]["value"];
        $extra = "";
        $arg["data"]["FROM_MONTH"] = knjCreateCombo($objForm, "FROM_MONTH", $model->field["FROM_MONTH"], $model->yearMonthArray, $extra, 1);

        //終了日付
        $model->field["TO_MONTH"] = ($model->field["TO_MONTH"] != "") ? $model->field["TO_MONTH"] : $model->yearMonthArray[0]["value"];
        $extra = "";
        $arg["data"]["TO_MONTH"] = knjCreateCombo($objForm, "TO_MONTH", $model->field["TO_MONTH"], $model->yearMonthArray, $extra, 1);

        //コンボボックス
        $query = knjp829Query::getSemester();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "BLANK");

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";

        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //ボタン作成
        //実行ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP829");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp829index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp829Form1.html", $arg);
    }
}
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
