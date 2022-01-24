<?php

require_once('for_php7.php');

class knjl324gForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl324gForm1", "POST", "knjl324gindex.php", "", "knjl324gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $extra = "onchange=\"return btn_submit('knjl324g');\"";
        $query = knjl324gQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('knjl324g');\"";
        $query = knjl324gQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //受験型コンボボックス
        $query = knjl324gQuery::getExamType($model->ObjYear, $model->field["TESTDIV"]);
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->field["EXAM_TYPE"], "", 1);

        //合格コースコンボボックス
        $extra = "";
        $query = knjl324gQuery::getExamCouse($model->ObjYear, $model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOUSE", $model->field["EXAMCOUSE"], $extra, 1, "all");

        //専併区分コンボボックス
        $extra = "";
        $query = knjl324gQuery::getNameMst($model->ObjYear, "L006");
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->field["SHDIV"], $extra, 1, "all");

        //合否区分ラジオボタン
        $opt = array(1, 2, 3);
        $model->field["JUDGE"] = ($model->field["JUDGE"] == "") ? "1" : $model->field["JUDGE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"JUDGE{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "JUDGE", $model->field["JUDGE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl324gForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $div = "")
{
    $opt = array();
    if ($div == "all") {
        $opt[] = array('label' => "－全て－",
                       'value' => "all");
    }
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
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL324G");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
