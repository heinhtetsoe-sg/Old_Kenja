<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knjm290wForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjm290wForm1", "POST", "knjm290windex.php", "", "knjm290wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度
         $arg["data"]["YEAR"] = CTRL_YEAR;

        //クラス、科目選択
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onClick=\"return btn_submit('output');\"", "id=\"OUTPUT2\" onClick=\"return btn_submit('output');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["OUTPUT"] === '1') {
            $arg["AUTH"] = '1';
        } else {
            $arg["CLASSCD"] = '1';
        }
        
        //学期
        $opt = array();
        $query = knjm290wQuery::getSemeMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["GAKKI"] = ($model->field["GAKKI"]) ? $model->field["GAKKI"] : CTRL_SEMESTER;
        $extra = "onChange=\"return btn_submit('knjm290w');\"";
        $arg["data"]["GAKKI"] = knjCreateCombo($objForm, "GAKKI", $model->field["GAKKI"], $opt, $extra, 1);
        
        //クラス
        $query = knjm290wQuery::getAuth(CTRL_YEAR, $model->field["GAKKI"]);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('knjm290w');\"";
        $arg["data"]["GRADE_HR_CLASS"] = createCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //教科
        $opt = array();
        $query = knjm290wQuery::getClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["CLASSCD_SET"] = ($model->field["CLASSCD_SET"]) ? $model->field["CLASSCD_SET"] : $opt[0]["value"];;
        $extra = "onChange=\"return btn_submit('knjm290w');\"";
        $arg["data"]["CLASSCD_SET"] = knjCreateCombo($objForm, "CLASSCD_SET", $model->field["CLASSCD_SET"], $opt, $extra, 1);

        //生徒一覧 OR 科目一覧リスト
        $opt_right = array();
        $opt_left  = array();
        //ラジオボタン選択時にリストを初期化する
        if ($model->cmd == 'output') {
            $model->selectStudent = "";
            $model->selectStudentLabel = "";
        }
        $selectStudent = ($model->selectStudent != "") ? explode(",",$model->selectStudent) : array();
        $selectStudentLabel = ($model->selectStudentLabel != "") ? explode(",",$model->selectStudentLabel) : array();

        for ($i = 0; $i < get_count($selectStudent); $i++) {
            $opt_left[] = array('label' => $selectStudentLabel[$i],
                                'value' => $selectStudent[$i]);
        }
        
        if ($model->field["OUTPUT"] === '1') {
            $query = knjm290wQuery::getSchno($model, CTRL_YEAR, $model->field["GAKKI"]);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if (!in_array($row["SCHREGNO"], $selectStudent)) {
                    $opt_right[] = array('label' => $row["NAME"],
                                         'value' => $row["SCHREGNO"]);
                }
            }
            $result->free();
        } else {
            $query = knjm290wQuery::getSubclass($model, $model->field["CLASSCD_SET"]);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if (!in_array($row["SCHREGNO"], $selectStudent)) {
                    $opt_right[] = array('label' => $row["LABEL"],
                                         'value' => $row["VALUE"]);
                }
            }
            $result->free();
        }

        //生徒一覧 OR 科目一覧リスト
        $extra = "multiple style=\"width:250px; height:350px;\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = createCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px; height:350px;\" ondblclick=\"move1('right')\"";
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

        knjCreateHidden($objForm, "selectStudent");
        knjCreateHidden($objForm, "selectStudentLabel");

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm290wForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJM290W");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useRepStandarddateCourseDat", $model->Properties["useRepStandarddateCourseDat"]);
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
