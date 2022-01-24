<?php

require_once('for_php7.php');

class knjh020_2aForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh020_2aindex.php", "", "edit");

        if (!isset($model->warning)) {
            if ($model->relano) {
                $Row_relative = knjh020_2aQuery::getRow_relative($model);
            }
            $RowBD = knjh020_2aQuery::getRowBaseD($model);
        } else {
            $Row_relative =& $model->field;
            $RowBD["BASE_RELATIONSHIP"] = $model->field["BASE_RELATIONSHIP"];
            $RowBD["BASE_FIRST_CHILD"] = $model->field["BASE_FIRST_CHILD"];
        }
        global $sess;

        $db     = Query::dbCheckOut();

        //本人続柄
        $opt   = array();
        $opt[] = array("label" => "","value" => "");
        $query  = knjh020_2aQuery::getNameMst_data("H203");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"]."：".htmlspecialchars($row["NAME1"]),"value" => $row["NAMECD2"]);
        }
        $arg["data"]["BASE_RELATIONSHIP"] = knjCreateCombo($objForm, "BASE_RELATIONSHIP", $RowBD["BASE_RELATIONSHIP"], $opt, "", 1);
        //長子
        $extra  = (strlen($RowBD["BASE_FIRST_CHILD"])) ? "checked " : "";
        $extra .= "id=\"BASE_FIRST_CHILD\"";
        $arg["data"]["BASE_FIRST_CHILD"] = knjCreateCheckBox($objForm, "BASE_FIRST_CHILD", "1", $extra);

        //本籍都道府県コンボボックス
        $query = knjh020_2aQuery::getPrefMst();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "BASE_PREF", $RowBD["BASE_PREF"], $extra, 1, "blank");

        //テキストエリア
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["STUCD"] = knjCreateTextBox($objForm, $model->stucd, "STUCD", 10, 10, $extra);

        //検索ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('./knjh020_2aSubForm1.php?cmd=search&MODE=reflect&useGuardian2={$model->Properties["useGuardian2"]}&CD=$model->schregno&SCHREGNO='+document.forms[0]['STUCD'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["BTN_STUCD"] = knjCreateBtn($objForm, "btn_stucd", "兄弟姉妹検索", $extra);

        //反映ボタンを作成する
        $extra = "onclick=\"return btn_submit('apply');\"";
        $arg["data"]["BTN_APPLY"] = knjCreateBtn($objForm, "btn_apply", "反映", $extra);

        //親族氏名
        $extra = "onChange=\"\"";
        $arg["data"]["RELANAME"] = knjCreateTextBox($objForm, $Row_relative["RELANAME"], "RELANAME", 40, 40, $extra);
        $arg["data"]["NAMESLEN"] = $this->Properties["NAME_INPUT_SIZE"] ? intval($this->Properties["NAME_INPUT_SIZE"]) : 20;

        //親族氏名かな
        $extra = "onChange=\"\"";
        $arg["data"]["RELAKANA"] = knjCreateTextBox($objForm, $Row_relative["RELAKANA"], "RELAKANA", 40, 80, $extra);

        //性別
        $query  = knjh020_2aQuery::getNameMst_data("Z002");
        $result = $db->query($query);
        //性別コンボボックスの中身を作成------------------------------
        $opt_sex   = array();
        $opt_sex[] = array("label" => "","value" => "0");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sex[] = array("label" => $row["NAMECD2"]."：".htmlspecialchars($row["NAME2"]),"value" => $row["NAMECD2"]);
        }

        $extra = "onChange=\"\"";
        $arg["data"]["RELASEX"] = knjCreateCombo($objForm, "RELASEX", $Row_relative["RELASEX"], $opt_sex, $extra, 1);

        //生年月日カレンダーコントロール
        $arg["data"]["RELABIRTHDAY"] = View::popUpCalendar($objForm, "RELABIRTHDAY", str_replace("-", "/", $Row_relative["RELABIRTHDAY"]), "");

        //続柄
        $query  = knjh020_2aQuery::getNameMst_data("H201");
        $result = $db->query($query);
        //続柄コンボボックスの中身を作成------------------------------
        $opt_relat   = array();
        $opt_relat[] = array("label" => "","value" => "00");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_relat[] = array("label" => $row["NAMECD2"]."：".htmlspecialchars($row["NAME1"]),"value" => $row["NAMECD2"]);
        }

        $extra = "onChange=\"\"";
        $arg["data"]["RELATIONSHIP"] = knjCreateCombo($objForm, "RELATIONSHIP", $Row_relative["RELATIONSHIP"], $opt_relat, $extra, 1);

        //職業または学校
        $extra = "onChange=\"\"";
        $arg["data"]["OCCUPATION"] = knjCreateTextBox($objForm, $Row_relative["OCCUPATION"], "OCCUPATION", 40, 40, $extra);

        if ($model->rela_schregno) {
            $Row_relative["RELA_SCHREGNO"] = $model->rela_schregno;
        }

        //テキストエリア
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RELA_SCHREGNO"] = knjCreateTextBox($objForm, $Row_relative["RELA_SCHREGNO"], "RELA_SCHREGNO", 10, 10, $extra);
        //検索ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('./knjh020_2aSubForm1.php?cmd=search&MODE=set&useGuardian2={$model->Properties["useGuardian2"]}&CD=$model->schregno&SCHREGNO='+document.forms[0]['RELA_SCHREGNO'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["BTN_STUCD2"] = knjCreateBtn($objForm, "btn_stucd2", "兄弟姉妹検索", $extra);

        //在卒区分コンボボックス
        $opt = array();
        $opt[] = array('label' => "",     'value' => "");
        $opt[] = array('label' => "在学", 'value' => "1");
        $opt[] = array('label' => "卒業", 'value' => "2");
        $Row_relative["REGD_GRD_FLG"] = ($Row_relative["REGD_GRD_FLG"]) ? $Row_relative["REGD_GRD_FLG"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["REGD_GRD_FLG"] = knjCreateCombo($objForm, "REGD_GRD_FLG", $Row_relative["REGD_GRD_FLG"], $opt, $extra, 1);

        //学年コンボボックス
        $query = knjh020_2aQuery::getSchregRegdGdat($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "RELA_GRADE", $Row_relative["RELA_GRADE"], $extra, 1, "blank");

        //同居区分
        $query  = knjh020_2aQuery::getNameMst_data("H200");
        $result = $db->query($query);

        //同居区分コンボボックスの中身を作成------------------------------
        $opt_relat   = array();
        $opt_relat[] = array("label" => "","value" => "00");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_relat[] = array( "label" => $row["NAMECD2"]."：".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
        }

        $extra = "onChange=\"\"";
        $arg["data"]["REGIDENTIALCD"] = knjCreateCombo($objForm, "REGIDENTIALCD", $Row_relative["REGIDENTIALCD"], $opt_relat, $extra, 1);

        //備考
        $extra = "onChange=\"\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row_relative["REMARK"], "REMARK", 30, 30, $extra);

        $result->free();
        Query::dbCheckIn($db);

        //更 新ボタンを作成する
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //追 加ボタンを作成する
        $extra = " onclick=\"return btn_submit('add');\"";
        $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //取 消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);

        //削 除ボタンを作成する
        $extra = " onclick=\"return btn_submit('delete');\"";
        $arg["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //保護者情報へボタンを作成する
        $link1 = REQUESTROOT."/H/KNJH020A/knjh020aindex.php?cmd=edit";
        $extra = "onclick=\" Page_jumper('".$link1."','".$model->schregno."');\"";
        $arg["btn_jump"] = knjCreateBtn($objForm, "btn_jump", "保護者情報へ", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //hiddenを作成する
        knjCreateHidden($objForm, "RELANO", $model->relano);

        //hiddenを作成する
        knjCreateHidden($objForm, "UPDATED", $Row_relative["UPDATED"]);

        //学籍番号
        knjCreateHidden($objForm, "tmpSCHREGNO", $model->schregno);

        $arg["finish"]  = $objForm->get_finish();

        //更新後リスト画面をリロードする
        if ($model->isMessage()) {
            $arg["reload"] = "window.open('knjh020_2aindex.php?cmd=main','top_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh020_2aForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
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
?>
