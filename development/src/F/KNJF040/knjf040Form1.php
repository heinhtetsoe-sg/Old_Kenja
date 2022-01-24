<?php

require_once('for_php7.php');


class knjf040Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjf040Form1", "POST", "knjf040index.php", "", "knjf040Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );

//学期名を表示する///////////////////////////////////////////////////////////////////////////////////
$arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

//現在の学期コードをhiddenで送る
$objForm->ae( array("type"      => "hidden",
                    "name"      => "GAKKI",
                    "value"      => CTRL_SEMESTER,
                    ) );


//クラス選択コンボボックス作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjf040Query::getAuth($model,CTRL_YEAR,CTRL_SEMESTER);
/*
$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
            "FROM SCHREG_REGD_HDAT ".
            "WHERE YEAR='" .CTRL_YEAR ."' ".
            "AND SEMESTER='" .CTRL_SEMESTER ."'";     
*/
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

if($model->field["GRADE_HR_CLASS"]=="") $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

$objForm->ae( array("type"		 => "select",
					"name"		 => "GRADE_HR_CLASS",
					"extrahtml"  => " onChange=\"return btn_submit('read');\"",
					"size"		 => "1",
					"value" 	 => $model->field["GRADE_HR_CLASS"],
					"options"	 => isset($row1)?$row1:array()));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");




//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO,SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
            "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
            "WHERE (((SCHREG_REGD_DAT.YEAR)='" .CTRL_YEAR ."') AND ".
			"((SCHREG_REGD_DAT.SEMESTER)='" .CTRL_SEMESTER ."') AND ".
//			"((SCHREG_REGD_DAT.GRADE)='" .substr($model->field["GRADE_HR_CLASS"],0,2) ."') AND ".
//			"((SCHREG_REGD_DAT.HR_CLASS)='" .substr($model->field["GRADE_HR_CLASS"],2,2) ."'))".
			"((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))".	//  04/07/23  yamauchi
			"ORDER BY ATTENDNO";
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt1[]= array('label' =>  $row["NAME"],
                    'value' => $row["SCHREGNO"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=290px\" width=\"290px\" ondblclick=\"move1('left')\"",
                    "size"       => "16",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


//生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=290px\" width=\"290px\" ondblclick=\"move1('right')\"",
                    "size"       => "16",
                    "options"    => array()));

$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


//対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


//健康診断票チェックボックスを作成（一般）////////////////////////////////////////////////////////////////////
if($model->field["CHECK1"] == "on")
{
	$check_1 = "checked";
	$out_disA = "";				// 2003/11/27 nakamoto
}
else
{
	$check_1 = "";
	$out_disA = "disabled";		// 2003/11/27 nakamoto
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK1",
					"value"		=> "on",
//					"extrahtml"	=>$check_1) );
					"extrahtml"	=>"onclick=\"OptionUse2('this');\"".$check_1 ) );	// 2003/11/27 nakamoto

$arg["data"]["CHECK1"] = $objForm->ge("CHECK1");


//出力順ラジオボタンを作成/////////////// 2003/11/27 nakamoto /////////////////////////////////////////
$optA[0]=1;
$optA[1]=2;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUTA",
					"value"      => isset($model->field["OUTPUTA"])?$model->field["OUTPUTA"]:"1",
					"extrahtml"	 => $out_disA,
					"multiple"   => $optA));

$arg["data"]["OUTPUTA1"] = $objForm->ge("OUTPUTA",1);
$arg["data"]["OUTPUTA2"] = $objForm->ge("OUTPUTA",2);


//健康診断票チェックボックスを作成（歯・口腔）///////////////////////////////////////////////////////////////////
if($model->field["CHECK2"] == "on")
{
	$check_2 = "checked";
	$out_disB = "";				// 2003/11/27 nakamoto
}
else
{
	$check_2 = "";
	$out_disB = "disabled";		// 2003/11/27 nakamoto
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK2",
					"value"		=> "on",
//					"extrahtml"	=>$check_2 ) );
					"extrahtml"	=>"onclick=\"OptionUse3('this');\"".$check_2 ) );	// 2003/11/27 nakamoto

$arg["data"]["CHECK2"] = $objForm->ge("CHECK2");


//出力順ラジオボタンを作成///////////// 2003/11/27 nakamoto //////////////////////////////////////////////
$optB[0]=1;
$optB[1]=2;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUTB",
					"value"      => isset($model->field["OUTPUTB"])?$model->field["OUTPUTB"]:"1",
					"extrahtml"	 => $out_disB,
					"multiple"   => $optB));

$arg["data"]["OUTPUTB1"] = $objForm->ge("OUTPUTB",1);
$arg["data"]["OUTPUTB2"] = $objForm->ge("OUTPUTB",2);


//未提出項目生徒チェックボックスを作成////////////////////////////////////////////////////////////////////
if($model->field["CHECK3"] == "on")
{
	$check_3 = "checked";
}
else
{
	$check_3 = "";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK3",
					"value"		=> "on",
					"extrahtml"	=>"onclick=\"DataUse('this');\"".$check_3 ) );

$arg["data"]["CHECK3"] = $objForm->ge("CHECK3");

//眼科検診チェックボックスを作成////////////////////////////////////////////////////////////////////
if($model->field["CHECK4"] == "on")
{
	$check_4 = "checked";
}
else
{
	$check_4 = "";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK4",
					"value"		=> "on",
					"extrahtml"	=> "onclick=\"DataUse('this');\"".$check_4) );

$arg["data"]["CHECK4"] = $objForm->ge("CHECK4");

//学校への提出日カレンダーを作成/////////////////////////////////////////////////////////////////////////////////
if(($model->field["CHECK3"] == "on")|| ($model->field["CHECK4"] == "on"))
{
	$dis3 = "";
    $arg["Dis_Date"]  = " dis_date(false); " ;
}
else
{
	$dis3 = "disabled";
    $arg["Dis_Date"]  = " dis_date(true); " ;
}

$value = isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"];
//$value = str_replace("-","/",CTRL_DATE);
/*
$name  = "DATE";
//$param = "name=$name&";
$param = "";
//テキストエリア
$objForm->ae( array("type"        => "text",
					"name"        => $name,
                    "size"        => 12,
                    "maxlength"   => 12,
                    "extrahtml"   => "onblur=\"isDate(this)\"".$dis3,
                    "value"       => $value));

//読込ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_calen",
                    "value"       => "...",
//                    "extrahtml"   => "onclick=\"subWinOpen_cal('$name','$param');\"".$dis3) );
                    "extrahtml"   => "onclick=\"loadwindow('" .REQUESTROOT ."/common/cal.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value, event.x, event.y, 300, 200)\"".$dis3) );
    
$arg["data"]["DATE"] = View::setIframeJs() .$objForm->ge($name) .$objForm->ge("btn_calen");
//$arg["data"]["DATE"] = $objForm->ge($name) .$objForm->ge("btn_calen");
*/
$arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",$value);


//診断結果チェックボックスを作成///////////////////////////////////////////////////////////////////
if($model->field["CHECK5"] == "on")
{
	$check_5 = "checked";
}
else
{
	$check_5 = "";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK5",
					"value"		=> "on",
					"extrahtml"	=>$check_5 ) );

$arg["data"]["CHECK5"] = $objForm->ge("CHECK5");


//健康診断結果チェックボックスを作成////////////////////////////////////////////////////////////////////
if($model->field["CHECK6"] == "on")
{
	$check_6 = "checked";
	$out_dis = "";
}
else
{
	$check_6 = "";
	$out_dis = "disabled";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK6",
					"value"		=> "on",
					"extrahtml"	=>"onclick=\"OptionUse('this');\"".$check_6 ) );

$arg["data"]["CHECK6"] = $objForm->ge("CHECK6");


//出力順ラジオボタンを作成////////////////////////////////////////////////////////////////////////
$opt[0]=1;
$opt[1]=2;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUT",
					"value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:"1",
					"extrahtml"	=>$out_dis,
					"multiple"   => $opt));

$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);


//定期検診結果チェックボックスを作成////////////////////////////////////////////////////////////////////
if($model->field["CHECK7"] == "on")
{
	$check_7 = "checked";
}
else
{
	$check_7 = "";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK7",
					"value"		=> "on",
					"extrahtml"	=>$check_7) );

$arg["data"]["CHECK7"] = $objForm->ge("CHECK7");


//内科検診所見ありチェックボックスを作成///////////////////////////////////////////////////////////////////
if($model->field["CHECK8"] == "on")
{
	$check_8 = "checked";
}
else
{
	$check_8 = "";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK8",
					"value"		=> "on",
					"extrahtml"	=>$check_8 ) );

$arg["data"]["CHECK8"] = $objForm->ge("CHECK8");


//診断異常者一覧チェックボックスを作成////////////////////////////////////////////////////////////////////
if($model->field["CHECK9"] == "on")
{
	$check_9 = "checked";
	$dis9 = "";
}
else
{
	$check_9 = "";
	$dis9 = "disabled";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "CHECK9",
					"value"		=> "on",
					"extrahtml"	=>"onclick=\"SelectUse('this');\"".$check_9 ) );

$arg["data"]["CHECK9"] = $objForm->ge("CHECK9");


//一般条件コンボボックスを作成する///////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query2 = "SELECT M.NAMECD2 AS VALUE,M.NAME1 AS LABEL,M.NAMECD1 AS CD1 ".
			"FROM NAME_YDAT D INNER JOIN NAME_MST M ON D.NAMECD1=M.NAMECD1 AND D.NAMECD2=M.NAMECD2 ".
			"WHERE ((D.YEAR='" .CTRL_YEAR ."') ".
			"AND (M.NAMECD1='F610')) ORDER BY M.NAMECD2";

$result2 = $db->query($query2);
while($rowf = $result2->fetchRow(DB_FETCHMODE_ASSOC))
{
	$row2[]= array('label' => $rowf["LABEL"],
					'value' => $rowf["VALUE"]);
}
$result2->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"		 => "select",
					"name"		 => "SELECT1",
					"size"		 => "1",
					"value" 	 => $model->field["SELECT1"],
					"extrahtml"  => "style=\"width=170px\" width=\"170px\" ".$dis9,
					"options"	 => isset($row2)?$row2:array()));

$arg["data"]["SELECT1"] = $objForm->ge("SELECT1");


//歯・口腔条件コンボボックスを作成する////////////////////////////////////////////////////////////////////////

$db = Query::dbCheckOut();
$query3 = "SELECT M.NAMECD2 AS VALUE,M.NAME1 AS LABEL,M.NAMECD1 AS CD1 ".
			"FROM NAME_YDAT D INNER JOIN NAME_MST M ON D.NAMECD1=M.NAMECD1 AND D.NAMECD2=M.NAMECD2 ".
			"WHERE ((D.YEAR='" .CTRL_YEAR ."') ".
			"AND (M.NAMECD1='F620')) ORDER BY M.NAMECD2";

$result3 = $db->query($query3);
while($rowt = $result3->fetchRow(DB_FETCHMODE_ASSOC))
{
	$row3[]= array('label' => $rowt["LABEL"],
					'value' => $rowt["VALUE"]);
}
$result3->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"		 => "select",
					"name"		 => "SELECT2",
					"size"		 => "1",
					"value" 	 => $model->field["SELECT2"],
					"extrahtml"  => "style=\"width=170px\" width=\"170px\" ".$dis9,
					"options"	 => isset($row3)?$row3:array()));

$arg["data"]["SELECT2"] = $objForm->ge("SELECT2");


//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"      => PROGRAMID
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjf040Form1.html", $arg); 

}

}
?>
