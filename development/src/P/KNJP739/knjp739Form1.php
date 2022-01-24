<?php

require_once('for_php7.php');

class knjp739Form1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp739index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //納期限月コンボ
        $opt = array();
        foreach ($model->monthArray as $month) {
            $opt[] = array('label' => $month, 'value' => $month);
        }
        $extra = "onchange=\"return btn_submit('chengeDiv');\"";
        list($ctrlyear, $setCtrlMonth, $ctrlday) = explode("-", CTRL_DATE);
        $model->field["PAID_LIMIT_MONTH"] = ($model->field["PAID_LIMIT_MONTH"]) ? $model->field["PAID_LIMIT_MONTH"]: $setCtrlMonth;
        $arg["PAID_LIMIT_MONTH"] = knjCreateCombo($objForm, "PAID_LIMIT_MONTH", $model->field["PAID_LIMIT_MONTH"], $opt, $extra, 1);

        //radio(1:年組番号, 2:学籍番号)
        $opt = array(1, 2);
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "1" : $model->field["SORT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORT_DIV{$val}\" onClick=\"btn_submit('chengeDiv')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        //年組表示追加
        if ($model->field["SORT_DIV"] == "1") {
            $arg["HR_CLASS_HYOUJI"] = "1";
        } else {
            $arg["NOT_HR_CLASS_HYOUJI"] = "1";
        }

        //入金日
        $model->field["PAID_DATE"] = ($model->field["PAID_DATE"]) ? $model->field["PAID_DATE"]: strtr(CTRL_DATE, "-", "/");
        $arg["PAID_DATE"] = View::popUpCalendar($objForm, "PAID_DATE", $model->field["PAID_DATE"]);

        //入金方法コンボ
        $query = knjp739Query::getNameMst("P004");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PLAN_PAID_MONEY_DIV", $model->field["PLAN_PAID_MONEY_DIV"], $extra, 1, "BLANK");

        //生徒データ表示
        if ($model->cmd == "search" || $model->cmd == "chengeDiv") {
            $query = knjp739Query::getStudentInfoData($model);

            $model->arr_schregData = array();
            $bifKey = "";
            $hasData = false;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($bifKey !== $row["SLIP_NO"]) {
                    $cnt = $db->getOne(knjp739Query::getSlipNoCnt($model, $row["SCHOOL_KIND"], $row["SCHREGNO"], $row["SLIP_NO"]));
                    $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
                }
                $bifKey = $row["SLIP_NO"];

                //更新用
                $model->arr_schregData[] = $row["SCHOOL_KIND"]."-".$row["SCHREGNO"]."-".$row["SLIP_NO"]."-".$row["PAID_LIMIT_DATE"];

                //チェックボックス
                $setName = "PAID_FLG-".$row["SCHOOL_KIND"]."-".$row["SCHREGNO"]."-".$row["SLIP_NO"]."-".$row["PAID_LIMIT_DATE"];
                $row["PAID_FLG"] = knjCreateCheckBox($objForm, $setName, "1", $extra, "");

                $row["PAID_LIMIT_DATE"] = strtr($row["PAID_LIMIT_DATE"], "-", "/");

                //金額カンマ区切り,
                $row["PLAN_MONEY"] = number_format($row["PLAN_MONEY"]);

                $arg["data"][] = $row;
                $hasData = true;
            }
            $result->free();
        }

        //ボタン作成
        $disUpd = ($hasData) ? "": " disabled";
        $extra = $disabled."onclick=\"return btn_submit('update');\"".$disUpd;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjp739Form1.html", $arg);
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
