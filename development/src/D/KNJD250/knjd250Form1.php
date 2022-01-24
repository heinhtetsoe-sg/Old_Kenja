<?php

require_once('for_php7.php');


class knjd250Form1
{
    function main(&$model){

        //権限チェック          
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd250Form1", "POST", "knjd250index.php", "", "knjd250Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////
$arg["data"]["YEAR"] = $model->control["年度"];

//学年コンボボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd250Query::getSelectGrade($model->control["年度"]);
$result = $db->query($query);
$i=0;
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $grade_show= sprintf("%d",$row["GRADE"]);
	$opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
						 'value' => $row["GRADE"]);
	$i++;
}
//if($model->field["GAKUNEN"]=="") $model->field["GAKUNEN"] = $opt_grade[0];
$result->free();
Query::dbCheckIn($db);

if(!isset($model->field["GAKUNEN"])) {
    $model->field["GAKUNEN"] = $opt_grade[0]["value"];
}

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKUNEN",
                    "size"       => "1",
                    "value"      => $model->field["GAKUNEN"],
                    "extrahtml"   => "onchange=\"return btn_submit('knjd250');\"",
                    "options"    => $opt_grade) );

$arg["data"]["GAKUNEN"] = $objForm->ge("GAKUNEN");
//echo $opt_grade[0]["value"];
//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
if (isset($model->field["GAKUNEN"])){ 
    $db = Query::dbCheckOut();
    $query = knjd250Query::getSelectClass($model);

    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt1[]= array('label' =>  $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    Query::dbCheckIn($db);
}

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                    "size"       => "20",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


//科目一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                    "size"       => "20",
                    "options"    => array()));

$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


//対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
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
                    "value"     => "KNJD250"
                    ) );

//現在学期
$objForm->ae( array("type"      => "hidden",
                    "name"      => "GAKKI",
                    "value"     => $model->control["学期"]
                    ) );

$arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//年度データ
$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"     => $model->control["年度"]
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd250Form1.html", $arg); 
}
}
?>
