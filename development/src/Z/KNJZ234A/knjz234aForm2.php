<?php

require_once('for_php7.php');

class knjz234aForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz234aindex.php", "", "sel");

        if (!isset($model->warning)) {
            if ($model->cmd != "change" && $model->cmd != "init") {
                $Row = knjz234aQuery::getRow($model->chair_group_cd, $model);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd != 'init') {
            //学科、コース、学年名、科目名表示
            $NameArray = array();
            $query = knjz234aQuery::getTitleName($model);
            $NameArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["data"]["NAME_SET"]     = $NameArray["NAME_SET"];
            $arg["data"]["SUBCLASS_SET"] = $NameArray["SUBCLASS_SET"];
            $arg["data"]["SUBCLASSNAME"] = $NameArray["SUBCLASSNAME"];

            if ($model->Properties["useKoteiTestCd"] !== '1') {
                $arg["data"]["ITEMNAME_SET"] = $db->getOne(knjz234aQuery::getTestItemName($model));
            }
        }
        //タイトル設定
        makeTitle($arg, $db);

        //講座グループコードテキスト
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        if ($model->Properties["useKoteiTestCd"] === '1') {
            if (strlen($Row["CHAIR_GROUP_CD"]) > 3) {
                $Row["CHAIR_GROUP_CD"] = substr($Row["CHAIR_GROUP_CD"], 3, 3);
            }
            $arg["data"]["CHAIR_GROUP_CD"] = knjCreateTextBox($objForm, $Row["CHAIR_GROUP_CD"], "CHAIR_GROUP_CD", 3, 3, $extra);
            $arg["data"]["CHAIR_GCD_KETA"] = "3";
        } else {
            $keta = (strlen($model->Properties["KNJZ234A_GroupCdKeta"]) > 0) ? $model->Properties["KNJZ234A_GroupCdKeta"] : 6;
            $arg["data"]["CHAIR_GROUP_CD"] = knjCreateTextBox($objForm, $Row["CHAIR_GROUP_CD"], "CHAIR_GROUP_CD", $keta, $keta, $extra);
            $arg["data"]["CHAIR_GCD_KETA"] = $keta;
        }
        //講座グループ名称テキスト
        $extra = "";
        $arg["data"]["CHAIR_GROUP_NAME"] = knjCreateTextBox($objForm, $Row["CHAIR_GROUP_NAME"], "CHAIR_GROUP_NAME", 40, 60, $extra);
        $arg["data"]["CHAIR_GNAME_MOJI"] = "20";

        //講座グループ略称テキスト
        $extra = "";
        $arg["data"]["CHAIR_GROUP_ABBV"] = knjCreateTextBox($objForm, $Row["CHAIR_GROUP_ABBV"], "CHAIR_GROUP_ABBV", 40, 60, $extra);
        $arg["data"]["CHAIR_GABBV_MOJI"] = "20";

        //講座グループリストToリスト作成
        makeGroupDataList($objForm, $arg, $db, $model, "CHAIR", "0", 15);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);
        knjCreateHidden($objForm, "TARGET_SEME", $model->target_seme);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz234aindex.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjz234aForm2.html", $arg);
    }
}

//タイトル設定
function makeTitle(&$arg, $db)
{
    $arg["info"]    = array("LEFT_LIST"     => "対象講座コード",
                            "RIGHT_LIST"    => "講座コード一覧" );
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
    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, 1);
}

//科目リストToリスト作成
function makeGroupDataList(&$objForm, &$arg, $db, &$model, $name, $target, $size)
{
    $opt_left = $opt_right = array();

    if ($model->cmd != 'init') {
        $target_seme = ($model->target_seme == 9) ? $db->getOne(knjz234aQuery::getSemesterLast($model)) : $model->target_seme;
        $result = $db->query(knjz234aQuery::selectQuery($model->subclasscd, $model->chair_group_cd, $model->test_cd, $target_seme, $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["JOINCD"]) {
                if ($model->chair_group_cd != $row["CHAIR_GROUP_CD"] && $model->Properties["KNJZ234A_HideChairDuplicate"] == '1') {
                    //選択CDと、データCDが不一致＆登録済講座非表示
                } else {
                    $opt_left[]  = array("label" => $row["LABEL"],
                                         "value" => $row["VALUE"]);
                }
            } else {
                $opt_right[] = array("label" => $row["LABEL"],
                                     "value" => $row["VALUE"]);
            }
        }
        $result->free();
    }

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
    $objForm->ae(createHiddenAe("cmd"));                        //コマンド
    $objForm->ae(createHiddenAe("chairselect"));                //更新用模試マスタ
    $objForm->ae(createHiddenAe("TEST_CD", $model->test_cd));   //テスト種別
}

//テキスト作成
function createText(&$objForm, $data, $name, $size, $maxlength, $extra)
{
    $objForm->ae(array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlength,
                        "value"     => $data,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae(array("type"      => "select",
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
    $objForm->ae(array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ));
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
