<?php

require_once('for_php7.php');

class knjc161aForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjc161aForm1", "POST", "knjc161aindex.php", "", "knjc161aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        
        $extra = " onchange=\"return btn_submit('knjc161a')\"";
        
        $query = knjc161aQuery::getSemesterMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field['SETSEMESTER'], "SETSEMESTER", $extra, 1);
        
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field['SETSEMESTER'] == $row["VALUE"]) {
                $Sdate=substr($row['SDATE'],5,2);
                $Edate=substr($row['EDATE'],5,2);
                break;
            }
        }
        $extra = "";
        makeMonthCmb($objForm, $arg, $model->field["SMONTH"], "SMONTH", $extra, 1, $Sdate, $Edate);
        makeMonthCmb($objForm, $arg, $model->field["EMONTH"], "EMONTH", $extra, 1, $Sdate, $Edate);

        //ボタン作成
        makeBtn($objForm, $arg);

        //1:指定月 2:通年
        $opt = array(1, 2);
        $model->field["OUTPUT_SELECT"] = ($model->field["OUTPUT_SELECT"] == "") ? "1" : $model->field["OUTPUT_SELECT"];
        $extra = array("id=\"OUTPUT_SELECT1\" onclick=\"return btn_submit('knjc161a')\"", "id=\"OUTPUT_SELECT2\" onclick=\"return btn_submit('knjc161a')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_SELECT", $model->field["OUTPUT_SELECT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //欠席日数テキストボックス
        $extra  = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right;\"";
        $arg["data"]["SICK"] = knjCreateTextBox($objForm, $model->field["SICK"], "SICK", 3, 3, $extra);
        //朝礼欠テキストボックス
        $extra  = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right;\"";
        $arg["data"]["CHOREIKETSU"] = knjCreateTextBox($objForm, $model->field["CHOREIKETSU"], "CHOREIKETSU", 3, 3, $extra);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc161aForm1.html", $arg);
    }
}
/******************************************* 以下関数 *****************************************************/
//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSVボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//makeMonthCmb
function makeMonthCmb(&$objForm, &$arg, &$value, $name, $extra, $size, $Sdate, $Edate)
{
    $Sdate = intval($Sdate);
    $Edate = intval($Edate);
    
    if ($Sdate <= 3) {
    	$Sdate += 12;
    }
    if ($Edate <= 3) {
    	$Edate += 12;
    }
    $opt = array();
    if($Sdate<=4 && $Edate>=4){
        $opt[] = array("label" => " 4月", "value" => "04");
    }
    if($Sdate<=5 && $Edate>=5){
        $opt[] = array("label" => " 5月", "value" => "05");
    }
    if($Sdate<=6 && $Edate>=6){
        $opt[] = array("label" => " 6月", "value" => "06");
    }
    if($Sdate<=7 && $Edate>=7){
        $opt[] = array("label" => " 7月", "value" => "07");
    }
    if($Sdate<=8 && $Edate>=8){
        $opt[] = array("label" => " 8月", "value" => "08");
    }
    if($Sdate<=9 && $Edate>=9){
        $opt[] = array("label" => " 9月", "value" => "09");
    }
    if($Sdate<=10 && $Edate>=10){
        $opt[] = array("label" => "10月", "value" => "10");
    }
    if($Sdate<=11 && $Edate>=11){
        $opt[] = array("label" => "11月", "value" => "11");
    }
    if($Sdate<=12 && $Edate>=12){
        $opt[] = array("label" => "12月", "value" => "12");
    }
    if($Sdate<=13 && $Edate>=13){
        $opt[] = array("label" => " 1月", "value" => "01");
    }
    if($Sdate<=14 && $Edate>=14){
        $opt[] = array("label" => " 2月", "value" => "02");
    }
    if($Sdate<=15 && $Edate>=15){
        $opt[] = array("label" => " 3月", "value" => "03");
    }
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}


//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", str_replace("-","/",CTRL_DATE));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJC161A");
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
?>
