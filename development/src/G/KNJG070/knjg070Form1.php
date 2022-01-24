<?php

require_once('for_php7.php');

class knjg070Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjg070Form1", "POST", "knjg070index.php", "", "knjg070Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;
        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        /****************/
        /* ラジオボタン */
        /****************/
        //新入生  転入生・編入生
        $opt = array(1, 2); //1:新入生 2:転入生、編入生
        $model->field["SINNYU_TENNYU"] = ($model->field["SINNYU_TENNYU"] == "") ? "1" : $model->field["SINNYU_TENNYU"];
        $sinnyu_tennyu_extra_1 = "id=\"SINNYU_TENNYU1\" onclick=\"btn_submit('knjg070');\"";
        $sinnyu_tennyu_extra_2 = "id=\"SINNYU_TENNYU2\" onclick=\"btn_submit('knjg070');\"";
        $extra = array($sinnyu_tennyu_extra_1, $sinnyu_tennyu_extra_2);
        $radioArray = knjCreateRadio($objForm, "SINNYU_TENNYU", $model->field["SINNYU_TENNYU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* コンボ */
        /**********/
        //年度
        $query = knjg070Query::getYear();
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["YEAR"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["YEAR"] = ($model->field["YEAR"] && $value_flg) ? $model->field["YEAR"] : $opt[0]["value"];
        if ($model->field["SINNYU_TENNYU"] != 1) {
            $extra = "onChange=\"btn_submit('knjg070')\"";
        } else {
            $extra .= "";
        }
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        /********/
        /* 日付 */
        /********/
        if ($model->field["SINNYU_TENNYU"] != 2) {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        $arg["data"]["SDATE"]      = View::popUpCalendar2($objForm, "SDATE", $model->field["SDATE"], "", "", $disabled);
        $arg["data"]["EDATE"]      = View::popUpCalendar2($objForm, "EDATE", $model->field["EDATE"], "", "", $disabled);
        $arg["data"]["ENTRY_DATE"] = View::popUpCalendar2($objForm, "ENTRY_DATE", $model->field["ENTRY_DATE"], "", "", "");

        /******************/
        /* リストToリスト */
        /******************/
        if ($model->field["SINNYU_TENNYU"] == 2) {
            makeListToList($objForm, $arg, $db, $model);
        }

        /**********/
        /* ボタン */
        /**********/
        //検索
        $extra = "onclick=\"btn_submit('search');\"";
        if ($model->field["SINNYU_TENNYU"] != 2) {
            $extra .= " disabled";
        }
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
        //プレビュー/印刷
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJG070");
        knjCreateHidden($objForm, "STAFFCD",       STAFFCD);
        knjCreateHidden($objForm, "CHK_SDATE", $model->field["YEAR"] . '/04/01');
        knjCreateHidden($objForm, "CHK_EDATE", ($model->field["YEAR"] + 1) . '/03/31');

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg070Form1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//対象者一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象者一覧
    $opt = array();
    $query = knjg070Query::getCategoryName($model);
    if ($model->cmd == 'search') {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //対象者一覧作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //出力対象者作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
