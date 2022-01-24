<?php

require_once('for_php7.php');


class knjp340Form1 {

    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp340Form1", "POST", "knjp340index.php", "", "knjp340Form1");

        $opt=array();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //帳票種別 1:国 2:府県 3:合計
        $opt = array(1, 2, 3);
        $model->outdiv = ($model->outdiv == "") ? "3" : $model->outdiv;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUT_DIV{$val}\" onClick=\"btn_submit('knjp340')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUT_DIV", $model->outdiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        $extra  = $model->fukenBurden ? " checked " : "";
        $extra .= $model->outdiv == "2" ? "" : " disabled ";
        $extra .= " id=\"FUKEN_BURDEN\" ";
        $arg["data"]["FUKEN_BURDEN"] = knjCreateCheckBox(&$objForm, "FUKEN_BURDEN", "1", $extra);

        $extra  = $model->ryohouBurden ? " checked " : "";
        $extra .= $model->outdiv == "3" ? "" : " disabled ";
        $extra .= " id=\"RYOHOU_BURDEN\" ";
        $arg["data"]["RYOHOU_BURDEN"] = knjCreateCheckBox(&$objForm, "RYOHOU_BURDEN", "1", $extra);

        //クラスを作成する
        $opt_class = array();
        $db = Query::dbCheckOut();

        $result = $db->query(knjp340Query::GetExamClasscd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_class[] = array("label" => $row["NAME"],
                                 "value" => $row["CD"]);
        }
        $result->free();

        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, $opt_class, $extra, 20);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //disabled
        $disOutput        = ($model->outdiv == 1) ? " disabled" : "";
        $disPaidYearMonth = ($model->outdiv == 2) ? " disabled" : "";

        //出力対象者を作成する
        //radio
        $opt = array(1, 2, 3, 4);
        $model->output = ($model->output == "") ? "3" : $model->output;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" {$disOutput}");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //相殺年月コンボボックス
        $query = knjp340Query::getYearMonth();
        $extra = $disPaidYearMonth;
        makeCmb(&$objForm, &$arg, $db, $query, &$model->paidYearMonth, "PAID_YEARMONTH", $extra, 1);

        //所得・都道府県印字チェックボックスを作成する
        $extra = ($model->printPref == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"PRINT_PREF\" ";
        $arg["data"]["PRINT_PREF"] = knjCreateCheckBox($objForm, "PRINT_PREF", "on", $extra);

        //異動日付
        $model->grdDate = $model->grdDate ? $model->grdDate : CTRL_DATE;
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE", str_replace("-", "/", $model->grdDate),"");

        //調整金実行日付
        $AdjustmentDate = $db->getOne(knjp340Query::getAdjustmentDate());
        $arg["data"]["ADJUSTMENT_DATE"] = str_replace("-", "/", $AdjustmentDate);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSV
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP340");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp340Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo(&$objForm, $name, $value, $opt, $extra, $size);
}
?>
