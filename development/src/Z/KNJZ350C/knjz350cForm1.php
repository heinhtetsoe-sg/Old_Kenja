<?php

require_once('for_php7.php');

class knjz350cForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz350cindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjz350cQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('change')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->school_kind, $extra, 1, "");
        } else {
            $model->school_kind = '00';
            if ($model->Properties["useSchool_KindField"] == "1") {
                $model->school_kind = SCHOOLKIND;
            }
        }

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        /**************/
        /*  対象選択  */
        /**************/
        $height = "70";
        //対象プログラムコンボ
        $extra = "onChange=\"btn_submit('change')\";";
        $query = knjz350cQuery::getProgramIdList($model);
        makeCmb($objForm, $arg, $db, $query, "PRG_ID", $model->field["PRG_ID"], $extra, 1);

        //対象グループコンボ
        $model->field["GROUPCD"] =($model->field["GROUPCD"])?$model->field["GROUPCD"]:'9999';
        $extra = "onChange=\"btn_submit('change')\";";
        $query = knjz350cQuery::getUserGroup($model);
        makeCmb($objForm, $arg, $db, $query, "GROUPCD", $model->field["GROUPCD"], $extra, 1);

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //対象課程学科コンボ
            $extra = "onChange=\"btn_submit('change')\";";
            $query = knjz350cQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
            $height = "100";
        }

        //サイズ調整
        $arg["HEIGHT"] = $height;

        //ATTEND_DIV
        $attend_div = $model->prgid[$model->field["PRG_ID"]];


        /******************/
        /*  出欠項目取得  */
        /******************/
        //名称マスタから項目名取得
        $nameMst = array();
        $query = knjz350cQuery::getAttendNameList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameMst[$row["NAMECD1"]."_".$row["NAMECD2"]] = $row["NAME1"];
        }

        //ADMIN_CONTROL_ATTEND_ITEMNAME_DATから項目名取得
        $itemName = array();
        $query = knjz350cQuery::getAdminControlAttendItemnameDat($model, $attend_div);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemName[$row["ATTEND_ITEM"]] = $row["ATTEND_ITEMNAME"];
        }

        //出欠項目一覧取得
        $itemS_array = $itemI_array = $itemR_array = array();
        $labelName = array();
        $itemList = $sep = "";
        foreach ($model->attendItem as $key => $array) {
            foreach ($array as $field => $val) {
                if ($val[0] != "0000" && !strlen($nameMst[$val[0]."_".$val[1]])) {
                    continue;
                }

                //前年度コピー用
                $itemList .= $sep.$field;
                $sep=",";

                $cd = ($attend_div == "1") ? "2" : "3";
                if ($val[$cd][0] != "1") {
                    continue;
                }

                $label = $val[$cd][1];
                if ($val[0] != "0000") {
                    $label = $nameMst[$val[0]."_".$val[1]];
                }
                if (strlen($itemName[$field])) {
                    $label = $itemName[$field];
                }

                //出欠表示項目
                if ($field != "KESSEKI") {
                    $itemS_array[] = array("key"    => $key,
                                          "label"   => $label,
                                          "value"   => $field);
                }

                //出欠入力可項目
                if ($val[0] != "0000" || $field == "LESSON") {
                    $itemI_array[] = array("key"    => $key,
                                          "label"   => $label,
                                          "value"   => $field);
                }

                if ($field == "KESSEKI") {
                    $cnt = 0;
                    if (isset($nameMst['C001_4'])) {
                        $cnt++;
                    }
                    if (isset($nameMst['C001_5'])) {
                        $cnt++;
                    }
                    if (isset($nameMst['C001_6'])) {
                        $cnt++;
                    }
                    if ($cnt > 1) {
                        //累積表示項目
                        $itemR_array[] = array("key"    => $key,
                                               "label"  => $label,
                                               "value"  => $field);
                    }
                } else {
                    //累積表示項目
                    $itemR_array[] = array("key"    => $key,
                                           "label"  => $label,
                                           "value"  => $field);
                }

                $labelName[$field] = sprintf("%02d", $key)."-".$label;
            }
        }
        knjCreateHidden($objForm, "itemList", $itemList);


        /******************/
        /*  出欠表示項目  */
        /******************/
        //(左)表示データ取得
        $opt_left1 = $opt_left1_id = array();
        $query = knjz350cQuery::getAdminFieldList($model, "1", $attend_div);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            list($no, $label) = explode('-', $labelName[$row["ATTEND_ITEM"]]);
            $opt_left1[]    = array("label" => $label,
                                    "value" => $no."-".$row["ATTEND_ITEM"]);

            $opt_left1_id[] = $row["ATTEND_ITEM"];
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
        $arg["main_part1"]["LEFT_PART"] = knjCreateCombo($objForm, "left_part1", "right", $opt_left1, $extra, 15);

        //非表示
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"moveList('left','left_part1','right_part1',0)\"";
        $arg["main_part1"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_part1", "left", $opt_right1, $extra, 15);

        //各種ボタン
        $arg["main_part1"]["SEL_ADD_ALL"]   = knjCreateBtn($objForm, "sel_add_all1", "≪", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_add_all','left_part1','right_part1',0);\"");
        $arg["main_part1"]["SEL_ADD"]       = knjCreateBtn($objForm, "sel_add1", "＜", "style=\"height:20px;width:40px\" onclick=\"return moveList('left','left_part1','right_part1',0);\"");
        $arg["main_part1"]["SEL_DEL"]       = knjCreateBtn($objForm, "sel_del1", "＞", "style=\"height:20px;width:40px\" onclick=\"return moveList('right','left_part1','right_part1','val');\"");
        $arg["main_part1"]["SEL_DEL_ALL"]   = knjCreateBtn($objForm, "sel_del_all1", "≫", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_del_all','left_part1','right_part1','val');\"");

        //更新ボタン
        $extra = "onclick=\"return doSubmit('selectdata1', 'left_part1', 'right_part1', 'update1');\"";
        $arg["main_part1"]["BTN_UPDATE"] = knjCreateBtn($objForm, "btn_keep1", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["main_part1"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear1", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["main_part1"]["BTN_END"] = knjCreateBtn($objForm, "btn_end1", "終 了", $extra);


        /********************/
        /*  出欠入力可項目  */
        /********************/
        $opt_left2 = $opt_right2 = array();
        $query = knjz350cQuery::getAdminFieldList($model, "1", $attend_div);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //入力不可項目
            if ($row["ATTEND_ITEM"] == "OFFDAYS" && $model->Properties["unUseOffdays"]) {
                continue;
            }
            if ($row["ATTEND_ITEM"] == "ABROAD" && $model->Properties["unUseAbroad"]) {
                continue;
            }
            if ($row["ATTEND_ITEM"] == "ABSENT" && $model->Properties["unUseAbsent"]) {
                continue;
            }

            foreach ($itemI_array as $key => $val) {
                if ($val["value"] == $row["ATTEND_ITEM"]) {
                    if ($row["INPUT_FLG"] == "1") {
                        $opt_left2[]    = array("label" => $val["label"],
                                                "value" => sprintf("%02d", $row["SHOWORDER"])."-".$row["ATTEND_ITEM"]);
                    } else {
                        $opt_right2[]   = array("label" => $val["label"],
                                                "value" => sprintf("%02d", $row["SHOWORDER"])."-".$row["ATTEND_ITEM"]);
                    }
                }
            }
        }
        $result->free();

        //入力可
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"moveList('right','left_part2','right_part2','val')\"";
        $arg["main_part2"]["LEFT_PART"] = knjCreateCombo($objForm, "left_part2", "right", $opt_left2, $extra, 15);

        //入力不可
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"moveList('left','left_part2','right_part2','val')\"";
        $arg["main_part2"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_part2", "left", $opt_right2, $extra, 15);

        //各種ボタン
        $arg["main_part2"]["SEL_ADD_ALL"]   = knjCreateBtn($objForm, "sel_add_all2", "≪", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_add_all','left_part2','right_part2','val');\"");
        $arg["main_part2"]["SEL_ADD"]       = knjCreateBtn($objForm, "sel_add2", "＜", "style=\"height:20px;width:40px\" onclick=\"return moveList('left','left_part2','right_part2','val');\"");
        $arg["main_part2"]["SEL_DEL"]       = knjCreateBtn($objForm, "sel_del2", "＞", "style=\"height:20px;width:40px\" onclick=\"return moveList('right','left_part2','right_part2','val');\"");
        $arg["main_part2"]["SEL_DEL_ALL"]   = knjCreateBtn($objForm, "sel_del_all2", "≫", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_del_all','left_part2','right_part2','val');\"");

        //更新ボタン
        $extra = "onclick=\"return doSubmit('selectdata2', 'left_part2', 'right_part2', 'update2');\"";
        $arg["main_part2"]["BTN_UPDATE"] = knjCreateBtn($objForm, "btn_keep2", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["main_part2"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear2", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["main_part2"]["BTN_END"] = knjCreateBtn($objForm, "btn_end2", "終 了", $extra);


        /******************/
        /*  累積表示項目  */
        /******************/
        if ($model->field["PRG_ID"] == "KNJC031F" || $model->field["PRG_ID"] == "KNJC035F") {
            //(左)表示データ取得
            $opt_left3 = $opt_left3_id = array();
            $query = knjz350cQuery::getAdminFieldList($model, "2", $attend_div);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                list($no, $label) = explode('-', $labelName[$row["ATTEND_ITEM"]]);
                $opt_left3[]    = array("label" => $label,
                                        "value" => $no."-".$row["ATTEND_ITEM"]);

                $opt_left3_id[] = $row["ATTEND_ITEM"];
            }
            $result->free();

            //(右)非表示データ取得
            $opt_right3 = array();
            foreach ($itemR_array as $key => $val) {
                if (!in_array($val["value"], $opt_left3_id)) {
                    $opt_right3[] = array("label" => $val["label"],
                                          "value" => sprintf("%02d", $val["key"])."-".$val["value"]);
                }
            }

            //表示
            $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"moveList('right','left_part3','right_part3','val')\"";
            $arg["main_part3"]["LEFT_PART"] = knjCreateCombo($objForm, "left_part3", "right", $opt_left3, $extra, 15);

            //非表示
            $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"moveList('left','left_part3','right_part3',0)\"";
            $arg["main_part3"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_part3", "left", $opt_right3, $extra, 15);

            //各種ボタン
            $arg["main_part3"]["SEL_ADD_ALL"]   = knjCreateBtn($objForm, "sel_add_all3", "≪", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_add_all','left_part3','right_part3',0);\"");
            $arg["main_part3"]["SEL_ADD"]       = knjCreateBtn($objForm, "sel_add3", "＜", "style=\"height:20px;width:40px\" onclick=\"return moveList('left','left_part3','right_part3',0);\"");
            $arg["main_part3"]["SEL_DEL"]       = knjCreateBtn($objForm, "sel_del3", "＞", "style=\"height:20px;width:40px\" onclick=\"return moveList('right','left_part3','right_part3','val');\"");
            $arg["main_part3"]["SEL_DEL_ALL"]   = knjCreateBtn($objForm, "sel_del_all3", "≫", "style=\"height:20px;width:40px\" onclick=\"return moveList('sel_del_all','left_part3','right_part3','val');\"");

            //更新ボタン
            $extra = "onclick=\"return doSubmit('selectdata3', 'left_part3', 'right_part3', 'update3');\"";
            $arg["main_part3"]["BTN_UPDATE"] = knjCreateBtn($objForm, "btn_keep3", "更 新", $extra);
            //取消ボタン
            $extra = "onclick=\"return btn_submit('clear');\"";
            $arg["main_part3"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear3", "取 消", $extra);
            //終了ボタン
            $extra = "onclick=\"closeWin();\"";
            $arg["main_part3"]["BTN_END"] = knjCreateBtn($objForm, "btn_end3", "終 了", $extra);
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata1");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectdata3");
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz350cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "blank") {
        $opt[] = array('label' => "", 'value' => "");
    }
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
