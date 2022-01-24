<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja142bForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("knja142bForm1", "POST", "knja142bindex.php", "", "knja142bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //発行日
        if (!isset($model->field["TERM_SDATE"])) {
            $model->field["TERM_SDATE"] = str_replace("-", "/", CTRL_DATE);
        }
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm, "TERM_SDATE", $model->field["TERM_SDATE"]);

        //有効期限
        $arg["showExpireDate"] = "1";
        $setYear = CTRL_YEAR + 3;
        $setDate = $setYear.'-03-31';
        if (!isset($model->field["TERM_EDATE"])) {
            $model->field["TERM_EDATE"] = str_replace("-", "/", $setDate);
        }
        $arg["data"]["TERM_EDATE"]=View::popUpCalendar($objForm, "TERM_EDATE", $model->field["TERM_EDATE"]);

        //開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
        $row = array(array('label' => "１行", 'value' => 1),
                     array('label' => "２行", 'value' => 2),
                     array('label' => "３行", 'value' => 3),
                     array('label' => "４行", 'value' => 4),
                     array('label' => "５行", 'value' => 5),
                    );

        $extra = "";
        $arg["data"]["POROW"] = knjCreateCombo($objForm, "POROW", $model->field["POROW"], $row, $extra, 1);
        //開始位置（列）コンボボックスを作成する////////////////////////////////////////////////////////////////////////
        $col = array(array('label' => "１列", 'value' => 1),
                     array('label' => "２列", 'value' => 2),
                    );
        $extra = "";
        $arg["data"]["POCOL"] = knjCreateCombo($objForm, "POCOL", $model->field["POCOL"], $col, $extra, 1);

        //クラス
        $query = knja142bQuery::getAuth($model, CTRL_YEAR, CTRL_SEMESTER);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) {
                $class_flg = true;
            }
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('knja142b');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //生徒一覧リスト
        $opt_right = array();
        $opt_left  = array();

        $selectStudent = ($model->selectStudent != "") ? explode(",", $model->selectStudent) : array();
        $selectStudentLabel = ($model->selectStudentLabel != "") ? explode(",", $model->selectStudentLabel) : array();

        for ($i = 0; $i < get_count($selectStudent); $i++) {
            $opt_left[] = array('label' => $selectStudentLabel[$i],
                                'value' => $selectStudent[$i]);
        }
        $query = knja142bQuery::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["VALUE"], $selectStudent)) {
                $opt_right[] = array('label' => $row["NAME"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        knjCreateHidden($objForm, "selectStudent");
        knjCreateHidden($objForm, "selectStudentLabel");

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja142bForm1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $db)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA142B");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "cmd");
}
