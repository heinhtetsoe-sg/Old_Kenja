<?php

require_once('for_php7.php');


class knjd200Form1
{
    function main(&$model){

$objForm = new form;
//フォーム作成
$arg["start"]   = $objForm->get_start("knjd200Form1", "POST", "knjd200index.php", "", "knjd200Form1");


//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////
$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"],
                    ) );


$opt=array();

$arg["data"]["SEME_SHOW"] = $model->control["学期名"][$model->control["学期"]];


//カレンダーコントロール
$arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

//クラス選択コンボボックスを作成する
$db = Query::dbCheckOut();
$query = knjd200Query::getAuth($model->control["年度"],$model->control["学期"]);
/*
$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
            "FROM SCHREG_REGD_HDAT ".
            "WHERE YEAR='" .$model->control["年度"] ."'".
            "AND SEMESTER='" .$model->control["学期"] ."'"; 
*/
$row1=array();
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
					"extrahtml"  => "onchange=\"return btn_submit('knjd200');\"",
                    "options"    => $row1));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

//クラス選択コンボボックスを作成する
$db = Query::dbCheckOut();
$query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
            "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
            "WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ".
			"((SCHREG_REGD_DAT.SEMESTER)='" .$model->control["学期"] ."') AND ".
//			"((SCHREG_REGD_DAT.GRADE)='" .substr($model->field["GRADE_HR_CLASS"],0,2) ."') AND ".
//			"((SCHREG_REGD_DAT.HR_CLASS)='" .substr($model->field["GRADE_HR_CLASS"],2,2) ."'))".
			"((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))".	//  04/07/23  yamauchi
            "ORDER BY ATTENDNO";      //出席番号でソート
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt1[]= array('label' =>  $row["NAME"],
                    'value' => $row["SCHREGNO"]);
}
$result->free();
Query::dbCheckIn($db);

//対象者リストを作成する
$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
					"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move('right')\"",
                    "size"       => "20",
                    "options"    => array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

//生徒一覧リストを作成する
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
					"extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move('left')\"",
                    "size"       => "20",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");
/*
//読込ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_read",
                    "value"       => "読　込",
                    "extrahtml"   => "onclick=\"return btn_submit('knjd200');\"" ) );

$arg["button"]["btn_read"] = $objForm->ge("btn_read");
*/
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


//対象取消ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('right');\"" ) );

$arg["button"]["btn_right"] = $objForm->ge("btn_right");

//対象選択ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('left');\"" ) );

$arg["button"]["btn_left"] = $objForm->ge("btn_left");

//印刷ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");

//終了ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJD200"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//学期用データ
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEMESTER",
                    "value"     => $model->control["学期"]
                    ) );

$arg["finish"]  = $objForm->get_finish();
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd200Form1.html", $arg); 
}
}
?>
