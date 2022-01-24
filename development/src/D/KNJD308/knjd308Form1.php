<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：テスト項目の設定をDBからでなく、直接設定 山城 2004/10/26 */
/* ･NO002：異動対象日付追加                         山城 2004/11/29 */
/********************************************************************/

class knjd308Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd308Form1", "POST", "knjd308index.php", "", "knjd308Form1");


//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );

//学期リスト
$db = Query::dbCheckOut();
$query = knjd308Query::getSemester($model);
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
					"extrahtml"  => "onChange=\"return btn_submit('knjd308');\"",
                    "options"    => $opt_seme));

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


//テスト種別リスト

/* NO001 ↓ */
if($model->field["GAKKI"] != 3 )
{
   	$opt_kind[]= array('label' => '0101　中間テスト',
            	       'value' => '0101');
   	$opt_kind[]= array('label' => '0201　期末テスト',
        	           'value' => '0201');
}
else
{
   	$opt_kind[]= array('label' => '0201　期末テスト',
        	           'value' => '0201');
}
/* NO001 ↑ */

//if ($model->field["TESTKINDCD"]=="") $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

$objForm->ae( array("type"       => "select",
                    "name"       => "TESTKINDCD",
                    "size"       => "1",
                    "value"      => $model->field["TESTKINDCD"],
					"extrahtml"  => "",
                    "options"    => $opt_kind));

$arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");


//学年リストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd308Query::getSelectGrade($model);
$result = $db->query($query);
$i=0;
$grade_flg = true;
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $grade_show= sprintf("%d",$row["GRADE"]);
	$opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
						 'value' => $row["GRADE"]);
	$i++;
	if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
}
if( $grade_flg ) $model->field["GRADE"] = $opt_grade[0]["value"];
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE",
                    "size"       => $i,
                    "value"      => $model->field["GRADE"],
                    "options"    => $opt_grade,
					"extrahtml"	 => "multiple" ) );

$arg["data"]["GRADE"] = $objForm->ge("GRADE");

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
                    "value"     => "KNJD308"
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
View::toHTML($model, "knjd308Form1.html", $arg); 
}
}
?>
