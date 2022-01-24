<?php

require_once('for_php7.php');

/********************************************************************/
/* 4科目成績一覧表                                  山城 2004/12/15 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に年度,予備2を追加               山城 2005/01/12 */
/* ･NO002：受験者指定にチェックボックスを追加       仲本 2005/01/13 */
/* ･NO003：合格通知に合格者全員を追加               山城 2005/12/29 */
/*         入学手続延期願いに合格者全員を追加                       */
/*         不合格通知に受験者全員を追加                             */
/********************************************************************/

class knjl327Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl327Form1", "POST", "knjl327index.php", "", "knjl327Form1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl327Query::get_apct_div("L003",$model->ObjYear));	/* NO001 */

	while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"], 
							   "value" => $Rowtyp["NAMECD2"]);
	}

	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["APDIV"])) {
		$model->field["APDIV"] = $opt_apdv_typ[0]["value"];
	}
	$objForm->ae( array("type"       => "select",
    	                "name"       => "APDIV",
        	            "size"       => "1",
            	        "value"      => $model->field["APDIV"],
						"extrahtml"  => " onChange=\"return btn_submit('knjl327');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");
	//入試区分コンボの設定
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 1 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl327Query::get_test_div($model->ObjYear));	/* NO001 */
	$opt_test_dv[]= array("label" => "", 
						  "value" => "");

	while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_test_dv[]= array("label" => $Rowtdv["NAME1"], 
							  "value" => $Rowtdv["NAMECD2"]);
		//NO001 ↓
		if ($Rowtdv["NAMESPARE2"] != 1 && !$defoult_flg){
			$defoult++;
		} else {
			$defoult_flg = true;
		}
		//NO001 ↑
	}

	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl327');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");
	//通知日付
	$value = isset($model->field["NOTICEDAY"])?$model->field["NOTICEDAY"]:str_replace("-","/",$model->control["学籍処理日"]);
	$arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

	//帳票種類ラジオボタン作成
	$disp_onoff = "";
	$opt_opt[] = array();
	for ($i = 1;$i <= 6;$i++) {
		$opt_opt[] = $i;
	}
	if (!isset($model->field["OUTPUT"])){
		$model->field["OUTPUT"] = 1;
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUT",
						"value"      => $model->field["OUTPUT"],
						"extrahtml"  => " onclick=\"return btn_submit('knjl327');\"",
						"multiple"   => $opt_opt));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);	//合格通知
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);	//特待生合格通知書
	$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);	//特待生合格証明書
	$arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);	//不合格通知書
	$arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT",5);	//入学金振込み用紙
	$arg["data"]["OUTPUT6"] = $objForm->ge("OUTPUT",6);	//入学手続延期願い
	$arg["data"]["OUTPUT7"] = $objForm->ge("OUTPUT",7);	//アップ合格通知書
	$arg["data"]["OUTPUT8"] = $objForm->ge("OUTPUT",8);	//スライド合格通知書

	//帳票出力Aラジオボタン作成
	$opt_opta[] = array();
	$opt_opta[] = 1;
	$opt_opta[] = 2;
	$opt_opta[] = 3;	//NO003
	if (($model->field["OUTPUT"] == 1)||($model->field["OUTPUT"] == 5)){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTA"])){
		$model->field["OUTPUTA"] = 3;	//NO003
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTA",
						"value"      => $model->field["OUTPUTA"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327');\"",
						"multiple"   => $opt_opta));

	$arg["data"]["OUTPUTA1"] = $objForm->ge("OUTPUTA",1);
	$arg["data"]["OUTPUTA2"] = $objForm->ge("OUTPUTA",2);
	$arg["data"]["OUTPUTA3"] = $objForm->ge("OUTPUTA",3);	//NO003

	//disabled設定
	if (($model->field["OUTPUTA"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

	//合格者のみチェックボックスA /* NO002 */
	$objForm->ae( array("type"  => "checkbox",
    	                "name"  => "CHKBOXA",
						"value"	=> "1",
						"extrahtml"	=> "checked ".$disp_onoff) );
	$arg["data"]["CHKBOXA"] = $objForm->ge("CHKBOXA");

	//受験番号A
	if (!isset($model->field["EXAMNOA"])){
		$model->field["EXAMNOA"] = "";
	}
	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOA",
    	                "size"       => 5,
    	                "maxlength"  => 5,
						"value"      => $model->field["EXAMNOA"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOA"] = $objForm->ge("EXAMNOA");

	//帳票出力Bラジオボタン作成
	$opt_optb[] = array();
	$opt_optb[] = 1;
	$opt_optb[] = 2;
	$opt_optb[] = 3;	//NO003
	if ($model->field["OUTPUT"] == 6){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTB"])){
		$model->field["OUTPUTB"] = 3;	//NO003
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTB",
						"value"      => $model->field["OUTPUTB"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327');\"",
						"multiple"   => $opt_optb));

	$arg["data"]["OUTPUTB1"] = $objForm->ge("OUTPUTB",1);
	$arg["data"]["OUTPUTB2"] = $objForm->ge("OUTPUTB",2);
	$arg["data"]["OUTPUTB3"] = $objForm->ge("OUTPUTB",3);	//NO003

	if (!isset($model->field["DEADLINE"])){
		$model->field["DEADLINE"] = str_replace("-","/",$model->control["学籍処理日"]);
	}
	//disabled設定
	if ($disp_onoff == ""){
		$disp_onoff = "";
		//提出期限
		$value = $model->field["DEADLINE"];
		$arg["el"]["DEADLINE"] = View::popUpCalendar($objForm, "DEADLINE", $value);
	} else {
		$disp_onoff = "disabled";
	}

	//期限時
	if (!isset($model->field["TIMEUPH"])){
		$model->field["TIMEUPH"] = "12";
	}
	$objForm->ae( array("type"       => "text",
    	                "name"       => "TIMEUPH",
    	                "size"       => 2,
    	                "maxlength"  => 2,
						"value"      => $model->field["TIMEUPH"],
						"extrahtml"	 => "$disp_onoff onblur=\"time_check(this)\""));

	$arg["data"]["TIMEUPH"] = $objForm->ge("TIMEUPH");

	//期限分
	if (!isset($model->field["TIMEUPM"])){
		$model->field["TIMEUPM"] = "00";
	}
	$objForm->ae( array("type"       => "text",
    	                "name"       => "TIMEUPM",
    	                "size"       => 2,
    	                "maxlength"  => 2,
						"value"      => $model->field["TIMEUPM"],
						"extrahtml"	 => "$disp_onoff onblur=\"time_check(this)\""));

	$arg["data"]["TIMEUPM"] = $objForm->ge("TIMEUPM");
	//disabled設定
	if ($model->field["OUTPUT"] == 6){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	//disabled設定
	if (($model->field["OUTPUTB"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

	//合格者のみチェックボックスB /* NO002 */
	$objForm->ae( array("type"  => "checkbox",
    	                "name"  => "CHKBOXB",
						"value"	=> "1",
						"extrahtml"	=> "checked ".$disp_onoff) );
	$arg["data"]["CHKBOXB"] = $objForm->ge("CHKBOXB");

	//受験番号B
	if (!isset($model->field["EXAMNOB"])){
		$model->field["EXAMNOB"] = "";
	}
	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOB",
    	                "size"       => 5,
    	                "maxlength"  => 5,
						"value"      => $model->field["EXAMNOB"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOB"] = $objForm->ge("EXAMNOB");

	//帳票出力Cラジオボタン作成
	$opt_optc[] = array();
	$opt_optc[] = 1;
	$opt_optc[] = 2;
	if (($model->field["OUTPUT"] == 2)||($model->field["OUTPUT"] == 3)){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTC"])){
		$model->field["OUTPUTC"] = 1;
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTC",
						"value"      => $model->field["OUTPUTC"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327');\"",
						"multiple"   => $opt_optc));

	$arg["data"]["OUTPUTC1"] = $objForm->ge("OUTPUTC",1);
	$arg["data"]["OUTPUTC2"] = $objForm->ge("OUTPUTC",2);

	//disabled設定
	if (($model->field["OUTPUTC"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	//受験番号C
	if (!isset($model->field["EXAMNOC"])){
		$model->field["EXAMNOC"] = "";
	}
	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOC",
    	                "size"       => 5,
    	                "maxlength"  => 5,
						"value"      => $model->field["EXAMNOC"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOC"] = $objForm->ge("EXAMNOC");

	//帳票出力Dラジオボタン作成
	$opt_opta[] = array();
	$opt_opta[] = 1;
	$opt_opta[] = 2;
	$opt_opta[] = 3;	//NO003
	if ($model->field["OUTPUT"] == 4){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTD"])){
		$model->field["OUTPUTD"] = 3;	//NO003
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTD",
						"value"      => $model->field["OUTPUTD"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327');\"",
						"multiple"   => $opt_opta));

	$arg["data"]["OUTPUTD1"] = $objForm->ge("OUTPUTD",1);
	$arg["data"]["OUTPUTD2"] = $objForm->ge("OUTPUTD",2);
	$arg["data"]["OUTPUTD3"] = $objForm->ge("OUTPUTD",3);	//NO003

	//disabled設定
	if (($model->field["OUTPUTD"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

	//受験番号D
	if (!isset($model->field["EXAMNOD"])){
		$model->field["EXAMNOD"] = "";
	}
	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOD",
    	                "size"       => 5,
    	                "maxlength"  => 5,
						"value"      => $model->field["EXAMNOD"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOD"] = $objForm->ge("EXAMNOD");

	//帳票出力Eラジオボタン作成
	$opt_opte[] = array();
	$opt_opte[] = 1;
	$opt_opte[] = 2;
	if ($model->field["OUTPUT"] == 7 || $model->field["OUTPUT"] == 8){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTE"])){
		$model->field["OUTPUTE"] = 1;
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTE",
						"value"      => $model->field["OUTPUTE"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327');\"",
						"multiple"   => $opt_opte));

	$arg["data"]["OUTPUTE1"] = $objForm->ge("OUTPUTE",1);
	$arg["data"]["OUTPUTE2"] = $objForm->ge("OUTPUTE",2);

	//disabled設定
	if (($model->field["OUTPUTE"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

	//受験番号E
	if (!isset($model->field["EXAMNOE"])){
		$model->field["EXAMNOE"] = "";
	}
	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOE",
    	                "size"       => 5,
    	                "maxlength"  => 5,
						"value"      => $model->field["EXAMNOE"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOE"] = $objForm->ge("EXAMNOE");

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
    	                "name"      => "YEAR",
        	            "value"     => $model->ObjYear
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DBNAME",
        	            "value"     => DB_DATABASE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJL327"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );
//以下のhiddenは、印刷処理時の判定用
	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUT",
        	            "value"     => $model->field["OUTPUT"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTA",
        	            "value"     => $model->field["OUTPUTA"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTB",
        	            "value"     => $model->field["OUTPUTB"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTC",
        	            "value"     => $model->field["OUTPUTC"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTD",
        	            "value"     => $model->field["OUTPUTD"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTE",
        	            "value"     => $model->field["OUTPUTE"]
            	        ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl327Form1.html", $arg); 
	}
}
?>
