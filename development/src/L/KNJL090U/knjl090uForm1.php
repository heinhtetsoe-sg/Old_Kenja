<?php

require_once('for_php7.php');

class knjl090uForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090uindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090uQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090uQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
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
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change') {
            $Row["JUDGEMENT"] =& $model->field["JUDGEMENT"];
            $Row["PROCEDUREDATE"] =& $model->field["PROCEDUREDATE"];
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度
        $query = knjl090uQuery::get_name_cd($model->year, "L003");
        $extra = "onChange=\"change_flg(); return btn_submit('changeApp');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $query = knjl090uQuery::get_name_cd($model->year, "L004");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);

        //合否コンボ
        knjl090uForm1::FormatOpt($opt["L013"]);
        if (strlen($model->examno)) $opt["L013"] = knjl090uForm1::GetOpt($db, $model->year, array("L013"));
        $arg["data"]["JUDGEMENT"] = knjl090uForm1::CreateCombo($objForm, "JUDGEMENT", $Row["JUDGEMENT"], "100", $opt["L013"], "onChange=\"change_flg(), btn_submit('change')\";");
        $model->judgement = $Row["JUDGEMENT"];

        //判定名称
        $result = $db->query(knjl090uQuery::getName($model->year, array("L013")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L013") $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
        }
        $result->free();

        //氏名(判定で合格ならを赤、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1'){
            $setColor = "red";
            $arg["data"]["NAME"] = "<font color=\"{$setColor}\">".htmlspecialchars($Row["NAME"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

        //氏名カナ
        $arg["data"]["NAME_KANA"]   = htmlspecialchars($Row["NAME_KANA"]);

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        knjl090uForm1::FormatOpt($opt["L006"]);
        knjl090uForm1::FormatOpt($opt["L011"]);
        knjl090uForm1::FormatOpt($opt["L012"]);
        $disabled_date = "disabled";//デフォルト：手続日を編集不可
        $disNo = " disabled";

        //合格・入学コースフラグ
        $sucFlg = $proFlg = $entFlg = "";

        //判定で合格各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1') {
            $sucFlg = "1";

            //手続区分に追加
            $opt["L011"] = knjl090uForm1::GetOpt($db, $model->year, array("L011"));

            //手続区分が「済み」なら入学区分に値を追加
            if ($model->procedurediv == "1") {
                $proFlg = "1";
                //手続日を編集可能
                $disabled_date = "";
                $disNo = "";
                $opt["L006"] = knjl090uForm1::GetOpt($db, $model->year, array("L006"));
                $opt["L012"] = knjl090uForm1::GetOpt($db, $model->year, array("L012"));
                //入学コースは手続区分、入学区分共に「1:済み」の時のみ値を追加
                if ($model->entdiv === '1') {
                    $entFlg = "1";
                }
                if ($model->entdiv === '2') {
                    $disNo = " disabled";
                }
            }
        }

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090uForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"change_flg(), btn_submit('change')\";");

        //手続日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", str_replace("-", "/", $Row["PROCEDUREDATE"]), "", "", $disabled_date);

        //入辞区分コンボ
        $arg["data"]["ENTDIV"] = knjl090uForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"change_flg(), btn_submit('change')\";");

        //学籍番号
        $extra = "onChange=\"change_flg();\" onblur=\"checkNo(this);\"";
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $Row["SCHREGNO"], "SCHREGNO", 8, 8, $extra.$disNo);

/********************************************************* SET DATA ********************************************************/

        //受験科目
        $result = $db->query(knjl090uQuery::getTestSubclasscd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["TESTSUBCLASSCD".$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        //志願者得点データ
        $result = $db->query(knjl090uQuery::getScore($model));
        while ($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["SCORE" .$Row4["TESTSUBCLASSCD"]] = ($Row4["ATTEND_FLG"] == '0') ? "＊" : $Row4["SCORE"];
        }
        $result->free();

        //受付データ取得
        $Row2 = $db->getRow(knjl090uQuery::getRecept($model), DB_FETCHMODE_ASSOC);

        $arg["data"]["TOTAL4"]              = $Row2["TOTAL4"];
        $arg["data"]["TOTAL_RANK4"]         = $Row2["TOTAL_RANK4"];

        //観察者フラグ
        $arg["data"]["SLIDE_FLG"] = ($Row["SLIDE_FLG"] == '1') ? "有" : "";

/***************************************************** CREATE BUTTONS ********************************************************/

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        global $sess;
        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090U/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "onClick=\"btn_submit('back1');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "onClick=\"btn_submit('next1');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新ボタン(更新後前の志願者)
        $extra = "style=\"width:150px\" onclick=\"return btn_submit('back2');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

        //更新ボタン(更新後次の志願者)
        $extra = "style=\"width:150px\" onclick=\"return btn_submit('next2');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"OnClosing();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "cflg", $model->cflg);
        knjCreateHidden($objForm, "auth_check", (AUTHORITY == DEF_UPDATABLE && is_array($Row)) ? "2" : (AUTHORITY == DEF_UPDATABLE && !is_array($Row) ? "1" : "0"));

        $arg["IFRAME"] = View::setIframeJs();

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl090uForm1.html", $arg);
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
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");
    }

    function GetOpt(&$db, $year, $namecd, $flg=1, $namecd2="", $namecd3="") {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl090uQuery::getName($year, $namecd));
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
        if ($name == 'APPLICANTDIV' || $name == 'TESTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>