<?php

require_once('for_php7.php');

/***********************************************************************/
/* 変更履歴                                                            */
/* ･NO001：異動対象日付追加                            山城 2005/03/07 */
/***********************************************************************/

class knjd320Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成///////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd320Form1", "POST", "knjd320index.php", "", "knjd320Form1");


	//	起動チェック（今学期が１学期の場合、表示しない）04/12/17
//		if (CTRL_SEMESTER=="1") $arg["jscript"] = "closeWindow();" ;

//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );


//学期
$arg["data"]["GAKKI"] = $model->control["学期名"][9];	//学年末

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GAKKI",
                    "value"     => "9",
                    ) );

//学年リストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd320Query::getSelectGrade($model);
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


//科目数テキストボックスを作成する////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "text",
                    "name"       => "SUBCLASS",
                    "size"       => "2",
                    "maxlength"  => 2,
                    "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
                    "value"      => isset($model->field["SUBCLASS"])?$model->field["SUBCLASS"]:"4") );

$arg["data"]["SUBCLASS"] = $objForm->ge("SUBCLASS");


//帳票選択ラジオボタンを作成////////////////////////////////////////////////////////////////////////
$opt[0]=1;
$opt[1]=2;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUT",
					"value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:2,
					"multiple"   => $opt));

$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

/* NO001↓ */
if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
$arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);
/* NO001↑ */

//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" 		  => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" 		  => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"     => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJD320"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "useCurriculumcd",
                    "value"     => $model->Properties["useCurriculumcd"]
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd320Form1.html", $arg); 
}
}
?>
