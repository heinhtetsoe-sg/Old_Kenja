<?php

require_once('for_php7.php');

class knjb0045Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb0045Form1", "POST", "knjb0045index.php", "", "knjb0045Form1");

        //起動チェック
        if ($model->cmd == "") $model->StartCheck();

        //時間割種別ラジオボタンを作成（1:基本時間割/2:通常時間割）
        $opt[0]=1;
        $opt[1]=2;

        for ($i = 1; $i <= 2; $i++) {
            $name = "RADIO".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "RADIO",
                                "value"      => isset($model->field["RADIO"])?$model->field["RADIO"]:"1",
                                "extrahtml"  => "onclick=\"jikanwari(this.value);\" id=\"$name\"",
                                "multiple"   => $opt));
            $arg["data"][$name] = $objForm->ge("RADIO",$i);
        }

		//年度・タイトル・日付の使用可・不可にするフラグ変数を設定
        if ($model->field["RADIO"] == 2) {	//通常
		    $arg["Dis_Date"]  = " jikanwari('2'); " ;
        } else {                            //基本
		    $arg["Dis_Date"]  = " jikanwari('1'); " ;
        }
        //年度コンボボックスを作成
        $db = Query::dbCheckOut();
		$opt_year = array();
		$nendo_gakki = "";
        $query = knjb0045Query::getYearQuery();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_year[] = array('label' => $row["YEAR"]."年度".$row["SEMESTERNAME"],
                           		'value' => $row["YEAR"]."-".$row["SEMESTER"]);
			$nendo_gakki = $row["YEAR"]."-".$row["SEMESTER"];
        }
        $result->free();
        Query::dbCheckIn($db);
		if ($model->field["NENDO"] == "") $model->field["NENDO"] = $nendo_gakki;//初期値：最新をセット。

        $objForm->ae( array("type"       => "select",
                            "name"       => "NENDO",
                            "size"       => "1",
                            "value"      => $model->field["NENDO"],
		                    "extrahtml"  => "onChange=\"return btn_submit('knjb0045');\"",//---05/01/21Modify
        					//"extrahtml"  => "$dis_radio1",
                            "options"    => $opt_year));
        $arg["data"]["NENDO"] = $objForm->ge("NENDO");
        //タイトルコンボボックスを作成
        $db = Query::dbCheckOut();
		$opt_title = array();
        $query = knjb0045Query::getTitleQuery($model->field["NENDO"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_title[] = array('label' => "SEQ".$row["BSCSEQ"].":".$row["TITLE"],
                           		 'value' => $row["BSCSEQ"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "TITLE",
                            "size"       => "1",
                            "value"      => $model->field["TITLE"],
        					//"extrahtml"  => "$dis_radio1",
                            "options"    => $opt_title));
        $arg["data"]["TITLE"] = $objForm->ge("TITLE");
        //名簿の日付（初期値：学籍処理日(CTRL_DATE)をセット）04/11/27Add
		if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);

		$arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);


        //指定日付（初期値：学籍処理日(CTRL_DATE)をセット）
		if ($model->field["DATE_FROM"] == "") 	$model->field["DATE_FROM"] 	= str_replace("-","/",CTRL_DATE);
		if ($model->field["DATE_TO"] == "") 	$model->field["DATE_TO"] 	= str_replace("-","/",CTRL_DATE);

		$arg["data"]["DATE_FROM"] 	= View::popUpCalendar($objForm	,"DATE_FROM"	,$model->field["DATE_FROM"]);
		$arg["data"]["DATE_TO"] 	= View::popUpCalendar($objForm	,"DATE_TO"		,$model->field["DATE_TO"]);

		//教師稼動数
        $objForm->ae( array("type"       => "text",
                            "name"       => "OPERATION",
                            "size"       => "3",
                            "maxlength"  => "3",
		                    "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"      => isset($model->field["OPERATION"]) ? $model->field["OPERATION"] : "4" ));
        $arg["data"]["OPERATION"] = $objForm->ge("OPERATION");

		//データ内容
        $db = Query::dbCheckOut();
        $datanaiyo = $status = "";
        $row = knjb0045Query::getCheckListHdat($db);
		if (is_array($row)) {
			if ($row["RADIO"]=="1") {
		        $row_title = knjb0045Query::getTitle($db,$row["YEAR"],$row["SEMESTER"],$row["BSCSEQ"]);
	    	    $datanaiyo  = "時間割種別：&nbsp;基本時間割";
	    	    $datanaiyo .= "<BR>&nbsp;&nbsp;&nbsp;指定日付&nbsp;&nbsp;：&nbsp;".$row["YEAR"]."年度".$row_title["SEMESTERNAME"]."　SEQ".$row["BSCSEQ"].":".$row_title["TITLE"];
			} else if ($row["RADIO"]=="2") {
	    	    $datanaiyo  = "時間割種別：&nbsp;通常時間割";
	    	    $datanaiyo .= "<BR>&nbsp;&nbsp;&nbsp;指定日付&nbsp;&nbsp;：&nbsp;".str_replace("-","/",$row["DATE_FROM"])."～".str_replace("-","/",$row["DATE_TO"]);
			}
    	    $datanaiyo .= "<BR>&nbsp;&nbsp;&nbsp;教師稼動数：&nbsp;".$row["OPERATION"]."&nbsp;回／週";
    	    $datanaiyo .= "<BR><BR>&nbsp;&nbsp;&nbsp;前回実行日：&nbsp;".str_replace("-","/",$row["DATE_CREATE"])." ".$row["DATE_TIME"];
    	    $datanaiyo .= "<BR>&nbsp;&nbsp;&nbsp;前回実行者：&nbsp;".$row["STAFFNAME"];
			if ($row["STATUS"]=="OK") {
		        $record_cnt = knjb0045Query::getRecordCnt($db);
	    	    $datanaiyo .= "<BR>&nbsp;&nbsp;&nbsp;エラー件数：&nbsp;".$record_cnt."&nbsp;件";
			}
			if ($row["STATUS"]=="RUNNING") 	$status = "（処理中）";
			if ($row["STATUS"]=="OK") 		$status = "（処理済）";
		}
        Query::dbCheckIn($db);

		//チェック実行後は、データ内容クリアする
	    if ($model->check == "on") {
	        $datanaiyo = $status = "";
		}

        $arg["data"]["CHECKDATA"] = $datanaiyo;
        $arg["data"]["CHECKSTATUS"] = $status;

        /*---ボタンを作成する---*/
        //チェック実行
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "チェック実行",
		                    "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ) );
        $arg["button"]["btn_ok"] = $objForm->ge("btn_ok");
        //プレビュー／印刷
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
		                    "extrahtml"   => "onclick=\"return btn_submit('print');\"" ) );
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");
        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin('" . SERVLET_URL . "');\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");
        
		//チェック実行
	    $arg["CheckSubmit"]  = ($model->check == "on") ? " chk_submit('" . SERVLET_URL . "'); " : "";
		//プレビュー／印刷
	    $arg["PrintSubmit"]  = ($model->print == "on") ? " newwin('" . SERVLET_URL . "'); " : "";

        //フラグを初期値にセット
		$model->check = $model->print = "off";
        //パラメータ
		$nendo = explode("-",$model->field["NENDO"]);
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => ($model->field["RADIO"] == 2) ? $model->year : $nendo[0]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"      => ($model->field["RADIO"] == 2) ? $model->semester : $nendo[1]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "BSCSEQ",
                            "value"      => ($model->field["RADIO"] == 2) ? "" : $model->field["TITLE"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFFCD",
                            "value"      => STAFFCD
                            ) );
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID"
//                            "value"     => "KNJB0045"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        //パラメータ（エラー件数を帳票に渡す）01/12/10Add
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ERR_CNT",
                            "value"     => $record_cnt
                            ) );
        
        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb0045Form1.html", $arg); 
    }
}
?>
