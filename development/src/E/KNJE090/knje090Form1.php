<?php

require_once('for_php7.php');


class knje090Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knje090Form1", "POST", "knje090index.php", "", "knje090Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"],
                    ) );


//現在の学期コードをhiddenで送る//////////////////////////////////////////////////////////////////////////////////
$arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GAKKI",
                    "value"      => $model->control["学期"],
                    ) );


//クラス選択コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knje090Query::getAuth($model->control["年度"],$model->control["学期"]);
/*
$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
            "FROM SCHREG_REGD_HDAT ".
            "WHERE YEAR='" .$model->control["年度"] ."'".
			"AND SEMESTER='".$model->control["学期"] ."'";
*/
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

if(!isset($model->field["GRADE_HR_CLASS"])) {
    $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
}

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE_HR_CLASS",
                    "size"       => "1",
                    "value"      => $model->field["GRADE_HR_CLASS"],
                    "extrahtml"  => "onchange=\"return btn_submit('knje090');\"",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");


//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO,
SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
            "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
            "WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ".
			"((SCHREG_REGD_DAT.SEMESTER)='" .$model->control["学期"] ."') AND ".
			"((SCHREG_REGD_DAT.GRADE)='" .substr($model->field["GRADE_HR_CLASS"],0,2) ."') AND ".
			"((SCHREG_REGD_DAT.HR_CLASS)='" .substr($model->field["GRADE_HR_CLASS"],2,2) ."'))".
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
					"extrahtml"  => "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


//生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
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


//テスト項目リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();

$query2 = "SELECT TESTKINDCD || TESTITEMCD AS VALUE,TESTKINDCD || TESTITEMCD || '　' || TESTITEMNAME AS LABEL ".
			"FROM TESTITEM_MST ".
			"WHERE YEAR='" .$model->control["年度"] ."' AND TESTKINDCD > '02' ".
			"GROUP BY TESTKINDCD,TESTITEMCD,TESTITEMNAME ".
			"ORDER BY 1";

$result2 = $db->query($query2);
while($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)){
    $row2[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result2->free();
Query::dbCheckIn($db);


$objForm->ae( array("type"       => "select",
                    "name"       => "TEST_NAME",
					"extrahtml"  => "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('leftt')\"",
                    "size"       => "15",
                    "options"    => isset($row2)?$row2:array()));

$arg["data"]["TEST_NAME"] = $objForm->ge("TEST_NAME");



//出力テスト項目リストを作成する////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "TEST_SELECTED",
					"extrahtml"  => "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('rightt')\"",
                    "size"       => "15",
                    "options"    => array()));

$arg["data"]["TEST_SELECTED"] = $objForm->ge("TEST_SELECTED");


//テスト項目用対象選択ボタンを作成する（全部）///////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights2",
                    "value"       => ">>",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves2('right');\"" ) );

$arg["button"]["btn_rights2"] = $objForm->ge("btn_rights2");


//テスト項目対象取消ボタンを作成する（全部）///////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts2",
                    "value"       => "<<",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves2('left');\"" ) );

$arg["button"]["btn_lefts2"] = $objForm->ge("btn_lefts2");


//対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right12",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('rightt');\"" ) );

$arg["button"]["btn_right12"] = $objForm->ge("btn_right12");


//対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left12",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('leftt');\"" ) );

$arg["button"]["btn_left12"] = $objForm->ge("btn_left12");


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
                    "value"     => "KNJE090"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "DATE",
                    "value"      => $model->control["学籍処理日"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knje090Form1.html", $arg); 
}
}
?>
