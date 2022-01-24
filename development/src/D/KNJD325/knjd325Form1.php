<?php

require_once('for_php7.php');

/********************************************************************/
/* 成績一覧表(クラス・個人別)                       山城 2005/02/24 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/

class knjd325Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd325Form1", "POST", "knjd325index.php", "", "knjd325Form1");


//今学期---2005.05.09
$objForm->ae( array("type"      => "hidden",
                    "name"      => "CTRL_SEME",
                    "value"      => CTRL_SEMESTER) );
//学籍処理日---2005.05.09
$objForm->ae( array("type"      => "hidden",
                    "name"      => "CTRL_DAY",
                    "value"      => CTRL_DATE) );
//Ｆ表示有りチェックボックス---2005.05.12
if ($model->field["FINCD"] == "on") $chk_flg = "checked";
$objForm->ae( array("type"      => "checkbox",
                    "name"      => "FINCD",
					"extrahtml" => $chk_flg,
					"value"		=> "on") );
$arg["data"]["FINCD"] = $objForm->ge("FINCD");

//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );

//学期リスト
$db = Query::dbCheckOut();
$query = knjd325Query::getSemester($model);
$result = $db->query($query);
$opt_seme = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_seme[]= array('label' 	=> $row["SEMESTERNAME"],
                    	'value' => $row["SEMESTER"]);
}
$result->free();
Query::dbCheckIn($db);

if ($model->field["GAKKI"]=="") $model->field["GAKKI"] = CTRL_SEMESTER;

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKKI",
                    "size"       => "1",
                    "value"      => $model->field["GAKKI"],
					"extrahtml"  => "onChange=\"return btn_submit('knjd325');\"",
                    "options"    => $opt_seme));

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

//テスト種別リスト

if($model->field["GAKKI"] < 3 )
{
   	$opt_kind[]= array('label' => '中間試験',
            	       'value' => '01');
   	$opt_kind[]= array('label' => '期末試験',
        	           'value' => '02');
   	$opt_kind[]= array('label' => '学期成績',
            	       'value' => '0');
}else if ($model->field["GAKKI"] == 3 )
{
   	$opt_kind[]= array('label' => '期末試験',
        	           'value' => '02');
   	$opt_kind[]= array('label' => '学期成績',
            	       'value' => '0');
}else{
   	$opt_kind[]= array('label' => '学年成績',
        	           'value' => '9');
    //---2005.05.09
   	$opt_kind[]= array('label' => '絶対評価(学年評定)',
        	           'value' => '90');
   	$opt_kind[]= array('label' => '相対評価(５段階)',
        	           'value' => '91');
   	$opt_kind[]= array('label' => '相対評価(１０段階)',
        	           'value' => '92');
}

//初期化処理
$test_flg = false;
for ($i=0; $i<get_count($opt_kind); $i++) 
    if ($model->field["TESTKINDCD"] == $opt_kind[$i]["value"]) $test_flg = true;
if (!$test_flg) $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

$objForm->ae( array("type"       => "select",
                    "name"       => "TESTKINDCD",
                    "size"       => "1",
                    "value"      => $model->field["TESTKINDCD"],
					"extrahtml"  => "onChange=\"return btn_submit('knjd325');\"",//---2005.05.09
//					"extrahtml"  => "",
                    "options"    => $opt_kind));

$arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

//クラス一覧リスト---2005.05.18
$db = Query::dbCheckOut();
//---2005.06.01---↓---
$semester = ($model->field["GAKKI"]=="9") ? CTRL_SEMESTER : $model->field["GAKKI"];
$query = common::getHrClassAuth(CTRL_YEAR,$semester,AUTHORITY,STAFFCD);
$test9192 = false;
if ($model->field["TESTKINDCD"] == "91" || $model->field["TESTKINDCD"] == "92") $test9192 = true;
//$query = knjd325Query::getAuth($model);
//---2005.06.01---↑---
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    if ($test9192 && substr($row["VALUE"],0,2) != "03") continue;//---2005.06.01
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width=190px\" width=\"190px\" ondblclick=\"move1('left')\"",
                    "size"       => "20",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


//出力対象クラスリスト---2005.05.18
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width=190px\" width=\"190px\" ondblclick=\"move1('right')\"",
                    "size"       => "20",
                    "options"    => array()));

$arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


//対象選択ボタンを作成する（全部）---2005.05.18
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）---2005.05.18
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）---2005.05.18
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）---2005.05.18
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

//---2005.05.18Del
//クラスリストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
//$db = Query::dbCheckOut();
//$opt_class=array();
//$query = knjd325Query::getSelectClass($model);
//$result = $db->query($query);
//$class_flg = true;//---2005.05.09
//while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
//	$opt_class[] = array('label' => $row["HR_NAME"],
//						 'value' => $row["GRADE"].$row["HR_CLASS"]);
//	if( $model->field["CLASS"]==$row["GRADE"].$row["HR_CLASS"] ) $class_flg = false;//---2005.05.09
//}
//if( $class_flg ) $model->field["CLASS"] = $opt_class[0]["value"];//---2005.05.09
//if(!$model->field["CLASS"]) $model->field["CLASS"] = $opt_class[0]["value"];
//$result->free();
//Query::dbCheckIn($db);

//$objForm->ae( array("type"       => "select",
//                    "name"       => "CLASS",
//                    "size"       => 1,
//                    "value"      => $model->field["CLASS"],
//					"extrahtml"  => "onChange=\"return btn_submit('knjd325');\"",
//                    "options"    => $opt_class ) );

//$arg["data"]["CLASS"] = $objForm->ge("CLASS");
//クラスコースを表示
//$db = Query::dbCheckOut();
//$query = knjd325Query::getSelectCorseShow($model);
//$result = $db->query($query);
//$row = $result->fetchRow(DB_FETCHMODE_ASSOC);

//$arg["data"]["COURSESHOW"] = $row["COURSECODENAME"];

//$objForm->ae( array("type"      => "hidden",
//                    "name"      => "COURSESHOW",
//                    "value"      => $row["COURSECODE"],
//                    ) );

//$model->field["COURSESHOW"] = $row["COURSECODE"];
//$result->free();
//Query::dbCheckIn($db);

//コースリストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
//$db = Query::dbCheckOut();
//$opt_corse=array();
//$query = knjd325Query::getSelectCorse($model);
//$result = $db->query($query);
//$i=0;
//$corse_flg = true;
//while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
//	$opt_corse[] = array('label' => $row["COURSECODENAME"],
//						 'value' => $row["COURSECODE"]);
//	$i++;
//}
//$model->field["COURSE_CD"] = $model->field["COURSESHOW"];
//$result->free();
//Query::dbCheckIn($db);

//$objForm->ae( array("type"       => "select",
//                    "name"       => "COURSE_CD",
//                    "size"       => $i,
//                    "value"      => $model->field["COURSE_CD"],
//                    "options"    => $opt_corse,
//					"extrahtml"	 => "multiple" ) );

//$arg["data"]["COURSE_CD"] = $objForm->ge("COURSE_CD");


//異動対象日付
if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
$arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);

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
                    "value"     => "KNJD325"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "useCurriculumcd",
                    "value"     => $model->Properties["useCurriculumcd"]
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd325Form1.html", $arg); 
}
}
?>
