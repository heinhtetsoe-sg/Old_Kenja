<?php

require_once('for_php7.php');

class knjh701form2
{
    public function main(&$model)
    {
        //取消ボタン押下時
        if ($model->cmd == "reset") {
            $model->field["TESTID"]    = null;
            $model->field["TESTDIV"]   = "";
            $model->field["EXAM_DATE"] = "";
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh701index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)   &&
            ($model->cmd != "change") &&
            ($model->testdiv != "")   &&
            ($model->testid != "")    &&
            ($model->date != "")) {
            $result = $db->query(knjh701Query::getAcademicTestMst(CTRL_YEAR, $model->testdiv, $model->testid, $model->date));
            while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = $rowTemp;
            }
        } else {
            $row =& $model->field;
        }

        //テスト区分
        $result = $db->query(knjh701Query::getNameMst($model, "H320"));
        $opt    = array();
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $rowTemp["LABEL"],
                           "value" => $rowTemp["VALUE"]);
        }
        $result->free();
        $extra = "";
        if ($row["TESTDIV"] == "") {
            $row["TESTDIV"] = $model->field["TESTDIV"];
        }
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $row["TESTDIV"], $opt, $extra, 1);

        //テストID
        $extra = "onblur=\"numCheck(this);\"";
        if ($row["TESTID"] != "") {
            $testId = sprintf("%02d", substr($row["TESTID"], 1, 2));
        } else {
            $testId = "";
        }
        if ($model->field["TESTID"] !== null) {
            $testId = sprintf("%02d", $model->field["TESTID"]);
        }
        $arg["data"]["TESTID"] = knjCreateTextBox($objForm, $testId, "TESTID", 2, 2, $extra);

        //実施日付
        $row["EXAM_DATE"] = str_replace("-", "/", $row["EXAM_DATE"]);
        if ($row["EXAM_DATE"] == "") {
            $row["EXAM_DATE"] = $model->field["EXAM_DATE"];
        }
        $arg["data"]["EXAM_DATE"] = View::popUpCalendar2($objForm, "EXAM_DATE", $row["EXAM_DATE"], "", "", $entMove);

        //テスト名称
        $extra = "";
        if ($row["TESTNAME"] == "") {
            $row["TESTNAME"] = $model->field["TESTNAME"];
        }
        $arg["data"]["TESTNAME"] = knjCreateTextBox($objForm, $row["TESTNAME"], "TESTNAME", 45, 40, $extra);

        //テスト略称
        $extra = "";
        if ($row["TESTNAMEABBV"] == "") {
            $row["TESTNAMEABBV"] = $model->field["TESTNAMEABBV"];
        }
        $arg["data"]["TESTNAMEABBV"] = knjCreateTextBox($objForm, $row["TESTNAMEABBV"], "TESTNAMEABBV", 25, 20, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" &&
            $model->cmd != "reset"     &&
            $model->cmd != "change") {
            if (!isset($model->warning)) {
                $arg["reload"] = "parent.left_frame.location.href='knjh701index.php?cmd=list';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh701Form2.html", $arg);
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
