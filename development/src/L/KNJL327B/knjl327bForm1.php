<?php

require_once('for_php7.php');

class knjl327bForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl327bForm1", "POST", "knjl327bindex.php", "", "knjl327bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl327bQuery::getNameMst($model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjl327b');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl327bQuery::getNameMst($model->ObjYear, "L004"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["TESTDIV"] = $row["VALUE"];
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjl327b');\"";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //通知日付
        $extra = "";
        $arg["data"]["TSUCHI_DATE"] = View::popUpCalendar2($objForm, "TSUCHI_DATE", str_replace("-", "/", $model->field["TSUCHI_DATE"]), "", "", $extra);

        //1:正規合格、2:単願切換合格
        $opt = array(1, 2);
        //推薦入試のときは正規合格のみ
        if ($model->field["TESTDIV"] === '1') {
            $model->field["GOUKAKU"] = "1";
        } else {
            $arg["ippan"] = "1";
            $model->field["GOUKAKU"] = ($model->field["GOUKAKU"] == "") ? "1" : $model->field["GOUKAKU"];

            //合格コース
            $opt = array();
            $value_flg = false;
            $result = $db->query(knjl327bQuery::getTankiriCourse($model->ObjYear));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
                if ($model->field["EARLY_PASSEXAMCOURSECD"] == $row["VALUE"]) $value_flg = true;
            }
            $opt[] = array('label' => '9999:すべて',
                           'value' => '9999');
            $result->free();
            $model->field["EARLY_PASSEXAMCOURSECD"] = ($model->field["EARLY_PASSEXAMCOURSECD"] && $value_flg) ? $model->field["EARLY_PASSEXAMCOURSECD"] : $opt[0]["value"];
            $extra = "";
            if ($model->field["GOUKAKU"] == '1') $extra .= " disabled ";
            $arg["data"]["EARLY_PASSEXAMCOURSECD"] = knjCreateCombo($objForm, "EARLY_PASSEXAMCOURSECD", $model->field["EARLY_PASSEXAMCOURSECD"], $opt, $extra, 1);
        }
        $extra = array("id=\"GOUKAKU1\" onclick =\" return btn_submit('knjl327b');\"", "id=\"GOUKAKU2\" onclick =\" return btn_submit('knjl327b');\"");
        $radioArray = knjCreateRadio($objForm, "GOUKAKU", $model->field["GOUKAKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //帳票種類ラジオボタン 1:合格通知書 2:単願切換合格通知書 3:入学確約書 4:振込依頼書
        $opt_form = array(1, 2, 3, 4);
        if ($model->field["GOUKAKU"] == "1") {
            $arg["seiki"] = "1";
            $model->field["FORM"] = ($model->field["FORM"] == "2" || $model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        } else {
            $arg["tangan"] = "1";
            $model->field["FORM"] = ($model->field["FORM"] == "1" || $model->field["FORM"] == "") ? "2" : $model->field["FORM"];
        }
        $extra = array("id=\"FORM1\" onclick =\" return btn_submit('knjl327b');\"", "id=\"FORM2\" onclick =\" return btn_submit('knjl327b');\"", "id=\"FORM3\" onclick =\" return btn_submit('knjl327b');\"", "id=\"FORM4\" onclick =\" return btn_submit('knjl327b');\"");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt_form, get_count($opt_form));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（奨学生）出力範囲ラジオボタン 1:全員 2:指定
        $opt_output = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('knjl327b');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('knjl327b');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（奨学生）受験番号指定
        if($model->field["OUTPUT"] == "2"){
            $extra = " STYLE=\"text-align: left\"; onblur=\"this.value=toAlphaNumber(this.value)\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $value = ($model->field["EXAMNO_S"]) ? $model->field["EXAMNO_S"] : "";
        $arg["data"]["EXAMNO_S"] = knjCreateTextBox($objForm, $value, "EXAMNO_S", 5, 5, $extra);
        $value = ($model->field["EXAMNO_E"]) ? $model->field["EXAMNO_E"] : "";
        $arg["data"]["EXAMNO_E"] = knjCreateTextBox($objForm, $value, "EXAMNO_E", 5, 5, $extra);

        //早期入学手続期間（単願切換合格通知書の時）
        if ($model->field["FORM"] == "2") {
            $arg["early"] = "1";
            //自
            $extra = "";
            $arg["data"]["EARLY_S_DATE"] = View::popUpCalendar2($objForm, "EARLY_S_DATE", str_replace("-", "/", $model->field["EARLY_S_DATE"]), "", "", $extra);
            $extra = " onblur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["EARLY_S_TIME"] = knjCreateTextBox($objForm, $model->field["EARLY_S_TIME"], "EARLY_S_TIME", 2, 2, $extra);
            //至
            $extra = "";
            $arg["data"]["EARLY_E_DATE"] = View::popUpCalendar2($objForm, "EARLY_E_DATE", str_replace("-", "/", $model->field["EARLY_E_DATE"]), "", "", $extra);
            $extra = " onblur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["EARLY_E_TIME"] = knjCreateTextBox($objForm, $model->field["EARLY_E_TIME"], "EARLY_E_TIME", 2, 2, $extra);
        } else if ($model->field["FORM"] == "1" || $model->field["FORM"] == "3") {
            $arg["tsujyou"] = "1";
        }
        
        //納入期限（振込依頼書の時）
        if ($model->field["FORM"] == "4") {
            $arg["nounyu"] = "1";
            knjCreateHidden($objForm, "NOUNYU_FLG", "1");
            $extra = "";
            $arg["data"]["NOUNYU_DATE"] = View::popUpCalendar2($objForm, "NOUNYU_DATE", str_replace("-", "/", $model->field["NOUNYU_DATE"]), "", "", $extra);
        } else {
            knjCreateHidden($objForm, "NOUNYU_FLG", "");
        }
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl327bForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL327B");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
