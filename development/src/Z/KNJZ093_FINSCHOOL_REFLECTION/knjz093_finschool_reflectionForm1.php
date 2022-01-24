<?php

require_once('for_php7.php');

class knjz093_finschool_reflectionForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjz093_finschool_reflectionForm1", "POST", "knjz093_finschool_reflectionindex.php", "", "knjz093_finschool_reflectionForm1");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //校種コンボ
        $extra = "onChange=\"return btn_submit('changeType')\"";
        $query = knjz093_finschool_reflectionQuery::getNameMst('L019');
        makeCmb($objForm, $arg, $db, $query, "SELECT_FINSCHOOL_TYPE", $model->selectFinschoolType, $extra, 1, "ALL");

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $db2, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz093_finschool_reflectionForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $all)
{
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    if ($all == "ALL") {
        $opt[] = array('label' => '-- 全て --', 'value' => '99');
        if ($value == "99") {
            $value_flg = true;
        }
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $db2, $model)
{
    //学校の出身学校一覧取得
    $query = knjz093_finschool_reflectionQuery::selectQuery($model, "list");
    $finschool = $db->getCol($query);

    //教育委員会の出身学校一覧取得
    $opt_left = $opt_right = array();
    $query  = knjz093_finschool_reflectionQuery::selectQuery($model, "list");
    $result = $db2->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学校DBにあるものは、左リストに表示し、それ以外は、右リストに表示する。
        if (0 < get_count($finschool) && in_array($row["FINSCHOOLCD"], $finschool)) {
            $opt_left[]     = array("label" => $row["FINSCHOOLCD"]." ".substr($row["NAME1"], 0, 3)."：".$row["FINSCHOOL_NAME"],
                                    "value" => $row["FINSCHOOLCD"]);
        } else {
            $opt_right[]    = array("label" => $row["FINSCHOOLCD"]." ".substr($row["NAME1"], 0, 3)."：".$row["FINSCHOOL_NAME"],
                                    "value" => $row["FINSCHOOLCD"]);
        }
    }
    $result->free();

    //教育委員会）出身学校一覧リスト
    $extra = "multiple style=\"width:280px\" width=\"280px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //学校）出身学校一覧リスト
    $extra = "multiple style=\"width:280px\" width=\"280px\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "取 込", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //戻るボタン
    $link = REQUESTROOT."/Z/KNJZ093A/knjz093aindex.php?";
    $extra = "onclick=\"document.location.href='$link'\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
