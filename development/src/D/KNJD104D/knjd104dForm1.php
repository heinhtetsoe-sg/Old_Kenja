<?php

require_once('for_php7.php');

class knjd104dForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd104dForm1", "POST", "knjd104dindex.php", "", "knjd104dForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度を作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //カテゴリ区分ラジオボタン 1:生徒 2:年組
        $opt_shubetsu = array(1, 2);
        $model->field["CATEGORY_IS_CLASS"] = ($model->field["CATEGORY_IS_CLASS"] == "") ? "1" : $model->field["CATEGORY_IS_CLASS"];
        $click = " onclick =\" return btn_submit('knjd104d');\"";
        $extra = array("id=\"CATEGORY_IS_CLASS1\"".$click, "id=\"CATEGORY_IS_CLASS2\"".$click);
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('knjd104d');\"";
        $query = knjd104dQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //テスト種別コンボボックスを作成する
        $query = knjd104dQuery::getTestcd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTCD"], "TESTCD", "", 1);

        //学年コンボボックスを作成する
        $extra = "onChange=\"return btn_submit('knjd104d');\"";
        $query = knjd104dQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //カテゴリ区分により処理が変わる
        if ($model->field["CATEGORY_IS_CLASS"] == "2") {
            $arg["class"] = "ON";
            //クラスコンボボックスを作成する
            $extra = "onChange=\"return btn_submit('knjd104d');\"";
            $query = knjd104dQuery::getSelectHrclass($model);
            makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1);
        }

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //チェックボックスを作成する
        $check  = ($model->cmd == "" || $model->field["USE_GRAPH"] != "") ? "checked" : "";
        $check .= " id=\"USE_GRAPH\"";
        $arg["data"]["USE_GRAPH"] = knjCreateCheckBox($objForm, "USE_GRAPH", "1", $check, "");

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd104dForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

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
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }
        $result->free();
    }
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//出力対象一覧リストを作成する
function makeListToList(&$objForm, &$arg, $db, $model)
{
    if ($model->field["CATEGORY_IS_CLASS"] == "1") {
        $arg["PUTNAME"] = "クラス";
        $query = knjd104dQuery::getAuthClass($model);
    } else {
        $arg["PUTNAME"] = "生徒";
        $query = knjd104dQuery::getAuthStudent($model);
    }

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    makeCmb($objForm, $arg, $db, $query, $model->field["DUMMY"], "CATEGORY_NAME", $extra, 20);

    //出力対象リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    makeCmb($objForm, $arg, $db, "", $model->field["DUMMY"], "CATEGORY_SELECTED", $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //終了ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    //テスト名称
    $result = $db->query(knjd104dQuery::getTestcd($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $objForm->ae(createHiddenAe("TESTNAMES".$row["VALUE"], $row["LABEL"]));
    }
    $result->free();

    $objForm->ae(createHiddenAe("PRGID", "KNJD104D"));
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("TESTNAME"));
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
