<?php

require_once('for_php7.php');

class knjwa170Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjwa170index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
//------------------------------------------------------------------------------------------------------------
$opt=array();

//クラス選択コンボボックスを作成する
$query = knjwa170Query::getAuth();
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();

if(!isset($model->field["GRADE_HR_CLASS"])) {
    $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
}
$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE_HR_CLASS",
                    "size"       => "1",
                    "value"      => $model->field["GRADE_HR_CLASS"],
					"extrahtml"  => " onChange=\"return btn_submit('read');\"",
                    "options"    => isset($row1)?$row1:array()));

$arg["data0"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

//対象者リストを作成する
$query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
            "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
            "WHERE (((SCHREG_REGD_DAT.YEAR)='" .CTRL_YEAR ."') AND ".    // 02/10/04 nakamoto
			"((SCHREG_REGD_DAT.SEMESTER)='".CTRL_SEMESTER ."') AND ".
			"((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))".	//  04/07/22  yamauchi
			"ORDER BY ATTENDNO";
//print($query);
$result = $db->query($query);

while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
      //選択済にあったら外す
      if (!in_array($row["SCHREGNO"],$model->select_data[0])){
          $opt1[]= array('label' =>  $row["NAME"],
                    'value' => $row["SCHREGNO"]);
      }
}
$result->free();
//選択済データは対象者リストに残す
unset($opt2);
foreach ($model->select_data[0] as $val){
   $query  = "SELECT ";
   $query .= "    SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO,";
   $query .= "    SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME"; 
   $query .= " FROM";
   $query .= "    SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO";
   $query .= " WHERE";
   $query .= "    SCHREG_REGD_DAT.SCHREGNO='".$val."' ";
   $result = $db->query($query);

      if($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
         $opt2[]= array('label' =>  $row["NAME"],
                    'value' => $row["SCHREGNO"]);
      }
}

//print_r($opt1);
//対象者リストを作成する
$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                    "size"       => "10",
                    "options"    => isset($opt2)?$opt2:array()));

$arg["data0"]["CATEGORY_NAME"] = $objForm->ge("category_name");

//生徒一覧リストを作成する
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                    "size"       => "10",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data0"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

//対象取り消しボタンを作成する(個別)
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "　＞　",
                    "extrahtml"   => " onclick=\"move('right');\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

//対象取り消しボタンを作成する(全て)
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right2",
                    "value"       => "　≫　",
                    "extrahtml"   => " onclick=\"move('rightall');\"" ) );

$arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

//対象選択ボタンを作成する(個別)
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "　＜　",
                    "extrahtml"   => " onclick=\"move('left');\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

//対象選択ボタンを作成する(全て)
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left2",
                    "value"       => "　≪　",
                    "extrahtml"   => " onclick=\"move('leftall');\"" ) );

$arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

  
//------------------------------------------------------------------------------------------------------------
        //印刷指示
        makePrintSendData($objForm, $arg, $db, $model, $param);

        //生徒データ表示
        //if ($model->cmd == "search" || $model->cmd == "change" || $model->cmd == "monthChange") {
        if ($model->cmd == "read2" || $model->cmd == "change" || $model->cmd == "monthChange") {
            //$checkCnt = 20;
            //$cnt = $db->getOne(knjwa170Query::getStudentInfoCnt($model));
            //if ($checkCnt < $cnt) {
            //    $model->setWarning("検索結果：".$cnt."件です。\\n表示可能件数は".$checkCnt."件までです。");
            //} else {
                makeStudentInfo($objForm, $arg, $db, $model);
            //}
        }
        //ヶ月
        $query = knjwa170Query::getNameMst($model);
        $extra = " onChange=\"return btn_submit('monthChange');\"";
        makeCombo($objForm, $arg, $db, $query, $model->month_div, "MONTH_DIV", $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $arg, $model);
        //CSV用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        
        $arg["print"] = $model->print == "on" ? "newwin('" . SERVLET_URL . "');" :"";
        $model->print = "off";
        $model->print_field = array();
        //print_r($arg);
        View::toHTML($model, "knjwa170Form1.html", $arg);
    }
}
//---------------------------------------------------------------------------------------------------------------
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["div"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//---------------------------------------------------------------------------------------------------

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, $model)
{
    $index = 0;
    foreach ($model->select_data[0] as $val){
        //$query = knjwa170Query::getStudentInfoData($model);
        $query = knjwa170Query::getStudentInfoData2($val);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);

        //チェックボックス
        $checkVal = $index.":".$row["SCHREGNO"];
        $extra = in_array($checkVal, $model->checkBoxVal) ? "checked " : "";
        $row["PRINT_CHECK"] = knjcreateCheckBox($objForm, "PRINT_CHECK", $checkVal, $extra, "1");

        //印刷指定が生徒証と生徒証以外で表示
        //if ($model->print_div == "1") {
            //テキストボックス
        //    $extra = "onblur=\"isDate(this);\"";
        //    $row["ENT_DATE"] = knjcreateTextBox($objForm, str_replace("-", "/", $row["ENT_DATE"]), "ENT_DATE[]", 12, 12, $extra);
        //    $row["GRD_SCHEDULE_DATE"] = knjcreateTextBox($objForm, str_replace("-", "/", $row["GRD_SCHEDULE_DATE"]), "GRD_SCHEDULE_DATE[]", 12, 12, $extra);

        //} else {
            //テキストボックス
            $row["STATION_FROM"] = knjcreateTextBox($objForm, $row["STATION_FROM"], "STATION_FROM[]", 10, 15, "");
            $row["STATION_TO"]   = knjcreateTextBox($objForm, $row["STATION_TO"], "STATION_TO[]", 10, 15, "");
            $row["STATION_VIA"]  = knjcreateTextBox($objForm, $row["STATION_VIA"], "STATION_VIA[]", 10, 15, "");
        //}

        $arg["data"][] = $row;
        
        $index++;
    }
    $result->free();
    
    
}

//印刷指示
function makePrintSendData(&$objForm, &$arg, $db, &$model, $param)
{
    $arg["PRINT_DIV2"] = "1";
    //有効期間開始日付
    $model->str_date = $model->str_date ? $model->str_date : CTRL_DATE;
    $arg["div"]["STR_DATE"] = View::popUpCalendar($objForm, "STR_DATE", str_replace("-", "/", $model->str_date));

    //有効期間終了日付
    $sDate = str_replace("-", "/", $model->str_date);
    $date = preg_split("/\//", $sDate);
    list($year, $month, $day) = $date;

        //次月(単純に月に＋１)
        $date2 = mktime (0, 0 , 0 , (int)$month + (int)$model->month_div, $day, $year);
        $addMonth = (int)$month + (int)$model->month_div;
        $nowMonth = date("m", $date2);
        $setMonth = (int)$nowMonth;
        if ($addMonth < $setMonth) {
            /*
            * 2008/05/31に1月足すと
            * 2008/07/01になる。
            * この場合、2008/06/30とする。
            */
            $setMonth = $addMonth;
            $d2Last = date("t", mktime( 0, 0, 0, $setMonth, 1, $year ));
        } else {
            /*
            * 2008/05/30に1月足すと
            * 2008/06/30になる。
            * この場合、2008/06/29とする。
            */
            $d2Last = $day - 1;
        }
        $hogeDay = mktime (0, 0 , 0 , $setMonth, $d2Last, $year);
        $model->end_date = date("Y/m/d", $hogeDay);

        //開始より終了が小さい場合（年+１）2008/12/02 → 2009/01/01
        if ($model->end_date < $model->str_date) {
            $hogeDay = mktime (0, 0 , 0 , $setMonth, $d2Last, (int)$year + 1);
            $model->end_date = date("Y/m/d", $hogeDay);
        }

    $arg["div"]["END_DATE"] = View::popUpCalendar($objForm, "END_DATE", str_replace("-", "/", $model->end_date));

    //発行日付
    $model->print_date = $model->print_date ? $model->print_date : CTRL_DATE;
    $arg["div"]["DATE"] = View::popUpCalendar($objForm, "DATE", str_replace("-", "/", $model->print_date));

}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //読み込み
    $extra = "onclick=\"return btn_submit('read2');\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込み", $extra);
    
    //発行
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "発 行", $extra);
    }

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, &$arg, $model)
{
    knjCreateHidden($objForm, "cmd");
    
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "PROGRAMID", "KNJWA170");
    knjCreateHidden($objForm, "PRGID", "KNJWA170");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "EXE_YEAR", $model->search["EXE_YEAR"]);
    
    
    
    //印刷に渡すパラメータ
    if ($model->print_field) {
        //学籍番号
        //foreach ($model->print_field[0] as $val) {
        //    $arg["data1"][] = $val;
        //}
        //証明書連番（生徒証）
        //foreach ($model->print_field[1] as $val) {
        //    $arg["data2"][] = $val;
        //}
        //証明書連番（通学証）
        foreach ($model->print_field[2] as $val) {
            $arg["data3"][] = $val;
        }
        //証明書連番（運賃割引証）
        //foreach ($model->print_field[3] as $val) {
        //    $arg["data4"][] = $val;
        //}
    }
    
    //印刷指定
    if ($model->print == "on") {
        //switch ($model->print_div) {
            //生徒証
            //case "1":
            //    createHidden($objForm, "CHECK_SEITO", "1");
            //    createHidden($objForm, "TYPE", "9");
            //    break;
            //通学証明書
            //case "2":
                knjCreateHidden($objForm, "CHECK_TUGAKU", "1");
                knjCreateHidden($objForm, "TYPE", "2");
            //    break;
            //学校学生生徒旅客運賃割引証
            //case "3":
            //    createHidden($objForm, "CHECK_UNTIN", "1");
            //    createHidden($objForm, "TYPE", "2");
            //    break;
        //}
    }
}

?>
