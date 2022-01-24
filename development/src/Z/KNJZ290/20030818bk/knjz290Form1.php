<?php

require_once('for_php7.php');

require_once('knjz290Query.inc');
//ブランクをつなげて、指定の長さまで文字列の長さを長くする関数///////////////////////////////////////////////////////
function deflengs($str,$len)
{
	$a = 0;
	$x = 0;
	$a = strlen($str);
	if($a<$len)
	{
		$x = $len - $a;
		$x = $x / 2;
		for($i=1; $i<=$x; $i++)
		{
			$str .="　";
		}
		return $str;
	}
}
//権限チェック///////////////////////////////////////////////////////////////////////////////////////////////////////
if (common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE){
	$arg["jscript"] = "OnAuthError();";
}

$objForm = new form;

//フォーム作成///////////////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("list", "POST", "knjz290index.php", "", "edit");
$db             = Query::dbCheckOut();
$no_year		= 0;
$default_year	= 0;
$$listyear	= 0;

//追加、更新、削除用配列を初期化する。//////////////////////////////////////////////////////////////////////////////
//$add = array();
//$upd = array();
//$del = array();


//年度コンボボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$db     = Query::dbCheckOut();
$result    = $db->query(knjz290Query::selectYearQuery());
$opt       = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
{
    $opt[] = array("label" => $row["YEAR"],
				   "value" => $row["YEAR"]);
///	if ($row["STAFFYEAR"] == $model->year)
///		$no_year = 1;
	if($row["YEAR"] == $model->control["年度"])
	{
		$default_year = 1;
	}
	$su = $su + 1;
}
///	//年度の追加
///if ($no_year == 0 && $model->year != "")
///	$opt[] = array("label" => $model->year, "value" => $model->year);

//rsort($opt);
//reset($opt);

//最初に画面を表示した場合の年度は？
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

//echo "opt[0]["value"]".$opt[0]["value"];
//echo "listyear=".$listyear;

$objForm->ae( array("type"        => "select",
                    "name"        => "year",
                    "size"        => "1",
                    "extrahtml"   => "onchange=\"return temp_clear();\"",
//                    "value"       => $model->year,
//                    "value"       => isset($model->year)?$model->year:$model->control["年度"],
                    "value"       => $listyear,
                    "options"     => $opt));

$arg["data"]["year"] = $objForm->ge("year");


//読込ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_read",
                    "value"       => "読 込",
                    "extrahtml"   => "onclick=\"return btn_submit('list');\"" ));

$arg["button"]["btn_read"] = $objForm->ge("btn_read");


//前年度からコピーボタンを作成する//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_copy",
                    "value"       => "前年度からコピー",
                    "extrahtml"   => "onclick=\"return btn_AddSubmit('copy');\"" ));

$arg["button"]["btn_copy"] = $objForm->ge("btn_copy");

//年度追加テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////
//$objForm->ae( array("type"		  => "text",
//					"name"		  => "year_add",
//					"size"		  => 5,
//					"maxlength"   => 4,
//					"extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
//					"value" 	  => $year_add )); 

//$arg["data"]["year_add"] = $objForm->ge("year_add");


//年度追加ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////
//$objForm->ae( array("type"		  => "button",
//					"name"		  => "btn_year_add",
//					"value" 	  => "年度追加",
//					"extrahtml"   => "onclick=\"return add('');\"" ));

//$arg["button"]["btn_year_add"] = $objForm->ge("btn_year_add");


//年度職名一覧取得///////////////////////////////////////////////////////////////////////////////////////////////////
	//職名を配列へ
$db     = Query::dbCheckOut();
$queryjob  = knjz290Query::getVJobMst_data($listyear);
$resultjob = $db->query($queryjob);
$job = array();
while($rowjob = $resultjob->fetchRow(DB_FETCHMODE_ASSOC)){
      $job[$rowjob["JOBCD"]] = $rowjob["JOBNAME"];
}
	//所属を配列へ
$db     = Query::dbCheckOut();
$querysec  = knjz290Query::getVSectionMst_data($listyear);
$resultsec = $db->query($querysec);
$sec = array();
while($rowsec = $resultsec->fetchRow(DB_FETCHMODE_ASSOC)){
      $sec[$rowsec["SECTIONCD"]] = $rowsec["SECTIONNAME"];
}
	//校務分掌部を配列へ
$db     = Query::dbCheckOut();
$querysha  = knjz290Query::getVDutyshareMst_data($listyear);
$resultsha = $db->query($querysha);
$sha = array();
while($rowsha = $resultsha->fetchRow(DB_FETCHMODE_ASSOC)){
      $sha[$rowsha["DUTYSHARECD"]] = $rowsha["SHARENAME"];
}
	//クラブ名・クラブコードを配列へ
$db     = Query::dbCheckOut();
$queryclb  = knjz290Query::getVClubMst_data($listyear);
$resultclb = $db->query($queryclb);
$clb = array();
while($rowclb = $resultclb->fetchRow(DB_FETCHMODE_ASSOC)){
      $clb[$rowclb["CLUBCD"]] = $rowclb["CLUBNAME"];
}
//echo $clb['5001'];
//echo $clb['5002'];
//echo $clb['5101'];


	//リストに表示する職員名とその情報
$result = $db->query(knjz290Query::selectQuery($model));
	//echo $query;
	//echo $model->year;
$opt_over_id = $opt_over = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
{
	$staffname = str_pad($row["STAFFNAME"],20,"　",STR_PAD_RIGHT);
	$jobname = str_pad($job[$row["JOBNAMECD"]],12,"　",STR_PAD_RIGHT);
	$secname = str_pad($sec[$row["STAFFSEC_CD"]],16,"　",STR_PAD_RIGHT);
	$shaname = str_pad($sha[$row["DUTYSHARECD"]],16,"　",STR_PAD_RIGHT);
	$hclbname = str_pad($clb[$row["REQUIRECLUBCD"]],20,"　",STR_PAD_RIGHT);
	$sclbname = str_pad($clb[$row["EXTRACLUBCD"]],20,"　",STR_PAD_RIGHT);
	switch($row["CHARGECLASSCD"])
	{
		case "0":
			$claname="無";
			break;
		case "1":
			$claname="有";
			break;
	}
//	$opt_over[]    = array("label" => "　　　　"."|".$row["STAFFCD"]."|".$staffname."|".$jobname.
//									"|".$secname."|".$shaname."|".$claname."|".$hclbname."|".$sclbname,
//						   "value" => $row["STAFFCD"].",".",".$row["JOBNAMECD"].",".$row["STAFFSEC_CD"].
//									",".$row["DUTYSHARECD"].",".$row["CHARGECLASSCD"].",".$row["REQUIRECLUBCD"].",".
//									$row["EXTRACLUBCD"]
//							);
	$opt_over[]    = array("label" => "　　　　"."|".$row["STAFFCD"]."|".$staffname."|".$jobname.
									"|".$secname."|".$shaname."|".$claname,
						   "value" => $row["STAFFCD"].",".",".$row["JOBNAMECD"].",".$row["STAFFSEC_CD"].
									",".$row["DUTYSHARECD"].",".$row["CHARGECLASSCD"].",".$row["REQUIRECLUBCD"].",".
									$row["EXTRACLUBCD"]
							);
	$opt_over_id[] = $row["STAFFCD"];
}

//$result->free();
//Query::dbCheckIn($db);

$opt_under = array();

	//職名年度
$objForm->ae( array("type"        => "select",
                    "name"        => "staffyear",
                    "size"        => "15",
                    "value"       => "left",
                    "extrahtml"   => "onclick=\"return ToEdit();\" STYLE=\"WIDTH:100%;font-family:monospace;\"",
                    "options"     => isset($opt_over)?$opt_over:array())); 

$arg["data"]["staffyear"] = $objForm->ge("staffyear");


//職名マスタボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////
$link = REQUESTROOT."/Z/KNJZ290_2/knjz290_2index.php?mode=1";

$objForm->ae( array("type"        => "button",
                    "name"        => "btn_master",
                    "value"       => " 職員マスタ ",
                    "extrahtml"   => "onclick=\"parent.location.href='$link'\"") ); 

$arg["button"]["btn_master"] = $objForm->ge("btn_master");


//保存ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_keep",
                    "value"       => "保存",
                    "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

$arg["button"]["btn_keep"] = $objForm->ge("btn_keep");


//取消ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_clear",
                    "value"       => "取消",
                    "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

$arg["button"]["btn_clear"] = $objForm->ge("btn_clear");


//終了ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_end",
                    "value"       => "終了",
//                    "extrahtml"   => "onclick=\"return btn_submit('end');\"" ) ); 
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//選択リスト（年度データになくてマスタにあるデータ）取得/////////////////////////////////////////////////////////////
if (is_array($opt_over_id) && ($model->year))
{
    $result = $db->query(knjz290Query::selectstaffQuery($opt_over_id,$model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	{
		$staffmastername = str_pad($row["STAFFMASTERNAME"],20,"　",STR_PAD_RIGHT);
		$opt_under[] = array("label" => "　　　　"."|".$row["STAFFCD"]."|".$staffmastername."|"."　　　　　　".
										"|"."　　　　　　　　"."|"."　　　　　　　　"."|"."　",
							 "value" => $row["STAFFCD"].","."a".","."".","."".","."".","."");
    }
}

$result->free();
Query::dbCheckIn($db);

	//職名マスタ
$objForm->ae( array("type"        => "select",
                    "name"        => "staffmaster",
                    "size"        => "10",
                    "value"       => "left",
                    "extrahtml"   => "multiple STYLE=\"WIDTH:40%;font-family:monospace;\"",
                    "options"     => isset($opt_under)?$opt_under:array()));  

$arg["data"]["staffmaster"] = $objForm->ge("staffmaster");


//全追加ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "mst_add_all",
                    "value"       => "全追加",
                    "extrahtml"   => "onclick=\"return move('all');\"" ) );

$arg["button"]["mst_add_all"] = $objForm->ge("mst_add_all");


//追加ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "button",
                    "name"        => "mst_add",
                    "value"       => "追加",
                    "extrahtml"   => "onclick=\"return move('one');\"" ) );

$arg["button"]["mst_add"] = $objForm->ge("mst_add");


//hiddenを作成する///////////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//$objForm->ae( array("type"      => "hidden",
//                    "name"      => "selectdata"
//                    ) );

	//追加されたデータの情報
$objForm->ae( array("type"      => "hidden",
                    "name"      => "addcd"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "addsha"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "addjob"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "addsec"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "addcla"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "addhcl"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "addscl"
                    ) );

	//変更されたデータの情報
$objForm->ae( array("type"      => "hidden",
                    "name"      => "updcd"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "updsha"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "updjob"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "updsec"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "updcla"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "updhcl"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "updscl"
                    ) );

	//削除されたデータの情報
$objForm->ae( array("type"      => "hidden",
                    "name"      => "delcd"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "delsha"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "deljob"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "delsec"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "delcla"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "delhcl"
                    ) );
$objForm->ae( array("type"      => "hidden",
                    "name"      => "delscl"
                    ) );

	//前年度からコピーするデータの情報
$objForm->ae( array("type"		=> "hidden",
					"name"		=> "copy1"
					) );
$objForm->ae( array("type"		=> "hidden",
					"name"		=> "copy2"
					) );
$objForm->ae( array("type"		=> "hidden",
					"name"		=> "copy3"
					) );
$objForm->ae( array("type"		=> "hidden",
					"name"		=> "copy4"
					) );


$arg["info"]    = array("TOP"        => "対象年度",
                        "LEFT_LIST"  => "職員年度一覧",
                        "RIGHT_LIST" => "職員一覧");


$arg["TITLE"]   = "マスタメンテナンス - 職員マスタ";
$arg["finish"]  = $objForm->get_finish();

if (VARS::get("cmd") != "list"){
    $arg["reload"]  = "parent.right_frame.location.reload();";
}

//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。////////////////////////////////////////////////////////////
View::toHTML($model, "knjz290From1.html", $arg); 

?>
