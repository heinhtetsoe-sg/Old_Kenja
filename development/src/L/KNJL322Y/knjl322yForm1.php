<?php

require_once('for_php7.php');

class knjl322yForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl322yForm1", "POST", "knjl322yindex.php", "", "knjl322yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('knjl322y');\"";
        $query = knjl322yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $model->field["TESTDIV"] = "";
        $namecd  = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $namecd2 = ($model->field["APPLICANTDIV"] == "1") ? "1" : "2"; //推薦入試は表示しない
        $query = knjl322yQuery::getNameMst($namecd, $model->ObjYear, $namecd2);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

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

        //傾斜配点ラジオボタン 1:なし 2:あり
        $opt_keisha = array(1, 2);
        $model->field["KEISHA"] = ($model->field["KEISHA"] == "") ? "1" : $model->field["KEISHA"];
        $extra = array("id=\"KEISHA1\"", "id=\"KEISHA2\"");
        $radioArray = knjCreateRadio($objForm, "KEISHA", $model->field["KEISHA"], $extra, $opt_keisha, get_count($opt_keisha));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl322yForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL322Y");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
