<?php

require_once('for_php7.php');

class knjh702form2
{
    public function main(&$model)
    {

        //取消ボタン押下時
        if ($model->cmd == "reset") {
            $model->field["TESTDIV"]  = null;
            $model->field["BUNRIDIV"] = null;
            $model->field["CLASSCD"]  = null;
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh702index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (isset($model->testdiv)    === true  &&
            isset($model->bunridiv)   === true  &&
            isset($model->classcd)    === true  &&
            isset($model->subclasscd) === true  &&
            isset($model->warning)    === false &&
            ($model->cmd != "change")) {
            $result = $db->query(knjh702Query::getAcademicTestSubclassData(CTRL_YEAR, $model->testdiv, $model->bunridiv, $model->classcd, $model->subclasscd));
            while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = $rowTemp;
            }
            $result->free();
        } else {
            $row =& $model->field;
        }

        //テスト区分コンボボックス
        $result = $db->query(knjh702Query::getTestDivNameMst("H320"));
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
        $arg["data"]["TESTDIVNAME"] = knjCreateCombo($objForm, "TESTDIV", $row["TESTDIV"], $opt, $extra, 1);

        //文理区分コンボボックス
        $query = knjh702Query::getBunriDivNameMst("H319");
        $extra = "";
        if ($row["BUNRIDIV"] == "") {
            $row["BUNRIDIV"] = $model->field["BUNRIDIV"];
        }
        makeCmb($objForm, $arg, $db, $query, "BUNRIDIV", $row["BUNRIDIV"], $extra, 1);

        //教科コンボボックス
        $query = knjh702Query::getClasscd();
        $extra = "";
        if ($row["CLASSCD"] == "") {
            $row["CLASSCD"] = $model->field["CLASSCD"];
        }
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $row["CLASSCD"], $extra, 1);

        //科目コードテキストボックス
        $extra = "onblur=\"numCheck(this);\"";
        if ($row["SUBCLASSCD"] == "") {
            $row["SUBCLASSCD"] = $model->field["SUBCLASSCD"];
        }
        $arg["data"]["SUBCLASSCD"] = knjCreateTextBox($objForm, $row["SUBCLASSCD"], "SUBCLASSCD", 6, 6, $extra);

        //科目名称テキストボックス
        $extra = "onkeypress=\"btn_keypress();\"";
        if ($row["SUBCLASSNAME"] == "") {
            $row["SUBCLASSNAME"] = $model->field["SUBCLASSNAME"];
        }
        $arg["data"]["SUBCLASSNAME"] = knjCreateTextBox($objForm, $row["SUBCLASSNAME"], "SUBCLASSNAME", 40, 20, $extra);

        //科目略称テキストボックス
        $extra = "onkeypress=\"btn_keypress();\"";
        if ($row["SUBCLASSABBV"] == "") {
            $row["SUBCLASSABBV"] = $model->field["SUBCLASSABBV"];
        }
        $arg["data"]["SUBCLASSABBV"] = knjCreateTextBox($objForm, $row["SUBCLASSABBV"], "SUBCLASSABBV", 10, 5, $extra);

        //必修/選択区分チェックボックスを作成
        if ($row["ELECTDIV"] == "") {
            $row["ELECTDIV"] = $model->field["ELECTDIV"];
        }
        $extra  = ($row["ELECTDIV"] == "2")? "checked" : "";
        $extra .= " id=\"ELECTDIV\"";
        $arg["data"]["ELECTDIV"] = knjCreateCheckBox($objForm, "ELECTDIV", "2", $extra, "");

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
                $arg["reload"] = "parent.left_frame.location.href='knjh702index.php?cmd=list';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh702Form2.html", $arg);
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

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
