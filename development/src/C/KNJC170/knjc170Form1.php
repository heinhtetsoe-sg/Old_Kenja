<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：ラジオボタンを追加                       山城 2005/02/05 */
/* ･NO002：学期をコンボボックスに変更               山城 2005/02/05 */
/* ･NO003：3学期指定でも、全学年表示                山城 2005/02/05 */
/* ･NO004：学期指定時の日付範囲チェック追加         山城 2005/02/05 */
/* ･NO005：チェックボックス追加                     山城 2005/03/07 */
/* ･NO006：大幅変更ほぼ新規                         山城 2005/04/05 */
/* ･NO007：月指定可能に戻す                         山城 2005/10/18 */
/********************************************************************/

class knjc170Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成
$arg["start"]   = $objForm->get_start("knjc170Form1", "POST", "knjc170index.php", "", "knjc170Form1");


//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );

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

$db = Query::dbCheckOut();
$query = knjc170Query::GetDate($model);
$result = $db->query($query);

$row = $result->fetchRow(DB_FETCHMODE_ASSOC);

$result->free();
Query::dbCheckIn($db);

$sdate = preg_split("/-/",$row["SDATE"]);
$edate = preg_split("/-/",$row["EDATE"]);
if ($model->field["NENGETSU_FROM"] == '') $model->field["NENGETSU_FROM"] = $sdate[0]."-".$sdate[1];
if ($model->field["NENGETSU_TO"] == '') $model->field["NENGETSU_TO"] = $edate[0]."-".$edate[1];
/*
$objForm->ae( array("type"       => "select",
                    "name"       => "NENGETSU_FROM",
                    "size"       => "1",
                    "value"      => $model->field["NENGETSU_FROM"],
        			"extrahtml"  => "",
//        			"extrahtml"  => "onChange=\"return date_check();\"",
                    "options"    => isset($rowg)?$rowg:array()));
*/
$objForm->ae( array("type"       => "select",
                    "name"       => "NENGETSU_TO",
                    "size"       => "1",
                    "value"      => $model->field["NENGETSU_TO"],
        			"extrahtml"  => "",
//        			"extrahtml"  => "onChange=\"return date_check();\"",
                    "options"    => isset($rowg)?$rowg:array()));

//$arg["data"]["NENGETSU_FROM"] = $objForm->ge("NENGETSU_FROM");
$arg["data"]["NENGETSU_FROM"] = $rowg[1]["label"];
$arg["data"]["NENGETSU_TO"] = $objForm->ge("NENGETSU_TO");

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
                    "value"     => "KNJC170"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjc170Form1.html", $arg); 
}
}
?>
