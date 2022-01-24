<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：学年末指定した場合、今学期のデータを使用 山城 2004/10/26 */
/* ･NO002：テスト項目の設定をDBからでなく、直接設定 山城 2004/10/26 */
/* ･NO003：テスト項目の設定を３学期は、期末のみ設定 山城 2004/11/29 */
/********************************************************************/

class knjd061Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd061Form1", "POST", "knjd061index.php", "", "knjd061Form1");


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

	//４＝学年を追加
	$opt[] = array("label" => $model->control["学期名"][9],
					"value" => sprintf("%d", 9)
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
					"extrahtml"  => "onchange=\"return btn_submit('');\"",
                    "options"    => $opt ) );

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

//テスト名コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
$row2[] = array("label" => "","value" => "0");

if($model->field["GAKKI"] != 9 )
{
	if ($model->field["GAKKI"] != 3 )	/* NO003 */
	{
		/* NO002 ↓ */
	   	$row2[]= array('label' => '0101　中間テスト',
        		       'value' => '0101');
	   	$row2[]= array('label' => '0201　期末テスト',
    	    	       'value' => '0201');
		/* NO002 ↑ */
	} else {
	   	$row2[]= array('label' => '0201　期末テスト',
    	    	       'value' => '0201');
	}
}

$objForm->ae( array("type"       => "select",
                    "name"       => "TEST",
                    "size"       => "1",
                    "value"      => $model->field["TEST"],
                    "extrahtml"   => "STYLE=\"WIDTH:130\" ",
                    "options"    => isset($row2)?$row2:array()));

$arg["data"]["TEST"] = $objForm->ge("TEST");


//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
$query = knjd061Query::getAuth($model->control["年度"],$ga);
/*
$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
            "FROM SCHREG_REGD_HDAT ".
            "WHERE YEAR='" .$model->control["年度"] ."'".
			"AND SEMESTER='".$ga ."'";
*/
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt1[]= array('label' =>  $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


//生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
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
                    "value"     => "KNJD061"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり/////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 ///////////////////////////////////////////////////////////
View::toHTML($model, "knjd061Form1.html", $arg); 
}
}
?>
