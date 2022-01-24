<?php

require_once('for_php7.php');

class knjp805Form1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp805index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["valWindowHeight"]  = $model->windowHeight - 170;
        $resizeFlg = ($model->cmd == "cmdStart" || $model->cmd == "chengeDiv") ? true : false;

        //納期限月コンボ
        $opt = array();
        foreach ($model->monthArray as $month) {
            $opt[] = array('label' => $month, 'value' => $month);
        }
        $extra = "onchange=\"return btn_submit('chengeDiv');\"";
        list($ctrlyear, $setCtrlMonth, $ctrlday) = explode("-", CTRL_DATE);
        $model->field["PAID_LIMIT_MONTH"] = ($model->field["PAID_LIMIT_MONTH"]) ? $model->field["PAID_LIMIT_MONTH"]: $setCtrlMonth;
        $arg["PAID_LIMIT_MONTH"] = knjCreateCombo($objForm, "PAID_LIMIT_MONTH", $model->field["PAID_LIMIT_MONTH"], $opt, $extra, 1);

        //事務担当コンボ
        $query = knjp805Query::getStaffMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "REMINDER_STAFFCD", $model->field["REMINDER_STAFFCD"], $extra, 1, "BLANK");

        //学年コンボ
        $query = knjp805Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('chengeDiv');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "BLANK");

        //年組コンボ
        $query = knjp805Query::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('chengeDiv');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "BLANK");

        //帳票に渡す用
        if (!isset($model->warning) && ($model->cmd == 'read' || $model->cmd == 'readTest')) {
            $model->setSlipNo = $comma = "";
            foreach ($model->arr_schregData as $key => $val) {
                if ($model->field["GO_PRINT:".$val]) {
                    $model->setSlipNo .= $comma.$model->field["GO_PRINT:".$val];
                    $comma = ",";
                }
            }
        }
        $arg["TOP"]["PRINT_SLIP_NO"]  = knjCreateHidden($objForm, "PRINT_SLIP_NO", $model->setSlipNo);

        //checkbox
        $extra = " id=\"ALL_PRINT\" onclick=\"allCheck(this, 'printCheck');\" ";
        $arg["ALL_PRINT"] = knjCreateCheckBox($objForm, "ALL_PRINT", "1", $extra);

        //checkbox
        $extra = " id=\"ALL_COUNT\" onclick=\"allCheck(this, 'countCheck');\" ";
        $arg["ALL_COUNT"] = knjCreateCheckBox($objForm, "ALL_COUNT", "1", $extra);

        //文面コンボ
        $opt = array();
        $query = knjp805Query::getDocumentMst();
        $result = $db->query($query);
        while ($rowD = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $rowD["LABEL"], 'value' => $rowD["VALUE"]);
        }
        $extra = "";
        $arg["DEFAULT_DOCUMENT"] = knjCreateCombo($objForm, "DEFAULT_DOCUMENT", $model->field["DEFAULT_DOCUMENT"], $opt, $extra, 1);
        //文面反映
        $extra = "onclick=\"return setDocument();\"";
        $arg["button"]["btn_documentSet"] = knjCreateBtn($objForm, "btn_documentSet", "反映", $extra);

        //生徒データ表示
        $query = knjp805Query::getStudentInfoData($model);

        $model->arr_schregData = array();
        $bifKey = "";
        $hasData = false;
        $result = $db->query($query);
        if ($model->field["GRADE"] != '') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //更新用
                $model->arr_schregData[] = $row["SCHOOL_KIND"]."-".$row["SCHREGNO"]."-".$row["SLIP_NO"];
                //IDセット
                $setId = $row["SCHOOL_KIND"]."-".$row["SCHREGNO"]."-".$row["SLIP_NO"];

                //印刷チェックボックス
                $row["GO_PRINT"] = knjCreateCheckBox($objForm, "GO_PRINT:".$setId, $row["SLIP_NO"], " class=\"printCheck\" ");

                //カウントUPしないチェックボックス
                $row["NO_COUNT_UP"] = knjCreateCheckBox($objForm, "NO_COUNT_UP-".$setId, "1", " class=\"countCheck\" ", "");

                //回数（更新で使用）
                knjCreateHidden($objForm, "REMINDER_COUNT-".$setId, $row["REMINDER_COUNT"]);

                //文面コンボ
                $opt = array();
                $query2 = knjp805Query::getDocumentMst();
                $result2 = $db->query($query2);
                $opt[] = array('label' => "", 'value' => "");
                while ($rowD = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array('label' => $rowD["LABEL"], 'value' => $rowD["VALUE"]);
                }
                $extra = "";
                $row["DOCUMENTCD"] = knjCreateCombo($objForm, "DOCUMENTCD-".$setId, $model->field["DOCUMENTCD-".$setId], $opt, $extra, 1);

                //金額（更新用）
                knjCreateHidden($objForm, "REMINDER_MONEY-".$setId, $row["REMINDER_MONEY"]);
                //金額カンマ区切り
                $row["REMINDER_MONEY"] = number_format($row["REMINDER_MONEY"]);

                $arg["data"][] = $row;
                $hasData = true;
            }
        }
        $result->free();

        //date
        $arg["PAID_DATE1"] = View::popUpCalendar($objForm, "PAID_DATE1", str_replace("-", "/", $model->field["PAID_DATE1"]));
        $arg["TOP"]["SEND_PAID_DATE1"] = knjCreateHidden($objForm, "SEND_PAID_DATE1", str_replace("-", "/", $model->field["PAID_DATE1"]));

        //date
        $arg["PAID_DATE2"] = View::popUpCalendar($objForm, "PAID_DATE2", str_replace("-", "/", $model->field["PAID_DATE2"]));
        $arg["TOP"]["SEND_PAID_DATE2"] = knjCreateHidden($objForm, "SEND_PAID_DATE2", str_replace("-", "/", $model->field["PAID_DATE2"]));

        //ボタン作成
        //テスト印刷
        $extra = "onclick=\"return btn_submit('updateTest');\"";
        $arg["button"]["btn_testPrint"] = knjCreateBtn($objForm, "btn_testPrint", "テスト印刷", $extra);
        //印刷/更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷／更新", $extra);
        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");
        $arg["TOP"]["CTRL_YEAR"]       = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        $arg["TOP"]["CTRL_SEMESTER"]   = knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        $arg["TOP"]["CTRL_DATE"]       = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        $arg["TOP"]["DBNAME"]          = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        $arg["TOP"]["PRGID"]           = knjCreateHidden($objForm, "PRGID", "KNJP805");
        $arg["TOP"]["SCHOOLCD"]        = knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        $arg["TOP"]["SENDCMD"]         = knjCreateHidden($objForm, "SENDCMD");
        $arg["TOP"]["useFormNameP805"] = knjCreateHidden($objForm, "useFormNameP805", $model->Properties["useFormNameP805"]);
        $arg["TOP"]["documentMstSchregnoFlg"] = knjCreateHidden($objForm, "documentMstSchregnoFlg", $model->Properties["documentMstSchregnoFlg"]);

        if ($model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLmonth = "01";
        } else {
            $setLmonth = sprintf("%02d", ($model->field["PAID_LIMIT_MONTH"] + 1));
        }
        if ($model->field["PAID_LIMIT_MONTH"] < "04" || $model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLimitYear = CTRL_YEAR + 1;
        } else {
            $setLimitYear = CTRL_YEAR;
        }
        $setLimitDate = $setLimitYear."-".$setLmonth."-01";
        $arg["TOP"]["LIMIT_DATE"]      = knjCreateHidden($objForm, "LIMIT_DATE", $setLimitDate);

        //印刷
        if (!isset($model->warning) && ($model->cmd == 'read' || $model->cmd == 'readTest')) {
            $arg["printgo"] = "newwin('" . SERVLET_URL . "', '{$model->cmd}')";
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML5($model, "knjp805Form1.html", $arg);
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
