<?php

require_once('for_php7.php');


class knjc171Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc171Form1", "POST", "knjc171index.php", "", "knjc171Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //日付
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $extra = "btn_submit('knjc171')";
        $arg["data"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $model->field["DATE"], "reload=true", $extra);

        //校時コンボ
        $query = knjc171Query::getNameMst('B001');
        $extra = "onchange=\"return btn_submit('knjc171');\"";
        makeCmb($objForm, $arg, $db, $query, "PERIOD", $model->field["PERIOD"], $extra, 1);

        //講座一覧
        $opt_chrcd = array();
        $query = knjc171Query::selectChairQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_chrcd[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();

        //右リスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt_chrcd));
        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //左リスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));
        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象選択ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        //対象取消ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //年度日付範囲
        $query = knjc171Query::getSemester9();
        $result = $db->query($query);
        $yearSdate = CTRL_DATE;
        $yearEdate = CTRL_DATE;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $yearSdate = $row["SDATE"];
            $yearEdate = $row["EDATE"];
        }
        $result->free();

        knjCreateHidden($objForm, "YEAR_SDATE", str_replace('-', '/', $yearSdate));
        knjCreateHidden($objForm, "YEAR_EDATE", str_replace('-', '/', $yearEdate));

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc171Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "STAFF", STAFFCD);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC171");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}

?>
