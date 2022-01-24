<?php

require_once('for_php7.php');

class knjd280aForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd280aForm1", "POST", "knjd280aindex.php", "", "knjd280aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $query = knjd280aQuery::getSemester();
        $result = $db->query($query);
        $semesters = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["SEMESTERNAME".$row["SEMESTER"]] = $row["SEMESTERNAME"];
            if ($row["SEMESTER"] == CTRL_SEMESTER) {
                $arg["data"]["SEMESTER"] = $row["SEMESTERNAME"];
            }
        }
        $result->free();

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //単位認定チェックボックスを作成
        $extra = $model->field["NINTEI"] == "1" ? "checked" : "";
        $extra .= " id=\"NINTEI\" onclick=\"kubun()\" ";
        $arg["data"]["NINTEI"] = knjCreateCheckBox($objForm, "NINTEI", "1", $extra, "");
        for ($i = 1; $i <= 2; $i++) {
            $extra = $model->field["NINTEI_SEME".$i] == "1" ? "checked" : "";
            $extra .= " id=\"NINTEI_SEME".$i."\" onclick=\"kubun()\"";
            $arg["data"]["NINTEI_SEME".$i] = knjCreateCheckBox($objForm, "NINTEI_SEME".$i, "1", $extra, "");
        }
        //仮評定チェックボックスを作成
        $extra = $model->field["KARI"] == "1" ? "checked" : "";
        $extra .= " id=\"KARI\" onclick=\"kubun()\"";
        $arg["data"]["KARI"] = knjCreateCheckBox($objForm, "KARI", "1", $extra, "");
        for ($i = 1; $i <= 2; $i++) {
            $extra = $model->field["KARI_SEME".$i] == "1" ? "checked" : "";
            $extra .= " id=\"KARI_SEME".$i."\" onclick=\"kubun()\"";
            $arg["data"]["KARI_SEME".$i] = knjCreateCheckBox($objForm, "KARI_SEME".$i, "1", $extra, "");
        }

        //出欠集計日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd280aForm1.html", $arg); 
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

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd280aQuery::getClass($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px; height:300px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px; height:300px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);
    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //履歴一覧
    $query = knjd280aQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["REGISTER_STAFFNAME"] = $row["STAFFNAME"];
        $row["HR_CLASS"] = knjd280aQuery::getHrName($db, $row["SELECT_HR_CLASS"]);
        $row["ATTEND_CALC_DATE"] = str_replace("-", "/", $row["ATTEND_CALC_DATE"]);
        $row["CHECK_NINTEI"] = strlen($row["APPROVE_FLG"]) ? "レ" : "";
        $row["CHECK_NINTEI_SEMESTER1"] = strlen($row["APPROVE_SEMESTER1_FLG"]) ? "レ" : "";
        $row["CHECK_NINTEI_SEMESTER2"] = strlen($row["APPROVE_SEMESTER2_FLG"]) ? "レ" : "";
        $row["CHECK_KARI"] = strlen($row["SET_PROV_FLG"]) ? "レ" : "";
        $row["CHECK_KARI_SEMESTER1"] = strlen($row["SET_PROV_SEMESTER1_FLG"]) ? "レ" : "";
        $row["CHECK_KARI_SEMESTER2"] = strlen($row["SET_PROV_SEMESTER2_FLG"]) ? "レ" : "";
        $arg["data"]["hist"][] = $row;
    }
    $result->free();
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["button"]["btn_exec"] = createBtn($objForm, "btn_exec", "実 行", "onclick=\"btn_submit('exec');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD280A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
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

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

?>
