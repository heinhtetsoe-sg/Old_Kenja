<?php

require_once('for_php7.php');

class knjz070_2aForm2 {

    function main(&$model) {

        $arg["reload"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz070_2aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz070_2aQuery::getRow($model, $model->ibclasscd, $model->ibprg_course, $model->ibcurriculum_cd, $model->ibsubclasscd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //教科・IBコース
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value = $Row["IBCLASSCD"].'-'.$Row["IBPRG_COURSE"];
        $value_flg = false;
        $query = knjz070_2aQuery::getClassData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( 'label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg["data"]["IBCLASS_SCHOOL"] = knjCreateCombo($objForm, "IBCLASS_SCHOOL", $value, $opt, "", 1);

        //教育課程
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $query = knjz070_2aQuery::getIBCurriculumCd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( 'label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
            if ($Row["IBCURRICULUM_CD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["IBCURRICULUM_CD"] = ($Row["IBCURRICULUM_CD"] && $value_flg) ? $Row["IBCURRICULUM_CD"] : $opt[0]["value"];
        $arg["data"]["IBCURRICULUM_CD"] = knjCreateCombo($objForm, "IBCURRICULUM_CD", $Row["IBCURRICULUM_CD"], $opt, "", 1);

        //科目コード
        $ibsubclasscd = (strlen($Row["IBSUBCLASSCD"]) > 4) ? substr($Row["IBSUBCLASSCD"], 2, 4) : $Row["IBSUBCLASSCD"];
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["IBSUBCLASS"] = knjCreateTextBox($objForm, $ibsubclasscd, "IBSUBCLASS", 4, 4, $extra);

        //科目名
        $arg["data"]["IBSUBCLASSNAME"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSNAME"], "IBSUBCLASSNAME", 40, 60, "");

        //科目略称
        if ($model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] != "") {
            $model->set_abbv = $model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] * 2;
            $model->set_maxabbv = $model->Properties["SUBCLASS_MST_SUBCLASSABBV_SIZE"] * 3;
        } else {
            $model->set_abbv = 6;
            $model->set_maxabbv = 9;
        }
        $arg["data"]["IBSUBCLASSABBV"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSABBV"], "IBSUBCLASSABBV", $model->set_abbv, $model->set_maxabbv, "");

        //科目名英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["IBSUBCLASSNAME_ENG"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSNAME_ENG"], "IBSUBCLASSNAME_ENG", 50, 50, $extra);

        //科目略称英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["IBSUBCLASSABBV_ENG"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSABBV_ENG"], "IBSUBCLASSABBV_ENG", 30, 30, $extra);

        //科目名その他１
        $arg["data"]["IBSUBCLASSORDERNAME1"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSORDERNAME1"], "IBSUBCLASSORDERNAME1", 60, 60, "");

        //科目名その他２
        $arg["data"]["IBSUBCLASSORDERNAME2"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSORDERNAME2"], "IBSUBCLASSORDERNAME2", 60, 60, "");

        //科目名その他３
        $arg["data"]["IBSUBCLASSORDERNAME3"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSORDERNAME3"], "IBSUBCLASSORDERNAME3", 60, 60, "");

        //表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSHOWORDER"] = knjCreateTextBox($objForm, $Row["IBSHOWORDER"], "IBSHOWORDER", 2, 2, $extra);

        //調査書用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSHOWORDER2"] = knjCreateTextBox($objForm, $Row["IBSHOWORDER2"], "IBSHOWORDER2", 2, 2, $extra);

        //通知表用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSHOWORDER3"] = knjCreateTextBox($objForm, $Row["IBSHOWORDER3"], "IBSHOWORDER3", 2, 2, $extra);

        //調査書・指導要録用科目グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["IBSUBCLASSCD2"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSCD2"], "IBSUBCLASSCD2", 6, 6, $extra);

        //通知表用科目グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["IBSUBCLASSCD3"] = knjCreateTextBox($objForm, $Row["IBSUBCLASSCD3"], "IBSUBCLASSCD3", 6, 6, $extra);

        //選択
        $extra  = ($Row["IBELECTDIV"] == 1) ? "checked" : "nocheck";
        $extra .= " id=\"IBELECTDIV\"";
        $arg["data"]["IBELECTDIV"] = knjCreateCheckBox($objForm, "IBELECTDIV", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjz070_2aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz070_2aForm2.html", $arg);
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
    $link = REQUESTROOT."/Z/KNJZ070A/knjz070aindex.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
}
?>
