<?php

require_once('for_php7.php');

class knjz060_2aForm2 {

    function main(&$model) {

        $arg["reload"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz060_2aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz060_2aQuery::getRow($model, $model->ibclasscd, $model->ibprg_course);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //教科コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["IBCLASSCD"] = knjCreateTextBox($objForm, $Row["IBCLASSCD"], "IBCLASSCD", 3, 2, $extra);

        //IBコース
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $query = knjz060_2aQuery::getIBSchoolKind();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( 'label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
            if ($Row["IBPRG_COURSE"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["IBPRG_COURSE"] = ($Row["IBPRG_COURSE"] && $value_flg) ? $Row["IBPRG_COURSE"] : $opt[0]["value"];
        $arg["data"]["IBPRG_COURSE"] = knjCreateCombo($objForm, "IBPRG_COURSE", $Row["IBPRG_COURSE"], $opt, "", 1);

        //教科名
        $arg["data"]["IBCLASSNAME"] = knjCreateTextBox($objForm, $Row["IBCLASSNAME"], "IBCLASSNAME", 40, 60, "");

        //教科略称
        if ($model->Properties["CLASS_MST_CLASSABBV_SIZE"] != "") {
            $model->set_abbv = $model->Properties["CLASS_MST_CLASSABBV_SIZE"] * 2;
            $model->set_maxabbv = $model->Properties["CLASS_MST_CLASSABBV_SIZE"] * 3;
        } else {
            $model->set_abbv = 10;
            $model->set_maxabbv = 15;
        }
        $arg["data"]["IBCLASSABBV"] = knjCreateTextBox($objForm, $Row["IBCLASSABBV"], "IBCLASSABBV", $model->set_abbv, $model->set_maxabbv, "");

        //教科名英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["IBCLASSNAME_ENG"] = knjCreateTextBox($objForm, $Row["IBCLASSNAME_ENG"], "IBCLASSNAME_ENG", 40, 40, $extra);

        //教科略称英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["IBCLASSABBV_ENG"] = knjCreateTextBox($objForm, $Row["IBCLASSABBV_ENG"], "IBCLASSABBV_ENG", 30, 30, $extra);

        //調査書用教科名
        $arg["data"]["IBCLASSORDERNAME1"] = knjCreateTextBox($objForm, $Row["IBCLASSORDERNAME1"], "IBCLASSORDERNAME1", 40, 60, "");

        //教科名その他２
        $arg["data"]["IBCLASSORDERNAME2"] = knjCreateTextBox($objForm, $Row["IBCLASSORDERNAME2"], "IBCLASSORDERNAME2", 40, 60, "");

        //教科名その他３
        $arg["data"]["IBCLASSORDERNAME3"] = knjCreateTextBox($objForm, $Row["IBCLASSORDERNAME3"], "IBCLASSORDERNAME3", 40, 60, "");

        //科目数
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSUBCLASSES"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSES"], "IBSUBCLASSES", 3, 2, $extra);

        //表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSHOWORDER"] = knjCreateTextBox($objForm, $Row["IBSHOWORDER"], "IBSHOWORDER", 3, 2, $extra);

        //調査書用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSHOWORDER2"] = knjCreateTextBox($objForm, $Row["IBSHOWORDER2"], "IBSHOWORDER2", 3, 2, $extra);

        //通知表用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSHOWORDER3"] = knjCreateTextBox($objForm, $Row["IBSHOWORDER3"], "IBSHOWORDER3", 3, 2, $extra);

        //成績一覧用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSHOWORDER4"] = knjCreateTextBox($objForm, $Row["IBSHOWORDER4"], "IBSHOWORDER4", 3, 2, $extra);

        //選択
        $extra  = ($Row["IBELECTDIV"] == 1) ? "checked" : "nocheck";
        $extra .= " id=\"IBELECTDIV\"";
        $arg["data"]["IBELECTDIV"] = knjCreateCheckBox($objForm, "IBELECTDIV", "1", $extra, "");

        //専門･その他
        $opt = array();
        $opt[] = array('label' => "", 'value' => "0");
        $opt[] = array('label' => '1：専門', 'value' => '1');
        $opt[] = array('label' => '2：その他', 'value' => '2');
        $Row["IBSPECIALDIV"] = ($Row["IBSPECIALDIV"]) ? $Row["IBSPECIALDIV"] : $opt[0]["value"];
        $arg["data"]["IBSPECIALDIV"] = knjCreateCombo($objForm, "IBSPECIALDIV", $Row["IBSPECIALDIV"], $opt, "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjz060_2aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz060_2aForm2.html", $arg);
    }
}
//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $link = REQUESTROOT."/Z/KNJZ060A/knjz060aindex.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
}
?>
