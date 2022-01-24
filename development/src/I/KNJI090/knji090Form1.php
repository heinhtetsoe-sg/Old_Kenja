<?php

require_once('for_php7.php');


class knji090Form1
{
    function main(&$model){

        //権限チェック          
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knji090Form1", "POST", "knji090index.php", "", "knji090Form1");

$db = Query::dbCheckOut();


//卒業年度・卒業期
$opt_year = array();
$query = knji090Query::GetYear();
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_year[]= array('label' => $row["GRADUATE_YEAR"]."年度卒(第".$row["PRESENT_EST"]."期卒)",
	                    'value' => $row["GRADUATE_YEAR"]);

	if($model->field["YEAR"]=="") $model->field["YEAR"] = $row["GRADUATE_YEAR"];
}

$objForm->ae( array("type"       => "select",
                    "name"       => "YEAR",
                    "size"       => "1",
                    "extrahtml"  => "onChange=\"return btn_submit('knji090');\"",
                    "value"      => $model->field["YEAR"],
                    "options"    => $opt_year));

$arg["data"]["YEAR"] = $objForm->ge("YEAR");


//クラスコンボボックス
$flg = "false";
$opt_class = array();
$query = knji090Query::GetGradeHrClass($model->field["YEAR"],$model->control["学校区分"]);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_class[]= array('label' => $row["HR_NAME"],
                    	'value' => $row["VALUE"]);

	if($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $flg = "true";
}

if($flg == "false") $model->field["GRADE_HR_CLASS"] = $opt_class[0]["value"];

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE_HR_CLASS",
                    "size"       => "1",
                    "value"      => $model->field["GRADE_HR_CLASS"],
					"extrahtml"  => "onchange=\"return btn_submit('knji090');\"",
                    "options"    => $opt_class));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");


//生徒一覧
$opt_schregno = array();
$query = knji090Query::GetSchregno($model->field["YEAR"],$model->field["GRADE_HR_CLASS"],$model->control["学校区分"]);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_schregno[]= array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                    		'value' => $row["SCHREGNO"]);
}

$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('left')\"",
                    "size"       => "20",
                    "options"    => $opt_schregno));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('right')\"",
                    "size"       => "20",
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


//ページ番号初期値リストボックスを作成する/////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "text",
                    "name"       => "PAGE",
                    "size"       => "3",
                    "value"      => isset($model->field["PAGE"])?$model->field["PAGE"]:1 ) );

$arg["data"]["PAGE"] = $objForm->ge("PAGE");


//印刷ボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////////
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

//プログラムＩＤ
$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJI090"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knji090Form1.html", $arg); 
}
}
?>
