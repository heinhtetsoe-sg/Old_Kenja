<?php

require_once('for_php7.php');

class knjl691iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl691iindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["data"]["YEAR"] = $model->examyear;

        //学科コンボ
        $extra = "onchange=\"return btn_submit('main')\"";
        $opt = array();
        $opt[] = array("label" => "1:普通科", "value" => "1");
        $opt[] = array("label" => "2:工業科", "value" => "2");
        $model->testdiv0 = $model->testdiv0 != "" ? $model->testdiv0 : "1";
        $arg["data"]["TESTDIV0"] = knjCreateCombo($objForm, "TESTDIV0", $model->testdiv0, $opt, $extra, 1);

        //試験コンボ
        $query = knjl691iQuery::getTestdivMst($model->examyear, $model->applicantdiv);
        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["data"]["TESTDIV"] = makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");
        $query = knjl691iQuery::getCsvData1($model->examyear, $model->applicantdiv, $model->testdiv0, $model->testdiv);
        
        if ($model->cmd == "match") {
            $arg["data"]["match"]    = $model->match."件";
            $arg["data"]["unmatch1"] = $model->unmatch1."件";
            $arg["data"]["unmatch2"] = $model->unmatch2."件";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL691I");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl691iForm1.html", $arg);
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if ($row["NAMESPARE2"] == '1' && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]	["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実行", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "チェックリスト出力", $extra);
    
    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
