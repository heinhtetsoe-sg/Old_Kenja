<?php

require_once('for_php7.php');

/********************************************************************/
/* テスト個人成績表                                 山城 2005/04/12 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjd173Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成/////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd173Form1", "POST", "knjd173index.php", "", "knjd173Form1");


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
					"extrahtml"	 => "onchange=\"return btn_submit('gakki'),AllClearList();\"",
                    "options"    => $opt ) );

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

//テストコンボ作成
/*
$opt_kind[0]= array('label' => '中間テスト',
   	      	        'value' => '01');
$opt_kind[1]= array('label' => '期末テスト',
   	   	            'value' => '02');

if ($model->field["TESTKINDCD"] == "") $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

$objForm->ae( array("type"       => "select",
                    "name"       => "TESTKINDCD",
                    "size"       => "1",
                    "value"      => $model->field["TESTKINDCD"],
					"extrahtml"	 => "",
                    "options"    => $opt_kind));

$arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");
*/

//クラス選択コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjd173Query::getAuth($model->control["年度"],$ga);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

if(!isset($model->field["GRADE_HR_CLASS"])) {
    $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
}

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE_HR_CLASS",
                    "size"       => "1",
                    "value"      => $model->field["GRADE_HR_CLASS"],
					"extrahtml"	 => "onchange=\"return btn_submit('knjd173'),AllClearList();\"",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");


//異動対象日付
if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
$arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);


    //保護者の住所を出力するチェックボックスを作成する 2005.08.17
   	$chk = ($model->field["OUTPUT"] == "on") ? "checked" : "" ;

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUT",
    					"value"		=> "on",
    					"extrahtml"	=> $chk ) );

    $arg["data"]["OUTPUT"] = $objForm->ge("OUTPUT");

    //認定単位・委員会・部活動の表記なしチェックボックスを作成する 2005.09.14
   	$chk = ($model->field["OUTPUT2"] == "on") ? "checked" : "" ;

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUT2",
    					"value"		=> "on",
    					"extrahtml"	=> $chk ) );

    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2");

    //遅刻回数・早退回数の表記なしチェックボックスを作成する---2005.09.28
   	$chk3 = ($model->field["OUTPUT3"] == "on") ? "checked" : "" ;

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUT3",
    					"value"		=> "on",
    					"extrahtml"	=> $chk3 ) );

    $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

//対象外の生徒取得
$db = Query::dbCheckOut();
$query = knjd173Query::getSchnoIdou($model,$ga);
$result = $db->query($query);
$opt_idou = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_idou[] = $row["SCHREGNO"];
}
$result->free();
Query::dbCheckIn($db);

//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
	$db = Query::dbCheckOut();
	$query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW ";
	$query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2 ";
	$query .= "WHERE T2.YEAR = '".$model->control["年度"]."' AND ";
	$query .= "		 T2.SEMESTER = '".$ga."' AND ";
	$query .= "		 T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
	$query .= "		 T2.SCHREGNO = T1.SCHREGNO ";
	$query .= "ORDER BY T2.ATTENDNO ";

	$result = $db->query($query);
	$opt1 = array();
	while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		if (in_array($row["SCHREGNO"],$opt_idou)) {
		    $opt1[] = array('label' => $row["SCHREGNO"]."●".$row["ATTENDNO"]."番●".$row["NAME_SHOW"],
		                    'value' => $row["SCHREGNO"]);
		} else {
		    $opt1[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
		                    'value' => $row["SCHREGNO"]);
		}
	}
	$result->free();
	Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"",
                    "size"       => "20",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


//生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"",
                    "size"       => "20",
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
$objForm->ae( array("type"		   => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//csvボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" 		  => "button",
                    "name"        => "btn_csv",
                    "value"       => "ＣＳＶ出力",
                    "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

$arg["button"]["btn_csv"] = $objForm->ge("btn_csv");
//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" 		  => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


// 2004/02/12 nakamoto 証明日付カット
//$arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJD173"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

	$objForm->ae( array("type"      => "hidden",
	                    "name"      => "selectdata") );  


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd173Form1.html", $arg); 
}
}
?>
