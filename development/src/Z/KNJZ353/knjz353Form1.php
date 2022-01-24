<?php

require_once('for_php7.php');

class knjz353Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz353index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        /**************/
        /*  対象選択  */
        /**************/
        $height = "70";
        //対象プログラムコンボ
        $extra = " onChange=\"btn_submit('change')\";";
        $query = knjz353Query::getProgramIdList($model);
        makeCmb($objForm, $arg, $db, $query, "PRG_ID", $model->field["PRG_ID"], $extra, 1);


        //サイズ調整
        $arg["HEIGHT"] = $height;

        //ATTEND_DIV
        $medexam_div = $model->prgid[$model->field["PRG_ID"]];

        /******************/
        /*  出欠項目取得  */
        /******************/

        //出欠項目一覧取得
        $itemS_array  = array();
        $labelName = array();
        $itemList = $sep = "";
        foreach ($model->medexamItem as $key => $array) {
            foreach ($array as $field => $val) {
                if ($val[0] != "0000" && !strlen($nameMst[$val[0]."_".$val[1]])) continue; 

                //前年度コピー用
                $itemList .= $sep.$field;
                $sep=",";

                $cd = ($medexam_div == "1") ? "2" : "3";
               
                if ($val[$cd][0] != "1") continue;

                $label = $val[$cd][1];

                $itemS_array[] = array("key"    => $key,
                                      "label"   => $label,
                                      "value"   => $field);

                $labelName[$field] = sprintf("%02d", $key)."-".$label;
            }
        }
        knjCreateHidden($objForm, "itemList", $itemList);


        /******************/
        /*  出欠表示項目  */
        /******************/
        //(左)表示データ取得

        $opt_left1 = $opt_left1_id = array();
        $query = knjz353Query::getAdminFieldList($model, $medexam_div);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            list ($no, $label) = explode('-', $labelName[$row["MEDEXAM_ITEM"]]);
            $opt_left1[]    = array("label" => $label,
                                    "value" => $no."-".$row["MEDEXAM_ITEM"]);

            $opt_left1_id[] = $row["MEDEXAM_ITEM"];
        }
        $result->free();

        //(右)非表示データ取得
        $opt_right1 = array();
        foreach ($itemS_array as $key => $val) {
            if (!in_array($val["value"], $opt_left1_id)) {
                $opt_right1[] = array("label" => $val["label"],
                                      "value" => sprintf("%02d", $val["key"])."-".$val["value"]);
            }
        }

        //表示
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"moveList('right','left_part1','right_part1','val')\"";
        $arg["main_part1"]["LEFT_PART"] = knjCreateCombo($objForm, "left_part1", "right", $opt_left1, $extra, 20);

        //非表示
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"moveList('left','left_part1','right_part1',0)\"";
        $arg["main_part1"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_part1", "left", $opt_right1, $extra, 20);

        //各種ボタン
        $arg["main_part1"]["SEL_ADD_ALL"]   = knjCreateBtn($objForm, "sel_add_all1",    "≪", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_add_all','left_part1','right_part1',0);\"");
        $arg["main_part1"]["SEL_ADD"]       = knjCreateBtn($objForm, "sel_add1",        "＜", "style=\"height:20px;width:40px\" onclick=\"return moveList('left','left_part1','right_part1',0);\"");
        $arg["main_part1"]["SEL_DEL"]       = knjCreateBtn($objForm, "sel_del1",        "＞", "style=\"height:20px;width:40px\" onclick=\"return moveList('right','left_part1','right_part1','val');\"");
        $arg["main_part1"]["SEL_DEL_ALL"]   = knjCreateBtn($objForm, "sel_del_all1",    "≫", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_del_all','left_part1','right_part1','val');\"");

        //更新ボタン
        $extra = "onclick=\"return doSubmit('left_part1', 'right_part1', 'update');\"";
        $arg["main_part1"]["BTN_UPDATE"] = knjCreateBtn($objForm, "btn_keep1", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["main_part1"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear1", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["main_part1"]["BTN_END"] = knjCreateBtn($objForm, "btn_end1", "終 了", $extra);


        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata1");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz353Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
