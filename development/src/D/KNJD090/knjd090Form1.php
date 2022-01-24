<?php

require_once('for_php7.php');


class knjd090Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd090Form1", "POST", "knjd090index.php", "", "knjd090Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"],
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

	$ga = isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"];
}

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKKI",
                    "size"       => "1",
                    "value"      => isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"],
					"extrahtml"  => "onchange=\"return btn_submit('');\"",
                    "options"    => $opt ) );

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


//出力形式ラジオボタンを作成する//////////////////////////////////////////////////////////////////////////
$opt2[0]=1;
$opt2[1]=2;
$opt2[2]=3;
$objForm->ae( array("type"       => "radio",
                    "name"       => "STUD",
					"value"      => isset($model->field["STUD"])?$model->field["STUD"]:1,
					"extrahtml"   => "onClick=\"ra_stud(this);\"",
					"multiple"   => $opt2));

$arg["data"]["STUD1"] = $objForm->ge("STUD",1);
$arg["data"]["STUD2"] = $objForm->ge("STUD",2);
$arg["data"]["STUD3"] = $objForm->ge("STUD",3);


//クラス・総合の区分ラジオボタンを作成する//////////////////////////////////////////////////////////////////////////
$objForm->add_element(array("type"       => "radio",
                            "value"      => isset($model->field["OUT2"])?$model->field["OUT2"]:1,
                            "name"      => "OUT2"));

$arg["OUTPUT3"] = $objForm->ge("OUT2",1);
$arg["OUTPUT4"] = $objForm->ge("OUT2",2);


//席次テキストボックスを作成/////////////////////////////////////////////////////////////////////////////////////////
if($model->field["STUD"] == 2)					//「席次が○○番以内の生徒」が選択された時のみ使用可
{
	$dis_number = "";
}
else
{
	$dis_number = "disabled";
}

$objForm->ae( array("type"       => "text",
                    "name"       => "NUMBER",
                    "size"       => "3",
                    "value"      => $model->field["STUD"]==2?$model->field["NUMBER"]:"",
					"extrahtml"  => $dis_number));

$arg["data"]["NUMBER"] = $objForm->ge("NUMBER");


//評定点数テキストボックスを作成/////////////////////////////////////////////////////////////////////////////////////
if($model->field["STUD"] == 3)					//「評定が○○点以内の生徒」が選択された時のみ使用可
{
	$dis_point = "";
}
else
{
	$dis_point = "disabled";
}

$objForm->ae( array("type"       => "text",
                    "name"       => "POINT",
                    "size"       => "3",
                    "value"      => $model->field["STUD"]==3?$model->field["POINT"]:"",
					"extrahtml"  => "$dis_point"));

$arg["data"]["POINT"] = $objForm->ge("POINT");


//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjd090Query::getAuth($model->control["年度"],$ga);
/*
$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
            "FROM SCHREG_REGD_HDAT ".
            "WHERE YEAR='" .$model->control["年度"] ."'".
			"AND SEMESTER='".$ga ."'";
*/
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt1[]= array('label' =>  $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


//生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('right')\"",
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
                    "value"     => "KNJD090"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり/////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 ///////////////////////////////////////////////////////////
View::toHTML($model, "knjd090Form1.html", $arg); 
}
}
?>
