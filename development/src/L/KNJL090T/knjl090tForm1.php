<?php

require_once('for_php7.php');

class knjl090tForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090tindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090tQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090tQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
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
            $Row["SUC_MAJORCD"]           =& $model->field["SUC_MAJORCD"];
            $Row["SUCCESS_NOTICENO"]      =& $model->field["SUCCESS_NOTICENO"];
            $Row["FAILURE_NOTICENO"]      =& $model->field["FAILURE_NOTICENO"];
            $Row["REMARK1"]               =& $model->field["REMARK1"];
            $Row["REMARK2"]               =& $model->field["REMARK2"];
        }

        //内申科目 判定名称
        $result = $db->query(knjl090tQuery::getName($model->year, array("L003","L013","L008")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L003") $applicantdivName[$row["NAMECD2"]] = $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L008") $arg["data"]["CONFIDENTIAL".$row["NAMECD2"]]    = $row["NAME1"];
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;


        if (isset($Row["EXAMNO"])) {
            if (!strlen($Row["INTERVIEW_ATTEND_FLG"])) {
                $arg["data"]["EXAMINEE"] = "【受検】";
            }
        }

        //受検番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => preg_replace('/^0*/', '', $model->examno) ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //合格通知NO.
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUCCESS_NOTICENO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["SUCCESS_NOTICENO"] ));
        $arg["data"]["SUCCESS_NOTICENO"] = $objForm->ge("SUCCESS_NOTICENO");

        //不合格通知NO.
        $objForm->ae( array("type"        => "text",
                            "name"        => "FAILURE_NOTICENO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["FAILURE_NOTICENO"] ));
        $arg["data"]["FAILURE_NOTICENO"] = $objForm->ge("FAILURE_NOTICENO");

        //変更無し
        if ($model->cmd != "change")  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT_NAME"];

        //合格
        } elseif (strlen($Row["SUC_MAJORCD"])) {
            $model->judgement = "1";
            $arg["data"]["JUDGEMENT"] = $judgename["1"];

        //不合格
        } elseif (!strlen($Row["SUC_MAJORCD"])) {
            $model->judgement = "2";
            $arg["data"]["JUDGEMENT"] = $judgename["2"];

        }

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($model->judgement == '1'){
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"red\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

/**************************************** CREATE 15 COMBOS ! ************************************************************/


        knjl090tForm1::FormatOpt($optMajor);
        knjl090tForm1::FormatOpt($opt["L011"]);
        knjl090tForm1::FormatOpt($opt["L012"]);

        //合否学科コンボ
        $result = $db->query(knjl090tQuery::getWishno($model->year, $Row["EXAMNO"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optMajor[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            //志望区分
            $arg["data"]["MAJORCD" .$row["WISHNO"]] = $row["LABEL"];
        }
        $arg["data"]["SUC_MAJORCD"] = knjl090tForm1::CreateCombo($objForm, "SUC_MAJORCD", $Row["SUC_MAJORCD"], "250", $optMajor, "onChange=\"btn_submit('change')\";");

        //判定で合格各コンボに値を追加
        if ($model->judgement == '1') {
            //手続区分に追加
            $opt["L011"] = knjl090tForm1::GetOpt($db, $model->year, array("L011"));
            //手続区分が「済み」なら入学区分に値を追加
            if($model->procedurediv == "1") {
                $opt["L012"] = knjl090tForm1::GetOpt($db, $model->year, array("L012"));
            }
        }

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090tForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl090tForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"btn_submit('change')\";");


/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["APPLICANTDIV"] = $applicantdivName[$Row["APPLICANTDIV"]];
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
        $arg["data"]["TOTAL3"] = $Row["TOTAL3"];
        $arg["data"]["TOTAL5"] = $Row["TOTAL5"];
        $arg["data"]["TOTAL9"] = $Row["TOTAL9"];
        $arg["data"]["AVERAGE_ALL"]  = $Row["AVERAGE_ALL"];
        $arg["data"]["ABSENCE_DAYS1"] = $Row["ABSENCE_DAYS1"];
        $arg["data"]["ABSENCE_DAYS2"] = $Row["ABSENCE_DAYS2"];
        $arg["data"]["ABSENCE_DAYS3"] = $Row["ABSENCE_DAYS3"];

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
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090T/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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
        View::toHTML($model, "knjl090tForm1.html", $arg);
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
            $result = $db->query(knjl090tQuery::getName($year, $namecd));
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