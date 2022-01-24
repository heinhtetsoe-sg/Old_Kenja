<?php

require_once('for_php7.php');


class knjc040Form1
{
    function main(&$model){

$objForm = new form;
//フォーム作成
$arg["start"]   = $objForm->get_start("knjc040Form1", "POST", "knjc040index.php", "", "knjc040Form1");

$opt=array();

//カレンダーコントロール１//////////////////////////////////////////////
$value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];
$arg["el"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $value);


//カレンダーコントロール２//////////////////////////////////////////////
$value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];
$arg["el"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $value2);


//学期期間日付取得///////////////////////////////////////////////////////////////////////////////////////////////
$opt_seme=array();
if (is_numeric($model->control["学期数"])){
    for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
		$opt_seme[$i*2] = $model->control['学期開始日付'][$i+1];
		$opt_seme[$i*2+1] = $model->control['学期終了日付'][$i+1];
		//学期を取得する
		if( ($opt_seme[$i*2] <= $value) && ($value2 <= $opt_seme[$i*2+1]) )
		{
			$seme = $i+1;
		}
    }
}

$semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
$semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
$semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];


//クラス選択コンボボックスを作成する
$db = Query::dbCheckOut();
$query = knjc040Query::getAuth($model->control["年度"],$seme);
/*
$query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
            "FROM SCHREG_REGD_HDAT ".
            "WHERE YEAR='" .$model->control["年度"] ."' ".
            "AND SEMESTER='" .$seme ."'";      
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


//ラジオボタンを作成//累計種別（学期間/印刷範囲）
$opt[0]=1;
$opt[1]=2;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUT2",
					"value"      => isset($model->field["OUTPUT2"])?$model->field["OUTPUT2"]:"1",
					"multiple"   => $opt));

$arg["data"]["OUTPUT21"] = $objForm->ge("OUTPUT2",1);
$arg["data"]["OUTPUT22"] = $objForm->ge("OUTPUT2",2);

//読込ボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_read",
                    "value"       => "読込",
                 "extrahtml"   => "onclick=\"return btn_submit();\"" ) );
$arg["button"]["btn_read"] = $objForm->ge("btn_read");


//印刷ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");

//終了ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");

////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJC040"
                    ) );

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//年度データ
$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"     => $model->control["年度"]
                    ) );

$arg["data"]["YEAR"] = $model->control["年度"];


//学期
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEMESTER",
                    "value"     => $seme
                    ) );

$arg["data"]["SEMESTER"] = $model->control["学期名"][$seme];

//学期開始日
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME_DATE",
                    "value"     => $semester
                    ) );


$arg["finish"]  = $objForm->get_finish();
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjc040Form1.html", $arg); 

}
}
?>
