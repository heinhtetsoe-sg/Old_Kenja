<?php
class knjl040vForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->objYear;

        //校種コンボ
        $extra = "onchange=\"return btn_submit('main')\"";
        $query = knjl040vQuery::getSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->examSchoolKind, $extra, 1);

        //試験IDコンボ
        $query = knjl040vQuery::getExamId($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAM_ID", $model->examId, $extra, 1, "");
        $model->wkExamId = ($model->examId != "") ? explode("-", $model->examId) : array();

        //一覧表示
        $model->keyArray = $model->examnoArray = array();
        $model->depositCheckArray = $model->depositDateArray = array();
        $model->feeCheckArray = $model->feeDateArray = array();
        $model->declineCheckArray = $model->declineDateArray = array();
        $query = knjl040vQuery::selectQuery($model);
        $result = $db->query($query);
        $count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["EXAMNO"];

            //取得した内容の編集
            $row["DEPOSIT_DATE"] = str_replace("-", "/", $row["DEPOSIT_DATE"]);
            $row["FEE_DATE"] = str_replace("-", "/", $row["FEE_DATE"]);
            $row["DECLINE_DATE"] = str_replace("-", "/", $row["DECLINE_DATE"]);

            //入力制御
            $disable = ($row["DECLINE_CHECK"] == "1") ? " disabled " : "";
            $depositDatedisable = ($row["DEPOSIT_CHECK"] != "1" || $row["DECLINE_CHECK"] == "1") ? " disabled " : "";
            $feeDatedisable = ($row["FEE_CHECK"] != "1" || $row["DECLINE_CHECK"] == "1") ? " disabled " : "";
            $declineDatedisable = ($row["DECLINE_CHECK"] != "1") ? " disabled " : "";

            //入学手続き金 チェックボックス
            $extra  = " id=\"DEPOSIT_CHECK_{$key}\" onClick=\"return checkExce(this, {$key});\" ";
            $extra .= ($row["DEPOSIT_CHECK"] == "1") ? "checked".$disable : "".$disable;
            $row["DEPOSIT_CHECK"] = knjCreateCheckBox($objForm, "DEPOSIT_CHECK_{$key}", "1", $extra, "");

            //入学手続き金 日付
            $extra = "\" id=\"DEPOSIT_DATE_{$key}\" ";
            $row["DEPOSIT_DATE"] = View::popUpCalendar2($objForm, "DEPOSIT_DATE_{$key}", $row["DEPOSIT_DATE"], '', $extra, $depositDatedisable);

            //入学金 チェックボックス
            $extra  = " id=\"FEE_CHECK_{$key}\" onClick=\"return checkExce(this, {$key});\" ";
            $extra .= ($row["FEE_CHECK"] == "1") ? "checked".$disable : "".$disable;
            $row["FEE_CHECK"] = knjCreateCheckBox($objForm, "FEE_CHECK_{$key}", "1", $extra, "");

            //入学金 日付
            $extra = "\" id=\"FEE_DATE_{$key}\" ";
            $row["FEE_DATE"] = View::popUpCalendar2($objForm, "FEE_DATE_{$key}", $row["FEE_DATE"], '', $extra, $feeDatedisable);

            //辞退 チェックボックス
            $extra  = "  id=\"DECLINE_CHECK_{$key}\" onClick=\"return checkExce(this, {$key});\" ";
            $extra .= ($row["DECLINE_CHECK"] == "1") ? "checked" : "";
            $row["DECLINE_CHECK"] = knjCreateCheckBox($objForm, "DECLINE_CHECK_{$key}", "1", $extra, "");

            //辞退 日付
            $extra = "\" id=\"DECLINE_DATE_{$key}\" ";
            $row["DECLINE_DATE"] = View::popUpCalendar2($objForm, "DECLINE_DATE_{$key}", $row["DECLINE_DATE"], '', $extra, $declineDatedisable);

            //受験者
            knjCreateHidden($objForm, "EXAMNO_{$key}", $row["EXAMNO"]);

            $arg["data"][] = $row;

            $model->keyArray[] = $key;
            $count++;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $count);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl040vindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl040vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $retDiv = "")
{
    $opt = array();
    $retOpt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        $retOpt[$row["VALUE"]] = $row["LABEL"];

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $retOpt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $count)
{
    $disable  = ($count > 0) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    if (count($model->keyArray) > 0) {
        knjCreateHidden($objForm, "HID_KEY", implode(",", $model->keyArray));
    } else {
        knjCreateHidden($objForm, "HID_KEY", array());
    }
    knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
}
