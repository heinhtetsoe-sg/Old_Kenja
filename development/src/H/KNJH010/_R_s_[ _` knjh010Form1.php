<?php

require_once('for_php7.php');

class knjh010Form1
{
	function main(&$model)
	{
		$objForm = new form;
		$arg["start"] = $objForm->get_start("edit", "POST", "knjh010index.php", "", "edit");

		if(isset($model->schregno) && !isset($model->warning) && VARS::post("cmd")!="update")
		{
			$row = knjh010Query::getSchreg_Envir_Dat($model);
			if($row["COMMUTE_HOURS"]){
				$row["COMMUTE_HOURS"] = sprintf("%d",$row["COMMUTE_HOURS"]);
			}
			if($row["COMMUTE_MINUTES"]){
				$row["COMMUTE_MINUTES"] = sprintf("%d",$row["COMMUTE_MINUTES"]);
			}
			if($row["BEDTIME_HOURS"]){
				$row["BEDTIME_HOURS"] = sprintf("%d",$row["BEDTIME_HOURS"]);
			}
			if($row["BEDTIME_MINUTES"]){
				$row["BEDTIME_MINUTES"] = sprintf("%d",$row["BEDTIME_MINUTES"]);
			}
			if($row["RISINGTIME_HOURS"]){
				$row["RISINGTIME_HOURS"] = sprintf("%d",$row["RISINGTIME_HOURS"]);
			}
			if($row["RISINGTIME_MINUTES"]){
				$row["RISINGTIME_MINUTES"] = sprintf("%d",$row["RISINGTIME_MINUTES"]);
			}
		}else{
			$row =& $model->field;
		}
		if($model->schregno){
			$arg["name"] = $model->schregno."&nbsp;&nbsp;：&nbsp;&nbsp;".$model->name_show;
		}else{
			$arg["name"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:";
		}

		$db = Query::dbCheckOut();

		//性質 長所
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "MERITS",
							"value"	=> $row["MERITS"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["MERITS"] = $objForm->ge("MERITS");
		//性質 短所
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "DEMERITS",
							"value"	=> $row["DEMERITS"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["DEMERITS"] = $objForm->ge("DEMERITS");
		//学業 得意科目
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "GOOD_SUBJECT",
							"value"	=> $row["GOOD_SUBJECT"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["GOOD_SUBJECT"] = $objForm->ge("GOOD_SUBJECT");
		//学業 不得意科目
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "BAD_SUBJECT",
							"value"	=> $row["BAD_SUBJECT"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["BAD_SUBJECT"] = $objForm->ge("BAD_SUBJECT");
		//趣味・娯楽
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "HOBBY",
							"value"	=> $row["HOBBY"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["HOBBY"] = $objForm->ge("HOBBY");
		//入学以前通っていた塾
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "OLD_CRAM",
							"value"	=> $row["OLD_CRAM"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["OLD_CRAM"] = $objForm->ge("OLD_CRAM");
		//現在通っている塾(コンボ)
		$opt1 = array();
		$opt1[] = array("label" => "","value" => "0");
		$result = $db->query(knjh010Query::getV_name_mst("H101",$model));
		while($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt1[] =array("label"	=> $row1["NAMECD2"]."：".$row1["NAME1"],
							 "value"=> $row1["NAMECD2"]);
		}
		$result->free();
		$objForm->ae( array("type"	 => "select",
							"name"	 => "CUR_CRAMCD",
							"value"	 => $row["CUR_CRAMCD"],
							"options"=> $opt1));
		$arg["data"]["CUR_CRAMCD"] = $objForm->ge("CUR_CRAMCD");
		//現在通っている塾(塾名)
		$objForm->ae( array("type"	=> "text",
							"name"	=> "CUR_CRAM",
							"value"	=> $row["CUR_CRAM"],
							"size"	=> "20",
							"maxlength" => "10"));
		$arg["data"]["CUR_CRAM"] = $objForm->ge("CUR_CRAM");
		//けいこごと(コンボ)
		$opt2 = array();
		$opt2[] = array("label" => "","value" => "0");
		$result = $db->query(knjh010Query::getV_name_mst("H102",$model));
		while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt2[] =array("label" 	 => $row2["NAMECD2"]."：".$row2["NAME1"],
							 "value" => $row2["NAMECD2"]);
		}
		$result->free();
		$objForm->ae( array("type"	 => "select",
							"name"	 => "LESSONCD",
							"value"	 => $row["LESSONCD"],
							"options"=> $opt2));
		$arg["data"]["LESSONCD"] = $objForm->ge("LESSONCD");
		//けいこごと
		$objForm->ae( array("type"		=> "text",
							"name"		=> "LESSON",
							"value"		=> $row["LESSON"],
							"size"		=> "20",
							"maxlength" => "10"));
		$arg["data"]["LESSON"] = $objForm->ge("LESSON");
		//賞罰・検定・その他
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "PRIZES",
							"value"	=> $row["PRIZES"],
							"rows"	=> "4",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["PRIZES"] = $objForm->ge("PRIZES");
		//兄弟姉妹調査(コンボ)
		$opt4 = array();
		$opt4[] = array("label" => "","value" => "0");
		$result = $db->query(knjh010Query::getV_name_mst("H107",$model));
		while($row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt4[] =array("label"	 => $row4["NAMECD2"]."：".$row4["NAME1"],
							 "value" => $row4["NAMECD2"]);
		}
		$result->free();
		$objForm->ae( array("type"	 => "select",
							"name"	 => "BRO_SISCD",
							"value"	 => $row["BRO_SISCD"],
							"options"=> $opt4));
		$arg["data"]["BRO_SISCD"] = $objForm->ge("BRO_SISCD");
		//読書傾向
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "READING",
							"value"	=> $row["READING"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["READING"] = $objForm->ge("READING");
		//スポーツ
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "SPORTS",
							"value"	=> $row["SPORTS"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["SPORTS"] = $objForm->ge("SPORTS");
		//交友
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "FRIENDSHIP",
							"value"	=> $row["FRIENDSHIP"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["FRIENDSHIP"] = $objForm->ge("FRIENDSHIP");
		//卒業後の進路：進学
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "PLANUNIV",
							"value"	=> $row["PLANUNIV"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["PLANUNIV"] = $objForm->ge("PLANUNIV");
		//卒業後の進路：就職
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "PLANJOB",
							"value"	=> $row["PLANJOB"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["PLANJOB"] = $objForm->ge("PLANJOB");
		//特別教育活動
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "ED_ACT",
							"value"	=> $row["ED_ACT"],
							"rows"	=> "2",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["ED_ACT"] = $objForm->ge("ED_ACT");

		//通学：所要時間
		$objForm->ae( array("type"		=> "text",
							"name"		=> "COMMUTE_HOURS",
							"value"		=> $row["COMMUTE_HOURS"],
							"size"		=> "2",
							"maxlength" => "2",
							"extrahtml" => "onBlur=\"return to_Integer(this);\""));
		$arg["data"]["COMMUTE_HOURS"] = $objForm->ge("COMMUTE_HOURS");
		//通学：所要分
		$objForm->ae( array("type"		=> "text",
							"name"		=> "COMMUTE_MINUTES",
							"value"		=> $row["COMMUTE_MINUTES"],
							"size"		=> "2",
							"maxlength" => "2",
							"extrahtml" => "onBlur=\"return to_Integer(this);\""));
		$arg["data"]["COMMUTE_MINUTES"] = $objForm->ge("COMMUTE_MINUTES");
		//通学：最寄駅名
		$objForm->ae( array("type"		=> "text",
							"name"		=> "STATIONNAME",
							"value"		=> $row["STATIONNAME"],
							"size"		=> "20",
							"maxlength" => "20",
							"extrahtml" => ""));
		$arg["data"]["STATIONNAME"] = $objForm->ge("STATIONNAME");
		//通学 最寄駅路線名
		$objForm->ae( array("type"		=> "text",
							"name"		=> "OTHERHOWTOCOMMUTE",
							"value"		=> $row["OTHERHOWTOCOMMUTE"],
							"size"		=> "20",
							"maxlength" => "20"));
		$arg["data"]["OTHERHOWTOCOMMUTE"] = $objForm->ge("OTHERHOWTOCOMMUTE");
		//通学最寄駅までの手段
		$opt5 = array();
		$opt5[] = array("label" => "","value" => "0");
		$result = $db->query(knjh010Query::getV_name_mst("H100",$model));
		while($row5 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt5[] =array("label"	 => $row5["NAMECD2"]."：".$row5["NAME1"],
							 "value" => $row5["NAMECD2"]);
		}
		$result->free();
		$objForm->ae( array("type"	 => "select",
							"name"	 => "HOWTOCOMMUTECD",
							"value"	 => $row["HOWTOCOMMUTECD"],
							"options"=> $opt5));
		$arg["data"]["HOWTOCOMMUTECD"] = $objForm->ge("HOWTOCOMMUTECD");
		//途中経由駅
		$opt_changetrain= array();
		$opt_changetrain[] = array("label" => "","value" => "");
		$result = $db->query(knjh010Query::getstation_mst($model));
		while($row6 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt_changetrain[] = array ("label" => $row6["STATIONCD"]."：".$row6["STATIONNAME"],
										"value" => $row6["STATIONCD"]);
		}
		$result->free();
		for ($i = 1;$i < 9;$i++){
			$objForm->ae( array("type"	 => "select",
								"name"	 => "CHANGETRAIN".$i,
								"value"	 => $row["CHANGETRAIN".$i],
								"options"=> $opt_changetrain));
		}
		$arg["data"]["CHANGETRAIN1"] = $objForm->ge("CHANGETRAIN1");
		$arg["data"]["CHANGETRAIN2"] = $objForm->ge("CHANGETRAIN2");
		$arg["data"]["CHANGETRAIN3"] = $objForm->ge("CHANGETRAIN3");
		$arg["data"]["CHANGETRAIN4"] = $objForm->ge("CHANGETRAIN4");
		$arg["data"]["CHANGETRAIN5"] = $objForm->ge("CHANGETRAIN5");
		$arg["data"]["CHANGETRAIN6"] = $objForm->ge("CHANGETRAIN6");
		$arg["data"]["CHANGETRAIN7"] = $objForm->ge("CHANGETRAIN7");
		$arg["data"]["CHANGETRAIN8"] = $objForm->ge("CHANGETRAIN8");

		//学習時間(コンボ)
		$opt6 = array();
		$opt6[] = array("label" => "","value" => "0");
		$result = $db->query(knjh010Query::getV_name_mst("H103",$model));
		while($row6 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt6[] =array("label"	 => $row6["NAMECD2"]."：".$row6["NAME1"],
							 "value" => $row6["NAMECD2"]);
		}
		$result->free();
		$objForm->ae( array("type"	 => "select",
							"name"	 => "STUDYTIME",
							"value"	 => $row["STUDYTIME"],
							"options"=> $opt6));
		$arg["data"]["STUDYTIME"] = $objForm->ge("STUDYTIME");
		//備考
		$objForm->ae( array("type"	=> "textarea",
							"name"	=> "REMARK",
							"value"	=> $row["REMARK"],
							"rows"	=> "4",
							"cols"	=> "20",
							"wrap"	=> "hard"));
		$arg["data"]["REMARK"] = $objForm->ge("REMARK");
		//住居調査(コンボ)
		$opt9 = array();
		$opt9[] = array("label" => "","value" => "0");
		$result = $db->query(knjh010Query::getV_name_mst("H108",$model));
		while($row9 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt9[] =array("label"	 => $row9["NAMECD2"]."：".$row9["NAME1"],
							 "value" => $row9["NAMECD2"]);
		}
		$result->free();
		$objForm->ae( array("type"	 => "select",
							"name"	 => "RESIDENTCD",
							"value"	 => $row["RESIDENTCD"],
							"options"=> $opt9));
		$arg["data"]["RESIDENTCD"] = $objForm->ge("RESIDENTCD");

		Query::dbCheckIn($db);

		/* ボタン作成 */

		//その他ボタン
		//リンク設定
        $subdata = "loadwindow('knjm390index.php?cmd=subform1',500,200,350,250)";
        $link = REQUESTROOT."/H/KNJH010/knjh010index.php?cmd=subform1&SCHREGNOSUB=".$model->schregno."&PRG=".VARS::get("PRG");
		$objForm->ae( array("type"		=> "button",
							"name"		=> "OTHER",
							"value"		=> "その他",
							"extrahtml"   => "onclick=\"Page_jumper('$link');\"" ) );
//							"extrahtml" => "onclick=\"return btn_submit('subform1');\"" ) );

		$arg["button"]["OTHER"] = $objForm->ge("OTHER");

		//更新ボタン
		$objForm->ae( array("type"		=> "button",
							"name"		=> "btn_udpate",
							"value"		=> "更 新",
							"extrahtml" => "onclick=\"return btn_submit('update');\"" ) );
		$arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

		//更新後前の生徒へボタン
		$arg["button"]["btn_up_next"]	= View::updateNext($model, $objForm, 'btn_udpate');

		//削除ボタン
		$objForm->ae( array("type"	=> "button",
							"name"	=> "btn_del",
							"value"	 => "削 除",
							"extrahtml" => "onclick=\"return btn_submit('delete');\"" ) );
		$arg["button"]["btn_del"] = $objForm->ge("btn_del");
		//取消ボタン
		$objForm->ae( array("type"	=> "button",
							"name"	=> "btn_reset",
							"value"	 => "取 消",
							"extrahtml" => "onclick=\"return btn_submit('reset');\"") );
		$arg["button"]["btn_reset"] = $objForm->ge("btn_reset");
		//終了ボタン
		$objForm->ae( array("type"	=> "button",
							"name"	=> "btn_end",
							"value"	 => "終 了",
							"extrahtml" => "onclick=\"closeWin();\"" ) );
		$arg["button"]["btn_end"] = $objForm->ge("btn_end");

		//hidden
		$objForm->ae( array("type"	=> "hidden",
							"name"	=> "cmd") );

		$objForm->ae( array("type"	=> "hidden",
							"name"	=> "SCHREGNO",
							"value"	 => $model->schregno) );

		if(get_count($model->warning)== 0 && $model->cmd !="reset"){
			$arg["next"] = "NextStudent(0);";
		}elseif($model->cmd =="reset"){
			$arg["next"] = "NextStudent(1);";
		}

		$arg["finish"] = $objForm->get_finish();
		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す
		View::toHTML($model, "knjh010Form1.html", $arg);
	}
}
?>
