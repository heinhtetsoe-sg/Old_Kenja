<?php
class knjl413hForm2
{
    public function main(&$model)
    {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl413hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //入試制度
        $query = knjl413hQuery::getNameMst($model, "L003", "", $model->applicantdiv);
        $applicantRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["APPLICANTDIV_NAME"] = $applicantRow["NAME1"];

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg") && $model->year && $model->applicantdiv && $model->examcoursecd && $model->shdiv) {
            $query = knjl413hQuery::getRow($model->year, $model->applicantdiv, $model->examcoursecd, $model->shdiv);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //受験コースコンボボックス
        $query = knjl413hQuery::getExamCourseMst($model, $model->examcoursecd);
        $examcoursecRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["EXAMCOURSECD"] = $examcoursecRow["LABEL"];

        //出願区分コンボボックス
        $query = knjl413hQuery::getSettingMst($model, "L006");
        $extra = "onchange=\"shdiv_submit()\"";
        $value = $model->field["SHDIV"] ? $model->field["SHDIV"] : $Row["SHDIV"];
        makeCombo($objForm, $arg, $db, $query, "SHDIV", $value, $extra, 1, "BLANK");

        $disable1 = " disabled";
        $disable2 = " disabled";
        $disable3 = " disabled";
        //中学を選択した場合
        if ($model->applicantdiv == "1") {
            $disable1 = "";
        }
        //高校を選択した場合
        if ($model->applicantdiv == "2") {
            $disable2 = "";
        }
        //高校特進Aコースを選択した場合 EXAMCOURSECD:02
        if ($model->applicantdiv == "2" && $model->examcoursecd == $model->tokushinACd) {
            $disable3 = "";
        }

        //内申内諾基準3年間5科テキストボックス
        $extra  = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable2;
        $arg["data"]["BORDER1"] = knjCreateTextBox($objForm, $Row["BORDER1"], "BORDER1", 3, 3, $extra);

        //内申内諾基準5科テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable2;
        $arg["data"]["BORDER2"] = knjCreateTextBox($objForm, $Row["BORDER2"], "BORDER2", 3, 3, $extra);

        //内申内諾基準9科テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable2;
        $arg["data"]["BORDER3"] = knjCreateTextBox($objForm, $Row["BORDER3"], "BORDER3", 3, 3, $extra);

        //強化クラブ推薦基準9科テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable2;
        $arg["data"]["BORDER4"] = knjCreateTextBox($objForm, $Row["BORDER4"], "BORDER4", 3, 3, $extra);

        //特別奨学生基準3年間5科(全額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable3;
        $arg["data"]["BORDER5"] = knjCreateTextBox($objForm, $Row["BORDER5"], "BORDER5", 3, 3, $extra);

        //特別奨学生基準3年次5科(全額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable3;
        $arg["data"]["BORDER6"] = knjCreateTextBox($objForm, $Row["BORDER6"], "BORDER6", 3, 3, $extra);

        //特別奨学生基準3年間5科(半額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable3;
        $arg["data"]["BORDER7"] = knjCreateTextBox($objForm, $Row["BORDER7"], "BORDER7", 3, 3, $extra);

        //特別奨学生基準3年次5科(半額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable3;
        $arg["data"]["BORDER8"] = knjCreateTextBox($objForm, $Row["BORDER8"], "BORDER8", 3, 3, $extra);

        //特別奨学生基準入試得点率(全額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable2;
        $arg["data"]["BORDER9"] = knjCreateTextBox($objForm, $Row["BORDER9"], "BORDER9", 3, 3, $extra);

        //特別奨学生基準入試成績上位(全額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable1;
        $arg["data"]["BORDER10"] = knjCreateTextBox($objForm, $Row["BORDER10"], "BORDER10", 3, 3, $extra);

        //特別奨学生基準入試得点率(半額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable2;
        $arg["data"]["BORDER11"] = knjCreateTextBox($objForm, $Row["BORDER11"], "BORDER11", 3, 3, $extra);

        //特別奨学生基準入試成績上位(半額)テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disable1;
        $arg["data"]["BORDER12"] = knjCreateTextBox($objForm, $Row["BORDER12"], "BORDER12", 3, 3, $extra);

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
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_EXAMCOURSECD", $model->examcoursecd);
        //高校特進Aコースを選択した場合 EXAMCOURSECD:02
        knjCreateHidden($objForm, "TOKUSHINA", $model->tokushinACd);
        knjCreateHidden($objForm, "chFlg");

        $hidCoursecd = $Row["COURSECD"];
        $hidMajorcd = $Row["MAJORCD"];
        if ($hidCoursecd == "" || $hidMajorcd == "") {
            $query = knjl413hQuery::getExamCourseMst($model);
            $rowCourse = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $hidCoursecd = $rowCourse["COURSECD"];
            $hidMajorcd = $rowCourse["MAJORCD"];
        }
        knjCreateHidden($objForm, "COURSECD", $hidCoursecd);
        knjCreateHidden($objForm, "MAJORCD", $hidMajorcd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl413hindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl413hForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    }

    //SHDIVの場合
    if ($name == "SHDIV") {
        $opt[0]["value"] = "0";
    }

    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["DEFAULT"] && $default_flg) {
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
