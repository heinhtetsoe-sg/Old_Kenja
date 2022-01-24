<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：学年末指定した場合、今学期のデータを使用 山城 2004/10/26 */
/********************************************************************/

class knjd231Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成
$arg["start"]   = $objForm->get_start("knjd231Form1", "POST", "knjd231index.php", "", "knjd231Form1");


//年度テキストボックスを作成する
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );

//学期リスト作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjd231Query::getSemester_mst();
$result = $db->query($query);

$opt_seme = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_seme[]= array('label' => $row["LABEL"],
                  	   'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

if (!isset($model->field["GAKKI"])) $model->field["GAKKI"] = CTRL_SEMESTER;

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKKI",
                    "size"       => "1",
                    "value"      => $model->field["GAKKI"],
                    "extrahtml"  => "onChange=\"return btn_submit('knjd231');\"",
                    "options"    => $opt_seme ) );

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

if ($model->field["GAKKI"]=="9"){
	$semester = $model->control["学期"];	/* NO001 */
} else {
	$semester = $model->field["GAKKI"];
}


//クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$opt_left = $opt_right = array();

$query = knjd231Query::getAuth($semester);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	if (!isset($model->warning))
	{
			$opt_right[] = array("label" => $row["LABEL"],
								 "value" => $row["VALUE"]);
	} else {
		if ( strstr($model->selectdata,$row["VALUE"]) ){
			$opt_left[]  = array("label" => $row["LABEL"],
								 "value" => $row["VALUE"]);
		} else {
			$opt_right[] = array("label" => $row["LABEL"],
								 "value" => $row["VALUE"]);
		}
	}
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => $opt_right));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


//出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
                    "options"    => $opt_left));

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


//評価区分選択コンボボックスを作成/////  //add  04/07/06  nakamoto  ///////////////////////////////////////////////
$db = Query::dbCheckOut();

	if($model->field["GAKKI"] == "9")
	{
		$query2 = "SELECT ASSESSLEVEL AS VALUE, ASSESSMARK AS LABEL ".
			  	  "FROM ASSESS_MST WHERE ASSESSCD='3' ORDER BY ASSESSLEVEL";

		$result2 = $db->query($query2);
		while($rowg = $result2->fetchRow(DB_FETCHMODE_ASSOC))
		{
    		$row2[]= array('label' => $rowg["LABEL"],
    					   'value' => $rowg["VALUE"]);
		}
		$result2->free();
		Query::dbCheckIn($db);

	}
	else
	{
		if($model->control["学期末評価区分"] == "2")
		{
			$query2 = "SELECT ASSESSLEVEL AS VALUE, ASSESSMARK AS LABEL ".
					  "FROM ASSESS_MST WHERE ASSESSCD='2' ORDER BY ASSESSLEVEL";

			$result2 = $db->query($query2);
			while($rowg = $result2->fetchRow(DB_FETCHMODE_ASSOC))
			{
    			$row2[]= array('label' => $rowg["LABEL"],
    						   'value' => $rowg["VALUE"]);
			}
			$result2->free();
			Query::dbCheckIn($db);

		}
		else
		{
			for ($i=0; $i<101; $i++)
			{
    			$row2[]= array('label' => $i,
    						   'value' => $i);
			}

		}
	}


$objForm->ae( array("type"       => "select",
                    "name"       => "ASSESS",
                    "size"       => "1",
                    "value"      => $model->field["ASSESS"],
                    "options"    => isset($row2)?$row2:array(),
					"extrahtml"	 => $asses_dis ) );

$arg["data"]["ASSESS"] = $objForm->ge("ASSESS");



//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//csvボタンを作成する//////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_csv",
                    "value"       => "ＣＳＶ出力",
                    "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

$arg["button"]["btn_csv"] = $objForm->ge("btn_csv");


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
                    "value"     => "KNJD231"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );
//echo str_replace("/","-",$model->control["学期終了日付"][$model->field["GAKKI"]]);
	$objForm->ae( array("type"      => "hidden",
	                    "name"      => "selectdata") );  

//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd231Form1.html", $arg); 
}
}
?>
