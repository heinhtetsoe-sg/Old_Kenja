<?php

require_once('for_php7.php');

/********************************************************************/
/* 学年・試験・クラス別平均点一覧                   山城 2005/02/09 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/

class knjd323Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd323Form1", "POST", "knjd323index.php", "", "knjd323Form1");


//今学期---2005.05.09
$objForm->ae( array("type"      => "hidden",
                    "name"      => "CTRL_SEME",
                    "value"      => CTRL_SEMESTER) );
//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );

//学期リスト
$db = Query::dbCheckOut();
$query = knjd323Query::getSemester($model);
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
					"extrahtml"  => "onChange=\"return btn_submit('knjd323');\"",
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
}else if($model->field["GAKKI"] == 3){
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


$objForm->ae( array("type"       => "select",
                    "name"       => "TESTKINDCD",
                    "size"       => "1",
                    "value"      => $model->field["TESTKINDCD"],
					"extrahtml"  => "onChange=\"return btn_submit('knjd323');\"",//---2005.05.09
//					"extrahtml"  => "",
                    "options"    => $opt_kind));

$arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");


//学年リストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd323Query::getSelectGrade($model);
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
                    "value"     => "KNJD323"
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
View::toHTML($model, "knjd323Form1.html", $arg); 
}
}
?>
