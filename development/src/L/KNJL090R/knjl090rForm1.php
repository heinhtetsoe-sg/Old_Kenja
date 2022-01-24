<?php

require_once('for_php7.php');

class knjl090rForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090rindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090rQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090rQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
            $model->applicantdiv = $Row["APPLICANTDIV"];
        }

        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_testdiv2') {
                $model->judgement           = $Row["JUDGEMENT"];
                $model->judge_kind          = $Row["JUDGE_KIND"];
                $model->procedurediv        = $Row["PROCEDUREDIV"];
                $model->proceduredate       = $Row["PROCEDUREDATE"];
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
            $model->testdiv2 = $db->getOne(knjl090rQuery::getMaxtestdiv($model, $Row["APPLICANTDIV"]));    //最大testdiv取得
        }
        $Row2 = $db->getRow(knjl090rQuery::getRecept($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);

        if (!isset($model->warning) && ($model->cmd != 'change')) {
            $model->judgediv = $Row2["JUDGEDIV"];
            $model->procedurediv1 = $Row2["PROCEDUREDIV1"];
            $model->proceduredate1 = $Row2["PROCEDUREDATE1"];
        }

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["PROCEDUREDATE"]       =& $model->field["PROCEDUREDATE"];
            if (strlen($model->examno) && (isset($model->warning))) {
                $Row["COURSEMAJOR"]      =& $model->field["COURSEMAJOR"];
            }
        }

        //志願者得点データ
        $result = $db->query(knjl090rQuery::getScore($model, $Row["APPLICANTDIV"]));
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd = $Row4["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
        }
        $result->free();

        //受験科目 判定名称
        $result = $db->query(knjl090rQuery::getName($model->year, array("L013","L009")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L009") $arg["data"]["TESTSUBCLASSCD".$row["NAMECD2"]]  = $row["NAME1"];
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L013") $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        if (isset($model->examno)) {
            if (strlen($Row2["RECEPTNO"]) && $Row2["JUDGEDIV"] != "3" && $Row2["JUDGEDIV"] != "4") {
                $arg["data"]["EXAMINEE"] = "【受験】";
            }
        }
        //入試制度コンボ
        $query = knjl090rQuery::get_name_cd($model->year, "L003", $model->fixApplicantDiv);
        $extra = "onChange=\"return btn_submit('changeApp');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //判定
        $judge = $db->getRow(knjl090rQuery::getReceptJudge($model), DB_FETCHMODE_ASSOC); 

        if ($judgeNameSpare1[$model->judgediv] == "1") $model->special_measures = "";

        //変更無し
        if ($model->cmd != "change" && !isset($model->warning))  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT_NAME"];
//echo "No1:".$model->judgement;

        //合格(受付データに1件（画面上も含む）でも合格がある場合)
        } elseif ($judgeNameSpare1[$model->judgediv] == "1" || (int)$judge["PASS"] > 0) {
            $model->judgement         = $model->judgediv;
            $arg["data"]["JUDGEMENT"] = $judgename[$model->judgediv];
//echo "No2:".$model->judgement;

        //すべて未設定
        } elseif ($model->judgediv == "" && ((int)$judge["UNKNOWN"] == (int)$judge["CNT"])) {
            $model->judgement         = "";
            $arg["data"]["JUDGEMENT"] = "";
//echo "No3:".$model->judgement;

        //不合格(受付データに1件も合格がない場合)
        } elseif ($judgeNameSpare1[$model->judgediv] != "1" && (int)$judge["PASS"] == 0 ) {
            $model->judgement         = $model->judgediv;
            $arg["data"]["JUDGEMENT"] = $judgename[$model->judgediv];
//echo "No4:".$model->judgement;

        }

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1'){
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"red\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $Row["APPLICANTDIV"]? $Row["APPLICANTDIV"]."：".$Row["APPLICANTDIVNAME"] : "";

        //出願区分
        $arg["data"]["TESTDIV"] = $Row["TESTDIV"]? $Row["TESTDIV"]."：".$Row["TESTDIVNAME"] : "";

        //志望区分
        knjl090rForm1::FormatOpt($opt_cmcd);
        $seq = "";
        $desiredivLabel = "";
        $result = $db->query(knjl090rQuery::getDesirediv($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["DESIREDIV"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($judgeNameSpare1[$model->judgement] == '1') {
                $opt_cmcd[] = array("label" => $row["EXAMCOURSE"].":".$row["EXAMCOURSE_NAME"],
                                    "value" => $row["EXAMCOURSE"]);
            }
            $desiredivLabel .= $seq . $row["EXAMCOURSE_NAME"];
            $seq = "／";
        }
        $arg["data"]["DESIREDIV"]  = $desiredivLabel;

        knjl090rForm1::FormatOpt($opt["L011"]);
        knjl090rForm1::FormatOpt($opt["L012"]);
        $disabled_date = "disabled";//デフォルト：手続日を編集不可

        //入学コース
        knjl090rForm1::FormatOpt($opt_enter);

        //判定で合格各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1') {

            //手続区分に追加
            $opt["L011"] = knjl090rForm1::GetOpt($db, $model->year, array("L011"));

            //合格なら手続日を編集可能
            $disabled_date = "";
            $value_date = str_replace("-","/",$model->proceduredate);

            //手続区分が「済み」なら入学区分、入学コースに値を追加
            if ($model->procedurediv == "1") {
                $opt["L012"] = knjl090rForm1::GetOpt($db, $model->year, array("L012"));
                //入学コースは手続区分、入学区分共に「1:済み」の時のみ値を追加
                if ($model->entdiv === '1') {
                    $opt_enter = knjl090rForm1::GetEnterOpt($db, $model->year, $Row);
                }
            }
        }

        //志望学科コンボ
        $arg["data"]["COURSEMAJOR"] = knjl090rForm1::CreateCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "250", $opt_cmcd, "onChange=\"change_flg()\"");

        //入学コースコンボ
        $arg["data"]["ENTER_COURSEMAJOR"] = knjl090rForm1::CreateCombo($objForm, "ENTER_COURSEMAJOR", $Row["ENTER_COURSEMAJOR"], "250", $opt_enter, "onChange=\"change_flg()\"");

        //奨学生コンボ
        $opt["L031"] = knjl090rForm1::GetOpt($db, $model->year, array("L031"));
        $arg["data"]["JUDGE_KIND"] = knjl090rForm1::CreateCombo($objForm, "JUDGE_KIND", $model->judge_kind, "100", $opt["L031"], "onChange=\"change_flg()\"");

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090rForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //手続日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $value_date, "", "", $disabled_date);

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl090rForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"change_flg(), btn_submit('change')\";");

        //入試区分コンボ
        $namecd1 = "L004";
        $opt = (!isset($Row2["EXAMNO"])) ? array() : knjl090rForm1::GetOpt($db, $model->year, array($namecd1), 0) ;
        $arg["data"]["TESTDIV2"] = knjl090rForm1::CreateCombo($objForm, "TESTDIV2", $model->testdiv2, "150", $opt, "onChange=\"change_flg(), btn_submit('change_testdiv2')\";");

        //合否区分コンボ(受付データがなければ合否区分コンボに空セット)
        $opt = (!isset($Row2["EXAMNO"]) ? array() : knjl090rForm1::GetOpt($db, $model->year, array("L013"), 1, "", "", "JUDGEDIV"));
        $arg["data"]["JUDGEDIV"] = knjl090rForm1::CreateCombo($objForm, "JUDGEDIV", $model->judgediv, "150", $opt, "onChange=\"btn_submit('change')\";");

        //１次手続区分コンボ ※入試区分が2:一般入試または願書(一般)ありで事前データがない場合は表示する
        if ($model->testdiv2 === '2' || $model->testdiv2 == "") {
            $arg["data"]["PROCEDURE_NAME2"] = '２次手続区分';
            $arg["data"]["PROCEDURE_NAME1"] = '１次手続区分';
            $arg["data"]["PROCEDUREDATE_NAME1"] = '手続日: ';
            $opt = (isset($Row2["EXAMNO"]) && $model->judgediv == "1") ? knjl090rForm1::GetOpt($db, $model->year, array("L011")) : array();
            $arg["data"]["PROCEDUREDIV1"] = knjl090rForm1::CreateCombo($objForm, "PROCEDUREDIV1", $model->procedurediv1, "100", $opt, "onChange=\"change_flg()\";");

            //１次手続日
            $value_date1 = (isset($Row2["EXAMNO"]) && $model->judgediv == "1") ? str_replace("-","/",$model->proceduredate1) : "";
            $disabled_date1 = (isset($Row2["EXAMNO"]) && $model->judgediv == "1") ? "" : "disabled";
            $arg["data"]["PROCEDUREDATE1"] = View::popUpCalendar2($objForm, "PROCEDUREDATE1", $value_date1, "", "", $disabled_date1);
        } else {
            $arg["data"]["PROCEDURE_NAME2"] = '手続区分';
            $arg["data"]["PROCEDURE_NAME1"] = '';
        }

        //事前
        $arg["data"]["JIZEN"] = $Row["JIZEN"];
        //事前データの入試区分と受験コースを取得
        $jizen_data = array();
        $jizen_data = $db->getRow(knjl090rQuery::getEntexamApplicantBeforeDat($model, $model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["JIZEN_PAGE"], $Row["JIZEN_SEQ"]), DB_FETCHMODE_ASSOC);
        if ($jizen_data["JIZEN_TEST_CD"] != "" || $jizen_data["EXAMCOURSE_MARK"] != "") {
            $arg["data"]["JIZEN_SET_DATA"] = '('.$jizen_data["JIZEN_TEST_CD"].'-'.$jizen_data["EXAMCOURSE_MARK"].')';
        }

/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
//        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";
        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? str_replace("-","/",$Row["BIRTHDAY"]) : "";

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];
        //受付№（座席番号）
        $arg["data"]["RECEPTNO"]        = $Row2["RECEPTNO"];

        $arg["data"]["TOTAL4"]      = $Row2["TOTAL4"];
        $arg["data"]["TOTAL_RANK4"] = $Row2["TOTAL_RANK4"];
        $arg["data"]["DIV_RANK4"]   = $Row2["DIV_RANK4"];
        $arg["data"]["AVARAGE4"]    = $Row2["AVARAGE4"];

/***************************************************** CREATE BUTTONS ********************************************************/

        //検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"btn_submit('reference');\"" ) );
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");

        global $sess;
        //カナ検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_kana_reference",
                            "value"     => "カナ検索",
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090R/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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
        View::toHTML($model, "knjl090rForm1.html", $arg);
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

    function GetOpt(&$db, $year, $namecd, $flg=1, $namecd2="", $namecd3="", $name="")
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl090rQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($name == "JUDGEDIV" && $row["NAMECD2"] == "4") continue;

                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }
    
    function GetEnterOpt(&$db, $year, $Row)
    {
        $opt = array();
        $opt[] = array("label" => "", "value" => "");

        if (is_array($Row)) {
            $result = $db->query(knjl090rQuery::getCourseMajorCoursecode($year));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //入学コース取得
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
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
        if ($name === 'TESTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>