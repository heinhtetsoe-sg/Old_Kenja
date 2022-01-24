<?php

require_once('for_php7.php');

class knjd417nform1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd417nindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('copy');\"" : "disabled";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //帳票パターン（知的用・自立活動中心用）ラジオボタン 1:知的用 2:自立活動中心用
        $opt_pattern = array(1, 2);
        $click = " onclick=\"return btn_submit('change');\"";
        $extra = array("id=\"TYOUHYOU_PATTERN1\"".$click, "id=\"TYOUHYOU_PATTERN2\"".$click);
        $radioArray = knjCreateRadio($objForm, "TYOUHYOU_PATTERN", $model->tyouhyou_pattern, $extra, $opt_pattern, get_count($opt_pattern));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //構成種類コンボ
        $query = knjd417nQuery::getCompositionTypeList();
        $extra = "onchange=\"return btn_submit('change');\"";
        $maxlabellen = makeCmb($objForm, $arg, $db, $query, "COMPOSITION_TYPE", $model->compositiontype, $extra, 1);
        $maxlabellen = $maxlabellen < 3 ? 0 : $maxlabellen - 3;
        $maxlabellen = intval(ceil(3.0 * $maxlabellen / 2.0));
        $typechk = explode("-", $model->compositiontype);
        list($nameCd2, $nameSpare1, $nameSpare3) = explode("-", $model->compositiontype);

        if ($model->tyouhyou_pattern == "2") $nameCd2 = "30";

        $typechk_0_is_not_1_2_3_4_30 = "1";
        $typechk_0_is_1 = "";
        $disppattern_2 = "";
        $disppattern_3 = "";
        $disppattern_4 = "";
        $typechk_0_is_30 = "";
        $typechk_0_is_not_30 = "1";
        switch ($nameCd2) {
            case "01" :
                $typechk_0_is_not_1_2_3_4_30 = "";
                $typechk_0_is_1 = "1";
                break;
            case "02" :
                $typechk_0_is_not_1_2_3_4_30 = "";
                $typechk_0_is_2 = "1";
                break;
            case "03" :
                $typechk_0_is_not_1_2_3_4_30 = "";
                $typechk_0_is_3 = "1";
                break;
            case "04" :
                $typechk_0_is_not_1_2_3_4_30 = "";
                $typechk_0_is_4 = "1";
                break;
            case "30" :
                $typechk_0_is_not_1_2_3_4_30 = "";
                $typechk_0_is_30 = "1";
                $typechk_0_is_not_30 = "";
                break;
        }

        $typechk_0_flg =  $typechk_0_is_not_1_2_3_4_30 === "" ? "1" : "0"; 
        $arg["typechk_0_is_not_1_2_3_4_30"] = $typechk_0_is_not_1_2_3_4_30;
        $arg["typechk_0_is_1"] = $typechk_0_is_1;
        $arg["typechk_0_is_2"] = $typechk_0_is_2;
        $arg["typechk_0_is_3"] = $typechk_0_is_3;
        $arg["typechk_0_is_4"] = $typechk_0_is_4;
        $arg["typechk_0_is_30"] = $typechk_0_is_30;
        $arg["typechk_0_is_not_30"] = $typechk_0_is_not_30;

        //登録済データ取得
        $savedat = array();
        $savecnt = 0;
        $query = knjd417nQuery::getHDKindNameDat($model, $nameCd2);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $savedat["KIND_NAME"]      = $row["KIND_NAME"];
            if ($row["KIND_SEQ"] != '') {
                $savedat[$row["KIND_SEQ"]] = $row["KIND_REMARK"];
                $savecnt++;
            }
        }

        if ($typechk_0_flg === "1") {
            $field_cnt = 1;
            switch ($nameCd2) {
                case "01" :
                    $field_cnt = 8;
                    break;
                case "03" :
                    $field_cnt = 4;
                    break;
                case "30" :
                    $field_cnt = 12;
                    break;
            }
            $extra = "style=\"text-align: left;\"";
            for ($i = 1; $i <= $field_cnt; $i++) {
                $obj_nm = "KIND_NO_".intval($nameCd2)."_KIND_SEQ_".$i;
                $value = "";
                $seq = sprintf("%03d", $i);
                if ($model->cmd == "" || $model->cmd == "change" || $model->cmd == "change2" || $model->cmd == "reset" || $model->cmd == "level") {
                    if (isset($savedat[$seq])) $value = $savedat[$seq];
                } else {
                    if (isset($model->field[$obj_nm])) $value = $model->field[$obj_nm];
                }
                $maxlabellen = 20;
                switch ($nameCd2) {
                    case "01" :
                        if (intval($i) === 1 ||intval($i) === 2 ||intval($i) === 6 ||intval($i) === 7 ||intval($i) === 8) {
                            $maxlabellen = 30;
                        }
                        break;
                    case "04" :
                        $maxlabellen = 30;
                        break;
                    case "30" :
                        if (intval($i) === 1 ||intval($i) === 2) {
                            $maxlabellen = 30;
                        }
                        break;
                }
                $arg[$obj_nm] = knjCreateTextBox($objForm, $value, $obj_nm, $maxlabellen, $maxlabellen, $extra);
            }
        } else {
            //構成種類名テキスト
            $extra = "onchange=\"resetcolor(this);\" style=\"text-align: left;";
            //切り替わりのタイミングで設定する
            if ($model->cmd == "" || $model->cmd == "change" || $model->cmd == "change2" || $model->cmd == "reset" || $model->cmd == "clear") {
                $model->compositionname = $savedat["KIND_NAME"];
                $model->compositionname_inflg = "0";
            }
            if (($savedat["KIND_NAME"] === "" || $savedat["KIND_NAME"] === null) 
                && ($model->compositionname === "" || $model->compositionname === null)) {
                    if ($nameCd2 !== "") {
                        $query = knjd417nQuery::getCompositionTypeSmplList("D090", true, $nameCd2);
                        $rowx = $db->getRow($query);
                        $model->compositionname = $rowx["0"];
                        $extra .= " color: #FF20B0;\"";
                        $model->compositionname_inflg = "1";
                    } else {
                        $extra .= "\"";
                        $model->compositionname = "";
                    }
            } else {
                if ($model->compositionname_inflg == "1") {
                    $extra .= " color: #FF20B0;\"";
                } else {
                    $extra .= "\"";
                }
            }

            knjCreateHidden($objForm, "COMPOSITIONNAME_FLG", $model->compositionname_inflg);
            $arg["COMPOSITION_NAME"] = knjCreateTextBox($objForm, $model->compositionname, "COMPOSITION_NAME", $maxlabellen, $maxlabellen, $extra);

            if (get_count($typechk) > 0 && ($nameSpare1 == "1" || $nameSpare1 == "2")) {
                $arg["dispcomptype_p2"] = "1";

                $extra = "onclick=\"return btn_submit('capture');\"";
                $arg["button"]["btn_capture"] = knjCreateBtn($objForm, "btn_capture", "取込", $extra);

                //コンボを変更したら項目数をリセット
                if ($nameSpare1 != "2" && ($model->cmd == "change" || $model->cmd == "change2" || $model->cmd == "reset" || $model->cmd == "clear")) {
                    unset($model->compcnt);
                } else if ($nameSpare1 == "2") {
                    $model->compcnt = 3;
                }

                //データ件数
                if ($model->compcnt == "") {
                    $cnt = ($savecnt > 0) ? $savecnt : "";
                } else {
                    $cnt = $model->compcnt;
                }

                if ($nameSpare1 == "1") {
                    $arg["dispcomptype_p1"] = "1";
                    //項目数数テキスト
                    $model->compcnt = (($model->cmd != "level" && $model->cmd != "check") || $model->compcnt == "") ? $cnt : $model->compcnt;
                    $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
                    $arg["COMPCNT"] = knjCreateTextBox($objForm, $model->compcnt, "COMPCNT", 3, 3, $extra);

                    //確定ボタン
                    $extra = "onclick=\"return level(".$cnt.");\"";
                    $arg["button"]["btn_comp"] = knjCreateBtn($objForm, "btn_comp", "確 定", $extra);
                }

                //一覧表示
                $extra = "";
                for ($i = 1; $i <= $model->compcnt; $i++) {
                    $Row = array();
                    $Row["ASSESSMARK"] = "";
                    //注意：条件によって文字色を調整するため、styleの終了記号を”後付けしている”ので、注意。
                    //切り替わりのタイミングで設定する
                    if ($model->cmd == "" || $model->cmd == "change" || $model->cmd == "change2" || $model->cmd == "reset" || $model->cmd == "level") {
                        $extra = "id =\"ASSESSMARK_".$i."\" onchange=\"resetAssessLevelcolor(this, '".$i."');\" style=\"text-align: left;\"";
                        $Row["ASSESSLEVEL"] = knjCreateTextBox($objForm, $savedat[sprintf("%03d", $i)], "ASSESSLEVEL_".$i, 30, 30, $extra);
                        $model->field["ASSESSLEVEL_INFLG_".$i] = "0";
                    } else if ($model->cmd == "capture") {
                        //取込ボタン処理
                        $query = knjd417nQuery::getCompositionTypeSmplList($nameSpare3, true, $model->field["ASSESSMARK_".$i]);
                        $setdefstr = $db->getRow($query);
                        $extra = "id =\"ASSESSMARK_".$i."\" onchange=\"resetAssessLevelcolor(this, '".$i."');\" style=\"text-align: left; color: #FF20B0;\"";
                        $Row["ASSESSLEVEL"] = knjCreateTextBox($objForm, $setdefstr["0"], "ASSESSLEVEL_".$i, 30, 30, $extra);
                        $model->field["ASSESSLEVEL_INFLG_".$i] = "1";
                    } else {
                        if ($model->field["ASSESSLEVEL_INFLG_".$i] == "1") {
                            $extra = "id =\"ASSESSMARK_".$i."\" onchange=\"resetAssessLevelcolor(this, '".$i."');\" style=\"text-align: left; color: #FF20B0;\"";
                        } else {
                            $extra = "id =\"ASSESSMARK_".$i."\" onchange=\"resetAssessLevelcolor(this, '".$i."');\" style=\"text-align: left;\"";
                        }
                        $Row["ASSESSLEVEL"] = knjCreateTextBox($objForm, $model->field["ASSESSLEVEL_".$i], "ASSESSLEVEL_".$i, 30, 30, $extra);
                        $Row["ASSESSMARK"] = $model->field["ASSESSMARK_".$i];
                    }

                    $extra = "id =\"ASSESSMARK_".$i."\" ";
                    $query = knjd417nQuery::getCompositionTypeSmplList($nameSpare3, true);
                    makeCmbInList($objForm, $Row, $db, $query, "ASSESSMARK", "ASSESSMARK_".$i, sprintf("%02d", $i), $extra, 1);
                    $arg["data"][] = $Row;
                }
                //チェック用の件数を設定
                $query = knjd417nQuery::getCompositionTypeSmplList($nameSpare3, true);
                $cmbcnt = get_count($db->getCol($query));
                knjCreateHidden($objForm, "DEFSEL_CMBCNT", $cmbcnt);
            } else {
                unset($model->compcnt);
            }

            //NAMESPARE1が"3"の時、
            if ($nameSpare1 == '3') {
                $arg["namespare1_is_3"] = "1";

                if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
                    $arg["useGradeKindCompGroupSemester"] = "1";
                    //学期コンボ
                    $query = knjd417nQuery::getSemester($model);
                    $extra = "onchange=\"return btn_submit('change');\"";
                    makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, $model);
                }

                //学部コンボ
                $query = knjd417nQuery::getSchoolKind();
                $extra = "onchange=\"return btn_submit('change');\"";
                makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1, $model);

                //状態区分コンボ
                $query = knjd417nQuery::getCondition($model);
                $extra = "onchange=\"return btn_submit('change');\"";
                makeCmb($objForm, $arg, $db, $query, "CONDITION", $model->condition, $extra, 1, $model);

                //指導計画帳票パターンコンボ
                if ($model->Properties["unuseEduPlan_Group_GuidancePattern"] == "1" && $model->cmd == "change") {
                    //プロパティが立っていて、未設定の場合にはDBから取得する。
                    //※プロパティが立っているときは校種+状態区分でパターンは必ず1つ。
                    $query2 = knjd417nQuery::getDefaultGuidancePattern($model);
                    $model->guidance_pattern = $db->getOne($query2);
                }
                $query = knjd417nQuery::getGuidancePattern($model);
                $extra = "onchange=\"return btn_submit('change2');\"";
                makeCmb($objForm, $arg, $db, $query, "GUIDANCE_PATTERN", $model->guidance_pattern, $extra, 1, $model);

                //指導計画帳票パターン別設定
                $model->pattern = array();
                $query = knjd417nQuery::getPattern($model->guidance_pattern);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $model->pattern[$row["NAMECD2"]] = $row["NAME1"];
                }

                //項目一覧作成
                if ($model->guidance_pattern) {
                    //データ取得
                    $query = knjd417nQuery::getHreportGuidanceItemNameDat($model);
                    $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $cnt = 1;
                    $tmp = array();
                    foreach ($model->pattern as $key => $val) {
                        //連番
                        $tmp["ITEM"] = '項目'.$cnt;
                        //項目名テキスト
                        if ($model->cmd == "check") $Row["ITEM_REMARK".$key] = $model->field["ITEM_REMARK".$key];
                        if ($model->cmd == "set") $Row["ITEM_REMARK".$key] = $val;
                        $tmp["ITEM_REMARK"] = knjCreateTextBox($objForm, $Row["ITEM_REMARK".$key], "ITEM_REMARK".$key, 20, 30, "");
                        //初期値
                        $tmp["DEFAULT_REMARK"] = $val;

                        $arg["data3"][] = $tmp;
                        $cnt++;
                    }
                }
                //取込ボタン
                $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "style=\"font-size:8pt;\" onclick=\"return btn_submit('set');\"" : "style=\"font-size:8pt;\" disabled";
                $arg["button"]["btn_set"] = knjCreateBtn($objForm, "btn_set", "取込", $extra);

                //印刷ボタン
                $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
                $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
            } else {
                $model->guidance_pattern = '';
            }
        }

        //更新ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //Hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COMPVAL", $model->compcnt);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD418");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "TYPECHK_0_FLG", $typechk_0_flg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd417nForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $maxlabellen = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        $maxlabellen = $maxlabellen < strlen($row["LABEL"]) ? strlen($row["LABEL"]) : $maxlabellen;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $maxlabellen;
}

function makeCmbInList(&$objForm, &$arg, $db, $query, $name, $nameId, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $maxlabellen = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        $maxlabellen = $maxlabellen < strlen($row["LABEL"]) ? strlen($row["LABEL"]) : $maxlabellen;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $nameId, $value, $opt, $extra, $size);
    return $maxlabellen;
}
?>
