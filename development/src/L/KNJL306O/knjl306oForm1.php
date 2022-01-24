<?php

require_once('for_php7.php');

/********************************************************************/
/* 入試データ照会                                   山城 2005/12/28 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：特別措置出力条件を修正                   山城 2006/01/10 */
/********************************************************************/

class knjl306oForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl306oindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl306oQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);

        if (!is_array($Row) && ($model->cmd == "back2" || $model->cmd == "next2")) {
            $model->setWarning("MSG303","移動先のデータが存在しません。");
            $model->cmd = "main";
            $Row = $db->getRow(knjl306oQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
        }

        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_testdiv2') {
                $model->desirediv           = $Row["DESIREDIV"];
                $model->judgement           = $Row["JUDGEMENT"];
                $model->special_measures    = $Row["SPECIAL_MEASURES"];
                $model->procedurediv        = $Row["PROCEDUREDIV"];
                $model->entdiv              = $Row["ENTDIV"];
            }
        }

        //データが無ければ更新ボタン等を無効
        if (!is_array($Row) && $model->cmd == 'reference') {
            $model->setWarning("MSG303");
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //入試区分の値が変わればそれをキーにして受付データ取得
        if($model->cmd == 'change_testdiv2' || $model->cmd == 'change' || $model->cmd == 'update' || (isset($model->warning))) {
            $Row2 = $db->getRow(knjl306oQuery::getRecept($model), DB_FETCHMODE_ASSOC);
        }else{
            $model->testdiv2 = $db->getOne(knjl306oQuery::getMaxtestdiv($model));    //最大testdiv取得
            $Row2 = $db->getRow(knjl306oQuery::getRecept($model), DB_FETCHMODE_ASSOC);
        }

        if (!isset($model->warning) && ($model->cmd != 'change')) {
            $model->judgediv = $Row2["JUDGEDIV"];
        }

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["TESTDIV"]               =& $model->field["TESTDIV"];
            $Row["SUCCESS_NOTICENO"]      =& $model->field["SUCCESS_NOTICENO"];
            $Row["FAILURE_NOTICENO"]      =& $model->field["FAILURE_NOTICENO"];
            $Row["INTERVIEW_ATTEND_FLG"]  =& $model->field["INTERVIEW_ATTEND_FLG"];
            $Row["REMARK1"]               =& $model->field["REMARK1"];
            $Row["REMARK2"]               =& $model->field["REMARK2"];
            if ($model->cmd != 'change_testdiv2') {
                $Row2["HONORDIV"]         =& $model->field["HONORDIV2"];
                $Row2["ADJOURNMENTDIV"]   =& $model->field["ADJOURNMENTDIV"];
            }
            $Row["SPECIAL_MEASURES3"]     =& $model->field["SPECIAL_MEASURES3"];
        }

        //志願者データ取得
        $Row3 = $db->getRow(knjl306oQuery::getDesire($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある場合はフォームの値を参照する
        if ((isset($model->warning)) || $model->cmd == 'change') {
            if ((!isset($Row3["APPLICANT_DIV"]))) {
                $Row3["APPLICANT_DIV"]  =& $model->field["APPLICANT_DIV"];
            }
            if ((!isset($Row3["EXAMINEE_DIV"]))) {
                $Row3["EXAMINEE_DIV"]   =& $model->field["EXAMINEE_DIV"];
            }
        }

        //志願者得点データ
        $result = $db->query(knjl306oQuery::getScore($model));
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd = $Row4["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
            $arg["data"]["STDSC".$subclasscd]  = $Row4["STD_SCORE"];
            $arg["data"]["RANK".$subclasscd]   = $Row4["RANK"];
        }
        $result->free();

        //受験科目 内申科目 判定名称
        $special_measures3_name = array();
        $result = $db->query(knjl306oQuery::getName($model->year, array("L013","L008","L009","L010")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L008") $arg["data"]["CONFIDENTIAL".$row["NAMECD2"]]    = $row["NAME1"];
            if ($row["NAMECD1"]=="L009") $arg["data"]["TESTSUBCLASSCD".$row["NAMECD2"]]  = $row["NAME1"];
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L010") $special_measures3_name[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        if (isset($model->examno)) {
            //何れかの入試区分で受験区分が「有り」であればラベルを表示
            $DESIRE_FLG["EXAMINEE_DIV"] = $db->getOne(knjl306oQuery::get_desire_flg($model));
            if(isset($DESIRE_FLG["EXAMINEE_DIV"])) {
                $arg["data"]["EXAMINEE"] = "【受験】";
            }

            $RECEPT_FLG["HONORDIV"] = $db->getOne(knjl306oQuery::get_recept_flg($model, "HONORDIV"));
            //何れかの入試区分で特待区分が「対象」であればラベルを表示
            if(isset($RECEPT_FLG["HONORDIV"])) {
                $arg["data"]["HONOR"] = "【特待】";
            }
            $RECEPT_FLG["ADJOURNMENTDIV"] = $db->getOne(knjl306oQuery::get_recept_flg($model, "ADJOURNMENTDIV"));
            //何れかの入試区分で延期区分が「延期あり」であればラベルを表示
            if(isset($RECEPT_FLG["ADJOURNMENTDIV"])) {
                $arg["data"]["ADJOURNMENT"] = "【延期】";
            }
        }

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //合格通知NO.
		if (!$Row["SUCCESS_NOTICENO"]){
			$Row["SUCCESS_NOTICENO"] = "　　";
		}
        $arg["data"]["SUCCESS_NOTICENO"] = $Row["SUCCESS_NOTICENO"];

        //不合格通知NO.
		if (!$Row["FAILURE_NOTICENO"]){
			$Row["FAILURE_NOTICENO"] = "　　";
		}
        $arg["data"]["FAILURE_NOTICENO"] = $Row["FAILURE_NOTICENO"];

        //判定
        $judge = $db->getRow(knjl306oQuery::getReceptJudge($model), DB_FETCHMODE_ASSOC); 

        if ($model->judgediv == "1") $model->special_measures = "";

        //変更無し
        if ($model->cmd != "change")  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT_NAME"];
            $model->special_measures  = $Row["SPECIAL_MEASURES"];

        //特別措置
        } elseif  ($model->special_measures != "") {
            $model->judgement = "1";
            $arg["data"]["JUDGEMENT"] = $judgename["1"];

        //合格(受付データに1件（画面上も含む）でも合格がある場合)
        } elseif ($model->judgediv == "1" || (int)$judge["PASS"] > 0) {
            $model->judgement = "1";
            $arg["data"]["JUDGEMENT"] = $judgename["1"];
            $model->special_measures  = "";

        //すべて未設定
        } elseif ($model->judgediv == "" && ((int)$judge["UNKNOWN"] == (int)$judge["CNT"])) {
            $model->judgement         = "";
            $arg["data"]["JUDGEMENT"] = "";

        //不合格(受付データに1件も合格がない場合)
        } elseif ($model->judgediv != "1" && (int)$judge["PASS"] == 0 ) {
            $model->judgement = "2";
            $arg["data"]["JUDGEMENT"] = $judgename["2"];
        }

        //3:特別アップ合格
        $arg["data"]["SPECIAL_MEASURES3_NAME"] = $special_measures3_name[$Row["SPECIAL_MEASURES3"]];
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SPECIAL_MEASURES3",
                            "value"     => $Row["SPECIAL_MEASURES3"]) );

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($model->judgement == '1' && $model->special_measures == '' && $Row["SPECIAL_MEASURES3"] ==''){
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"red\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } elseif ($model->special_measures !='' || $Row["SPECIAL_MEASURES3"] !='') {
            $arg["data"]["NAME"]      = "<font color=\"blue\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"blue\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
            $arg["data"]["SPECIAL_MEASURES3_NAME"] = "<font color=\"blue\">".htmlspecialchars($arg["data"]["SPECIAL_MEASURES3_NAME"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度コンボ
		$applicantdiv = $db->getOne(knjl306oQuery::getName2($model->year,"L003",$Row["APPLICANTDIV"]));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv;

        //入試区分
        $testdiv = $seq = "";
        $result = $db->query(knjl306oQuery::getTestdivMst($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (strlen($Row["TESTDIV" .$row["NAMECD2"]])) {
                $testdiv .= $seq .$row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]);
                $seq = " ／ ";
            }
        }
        $arg["data"]["TESTDIV"] = $testdiv;

        //出願コースコンボ
        $desirediv = $db->getOne(knjl306oQuery::get_desirediv($model->year,$Row["DESIREDIV"]));
        $arg["data"]["DESIREDIV"] = $desirediv;

        knjl306oForm1::FormatOpt($opt_cmcd);
        knjl306oForm1::FormatOpt($opt["L010"]);
        knjl306oForm1::FormatOpt($opt["L011"]);
        knjl306oForm1::FormatOpt($opt["L012"]);
        knjl306oForm1::FormatOpt($opt_honor1);

        //面接出欠に値を追加
		if ($Row["INTERVIEW_ATTEND_FLG"] == "1"){
	        $opt_inflg = "1:出欠";
		}else if ($Row["INTERVIEW_ATTEND_FLG"] == "2"){
	        $opt_inflg = "2:欠席";
		}else {
	        $opt_inflg = "";
		}
        //面接出欠コンボ
        $arg["data"]["INTERVIEW_ATTEND_FLG"] = $opt_inflg;

        //特別措置区分(合格が一件もない場合）
		$special_measures = $db->getOne(knjl306oQuery::getName2($model->year,"L010",$Row["SPECIAL_MEASURES"]));
        $arg["data"]["SPECIAL_MEASURES"] = $special_measures;

        //入学クラス
        $entclass = $db->getOne(knjl306oQuery::getName2($model->year,"L017",$Row["ENTCLASS"]));
        $arg["data"]["ENTCLASS"] = $entclass;

        //手続区分
        $procedurediv = $db->getOne(knjl306oQuery::getName2($model->year,"L011",$Row["PROCEDUREDIV"]));
        $arg["data"]["PROCEDUREDIV"] = $procedurediv;

        //志望学科
        $coursemajor = $db->getOne(knjl306oQuery::get_coursemajor($model,$Row["DESIREDIV"],$Row["SUC_COURSECD"].$Row["SUC_MAJORCD"].$Row["SUC_COURSECODE"]));
        $Row["COURSEMAJOR"] = $coursemajor;
        $arg["data"]["COURSEMAJOR"] = $Row["COURSEMAJOR"];

        //入学区分
        $entdiv = $db->getOne(knjl306oQuery::getName2($model->year,"L012",$Row["ENTDIV"]));
        $arg["data"]["ENTDIV"] = $entdiv;

        //特待入学
        if($Row["HONORDIV"] == "1") {
            $honordiv1 = "1:特待入学";
        }
        $arg["data"]["HONORDIV1"] = $honordiv1;

//--------------------------------------------------

        //入試区分コンボ
        $opt = knjl306oForm1::getTestdivMstOpt($db, $model->year, 0) ;
        $arg["data"]["TESTDIV2"] = knjl306oForm1::CreateCombo($objForm, "TESTDIV2", $model->testdiv2, "155", $opt, "onChange=\"change_flg(), btn_submit('change_testdiv2')\";");

        //合否区分
        $judgediv = (!isset($Row2["EXAMNO"]) ? "" : $db->getOne(knjl306oQuery::getName2($model->year,"L013",$model->judgediv)));
        $arg["data"]["JUDGEDIV"] = $judgediv;

        //特待区分2
        $honordiv2 = "";
        $adjournmentdiv = "";
		if ($Row2["HONORDIV"] == "1"){
            $honordiv2 = "1:対象";
		}
        $arg["data"]["HONORDIV2"] = $honordiv2;

        //延期区分
		if ($Row2["ADJOURNMENTDIV"] == "1"){
            $adjournmentdiv = "1:延期あり";
		}
        $arg["data"]["ADJOURNMENTDIV"] = $adjournmentdiv;

        //志願区分コンボ
		$applicant_div = "";
		if ($Row3["APPLICANT_DIV"] == "1"){
	        $applicant_div = "1:有り";
		}else if($Row3["APPLICANT_DIV"] == "2"){
	        $applicant_div = "2:無し";
		}
        $arg["data"]["APPLICANT_DIV"] = $applicant_div;

        //受験区分コンボ
		$examinee_div = "";
		if ($Row3["EXAMINEE_DIV"] == "1"){
	        $examinee_div = "1:出席";
		}else if($Row3["EXAMINEE_DIV"] == "2"){
	        $examinee_div = "2:欠席";
		}
        $arg["data"]["EXAMINEE_DIV"] = $examinee_div;

        //合格クラス
        $judgeclass = $db->getOne(knjl306oQuery::getName2($model->year,"L016",$Row2["JUDGECLASS"]));
        $arg["data"]["JUDGECLASS"] = $judgeclass;

/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
        $arg["data"]["FS_GRDYEAR"] = $Row["FS_GRDYEAR"]? $Row["FS_GRDYEAR"]."年" : "";
        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];
        //受付№（座席番号）
        $arg["data"]["RECEPTNO"]        = $Row2["RECEPTNO"];
        //受験型
        $arg["data"]["EXAM_TYPE"] = $Row2["EXAM_TYPE"]? $Row2["EXAM_TYPE"]."：".$Row2["NAME1"] : "";

        $arg["data"]["TOTAL2"]      = $Row2["TOTAL2"];
        $arg["data"]["TOTAL_RANK2"] = $Row2["TOTAL_RANK2"];
        $arg["data"]["DIV_RANK2"]   = $Row2["DIV_RANK2"];
        $arg["data"]["TOTAL4"]      = $Row2["TOTAL4"];
        $arg["data"]["TOTAL_RANK4"] = $Row2["TOTAL_RANK4"];
        $arg["data"]["DIV_RANK4"]   = $Row2["DIV_RANK4"];
        $arg["data"]["AVARAGE2"]    = $Row2["AVARAGE2"];
        $arg["data"]["AVARAGE4"]    = $Row2["AVARAGE4"];
        $arg["data"]["KATEN"]       = $Row2["KATEN"];

        //志願者内申データ
        $arg["data"]["RPT01"] = $Row["CONFIDENTIAL_RPT01"];
        $arg["data"]["RPT02"] = $Row["CONFIDENTIAL_RPT02"];
        $arg["data"]["RPT03"] = $Row["CONFIDENTIAL_RPT03"];
        $arg["data"]["RPT04"] = $Row["CONFIDENTIAL_RPT04"];
        $arg["data"]["RPT05"] = $Row["CONFIDENTIAL_RPT05"];
        $arg["data"]["RPT06"] = $Row["CONFIDENTIAL_RPT06"];
        $arg["data"]["RPT07"] = $Row["CONFIDENTIAL_RPT07"];
        $arg["data"]["RPT08"] = $Row["CONFIDENTIAL_RPT08"];
        $arg["data"]["RPT09"] = $Row["CONFIDENTIAL_RPT09"];
        $arg["data"]["RPT10"] = $Row["CONFIDENTIAL_RPT10"];
        $arg["data"]["RPT11"] = $Row["CONFIDENTIAL_RPT11"];
        $arg["data"]["RPT12"] = $Row["CONFIDENTIAL_RPT12"];
        $arg["data"]["AVERAGE5"]      = $Row["AVERAGE5"];
        $arg["data"]["AVERAGE_ALL"]   = $Row["AVERAGE_ALL"];
        $arg["data"]["ABSENCE_DAYS"] = $Row["ABSENCE_DAYS"];

        //備考１
        $arg["data"]["REMARK1"] = $Row["REMARK1"];

        //備考２
        $arg["data"]["REMARK2"] = $Row["REMARK2"];

/***************************************************** CREATE BUTTONS ********************************************************/

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
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL306O/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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
                            "name"      =>  "btn_udpate",
                            "value"     =>  "更 新",
                            "extrahtml" =>  "onclick=\"btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //更新ボタン(更新後前の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" =>  "style=\"width:150px\" onclick=\"btn_submit('back2');\"" ) );
        //更新ボタン(更新後次の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" =>  "style=\"width:150px\" onclick=\"btn_submit('next2');\"" ) );
        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");

        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"btn_submit('reset');\""  ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

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
        View::toHTML($model, "knjl306oForm1.html", $arg);
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
            $result = $db->query(knjl306oQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }

    function getTestdivMstOpt(&$db, $year, $flg=1)
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

            $result = $db->query(knjl306oQuery::getTestdivMst($year));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();

        return $opt;
    }
}
?>