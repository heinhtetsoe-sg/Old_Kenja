<?php

require_once('for_php7.php');

class knjz331Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjz331Form1", "POST", "knjz331index.php", "", "knjz331Form1");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning)) {
            //メニュー名
            $query = knjz331Query::getMenuName();
            $model->field["MENUNAME"] = $db->getOne($query);
        }

        //メニュー名
        $extra = "";
        $arg["sepa"]["MENUNAME"] = knjCreateTextBox($objForm, $model->field["MENUNAME"], "MENUNAME", 30, 30, $extra);

        //サブメニューコンボ
        $query = knjz331Query::getSubMenuId();
        $extra = "onchange=\"return btn_submit('subMain')\"";
        $arg["sepa"]["SUB_MENUID"] = makeCmb($objForm, $arg, $db, $query, "SUB_MENUID", $model->field["SUB_MENUID"], $extra, 1);

        //サブメニュー名
        $query = knjz331Query::getSubMenuName($model);
        $model->field["SUB_MENUNAME"] = $db->getOne($query);
        $extra = "";
        $arg["sepa"]["SUB_MENUNAME"] = knjCreateTextBox($objForm, $model->field["SUB_MENUNAME"], "SUB_MENUNAME", 30, 30, $extra);

        //メニュー数
        if ($model->cmd != "menuCnt") {
            $query = knjz331Query::getMenuCnt($model);
            $model->field["MENU_CNT"] = $db->getOne($query);
        }
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["sepa"]["MENU_CNT"] = knjCreateTextBox($objForm, $model->field["MENU_CNT"], "MENU_CNT", 2, 2, $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('menuCnt');\"";
        $arg["sepa"]["btn_cnt"] = knjCreateBtn($objForm, "btn_cnt", "確 定", $extra);

        //登録項目行作成
        makeMenuGyou($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $extra = "onClick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJZ331");
        knjCreateHidden($objForm, "selectMenu");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz331Form1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $argName = "", $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    $value_flg = false;
    $sep = "";
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        $sep = ",";
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//登録項目行作成
function makeMenuGyou(&$objForm, &$arg, $db, $model) {

    $subMenuTopCD = substr($model->field["SUB_MENUID"], 0, 3);

    $query = knjz331Query::getMenuCmb($model);
    $result = $db->query($query);
    $menuOpt = array();
    $menuOptPrgId = array();
    $menuOpt[] = array('label' => "",
                       'value' => "");
    $menuOpt[] = array('label' => "タイトル行",
                       'value' => "100");
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $menuOpt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        list($prgId, $menuId) = preg_split("/:/", $row["VALUE"]);
        $menuOptPrgId[$prgId] = $menuId;
    }
    $result->free();

    for ($syoriCnt = 1; $syoriCnt <= $model->field["MENU_CNT"]; $syoriCnt++) {
        $setSoeji = sprintf("%02d", $syoriCnt);
        $setGyou = array();

        $extra = "";
        $setGyou["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK".$setSoeji, "1", $extra);

        $extra = "";
        $setGyou["PROGRAMID"] = knjCreateCombo($objForm, "PROGRAMID", $value, $menuOpt, $extra, 1);

        $setGyou["TITLE_NAME"];
        $arg["data"][] = $setGyou;
    }
}
?>
