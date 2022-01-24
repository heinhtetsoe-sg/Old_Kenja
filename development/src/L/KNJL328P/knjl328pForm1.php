<?php

require_once('for_php7.php');

class knjl328pForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl328pForm1", "POST", "knjl328pindex.php", "", "knjl328pForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $query = knjl328pQuery::getNameMst($model->ObjYear, "L003");
        $extra = " onchange=\"return btn_submit('knjl328p');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl328pQuery::getNameMst($model->ObjYear, $namecd1);
        $extra = " onchange=\"return btn_submit('knjl328ptestdiv');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //通知日付
        $model->field["TSUCHI"] = ($model->field["TSUCHI"]) ? $model->field["TSUCHI"] : str_replace('-', '/', CTRL_DATE);
        $arg["data"]["TSUCHI"] = View::popUpCalendarAlp($objForm, "TSUCHI", $model->field["TSUCHI"], $disabled, "");

        //帳票種類（1:全校, 2:指定）
        $opt = array(1, 2);
        $model->field["ALLFLG"] = ($model->field["ALLFLG"] == "") ? "1" : $model->field["ALLFLG"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"ALLFLG{$val}\" onClick=\"disAbled(this);\"");
        }
        $radioArray = knjCreateRadio($objForm, "ALLFLG", $model->field["ALLFLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["ALLFLG"] == "1") {
            $flgDisabled = "disabled";
            $flgDisabledJizen = "";
        } else {
            $flgDisabled = "";
            $flgDisabledJizen = "disabled";
        }

        //事前印刷checkbox
        $extra  = ($model->field["JIZEN"] || $model->cmd == "") ? " checked " : "";
        $extra .= "id=\"JIZEN\"" .$flgDisabledJizen;
        $arg["data"]["JIZEN"] = knjCreateCheckBox($objForm, "JIZEN", "1", $extra);

        //出身学校コード
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"" .$flgDisabled;
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $model->field["FINSCHOOLCD"], "FINSCHOOLCD", 7, 7, $extra);

        if ($model->cmd == '' || $model->cmd == 'knjl328ptestdiv') {
            //合格者用 締切日付、不合格者用 出願期間のデフォルト値
            $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L044" : "L045";
            $query = knjl328pQuery::getNameMst($model->ObjYear, $namecd1);
            $result = $db->query($query);
            $abbv1 = "";
            $abbv2 = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["TESTDIV"] == $row["VALUE"]) {
                    $abbv1 = $row["ABBV1"];
                    $abbv2 = $row["ABBV2"];
                }
            }
            $result->free();
            $model->field["SHIMEKIRI"] = $abbv1;
            $model->field["SHUTSUGAN"] = $abbv2;
        }

        //合格者用 締切日付
        $model->field["SHIMEKIRI"] = ($model->field["SHIMEKIRI"]) ? $model->field["SHIMEKIRI"] : str_replace('-', '/', CTRL_DATE);
        $arg["data"]["SHIMEKIRI"] = View::popUpCalendarAlp($objForm, "SHIMEKIRI", $model->field["SHIMEKIRI"], "", "");

        //不合格者用 出願期間
        $model->field["SHUTSUGAN"] = ($model->field["SHUTSUGAN"]) ? $model->field["SHUTSUGAN"] : str_replace('-', '/', CTRL_DATE);
        $arg["data"]["SHUTSUGAN"] = View::popUpCalendarAlp($objForm, "SHUTSUGAN", $model->field["SHUTSUGAN"], "", "");

        //文書番号textbox
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["DOCUMENTNO"] = knjCreateTextBox($objForm, $model->field["DOCUMENTNO"], "DOCUMENTNO", 6, 6, $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJL328P");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl328pForm1.html", $arg); 
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

        if ($value === $row["VALUE"]) $value_flg = true;

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
