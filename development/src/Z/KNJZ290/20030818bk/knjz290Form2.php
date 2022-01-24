<?php

require_once('for_php7.php');

require_once('knjz290Query.inc');
//権限チェック
if (common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE){
	$arg["jscript"] = "OnAuthError();";
}

$objForm = new form;
//フォーム作成
$arg["start"]   = $objForm->get_start("edit", "POST", "knjz290index.php", "", "edit");
//警告メッセージを表示しない場合

//if (!isset($model->warning))
//{
//	$Row = knjz290Query::getRow($model->staffcd);
//}
//else
//{
//	$Row =& $model->field;
//}

//年度の取得///////////////////////////////////////////////////////////////////////////////////////
$default_year	= 0;
$listyear	= 0;
$db 	= Query::dbCheckOut();
$result    = $db->query(knjz290Query::selectYearQuery());
$opt	   = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
{
	$opt[] = array("label" => $row["STAFFYEAR"],
				   "value" => $row["STAFFYEAR"]);
	if($row["STAFFYEAR"] == $model->control["年度"])
	{
		$default_year = 1;
	}
}

if(isset($model->year))		//２回目以降の読込の場合の年度（前画面から）
{
	$listyear = $model->year;
}
else						//最初に画面を読込んだ場合の年度
{
	if($default_year == 1)
	{
		$listyear = $model->control["年度"];	//職員年度マスタに今年度があった場合それを表示
	}
	else
	{
		$listyear = $opt[0]["value"];			//職員年度マスタに今年度がなかった場合、最新の年度を表示
	}											//（年度のリストはDESC指定）
}

//echo "opt[0]["value"]=".$opt[0]["value"];
//echo "listyear=".$listyear;


//職員年度リストでselectされたデータの情報の取得///////////////////////////////////////////////////


//職員コード///////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "text",
                    "name"        => "STAFFCD",
                    "size"        => 6,
                    "maxlength"   => 6,
                    "extrahtml"   => "readonly",
                    "value"       => ""));

	$arg["data"]["STAFFCD"] = $objForm->ge("STAFFCD");

//職員氏名/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "text",
                    "name"        => "NAME",
                    "size"        => 20,
                    "maxlength"   => 20,
                    "extrahtml"   => "readonly",
                    "value"       => "" ));

$arg["data"]["NAME"] = $objForm->ge("NAME");

//職員氏名（名）//////////////////////////////////////////////////////////////////////////////////////////
//$objForm->ae( array("type"        => "text",
//                    "name"        => "STAFFFNAME",
//                    "size"        => 20,
//                    "maxlength"   => 20,
//                    "extrahtml"   => disabled,
//                    "value"       => "" ));

//$arg["data"]["STAFFFNAME"] = $objForm->ge("STAFFFNAME");

//校務分掌部リスト//////////////////////////////////////////////////////////////////////////////////////////
$db     = Query::dbCheckOut();
$query  = knjz290Query::getVDutyshareMst_data($listyear);
$result = $db->query($query);
$opt_sha = array();

$opt_sha[] = array("label" => "","value" => "");

while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
      $opt_sha[] = array("label" => htmlspecialchars($row["SHARENAME"]),"value" => $row["DUTYSHARECD"]);
}

$objForm->ae( array("type"        => "select",
                    "name"        => "SHARENAME",
                    "size"        => 1,
                    "extrahtml"   => "onChange=\"\"",
//                    "value"       => $Row["DUTYSHARECD"],
					"options"     => $opt_sha
					));

$arg["data"]["SHARENAME"] = $objForm->ge("SHARENAME");

//職名リスト//////////////////////////////////////////////////////////////////////////////////////////
$db     = Query::dbCheckOut();
$query  = knjz290Query::getVJobMst_data($listyear);
$result = $db->query($query);
$opt_job = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
      $opt_job[] = array("label" => htmlspecialchars($row["JOBNAME"]),"value" => $row["JOBCD"]);
}

$objForm->ae( array("type"        => "select",
                    "name"        => "JOBNAME",
                    "size"        => 1,
                    "extrahtml"   => "onChange=\"\"",
//                    "value"       => $Row["JOBNAMECD"],
					"options"     => $opt_job
					));

$arg["data"]["JOBNAME"] = $objForm->ge("JOBNAME");

//所属リスト//////////////////////////////////////////////////////////////////////////////////////////
$db     = Query::dbCheckOut();
$query  = knjz290Query::getVSectionMst_data($listyear);
$result = $db->query($query);
$opt_sec = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
      $opt_sec[] = array("label" => htmlspecialchars($row["SECTIONNAME"]),"value" => $row["SECTIONCD"]);
}

$objForm->ae( array("type"        => "select",
                    "name"        => "SECTIONNAME",
                    "size"        => 1,
                    "extrahtml"   => "onChange=\"\"",
//                    "value"       => $Row["STAFFSEC_CD"],
					"options"     => $opt_sec
					));

$arg["data"]["SECTIONNAME"] = $objForm->ge("SECTIONNAME");

//授業受持有無////////////////////////////////////////////////////////////////////////////////////////////////
$opt_cla[0] = array("label" => "無","value" => "0");
$opt_cla[1] = array("label" => "有","value" => "1");

$objForm->ae( array("type"        => "select",
                    "name"        => "CHARGECLASS",
                    "size"        => 1,
                    "extrahtml"   => "onChange=\"\"",
//                    "value"       => $Row["DUTYSHARECD"],
					"options"     => $opt_cla
					));

$arg["data"]["CHARGECLASS"] = $objForm->ge("CHARGECLASS");

//必修クラブコード・課外クラブコードリスト///////////////////////////////////////////////////////////////////////
$db     = Query::dbCheckOut();
$query  = knjz290Query::getVClubMst_data($listyear);
$result = $db->query($query);
$opt_cl1 = array();
$opt_cl2 = array();

$opt_cl1[] = array("label" => "","value" => "");
$opt_cl2[] = array("label" => "","value" => "");

while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
{
	if(substr($row["CLUBCD"],0,2) == "50")
	{
		$opt_cl1[] = array("label" => htmlspecialchars($row["CLUBNAME"]),"value" => $row["CLUBCD"]);
	}
	else if(substr($row["CLUBCD"],0,2) == "51")
	{
		$opt_cl2[] = array("label" => htmlspecialchars($row["CLUBNAME"]),"value" => $row["CLUBCD"]);
	}
}

$objForm->ae( array("type"        => "select",
                    "name"        => "HCLUBNAME",
                    "size"        => 1,
                    "extrahtml"   => "onChange=\"\"",
//                    "value"       => $Row["CLUBNAME1"],
					"options"     => $opt_cl1
					));

$arg["data"]["HCLUBNAME"] = $objForm->ge("HCLUBNAME");

$objForm->ae( array("type"        => "select",
                    "name"        => "SCLUBNAME",
                    "size"        => 1,
                    "extrahtml"   => "onChange=\"\"",
//                    "value"       => $Row["CLUBNAME2"],
					"options"     => $opt_cl2
					));

$arg["data"]["SCLUBNAME"] = $objForm->ge("SCLUBNAME");


//課外クラブコードリスト//////////////////////////////////////////////////////////////////////////////////////////
//$db     = Query::dbCheckOut();
//$query  = knjz290Query::getVSectionMst_data();
//$result = $db->query($query);
//$opt_sec = array();
//while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
//      $opt_sec[] = array("label" => htmlspecialchars($row["SECTIONNAME"]),"value" => $row["SECTIONCD"]);
//}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//修正ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_udpate",
                    "value"       => "更 新",
                    "extrahtml"   => "onclick=\"return ToListUpd();\"" ) );

$arg["button"]["btn_update"] = $objForm->ge("btn_udpate");


//削除ボタンを作成する////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_del",
                    "value"       => "削 除",
                    "extrahtml"   => "onclick=\"return ToListDel();\"" ) );

$arg["button"]["btn_del"] = $objForm->ge("btn_del");

//終了ボタンを作成する
//$objForm->ae( array("type" => "button",
//                    "name"        => "btn_back",
//                    "value"       => "戻 る",
//                    "extrahtml"   => "onclick=\"closeWin();\"" ) );
//                    
//$arg["button"]["btn_back"] = $objForm->ge("btn_back");


//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "UPDATED",
                    "value"     => $Row["UPDATED"]
                    ) );
                   
$arg["finish"]  = $objForm->get_finish();

if (VARS::get("cmd") != "edit"){
    $arg["reload"]  = "parent.left_frame.location.reload();";
}

//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjz290Form2.html", $arg); 
?>
