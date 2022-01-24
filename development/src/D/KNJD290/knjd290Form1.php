<?php

require_once('for_php7.php');


class knjd290Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("knjd290Form1", "POST", "knjd290index.php", "", "knjd290Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //リストtoリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //カレンダーコントロール
        $value = isset($model->date) ? $model->date : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd290Form1.html", $arg);
    }
}

//項目一覧リストToリスト
function makeListToList(&$objForm, &$arg, $db, &$model) {

    $opt_left = $opt_right = $item = array();
    if (isset($model->selectdata)) {
        $item = explode(",", $model->selectdata);
    }

    $query = knjd290Query::getSelectGrade($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row['VALUE'], $item)) {
            $opt_left[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        } else {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
    }

    //書き出し項目一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "", $opt_left, $extra, 20);

    //項目一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "", $opt_right, $extra, 20);

    // <<ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    // ＜ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    // ＞ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    // >>ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //ＣＳＶ書出しボタンを作成する
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
    //閉じるボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][9]);
    knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][9]);
}

?>