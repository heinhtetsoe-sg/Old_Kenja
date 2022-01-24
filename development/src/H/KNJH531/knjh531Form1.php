<?php
class knjh531Form1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh531Form1", "POST", "knjh531index.php", "", "knjh531Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $extra = "onchange=\"return btn_submit('changeYear');\"";
        $query = knjh531Query::getYear();
        makeCmb($objForm, $arg, $db, $model, $query, $model->field["YEAR"], "YEAR", $extra, 1);

        //学期コンボ
        $extra = "onchange=\"return btn_submit('knjh531');\"";
        $query = knjh531Query::getSemester($model);
        makeCmb($objForm, $arg, $db, $model, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjh531');\"";
        $query = knjh531Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $model, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //データ種別コンボ
        $extra = "onChange=\"return btn_submit('knjh531');\"";
        $query = knjh531Query::getProficiencyDiv($model);
        makeCmb($objForm, $arg, $db, $model, $query, $model->field["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1);

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh531Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $model, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }

        if ($name == "SEMESTER") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else if ($name == "CATEGORY_NAME" || $name == "CATEGORY_SELECTED") {
            $value = "";
        } else {
            if ($name == "YEAR") {
                $value = ($value && $value_flg) ? $value : CTRL_YEAR;
            } else {
                $value = ($value && $value_flg) ? $value : $opt[0]["value"];
            }
        }
        $result->free();
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

}

//出力対象一覧リストを作成する
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //一覧リストを作成する
    $query = knjh531Query::getRightList($model);
    $extra = "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    makeCmb($objForm, $arg, $db, $model, $query, $model->field["DUMMY"], "CATEGORY_NAME", $extra, 20);

    //登録済みリストを作成する
    $query = knjh531Query::getLeftList($model);
    $extra = "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    makeCmb($objForm, $arg, $db, $model, $query, $model->field["DUMMY"], "CATEGORY_SELECTED", $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //更新ボタン
    $extra = "onClick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了ボタン
    $extra = "onClick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJH531");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SELECT_DATA");
}

?>
