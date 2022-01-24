<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：登録更新後のデータ初期化処理修正         山城 2004/11/17 */
/* ･NO002：更新・削除時のエラーメッセージ修正       山城 2004/11/17 */
/* ･NO003：更新後、再更新できるよう修正             山城 2004/11/24 */
/* ･NO004：登録後、登録データが表示される様修正     山城 2004/11/24 */
/* ･NO005：委員会コード表示を変更                   山城 2004/11/26 */
/********************************************************************/

class knjj090Form2
{
    function main(&$model){


$objForm = new form;
//フォーム作成
$arg["start"]   = $objForm->get_start("edit", "POST", "knjj090index.php", "", "edit");


//警告メッセージを表示しない場合
if (isset($model->schregno) && $model->index!="" && !isset($model->warning))	/* NO004 */
{
	//対象レコードを取得
	$model->org_data = $Row = knjj090Query::getRow($model,$model->index);
	$temp_cd = $Row["SCHREGNO"];
	$seqcd   = $Row["SEQ"];/* NO003 */
}
else
{
	$Row =& $model->field;
}

//配列取得
$db     = Query::dbCheckOut();

$opt_commicd = $opt_rolecd = array();
$opt_commicd[0] = $opt_rolecd[0] = array("label" => "",       "value" => "");

//委員会
$query  = knjj090Query::getCommiMst($model, $model->control_data["年度"]);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_commicd[] = array("label" => htmlspecialchars($row["COMMITTEE_FLG"])."-".htmlspecialchars($row["COMMITTEECD"]) . "　" . htmlspecialchars($row["COMMITTEENAME"]), /* NO005 */
	                      "value" => $row["COMMITTEE_FLG"].$row["COMMITTEECD"]);
}
//役職
$query  = knjj090Query::getNameMst("J002",$model->control_data["年度"]);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_rolecd[] = array("label" => htmlspecialchars($row["NAMECD2"]) . "　" . htmlspecialchars($row["NAME1"]),
	                      "value" => $row["NAMECD2"]);
}

$result->free();
Query::dbCheckIn($db);


//委員会/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"		 => "select",
					"name"		 => "COMMITTEECD",
					"size"		 => "1",
					"value" 	 => $Row["COMMITTEE_FLG"].$Row["COMMITTEECD"],
					"options"	 => $opt_commicd));

$arg["data"]["COMMITTEECD"] = $objForm->ge("COMMITTEECD");

//係り名/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"		 => "text",
					"name"		 => "CHARGENAME",
					"size"		 => "34",
					"maxlength"  => "30",
					"value" 	 => $Row["CHARGENAME"]) );

$arg["data"]["CHARGENAME"] = $objForm->ge("CHARGENAME");


//役職/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"		 => "select",
					"name"		 => "EXECUTIVECD",
					"size"		 => "1",
					"value" 	 => $Row["EXECUTIVECD"],
					"options"	 => $opt_rolecd));

$arg["data"]["EXECUTIVECD"] = $objForm->ge("EXECUTIVECD");


//----------------------------------------------------------------------------------------------------------------

//追加ボタンを作成する
$objForm->ae( array("type" 		  => "button",
					"name"		  => "btn_add",
					"value" 	  => "登 録",
					"extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

$arg["button"]["btn_add"] = $objForm->ge("btn_add");

//修正ボタンを作成する
$objForm->ae( array("type" 		  => "button",
					"name"		  => "btn_update",
					"value" 	  => "更 新",
					"extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

$arg["button"]["btn_update"] = $objForm->ge("btn_update");

//削除ボタンを作成する
$objForm->ae( array("type" 		  => "button",
					"name"		  => "btn_del",
					"value" 	  => "削 除",
					"extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

$arg["button"]["btn_del"] = $objForm->ge("btn_del");

//クリアボタンを作成する
$objForm->ae( array("type" 		  => "reset",
					"name"		  => "btn_reset",
					"value" 	  => "取 消",
					"extrahtml"   => "onclick=\"return Btn_reset();\"" ) );

$arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

//終了ボタンを作成する
$objForm->ae( array("type"		  => "button",
					"name"		  => "btn_end",
					"value" 	  => "終 了",
					"extrahtml"   => "onclick=\"closeWin();\"" ) );
					
$arg["button"]["btn_end"] = $objForm->ge("btn_end");

//記録詳細入力ボタン
$extra = ($model->schregno) ? "onclick=\" wopen('".REQUESTROOT."/X/KNJXCOMMI_DETAIL/knjxcommi_detailindex.php?PROGRAMID=".PROGRAMID."&SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
$arg["button"]["btn_detail"] = KnjCreateBtn($objForm, "btn_detail", "記録備考入力", $extra);

//一括更新ボタン
$extra = "onClick=\" wopen('".REQUESTROOT."/J/KNJJ090_2/knjj090_2index.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
$arg["button"]["btn_batch"] = KnjCreateBtn($objForm, "btn_batch", "一括更新", $extra);

//hiddenを作成する
$objForm->ae( array("type"		=> "hidden",
					"name"		=> "cmd"
					) );

//hiddenを作成する
$objForm->ae( array("type"		=> "hidden",
					"name"		=> "UPDATED",
					"value" 	=> $Row["UPDATED"]
					) );

$objForm->ae( array("type"		=> "hidden",
					"name"		=> "SCHREGNO",
					"value" 	=> $model->schregno
					) );
if ($seqcd=="") $seqcd = $model->field["SEQPOST"];    /* NO003 */

$objForm->ae( array("type"		=> "hidden",
					"name"		=> "SEQPOST",
					"value" 	=> $seqcd    		  /* NO003 */
					) );

$objForm->ae( array("type"		=> "hidden",
					"name"		=> "PRGID",
					"value" 	=> "KNJJ090"
					) );

if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];
$objForm->ae( array("type"		=> "hidden",
					"name"		=> "temp_cd",
					"value" 	=> $temp_cd
					) );


$cd_change = false;
if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

$arg["finish"]  = $objForm->get_finish();
if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != 1)){
	$arg["reload"]  = "window.open('knjj090index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');"; /* NO001 */
}

//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjj090Form2.html", $arg); 
}
}
?>

