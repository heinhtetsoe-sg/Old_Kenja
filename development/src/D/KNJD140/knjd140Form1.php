<?php

require_once('for_php7.php');


class knjd140Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjd140Form1", "POST", "knjd140index.php", "", "knjd140Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////
$arg["data"]["YEAR"] = $model->control["年度"];


//学期コンボの設定/////////////////////////////////////////////////////////////////////////////////////////////
//if (is_numeric($model->control["学期数"]))
//{
//    for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ )
//	{
//        $opt[]= array("label" => $model->control["学期名"][$i+1],
//                      "value" => sprintf("%d", $i+1)
//                     );
//    }
// 2004/02/12 nakamoto 学年末カット
	//学年末を追加
//        $opt[]= array("label" => $model->control["学期名"][9], 
//                      "value" => sprintf("%d", 9)
//		              );
//}
$db = Query::dbCheckOut();

$query = knjd140Query::getSelectSemester($model);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt[]= array('label' => $row["SEMESTERNAME"],
                    'value' => $row["SEMESTER"]);
}

if(!isset($model->field["GAKKI"])) {
    $model->field["GAKKI"]=$model->control["学期"];
}

$objForm->ae( array("type"       => "select",
					"name"		 => "GAKKI",
					"size"		 => "1",
					"value" 	 => $model->field["GAKKI"],
					"extrahtml"  => "onchange=\"return btn_submit('gakki');\"",
					"options"	 => isset($opt)?$opt:array() ) );

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


//クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
// 2004/02/12 nakamoto 学年末カット
//if($model->field["GAKKI"] == 9 )
//{
//	$query = "SELECT DISTINCT substr(T1.SUBCLASSCD,1,2) AS VALUE,T2.CLASSNAME AS LABEL ".
//				"FROM chair_dat T1 INNER JOIN class_mst T2 ".
//				"ON substr(T1.SUBCLASSCD,1,2)=T2.CLASSCD ".
//				"WHERE ((T1.YEAR='".$model->control["年度"]."') ".
//				"AND (substr(T1.SUBCLASSCD,1,2)>='"."01"."') ".
//				"AND (substr(T1.SUBCLASSCD,1,2)<='"."49"."'))".
//				"order by substr(T1.SUBCLASSCD,1,2)";
//}
//else
//{
//	$query = "SELECT DISTINCT substr(T1.SUBCLASSCD,1,2) AS VALUE,T2.CLASSNAME AS LABEL ".
//				"FROM chair_dat T1 INNER JOIN class_mst T2 ".
//				"ON substr(T1.SUBCLASSCD,1,2)=T2.CLASSCD ".
//				"WHERE ((T1.YEAR='".$model->control["年度"]."') ".
//				"AND (T1.SEMESTER='".$model->field["GAKKI"]."') ".
//				"AND (substr(T1.SUBCLASSCD,1,2)>='"."01"."') ".
//				"AND (substr(T1.SUBCLASSCD,1,2)<='"."49"."'))".
//				"order by substr(T1.SUBCLASSCD,1,2)";
//}

$query = knjd140Query::getSelectClass($model);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["VALUE"]." ".$row["LABEL"],
                    'value' => $row["VALUE"]);
}

$result->free();
Query::dbCheckIn($db);


$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('left')\"",
                    "size"       => "15",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


//出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('right')\"",
                    "size"       => "15",
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
                    "value"     => "KNJD140"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//年度データ
$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"     => $model->control["年度"]
                    ) );




//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd140Form1.html", $arg); 
}
}
?>
