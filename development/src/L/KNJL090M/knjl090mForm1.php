<?php

require_once('for_php7.php');

class knjl090mForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090mindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090mQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090mQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
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

        //受付データ取得
        $Row2 = $db->getRow(knjl090mQuery::getRecept($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($Row["EXAMNO"]) && (isset($model->warning)) || $model->cmd == 'change') {
            $Row["JUDGEMENT"]             =& $model->field["JUDGEMENT"];
            $Row["SUC_COURSECD"]          =& $model->field["SUC_COURSECD"];
            $Row["SUC_MAJORCD"]           =& $model->field["SUC_MAJORCD"];
            $Row["SUC_COURSECODE"]        =& $model->field["SUC_COURSECODE"];
            $Row["PROCEDUREDATE"]         =& $model->field["PROCEDUREDATE"];
            $Row["SUCCESS_NOTICENO"]      =& $model->field["SUCCESS_NOTICENO"];
            $Row["FAILURE_NOTICENO"]      =& $model->field["FAILURE_NOTICENO"];
            $Row["REMARK1"]               =& $model->field["REMARK1"];
            $Row["REMARK2"]               =& $model->field["REMARK2"];
            $Row["SUB_ORDER"]             =& $model->field["SUB_ORDER"];
        }

        //試験科目
        $opt_testsub = array();
        $number = 0;
        $result = $db->query(knjl090mQuery::getTestsubclasscd($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $number++;
            $opt_testsub[$row["TESTSUBCLASSCD"]]  = $number;
        }
        $result->free();

        //志願者得点データ
        $result = $db->query(knjl090mQuery::getScore($model));
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd = $opt_testsub[$Row4["TESTSUBCLASSCD"]];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
            $arg["data"]["STDSC".$subclasscd]  = $Row4["STD_SCORE"];
            $arg["data"]["RANK".$subclasscd]   = $Row4["RANK"];
        }
        $result->free();

        //受験科目 内申科目 判定名称
        $optJudgement = array();
        $optJudgement[] = array("label" => "", "value" => "");
        $judgeSpare = array();
        $result = $db->query(knjl090mQuery::getName($model->year, array("L013","L008","L009")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L008") $arg["data"]["CONFIDENTIAL".$row["NAMECD2"]]    = $row["NAME1"];
            if ($row["NAMECD1"]=="L009") $arg["data"]["TESTSUBCLASSCD".$opt_testsub[$row["NAMECD2"]]]  = $row["NAME1"];
            if ($row["NAMECD1"]=="L013") {
                $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
                $judgeSpare[$row["NAMECD2"]] = $row["NAMESPARE1"];
                if (isset($Row["EXAMNO"]) && $row["NAMECD2"] != "5") {
                    $optJudgement[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                                            "value"  =>  $row["NAMECD2"]);
                }
            }
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        if (isset($Row["EXAMNO"])) {
            //何れかの入試区分で受験区分が「有り」であればラベルを表示
            $DESIRE_FLG["EXAMINEE_DIV"] = $db->getOne(knjl090mQuery::get_desire_flg($model));
            if(isset($DESIRE_FLG["EXAMINEE_DIV"])) {
                $arg["data"]["EXAMINEE"] = "【受験】";
            }
        }

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 3,
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

        $model->judgement = $Row["JUDGEMENT"];

        //氏名(判定で合格なら赤、措置は青、その他黒)
        if ($judgeSpare[$model->judgement] == '1' && $model->special_measures == '') {
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

        //「2:補員、4:補員合格」のみ入力可
        $disSubOrder = ($model->judgement == '2') ? "" : "readonly ";
        //補員順位
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUB_ORDER",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => $disSubOrder ."onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["SUB_ORDER"] ));
        $arg["data"]["SUB_ORDER"] = $objForm->ge("SUB_ORDER");

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $Row["APPLICANTDIV"]? $Row["APPLICANTDIV"]."：".$Row["APPLICANTDIVNAME"] : "";

        //合否判定コンボ(受付データがなければ合否判定コンボに空セット)
        $arg["data"]["JUDGEMENT"] = knjl090mForm1::CreateCombo($objForm, "JUDGEMENT", $model->judgement, "100", $optJudgement, "onChange=\"btn_submit('change')\";");

        knjl090mForm1::FormatOpt($opt_cmcd);
        knjl090mForm1::FormatOpt($opt["L011"]);
        knjl090mForm1::FormatOpt($opt["L012"]);

        //判定で合格各コンボに値を追加
        if ($judgeSpare[$model->judgement] == '1') {
            //志望学科に追加
            knjl090mForm1::FormatOpt($opt_cmcd,0);
            $result     = $db->query(knjl090mQuery::get_coursemajor($model->year));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt_cmcd[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["COURSENAME"],
                                    "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
            }
            $Row["COURSEMAJOR"] = $Row["SUC_COURSECD"].$Row["SUC_MAJORCD"].$Row["SUC_COURSECODE"];
            //手続区分に追加
            $opt["L011"] = knjl090mForm1::GetOpt($db, $model->year, array("L011"));
            //手続区分が「済み」なら入学区分に値を追加
            if ($model->procedurediv == "1") {
                $opt["L012"] = knjl090mForm1::GetOpt($db, $model->year, array("L012"));
            }
        }

        //志望学科コンボ
        $arg["data"]["COURSEMAJOR"] = knjl090mForm1::CreateCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "200", $opt_cmcd, "onChange=\"change_flg()\"");

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090mForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //手続き日付
        $Row["PROCEDUREDATE"] = str_replace("-", "/", $Row["PROCEDUREDATE"]);
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar($objForm, "PROCEDUREDATE", $Row["PROCEDUREDATE"]);

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl090mForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"btn_submit('change')\";");

/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
        $arg["data"]["FS_GRDYEAR"] = $Row["FS_GRDYEAR"]? $Row["FS_GRDYEAR"]."年" : "";
//        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";
        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? common::DateConv1(str_replace("-","/",$Row["BIRTHDAY"]), "0") : "";

        //合否区分
        $arg["data"]["JUDGEDIV"] = $Row2["JUDGEDIV"]? $Row2["JUDGEDIV"]."：".$judgename[$Row2["JUDGEDIV"]] : "";

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];
        //受付№（座席番号）
        $arg["data"]["RECEPTNO"]        = preg_replace('/^0*/', '', $Row2["RECEPTNO"]);
        //受験型
        $arg["data"]["EXAM_TYPE"] = $Row2["EXAM_TYPE"]? $Row2["EXAM_TYPE"]."：".$Row2["NAME1"] : "";

        $arg["data"]["TOTAL4"]      = $Row2["TOTAL4"];
        $arg["data"]["TOTAL_RANK4"] = $Row2["TOTAL_RANK4"];
        $arg["data"]["JUDGE_DEVIATION"]      = $Row2["JUDGE_DEVIATION"];
        $arg["data"]["JUDGE_DEVIATION_RANK"] = $Row2["JUDGE_DEVIATION_RANK"];

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
        //カナ検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_kana_reference",
                            "value"     => "カナ検索",
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090M/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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
        View::toHTML($model, "knjl090mForm1.html", $arg);
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
            $result = $db->query(knjl090mQuery::getName($year, $namecd));
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