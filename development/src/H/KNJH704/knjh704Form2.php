<?php

require_once('for_php7.php');

class knjh704form2
{
    public function main(&$model)
    {

        //取消ボタン押下時
        if ($model->cmd == "reset") {
            $model->field["TESTID"]       = null;
            $model->field["PERIODID"]     = null;
            $model->field["START_HOUR"]   = null;
            $model->field["START_MINUTE"] = null;
            $model->field["END_HOUR"]     = null;
            $model->field["END_MINUTE"]   = null;
            $model->field["SUBCLASSCD"]   = null;
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh704index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)           &&
            ($model->cmd         != "change") &&
            ($model->testId      != "")       &&
            ($model->periodid    != "")       &&
            ($model->startHour   != "")       &&
            ($model->startMinute != "")       &&
            ($model->endHour     != "")       &&
            ($model->endMinute   != "")       &&
            ($model->subclasscd  != "")) {
            $result = $db->query(knjh704Query::getTestNitteiData($model));
            while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = $rowTemp;
            }
        } else {
            $row =& $model->field;
        }

        //学力テストコンボボックス
        $firstTestId = "";
        $opt = array();
        $query = knjh704Query::getTestName($model);
        $result = $db->query($query);
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $rowTemp["LABEL"],
                           'value' => $rowTemp["VALUE"]);
            if ($firstTestId == "") {
                //コンボボックスの一番上の値を保持
                $firstTestId = $rowTemp["VALUE"];
            }
        }
        $extra = "onchange=\"return btn_submit('change_test');\"";
        if ($model->field["TESTID"] !== null) {
            $row["TESTID"] = $model->field["TESTID"];
        } else {
            $model->field["TESTID"] = $firstTestId;
        }
        $arg["data"]["TESTID"] = knjCreateCombo($objForm, "TESTID", $row["TESTID"], $opt, $extra, 1);

        //時限コンボボックス
        $opt = array();
        $query = knjh704Query::getPeriod($model, "H321");
        $result = $db->query($query);
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $rowTemp["LABEL"],
                           "value" => $rowTemp["VALUE"]);
        }
        $extra = "";
        if ($model->field["PERIODID"] !== null) {
            $row["PERIODID"] = $model->field["PERIODID"];
        }
        $arg["data"]["PERIODID"] = knjCreateCombo($objForm, "PERIODID", $row["PERIODID"], $opt, $extra, 1);

        //開始・終了時間(時間)コンボボックス
        $opt = array();
        for ($i = 8; $i <= 20; $i++) {
            $opt[] = array("label" => sprintf("%02d", $i),"value" => sprintf("%02d", $i));
        }
        $extra = "";
        if ($model->field["START_HOUR"] !== null) {
            $row["START_HOUR"] = $model->field["START_HOUR"];
        }
        if ($model->field["END_HOUR"] !== null) {
            $row["END_HOUR"] = $model->field["END_HOUR"];
        }
        $arg["data"]["START_HOUR"] = knjCreateCombo($objForm, "START_HOUR", $row["START_HOUR"], $opt, $extra, 1);
        $arg["data"]["END_HOUR"] = knjCreateCombo($objForm, "END_HOUR", $row["END_HOUR"], $opt, $extra, 1);

        //開始・終了時間(分)コンボボックス
        $opt = array();
        for ($i = 0; $i <= 55; $i++) {
            if ($i % 5 == 0) {
                $opt[] = array("label" => sprintf("%02d", $i),"value" => sprintf("%02d", $i));
            }
        }
        $extra = "";
        if ($model->field["START_MINUTE"] !== null) {
            $row["START_MINUTE"] = $model->field["START_MINUTE"];
        }
        if ($model->field["END_MINUTE"] !== null) {
            $row["END_MINUTE"] = $model->field["END_MINUTE"];
        }
        $arg["data"]["START_MINUTE"] = knjCreateCombo($objForm, "START_MINUTE", $row["START_MINUTE"], $opt, $extra, 1);
        $arg["data"]["END_MINUTE"] = knjCreateCombo($objForm, "END_MINUTE", $row["END_MINUTE"], $opt, $extra, 1);

        //科目コンボボックス
        $opt = array();
        $query = knjh704Query::getSubClassName($model);
        $result = $db->query($query);
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $rowTemp["SUBCLASSNAME"],
                           "value" => $rowTemp["SUBCLASSCD"]);
        }
        $extra = "";
        if ($model->field["SUBCLASSCD"] !== null) {
            $row["SUBCLASSCD"] = $model->field["SUBCLASSCD"];
        }
        $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $row["SUBCLASSCD"], $opt, $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "reset" && $model->cmd != "change") {
            if (!isset($model->warning)) {
                $arg["reload"] = "parent.left_frame.location.href='knjh704index.php?cmd=list&TESTID={$model->field["TESTID"]}';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh704Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
