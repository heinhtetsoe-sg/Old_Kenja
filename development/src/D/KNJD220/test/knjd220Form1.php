<?php

require_once('for_php7.php');


class knjd220Form1
{
    function main(&$model){

        //権限チェック          
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd220Form1", "POST", "knjd220index.php", "", "knjd220Form1");


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

	//９＝学年末を追加
	$opt[] = array("label" => $model->control["学期名"][9],
					"value" =>  sprintf("%d", 9)
				);

}

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKKI",
                    "size"       => "1",
					"extrahtml"  => "onChange=\"return Default(this);\"",
                    "value"      => isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"],
                    "options"    => isset($opt)?$opt:array() ) );

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


//学年リストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjd220Query::getSelectGrade($model->control["年度"]);
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
                    "size"       => $i,
                    "value"      => $model->field["GAKUNEN"],
                    "options"    => $opt_grade,
					"extrahtml"	 => "multiple" ) );

$arg["data"]["GAKUNEN"] = $objForm->ge("GAKUNEN");


//評定平均（以上）テキストボックスを作成する//////////////////////////////////////////////////////////////////////
if(isset($model->field["YURYO_OVER"]))
{
	$yover = $model->field["YURYO_OVER"];
}
else
{
	if($model->field["GAKKI"] == "9")
	{
		$yover = 4.3;
	}
	else
	{
		$yover = 80;
	}
}
$objForm->ae( array("type"       => "text",
                    "name"       => "YURYO_OVER",
                    "size"       => "3",
                    "value"      => $yover,
					"extrahtml"  => "onBlur=\"return OverCheck(this);\"" ) );

$arg["data"]["YURYO_OVER"] = $objForm->ge("YURYO_OVER");


//評定平均（未満）テキストボックスを作成する//////////////////////////////////////////////////////////////////////
if(isset($model->field["YURYO_UNDER"]))
{
	$yunder = $model->field["YURYO_UNDER"];
}
else
{
	if($model->field["GAKKI"] == "9")
	{
		$yunder = 1;
	}
	else
	{
		$yunder= 35;
	}
}
$objForm->ae( array("type"       => "text",
                    "name"       => "YURYO_UNDER",
                    "size"       => "3",
                    "value"      => $yunder,
					"extrahtml"  => "onBlur=\"return UnderCheck(this);\"" ) );

$arg["data"]["YURYO_UNDER"] = $objForm->ge("YURYO_UNDER");


//成績不良科目数テキストボックスを作成する///////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "text",
                    "name"       => "FURYO_S",
                    "size"       => "3",
                    "value"      => isset($model->field["FURYO_S"])?$model->field["FURYO_S"]:"1") );

$arg["data"]["FURYO_S"] = $objForm->ge("FURYO_S");


//皆出席開始日を作成する/////////////////////////////////////////////////////////////////////////////////

$arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);


//遅刻数テキストボックスを作成する////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "text",
                    "name"       => "KINTAI1_S",
                    "size"       => "3",
                    "value"      => isset($model->field["KINTAI1_S"])?$model->field["KINTAI1_S"]:"1") );

$arg["data"]["KINTAI1_S"] = $objForm->ge("KINTAI1_S");


//早退数テキストボックスを作成する//////  //add  04/07/13  yamauchi  //////////////////////////////////////

$objForm->ae( array("type"       => "text",
                    "name"       => "KINTAI4_S",
                    "size"       => "3",
                    "value"      => isset($model->field["KINTAI4_S"])?$model->field["KINTAI4_S"]:"1") );

$arg["data"]["KINTAI4_S"] = $objForm->ge("KINTAI4_S");


//欠席数テキストボックスを作成する////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "text",
                    "name"       => "KINTAI2_S",
                    "size"       => "3",
                    "value"      => isset($model->field["KINTAI2_S"])?$model->field["KINTAI2_S"]:"1") );

$arg["data"]["KINTAI2_S"] = $objForm->ge("KINTAI2_S");


//欠課数テキストボックスを作成する////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "text",
                    "name"       => "KINTAI3_S",
                    "size"       => "3",
                    "value"      => isset($model->field["KINTAI3_S"])?$model->field["KINTAI3_S"]:"1") );

$arg["data"]["KINTAI3_S"] = $objForm->ge("KINTAI3_S");


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
                    "value"     => "KNJD220"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり/////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 ///////////////////////////////////////////////////////////
View::toHTML($model, "knjd220Form1.html", $arg); 
}
}
?>
