<?php

require_once('for_php7.php');

class knjl321fForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl321fForm1", "POST", "knjl321findex.php", "", "knjl321fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $model->field["APPLICANTDIV"] = "1";
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl321fQuery::getNameMst($model->ObjYear, "L003", $model->field["APPLICANTDIV"]));

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('knjl321f');\"";
        $query = knjl321fQuery::getNameMst($model->ObjYear, "L024");
        makeCmb($objForm, $model, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //受験型コンボボックス
        $query = knjl321fQuery::getExamType($model->ObjYear, $model->field["TESTDIV"]);
        makeCmb($objForm, $model, $arg, $db, $query, "EXAM_TYPE", $model->field["EXAM_TYPE"], "", 1);

        //出力順
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["TESTDIV"] == "5") {
            $arg["testdiv5"] = "1";
            $arg["data"]["TESTDIV5_NAME"] = $model->testdivname;
            //第5回 出力対象
            $opt = array(1, 2, 3);
            $model->field["TESTDIV5OUT"] = ($model->field["TESTDIV5OUT"] == "") ? "3" : $model->field["TESTDIV5OUT"];
            $extra = array("id=\"TESTDIV5OUT1\"", "id=\"TESTDIV5OUT2\"", "id=\"TESTDIV5OUT3\"");
            $radioArray = knjCreateRadio($objForm, "TESTDIV5OUT", $model->field["TESTDIV5OUT"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl321fForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$model, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $value_name1 = "";
    $i = $default = 0;
    $default_flg = true;
    $default_name1 = "";
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) { $value_flg = true; $value_name1 = $row["NAME1"];}

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
            $default_name1 = $row["NAME1"];
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    if ($name == "TESTDIV") {
        $model->testdivname = ($value && $value_flg) ? $value_name1 : $default_name1;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'pdf');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSVボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL321F");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
