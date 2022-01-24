<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010dSubFormYourokuIkkatsuTorikomi
{
    public function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("formYourokuIkkatsuTorikomi", "POST", "knje010dindex.php", "", "formYourokuIkkatsuTorikomi");

        //DB接続
        $db = Query::dbCheckOut();

        //取込先パターン選択
        $opt_pattern = array(1, 2, 3);
        $extra = array("id=\"CHK_PATTERN1\"", "id=\"CHK_PATTERN2\"", "id=\"CHK_PATTERN3\"");
        $model->chkPattern = ($model->chkPattern=="") ? "1" : $model->chkPattern;
        $radioArray = knjCreateRadio($objForm, "CHK_PATTERN", $model->chkPattern, $extra, $opt_pattern, count($opt_pattern));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //対象学年
        $query = knje010dQuery::getGradeComb($model);
        $extra = "id=\"SELECT_GRADE\" ";
        makeCmb($objForm, $arg, $db, $query, "SELECT_GRADE", $model->selectGrade, $extra, 1, "", "ALL");

        /******************/
        /**リストToリスト**/
        /******************/
        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //取込ボタン
        $extra = "id=\"SELECT_GRADE\" onclick=\"return btn_submit('yourokuIkkatsuTorikomi_update');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdataYoroku");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010dSubFormYourokuIkkatsuTorikomi.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $all = "")
{
    $result = $db->query($query);
    $opt = array();
    $value_flg = false;

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    if ($all == "ALL") {
        $opt[] = array ("label" => "全て",
                        "value" => "ALL");
    }

    if ($value != "ALL") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //対象生徒一覧を配列に格納
    $selectdataYoroku = explode(",", $model->selectdataYoroku);

    $opt_left = $opt_right = array();
    $query = knje010dQuery::getSchList($model);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $hr_name = $row["HR_NAME"];
        if (in_array($row["VALUE"], $selectdataYoroku)) {
            $opt_left[] = array('label' => $row["SCHREGNO"]. "　".$hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["VALUE"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO"]. "　".$hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧（右）
    $extra = "multiple style=\"width:400px\" width=\"400px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //対象生徒一覧（左）
    $extra = "multiple style=\"width:400px\" width=\"400px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    // >> ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    // << ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＞ ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // ＜ ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}
?>
