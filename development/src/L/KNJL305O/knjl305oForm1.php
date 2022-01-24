<?php

require_once('for_php7.php');

/********************************************************************/
/* 受付番号変更                                     山城 2004/12/28 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：社会又は、理科の得点が1件でもある場合                    */
/*         エラーを表示して、画面を閉じる。         山城 2006/01/07 */
/********************************************************************/

class knjl305oForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl305oindex.php", "", "main");
        $db = Query::dbCheckOut();

		$model->testdiv = $db->getOne(knjl305oQuery::GetMaxDesirediv($model->year));

        //次年度の学期マスタが存在するか NO001
		$checkScore = $db->getOne(knjl305oQuery::selectScore($model));
        if ($checkScore > 0){
            $arg["Closing"] = "  closing_window(); " ;
        }

        $Row = $db->getRow(knjl305oQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);

        $model->examno = $Row["EXAMNO"];

        //データが無ければ更新ボタン等を無効
        if (!is_array($Row) && $model->cmd == 'reference') {
            $model->setWarning("MSG303");
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

		/*----------------------------*/
		/*       確認用データ         */
		/*----------------------------*/
        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //氏名
        $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);

        //かな氏名
        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);

        //入試制度コンボ
        $arg["data"]["APPLICANTDIV"] = $Row["APPLICANTDIVNM"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "APPLICANTDIV",
                            "value"     => $Row["APPLICANTDIV"]) );

        //入試区分コンボ
        $arg["data"]["TESTDIV"] = $Row["TESTDIVNM"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTDIV",
                            "value"     => $model->testdiv) );

        $arg["data"]["SEX"] = $Row["SEX"]? $Row["SEXNAME"] : "";

        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? $Row["BIRTHDAY"] : "";

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row["EXAMHALL_NAME"];
        //受付№（座席番号）
        $arg["data"]["RECEPTNO"]        = $Row["RECEPTNO"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "RECEPTNO",
                            "value"     => $Row["RECEPTNO"]) );

        //受験型
        $arg["data"]["EXAM_TYPE"] = $Row["EXAM_TYPE"]? $Row["EXAM_TYPENM"] : "";

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "EXAM_TYPE",
                            "value"     => $Row["EXAM_TYPE"]) );


		/*----------------------------*/
		/*       変更後データ         */
		/*----------------------------*/
		//変更後受験型
		if ($Row["EXAM_TYPE"] == "1"){
			$model->examtype = "2";
		}else {
			$model->examtype = "1";
		}

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHANGE_EXAM_TYPE",
                            "value"     => $model->examtype) );

        //会場変更
		$opt_hall   = array();
		$opt_hallno = array();
		$maxhall = 0;
		$result = $db->query(knjl305oQuery::getHall($model->testdiv, $model->examtype));
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

			$opt_hall[] = array("label"  =>  htmlspecialchars($row["EXAMHALL_NAME"]),
								"value"  =>  $row["EXAMHALLCD"]);

			$opt_hallno[$row["EXAMHALLCD"]] = array("sno"  =>  $row["S_RECEPTNO"],
													"eno"  =>  $row["E_RECEPTNO"]);

			$maxhall++;
		}
		$result->free();
		if (!$model->hallcd || ($model->hallcd > $opt_hall[$maxhall-1]["value"])){
			$model->hallcd = $opt_hall[$maxhall-1]["value"];
		}
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHANGE_HALL",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\" btn_submit('');\"",
                            "value"       => $model->hallcd,
                            "options"     => $opt_hall ) );

        $arg["data"]["CHANGE_HALL"] = $objForm->ge("CHANGE_HALL");
        $arg["data"]["CHANGE_HALLSNO"] = $opt_hallno[$model->hallcd]["sno"];
        $arg["data"]["CHANGE_HALLENO"] = $opt_hallno[$model->hallcd]["eno"];

        $Row2 = $db->getRow(knjl305oQuery::get_change_data($model->year,$model->testdiv,$model->examtype,$model->hallcd,$Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);
        //変更後受付番号
		if ($opt_hallno[$model->hallcd]["noe"] < sprintf('%04d',$Row2["CHANGERECEPTNO"])){
	        $arg["data"]["CHANGE_RECEPTNO"] = sprintf('%04d',$Row2["CHANGERECEPTNO"]);
		}else if ($opt_hallno[$model->hallcd]["sno"] <= sprintf('%04d',$Row2["CHANGERECEPTNO"]) &&
			$opt_hallno[$model->hallcd]["eno"] >= sprintf('%04d',$Row2["CHANGERECEPTNO"])){
	        $arg["data"]["CHANGE_RECEPTNO"] = sprintf('%04d',$Row2["CHANGERECEPTNO"]);
		}else {
	        $arg["data"]["CHANGE_RECEPTNO"] = $arg["data"]["CHANGE_HALLSNO"];
		}

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHANGE_RECEPTNO",
                            "value"     => $arg["data"]["CHANGE_RECEPTNO"]) );

        //変更後受験型
		$examtype_name = $db->getOne(knjl305oQuery::GetName2($model->year,"L005",$model->examtype));
        $arg["data"]["CHANGE_EXAM_TYPE_NAME"] = $examtype_name;

		if ($Row2["CHANGERECEPTNO"] > $Row2["E_RECEPTNO"]){
			$model->setWarning("会場の座席数をオーバーしています。");
		}

		/*----------------------------*/
		/*       その他データ         */
		/*----------------------------*/
        //検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"btn_submit('reference');\"" ) );
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");

        global $sess;
        //かな検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_kana_reference",
                            "value"     => "かな検索",
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL305O/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.x, event.y, 320, 260)\"") );
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");

        //前の志願者検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => " << ",
                            "extrahtml" => "onClick=\"btn_submit('back1');\"" ) );
        
        //次の志願者検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_next",
                            "value"     => " >> ",
                            "extrahtml" => "onClick=\"btn_submit('next1');\"" ) );
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");

        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_update",
                            "value"     =>  "更 新",
                            "extrahtml" =>  "onclick=\"btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"OnClosing();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "auth_check",
                            "value"     => (AUTHORITY == DEF_UPDATABLE && is_array($Row)) ? "2" : (AUTHORITY == DEF_UPDATABLE && !is_array($Row) ? "1" : "0") ) );
        //入試年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cflg",
                            "value"     => $model->cflg) );

        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl305oForm1.html", $arg);
    }

    function CreateCombo(&$objForm, $name, $value, $width, $opt, $extra)
    {
        $objForm->ae( array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra." style=\"width:".$width."\"",
                            "value"       => $value,
                            "options"     => $opt ) );
        return $objForm->ge($name);
    }
    
    function FormatOpt(&$opt, $flg=1){
        
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");
    }

    function GetOpt(&$db, $year, $namecd, $flg=1)
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl305oQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }
}
?>