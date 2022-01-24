<?php

require_once('for_php7.php');


class knjd280cForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjd280cForm1", "POST", "knjd280cindex.php", "", "knjd280cForm1");

        //DB接続
        $db = Query::dbCheckOut();
        
        //ログイン年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        
        //学期
        $opt = array();
        $query = knjd280cQuery::getSemestername();
        $arg["data"]["GAKKI"] = $db->getOne($query);
        $model->field["GAKKI"] = CTRL_SEMESTER;
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        
        //教科
        $opt = array();
        $query = knjd280cQuery::getClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["CLASSCD_SET"] = ($model->field["CLASSCD_SET"]) ? $model->field["CLASSCD_SET"] : $opt[0]["value"];;
        $extra = "onChange=\"return btn_submit('knjd280c');\"";
        $arg["data"]["CLASSCD_SET"] = knjCreateCombo($objForm, "CLASSCD_SET", $model->field["CLASSCD_SET"], $opt, $extra, 1);

        //学科（平日、土曜コース）
        $opt = array();
        $query = knjd280cQuery::getMajor($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["MAJOR"] = ($model->field["MAJOR"]) ? $model->field["MAJOR"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjd280c');\"";
        $arg["data"]["MAJOR"] = knjCreateCombo($objForm, "MAJOR", $model->field["MAJOR"], $opt, $extra, 1);


        //科目一覧リスト
        $opt_right = array();
        $opt_left  = array();
        //学期選択時にリストを初期化する
        if ($model->cmd == 'output') {
            $model->select = "";
            $model->selectLabel = "";
        }
        //$select = ($model->select != "") ? explode(",",$model->select) : array();
        //$selectLabel = ($model->selectLabel != "") ? explode(",",$model->selectLabel) : array();
        $select = array();
        $selectLabel = array();

        for ($i = 0; $i < get_count($select); $i++) {
            $opt_left[] = array('label' => $selectLabel[$i],
                                'value' => $select[$i]);
        }
        
        $query = knjd280cQuery::getSubclass($model, $model->field["GAKKI"], $model->field["CLASSCD_SET"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["SCHREGNO"], $select)) {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //生徒一覧 OR 科目一覧リスト
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = createCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);

        knjCreateHidden($objForm, "select");
        knjCreateHidden($objForm, "selectLabel");

        //出欠集計日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd280cForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "PRGID", "KNJD280C");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "printSubclassLastChairStd", $model->Properties["printSubclassLastChairStd"]);

}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

?>
