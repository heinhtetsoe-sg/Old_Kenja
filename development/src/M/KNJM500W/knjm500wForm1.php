<?php

require_once('for_php7.php');


class knjm500wForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm500wForm1", "POST", "knjm500windex.php", "", "knjm500wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $opt = array();
        $query = knjm500wQuery::getSemeMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["GAKKI"] = ($model->field["GAKKI"]) ? $model->field["GAKKI"] : CTRL_SEMESTER;
        $extra = "onChange=\"return btn_submit('knjm500w');\"";
        $arg["data"]["GAKKI"] = knjCreateCombo($objForm, "GAKKI", $model->field["GAKKI"], $opt, $extra, 1);

        //クラス選択コンボボックスを作成する
        $query = knjm500wQuery::getAuth(CTRL_YEAR, $model->field["GAKKI"]);
        $extra = "onchange=\"return btn_submit('clschange'),AllClearList();\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //リストを作成する
        $opt1 = array();
        $opt_left = array();
        $selectleft = explode(",", $model->selectleft);
        $query = knjm500wQuery::getSchreg($model);

        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["SCHREGNO"]] = array('label' => $row["NAME"], 
                                                         'value' => $row["SCHREGNO"]);
            if ($model->cmd == 'read' ) {
                if (!in_array($row["SCHREGNO"], $selectleft)){
                    $opt1[]= array('label' =>  $row["NAME"],
                                   'value' => $row["SCHREGNO"]);
                }
            } else {
                $opt1[]= array('label' =>  $row["NAME"],
                               'value' => $row["SCHREGNO"]);
            }
        //左リストで選択されたものを再セット
        }
        if ($model->cmd == 'read' ) {
            foreach ($model->select_opt as $key => $val) {
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }
        $result->free();

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);
        //対象者リストを作成する
        $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

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

        //集計基準日付データ
        if ($model->field["KIJUN"] == "") $model->field["KIJUN"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["KIJUN"] = View::popUpCalendar($objForm    ,"KIJUN"    ,str_replace("-","/",$model->field["KIJUN"]));

        //学期コンボ
        $query = knjm500wQuery::getSemeMst();
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $model->field["TAKESEMES"], "TAKESEMES", $extra, 1);

        //表紙印刷チェックボックス
        $extra = " id=\"HYOUSHI_PRINT\" ";
        $checked = $model->field["HYOUSHI_PRINT"] == '1' ? " checked " : "";
        $arg["data"]["HYOUSHI_PRINT"] = knjCreateCheckBox($objForm, "HYOUSHI_PRINT", "1", $checked.$extra);

        //送付先ラジオ
        $opt = array(1, 2);
        $model->field["SEND_DIV"] = ($model->field["SEND_DIV"] == "") ? "1" : $model->field["SEND_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SEND_DIV{$val}\" @");
        }
        $radioArray = knjCreateRadio($objForm, "SEND_DIV", $model->field["SEND_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //チェックボックス
        $extra = " id=\"HOGOSYA_MONGON\" ";
        $checked = $model->field["HOGOSYA_MONGON"] == '1' ? " checked " : "";
        $arg["data"]["HOGOSYA_MONGON"] = knjCreateCheckBox($objForm, "HOGOSYA_MONGON", "1", $checked.$extra);

        //コメントデータ取得
        if ($model->cmd != 'clschange') {

            $query = knjm500wQuery::getComment();

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->field["COMMENT".$row["REMARKID"]] = $row["REMARK"];
            }
            $result->free();
        }

        //コメントデータ
        $arg["data"]["COMMENT1"] = knjCreateTextBox($objForm, $model->field["COMMENT1"], "COMMENT1", 70, 70, "");
        $arg["data"]["COMMENT2"] = knjCreateTextBox($objForm, $model->field["COMMENT2"], "COMMENT2", 70, 70, "");
        $arg["data"]["COMMENT3"] = knjCreateTextBox($objForm, $model->field["COMMENT3"], "COMMENT3", 70, 70, "");
        $arg["data"]["COMMENT4"] = knjCreateTextBox($objForm, $model->field["COMMENT4"], "COMMENT4", 70, 70, "");
        $arg["data"]["COMMENT5"] = knjCreateTextBox($objForm, $model->field["COMMENT5"], "COMMENT5", 70, 70, "");
        $arg["data"]["COMMENT6"] = knjCreateTextBox($objForm, $model->field["COMMENT6"], "COMMENT6", 70, 70, "");

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'read'){
            $model->cmd = 'knjm500w';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm500wForm1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    $dataFlg = false;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        $dataFlg = $value == $row["VALUE"] ? true : $dataFlg;
    }
    $result->free();

    $value = ($value) && $dataFlg ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onClick=\"btn_submit('update');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷／更新", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, &$arg, $model) {
    $arg["TOP"]["YEAR"] = knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    $arg["TOP"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    $arg["TOP"]["LOGIN_DATE"] = knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    $arg["TOP"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJM500W");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectleft");
    $arg["TOP"]["useCurriculumcd"] = knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}

?>
