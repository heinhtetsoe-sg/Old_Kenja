<?php

require_once('for_php7.php');

class knjp826Form1 {
    function main(&$model) {

        $objForm = new form;

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度<br>入金一括登録・預金口座振替情報ＣＳＶ出力";

        //radio
        $opt = array(1, 2);
        $model->field["CSV_DIV"] = ($model->field["CSV_DIV"] == "") ? "1" : $model->field["CSV_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"CSV_DIV{$val}\" onClick=\"btn_submit('divChange')\"");
        }
        $radioArray = knjCreateRadio($objForm, "CSV_DIV", $model->field["CSV_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;


        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        if ($model->field["CSV_DIV"] == '1' && $model->Properties["use_collect_zengin"] == "1") {
            $arg["DATE_NAME"] = "引落日";
            //引落日コンボボックス
            $query = knjp826query::getDirectDebit($model);
            $extra = " onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "DIRECT_DEBIT", $model->field["DIRECT_DEBIT"], $extra, 1, "");

            //出力フラグ取得
            $model->outPutFlg = $db->getOne(knjp826query::getDirectDebit($model, $model->field["DIRECT_DEBIT"]));
        } else {
            if ($model->field["CSV_DIV"] == '1') {
                $arg["DATE_NAME"] = "引落日";
            } else {
                $arg["DISP_HURIKOMI"] = "1";
                $arg["DATE_NAME"] = "振込日";
            }
            //振込日コンボボックス
            $query = knjp826query::getPaidDate($model);
            $extra = " onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "DIRECT_DEBIT", $model->field["DIRECT_DEBIT"], $extra, 1, "");

            //出力フラグ取得
            $dateArray = explode(":", $model->field["DIRECT_DEBIT"]);
            $model->outPutFlg = $dateArray[1];
            //支払日取得
            $model->paidDate = $dateArray[0];

            //抽出条件コンボ
            if ($model->field["CSV_DIV"] == '2') {
                $extra = "";
                $query = knjp826query::getMonth($model);
                makeCmb($objForm, $arg, $db, $query, "PLAN_MONTH", $model->field["PLAN_MONTH"], $extra, 1, "BLANK");
                $query = knjp826query::getCollectLcd($model);
                makeCmb($objForm, $arg, $db, $query, "COLLECT_L_CD", $model->field["COLLECT_L_CD"], $extra, 1, "BLANK");
            }
        }

        //checkbox
        $extra = " id=\"REDISTER_DISP\" onclick=\"btn_submit('redisterChange')\" ";
        $extra .= $model->field["REDISTER_DISP"] == '1' ? " checked " : "";
        $arg["REDISTER_DISP"] = knjCreateCheckBox($objForm, "REDISTER_DISP", "1", $extra);

        //ボタン作成
        //実行ボタン
        $setCmd = ($model->outPutFlg == '1') ? 'csv': 'insertCsv';
        $extra = "onclick=\"return btn_submit('{$setCmd}');\"";
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
        knjCreateHidden($objForm, "PRGID", "KNJP826");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp826index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp826Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($name == "GRADE_HR_CLASS") {
        $opt[] = array("label" => "(全て出力)", "value" => "");
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
