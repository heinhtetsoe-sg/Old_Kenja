<?php

require_once('for_php7.php');

class knjh334Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh334index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル設定
        makeTitle($arg);

        //グループコード
        $query = knjh334Query::getGropuName($model);
        $extra = "onChange=\"btn_submit('sel')\";";
        makeCmb($objForm, $arg, $db, $query, $model->groupcd, "GROUPCD", $extra);

        //模試グループリストToリスト作成
        makeGroupDataList($objForm, $arg, $db, $model, "MOCK", "0", 10);

        //職員目標値グループリストToリスト作成
        makeGroupDataList($objForm, $arg, $db, $model, "USER", "2", 10);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjh334index.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjh334Form2.html", $arg); 
    }
}

//タイトル設定
function makeTitle(&$arg)
{
    $arg["info"]    = array("LEFT_LIST"     => "グループリスト",
                            "RIGHT_LIST"    => "マスタリスト" );
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    if ($name == "OYEAR") {
        $value = ($value) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, 1);
}

//リストToリスト作成
function makeGroupDataList(&$objForm, &$arg, $db, &$model, $name, $target, $size)
{
    $opt_left = $opt_right = array();

    $result = $db->query(knjh334Query::selectQuery($model, $target));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["JOINCD"]) {
            $opt_left[]  = array("label" => $row["LABEL"], 
                                 "value" => $row["VALUE"]);
        } else {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //グループリスト
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right', '".$name."')\"";
    $arg["main_part"][$name."LEFT_PART"]   = createCombo($objForm, "L".$name, "right", $opt_left, $extra, $size);
    //マスタリスト
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left', '".$name."')\"";
    $arg["main_part"][$name."RIGHT_PART"]  = createCombo($objForm, "R".$name, "left", $opt_right, $extra, $size);
    //各種ボタン
    $arg["main_part"][$name."SEL_ADD_ALL"] = createBtn($objForm, $name."sel_add_all", "≪", "onclick=\"return moves('sel_add_all', '".$name."');\"");
    $arg["main_part"][$name."SEL_ADD"]     = createBtn($objForm, $name."sel_add", "＜", "onclick=\"return move('left', '".$name."');\"");
    $arg["main_part"][$name."SEL_DEL"]     = createBtn($objForm, $name."sel_del", "＞", "onclick=\"return move('right', '".$name."');\"");
    $arg["main_part"][$name."SEL_DEL_ALL"] = createBtn($objForm, $name."sel_del_all", "≫", "onclick=\"return moves('sel_del_all', '".$name."');\"");

}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    $link = REQUESTROOT."/H/KNJH335/knjh335index.php?PROGRAMID=KNJH334";
    //追加ボタン
    $arg["button"]["btn_master"] = createBtn($objForm, "btn_master", "マスタ登録", "onclick=\"masterForm('".$link."')\"");
    //追加ボタン
    $arg["button"]["btn_insert"] = createBtn($objForm, "btn_insert", "追 加", "onclick=\"return doSubmit('insert');\"");
    //更新ボタン
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更 新", "onclick=\"return doSubmit('update');\"");
    //削除ボタン
    $arg["button"]["btn_delete"] = createBtn($objForm, "btn_delete", "削 除", "onclick=\"return btn_submit('delete');\"");
    //取消ボタン
    $arg["button"]["btn_clear"] = createBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));                    //コマンド
    $objForm->ae(createHiddenAe("mockselect"));             //更新用模試マスタ
    $objForm->ae(createHiddenAe("userselect"));             //更新用目標値マスタ
    $objForm->ae(createHiddenAe("RYEAR", $model->ryear));   //参照年度
    $objForm->ae(createHiddenAe("OYEAR", $model->oyear));   //対象年度
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
