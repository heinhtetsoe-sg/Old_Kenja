<?php

require_once('for_php7.php');


class knjh101bForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh101bForm1", "POST", "knjh101bindex.php", "", "knjh101bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["SEMESTER"] = $db->getOne(knjh101bQuery::getSemester("SEMESTERNAME"));

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, CTRL_SEMESTER);
        
        $model->field["BATSU_FROM"] = is_null($model->field["BATSU_FROM"]) ? $db->getOne(knjh101bQuery::getBatsuMAXMIN("MIN")) : $model->field["BATSU_FROM"];
        $model->field["BATSU_TO"] = is_null($model->field["BATSU_TO"]) ? $db->getOne(knjh101bQuery::getBatsuMAXMIN("MAX")) : $model->field["BATSU_TO"];
        
        //罰コンボ
        $extra = "id=\"BATSU_FROM\"";
        $query = knjh101bQuery::getBatsu();
        makeCmb($objForm, $arg, $db, $query, "BATSU_FROM", $model->field["BATSU_FROM"], $extra, 1);
        
        $extra = "id=\"BATSU_TO\"";
        $query = knjh101bQuery::getBatsu();
        makeCmb($objForm, $arg, $db, $query, "BATSU_TO", $model->field["BATSU_TO"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh101bForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model, $seme) {


    $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
    $arg["data"]["TITLE_RIGHT"] = "クラス一覧";

    //対象外の生徒取得
    $query = knjh101bQuery::getSchnoIdou($model, CTRL_SEMESTER);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //初期化
    $opt_right = $opt_left = array();

    //年組取得
    $query = knjh101bQuery::getHrClass(CTRL_YEAR, CTRL_SEMESTER, $model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //一覧リスト（右側）
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);
        
    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

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
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJH101B");
    knjCreateHidden($objForm, "cmd");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array ("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
