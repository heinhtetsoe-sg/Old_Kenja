<?php

require_once('for_php7.php');
class knjz331aForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //$arg["Read"] = "start();";

        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjz331aindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "change_menu") {
            //メニュー名
            $query = knjz331aQuery::getMenuName($model);
            $model->field["MENUNAME"] = $db->getOne($query);
        }

        //個人メニュー名
        $extra = "";
        $arg["MENUNAME"] = knjCreateTextBox($objForm, $model->field["MENUNAME"], "MENUNAME", 30, 30, $extra);

        //個人サブメニューコンボ
        $query = knjz331aQuery::getSubMenuId();
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUB_MENUID"], "SUB_MENUID", $extra, 1);

        //個人サブメニュー名
        if (!isset($model->warning) && $model->cmd != "change_menu") {
            $query = knjz331aQuery::getSubMenuName($model);
            $model->field["SUB_MENUNAME"] = $db->getOne($query);
        }
        $extra = "";
        $arg["SUB_MENUNAME"] = knjCreateTextBox($objForm, $model->field["SUB_MENUNAME"], "SUB_MENUNAME", 30, 30, $extra);

        //タイトル名
        $extra = "";
        $arg["TITLE_NAME"] = knjCreateTextBox($objForm, $model->field["TITLE_NAME"], "TITLE_NAME", 30, 30, $extra);

        //基本メニューコンボ
        $query = knjz331aQuery::getBaseMenuCmb($model);
        $extra = "onchange=\"return btn_submit('change_menu')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SELECT_MENUNAME"], "SELECT_MENUNAME", $extra, 1, "SORT");

        //サブメニューコンボ
        $query = knjz331aQuery::getSubMenuCmb($model);
        $extra = "onchange=\"return btn_submit('change_menu')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SELECT_SUB_MENUID"], "SELECT_SUB_MENUID", $extra, 1);

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz331aForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "SORT") {
        $opt[] = array("label" => "並び替え",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //左リスト(溜める式)
    $selectdata      = ($model->selectdata != "")       ? explode(",", $model->selectdata)      : array();
    $selectdataLabel = ($model->selectdataLabel != "")  ? explode(",", $model->selectdataLabel) : array();

    //左リスト
    $opt_left = array();
    if ($model->cmd == 'change_menu' ) {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array("label" => $selectdataLabel[$i],
                                "value" => $selectdata[$i]);
        }
    } else {
        $result = $db->query(knjz331aQuery::getStaffMenuCmb($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        }
        $result->free();
    }

    //右リスト
    $opt_right = array();
    $result = $db->query(knjz331aQuery::getListMenuCmb($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[]= array("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
    }
    $result->free();

    //生徒一覧リスト(右)
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('left', 0)\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "", $opt_right, $extra, 35);

    //対象者一覧リスト(左)
    $setSort = $model->field["SELECT_MENUNAME"] ? "1" : "0";
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"moveStudent('right', $setSort)\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "", $opt_left, $extra, 35);

    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('sel_add_all', 0);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //対象選択ボタン
    $extra = "onclick=\"return moveStudent('left', 0);\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('right', $setSort);\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタン
    $extra = "onclick=\"return moveStudent('sel_del_all', $setSort);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //タイトル
    $extra = "onclick=\"return titleAdd();\"";
    $arg["TITLE_BTN"] = knjCreateBtn($objForm, "TITLE_BTN", "タイトル追加", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataLabel");
}
?>
