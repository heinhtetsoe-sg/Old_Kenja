<?php

require_once('for_php7.php');

class knjd612Form1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd612Form1", "POST", "knjd612index.php", "", "knjd612Form1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd612Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //学年コンボ
        $query = knjd612Query::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('knjd612');\"", 1);

        //テスト種別コンボ
        $query = knjd612Query::getTestItem($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "STYLE=\"WIDTH:140\" ", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

//        //単位保留チェックボックス
//        makeCheckBox($objForm, $arg, $model, "OUTPUT4");
//
//        //空行チェックボックス
//        makeCheckBox($objForm, $arg, $model, "OUTPUT5");
//
//        //総合順位出力ラジオボタン 1:学級 2:学年 3:コース
//        $opt_rank = array(1, 2, 3);
//        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? "2" : $model->field["OUTPUT_RANK"];
//        $extra = "";
//        createRadio($objForm, $arg, "OUTPUT_RANK", $model->field["OUTPUT_RANK"], $extra, $opt_rank, get_count($opt_rank));
//
        //カレンダーコントロール
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : $model->control["学籍処理日"];
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd612Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd612Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

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

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name)
{
    $check = ($model->field[$name] == "1") ? "checked" : "";
    $value = isset($model->field[$name]) ? $model->field[$name] : 1;

    $arg["data"][$name] = createCheckBox($objForm, $name, $value, $check);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJD612"));
    $objForm->ae(createHiddenAe("cmd"));
    //学期
    $objForm->ae(createHiddenAe("SEME_DATE", $seme));
    //学期開始日付
    $objForm->ae(createHiddenAe("SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]));
    //学期終了日付
    $objForm->ae(createHiddenAe("SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]));
    //学期終了日付
    $objForm->ae(createHiddenAe("SEME_FLG", $semeflg));
    //年度
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    //ラジオ
    $objForm->ae(createHiddenAe("OUTPUT_RANK", "2"));
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

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"       => "checkbox",
                        "name"       => $name,
                        "extrahtml"  => $extra,    
                        "value"      => $value));

    return $objForm->ge($name);
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    $objForm->ae( array("type"      => "radio",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));
    for ($i = 1; $i <= $count; $i++) {
        $arg["data"][$name.$i] = $objForm->ge($name, $i);
    }
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

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
