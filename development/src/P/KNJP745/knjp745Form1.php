<?php

require_once('for_php7.php');

class knjp745Form1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp745index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["valWindowHeight"]  = $model->windowHeight - 150;
        $resizeFlg = ($model->cmd == "cmdStart" || $model->cmd == "search") ? true : false;

        //入金月コンボ
        if (!($model->cmd == "search" || $model->cmd == "chengeDiv")) {
            $cmbDis = false;
        } else {
            $cmbDis = true;
        }
        $disAbled = (!$cmbDis) ? " disabled": "";
        $opt = array();
        foreach ($model->monthArray as $month) {
            $opt[] = array('label' => $month, 'value' => $month);
        }
        $extra = "onchange=\"return btn_submit('chengeDiv');\"".$disAbled;
        list($ctrlyear, $setCtrlMonth, $ctrlday) = explode("-", CTRL_DATE);
        $model->field["PAID_LIMIT_MONTH"] = ($model->field["PAID_LIMIT_MONTH"]) ? $model->field["PAID_LIMIT_MONTH"]: $setCtrlMonth;
        $arg["PAID_LIMIT_MONTH"] = knjCreateCombo($objForm, "PAID_LIMIT_MONTH", $model->field["PAID_LIMIT_MONTH"], $opt, $extra, 1);

        //radio(1:年組番号, 2:学籍番号)
        $opt = array(1, 2);
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "1" : $model->field["SORT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORT_DIV{$val}\" onClick=\"btn_submit('chengeDiv')\"".$disAbled);
        }
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        //年組表示追加
        if ($model->field["SORT_DIV"] == "1") {
            $arg["HR_CLASS_HYOUJI"] = "1";
        } else {
            $arg["NOT_HR_CLASS_HYOUJI"] = "1";
        }

        //全チェックcheckbox
        $extra = " id=\"ALL_CHECK\" onclick=\"allCheck(this)\" ";
        $arg["ALL_CHECK"] = knjCreateCheckBox($objForm, "ALL_CHECK", "1", $extra);

        //生徒データ表示
        $sendSchregNo = array();
        if ($model->cmd == "search" || $model->cmd == "chengeDiv") {
            $query = knjp745Query::getStudentInfoData($model);

            $model->arr_schregData = array();
            $bifSlipNo = $bifSchregNo = "";
            $hasData = false;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($bifSchregNo !== $row["SCHREGNO"]) {
                    $cnt = $db->getOne(knjp745Query::getStudentInfoData($model, "cnt1", $row["SCHOOL_KIND"], $row["SCHREGNO"]));
                    $row["ROWSPAN1"] = $cnt > 0 ? $cnt : 1;
                }
                $bifSchregNo = $row["SCHREGNO"];

                if ($bifSlipNo !== $row["SLIP_NO"]) {
                    $cnt = $db->getOne(knjp745Query::getStudentInfoData($model, "cnt2", $row["SCHOOL_KIND"], $row["SCHREGNO"], $row["SLIP_NO"]));
                    $row["ROWSPAN2"] = $cnt > 0 ? $cnt : 1;
                }
                $bifSlipNo = $row["SLIP_NO"];

                $sendSchregNo[$row["SCHREGNO"]] = $row["SCHREGNO"];

                //更新用
                $model->arr_schregData[] = $row["SCHOOL_KIND"]."-".$row["SCHREGNO"]."-".$row["SLIP_NO"]."-".$row["PLAN_PAID_MONEY_DATE"];

                //チェックボックス
                $setName = "DEL_FLG-".$row["SCHOOL_KIND"]."-".$row["SCHREGNO"]."-".$row["SLIP_NO"]."-".$row["PLAN_PAID_MONEY_DATE"];
                $extra = "id=\"{$setName}\" class=\"changeColor\" data-name=\"{$setName}\" ";
                $row["DEL_FLG"] = knjCreateCheckBox($objForm, $setName, "1", $extra, "");
                $row["DEL_FLG_NAME"] = $setName;

                $row["PLAN_PAID_MONEY_DATE"] = strtr($row["PLAN_PAID_MONEY_DATE"], "-", "/");

                //金額カンマ区切り
                $row["PAID_MONEY"] = number_format($row["PAID_MONEY"]);

                $arg["data"][] = $row;
                $hasData = true;
            }
            $result->free();
        }

        /**********/
        /* ボタン */
        /**********/
        //印刷
        $disUpd = ($hasData) ? "": " disabled";
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra.$disUpd);
        //更新
        $disUpd = ($hasData) ? "": " disabled";
        $extra = $disabled."onclick=\"return btn_submit('update');\"".$disUpd;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP745");
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "schFlg", $model->schFlg);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "schoolKind", $model->schoolKind);
        $setSchregNos = "";
        $sep = "";
        foreach ($sendSchregNo as $key => $val) {
            $setSchregNos .= $sep.$val;
            $sep = ":";
        }
        knjCreateHidden($objForm, "printSchreg", $setSchregNos);
        if (is_array($model->search)) {
            foreach ($model->search as $key => $val) {
                $hiddenName = "P_".$key;
                knjCreateHidden($objForm, $hiddenName, $val);
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjp745Form1.html", $arg);
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
