<?php

require_once('for_php7.php');

class knje070hForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje070hForm1", "POST", "knje070hindex.php", "", "knje070hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //ラジオボタンを作成
        $opt = array(1, 2);
        if (!$model->field["OUTPUT"]) {
            $model->field["OUTPUT"] = "1";
        }
        $onclick = "onclick =\" return btn_submit('output');\"";
        $extra = array("id=\"OUTPUT1\" ".$onclick , "id=\"OUTPUT2\" ".$onclick);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //クラス選択コンボボックス
        $extra = "onchange=\"return btn_submit('knje070h'), AllClearList();\"";
        if ($model->field["OUTPUT"] == "1") {
            $query = knje070hQuery::getAuth($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, $model);
        } elseif ($model->field["OUTPUT"] == "2") {
            $query = knje070hQuery::getGradename($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, $model);
        }

        //年組のMAX文字数取得
        $max_len = 0;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
            $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $max_len);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje070hForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $max_len)
{

    if ($model->field["OUTPUT"] == "1") {
        $arg["KIND"] = "生徒";
    } elseif ($model->field["OUTPUT"] == "2") {
        $arg["KIND"] = "クラス";
    }
    $opt1 = array();
    //生徒一覧リストを作成する
    $query = knje070hQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->field["OUTPUT"] == "1") {
            //クラス名称調整
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
                $hr_name .= "&nbsp;";
            }
            if (!in_array($row["SCHREGNO"], $model->select_data["selectdata"])) {
                $opt1[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME"],
                               'value' => $row["VALUE"]);
            }
        } elseif ($model->field["OUTPUT"] == "2") {
            if (!in_array($row["VALUE"], $model->select_data["selectdata"])) {
                $opt1[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }
    }
    $result->free();
    //生徒一覧リスト
    $extra = "multiple style=\"width:280px; height: 320px; \" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //出力対象者リストを作成する
    $opt2 = array();
    if ($model->select_data["selectdata"][0]) {
        $query = knje070hQuery::getList($model, $model->select_data["selectdata"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["OUTPUT"] == "1") {
                //クラス名称調整
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $len = $zenkaku * 2 + $hankaku;
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
                    $hr_name .= "&nbsp;";
                }

                $opt2[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME"],
                               'value' => $row["VALUE"]);
            } elseif ($model->field["OUTPUT"] == "2") {
                if (in_array($row["VALUE"], $model->select_data["selectdata"])) {
                    $opt2[]= array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                }
            }
        }
        $result->free();
    }
    //出力対象者一覧リスト
    $extra = "multiple style=\"width:280px; height: 320px; \" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt2, $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE070H");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
