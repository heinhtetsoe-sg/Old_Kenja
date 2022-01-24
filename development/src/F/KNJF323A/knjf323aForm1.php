<?php

require_once('for_php7.php');


class knjf323aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("knjf323aForm1", "POST", "knjf323aindex.php", "", "knjf323aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //1:クラス,2:個人表示指定
        $opt_data = array(1, 2);
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "1" : $model->field["KUBUN"];
        $extra = array("id=\"KUBUN1\" onClick=\"btn_submit('knjf323a')\"", "id=\"KUBUN2\" onClick=\"btn_submit('knjf323a')\"");
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //リストToリスト作成
        makeItiran($objForm, $arg, $db, $model);

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf323aForm1.html", $arg);
    }
}

function makeItiran(&$objForm, &$arg, $db, &$model)
{
    $categorySelectedLabel = "";
    $categoryNameLabel = "";
    $rightList = $leftList = array();
    $query = "";
    if ($model->field["KUBUN"] == "1") {
        $categorySelectedLabel = "出力対象クラス";
        $categoryNameLabel = "クラス一覧";
        $query = knjf323aQuery::getHrClassList($model);
    } else {
        $categorySelectedLabel = "出力対象部活";
        $categoryNameLabel = "部活一覧";
        $query = knjf323aQuery::getClubList($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->cmd == "csv") {
            if (!in_array($row["VALUE"], $model->selectdata)) {
                $rightList[]= array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            } else {
                $leftList[]= array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
            }
        } else {
            $rightList[]= array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
    }
    $result->free();

    $arg["data"]["CATEGORY_SELECTED_LABEL"] = $categorySelectedLabel;
    $arg["data"]["CATEGORY_NAME_LABEL"] = $categoryNameLabel;

    //クラス一覧(右側)
    $extra = "multiple style=\"width:100%; height:340px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 33);

    //対象クラス一覧(左側)
    $extra = "multiple style=\"width:100%; height:340px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 33);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    $extra = "onclick=\"return btn_submit('csv')\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "CTRL_YEAR", $model->ctrlYear);
    knjCreateHidden($objForm, "CTRL_SEMESTER", $model->ctrlSemester);
    knjCreateHidden($objForm, "CTRL_DATE", $model->ctrlDate);
    knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJF323A");
}
