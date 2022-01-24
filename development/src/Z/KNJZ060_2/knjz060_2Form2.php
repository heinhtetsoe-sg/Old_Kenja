<?php

require_once('for_php7.php');

class knjz060_2Form2
{
    public function main(&$model)
    {
        $arg["reload"] = "";

        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz060_2index.php", "", "edit");
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1' || $model->Properties["use_prg_schoolkind"] == "1") && ($model->school_kind == "")) {
            $model->school_kind = VARS::get("SCHOOL_KIND");
        }
        
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz060_2Query::getRow($model->classcd, $model, $model->school_kind);
        } else {
            $Row =& $model->field;
        }
        $db = Query::dbCheckOut();
        
        //教科コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CLASSCD"] = createTextBox($objForm, $Row["CLASSCD"], "CLASSCD", 3, 2, $extra);

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["use_prg_schoolkind"] == "1") {
            $arg["useCurriculumcd"] = "1";
            //学校校種
            $opt_school = array();
            $query = knjz060_2Query::getNamecd($model, 'A023');
            $value_flg = false;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_school[] =  array('label' => $row["LABEL"],
                                       'value' => $row["VALUE"]);
                if ($value == $row["VALUE"]) {
                    $value_flg = true;
                }
            }
            $value = ($value && $value_flg) ? $value : $opt_school[0]["value"];
            $extra = "";
            $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $Row["SCHOOL_KIND"], $opt_school, $extra, 1);
        }

        //教科名称
        $arg["data"]["CLASSNAME"] = createTextBox($objForm, $Row["CLASSNAME"], "CLASSNAME", 40, 60, "");

        //教科略称
        if ($model->Properties["CLASS_MST_CLASSABBV_SIZE"] != "") {
            $model->set_abbv = $model->Properties["CLASS_MST_CLASSABBV_SIZE"] * 2;
            $model->set_maxabbv = $model->Properties["CLASS_MST_CLASSABBV_SIZE"] * 3;
        } else {
            $model->set_abbv = 10;
            $model->set_maxabbv = 15;
        }
        $arg["data"]["CLASSABBV"] = createTextBox($objForm, $Row["CLASSABBV"], "CLASSABBV", $model->set_abbv, $model->set_maxabbv, "");

        //教科名称英字
        $arg["data"]["CLASSNAME_ENG"] = createTextBox($objForm, $Row["CLASSNAME_ENG"], "CLASSNAME_ENG", 40, 40, "");

        //教科略称英字
        $arg["data"]["CLASSABBV_ENG"] = createTextBox($objForm, $Row["CLASSABBV_ENG"], "CLASSABBV_ENG", 30, 30, "");

        //調査書用教科名
        $arg["data"]["CLASSORDERNAME1"] = createTextBox($objForm, $Row["CLASSORDERNAME1"], "CLASSORDERNAME1", 40, 60, "");

        //科目名その他２
        $arg["data"]["CLASSORDERNAME2"] = createTextBox($objForm, $Row["CLASSORDERNAME2"], "CLASSORDERNAME2", 40, 60, "");

        //科目名その他３
        $arg["data"]["CLASSORDERNAME3"] = createTextBox($objForm, $Row["CLASSORDERNAME3"], "CLASSORDERNAME3", 40, 60, "");

        //科目数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SUBCLASSES"] = createTextBox($objForm, $Row["SUBCLASSES"], "SUBCLASSES", 3, 2, $extra);

        //表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER"] = createTextBox($objForm, $Row["SHOWORDER"], "SHOWORDER", 3, 2, $extra);

        //調査書用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER2"] = createTextBox($objForm, $Row["SHOWORDER2"], "SHOWORDER2", 3, 2, $extra);

        //通知表用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER3"] = createTextBox($objForm, $Row["SHOWORDER3"], "SHOWORDER3", 3, 2, $extra);

        //成績一覧用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER4"] = createTextBox($objForm, $Row["SHOWORDER4"], "SHOWORDER4", 3, 2, $extra);

        //選択
        $arg["data"]["ELECTDIV"] = createCheckBox($objForm, "ELECTDIV", "1", ($Row["ELECTDIV"]==1) ? "checked" : "nocheck", "");

        //専門･その他
        $opt = array();
        $opt[] = array('label' => "", 'value' => "0");
        $opt[] = array('label' => '1：専門', 'value' => '1');
        $opt[] = array('label' => '2：その他', 'value' => '2');
        $Row["SPECIALDIV"] = ($Row["SPECIALDIV"]) ? $Row["SPECIALDIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SPECIALDIV"] = knjCreateCombo($objForm, "SPECIALDIV", $Row["SPECIALDIV"], $opt, $extra, 1);

        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //教科区分
            $query = knjz060_2Query::getNamecdTwo($model, 'Z052');
            $opt = array();
            $result->free();
            $result = $db->query($query);
            while ($rowwk = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] =  array('label' => $rowwk["LABEL"],
                                'value' => $rowwk["VALUE"]);
            }

            if (get_count($opt) == 0) {
                $opt[] = array('label' => "", 'value' => "");
                $opt[] = array('label' => '1：各教科等を合わせた指導', 'value' => '1');
                $opt[] = array('label' => '2：教科以外の領域', 'value' => '2');
                $opt[] = array('label' => '3：教科以外の総学', 'value' => '3');
            }
            $Row["SEQ001"] = ($Row["SEQ001"]) ? $Row["SEQ001"] : $opt[0]["value"];
            $extra = "";
            $arg["data"]["SEQ001"] = knjCreateCombo($objForm, "SEQ001", $Row["SEQ001"], $opt, $extra, 1);

            //状態区分
            $query = knjz060_2Query::getNamecdTwo($model, 'A033');
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $Row["SEQ001_REMARK2"], "SEQ001_REMARK2", $extra, 1, "BLANK");
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz060_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz060_2Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = createBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_udpate", "更 新", $extra);

    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ060/knjz060index.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_back"] = createBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("UPDATED", $Row["UPDATED"]));
}

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae(array("type"      => "text",
                       "name"      => $name,
                       "size"      => $size,
                       "maxlength" => $maxlen,
                       "value"     => $data,
                       "extrahtml" => $extra));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{
    $objForm->ae(array("type"      => "checkbox",
                       "name"      => $name,
                       "value"     => $value,
                       "extrahtml" => $extra,
                       "multiple"  => $multi));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"      => "button",
                       "name"      => $name,
                       "value"     => $value,
                       "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
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
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
