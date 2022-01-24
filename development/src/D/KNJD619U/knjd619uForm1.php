<?php

require_once('for_php7.php');


class knjd619uForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd619uForm1", "POST", "knjd619uindex.php", "", "knjd619uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

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
        View::toHTML($model, "knjd619uForm1.html", $arg); 
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //各名称の文字数のMAX値取得
    $item = array("GRADE_NAME1", "MAJORNAME", "COURSECODENAME");
    $maxLen = array();
    $maxLen_name = array();
    $query = knjd619uQuery::getGradeCourseList($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach($item as $key) {
            $zenkaku = $hankaku = 0;
            $zenkaku = (strlen($row[$key]) - mb_strlen($row[$key])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row[$key]) - $zenkaku : mb_strlen($row[$key]);
            $maxLen[$key] = ($zenkaku * 2 + $hankaku > $maxLen[$key]) ? $zenkaku * 2 + $hankaku : $maxLen[$key];
        }
    }
    $result->free();

    //コース一覧リスト作成
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //名称の表示調整
        foreach($item as $key) {
            $row[$key] = adjustString($row[$key], $maxLen[$key]);
        }

        $opt[] = array("label" => $row["GRADE_NAME1"]."&nbsp;&nbsp;".
                                  $row["MAJORNAME"]."&nbsp;&nbsp;".
                                  $row["COURSECODENAME"], 
                       "value" => $row["VALUE"]);
    }
    $result->free();

    //コース一覧
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //出力対象コース
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

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

//表示の調整
function adjustString($string, $max_len) {
    $zenkaku = (strlen($string) - mb_strlen($string)) / 2;
    $hankaku = ($zenkaku > 0) ? mb_strlen($string) - $zenkaku : mb_strlen($string);
    $len = $zenkaku * 2 + $hankaku;
    $label = $string;
    for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $label .= "&nbsp;";

    return $label;
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷
    $extra = "onclick=\"return newwin('". SERVLET_URL ."');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJD619U");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND" , SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD" , SCHOOLCD);
}
?>
