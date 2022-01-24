<?php

require_once('for_php7.php');


class knjd240Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成/////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd240Form1", "POST", "knjd240index.php", "", "knjd240Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////
$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"]
                    ) );


//学年コンボボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd240Query::getSelectGrade($model->control["年度"]);
$result = $db->query($query);
$i=0;
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $grade_show= sprintf("%d",$row["GRADE"]);
	$opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
						 'value' => $row["GRADE"]);
	$i++;
}
if($model->field["GAKUNEN"]=="") $model->field["GAKUNEN"] = $opt_grade[0]["value"];
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKUNEN",
                    "size"       => "1",
                    "value"      => $model->field["GAKUNEN"],
                    "options"    => $opt_grade) );

$arg["data"]["GAKUNEN"] = $objForm->ge("GAKUNEN");

//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin();\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//学期（学年）
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEMESTER",      //パラメータ
                    "value"     => 9
                    ) );

//ﾃﾞｰﾀﾍﾞｰｽ名
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJD240"
                    ) );

//今学期
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME",
                    "value"     => $model->control["学期"]
                    ) );


//フォーム終わり/////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 ///////////////////////////////////////////////////////////
View::toHTML($model, "knjd240Form1.html", $arg); 
}
}
?>
