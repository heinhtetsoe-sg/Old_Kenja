<?php

require_once('for_php7.php');

class knjl327wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl327wForm1", "POST", "knjl327windex.php", "", "knjl327wForm1");

        //権限チェック
        $adminFlg = knjl327wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onChange=\"return btn_submit('main');\"";
        $query = knjl327wQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //様式ラジオボタン 1:結果通知書 2:合格内定通知書 3:開示資料 4:合格通知書
        $opt = array(1, 2, 3, 4);
        $model->style = ($model->style == "") ? "1" : $model->style;
        $click = " onClick=\"return btn_submit('main');\"";
        $extra = array("id=\"STYLE1\"".$click, "id=\"STYLE2\"".$click, "id=\"STYLE3\"".$click, "id=\"STYLE4\"".$click);
        $radioArray = knjCreateRadio($objForm, "STYLE", $model->style, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //入試区分コンボボックス
        $extra = "onChange=\"return btn_submit('main');\"";
        $query = knjl327wQuery::getNameMst($model->ObjYear, "L004", $model->style);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        if ($model->style == "2") {
            $arg["printNaitei"] = 1;
            $arg["RIGHT_LISTNAME"] = "内定者";

            //リストToリスト作成
            makeListToList($objForm, $arg, $db, $model);

            //合格発表日
            $model->suc_date = ($model->suc_date) ? $model->suc_date : str_replace('-', '/', CTRL_DATE);
            $arg["data"]["SUC_DATE"] = View::popUpCalendar($objForm, "SUC_DATE", $model->suc_date);
            //合格発表日(午前、午後)
            $model->suc_am_pm = ($model->suc_am_pm) ? $model->suc_am_pm : "1";
            $extra = "";
            $query = knjl327wQuery::getSucAmPm();
            makeCmb($objForm, $arg, $db, $query, "SUC_AM_PM", $model->suc_am_pm, $extra, 1);
            //合格発表日(時)
            $model->suc_hour = ($model->suc_hour) ? $model->suc_hour : "9";
            $extra = "";
            $query = knjl327wQuery::getSucHour();
            makeCmb($objForm, $arg, $db, $query, "SUC_HOUR", $model->suc_hour, $extra, 1);
            //合格発表日(分)
            $model->suc_minute = ($model->suc_minute) ? $model->suc_minute : "30";
            $extra = "";
            $query = knjl327wQuery::getSucMinute();
            makeCmb($objForm, $arg, $db, $query, "SUC_MINUTE", $model->suc_minute, $extra, 1);
        } else if ($model->style == "3") {
            $arg["printNaitei"] = 1;
            $arg["RIGHT_LISTNAME"] = "受検者";

            //リストToリスト作成
            makeListToList($objForm, $arg, $db, $model);
        }
        if ($model->style != "3") {
            $arg["DISP_DATEINFO"] = "1";
        }
        if ($model->style == "4") {
            $arg["DISP_COMMENT"] = "1";

            //テキストエリア
            $extra = "id=\"COMMENT\"";
            $arg["data"]["COMMENT"] = knjCreateTextArea($objForm, "COMMENT", 12, 38*2+1, "soft", $extra, $model->comment);
            $arg["data"]["COMMENT_SIZE"] = '<font size="1" color="red">(全角38文字12行まで)</font>';

            KnjCreateHidden($objForm, "COMMENT_KETA", 38*2+1);
            KnjCreateHidden($objForm, "COMMENT_GYO", 12);
            KnjCreateHidden($objForm, "COMMENT_STAT", "statusarea1");
        }

        //記載日
        $model->kisai_date = ($model->kisai_date) ? $model->kisai_date : str_replace('-', '/', CTRL_DATE);
        $arg["data"]["KISAI_DATE"] = View::popUpCalendar($objForm, "KISAI_DATE", $model->kisai_date);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl327wForm1.html", $arg);
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
    //内定者一覧
    $opt_left = $opt_right = array();
    $result = $db->query(knjl327wQuery::getRightList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
    }
    $result->free();

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move3('right','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["data"]["LEFT_LIST"] = knjCreateCombo($objForm, "LEFT_LIST", "", $opt_left, $extra, 20);

    //内定者一覧リスト（右）
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move3('left','LEFT_LIST','RIGHT_LIST',1);\"";
    $arg["data"]["RIGHT_LIST"] = knjCreateCombo($objForm, "RIGHT_LIST", "", $opt_right, $extra, 20);

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
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJL327W");
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
