<?php

require_once('for_php7.php');

class knjl005aForm2
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl005aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->testdiv && $model->mappingNo) {
            $query = knjl005aQuery::getRow($model->year, $model->applicantdiv, $model->testdiv, $model->mappingNo);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //初期値セット
        $model->year = ($model->year == "") ? CTRL_YEAR + 1: $model->year;
        if ($model->applicantdiv == "") {
            $extra = "onchange=\"return btn_submit('list');\"";
            $query = knjl005aQuery::getNameMst($model, "L003");
            makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");
        }
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl005aQuery::getNameMst($model, "L003", $model->applicantdiv));

        if ($model->testdiv == "") {
            $extra = "onchange=\"return btn_submit('list');\"";
            $query = knjl005aQuery::getTestDivMst($model);
            makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");
        }
        $arg["data"]["TESTDIV"] = $db->getOne(knjl005aQuery::getTestDivMst($model, $model->testdiv));

        /*****************/
        /** textbox作成 **/
        /*****************/

        //対応コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $value = (isset($Row["MAPPING_NO"])) ? $Row["MAPPING_NO"] : "";
        $arg["data"]["MAPPING_NO"] = knjCreateTextBox($objForm, $value, "MAPPING_NO", 2, 2, $extra);

        //認識用文字列
        $extra = "";
        $value = (isset($Row["TESTDIV_NAME"])) ? $Row["TESTDIV_NAME"] : "";
        $arg["data"]["TESTDIV_NAME"] = knjCreateTextBox($objForm, $value, "TESTDIV_NAME", 51, 50, $extra);

        /******************/
        /**  コンボ作成   **/
        /******************/

        //専併区分
        $extra = "";
        $query = knjl005aQuery::getNameMst($model, "L006");
        $value = (isset($Row["SHDIV"])) ? $Row["SHDIV"] : "";
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $value, $extra, 1, "");
        //志望コース
        $extra = "";
        $namecd1 = ($model->applicantdiv == "1") ? "LJ58" : "LH58";
        $query = knjl005aQuery::getNameMst($model, $namecd1);
        $value = (isset($Row["COURSEDIV"])) ? $Row["COURSEDIV"] : "";

        makeCmb($objForm, $arg, $db, $query, "COURSEDIV", $value, $extra, 1, "");
        //受験型
        $extra = "";
        $query = knjl005aQuery::getExamtypeMst($model);
        $value = (isset($Row["EXAM_TYPE"])) ? $Row["EXAM_TYPE"] : "";
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $value, $extra, 1, "");

        /**************/
        /**ボタン作成**/
        /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        /**************/
        /**hidden作成**/
        /**************/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl005aindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."&testdiv=".$model->testdiv."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl005aForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        $defaultConf = "";
        if ($name == "TESTDIV") { //入試区分
            $defaultConf = $row["DEFAULT_FLG"];
        } else { //名称マスタ系
            $defaultConf = $row["NAMESPARE2"];
        }
        if ($defaultConf && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
