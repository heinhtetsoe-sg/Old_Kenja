<?php

require_once('for_php7.php');


class knjl322cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl322cForm1", "POST", "knjl322cindex.php", "", "knjl322cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = " onChange=\"return btn_submit('knjl322c');\"";

        //入試制度コンボの設定
        $query = knjl322cQuery::getApctDiv("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボの設定
        $query = knjl322cQuery::getTestDiv("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //五條中学は、「専願」コンボをグレーアウトにする
        $disShdiv = ($model->isGojou && $model->field["APPLICANTDIV"] == "1" || $model->isCollege && $model->field["APPLICANTDIV"] == "2") ? " disabled" : "";

        //専併区分コンボの設定
        $div = (($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV"] == "3") || $model->isGojou) ? "" : "1";
        $query = knjl322cQuery::getSHDiv("L006", $model->ObjYear, $div);
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->field["SHDIV"], $disShdiv, 1);

        //印刷順序ラジオボタン 1:成績順 2:受験番号順
        $opt_sort = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], "", $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->isWakayama && $model->field["APPLICANTDIV"] == "1") {
            $arg["isWakayamaJ"] = 1;
        }

        //帳票選択チェックボックス
        for($i = 1; $i <= 3; $i++){
            $name = "PRINT_PASS".$i;
            $extra = ($model->field[$name] == "1" || $model->cmd == "" && $i == 1) ? "checked" : "";
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra, "");
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl322cForm1.html", $arg); 
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

    if($name == "TESTDIV"){
        $opt[]= array("label" => "-- 全て --", "value" => "9");
    }

    $result->free();
    $value = (($value && $value_flg) || $value == "9") ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL322C");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
