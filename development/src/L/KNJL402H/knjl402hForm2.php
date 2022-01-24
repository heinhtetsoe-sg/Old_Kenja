<?php
class knjl402hForm2
{
    public function main(&$model)
    {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl402hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2") && $model->year && $model->examcoursecd && $model->applicantdiv && $model->coursecd && $model->majorcd) {
            $query = knjl402hQuery::getRow($model->year, $model->examcoursecd, $model->applicantdiv, $model->coursecd, $model->majorcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /****************/
        /*  コンボ作成  */
        /****************/
        //課程学科コンボ
        $query = knjl402hQuery::getTotalcd($model->year, "default");
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $Row["TOTALCD"], "TOTALCD", $extra, 1, $model, "blank");

        //入学課程学科コンボ
        $query = knjl402hQuery::getTotalcd($model->year);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $Row["ENTER_TOTALCD"], "ENTER_TOTALCD", $extra, 1, $model, "blank");

        //入学コースコンボ
        $query = knjl402hQuery::getCourceCode($model->year);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $Row["ENTER_COURSECODE"], "ENTER_COURSECODE", $extra, 1, $model, "blank");

        /******************/
        /*  テキスト作成  */
        /******************/
        //コースコードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMCOURSECD"] = knjCreateTextBox($objForm, $Row["EXAMCOURSECD"], "EXAMCOURSECD", 2, 2, $extra);

        //コース名テキストボックス
        $arg["data"]["EXAMCOURSE_NAME"] = knjCreateTextBox($objForm, $Row["EXAMCOURSE_NAME"], "EXAMCOURSE_NAME", 30, 15, "");

        //コース名略称テキストボックス
        $arg["data"]["EXAMCOURSE_ABBV"] = knjCreateTextBox($objForm, $Row["EXAMCOURSE_ABBV"], "EXAMCOURSE_ABBV", 6, 3, "");

        //コース記号テキストボックス
        $extra = "onblur=\"this.value=toAlpha(this.value)\"";
        $arg["data"]["EXAMCOURSE_MARK"] = knjCreateTextBox($objForm, $Row["EXAMCOURSE_MARK"], "EXAMCOURSE_MARK", 2, 2, $extra);

        //出願情報認識用文字列テキストボックス
        $arg["data"]["MAPPING_NAME"] = knjCreateTextBox($objForm, $Row["MAPPING_NAME"], "MAPPING_NAME", 20, 10, "");

        /****************/
        /*  ボタン作成  */
        /****************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //更新ボタン
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

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && $model->cmd != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl402hindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl402hForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $model, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
        $i++;
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if (!isset($model->warning) && $row["DEFAULT"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
