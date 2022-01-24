<?php

require_once('for_php7.php');

class knjl290yForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl290yindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl290yQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl290yQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
        }
        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_testdiv2') {
                $model->judgement           = $Row["JUDGEMENT"];
                $model->procedurediv        = $Row["PROCEDUREDIV"];
                $model->proceduredate       = $Row["PROCEDUREDATE"];
                $model->pay_money           = $Row["PAY_MONEY"];
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
        if ($model->cmd == 'change_testdiv2' || $model->cmd == 'change' || $model->cmd == 'update' || (isset($model->warning))) {
        } else {
            $model->testdiv2 = $db->getOne(knjl290yQuery::getMaxtestdiv($model, $Row["APPLICANTDIV"]));    //最大testdiv取得
        }
        $Row2 = $db->getRow(knjl290yQuery::getRecept($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);

        if (!isset($model->warning) && ($model->cmd != 'change')) {
            $model->judgediv = $Row2["JUDGEDIV"];
        }

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["PROCEDUREDATE"]       =& $model->field["PROCEDUREDATE"];
            $Row["PAY_MONEY"]           =& $model->field["PAY_MONEY"];
        }

        //志願者得点データ
        $result = $db->query(knjl290yQuery::getScore($model, $Row["APPLICANTDIV"]));
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscd = $Row4["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
        }
        $result->free();

        //受験科目 判定名称 出願区分名称
        $testdivnameArray = array();
        $result = $db->query(knjl290yQuery::getName($model->year, array("L013","L009","L004")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD1"]=="L009") $arg["data"]["TESTSUBCLASSCD".$row["NAMECD2"]]  = $row["NAME1"];
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = $row["NAMECD2"]."：".htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L013") $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
            if ($row["NAMECD1"]=="L004") $testdivnameArray[$row["NAMECD2"]] = $row["NAMECD2"]."：".$row["NAME1"];
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        if (isset($model->examno)) {
            if (strlen($Row2["RECEPTNO"]) && $Row2["JUDGEDIV"] != "4") {
                $arg["data"]["EXAMINEE"] = "【受験】";
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

        //判定
        $judge = $db->getRow(knjl290yQuery::getReceptJudge($model), DB_FETCHMODE_ASSOC); 

        //変更無し
        if ($model->cmd != "change")  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT_NAME"];
        //合格(受付データに1件（画面上も含む）でも合格がある場合)
        } elseif ($judgeNameSpare1[$model->judgediv] == "1" || (int)$judge["PASS"] > 0) {
            $model->judgement         = $model->judgediv;
            $arg["data"]["JUDGEMENT"] = $judgename[$model->judgediv];
        //すべて未設定
        } elseif ($model->judgediv == "" && ((int)$judge["UNKNOWN"] == (int)$judge["CNT"])) {
            $model->judgement         = "";
            $arg["data"]["JUDGEMENT"] = "";
        //不合格(受付データに1件も合格がない場合)
        } elseif ($judgeNameSpare1[$model->judgediv] != "1" && (int)$judge["PASS"] == 0 ) {
            $model->judgement         = $model->judgediv;
            $arg["data"]["JUDGEMENT"] = $judgename[$model->judgediv];
        }

        //氏名(判定で合格ならを赤、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1') {
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"red\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $Row["APPLICANTDIV"]? $Row["APPLICANTDIV"]."：".$Row["APPLICANTDIVNAME"] : "";

        //出願区分
        $testdivname = "";
        $seq = "";
        foreach ($testdivnameArray as $div => $name) {
            if (strlen($Row["TESTDIVNAME".$div])) {
                $testdivname .= $seq . $Row["TESTDIVNAME".$div];
                $seq = "、";
            }
        }
        $arg["data"]["TESTDIV"] = $testdivname;


        knjl290yForm1::FormatOpt($opt["L011"]);
        knjl290yForm1::FormatOpt($opt["L012"]);
        $disabled_date = "disabled";//デフォルト：入金日を編集不可

        //判定で合格各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1') {

            //手続区分に追加
            $opt["L011"] = knjl290yForm1::GetOpt($db, $model->year, array("L011"));

            //合格なら入金日を編集可能
            $disabled_date = "";
            $value_date = str_replace("-","/",$model->proceduredate);
            $value_pay_money = strlen($model->pay_money) ? number_format($model->pay_money) : "";

            //手続区分が「済み」なら入学区分に値を追加
            if ($model->procedurediv == "1") {
                $opt["L012"] = knjl290yForm1::GetOpt($db, $model->year, array("L012"));
            }
        }

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl290yForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //入金日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $value_date, "", "", $disabled_date);

        //入金額
        $objForm->ae( array("type"        => "text",
                            "name"        => "PAY_MONEY",
                            "size"        => 9,
                            "maxlength"   => 9,
                            "extrahtml"   => "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value); this.value=addFigure(this.value)\", onChange=\"change_flg()\" " .$disabled_date,
                            "value"       => $value_pay_money ));
        $arg["data"]["PAY_MONEY"] = $objForm->ge("PAY_MONEY");

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl290yForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"btn_submit('change')\";");



        //入試区分コンボ
        $opt = knjl290yForm1::GetOpt($db, $model->year, array("L004"), 0) ;
        $arg["data"]["TESTDIV2"] = knjl290yForm1::CreateCombo($objForm, "TESTDIV2", $model->testdiv2, "150", $opt, "onChange=\"change_flg(), btn_submit('change_testdiv2')\";");

        //合否区分コンボ(受付データがなければ合否区分コンボに空セット)
        $opt = (!isset($Row2["EXAMNO"]) ? array() : knjl290yForm1::GetOpt($db, $model->year, array("L013"), 1));
        $arg["data"]["JUDGEDIV"] = knjl290yForm1::CreateCombo($objForm, "JUDGEDIV", $model->judgediv, "150", $opt, "onChange=\"btn_submit('change')\";");

/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
//        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";
        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? str_replace("-","/",$Row["BIRTHDAY"]) : "";

        //グループ
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];
        //グループ
        $arg["data"]["INTERVIEW_VALUE2_NAME"]   = $Row2["INTERVIEW_VALUE2_NAME"];

        for ($i = 1; $i <= 4; $i++) {
            $arg["data"]["TOTAL".$i]      = $Row2["TOTAL".$i];
            $arg["data"]["TOTAL_RANK".$i] = $Row2["TOTAL_RANK".$i];
            $arg["data"]["DIV_RANK".$i]   = $Row2["DIV_RANK".$i];
            $arg["data"]["AVARAGE".$i]    = $Row2["AVARAGE".$i];
        }

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
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL290Y/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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
        View::toHTML($model, "knjl290yForm1.html", $arg);
    }

    function CreateCombo(&$objForm, $name, $value, $width, $opt, $extra) {
        $objForm->ae( array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra." style=\"width:".$width."\"",
                            "value"       => $value,
                            "options"     => $opt ) );
        return $objForm->ge($name);
    }

    function FormatOpt(&$opt, $flg=1) {
        $opt = array();
        if ($flg == "1") $opt[] = array("label" => "", "value" => "");
    }

    function GetOpt(&$db, $year, $namecd, $flg=1) {
        $opt = array();
        if ($flg == "1") $opt[] = array("label" => "", "value" => "");
        if (is_array($namecd)) {
            $result = $db->query(knjl290yQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }
}
?>