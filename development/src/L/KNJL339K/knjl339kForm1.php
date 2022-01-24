<?php

class knjl339kForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl339kForm1", "POST", "knjl339kindex.php", "", "knjl339kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //特別理由区分
        $opt = array();
        $value_flg = false;
        $query = knjl339kQuery::getSpecialReasonDiv($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->special_reason_div == $row["VALUE"]) $value_flg = true;

            if ($row["NAMESPARE1"] == '1') {
                $special_reason_div = $row["VALUE"];
            }
        }
        $model->special_reason_div = (strlen($model->special_reason_div) && $value_flg) ? $model->special_reason_div : $special_reason_div;
        $extra = "onChange=\"btn_submit('knjl339k')\"";
        $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);

        //一覧リスト作成する
        makeListToList($objForm, $arg, $db, $model);

        //中高判別フラグを作成する
        $jhflg = 0;
        $row = $db->getOne(knjl339kQuery::GetJorH());
        $jhflg = ($row == 1) ? 1 : 2;

        //対象ファイル
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
                                    "size"      => 2048000,
                                    "extrahtml" => "" ));
        $arg["data"]["FILE"] = $objForm->ge("FILE");

        //ヘッダ有無
        $extra = ($model->headercheck == "1") ? "checked" : "";
        $arg["data"]["HEADERCHECK"] = createCheckBox($objForm, "HEADERCHECK", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model, $jhflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl339kForm1.html", $arg); 
    }
}

//一覧リスト作成する
function makeListToList(&$objForm, &$arg, $db, &$model)
{
    $opt_right = array();
    $opt_left  = array();
    $tmp_left  = array();

    $query = knjl339kQuery::GetExamno($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if (in_array($row["VALUE"], $model->opt_csv)) {
            $tmp_left[$row["VALUE"]]  = array("label" => $row["LABEL"],
                                              "value" => $row["VALUE"]);
        } else {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
    }
    foreach ($model->opt_csv as $key => $val){
        if (is_array($tmp_left[$val])) {
            $opt_left[]  = array("label" => $tmp_left[$val]["label"],
                                 "value" => $tmp_left[$val]["value"]);
        }
    }
    $result->free();

    //受験者一覧リストを作成する
    $extra = "multiple style=\"width=200px\" width=\"200px\" ondblclick=\"move1('left')\"";
    $arg["data"]["DATA_NAME"] = createCombo($objForm, "DATA_NAME", $value, $opt_right, $extra, 20);

    //出力対象リストを作成する
    $extra = "multiple style=\"width=200px\" width=\"200px\" ondblclick=\"move1('right')\"";
    $arg["data"]["DATA_SELECTED"] = createCombo($objForm, "DATA_SELECTED", $value, $opt_left, $extra, 20);

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
function makeBtn(&$objForm, &$arg)
{
    //実行
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["btn_ok"] = createBtn($objForm, "btn_ok", "実 行", $extra);

    //テンプレート
    $extra = "onclick=\"return btn_submit('output');\"";
    $arg["button"]["btn_output"] = createBtn($objForm, "btn_output", "テンプレート", $extra);

    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
}

//hiddenを作成する
function makeHidden(&$objForm, $model, $jhflg)
{
    $objForm->ae(createHiddenAe("JHFLG", $jhflg));
    $objForm->ae(createHiddenAe("YEAR", $model->ObjYear));
    $objForm->ae(createHiddenAe("SORT", "1"));
    $objForm->ae(createHiddenAe("OUTTYPE", "5"));
    $objForm->ae(createHiddenAe("OUTPUTNAME", "on"));
    $objForm->ae(createHiddenAe("OUTPUTEXAM", "on"));
    $objForm->ae(createHiddenAe("SENGAN", "on"));
    $objForm->ae(createHiddenAe("HEIGAN", "on"));
    $objForm->ae(createHiddenAe("OUTPUT2", "5"));

    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJL339K"));
    $objForm->ae(createHiddenAe("cmd"));
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
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

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
