<?php

require_once('for_php7.php');

class knje444aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje444aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //教育委員会チェック
        $getFlg = $db->getOne(knje444aQuery::getNameMst());
        if ($getFlg !== '1') {
            $arg["jscript"] = "OnEdboardError();";
        }

        //出力対象年度コンボボックス
        $query = knje444aQuery::getYear();
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1, "BLANK");

        /********** リストtoリスト(学校) **********/

        $opt_right = $opt_left = $schoolcd = array();

        if ($model->cmd != "edit") {
            //保存したフィードを取得
            $query = knje444aQuery::getFieldSql($model, "02");
            $result = $db->query($query);
            $model->selectdata_l = "";
            $sep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->selectdata_l .= $sep.$row["FIELD_NAME"]; //学校コード
                $sep = ",";
            }
            $result->free();
        }

        if (isset($model->selectdata_l)) {
            $schoolcd = explode(",", $model->selectdata_l);
        }

        $result = $db->query(knje444aQuery::getSchoolList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $teishutsu = ($row["DATA_CNT"] > 0) ? "【済】" : "【未】";
            //出力対象学校一覧
            if (in_array($row["VALUE"], $schoolcd)) {
                $opt_left[] = array('label' => $teishutsu.$row["LABEL"], 'value' => $row["VALUE"]);
            //学校一覧
            } else {
                $opt_right[] = array('label' => $teishutsu.$row["LABEL"], 'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //出力対象学校一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('right', 'l')\"";
        $arg["main_part"]["LEFT_PART_L"] = knjCreateCombo($objForm, "left_select_l", "", $opt_left, $extra, 30);
        //学校一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('left', 'l')\"";
        $arg["main_part"]["RIGHT_PART_L"] = knjCreateCombo($objForm, "right_select_l", "", $opt_right, $extra, 30);

        //全て追加
        $extra = "onclick=\"moves('left', 'l');\"";
        $arg["main_part"]["SEL_ADD_ALL_L"] = knjCreateBtn($objForm, "sel_add_all_l", "≪", $extra);
        //追加
        $extra = "onclick=\"move1('left', 'l');\"";
        $arg["main_part"]["SEL_ADD_L"] = knjCreateBtn($objForm, "sel_add_l", "＜", $extra);
        //削除
        $extra = "onclick=\"move1('right', 'l');\"";
        $arg["main_part"]["SEL_DEL_L"] = knjCreateBtn($objForm, "sel_del_l", "＞", $extra);
        //全て削除
        $extra = "onclick=\"moves('right', 'l');\"";
        $arg["main_part"]["SEL_DEL_ALL_L"] = knjCreateBtn($objForm, "sel_del_all_l", "≫", $extra);

        /********** リストtoリスト(項目) **********/

        $opt_left = $opt_right = $item = $item_array = array();

        //項目一覧
        foreach ($model->item as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = array("order" => sprintf("%03d", $key), "label" => $label);
            }
        }

        if ($model->cmd != "edit") {
            //保存したフィードを取得
            $query = knje444aQuery::getFieldSql($model, "01");
            $result = $db->query($query);
            $model->selectdata_r = "";
            $sep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->selectdata_r .= $sep.$row["FIELD_NAME"]; //項目フィールド
                $sep = ",";
            }
            $result->free();
        }

        if (isset($model->selectdata_r)) {
            $item = explode(",", $model->selectdata_r);
        }

        //書き出し項目一覧
        if ($model->selectdata_r) {
            foreach ($item as $field) {
                $opt_left[] = array("label" => $item_array[$field]["label"], "value" => $item_array[$field]["order"]."-".$field);
            }
        }

        //項目一覧
        foreach ($model->item as $key => $val) {
            foreach ($val as $field => $label) {
                if (!in_array($field, $item)) {
                    $opt_right[] = array("label" => $item_array[$field]["label"], "value" => $item_array[$field]["order"]."-".$field);
                }
            }
        }

        //書き出し項目一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('right', 'r')\" ";
        $arg["main_part"]["LEFT_PART_R"] = knjCreateCombo($objForm, "left_select_r", "", $opt_left, $extra, 30);
        //項目一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('left', 'r')\" ";
        $arg["main_part"]["RIGHT_PART_R"] = knjCreateCombo($objForm, "right_select_r", "", $opt_right, $extra, 30);

        //全て追加
        $extra = "onclick=\"moves('left', 'r');\"";
        $arg["main_part"]["SEL_ADD_ALL_R"] = knjCreateBtn($objForm, "sel_add_all_r", "≪", $extra);
        //追加
        $extra = "onclick=\"move1('left', 'r');\"";
        $arg["main_part"]["SEL_ADD_R"] = knjCreateBtn($objForm, "sel_add_r", "＜", $extra);
        //削除
        $extra = "onclick=\"move1('right', 'r');\"";
        $arg["main_part"]["SEL_DEL_R"] = knjCreateBtn($objForm, "sel_del_r", "＞", $extra);
        //全て削除
        $extra = "onclick=\"moves('right', 'r');\"";
        $arg["main_part"]["SEL_DEL_ALL_R"] = knjCreateBtn($objForm, "sel_del_all_r", "≫", $extra);

        //CSVボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_CSV"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ書出し", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata_r");
        knjCreateHidden($objForm, "selectdata_l");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje444aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
