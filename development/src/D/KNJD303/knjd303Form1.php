<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：テスト項目の設定をDBからでなく、直接設定 山城 2004/10/26 */
/* ･NO002：異動対象日付追加                         山城 2004/11/29 */
/* ･NO003：学期末・学年末を追加                     仲本 2004/12/17 */
/* ･NO004：科目リスト SUBCLASSCD < '900000'         仲本 2004/12/17 */
/********************************************************************/

class knjd303Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd303Form1", "POST", "knjd303index.php", "", "knjd303Form1");


//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );

//学期リスト
$db = Query::dbCheckOut();
$query = knjd303Query::getSemester($model);
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
					"extrahtml"  => "onChange=\"return btn_submit('knjd303');\"",
                    "options"    => $opt_seme));

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


//テスト種別リスト

/* NO003 ↓*/
if($model->field["GAKKI"] == 9 )
{
	$opt_kind[] = array("label" => "","value" => "0");
/* NO003 ↑*/
/* NO001 ↓ */
}
else if($model->field["GAKKI"] != 3 )
{
	$opt_kind[] = array("label" => "","value" => "0");
   	$opt_kind[]= array('label' => '01　中間テスト',
            	       'value' => '01');
   	$opt_kind[]= array('label' => '02　期末テスト',
        	           'value' => '02');
}
else
{
   	$opt_kind[]= array('label' => '02　期末テスト',
        	           'value' => '02');
}
/* NO001 ↑ */

//if ($model->field["TESTKINDCD"]=="") $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

$objForm->ae( array("type"       => "select",
                    "name"       => "TESTKINDCD",
                    "size"       => "1",
                    "value"      => $model->field["TESTKINDCD"],
					"extrahtml"  => "STYLE=\"WIDTH:130\" ",		/* NO003 */
                    "options"    => $opt_kind));

$arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");


//学年コンボボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd303Query::getSelectGrade($model);
$result = $db->query($query);
$grade_flg = true;
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $grade_show= sprintf("%d",$row["GRADE"]);
	$opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
						 'value' => $row["GRADE"]);
	if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
}
if( $grade_flg ) $model->field["GRADE"] = $opt_grade[0]["value"];
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE",
                    "size"       => "1",
                    "value"      => $model->field["GRADE"],
					"extrahtml"  => "",
					//"extrahtml"  => "onChange=\"return btn_submit('knjd303');\"",
                    "options"    => $opt_grade));

$arg["data"]["GRADE"] = $objForm->ge("GRADE");


//科目一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjd303Query::getSubclass($model);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' 	=> $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"],
                    'value' => $row["SUBCLASSCD"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"",
                    "size"       => "20",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


//出力対象科目リストを作成する///////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"",
                    "size"       => "20",
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

/* NO002↓ */
if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
$arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);
/* NO002↑ */

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
                    "value"     => "KNJD303"
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
View::toHTML($model, "knjd303Form1.html", $arg); 
}
}
?>
