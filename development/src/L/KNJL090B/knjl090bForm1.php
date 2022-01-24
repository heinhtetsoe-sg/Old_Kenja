<?php

require_once('for_php7.php');

class knjl090bForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090bindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090bQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090bQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
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
            $model->testdiv2 = $db->getOne(knjl090bQuery::getMaxtestdiv($model, $Row["APPLICANTDIV"]));    //最大testdiv取得
        }
        $Row2 = $db->getRow(knjl090bQuery::getRecept($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);
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
        //単願手続区分が1:済みの場合は、合否区分を1:合格に変更
        if ($model->cmd == 'change' && $model->procedurediv1 == '1') {
            $model->judgediv = '1';
        }
        //単願切換の合格コースをセット
        if ($model->cmd == 'change' && $model->procedurediv1 == '1' && $Row2["TANGAN_CD"] != '') {
            $Row["COURSEMAJOR"] = $Row2["TANGAN_CD"];
        }
        //志願者得点データ
        $result = $db->query(knjl090bQuery::getScore($model, $Row["APPLICANTDIV"]));
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd = $Row4["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
        }
        $result->free();

        //受験科目 判定名称
        $result = $db->query(knjl090bQuery::getName($model->year, array("L013","L009")));
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
            if (strlen($Row2["RECEPTNO"]) && $Row2["JUDGEDIV"] != "3") {
                $arg["data"]["EXAMINEE"] = "【受験】";
            }
        }

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //判定
        $judge = $db->getRow(knjl090bQuery::getReceptJudge($model), DB_FETCHMODE_ASSOC); 

        if ($judgeNameSpare1[$model->judgediv] == "1") $model->special_measures = "";

        //変更無し
        if ($model->cmd != "change" && !isset($model->warning))  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = strlen($Row2["JUDGE_TANSIN"]) ? "条件付合格" : $Row["JUDGEMENT_NAME"];

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

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1'){
            $setColor = $Row["PROCEDUREDIV1"] == "1" ? "blue" : "red";
            $arg["data"]["NAME"]      = "<font color=\"{$setColor}\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"{$setColor}\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

        $arg["data"]["TANGAN_DISP"] = $Row["PROCEDUREDIV1"] == "1" ? "【単願】" : "";

        $arg["data"]["SPECIAL_REASON_DIV"] = $Row["SPECIAL_REASON_DIV"] != "" ? "特別措置者" : "";

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $Row["APPLICANTDIV"]? $Row["APPLICANTDIV"]."：".$Row["APPLICANTDIVNAME"] : "";

        //出願区分
        $arg["data"]["TESTDIV"] = $Row["TESTDIV"]? $Row["TESTDIV"]."：".$Row["TESTDIVNAME"] : "";

        //志望区分
        knjl090bForm1::FormatOpt($opt_cmcd);
        $seq = "";
        $desiredivLabel = "";
        $result = $db->query(knjl090bQuery::getDesirediv($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($judgeNameSpare1[$model->judgement] == '1') {
                $opt_cmcd[] = array("label" => $row["EXAMCOURSE"].":".$row["EXAMCOURSE_NAME"]."（".$row["EXAMCOURSE_ABBV"]."）",
                                    "value" => $row["EXAMCOURSE"]);
            }
            $desiredivLabel .= $seq . $row["EXAMCOURSE_NAME"];
            $seq = "／";
        }
        $arg["data"]["DESIREDIV"]  = $desiredivLabel;

        knjl090bForm1::FormatOpt($opt["L011"]);
        knjl090bForm1::FormatOpt($opt["L012"]);
        $disabled_date = "disabled";//デフォルト：手続日を編集不可

        //入学コース
        knjl090bForm1::FormatOpt($opt_enter);

        //判定で合格各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1') {

            //手続区分に追加
            $opt["L011"] = knjl090bForm1::GetOpt($db, $model->year, array("L011"));

            //合格なら手続日を編集可能
            $disabled_date = "";
            $value_date = str_replace("-","/",$model->proceduredate);

            //手続区分が「済み」なら入学区分に値を追加
            if ($model->procedurediv == "1") {
                $opt["L012"] = knjl090bForm1::GetOpt($db, $model->year, array("L012"));
                //入学コースは手続区分、入学区分共に「1:済み」の時のみ値を追加
                if ($model->entdiv === '1') {
                    $opt_enter = knjl090bForm1::GetEnterOpt($db, $model->year, $Row);
                }
            }
        }

        //合格コースコンボ
        $arg["data"]["COURSEMAJOR"] = knjl090bForm1::CreateCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "300", $opt_cmcd, "onChange=\"change_flg()\"");

        //入学コースコンボ
        $arg["data"]["ENTER_COURSEMAJOR"] = knjl090bForm1::CreateCombo($objForm, "ENTER_COURSEMAJOR", $Row["ENTER_COURSEMAJOR"], "250", $opt_enter, "onChange=\"change_flg()\"");

        //奨学生コンボ
        $opt["L031"] = knjl090bForm1::GetOpt($db, $model->year, array("L031"));
        $arg["data"]["JUDGE_KIND"] = knjl090bForm1::CreateCombo($objForm, "JUDGE_KIND", $model->judge_kind, "100", $opt["L031"], "onChange=\"change_flg()\"");

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090bForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //手続日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $value_date, "", "", $disabled_date);

        //入辞区分コンボ
        $arg["data"]["ENTDIV"] = knjl090bForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"change_flg(), btn_submit('change')\";");

        //受付番号
        //手続キャンセルした場合、受付番号を取消す。
        $arg["data"]["SUB_ORDER"] = ($model->procedurediv != "1") ? "" : $Row["SUB_ORDER"];

        //入試区分コンボ
        $namecd1 = "L004";
        $opt = (!isset($Row2["EXAMNO"])) ? array() : knjl090bForm1::GetOpt($db, $model->year, array($namecd1), 0) ;
        $arg["data"]["TESTDIV2"] = knjl090bForm1::CreateCombo($objForm, "TESTDIV2", $model->testdiv2, "150", $opt, "onChange=\"change_flg(), btn_submit('change_testdiv2')\";");

        //合否区分(受付データがなければ空セット)
        if (!isset($Row2["EXAMNO"])) {
            $arg["data"]["JUDGEDIV"] = "";
            knjCreateHidden($objForm, "JUDGEDIV", "");
        } else {
            $arg["data"]["JUDGEDIV"] = $model->judgediv . ":" . $judgename[$model->judgediv];
            knjCreateHidden($objForm, "JUDGEDIV", $model->judgediv);
        }

        //単願手続区分コンボ
        $opt = (isset($Row2["EXAMNO"])) ? knjl090bForm1::GetOpt($db, $model->year, array("L011")) : array();
        $arg["data"]["PROCEDUREDIV1"] = knjl090bForm1::CreateCombo($objForm, "PROCEDUREDIV1", $model->procedurediv1, "100", $opt, "onChange=\"change_flg(), btn_submit('change')\";");

        //単願手続日
        $value_date1 = (isset($Row2["EXAMNO"])) ? str_replace("-","/",$model->proceduredate1) : "";
        $disabled_date1 = (isset($Row2["EXAMNO"])) ? "" : "disabled";
        $arg["data"]["PROCEDUREDATE1"] = View::popUpCalendar2($objForm, "PROCEDUREDATE1", $value_date1, "", "", $disabled_date1);

        //事前
        $arg["data"]["JIZEN"] = $Row["JIZEN"];
        //事前データの入試区分と受験コースを取得
        $jizen_data = array();
        $jizen_data = $db->getRow(knjl090bQuery::getEntexamApplicantBeforeDat($model, $model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["JIZEN_PAGE"], $Row["JIZEN_SEQ"]), DB_FETCHMODE_ASSOC);
        if ($jizen_data["JIZEN_TEST_CD"] != "" || $jizen_data["EXAMCOURSE_MARK"] != "") {
            $arg["data"]["JIZEN_SET_DATA"] = '('.$jizen_data["JIZEN_TEST_CD"].'-'.$jizen_data["EXAMCOURSE_MARK"].')';
        }

/********************************************************* SET DATA ********************************************************/

        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
//        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";
        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? str_replace("-","/",$Row["BIRTHDAY"]) : "";

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];
        //受付№（座席番号）
        $arg["data"]["RECEPTNO"]        = $Row2["RECEPTNO"];

        $arg["data"]["TOTAL3"]      = $Row2["TOTAL3"];
        $arg["data"]["TOTAL_RANK4"] = $Row2["TOTAL_RANK4"];
        $arg["data"]["DIV_RANK4"]   = $Row2["DIV_RANK4"];
        $arg["data"]["AVARAGE1"]    = $Row2["AVARAGE1"];
        $arg["data"]["AVARAGE3"]    = $Row2["AVARAGE3"];
        $arg["data"]["AVARAGE4"]    = $Row2["AVARAGE4"];

        //確約
        $query = knjl090bQuery::getKakuyaku($model);
        $setKakuyaku = $db->getOne($query);
        $arg["data"]["KAKUYAKU"] = $setKakuyaku;

        //正規、単願　高い方の合格コースを表示。つまり、単願があれば単願を表示。単願がなければ正規を表示。
        $arg["data"]["SEIKI_TANGAN"] = (strlen($Row2["TANGAN"])) ? $Row2["TANGAN2"] : $Row2["SEIKI2"];
        knjCreateHidden($objForm, "TANGAN_CD", $Row2["TANGAN_CD"]);


        //当日チェック
        $extra = " onchange=\"change_flg()\"";
        $arg["data"]["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1", 2, 41, "soft", $extra, $Row["REMARK1"]);

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
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090B/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl090bForm1.html", $arg);
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

    function GetOpt(&$db, $year, $namecd, $flg=1, $namecd2="", $namecd3="")
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl090bQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
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
            $result = $db->query(knjl090bQuery::getCourseMajorCoursecode($year));
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
?>