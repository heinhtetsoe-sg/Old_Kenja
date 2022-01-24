<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：ラジオボタンを追加                       山城 2005/02/05 */
/* ･NO002：学期をコンボボックスに変更               山城 2005/02/05 */
/* ･NO003：3学期指定でも、全学年表示                山城 2005/02/05 */
/* ･NO004：学期指定時の日付範囲チェック追加         山城 2005/02/05 */
/********************************************************************/

class knjc150Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成///////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjc150Form1", "POST", "knjc150index.php", "", "knjc150Form1");


//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );


//学期リスト
$db = Query::dbCheckOut();
$query = knjc150Query::getSemester($model);
$result = $db->query($query);
$opt_seme = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_seme[]= array('label' 	=> $row["SEMESTERNAME"],
                    	'value' => $row["SEMESTER"]);
}
$result->free();
Query::dbCheckIn($db);

if ($model->field["GAKKI"]=="") $model->field["GAKKI"] = CTRL_SEMESTER;

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKKI",
                    "size"       => "1",
                    "value"      => $model->field["GAKKI"],
					"extrahtml"  => "onChange=\"return btn_submit('knjc150');\"",
                    "options"    => $opt_seme));

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

//学年リストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjc150Query::getSelectGrade($model);
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
					"extrahtml"	 => "multiple") );

$arg["data"]["GRADE"] = $objForm->ge("GRADE");


//所属選択コンボボックスを作成

$selectyear = $model->control["年度"];
$next = $selectyear + 1;

$rowg[1]["label"] = $selectyear."年 4月";
$rowg[2]["label"] = $selectyear."年 5月";
$rowg[3]["label"] = $selectyear."年 6月";
$rowg[4]["label"] = $selectyear."年 7月";
$rowg[5]["label"] = $selectyear."年 8月";
$rowg[6]["label"] = $selectyear."年 9月";
$rowg[7]["label"] = $selectyear."年 10月";
$rowg[8]["label"] = $selectyear."年 11月";
$rowg[9]["label"] = $selectyear."年 12月";
$rowg[10]["label"] = $next."年　1月";
$rowg[11]["label"] = $next."年　2月";
$rowg[12]["label"] = $next."年　3月";
$rowg[1]["value"] = $selectyear."-04";
$rowg[2]["value"] = $selectyear."-05";
$rowg[3]["value"] = $selectyear."-06";
$rowg[4]["value"] = $selectyear."-07";
$rowg[5]["value"] = $selectyear."-08";
$rowg[6]["value"] = $selectyear."-09";
$rowg[7]["value"] = $selectyear."-10";
$rowg[8]["value"] = $selectyear."-11";
$rowg[9]["value"] = $selectyear."-12";
$rowg[10]["value"] = $next."-01";
$rowg[11]["value"] = $next."-02";
$rowg[12]["value"] = $next."-03";

$sday = common::DateConv1($model->control["学期開始日付"][$model->field["GAKKI"]],5);
$eday = common::DateConv1($model->control["学期終了日付"][$model->field["GAKKI"]],5);

$sday = str_replace("/","-",$sday);
$eday = str_replace("/","-",$eday);

$model->field["NENGETSU_FROM"] = $sday;

$objForm->ae( array("type"       => "select",
                    "name"       => "NENGETSU_FROM",
                    "size"       => "1",
                    "value"      => $model->field["NENGETSU_FROM"],
        			"extrahtml"  => "onChange=\"return date_check();\"",
                    "options"    => isset($rowg)?$rowg:array()));

$objForm->ae( array("type"      => "hidden",
                    "name"      => "sday",
                    "value"      => $sday
                    ) );

$model->field["NENGETSU_TO"] = $eday;

$objForm->ae( array("type"       => "select",
                    "name"       => "NENGETSU_TO",
                    "size"       => "1",
                    "value"      => $model->field["NENGETSU_TO"],
        			"extrahtml"  => "onChange=\"return date_check();\"",
                    "options"    => isset($rowg)?$rowg:array()));

$objForm->ae( array("type"      => "hidden",
                    "name"      => "eday",
                    "value"      => $eday
                    ) );

$arg["data"]["NENGETSU_FROM"] = $objForm->ge("NENGETSU_FROM");
$arg["data"]["NENGETSU_TO"] = $objForm->ge("NENGETSU_TO");

//帳票選択ラジオボタンを作成 NO001////////////////////////////////////////////////////////////////////////
$opt[0]=1;
$opt[1]=2;
if(!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUT",
					"value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:1,
        			"extrahtml"  => "onclick=\"kubun(this);\"",
					"multiple"   => $opt));

$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

//add 04/08/31  yamauchi
if (isset($model->field["OUTPUT"]))
{
	switch ($model->field["OUTPUT"])
    {
    	case 1:
        	$ratio = "";
        	break;
        case 2:
        	$ratio = "disabled";
        	break;
    }
}
else
{
	$ratio = "";
}

//対象日数コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
$row2 = array(array('label' => "5",'value'  => "5"),
			  array('label' => "10",'value' => "10"),
			  array('label' => "15",'value' => "15") );

$objForm->ae( array("type"       => "select",
                    "name"       => "DAYS",
                    "size"       => "1",
                    "value"      => isset($model->field["DAYS"])?$model->field["DAYS"]:10,
        			"extrahtml"	 => $ratio,
                    "options"    => $row2));

$arg["data"]["DAYS"] = $objForm->ge("DAYS");


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
                    "value"     => "KNJC150"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "useCurriculumcd",
                    "value"     => $model->Properties["useCurriculumcd"]
                    ) );

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjc150Form1.html", $arg); 
}
}
?>
