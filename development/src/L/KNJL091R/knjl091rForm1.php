<?php

require_once('for_php7.php');

class knjl091rForm1 {
    function main(&$model) {

        $objForm      = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjl091rindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $Row = $db->getRow(knjl091rQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl091rQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
            $model->applicantdiv = $Row["APPLICANTDIV"];
        }

        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_testdiv2') {
                $model->judgement           = $Row["JUDGEMENT"];
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
        $Row2 = $db->getRow(knjl091rQuery::getRecept($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["PROCEDUREDATE"] =& $model->field["PROCEDUREDATE"];
            if (strlen($model->examno) && (isset($model->warning))) {
                $Row["ENTER_COURSEMAJOR"] =& $model->field["ENTER_COURSEMAJOR"];
                $Row["COURSEMAJOR"]       =& $model->field["COURSEMAJOR"];
            }
        }

        //判定名称
        $result = $db->query(knjl091rQuery::getName($model->year, array("L013")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD1"] == "L013") $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
        }

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ(2:中学)
        $query = knjl091rQuery::get_name_cd($model->year, "L003", $model->fixApplicantDiv);
        $extra = "onChange=\"return btn_submit('changeApp');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //管理番号
        $extra = " onchange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1'){
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }
        //氏名カナ
        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        //性別
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
        //生年月日
        $arg["data"]["ERA_NAME"]   = ($Row["ERA_NAME"] != '') ? $Row["ERA_NAME"]: '　　';
        $arg["data"]["BIRTH_Y"]    = ($Row["BIRTH_Y"] != '')  ? $Row["BIRTH_Y"] : '　';
        $arg["data"]["BIRTH_M"]    = ($Row["BIRTH_M"] != '')  ? $Row["BIRTH_M"] : '　';
        $arg["data"]["BIRTH_D"]    = ($Row["BIRTH_D"] != '')  ? $Row["BIRTH_D"] : '　';

        //合否コンボ
        $query = knjl091rQuery::get_name_cd($model->year, 'L013');
        $extra = " onChange=\"change_flg(), btn_submit('change_testdiv2')\";";
        makeCmb($objForm, $arg, $db, $query, $model->judgement, "JUDGEMENT", $extra, 1, "BLANK");

        //合格入試区分
        $arg["data"]["PASS_ENTDIV"]   = ($Row["PASS_ENTDIV"] != '') ? $db->getOne(knjl091rQuery::get_name_cd($model->year, 'L024', $Row["PASS_ENTDIV"])): '';
        //合格受験番号
        $arg["data"]["PASS_EXAMNO"]   = $Row["PASS_EXAMNO"];
        //合格合否区分
        $arg["data"]["PASS_JUDGEDIV"] = ($Row["PASS_JUDGEDIV"] != '') ? $db->getOne(knjl091rQuery::get_name_cd($model->year, 'L013', $Row["PASS_JUDGEDIV"])): '';

        //合格コース
        knjl091rForm1::FormatOpt($opt_cmcd);
        $result = $db->query(knjl091rQuery::getEntexamCourseMst($model->year, $Row["APPLICANTDIV"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($judgeNameSpare1[$model->judgement] == '1') {
                $opt_cmcd[] = array("label" => $row["LABEL"],
                                    "value" => $row["VALUE"]);
            }
        }

        knjl091rForm1::FormatOpt($opt["L011"]);
        knjl091rForm1::FormatOpt($opt["L012"]);
        $disabled_date = "disabled";//デフォルト：手続日を編集不可

        //入学コース
        knjl091rForm1::FormatOpt($opt_enter);

        //判定で合格の時、各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1') {

            //手続区分に追加
            $opt["L011"] = knjl091rForm1::GetOpt($db, $model->year, array("L011"));

            //合格なら手続日を編集可能
            $disabled_date = "";
            $value_date = str_replace("-","/",$model->proceduredate);

            //手続区分が「済み」なら入学区分、入学コースに値を追加
            if ($model->procedurediv == "1") {
                $opt["L012"] = knjl091rForm1::GetOpt($db, $model->year, array("L012"));
                //入学コースは手続区分、入学区分共に「1:済み」の時のみ値を追加
                if ($model->entdiv === '1') {
                    $opt_enter = knjl091rForm1::GetEnterOpt($db, $model->year, $Row);
                }
            }
        }

        //合格コースコンボ
        $arg["data"]["COURSEMAJOR"] = knjl091rForm1::CreateCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "250", $opt_cmcd, "onChange=\"change_flg()\"");

        //入学コースコンボ
        $arg["data"]["ENTER_COURSEMAJOR"] = knjl091rForm1::CreateCombo($objForm, "ENTER_COURSEMAJOR", $Row["ENTER_COURSEMAJOR"], "250", $opt_enter, " onChange=\"change_flg()\"");

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl091rForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //手続日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $value_date, "", "", $disabled_date);

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl091rForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"change_flg(), btn_submit('change')\";");

        /***************/
        /* RECEPT_DATA */
        /***************/
        //入試区分コンボ
        $model->testDivArr = array();
        $namecd1 = $model->applicantdiv == '2' ? 'L024': 'L004';
        $query = knjl091rQuery::get_name_cd($model->year, $namecd1);
        $extra = " onChange=\"change_flg(), btn_submit('change_testdiv2')\";";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv2, "TESTDIV2", $extra, 1, "");
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->testDivArr[$row["VALUE"]] = $row["VALUE"];
        }

        //受験科目
        $query = knjl091rQuery::get_name_cd($model->year, 'L009');
        $result = $db->query($query);
        $cnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE1"] == $model->testdiv2) {
                $arg["data"]["TESTSUBCLASSCD".$cnt]  = $row["NAME2"];
                $cnt++;
            }
        }

        //志願者得点データ
        $cnt = 1;
        $query = knjl091rQuery::getScore($model, $Row["APPLICANTDIV"]);
        $result = $db->query($query);
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscd = $Row4["TESTSUBCLASSCD"];

            if ($Row4["NAMESPARE1"] == $model->testdiv2) {
                $arg["data"]["ATTEND".$cnt] = ($Row4["ATTEND_FLG"] == "1")? "○" : "";
                $arg["data"]["SCORE".$cnt]  = $Row4["SCORE"];
                $cnt++;
            }
        }

        //合否区分（表示のみ）
        $arg["data"]["JUDGEDIV"]        = $Row2["JUDGEDIV"];

        //手続日（表示のみ）recept
        $arg["data"]["PROCEDUREDATE1"] = str_replace('-', '/', $Row2["PROCEDUREDATE1"]);

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];

        //受付№
        $arg["data"]["RECEPTNO"]        = $Row2["RECEPTNO_".$model->testdiv2];
        //専併区分
        if ($Row2["SHDIV_".$model->testdiv2] != '') {
            $arg["data"]["SHDIV"]           = ($Row2["SHDIV_".$model->testdiv2] == '1') ? '専願': '併願';
        }
        //内諾
        $arg["data"]["INNER_PROMISE"]   = ($Row2["INNER_PROMISE_".$model->testdiv2] != '') ? $db->getOne(knjl091rQuery::get_name_cd($model->year, 'L064', $Row2["INNER_PROMISE_".$model->testdiv2])): '';

        //加点１
        $arg["data"]["ADD_SCORE1"]      = $Row2["ADD_SCORE1"];
        //加点２
        $arg["data"]["ADD_SCORE2"]      = $Row2["ADD_SCORE2"];
        //合計
        $arg["data"]["TOTAL4"]          = $Row2["TOTAL4"];
        //平均
        $arg["data"]["AVARAGE4"]        = $Row2["AVARAGE4"];
        //全体順位
        $arg["data"]["TOTAL_RANK4"]     = $Row2["TOTAL_RANK4"];

        /**********/
        /* button */
        /**********/

        global $sess;
        //検索ボタン
        $extra = " onclick=\"btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //カナ検索ボタン
        $extra = " style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL091R/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "カナ検索", $extra);

        //前の志願者検索ボタン
        $extra = " onClick=\"btn_submit('back1');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = " onClick=\"btn_submit('next1');\"";
        $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //更新
        $extra = " onclick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新ボタン(更新後前の志願者)
        $extra = " style=\"width:150px\" onclick=\"btn_submit('back2');\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

        //更新ボタン(更新後次の志願者)
        $extra = "style=\"width:150px\" onclick=\"btn_submit('next2');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

        //取消
        $extra = " onclick=\"btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了
        $extra = " onclick=\"OnClosing();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "auth_check", (AUTHORITY == DEF_UPDATABLE && is_array($Row)) ? "2" : (AUTHORITY == DEF_UPDATABLE && !is_array($Row) ? "1" : "0"));
        knjCreateHidden($objForm, "cflg", $model->cflg);

        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl091rForm1.html", $arg);
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

    function GetOpt(&$db, $year, $namecd, $flg=1, $namecd2="", $namecd3="", $name="") {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl091rQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($name == "JUDGEDIV" && $row["NAMECD2"] == "4") continue;

                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }

    function GetEnterOpt(&$db, $year, $Row) {
        $opt = array();
        $opt[] = array("label" => "", "value" => "");

        if (is_array($Row)) {
            $result = $db->query(knjl091rQuery::getCourseMajorCoursecode($year, $Row["APPLICANTDIV"]));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>