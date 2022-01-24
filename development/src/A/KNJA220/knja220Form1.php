<?php

require_once('for_php7.php');

/********************************************************************/
/* HR別名票                                                         */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：空行を詰めて印字チェックボックス追加     山城 2006/01/19 */
/********************************************************************/

class knja220Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knja220Form1", "POST", "knja220index.php", "", "knja220Form1");


//対象科目コンボボックスを作成する////////////////////////////////////////////////////////////////////////////
//$db = Query::dbCheckOut();

//$query2 = "SELECT COURSECD || MAJORCD AS VALUE,MAJORNAME AS LABEL FROM major_mst";

//$result2 = $db->query($query2);
//while($roww = $result2->fetchRow(DB_FETCHMODE_ASSOC)){
//    $row2[]= array('label' => $roww["LABEL"],
//                    'value' => $roww["VALUE"]);
//}

//$result2->free();
//Query::dbCheckIn($db);


//$objForm->ae( array("type"       => "select",
//                    "name"       => "MAJOR_NAME",
//                    "size"       => "1",
//                    "value"      => $model->field["MAJOR_NAME"],
//                    "options"    => isset($row2)?$row2:array()));

//$arg["data"]["MAJOR_NAME"] = $objForm->ge("MAJOR_NAME");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"],
                    ) );

//学期コードをhiddenで送る/////////////////////////////////////////////////////////////////////////////////////////
$arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GAKKI",
                    "value"      => $model->control["学期"],
                    ) );


//クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////

$db = Query::dbCheckOut();
//$query = common::getHrClassAuth(CTRL_YEAR, CTRL_SEMESTER, AUTHORITY, STAFFCD);// 05/01/07 keep
$query = knja220Query::getAuth($model);
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

$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width:220px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


//出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width:220px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
                    "options"    => array()));

$arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


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

//空行を詰めて印字チェックボックス NO001
$objForm->ae( array("type"       => "checkbox",
                    "name"       => "OUTPUT2",
                    "checked"    => false,
					"value"      => isset($model->field["OUTPUT2"])?$model->field["OUTPUT2"]:"1"));

$arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT2");

//名票ラジオボタンを作成（全学年用/学級用枠あり/学級用枠なし）//////////////////////////////////////////////////
$opt[0]=1;
$opt[1]=2;
//$opt[2]=3;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUT",
					"value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:1,
//					"extrahtml"  => "onclick=\"keisenselect(this);\"",
					"multiple"   => $opt));

$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
//$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);


//出力件数テキストボックス
$objForm->ae( array("type"        => "text",
                    "name"        => "KENSUU",
                    "size"        => 3,
                    "maxlength"   => 2,
                    "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                    "value"       => isset($model->field["KENSUU"])?$model->field["KENSUU"]:1 ));
$arg["data"]["KENSUU"] = $objForm->ge("KENSUU");


//罫線幅設定リストボックスを作成する/////////////////////////////////////////////////////////////////////
//$ks = array(array('label' => "１ミリ", 'value' => 1),
//			array('label' => "２ミリ", 'value' => 2),
//			array('label' => "３ミリ", 'value' => 3),
//			array('label' => "４ミリ", 'value' => 4),
//			array('label' => "５ミリ", 'value' => 5) );

//$objForm->ae( array("type"       => "select",
//                    "name"       => "KEISEN",
//                    "size"       => "1",
//                    "value"      => isset($model->field["KEISEN"])?$model->field["KEISEN"]:0,
//					"extrahtml"  => "disabled",
//                    "options"    => $ks ) );

//$arg["data"]["KEISEN"] = $objForm->ge("KEISEN");


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
                    "value"     => "KNJA220"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knja220Form1.html", $arg); 
}
}
?>
