<?php

require_once('for_php7.php');

class knjz093aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz093aindex.php", "", "main");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //年度コンボ作成
        makeYear($objForm, $arg, $db, $model);

        //リストTOリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz093aForm1.html", $arg); 
    }
}

//年度コンボ作成
function makeYear(&$objForm, &$arg, $db, $model) {

    //年度コンボ
    $opt = array();
    $value_flg = false;
    $query = knjz093aQuery::selectYearQuery($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["YEAR"], 
                       "value" => $row["YEAR"]);
        if ($row["YEAR"] == $model->year) $value_flg = true;
    }
    $result->free();
    $model->year = ($model->year && $value_flg) ? $model->year : CTRL_YEAR;

    $extra = "onchange=\"return btn_submit('');\"";
    $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

    //追加年度
    $extra = "onblur=\"this.value=toInteger(this.value);\"";
    $arg["year_add"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

    //年度追加ボタン
    $extra = "onclick=\"return add('');\"";
    $arg["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    //出身学校年度一覧取得
    $opt_left = array();
    $query = knjz093aQuery::selestFinschoolYdat($model);
    $result = $db->query($query);   
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[]    = array("label" => $row["FINSCHOOLCD"]." ".substr($row["NAME1"], 0, 3)."：".$row["FINSCHOOL_NAME"],
                               "value" => $row["FINSCHOOLCD"]);
    }
    $result->free();

    //出身学校一覧取得
    $opt_right = array();
    $query = knjz093aQuery::selectFinschoolMst($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array("label" => $row["FINSCHOOLCD"]." ".substr($row["NAME1"], 0, 3)."：".$row["FINSCHOOL_NAME"],
                             "value" => $row["FINSCHOOLCD"]);
    }
    $result->free();

    //出身学校年度一覧
    $extra = "multiple STYLE=\"WIDTH:280px\" WIDTH=\"280px\" ondblclick=\"move('right','finschoolyear','finschoolmaster',1)\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "finschoolyear", "left", $opt_left, $extra, 20);

    //出身学校一覧
    $extra = "multiple STYLE=\"WIDTH:280px\" WIDTH=\"280px\" ondblclick=\"move('left','finschoolyear','finschoolmaster',1)\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "finschoolmaster", "left", $opt_right, $extra, 20);

    //追加ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','finschoolyear','finschoolmaster',1);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //追加ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left','finschoolyear','finschoolmaster',1);\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //削除ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right','finschoolyear','finschoolmaster',1);\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //削除ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','finschoolyear','finschoolmaster',1);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

    //項目名
    $arg["info"] = array("LEFT_LIST"  => "出身学校年度一覧",
                         "RIGHT_LIST" => "出身学校一覧");
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //出身学校マスタボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ093/knjz093index.php?mode=1&SEND_PRGID=KNJZ093A&cmd=&SEND_SUBMIT=1&SEND_AUTH=".AUTHORITY;
    $extra = "onclick=\"document.location.href='$link'\"";
    $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "btn_master", " 出身学校マスタ ", $extra);

    //保存ボタンを作成する
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更新", $extra);

    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取消", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

    //教育委員会出身学校取込ボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ093_FINSCHOOL_REFLECTION/knjz093_finschool_reflectionindex.php?cmd=&SEND_PRGID=KNJZ093A&SEND_SUBMIT=1&SEND_AUTH=".AUTHORITY;
    $extra = "onclick=\"document.location.href='$link'\"";
    $arg["button"]["BTN_TORIKOMI"] = knjCreateBtn($objForm, "btn_torikomi", "教育委員会出身学校取込", $extra);
}
?>
