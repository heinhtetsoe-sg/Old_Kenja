<?php

require_once('for_php7.php');


class knjf100Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成
$arg["start"] = $objForm->get_start("knjf100Form1", "POST", "knjf100index.php", "", "knjf100Form1");

//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////
$arg["data"]["YEAR"] = $model->control["年度"];


/////////////////////////////////////////////////////////////////////////////////////////////////////
$semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
$semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
$semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];


//カレンダーコントロール１///////////////////////////////////////////////////////////////////////////////////////
$value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];
$arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$value);


//カレンダーコントロール２///////////////////////////////////////////////////////////////////////////////////////
$value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];
$arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",$value2);


//学期を取得する/////////////////////////////////////////////////////////////////////////////////////////////////
$seme = "";
if(isset($model->field["DATE1"]))
{
	if( ($model->control['学期開始日付'][1] <= $model->field["DATE1"]) && ($model->field["DATE2"] <= $model->control['学期終了日付'][1]))
	{
		$seme = "1";
	}
	if( ($model->control['学期開始日付'][2] <= $model->field["DATE1"]) && ($model->field["DATE2"] <= $model->control['学期終了日付'][2]))
	{
		$seme = "2";
	}
	if( ($model->control['学期開始日付'][3] <= $model->field["DATE1"]) && ($model->field["DATE2"] <= $model->control['学期終了日付'][3]))
	{
		$seme = "3";
	}
	if( ($model->control['学期開始日付'][1] <= $model->field["DATE1"]) && ($model->field["DATE1"] <= $model->control['学期終了日付'][1]))
	{
		$seme = "1";
	}
	if( ($model->control['学期開始日付'][2] <= $model->field["DATE1"]) && ($model->field["DATE1"] <= $model->control['学期終了日付'][2]))
	{
		$seme = "2";
	}
	if( ($model->control['学期開始日付'][3] <= $model->field["DATE1"]) && ($model->field["DATE1"] <= $model->control['学期終了日付'][3]))
	{
		$seme = "3";
	}
}
else		//最初の学期はコントロールマスタから現在の学期を取得
{
	$seme = $model->control["学期"];
}
//クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
//if(isset($model->field["SEMESTER"]))
//{
	$db = Query::dbCheckOut();
	$query = knjf100Query::getAuth($model->control["年度"],$seme);
/*
	$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
	            "FROM SCHREG_REGD_HDAT ".
	            "WHERE YEAR='" .$model->control["年度"] ."'".
				"AND SEMESTER='".$seme."'";
*/
	$result = $db->query($query);
	while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	    $row1[]= array('label' => $row["LABEL"],
	                    'value' => $row["VALUE"]);
	}
	$result->free();
	Query::dbCheckIn($db);
//}

$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


//出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
                    "options"    => array()));

$arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


//読込ボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_read",
                    "value"       => "読　込",
                 "extrahtml"   => "onclick=\"return btn_submit('knjf100');\"" ) );
$arg["button"]["btn_read"] = $objForm->ge("btn_read");


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


//出力する情報チェックボックスを作成/////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "checkbox",
                    "name"       => "CHECK1",
                    "checked"    => ($model->field["CHECK1"]=="on")?true:false,
					"value"      => "on"));


$arg["data"]["CHECK1"] = $objForm->ge("CHECK1");


//印刷ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する///////////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJF100"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//年度データ
$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"     => $model->control["年度"]
                    ) );


//学期開始日
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME_DATE",
                    "value"     => $semester
                    ) );


//学期
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEMESTER",
                    "value"     => $seme
                    ) );

$arg["data"]["SEMESTER"] = $model->control["学期名"][$seme];

//フォーム作成
$arg["finish"]  = $objForm->get_finish();

//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjf100Form1.html", $arg); 
}
}
?>
