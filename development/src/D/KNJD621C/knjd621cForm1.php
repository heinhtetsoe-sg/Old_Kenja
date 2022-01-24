<?php

require_once('for_php7.php');

class knjd621cForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd621cForm1", "POST", "knjd621cindex.php", "", "knjd621cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $extra = "onchange=\"return btn_submit('knjd621c');\"";
        $query = knjd621cQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //学年コンボ
        $query = knjd621cQuery::getGrade();
        $extra = "onchange=\"return btn_submit('knjd621c');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //データ種別コンボ
        $extra = "onChange=\"return btn_submit('knjd621c');\"";
        $query = knjd621cQuery::getDatadiv();
        makeCmb($objForm, $arg, $db, $query, $model->field["DATA_DIV"], "DATA_DIV", $extra, 1);

        //テスト名称コンボ
        $extra = "onchange=\"return btn_submit('knjd621c');\"";
        $query = knjd621cQuery::getMockcd($model->field["DATA_DIV"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["MOCKCD"], "MOCKCD", $extra, 1);

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //上位順位指定
        $value = ($model->field["RANK_RANGE"]) ? $model->field["RANK_RANGE"] : "20";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; ";
        $arg["data"]["RANK_RANGE"] = createText($objForm, "RANK_RANGE", $value, $extra, 4, 4);

        //段階値出力チェックボックス
        $extra  = ($model->field["ASSESS_LEVEL"] == "1") ? "checked" : "";
        $extra .= " id=\"ASSESS_LEVEL\"";
        $arg["data"]["ASSESS_LEVEL"] = knjCreateCheckBox($objForm, "ASSESS_LEVEL", "1", $extra, "");

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd621cForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
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

        if ($name == "CATEGORY_NAME") {
            $opt[] = array("label" => "999999:総合計", "value" => "999999");
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
    $query = knjd621cQuery::getSubclass($model);

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    makeCmb($objForm, $arg, $db, $query, $model->field["DUMMY"], "CATEGORY_NAME", $extra, 20);

    //出力対象リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
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
    //印刷ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //ＣＳＶ出力ボタン
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    $objForm->ae(createHiddenAe("PRGID", "KNJD621C"));
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    $objForm->ae(createHiddenAe("CTRL_SEMESTER", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
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

//テキスト作成
function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "extrahtml" => $extra,
                        "value"     => $value));
    return $objForm->ge($name);
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
