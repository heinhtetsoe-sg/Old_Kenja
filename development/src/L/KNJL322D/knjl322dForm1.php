<?php

require_once('for_php7.php');

class knjl322dForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl322dForm1", "POST", "knjl322dindex.php", "", "knjl322dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear."年度";

        //受験種別
        $query = knjl322dQuery::getNameMst($model->examyear, "L004");
        $extra = "onchange=\"return btn_submit('changeTest')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1);

        //通知種別
        $opt = array();
        $opt[] = array('label' => "1:合否通知書・類型未定通知書",'value' => "1");
        $opt[] = array('label' => "2:類型決定通知書",'value' => "2");
        $extra = "onchange=\"return btn_submit('knjl322d')\"";
        $model->noticeType = ($model->noticeType) ? $model->noticeType: "1";
        $arg["data"]["NOTICE_TYPE"] = knjCreateCombo($objForm, "NOTICE_TYPE", $model->noticeType, $opt, $extra, 1);

        //出力対象
        $query = knjl322dQuery::getFinSchool($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->outputDiv, "OUTPUT_DIV", $extra, 1);

        //通知書発行日
        $defNoDate = substr_replace($db->getOne(knjl322dQuery::getNameMstSpare($model, "1")), $model->examyear, 0, 4);  //名称マスタで設定した日付の年度は入試年度に置き換える(2020/03/11修正)
        $model->noticeDate = ($model->noticeDate) ? $model->noticeDate: str_replace("-", "/", $defNoDate);
        if ($model->cmd == "changeTest") {
            $model->noticeDate = str_replace("-", "/", $defNoDate);
        }
        $arg["data"]["NOTICE_DATE"] = View::popUpCalendarAlp($objForm, "NOTICE_DATE", $model->noticeDate, "", "");

        //類型の決定日
        $defDeDate = substr_replace($db->getOne(knjl322dQuery::getNameMstSpare($model, "4")), $model->examyear, 0, 4);
        $model->decisionDate = ($model->decisionDate) ? $model->decisionDate: str_replace("-", "/", $defDeDate);
        if ($model->cmd == "changeTest") {
            $model->decisionDate = str_replace("-", "/", $defDeDate);
        }
        $disUndeciFlg = "";
        $arg["data"]["DECISION_DATE"] = View::popUpCalendarAlp($objForm, "DECISION_DATE", $model->decisionDate, $disUndeciFlg, "");

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL322D");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl322dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($name == "OUTPUT_DIV") $opt[] = array("label" => "-- 全て --", "value" => "999999999999");
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
