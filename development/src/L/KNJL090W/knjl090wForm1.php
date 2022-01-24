<?php

require_once('for_php7.php');
class knjl090wForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090windex.php", "", "main");

        //権限チェック
        $adminFlg = knjl090wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090wQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090wQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
        }

        if ((!isset($model->warning))) {
            if ($model->cmd != 'change') {
                $model->judgement           = $Row["JUDGEMENT"];
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

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($Row["EXAMNO"]) && isset($model->warning) || $model->cmd == 'change') {
            $Row["COURSEMAJOR"]           =& $model->field["COURSEMAJOR"];
            $Row["REMARK1"]               =& $model->field["REMARK1"];
            $Row["REMARK2"]               =& $model->field["REMARK2"];
        }

        //内申科目 判定名称
        $result = $db->query(knjl090wQuery::getName($model->year, array("L003","L004","L013","L008")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L003") $applicantdivName[$row["NAMECD2"]] = $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L004") $testdivName[$row["NAMECD2"]] = $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L013") $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
            if ($row["NAMECD1"]=="L008") $arg["data"]["CONFIDENTIAL".$row["NAMECD2"]]    = $row["NAME1"];
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度
        $query = knjl090wQuery::get_name_cd($model->year, "L003");
        $extra = "onChange=\"change_flg(); return btn_submit('changeApp');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        if (isset($Row["EXAMNO"])) {
            if (!strlen($Row["INTERVIEW_ATTEND_FLG"])) {
                $arg["data"]["EXAMINEE"] = "【受検】";
            }
        }

        //受検番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);

        //変更無し
        if ($model->cmd != "change")  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT_NAME"];

        //合格
        } elseif (strlen($Row["COURSEMAJOR"])) {
            $model->judgement = "1";
            $arg["data"]["JUDGEMENT"] = $judgename["1"];

        //不合格
        } elseif (!strlen($Row["COURSEMAJOR"])) {
            $model->judgement = "2";
            $arg["data"]["JUDGEMENT"] = $judgename["2"];

        }

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1'){
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"red\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

/**************************************** CREATE 15 COMBOS ! ************************************************************/


        knjl090wForm1::FormatOpt($opt_cmcd);
        knjl090wForm1::FormatOpt($opt["L011"]);
        knjl090wForm1::FormatOpt($opt["L012"]);

        //合否学科コンボ
        $result = $db->query(knjl090wQuery::getSucCourse($model->year, $model->applicantdiv, $Row["EXAMNO"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //第１志望、第２志望
            if (strlen($row["VALUE"])) {
                $opt_cmcd[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
                $arg["data"]["EXAMCOURSE_NAME".$row["NUNBER"]] = $row["LABEL"];
            }
        }
        $arg["data"]["COURSEMAJOR"] = knjl090wForm1::CreateCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "300", $opt_cmcd, "onChange=\"btn_submit('change')\";");

        //判定で合格各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1' || $model->judgement == '2') {
            //手続区分に追加
            $opt["L011"] = knjl090wForm1::GetOpt($db, $model->year, array("L011"), "");
            //手続区分が「済み」なら入学区分に値を追加
            //if($model->procedurediv == "1") {
                //不合格の時(JUDGEMENT=2)は、辞退(ENTDIV=2)を選択できるようにする。
                $sitei = ($model->judgement == '2') ? "2" : "";
                $opt["L012"] = knjl090wForm1::GetOpt($db, $model->year, array("L012"), $sitei);
            //}
        }

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090wForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl090wForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"btn_submit('change')\";");


/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["TESTDIV_NAME"] = $testdivName[$Row["TESTDIV"]];
        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
//      $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";
        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? common::DateConv1(str_replace("-","/",$Row["BIRTHDAY"]), "0") : "";
        $arg["data"]["INTERVIEW_ATTEND_FLG"] = $Row["INTERVIEW_ATTEND_FLG"] ? $Row["INTERVIEW_ATTEND_FLG"]."：欠席" : "";
        $arg["data"]["TESTDIV2"] = $Row["TESTDIV2"] ? $Row["TESTDIV2"]."：追検査" : "";

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
        $arg["data"]["TOTAL9"] = $Row["TOTAL9"];
        $arg["data"]["ABSENCE_DAYS1"] = $Row["ABSENCE_DAYS1"];
        $arg["data"]["ABSENCE_DAYS2"] = $Row["ABSENCE_DAYS2"];
        $arg["data"]["ABSENCE_DAYS3"] = $Row["ABSENCE_DAYS3"];

        //別紙様式３の備考
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["YOUSIKI3_REMARK"] = knjCreateTextBox($objForm, $Row["YOUSIKI3_REMARK"], "YOUSIKI3_REMARK", 20, 30, $extra);

        //別紙様式５の転居の年月日
        $extra = "onChange=\"change_flg()\"";
        $arg["data"]["YOUSIKI5_TENKYOBI"] = View::popUpCalendar2($objForm, "YOUSIKI5_TENKYOBI", str_replace("-", "/", $Row["YOUSIKI5_TENKYOBI"]), "", "", $extra);

        //別紙様式５の転居地
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["YOUSIKI5_TENKYOTI"] = knjCreateTextBox($objForm, $Row["YOUSIKI5_TENKYOTI"], "YOUSIKI5_TENKYOTI", 50, 90, $extra);

        //備考１
        $objForm->ae( array("type"      => "text",
                            "name"      => "REMARK1",
                            "size"      => 40,
                            "maxlength" => 60,
                            "extrahtml" => "onChange=\"change_flg()\"",
                            "value"     => $Row["REMARK1"] ));
        $arg["data"]["REMARK1"] = $objForm->ge("REMARK1");

        //備考２
        $objForm->ae( array("type"      => "text",
                            "name"      => "REMARK2",
                            "size"      => 40,
                            "maxlength" => 60,
                            "extrahtml" => "onChange=\"change_flg()\"",
                            "value"     => $Row["REMARK2"] ));
        $arg["data"]["REMARK2"] = $objForm->ge("REMARK2");

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
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090W/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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
        View::toHTML($model, "knjl090wForm1.html", $arg);
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

    function GetOpt(&$db, $year, $namecd, $sitei, $flg=1)
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl090wQuery::getName($year, $namecd, $sitei));
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
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>