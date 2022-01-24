<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：学年末指定した場合、今学期のデータを使用 山城 2004/10/26 */
/********************************************************************/

class knjd230Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd230Form1", "POST", "knjd230index.php", "", "knjd230Form1");


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

	//９＝学年を追加
	$opt[] = array("label" => $model->control["学期名"][9],
					"value" =>  sprintf("%d", 9)
				);

	//学期リストボックスで「学年末」が選ばれている時は、「今学期」と同じ扱いでクラスを取得する
	if(isset($model->field["GAKKI"]))
	{
		if($model->field["GAKKI"] == 9 )
		{
			$ga = $model->control["学期"];	/* NO001 */
		}
		else
		{
			$ga = $model->field["GAKKI"];
		}
	}
	else
	{
		$ga =$model->control["学期"];
	}
}

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKKI",
                    "size"       => "1",
                    "value"      => isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"],
                    "extrahtml"  => "onChange=\"return btn_submit('knjd230');\"",
                    "options"    => $opt ) );

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


//クラス選択コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjd230Query::getAuth($model->control["年度"],$ga);
/*
$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
            "FROM SCHREG_REGD_HDAT ".
            "WHERE YEAR='" .$model->control["年度"] ."'".
			"AND SEMESTER='".$ga ."'";
*/
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE_HR_CLASS",
                    "size"       => "1",
                    "value"      => $model->field["GRADE_HR_CLASS"],
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");


//出欠状況チェックボックスを作成////////////////////////////////////////////////////////////////////////////////
if($model->field["KINTAI"] == "on")
{
	$check_kintai = "checked";
}
else
{
	$check_kintai = "";
}

$objForm->ae( array("type" 		=> "checkbox",
                    "name"      => "KINTAI",
					"value"		=> "on",
					"extrahtml"	=> $check_kintai ) );

$arg["data"]["KINTAI"] = $objForm->ge("KINTAI");


//異動チェックボックスを作成////////////////////////////////////////////////////////////////////////////////
if($model->field["IDO"] == "on")
{
	$check_ido = "checked";
}
else
{
	$check_ido = "";
}

$objForm->ae( array("type" 		=> "checkbox",
                    "name"      => "IDO",
					"value"		=> "on",
					"extrahtml"	=> $check_ido ) );

$arg["data"]["IDO"] = $objForm->ge("IDO");


//皆出席者チェックボックスを作成////////////////////////////////////////////////////////////////////////////////
if($model->field["ALL"] == "on")
{
	$check_all = "checked";
}
else
{
	$check_all = "";
}

$objForm->ae( array("type" 		=> "checkbox",
                    "name"      => "ALL",
					"value"		=> "on",
					"extrahtml"	=>"onClick=\"ch_all(this);\"".$check_all ) );

$arg["data"]["ALL"] = $objForm->ge("ALL");


//皆出席開始日を作成する/////////////////////////////////////////////////////////////////////////////////
if($model->field["ALL"] == "on")
{
//	$dis_date  = "";                        //指定日付テキスト使用可
    $arg["Dis_Date"]  = " dis_date(false); " ;
}
else
{
//	$dis_date  = "disabled";                //指定日付テキスト使用不可
    $arg["Dis_Date"]  = " dis_date(true); " ;
}
$value = isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"];
/*
$name  = "DATE";
$param = "name=$name&";
//テキストエリア
$objForm->ae( array("type"        => "text",
					"name"        => $name,
                    "size"        => 12,
                    "maxlength"   => 12,
                    "extrahtml"   => "$dis_date onblur=\"isDate(this)\"",
                    "value"       => $value));

//読込ボタンを作成する
$objForm->ae( array("type" 		  => "button",
                    "name"        => "btn_calen",
                    "value"       => "...",
                    "extrahtml"   => "$dis_date onclick=\"loadwindow('" .REQUESTROOT ."/common/cal.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value , event.x, event.y, 300, 200)\"") );

$arg["data"]["DATE"] = View::setIframeJs() .$objForm->ge($name) .$objForm->ge("btn_calen");
*/
$arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",$value);


//成績優秀者チェックボックスを作成////////////////////////////////////////////////////////////////////////////
if($model->field["SEISEKI"] == "on")
{
	$check_seiseki = "checked";
}
else
{
	$check_seiseki = "";
}

$objForm->ae( array("type" => "checkbox",
                    "name"        => "SEISEKI",
					"value"		=> "on",
					"extrahtml"	=>"onClick=\"ch_seiseki(this);\"".$check_seiseki ) );

$arg["data"]["SEISEKI"] = $objForm->ge("SEISEKI");


//評定平均（以上）テキストボックスを作成する//////////////////////////////////////////////////////////////////////
if ($model->field["SEISEKI"] == "on")
{
	$over_dis="";
	if($model->field["GAKKI"] == "9")
	{
		$yover = 4.3;
	}
	else
	{
		$yover = 80;
	}
}
else
{
	$over_dis="disabled";
}

$objForm->ae( array("type"       => "text",
                    "name"       => "H_OVER",
                    "size"       => "3",
                    "value"      => $yover,
					"extrahtml"  => "onBlur=\"return OverCheck(this);\"". $over_dis ) );

$arg["data"]["H_OVER"] = $objForm->ge("H_OVER");


//評定平均（未満）テキストボックスを作成する//////////////////////////////////////////////////////////////////////
if ($model->field["SEISEKI"] == "on")
{
	$under_dis="";
	if($model->field["GAKKI"] == "9")
	{
		$yunder = 1;
	}
	else
	{
		$yunder= 35;
	}
}
else
{
	$under_dis="disabled";
}

$objForm->ae( array("type"       => "text",
                    "name"       => "H_UNDER",
                    "size"       => "3",
                    "value"      => $yunder,
					"extrahtml"  => "onBlur=\"return UnderCheck(this);\"". $under_dis) );

$arg["data"]["H_UNDER"] = $objForm->ge("H_UNDER");


//単位保留懸念者チェックボックスを作成////////////////////////////////////////////////////////////////////////////
if($model->field["HORYU"] == "on")
{
	$check_horyu = "checked";
	$asses_dis="";
}
else
{
	$check_horyu = "";
	$asses_dis="disabled";
}

$objForm->ae( array("type" 		=> "checkbox",
                    "name"      => "HORYU",
					"value"		=> "on",
					"extrahtml"	=> "onclick=\"ck_horyu('this');\"".$check_horyu ) );
//					"extrahtml"	=> $check_horyu ) );

$arg["data"]["HORYU"] = $objForm->ge("HORYU");

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


//csvボタンを作成する///////////////////2004/07/07 add nakamoto///////////////////////////////////////////
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
                    "value"     => "KNJD230"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり/////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 ///////////////////////////////////////////////////////////
View::toHTML($model, "knjd230Form1.html", $arg); 
}
}
?>
