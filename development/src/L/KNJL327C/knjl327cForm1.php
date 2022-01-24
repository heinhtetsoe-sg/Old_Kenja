<?php

require_once('for_php7.php');


class knjl327cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl327cForm1", "POST", "knjl327cindex.php", "", "knjl327cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = " onChange=\"return btn_submit('knjl327c');\"";

        //入試制度コンボの設定
        $query = knjl327cQuery::getApctDiv("L003", $model->ObjYear, $model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボの設定
        $query = knjl327cQuery::getTestDiv("L004", $model->ObjYear, $model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //五條中学は、「専願」コンボをグレーアウトにする
        $disShdiv = ($model->isGojou && $model->field["APPLICANTDIV"] == "1" || $model->isCollege && $model->field["APPLICANTDIV"] == "2") ? " disabled" : "";

        //専併区分コンボの設定
        $div = (($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV"] == "3") || $model->isGojou) ? "" : "1";
        $query = knjl327cQuery::getSHDiv("L006", $model->ObjYear, $div);
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->field["SHDIV"], $disShdiv, 1);

        //通知日付作成
        $model->field["PRINT_DATE"] = $model->field["PRINT_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["PRINT_DATE"];
        $arg["data"]["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", $model->field["PRINT_DATE"]);

        $click = " onClick=\"return btn_submit('knjl327c');\"";

        //帳票種類ラジオボタン 1:合格通知書 2:入学許可書 3:移行合格通知書 4:追加合格通知書 5:補欠合格通知書
        $opt_output = array(1, 2, 3, 4, 5);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click, "id=\"OUTPUT5\"".$click);
        //五條は「6:進学通知書」を追加表示
        if ($model->isGojou || $model->isWakayama) {
            $arg["isGojou"] = 1; //表示切替フラグ
            $opt_output = array(1, 2, 3, 4, 5, 6);
            $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click, "id=\"OUTPUT5\"".$click, "id=\"OUTPUT6\"".$click);
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //五條は「6:進学通知書」を追加表示
        if ($model->isGojou || $model->isWakayama) {
            //出力範囲ラジオボタン 1:手続者全員 2:手続者指定
            $opt_outputF = array(1, 2);
            $model->field["OUTPUTF"] = ($model->field["OUTPUTF"] == "") ? "1" : $model->field["OUTPUTF"];
            if($model->field["OUTPUT"] == "6"){
                $extra_outF = array("id=\"OUTPUTF1\"".$click, "id=\"OUTPUTF2\"".$click);
            } else {
                $extra_outF = "disabled";
            }
            $radioArray = knjCreateRadio($objForm, "OUTPUTF", $model->field["OUTPUTF"], $extra_outF, $opt_outputF, get_count($opt_outputF));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
            //受験番号テキストボックス
            $value = ($model->field["EXAMNOF"]) ? $model->field["EXAMNOF"] : "";
            if($model->field["OUTPUT"] == "6" && $model->field["OUTPUTF"] == "2"){
                $extra_examF = " STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
            } else {
                $extra_examF = " disabled STYLE=\"background-color:#cccccc\"";
            }
            $arg["data"]["EXAMNOF"] = knjCreateTextBox($objForm, $value, "EXAMNOF", 5, 5, $extra_examF);
            //入学日付
            $model->field["SINGAKU_DATE"] = $model->field["SINGAKU_DATE"] == "" ? $model->ObjYear ."/04/01" : $model->field["SINGAKU_DATE"];
            $arg["data"]["SINGAKU_DATE"] = View::popUpCalendar($objForm, "SINGAKU_DATE", $model->field["SINGAKU_DATE"]);
        }

        if ($model->isWakayama && $model->field["APPLICANTDIV"] == "1") {
            $arg["isWakayamaJ"] = 1;
        }
        if ($model->isCollege && $model->field["APPLICANTDIV"] == "1") {
            $arg["isCollegeJ"] = 1;
            $opt = array();
            $opt[] = array('label' => "Sクラス", 'value' => "1");
            $opt[] = array('label' => "Gクラス", 'value' => "2");
            $arg["data"]["SG_CLASS_A"] = knjCreateCombo($objForm, "SG_CLASS_A", $model->field["SG_CLASS_A"], $opt, "", 1);
        }

        //出力範囲ラジオボタン 1:合格者全員 2:志願者全員 3:受験者指定 4:S合格者全員 5:G合格者全員
        if ($model->isWakayama && $model->field["APPLICANTDIV"] == "1") {
            $opt_outputA = array(1, 2, 3, 4, 5);
        } else {
            $opt_outputA = array(1, 2, 3);
        }
        $model->field["OUTPUTA"] = ($model->field["OUTPUTA"] == "") ? "1" : $model->field["OUTPUTA"];
        if ($model->isWakayama && $model->field["APPLICANTDIV"] == "1" && $model->field["OUTPUT"] == "1") {
            $extra_outA = array("id=\"OUTPUTA1\"".$click, "id=\"OUTPUTA2\"".$click, "id=\"OUTPUTA3\"".$click, "id=\"OUTPUTA4\"".$click, "id=\"OUTPUTA5\"".$click);
        } else if ($model->field["OUTPUT"] == "1") {
            $extra_outA = array("id=\"OUTPUTA1\"".$click, "id=\"OUTPUTA2\"".$click, "id=\"OUTPUTA3\"".$click);
        } else {
            $extra_outA = "disabled";
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUTA", $model->field["OUTPUTA"], $extra_outA, $opt_outputA, get_count($opt_outputA));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号テキストボックス
        $value = ($model->field["EXAMNOA"]) ? $model->field["EXAMNOA"] : "";
        if($model->field["OUTPUT"] == "1" && $model->field["OUTPUTA"] == "3"){
            $extra_examA = " STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra_examA = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNOA"] = knjCreateTextBox($objForm, $value, "EXAMNOA", 5, 5, $extra_examA);

        //出力範囲ラジオボタン 1:補欠合格者全員 2:受験者指定
        $opt_outputE = array(1, 2);
        $model->field["OUTPUTE"] = ($model->field["OUTPUTE"] == "") ? "1" : $model->field["OUTPUTE"];
        if($model->field["OUTPUT"] == "5"){
            $extra_outE = array("id=\"OUTPUTE1\"".$click, "id=\"OUTPUTE2\"".$click);
        } else {
            $extra_outE = "disabled";
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUTE", $model->field["OUTPUTE"], $extra_outE, $opt_outputE, get_count($opt_outputE));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //来校日付作成
        $model->field["VISIT_DATE"] = $model->field["VISIT_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["VISIT_DATE"];
        $arg["data"]["VISIT_DATE"] = View::popUpCalendar($objForm, "VISIT_DATE", $model->field["VISIT_DATE"]);

        //来校時間（時）テキストボックス
        $value = ($model->field["VISIT_HOUR"]) ? $model->field["VISIT_HOUR"] : "";
        if($model->field["OUTPUT"] == "5"){
            $extra_visit = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value); return TimeCheck('hour', this)\"";
        } else {
            $extra_visit = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["VISIT_HOUR"] = knjCreateTextBox($objForm, $value, "VISIT_HOUR", 2, 2, $extra_visit);

        //来校時間（分）テキストボックス
        $value = ($model->field["VISIT_MINUTE"]) ? $model->field["VISIT_MINUTE"] : "";
        if($model->field["OUTPUT"] == "5"){
            $extra_visit = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value); return TimeCheck('minute', this)\"";
        } else {
            $extra_visit = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["VISIT_MINUTE"] = knjCreateTextBox($objForm, $value, "VISIT_MINUTE", 2, 2, $extra_visit);

        //受験番号テキストボックス
        $value = ($model->field["EXAMNOE"]) ? $model->field["EXAMNOE"] : "";
        if($model->field["OUTPUT"] == "5" && $model->field["OUTPUTE"] == "2"){
            $extra_examE = " STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra_examE = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNOE"] = knjCreateTextBox($objForm, $value, "EXAMNOE", 5, 5, $extra_examE);

        //出力範囲ラジオボタン 1:移行合格者全員 2:受験者指定
        $opt_outputB = array(1, 2);
        $model->field["OUTPUTB"] = ($model->field["OUTPUTB"] == "") ? "1" : $model->field["OUTPUTB"];
        if($model->field["OUTPUT"] == "3"){
            $extra_outB = array("id=\"OUTPUTB1\"".$click, "id=\"OUTPUTB2\"".$click);
        } else {
            $extra_outB = "disabled";
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUTB", $model->field["OUTPUTB"], $extra_outB, $opt_outputB, get_count($opt_outputB));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号テキストボックス
        $value = ($model->field["EXAMNOB"]) ? $model->field["EXAMNOB"] : "";
        if($model->field["OUTPUT"] == "3" && $model->field["OUTPUTB"] == "2"){
            $extra_examB = " STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra_examB = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNOB"] = knjCreateTextBox($objForm, $value, "EXAMNOB", 5, 5, $extra_examB);

        //移行先テキストボックス
        $value = ($model->field["SHIFT_COURSE"]) ? $model->field["SHIFT_COURSE"] : "";
        $extra_shift = ($model->field["APPLICANTDIV"] == "2" && $model->field["OUTPUT"] == "3") ? "" : " disabled STYLE=\"background-color:#cccccc\"";
        $arg["data"]["SHIFT_COURSE"] = knjCreateTextBox($objForm, $value, "SHIFT_COURSE", 16, 16, $extra_shift);

        //フォーム選択チェックボックス
        if($model->field["OUTPUT"] == "3"){
            $extra_send = ($model->field["SHIFT_SEND"] == "1") ? "checked id=\"SHIFT_SEND\"" : "id=\"SHIFT_SEND\"";
        } else {
            $extra_send = "disabled";
        }
        $arg["data"]["SHIFT_SEND"] = knjCreateCheckBox($objForm, "SHIFT_SEND", "1", $extra_send, "");

        //出力範囲ラジオボタン 1:合格者全員 2:受験者全員 3:受験者指定 4:入学手続者全員
        if ($model->isGojouOnly) {
            $arg["isGojouOnly"] = 1; //表示切替フラグ
            $opt_outputD = array(1, 2, 3, 4);
        } else {
            $opt_outputD = array(1, 2, 3);
        }
        $model->field["OUTPUTD"] = ($model->field["OUTPUTD"] == "") ? "1" : $model->field["OUTPUTD"];
        if($model->field["OUTPUT"] == "2"){
            $extra_outD = array("id=\"OUTPUTD1\"".$click, "id=\"OUTPUTD2\"".$click, "id=\"OUTPUTD3\"".$click, "id=\"OUTPUTD4\"".$click);
        } else {
            $extra_outD = "disabled";
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUTD", $model->field["OUTPUTD"], $extra_outD, $opt_outputD, get_count($opt_outputD));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号テキストボックス
        $value = ($model->field["EXAMNOD"]) ? $model->field["EXAMNOD"] : "";
        if($model->field["OUTPUT"] == "2" && $model->field["OUTPUTD"] == "3"){
            $extra_examD = " STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra_examD = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNOD"] = knjCreateTextBox($objForm, $value, "EXAMNOD", 5, 5, $extra_examD);

        //出力範囲ラジオボタン 1:追加合格者全員 2:受験者指定
        $opt_outputC = array(1, 2);
        $model->field["OUTPUTC"] = ($model->field["OUTPUTC"] == "") ? "1" : $model->field["OUTPUTC"];
        if($model->field["OUTPUT"] == "4"){
            $extra_outC = array("id=\"OUTPUTC1\"".$click, "id=\"OUTPUTC2\"".$click);
        } else {
            $extra_outC = "disabled";
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUTC", $model->field["OUTPUTC"], $extra_outC, $opt_outputC, get_count($opt_outputC));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号テキストボックス
        $value = ($model->field["EXAMNOC"]) ? $model->field["EXAMNOC"] : "";
        if($model->field["OUTPUT"] == "4" && $model->field["OUTPUTC"] == "2"){
            $extra_examC = " STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra_examC = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNOC"] = knjCreateTextBox($objForm, $value, "EXAMNOC", 5, 5, $extra_examC);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl327cForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL327C");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");

    //チェック用
    knjCreateHidden($objForm, "CHECK_OUTPUT", $model->field["OUTPUT"]);
    knjCreateHidden($objForm, "CHECK_OUTPUTA", $model->field["OUTPUTA"]);
    knjCreateHidden($objForm, "CHECK_OUTPUTB", $model->field["OUTPUTB"]);
    knjCreateHidden($objForm, "CHECK_OUTPUTC", $model->field["OUTPUTC"]);
    knjCreateHidden($objForm, "CHECK_OUTPUTD", $model->field["OUTPUTD"]);
    knjCreateHidden($objForm, "CHECK_OUTPUTE", $model->field["OUTPUTE"]);
    knjCreateHidden($objForm, "CHECK_OUTPUTF", $model->field["OUTPUTF"]);

}
?>
