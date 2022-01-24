<?php

require_once('for_php7.php');


class knjc130Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjc130Form1", "POST", "knjc130index.php", "", "knjc130Form1");


//年度テキストボックスを作成する////////////////////////////////////////////////////////////////////////////////

$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"     => $model->control["年度"],
                    ) );

//学期コンボの設定/////////////////////////////////////////////////////////////////////////////////////////////
if (is_numeric($model->control["学期数"]))
{
    for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ )
	{
        $opt[]= array("label" => $model->control["学期名"][$i+1],
                      "value" => sprintf("%d", $i+1)
                     );
    }

	if(isset($model->field["SEMESTER"]))
	{
			$ga = $model->field["SEMESTER"];
	}
	else
	{
		$ga =$model->control["学期"];
	}
}

$objForm->ae( array("type"       => "select",
                    "name"       => "SEMESTER",
                    "size"       => "1",
                    "value"      => isset($model->field["SEMESTER"])?$model->field["SEMESTER"]:$model->control["学期"],
					"extrahtml"  => "onchange=\"return btn_submit('knjc130');\"",
                    "options"    => $opt ) );

$arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");


//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjc130Query::getAuth($model, $model->control["年度"],$ga);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt1[]= array('label' =>  $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width:150px\" width:\"150px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


//生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width:150px\" width:\"150px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
                    "options"    => array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


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


//カレンダーコントロール１///////////////////////////////////////////////////////////////////////////////////////
$value = isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"];
$arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",$value);


//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJC130"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり/////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 ///////////////////////////////////////////////////////////
View::toHTML($model, "knjc130Form1.html", $arg); 
}
}
?>
