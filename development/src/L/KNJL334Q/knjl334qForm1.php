<?php

require_once('for_php7.php');

class knjl334qForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl334qForm1", "POST", "knjl334qindex.php", "", "knjl334qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl334q');\"";
        $query = knjl334qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl334q');\"";
        if (SCHOOLKIND == "P") {
            $query = knjl334qQuery::getNameMst($model->ObjYear, "LP24");
        } else if (SCHOOLKIND == "J") {
            $query = knjl334qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl334qQuery::getNameMstL004($model->ObjYear);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        if (SCHOOLKIND == "P") {
            $arg["disabled_J"] = "";
            $arg["only_J"]     = "";
            $arg["only_P"]     = "1";
            $arg["stylePoint"] = "200";
        } else if (SCHOOLKIND == "J") {
            $arg["disabled_J"] = "";
            $arg["only_J"]     = "1";
            $arg["only_P"]     = "";
            $arg["stylePoint"] = "200";
        } else {
            $arg["disabled_J"] = "1";
            $arg["only_J"]     = "";
            $arg["only_P"]     = "";
            $arg["stylePoint"] = "100";
        }

        //出力対象（1:出身校、2:塾、3:個人）中学入試は（1:出身校、3:個人）
        $opt = array(1, 2, 3);
        $model->field["TAISYOU"] = ($model->field["TAISYOU"] == "") ? "1" : $model->field["TAISYOU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TAISYOU{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "TAISYOU", $model->field["TAISYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        $disabled3 = ($model->field["TAISYOU"] == "3") ? "" :" disabled";
        //個人指定時(1:合格者, 2:不合格者, 3:スカラーシップ希望者, 4:スカラーシップ採用者, 5:保護者)
        $opt = array(1, 2, 3, 4, 5);
        $model->field["KOJIN_SHITEI"] = ($model->field["KOJIN_SHITEI"] == "") ? "1" : $model->field["KOJIN_SHITEI"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"KOJIN_SHITEI{$val}\"".$disabled3);
        }
        $radioArray = knjCreateRadio($objForm, "KOJIN_SHITEI", $model->field["KOJIN_SHITEI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //小学入試は受験番号4桁
        if (SCHOOLKIND == "P") {
            $examNoLength = 4;
        } else {
            $examNoLength = 5;
        }

        //受験番号範囲指定テキスト
        //from
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"".$disabled3;
        $arg["data"]["F_EXAMNO"] = knjCreateTextBox($objForm, $model->field["F_EXAMNO"], "F_EXAMNO", $examNoLength, $examNoLength, $extra);
        //to
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"".$disabled3;
        $arg["data"]["T_EXAMNO"] = knjCreateTextBox($objForm, $model->field["T_EXAMNO"], "T_EXAMNO", $examNoLength, $examNoLength, $extra);

        //行
        $opt = array();
        $value_flg = false;
        $opt[] = array('label' => "１行",'value' => 1);
        $opt[] = array('label' => "２行",'value' => 2);
        $opt[] = array('label' => "３行",'value' => 3);
        $opt[] = array('label' => "４行",'value' => 4);
        $opt[] = array('label' => "５行",'value' => 5);
        $opt[] = array('label' => "６行",'value' => 6);
        $extra = "";
        $arg["data"]["POROW"] = knjCreateCombo($objForm, "POROW", $model->field["POROW"], $opt, $extra, 1);

        //列
        $opt = array();
        $opt[] = array('label' => "１列",'value' => 1);
        $opt[] = array('label' => "２列",'value' => 2);
        $opt[] = array('label' => "３列",'value' => 3);
        $extra = "";
        $arg["data"]["POCOL"] = knjCreateCombo($objForm, "POCOL", $model->field["POCOL"], $opt, $extra, 1);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL334Q");
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl334qForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
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
?>
