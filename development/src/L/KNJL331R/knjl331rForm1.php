<?php

require_once('for_php7.php');


class knjl331rForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl331rForm1", "POST", "knjl331rindex.php", "", "knjl331rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["ENTEXAMYEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl331rQuery::getNameMst($model, $model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //2:中学固定とする
            if ($row["VALUE"] != "2") {
                continue;
            }

            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl331r');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //出力範囲ラジオボタン 1:合格者 2:入学者
        $opt_printDiv = array(1, 2);
        $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? "1" : $model->field["PRINT_DIV"];
        $extra = array("id=\"PRINT_DIV1\"", "id=\"PRINT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt_printDiv, get_count($opt_printDiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl331rForm1.html", $arg); 
	}
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
    $value_flg = false;
    $default = 0;
    $i = 0;
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
    $value = (($value && $value_flg) || $value == "9") ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSV出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL331R");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
