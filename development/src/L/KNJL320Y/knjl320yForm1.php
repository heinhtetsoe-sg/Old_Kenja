<?php

require_once('for_php7.php');

class knjl320yForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl320yForm1", "POST", "knjl320yindex.php", "", "knjl320yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $model->field["APPLICANTDIV"] = "2";
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl320yQuery::getNameMst($model->ObjYear, "L003", $model->field["APPLICANTDIV"]));

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('change');\"";
        $query = knjl320yQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //志望区分コンボボックス
        $query = knjl320yQuery::getDesireDiv($model);
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->field["DESIREDIV"], "", 1);

        //対象者ラジオボタン 1:外部生のみ 2:内部生のみ 3:全て
        $opt = array(1, 2, 3);
        $model->field["INOUT"] = ($model->field["INOUT"]) ? $model->field["INOUT"] : "1";
        $extra = array("id=\"INOUT1\"", "id=\"INOUT2\"", "id=\"INOUT3\"");
        $radioArray = knjCreateRadio($objForm, "INOUT", $model->field["INOUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
        $opt = array(1, 2);
        $model->field["KIKOKU"] = ($model->field["APPLICANTDIV"] != "1" && $model->field["INOUT"] != "2" && $model->field["KIKOKU"]) ? $model->field["KIKOKU"] : "1";
        $disKikoku = ($model->field["APPLICANTDIV"] != "1" && $model->field["INOUT"] != "2") ? "" : "disabled";
        $extra = array("id=\"KIKOKU1\" {$disKikoku}", "id=\"KIKOKU2\" {$disKikoku}");
        $radioArray = knjCreateRadio($objForm, "KIKOKU", $model->field["KIKOKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //氏名出力チェックボックス
        $extra  = ($model->field["OUTPUT_NAME"] == "1") ? "checked" : "";
        $extra .= " id=\"OUTPUT_NAME\"";
        $arg["data"]["OUTPUT_NAME"] = knjCreateCheckBox($objForm, "OUTPUT_NAME", "1", $extra, "");

        //備考１出力チェックボックス
        $extra  = ($model->field["OUTPUT_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"OUTPUT_REMARK1\"";
        $arg["data"]["OUTPUT_REMARK1"] = knjCreateCheckBox($objForm, "OUTPUT_REMARK1", "1", $extra, "");

        //特別奨学生ラジオボタン 1:学力 2:スポーツ生
        $opt_sp = array(1, 2);
        $model->field["SPECIAL"] = ($model->field["SPECIAL"] == "") ? "1" : $model->field["SPECIAL"];
        $extra = array("id=\"SPECIAL1\"", "id=\"SPECIAL2\"");
        $radioArray = knjCreateRadio($objForm, "SPECIAL", $model->field["SPECIAL"], $extra, $opt_sp, get_count($opt_sp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順ラジオボタン 1:成績順 2:受験番号順
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //傾斜配点出力ラジオボタン 1:する 2:しない
        $opt = array(1, 2);
        $model->field["RATE_DIV"] = ($model->field["RATE_DIV"]) ? $model->field["RATE_DIV"] : "2";
        $extra = array("id=\"RATE_DIV1\"", "id=\"RATE_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "RATE_DIV", $model->field["RATE_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl320yForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    if($name == "DESIREDIV") {
        $opt[] = array('label' => '9:全て', 'value' => '9');
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL320Y");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
