<?php

require_once('for_php7.php');


class knjl363cForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl363cForm1", "POST", "knjl363cindex.php", "", "knjl363cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = "onchange=\"OptionUse('this');\"";

        //入試制度コンボの設定
        $query = knjl363cQuery::getApctDiv("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1);

        //入試区分コンボの設定
        $query = knjl363cQuery::getTestDiv("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //出力選択ラジオボタン 1:合格者全員 2:受験者全員 3:受験者指定 4:志願者全員
        $opt_print = array(1, 2, 3, 4);
        $model->field["PRINT_TYPE"] = ($model->field["PRINT_TYPE"] == "") ? "1" : $model->field["PRINT_TYPE"];
        $disabled = ($model->field["APPLICANTDIV"] == '1' && $model->field["TESTDIV"] == '6') ? " disabled" : "";
        $click = " onClick=\"return btn_submit('knjl363c');\"";
        $extra = array("id=\"PRINT_TYPE1\"".$click, "id=\"PRINT_TYPE2\"".$click.$disabled, "id=\"PRINT_TYPE3\"".$click.$disabled, "id=\"PRINT_TYPE4\"".$click.$disabled);
        $radioArray = knjCreateRadio($objForm, "PRINT_TYPE", $model->field["PRINT_TYPE"], $extra, $opt_print, get_count($opt_print));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //合格者チェックボックス 初期値チェック有
        if ($model->cmd == "") {
            $model->field["GOUKAKUSHA"] = "1";
        }
        if ($model->field["GOUKAKUSHA"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["GOUKAKUSHA"] = knjCreateCheckBox($objForm, "GOUKAKUSHA", "1", $extra);

        //受付開始番号（開始）
        $disabled = $model->field["PRINT_TYPE"] != "3" ? " disabled STYLE=\"background-color:#cccccc\"" : "";
        $value = ($model->field["EXAMNO"]) ? $model->field["EXAMNO"] : "";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $value, "EXAMNO", 5, 5, $extra.$disabled);

        //開始位置（行）コンボの設定
        $line[0] = array('label' => "１行", 'value' => 1);
        $line[1] = array('label' => "２行", 'value' => 2);
        $line[2] = array('label' => "３行", 'value' => 3);
        $line[3] = array('label' => "４行", 'value' => 4);
        $line[4] = array('label' => "５行", 'value' => 5);
        $line[5] = array('label' => "６行", 'value' => 6);
        $model->field["LINE"] = ($model->field["LINE"]) ? $model->field["LINE"] : $line[0]["value"];
        $arg["data"]["LINE"] = knjCreateCombo($objForm, "LINE", $model->field["LINE"], $line, "", 1);

        //開始位置（列）コンボの設定
        $row[0] = array('label' => "１列", 'value' => 1);
        $row[1] = array('label' => "２列", 'value' => 2);
        $row[2] = array('label' => "３列", 'value' => 3);
        $model->field["ROW"] = ($model->field["ROW"]) ? $model->field["ROW"] : $row[0]["value"];
        $arg["data"]["ROW"] = knjCreateCombo($objForm, "ROW", $model->field["ROW"], $row, "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl363cForm1.html", $arg);
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

        if($name == "APPLICANTDIV" || $name == "TESTDIV"){
            if ($row["NAMESPARE2"] && $default_flg){
                $default = $i;
                $default_flg = false;
            } else {
                $i++;
            }
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
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJL363C");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHECK_EXAMNO", $model->field["PRINT_TYPE"]);
}

?>
