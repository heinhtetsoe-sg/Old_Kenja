<?php

require_once('for_php7.php');

class knjl590fForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl590findex.php", "", "main");
        $db = Query::dbCheckOut();

        $query = knjl590fQuery::get_edit_data($model);

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl590fQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
        }

        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_testdiv2') {
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
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["JUDGEMENT"] =& $model->field["JUDGEMENT"];
            $Row["COURSEMAJOR"] =& $model->field["COURSEMAJOR"];
            $Row["ENTER_COURSEMAJOR"] =& $model->field["ENTER_COURSEMAJOR"];
            $Row["ENTRANCE_FLG"] =& $model->field["ENTRANCE_FLG"];
            $Row["ENT_PAY_DIV"] =& $model->field["ENT_PAY_DIV"];
            $Row["ENT_PAY_DATE"] =& $model->field["ENT_PAY_DATE"];
            $Row["ENT_PAY_CHAK_DATE"] =& $model->field["ENT_PAY_CHAK_DATE"];
            $Row["EXP_PAY_DIV"] =& $model->field["EXP_PAY_DIV"];
            $Row["EXP_PAY_DATE"] =& $model->field["EXP_PAY_DATE"];
            $Row["EXP_PAY_CHAK_DATE"] =& $model->field["EXP_PAY_CHAK_DATE"];
            $Row["PROCEDUREDATE"] =& $model->field["PROCEDUREDATE"];
        }

        //入試区分の値が変わればそれをキーにして受付データ取得
        if ($model->cmd == 'change_testdiv2' || $model->cmd == 'change' || $model->cmd == 'update' || (isset($model->warning))) {
        } else {
            $model->testdiv2 = $db->getOne(knjl590fQuery::getMaxtestdiv($model, $Row["APPLICANTDIV"]));    //最大testdiv取得
            //合格した入試区分(baseの入試区分と同じ)があれば、それを初期表示
            //$testdiv2 = $db->getOne(knjl590fQuery::getMintestdiv($model, $Row["APPLICANTDIV"]));
            //if (strlen($testdiv2)) $model->testdiv2 = $testdiv2;
        }

        $query = knjl590fQuery::getRecept($model, $Row["APPLICANTDIV"]);
        $receptData = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //年度
        $arg["TOP"]["YEAR"] = $model->year;
        if (isset($model->examno)) {
            if (strlen($receptData["RECEPTNO"])) {
                $arg["data"]["EXAMINEE"] = "【受験】";
            }
        }

        //入試制度
        $query = knjl590fQuery::get_name_cd($model->year, "L003");
        $extra = "Onchange=\"btn_submit('changApp');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");
        $examnoLen = 4;
        if ($model->applicantdiv == '1') {
            $arg["data"]["EXAM_TITLE"] = "管理番号";
            $examnoLen = 5;
        } else {
            $arg["data"]["EXAM_TITLE"] = "受験番号";
        }


        //受験科目 判定名称
        $query = knjl590fQuery::getName($model->year, array("L009","L013"));
        $result = $db->query($query);
        $model->nameMst = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD1"]=="L009" && $row["NAME".$model->applicantdiv] == "") {
                continue;
            }
            $model->nameMst[$row["NAMECD1"]][$row["NAMECD2"]] = $row;
        }
        $result->free();

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", $examnoLen, $examnoLen, $extra);

        //氏名(判定で合格ならを赤、その他黒)
        $sucFlg = "";
        if ($model->nameMst["L013"][$model->judgement]["NAMESPARE1"] == '1') {
            $sucFlg = "1";
            $setColor = "red";
            $arg["data"]["NAME"] = "<font color=\"{$setColor}\">".htmlspecialchars($Row["NAME"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }
        $arg["data"]["NAME_KANA"] = htmlspecialchars($Row["NAME_KANA"]);

        //志望区分1
        $arg["data"]["EXAMCOURSE"]  = $Row["EXAMCOURSE"] ? $Row["EXAMCOURSE"].":".$Row["EXAMCOURSE_NAME"] : "";
        //志望区分2
        $arg["data"]["EXAMCOURSE2"]  = $Row["EXAMCOURSE2"] ? $Row["EXAMCOURSE2"].":".$Row["EXAMCOURSE_NAME2"] : "";

        //合否コンボ
        $query = knjl590fQuery::get_name_cd($model->year, "L013");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGEMENT"], "JUDGEMENT", $extra, 1, "BLANK");

        //合格コースコンボ
        $query = knjl590fQuery::getSucCourse($model->year, $Row["APPLICANTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["COURSEMAJOR"], "COURSEMAJOR", $extra, 1, "BLANK");

        //手続区分コンボ
        $query = knjl590fQuery::get_name_cd($model->year, "L011");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $model->procedurediv, "PROCEDUREDIV", $extra, 1, "BLANK");

        //手続日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar($objForm, "PROCEDUREDATE", str_replace("-", "/", $Row["PROCEDUREDATE"]));
        
        //入学区分コンボ
        $query = knjl590fQuery::get_name_cd($model->year, "L012");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["ENTDIV"], "ENTDIV", $extra, 1, "BLANK");

        //入試区分コンボ
        $namecd1 = $model->applicantdiv == '1' ? "L024" : "L004";
        $query = knjl590fQuery::get_name_cd($model->year, $namecd1);
        $extra = "onChange=\"btn_submit('change_testdiv2')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv2, "TESTDIV2", $extra, 1, "BLANK");

        //志願者得点データ取得
        $query = knjl590fQuery::getScore($model, $Row["APPLICANTDIV"]);
        $result = $db->query($query);
        $model->subclassCdArray = array();
        while ($scoreData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscd = $scoreData["TESTSUBCLASSCD"];
            $model->subclassCdArray[$subclasscd] = $scoreData;
        }
        $result->free();

        //得点
        $subCnt = 1;
        foreach ($model->nameMst["L009"] as $namecd2 => $nameMst) {
            $arg["data"]["TESTSUBCLASSCD".$subCnt] = $nameMst["NAME".$model->applicantdiv];
            $arg["data"]["SCORE".$subCnt] = $model->subclassCdArray[$namecd2]["SCORE"];
            $subCnt++;
        }
        $arg["data"]["TESTSUBCLASSCD".$subCnt] = "見なし得点";
        $arg["data"]["SCORE".$subCnt]          = $Row["MINASHI"];

        //合計欄
        $arg["data"]["TOTAL4"]          = $receptData["TOTAL4"];
        $arg["data"]["AVARAGE4"]        = $receptData["AVARAGE4"];
        $arg["data"]["TOTAL_RANK4"]     = $receptData["TOTAL_RANK4"];
        $arg["data"]["JUDGE_DEVIATION"] = $receptData["JUDGE_DEVIATION"];
        
        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        global $sess;
        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL590F/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
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
        View::toHTML($model, "knjl590fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}?>
