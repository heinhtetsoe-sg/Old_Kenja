<?php

require_once('for_php7.php');

class knjl072wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl072wForm1", "POST", "knjl072windex.php", "", "knjl072wForm1");

        //権限チェック
        $adminFlg = knjl072wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onChange=\"return btn_submit('main');\"";
        $query = knjl072wQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onChange=\"return btn_submit('main');\"";
        $query = knjl072wQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        if ($model->testdiv == "5" || $model->testdiv == "8") {
            //追検査の受検者のみを表示するチェックボックス
            $extra  = " id=\"TESTDIV2\" ";
            $extra .= " onClick=\"return btn_submit('main');\" ";
            $extra .= ($model->testdiv2 == "1") ? "checked" : "";
            $arg["data"]["TESTDIV2"] = knjCreateCheckBox($objForm, "TESTDIV2", "1", $extra);
        }

        //志望区分ラジオボタン 1:第一志望 2:第二志望
        $opt_wish = array(1, 2);
        $model->wishdiv = ($model->wishdiv == "") ? "1" : $model->wishdiv;
        $click = " onClick=\"return btn_submit('main');\"";
        $extra = array("id=\"WISHDIV1\"".$click, "id=\"WISHDIV2\"".$click);
        $radioArray = knjCreateRadio($objForm, "WISHDIV", $model->wishdiv, $extra, $opt_wish, get_count($opt_wish));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //コースコンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl072wQuery::getCourse($model);
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->examcoursecd, $extra, 1);

        //合格コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl072wQuery::getJudge($model);
        makeCmb($objForm, $arg, $db, $query, "JUDGE", $model->judge, $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl072wForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //合格者一覧
    $opt_left = $tmp_id = array();
    $result = $db->query(knjl072wQuery::getLeftList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["JUDGEMENT"] == $model->judge) {
            $opt_left[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        $tmp_id[]   = $row["VALUE"];
    }
    $result->free();
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move3('right','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["data"]["LEFT_LIST"] = knjCreateCombo($objForm, "LEFT_LIST", "", $opt_left, $extra, 30);

    //受検者一覧
    $opt_right = array();
    $result = $db->query(knjl072wQuery::getRightList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $tmp_id)) {
            $opt_right[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move3('left','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["data"]["RIGHT_LIST"] = knjCreateCombo($objForm, "RIGHT_LIST", "", $opt_right, $extra, 30);

    //対象選択ボタン（全て）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move3('sel_add_all','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move3('left','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["button"]["sel_add"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move3('right','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["button"]["sel_del"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタン（全て）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move3('sel_del_all','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdata2");
}
?>
