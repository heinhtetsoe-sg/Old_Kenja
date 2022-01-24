<?php

require_once('for_php7.php');

class knjd623aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd623aForm1", "POST", "knjd623aindex.php", "", "knjd623aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["CTRL_YEAR"] = CTRL_YEAR.'年度';

        //学期
        $arg["data"]["CTRL_SEMESTER"] = CTRL_SEMESTERNAME;

        //学期コンボ作成
        $query = knjd623aQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd623a');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ作成
        $query = knjd623aQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "", 1);

        //テスト種別コンボ作成
        $query = knjd623aQuery::getTestkindcd($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1);

        //棒グラフ基準ラジオボタン 1:全教科 2:5教科 3:3教科
        $opt_graph = array(1, 2, 3);
        $model->field["GRAPH"] = ($model->field["GRAPH"] == "") ? "1" : $model->field["GRAPH"];
        $extra = array("id=\"GRAPH1\"", "id=\"GRAPH2\"", "id=\"GRAPH3\"");
        $radioArray = knjCreateRadio($objForm, "GRAPH", $model->field["GRAPH"], $extra, $opt_graph, get_count($opt_graph));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd623aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if($name == "SEMESTER"){
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD623A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "COUNTFLG", $model->testTable);
    knjCreateHidden($objForm, "useCurriculumcd",   $model->Properties["useCurriculumcd"]);
}
?>
