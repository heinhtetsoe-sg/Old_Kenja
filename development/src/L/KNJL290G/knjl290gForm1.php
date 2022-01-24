<?php

require_once('for_php7.php');

class knjl290gForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm      = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl290gindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $Row = $db->getRow(knjl290gQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl290gQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
        }

        if ($model->cmd == 'change_testdiv' || $model->cmd == 'main') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            $Row["TESTDIV"]      = $model->field["TESTDIV"];
        }

        //データが無ければ更新ボタン等を無効
        if (!is_array($Row) && $model->cmd == 'reference') {
            $model->setWarning("MSG303");
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning))) {
            $Row =& $model->field;
        }

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $query = knjl290gQuery::get_name_cd($model->year, "L003", "2");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $Row["APPLICANTDIV"], $extra, 1, "");

        //入試区分コンボ
        $query = knjl290gQuery::get_name_cd($model->year, "L004");
        $extra = " onChange=\"btn_submit('change_testdiv')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $Row["TESTDIV"], $extra, 1, "");

        //受験番号
        $extra = "onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 8, 8, $extra);

        //合否コンボ
        $query = knjl290gQuery::get_name_cd($model->year, "L013");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "JUDGEMENT", $Row["JUDGEMENT"], $extra, 1, "BLANK");

        //合格判定
        $passFlg = $db->getRow(knjl290gQuery::get_name_cdJ($model->year, "L013", $Row["JUDGEMENT"]), DB_FETCHMODE_ASSOC);

        //氏名(判定で合格ならを赤、その他黒)
        if ($passFlg["NAMESPARE1"] == '1'){
            $setColor = "red";
            $arg["data"]["NAME"] = "<font color=\"{$setColor}\">".htmlspecialchars($Row["NAME"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

        //かな
        $arg["data"]["NAME_KANA"]       = htmlspecialchars($Row["NAME_KANA"]);
        //性別
        $arg["data"]["SEX"]             = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
        //生年月日
        $arg["data"]["BIRTHDAY"]        = $Row["BIRTHDAY"]? str_replace("-","/",$Row["BIRTHDAY"]) : "";
        //専併区分
        $arg["data"]["SHDIV"]           = $Row["SHDIV"] ? $Row["SHDIV"]."：".$Row["SHDIVNAME"] : "";
        //第１志望
        $arg["data"]["EXAMCOURSE"]      = $Row["EXAMCOURSE"] ? $Row["EXAMCOURSE"].":".$Row["EXAMCOURSE_NAME"] : "";
        //第２志望
        $arg["data"]["EXAMCOURSE2"]     = $Row["EXAMCOURSE2"] ? $Row["EXAMCOURSE2"].":".$Row["EXAMCOURSE_NAME2"] : "";
        //特待生情報
        $arg["data"]["JUDGE_KIND_NAME"] = $Row["JUDGE_KIND_NAME"];

        //合格コースコンボ
        $query = knjl290gQuery::getSucCourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "COURSEMAJOR", $Row["COURSEMAJOR"], $extra, 1, "BLANK");

        //入学コースコンボ
        $query = knjl290gQuery::getCourseMajorCoursecode($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "ENTER_COURSEMAJOR", $Row["ENTER_COURSEMAJOR"], $extra, 1, "BLANK");

        //手続日
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", str_replace("-", "/", $Row["PROCEDUREDATE"]), "", "", $disabled_date);

        //手続区分コンボ
        $query = knjl290gQuery::get_name_cd($model->year, "L011");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "PROCEDUREDIV", $Row["PROCEDUREDIV"], $extra, 1, "BLANK");

        //入学区分コンボ
        $query = knjl290gQuery::get_name_cd($model->year, "L012");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "ENTDIV", $Row["ENTDIV"], $extra, 1, "BLANK");

        //入学支度金貸付チェック
        $extra = "";
        $extra .= $Row["ENTRYPAY_LOAN"] == "1" ? " checked " : "";
        $arg["data"]["ENTRYPAY_LOAN"] = knjCreateCheckBox($objForm, "ENTRYPAY_LOAN", "1", $extra);

        $btnDis = ($model->examno == '') ? ' disabled': '';

        //ボタン作成
        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        global $sess;
        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL290G/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "onClick=\"btn_submit('back1');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "onClick=\"btn_submit('next1');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$btnDis);

        //更新ボタン(更新後前の志願者)
        $extra = "style=\"width:150px\" onclick=\"return btn_submit('back2');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra.$btnDis);

        //更新ボタン(更新後次の志願者)
        $extra = "style=\"width:150px\" onclick=\"return btn_submit('next2');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra.$btnDis);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"OnClosing();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        $arg["IFRAME"] = View::setIframeJs();

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl290gForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>