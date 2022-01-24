<?php

require_once('for_php7.php');

class knjl090nForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090nindex.php", "", "main");
        $db = Query::dbCheckOut();

        /****************************************** GET DATA!! ***************************************************************/

        $Row = $db->getRow(knjl090nQuery::getEditData($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090nQuery::getEditData($model), DB_FETCHMODE_ASSOC);
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
            $model->testdiv2 = $db->getOne(knjl090nQuery::getMaxtestdiv($model, $Row["APPLICANTDIV"]));    //最大testdiv取得
        }
        $Row2 = $db->getRow(knjl090nQuery::getRecept($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);
        if (!isset($model->warning) && ($model->cmd != 'change')) {
            $model->judgediv = $Row2["JUDGEDIV"];
        }
        
        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["PROCEDUREDATE"]       =& $model->field["PROCEDUREDATE"];
            $Row["PAY_MONEY"]           =& $model->field["PAY_MONEY"];
            $Row["COURSEMAJOR"]     =& $model->field["COURSEMAJOR"];
        }
        //志願者得点データ
        $result = $db->query(knjl090nQuery::getScore($model, $Row["APPLICANTDIV"]));
        while ($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscd = $Row4["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
        }
        $result->free();

        //受験科目 判定名称
        $result = $db->query(knjl090nQuery::getName($model->year, array("L013","L009")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD1"]=="L009") {
                $arg["data"]["TESTSUBCLASSCD".$row["NAMECD2"]]  = $row["NAME1"];
            }
            if ($row["NAMECD1"]=="L013") {
                $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            }
            if ($row["NAMECD1"]=="L013") {
                $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
            }
        }
        $result->free();

        /*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;
        if (isset($model->examno)) {
            if (strlen($Row2["RECEPTNO"])) {
                $arg["data"]["EXAMINEE"] = "【受験】";
            }
        }

        //受験番号
        $objForm->ae(array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //判定
        if (strlen($model->examno) && ($model->cmd == 'change' || isset($model->warning))) {
            //2:不合格
            if (!strlen($Row["COURSEMAJOR"])) {
                $model->judgement         = "2";
                $arg["data"]["JUDGEMENT"] = $model->judgement . ":" . $judgename[$model->judgement];
            //1:合格
            } elseif (strlen($Row["COURSEMAJOR"]) && $Row["COURSEMAJOR"] == $Row["EXAMCOURSE"]) {
                $model->judgement         = "1";
                $arg["data"]["JUDGEMENT"] = $model->judgement . ":" . $judgename[$model->judgement];
            //3:まわし合格
            } elseif (strlen($Row["COURSEMAJOR"]) && $Row["COURSEMAJOR"] != $Row["EXAMCOURSE"]) {
                //判定はENTEXAM_COURSE_JUDGMENT_MSTがあれば「3:まわし合格」
                $cntMawashi = $db->getOne(knjl090nQuery::getSucCourseMawashi($model->year, $Row["EXAMCOURSE"], $Row["COURSEMAJOR"]));
                $model->judgement         = (0 < $cntMawashi) ? "3" : "1";
                $arg["data"]["JUDGEMENT"] = $model->judgement . ":" . $judgename[$model->judgement];
            }
        } else {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT"] ? $Row["JUDGEMENT"] . ":" . $judgename[$Row["JUDGEMENT"]] : "";
        }

        //氏名(判定で合格ならを赤、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1') {
            $setColor = "red";
            $arg["data"]["NAME"]      = "<font color=\"{$setColor}\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"{$setColor}\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

        /**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $Row["APPLICANTDIV"]? $Row["APPLICANTDIV"]."：".$Row["APPLICANTDIVNAME"] : "";

        //出願区分
        $arg["data"]["TESTDIV"] = $Row["TESTDIV"]? $Row["TESTDIV"]."：".$Row["TESTDIVNAME"] : "";

        //専併区分
        $arg["data"]["SHDIV"]       = $Row["SHDIV"] ? $Row["SHDIV"]."：".$Row["SHDIVNAME"] : "";

        //志望区分
        $arg["data"]["EXAMCOURSE"]  = $Row["EXAMCOURSE"] ? $Row["EXAMCOURSE"].":".$Row["EXAMCOURSE_NAME"] : "";

        //志望区分（第二志望）
        $arg["data"]["EXAMCOURSE2"] = $Row["EXAMCOURSE2"] ? $Row["EXAMCOURSE2"].":".$Row["EXAMCOURSE_NAME2"] : "";

        //合格コースコンボ
        knjl090nForm1::formatOpt($opt_cmcd);
        $query = knjl090nQuery::getSucCourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["EXAMCOURSE"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_cmcd[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        }
        $result->free();

        knjl090nForm1::formatOpt($opt["L011"]);
        knjl090nForm1::formatOpt($opt["L012"]);
        $disabled_date = "disabled";//デフォルト：手続日を編集不可

        //入学コース
        knjl090nForm1::formatOpt($opt_enter);

        //判定で合格各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1') {
            //手続区分に追加
            $opt["L011"] = knjl090nForm1::getOpt($db, $model->year, array("L011"));

            //合格なら手続日を編集可能
            $disabled_date = "";
            $value_date = str_replace("-", "/", $model->proceduredate);
            $value_pay_money = strlen($model->pay_money) ? number_format($model->pay_money) : "";

            //手続区分が「済み」なら入学区分に値を追加
            if ($model->procedurediv == "1") {
                $opt["L012"] = knjl090nForm1::getOpt($db, $model->year, array("L012"));
                //入学コースは手続区分、入学区分共に「1:済み」の時のみ値を追加
                if ($model->entdiv === '1') {
                    $opt_enter = knjl090nForm1::getEnterOpt($db, $model->year, $Row);
                }
            }
        }

        //合格コースコンボ
        $arg["data"]["COURSEMAJOR"] = knjl090nForm1::createCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "250", $opt_cmcd, "onChange=\"change_flg(), btn_submit('change')\"");

        //入学コースコンボ
        $arg["data"]["ENTER_COURSEMAJOR"] = knjl090nForm1::createCombo($objForm, "ENTER_COURSEMAJOR", $Row["ENTER_COURSEMAJOR"], "250", $opt_enter, "onChange=\"change_flg()\"");

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090nForm1::createCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"change_flg(), btn_submit('change')\";");

        //手続日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $value_date, "", "", $disabled_date);

        //入金額
        $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value); this.value=addFigure(this.value)\", onChange=\"change_flg()\" " .$disabled_date;
        $arg["data"]["PAY_MONEY"] = knjCreateTextBox($objForm, $value_pay_money, "PAY_MONEY", 9, 9, $extra);

        //入辞区分コンボ
        $arg["data"]["ENTDIV"] = knjl090nForm1::createCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"change_flg(), btn_submit('change')\";");

        //入試区分コンボ
        $namecd1 = "L004";
        $opt = (!isset($Row2["EXAMNO"])) ? array() : knjl090nForm1::getOpt($db, $model->year, array($namecd1), 0) ;
        $arg["data"]["TESTDIV2"] = knjl090nForm1::createCombo($objForm, "TESTDIV2", $model->testdiv2, "150", $opt, "onChange=\"change_flg()\";");

        //合否区分(受付データがなければ空セット)
        if (!isset($Row2["EXAMNO"])) {
            $arg["data"]["JUDGEDIV"] = "";
            knjCreateHidden($objForm, "JUDGEDIV", "");
        } else {
            $model->judgediv = $model->judgement;
            $arg["data"]["JUDGEDIV"] = $model->judgediv . ":" . $judgename[$model->judgediv];
            knjCreateHidden($objForm, "JUDGEDIV", $model->judgediv);
        }

        /********************************************************* SET DATA ********************************************************/

        $arg["data"]["NAME_KANA"]   = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]         = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
//        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";
        $arg["data"]["BIRTHDAY"]    = $Row["BIRTHDAY"]? str_replace("-", "/", $Row["BIRTHDAY"]) : "";

        $arg["data"]["TOTAL4"]      = $Row2["TOTAL4"];
        $arg["data"]["AVARAGE4"]    = $Row2["AVARAGE4"];
        $arg["data"]["DIV_RANK4"]   = $Row2["DIV_RANK4"];

        /***************************************************** CREATE BUTTONS ********************************************************/

        //検索ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"btn_submit('reference');\"" ));
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");

        global $sess;
        //かな検索ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_kana_reference",
                            "value"     => "かな検索",
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090N/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\""));
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");

        //前の志願者検索ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => " << ",
                            "extrahtml" => "onClick=\"btn_submit('back1');\"" ));
        
        //次の志願者検索ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_next",
                            "value"     => " >> ",
                            "extrahtml" => "onClick=\"btn_submit('next1');\"" ));
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");

        //更新ボタン
        $objForm->ae(array("type"      =>  "button",
                            "name"      =>  "btn_update",
                            "value"     =>  "更 新",
                            "extrahtml" =>  "onclick=\"btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新ボタン(更新後前の志願者)
        $objForm->ae(array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" =>  "style=\"width:150px\" onclick=\"btn_submit('back2');\"" ));
        //更新ボタン(更新後次の志願者)
        $objForm->ae(array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" =>  "style=\"width:150px\" onclick=\"btn_submit('next2');\"" ));
        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");

        //取消ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"btn_submit('reset');\""  ));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"OnClosing();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "auth_check",
                            "value"     => (AUTHORITY == DEF_UPDATABLE && is_array($Row)) ? "2" : (AUTHORITY == DEF_UPDATABLE && !is_array($Row) ? "1" : "0") ));
        //入試年度
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cflg",
                            "value"     => $model->cflg));

        $arg["IFRAME"] = View::setIframeJs();

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl090nForm1.html", $arg);
    }

    public function createCombo(&$objForm, $name, $value, $width, $opt, $extra)
    {
        $objForm->ae(array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra." style=\"width:".$width."\"",
                            "value"       => $value,
                            "options"     => $opt ));
        return $objForm->ge($name);
    }
    
    public function formatOpt(&$opt, $flg = 1)
    {
        $opt = array();
        if ($flg == "1") {
            $opt[] = array("label" => "", "value" => "");
        }
    }

    public function getOpt(&$db, $year, $namecd, $flg = 1, $namecd2 = "", $namecd3 = "")
    {
        $opt = array();
        if ($flg == "1") {
            $opt[] = array("label" => "", "value" => "");
        }

        if (is_array($namecd)) {
            $result = $db->query(knjl090nQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }
    
    public function getEnterOpt(&$db, $year, $Row)
    {
        $opt = array();
        $opt[] = array("label" => "", "value" => "");

        if (is_array($Row)) {
            $result = $db->query(knjl090nQuery::getCourseMajorCoursecode($year, $Row["COURSEMAJOR"]));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //入学コース取得
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
        }
        return $opt;
    }
}
