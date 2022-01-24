<?php

require_once('for_php7.php');

class knjc166aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjc166aindex.php", "", "main");
        $arg["IFRAME"] = View::setIframeJs();

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR."年度";

        //学年
        $query = knjc166aQuery::getGrade();
        $extra = "onChange=\"return btn_submit('main');\"";
        $model->gradeSchoolKindArray = makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");

        //実行履歴
        $query = knjc166aQuery::getHist();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["EXEC_TIME"] = str_replace("-", "/", $row["EXEC_TIME"]);
            $exeTimeArray = explode(".", $row["EXEC_TIME"]);
            list($exeDate, $exeTime) = explode(" ", $exeTimeArray[0]);
            $setData["HIST_DATE"] = $exeDate;
            $setData["HIST_TIME"] = $exeTime;

            $setData["HIST_GRADE"] = $row["GRADE_NAME1"];

            $setData["HIST_BASE_DATE"] = str_replace("-", "/", $row["BASE_DATE"]);
            $setData["HIST_HR_CLASS"] = $row["HR_NAME"];

            $arg["HIST"][] = $setData;
        }
        $result->free();

        //基準日
        $model->setDefDate = $model->setDefDate == "" ? str_replace("-", "/", CTRL_DATE) : $model->setDefDate;
        $model->base_date = $model->base_date ? $model->base_date : $model->setDefDate;
        $arg["data"]["BASE_DATE"] = View::popUpCalendar($objForm, "BASE_DATE", str_replace("-", "/", $model->base_date));

        //学年/年組ラジオボタン
        $opt = array(1, 2);
        $model->gradeOrHr = ($model->gradeOrHr == "") ? "1" : $model->gradeOrHr;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"GRADE_OR_HR{$val}\" onChange=\"return btn_submit('main2');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "GRADE_OR_HR", $model->gradeOrHr, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($model->gradeOrHr == 2) {
            $arg["SELHRCLASS"] = "1";
            makeListToList($objForm, $arg, $db, $model);
        }
        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SELECT_DATA");


        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc166aForm1.html", $arg);
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $opt_right = array();
    $opt_left = array();
    $query = knjc166aQuery::getHRClass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $model->selClass)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        } else {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //対象者一覧作成
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 10);

    //出力対象作成
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 10);

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

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    $retVal = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
        $retVal[$row["VALUE"]] = $row["RET_VAL"];
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
    return $retVal;
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('execute');\"");
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
