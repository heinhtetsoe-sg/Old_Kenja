<?php

require_once('for_php7.php');

class knjh705form2
{
    public function main(&$model)
    {
        //取消
        if ($model->cmd == "reset") {
            $model->field["TESTID"]   = "";
            $model->field["PERIODID"] = "";
            $model->field["FACCD"]    = "";
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh705index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)        &&
            ($model->cmd      != "change") &&
            ($model->testId   != "")       &&
            ($model->periodid != "")       &&
            ($model->faccd    != "")       &&
            ($model->staffcd1 != "")) {
            $result = $db->query(knjh705Query::getSikenkantokuData($model));
            while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = $rowTemp;
            }
        }

        //学力テストコンボボックス
        $opt = array();
        $query = knjh705Query::getTestName($model);
        $result = $db->query($query);
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $rowTemp["LABEL"], 'value' => $rowTemp["VALUE"]);
        }
        $extra = "";
        if ($row["TESTID"] == "") {
            $row["TESTID"] = $model->field["TESTID"];
        }
        $arg["data"]["TESTID"] = knjCreateCombo($objForm, "TESTID", $row["TESTID"], $opt, $extra, 1);

        //時限コンボボックス
        $opt = array();
        $query = knjh705Query::getPeriod($model, "H321");
        $result = $db->query($query);
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $rowTemp["LABEL"],
                           "value" => $rowTemp["VALUE"]);
        }
        $extra = "";
        if ($row["PERIODID"] == "") {
            $row["PERIODID"] = $model->field["PERIODID"];
        }
        $arg["data"]["PERIODID"] = knjCreateCombo($objForm, "PERIODID", $row["PERIODID"], $opt, $extra, 1);

        //教室コンボボックス
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $query = knjh705Query::getFacilityName();
        $result = $db->query($query);
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $rowTemp["LABEL"],
                           "value" => $rowTemp["VALUE"]);
        }
        $extra = "";
        if ($row["FACCD"] == "") {
            $row["FACCD"] = $model->field["FACCD"];
        }
        $arg["data"]["FACILITYNAME"] = knjCreateCombo($objForm, "FACILITYNAME", $row["FACCD"], $opt, $extra, 1);

        //iframe.jsを取得
        $arg["setIframeJs"] = View::setIframeJs();

        //監督1
        $extra = "style=\"background-color:darkgray;\"readonly";
        if ($row["STAFFCD1"] == "") {
            $row["STAFFCD1"]   = $model->field["STAFFCD1"];
            $row["STAFFNAME1"] = $model->field["STAFFNAME1"];
        }
        $arg["data"]["STAFFNAME1"] = knjCreateTextBox($objForm, $row["STAFFNAME1"], "STAFFNAME1", 20, 20, $extra);
        //監督選択ボタン1
        $extra = " onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSELECT_STAFF/knjxselect_staffindex.php?TEXT_CD=STAFFCD1&TEXT_NAME=STAFFNAME1&cmd=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 450)\"";
        $arg["button"]["SELECTSTAFF1"] = knjCreateBtn($objForm, "SELECTSTAFF1", "教職員選択", $extra);

        //監督2
        $extra = "style=\"background-color:darkgray;\"readonly";
        if ($row["STAFFCD2"] == "") {
            $row["STAFFCD2"]   = $model->field["STAFFCD2"];
            $row["STAFFNAME2"] = $model->field["STAFFNAME2"];
        }
        $arg["data"]["STAFFNAME2"] = knjCreateTextBox($objForm, $row["STAFFNAME2"], "STAFFNAME2", 20, 20, $extra);
        //監督選択ボタン2
        $extra = " onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSELECT_STAFF/knjxselect_staffindex.php?TEXT_CD=STAFFCD2&TEXT_NAME=STAFFNAME2&cmd=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 450)\"";
        $arg["button"]["SELECTSTAFF2"] = knjCreateBtn($objForm, "SELECTSTAFF2", "教職員選択", $extra);

        //監督3
        $extra = "style=\"background-color:darkgray;\"readonly";
        if ($row["STAFFCD3"] == "") {
            $row["STAFFCD3"]   = $model->field["STAFFCD3"];
            $row["STAFFNAME3"] = $model->field["STAFFNAME3"];
        }
        $arg["data"]["STAFFNAME3"] = knjCreateTextBox($objForm, $row["STAFFNAME3"], "STAFFNAME3", 20, 20, $extra);
        //監督選択ボタン3
        $extra = " onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSELECT_STAFF/knjxselect_staffindex.php?TEXT_CD=STAFFCD3&TEXT_NAME=STAFFNAME3&cmd=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 450)\"";
        $arg["button"]["SELECTSTAFF3"] = knjCreateBtn($objForm, "SELECTSTAFF3", "教職員選択", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "STAFFCD1", $row["STAFFCD1"]);
        knjCreateHidden($objForm, "STAFFCD2", $row["STAFFCD2"]);
        knjCreateHidden($objForm, "STAFFCD3", $row["STAFFCD3"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" &&
            $model->cmd != "reset"     &&
            $model->cmd != "change") {
            if (!isset($model->warning)) {
                $arg["reload"] = "parent.left_frame.location.href='knjh705index.php?cmd=list&TESTID={$model->field["TESTID"]}';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh705Form2.html", $arg);
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
