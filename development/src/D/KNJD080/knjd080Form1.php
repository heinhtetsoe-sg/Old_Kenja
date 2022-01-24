<?php

require_once('for_php7.php');


class knjd080Form1
{
    function main(&$model){

$objForm = new form;
$arg = array();
//フォーム作成
$arg["start"]   = $objForm->get_start("knjd080Form1", "POST", "knjd080index.php", "", "knjd080Form1");

if ($model->year==""){
	$dis_text = "readonly disabled";
}
else {
	$dis_text = "";
}
$objForm->ae( array("type"       => "text",
                    "name"       => "TEXT",
                    "size"       => "4",
                    "value"      => isset($model->field["TEXT"])?$model->field["TEXT"]:50,
                    "extrahtml"   => $dis_text ));
$arg["TEXT"] = $objForm->ge("TEXT");

//読込ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_toukei",
                    "value"       => "･･･",
                    "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE4/knjxtoke4index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE4',0,0,900,550);\"") );
    																						// 04/10/30
$arg["explore"] = $objForm->ge("btn_toukei");
//学習記録エクスプローラー
if(!isset($model->cmd)) {
    $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE4/knjxtoke4index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE4',0,0,900,550);";
}																// 04/10/30

$cd =& $model->attendclasscd;
$cd_name = "ATTENDCLASSCD";

if (isset($cd)){ 
    $db = Query::dbCheckOut();
    $query = knjd080Query::SQLGet_Main($model);

    //教科、科目、クラス取得
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $title  = "[" . $row["CLASSCD"]."　". htmlspecialchars($row["CLASSNAME"]) ."]-[" .$row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]) ."]";

        $subclasscd = $row["SUBCLASSCD"];


        $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
//2004-07-30 naka
        $checked2 = (is_array($model->checked_staff) && in_array($row["STAFFCD"], $model->checked_staff))? true:false;
//2004/06/30 nakamoto-------------------------------------
			if($row["CHARGEDIV"] == 1) {
				$row["CHARGEDIV"] = ' ＊';
			}
			else {
				$row["CHARGEDIV"] = ' ';
			}
        if($checked==0 || $checked2==0) {	//2004-07-30 naka
    	$objForm->add_element(array("type"      => "checkbox",
    	                             "name"     => "chk",
   	                                //"checked"  => $checked,	2004-07-30 naka
   	                                "checked"  => false,
    	                             "value"    => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                     "extrahtml"   => "multiple" ));

        $row["CHECK"] = $objForm->ge("chk");
        
        $start = str_replace("-","/",$row["STARTDAY"]);
        $end = str_replace("-","/",$row["ENDDAY"]);
        //学籍処理範囲外の場合背景色を変える
        if ((strtotime($model->control["学籍処理日"]) < strtotime($start)) ||
            (strtotime($model->control["学籍処理日"]) > strtotime($end))){
            $row["BGCOLOR"] = "#ccffcc";
        }else{
            $row["BGCOLOR"] = "#ffffff";
        }
        $row["TERM"] = $start ."～" .$end;
        $arg["data"][] = $row; 
        }
        else {
    	$objForm->add_element(array("type"      => "checkbox",
    	                             "name"     => "chk1",
    	                             "checked"  => $checked,
    	                             "value"    => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                     "extrahtml"   => "disabled" ));
        $row["CHECK"] = $objForm->ge("chk1");
        $row["TERM"] = str_replace("-","/",$row["STARTDAY"]) ."～" .str_replace("-","/",$row["ENDDAY"]);
        $arg["data1"][] = $row; 
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STARTDAY",
                            "value"     => $row["STARTDAY"]
                            ) );
        }
    }

/* 04/10/30 テスト名表示をカット
    //テスト名取得
	if($title!=""){
	    $query = knjd080Query::SQLGet_Test($model);
    	$result = $db->query($query);
	    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    	    $title .= sprintf("-[%02d%02d　%s]", (int) $row["TESTITEMCD"],(int) $row["TESTKINDCD"], htmlspecialchars($row["TESTITEMNAME"]));
	    }
    }		*/
    Query::dbCheckIn($db);
}
$objForm->add_element(array("type"      => "checkbox",
                            "name"      => "chk_all",
                            "extrahtml"   => "disabled" ));

$arg["CHECK_ALL"] = $objForm->ge("chk_all");

//ラジオボタンを作成する
$objForm->add_element(array("type"       => "radio",
                            "value"      => isset($model->field["OUT2"])?$model->field["OUT2"]:1,
                            "name"      => "OUT2"));

$arg["OUTPUT3"] = $objForm->ge("OUT2",1);
$arg["OUTPUT4"] = $objForm->ge("OUT2",2);

// 04/10/30 テスト種別リストを追加
if($model->semester != "3" )
{
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
$objForm->ae( array("type"       => "select",
                    "name"       => "TESTKINDCD",
                    "size"       => "1",
                    "value"      => $model->testkindcd,
                    "options"    => $opt_kind));
$arg["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

//ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_ok",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return opener_submit('" . SERVLET_URL . "');\"" ) );

$arg["btn_ok"] = $objForm->ge("btn_ok");

//ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_can",
                    "value"       => " 終 了 ",
                    "extrahtml"   => "onClick=\"closeWin();\"" ));

$arg["btn_can"] = $objForm->ge("btn_can");

//タイトル
$arg["TITLE"] = $title;

//年度・学期（表示）
if (($model->year != "") && ($model->semester != "")) {
    $arg["YEAR_SEMESTER"] = $model->year."年度&nbsp;" .$model->control["学期名"][$model->semester]."&nbsp;";
}

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJD080"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"     => $model->year,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEMESTER",
                    "value"     => $model->semester,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "CLASSCD",
                    "value"     => $model->classcd,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SUBCLASSCD",
                    "value"     => $subclasscd,
                    ) );

/* 04/10/30 カット
$objForm->ae( array("type"      => "hidden",
                    "name"      => "TESTKINDCD",
                    "value"     => $model->testkindcd,
                    ) );
*/
$objForm->ae( array("type"      => "hidden",
                    "name"      => "TESTITEMCD",
                    "value"     => $model->testitemcd,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "ATTENDCLASSCD"
                    ) );

//2004-07-30 naka
$objForm->ae( array("type"      => "hidden",
                    "name"      => "STAFF_CD"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "ATTENDCLASSCD1"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "ATTENDCLASSCD2"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GROUPCD"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TARGETCLASS"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TAI"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TOKE"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "DISP"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );




$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd080Form1.html", $arg); 
}
}
?>
