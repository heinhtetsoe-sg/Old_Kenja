<?php

require_once('for_php7.php');

class knjd417mform1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd417mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('copy');\"" : "disabled";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //構成種類コンボ
        $query = knjd417mQuery::getCompositionTypeList();
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "KIND_NO", $model->KindNo, $extra, 1);
        //名称マスタ取得
        $query = knjd417mQuery::getNameMst("D090", $model->KindNo);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $kindName = $row["NAME1"];
        $nameSpare1 = $row["NAMESPARE1"];
        $maxKindNo = $row["ABBV3"];

        //構成種類タイプ
        knjCreateHidden($objForm, "KIND_TYPE", $nameSpare1);
        //最大項目数
        knjCreateHidden($objForm, "MAX_KIND_NO", $maxKindNo);

        //ヘッダ取得
        $query = knjd417mQuery::getHDKindNameHDat($model->KindNo);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //構成種類名テキスト
        if ($model->cmd == "" || $model->cmd == "change" || $model->cmd == "clear") {
            $model->kindName  = $row["KIND_NAME"];
            $model->kindNameFlg = "0";
        }
        $extra = "onchange=\"resetcolor(this);\" style=\"text-align: left;";
        //構成種類名が設定されていない場合は名称マスタの名称を設定
        if (!$model->kindName) {
            $extra .= " color: #FF20B0;";
            $model->kindName = $kindName;
            $model->kindNameFlg = "1";
        }
        $extra .= "\"";
        $arg["KIND_NAME"] = knjCreateTextBox($objForm, $model->kindName, "KIND_NAME", 32, 15, $extra);
        $arg["KIND_NAME_COMMENT"] = getTextAreaComment(15, 0);
        //名称マスタからの取得フラグ
        knjCreateHidden($objForm, "KIND_NAME_FLG", $model->kindNameFlg);

        //登録済データ取得
        $savedat = array();
        $query = knjd417mQuery::getHDKindNameDat($model, $model->KindNo);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_SEQ"] != '') {
                $savedat[$row["KIND_SEQ"]] = $row["KIND_REMARK"];
            }
        }

        //設定項目なし
        if (!$nameSpare1) {
            $arg["dispcomptype_p0"] = "1";
        }

        //コンボを変更したら項目数をリセット
        if ($model->cmd == "change" || $model->cmd == "clear") {
            unset($model->kindCnt);
        }
        //データ件数
        if ($model->kindCnt == "") {
            $model->kindCnt = (get_count($savedat) > 0) ? get_count($savedat) : "";
        }
        //項目数設定あり
        if ($nameSpare1 == "1") {
            $arg["dispcomptype_p1"] = "1";

            //項目数数テキスト
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["KIND_CNT"] = knjCreateTextBox($objForm, $model->kindCnt, "KIND_CNT", 3, 3, $extra);

            //項目数コメント
            $arg["KIND_CNT_COMMENT"] = "(半角数字1～{$maxKindNo})";

            //確定ボタン
            $extra = "onclick=\"return level(".$cnt.");\"";
            $arg["button"]["btn_comp"] = knjCreateBtn($objForm, "btn_comp", "確 定", $extra);
        }

        if ($nameSpare1 == "2") {
            //タイプ２の場合は固定で項目数3を設定
            $model->kindCnt = 3;
        }

        if ($nameSpare1 == "1" || $nameSpare1 == "2") {
            $arg["dispcomptype_p2"] = "1";
            knjCreateHidden($objForm, "HID_KIND_CNT", $model->kindCnt);

            //一覧表示
            $extra = "";
            for ($i = 1; $i <= $model->kindCnt; $i++) {
                $Row = array();
                $Row["KIND_LABEL"] = "目標".$i;
                if ($model->cmd == "" || $model->cmd == "change" || $model->cmd == "clear" || $model->cmd == "level") {
                    $Row["KIND_REMARK"] = knjCreateTextBox($objForm, $savedat[sprintf("%03d", $i)], "KIND_REMARK_".$i, 30, 30, $extra);
                } else {
                    $Row["KIND_REMARK"] = knjCreateTextBox($objForm, $model->field["KIND_REMARK_".$i], "KIND_REMARK_".$i, 30, 30, $extra);
                }
                $arg["KIND_REMARK_COMMENT"] = getTextAreaComment(10, 0);

                $arg["data"][] = $Row;
            }
        }

        //NAMESPARE1が"3"の時、
        if ($nameSpare1 == '3') {
            $arg["namespare1_is_3"] = "1";

            if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
                $arg["useGradeKindCompGroupSemester"] = "1";
                //学期コンボ
                $query = knjd417mQuery::getSemester($model);
                $extra = "onchange=\"return btn_submit('change');\"";
                makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, $model);
            }

            //学部コンボ
            $query = knjd417mQuery::getSchoolKind();
            $extra = "onchange=\"return btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind, $extra, 1, $model);

            //状態区分コンボ
            $query = knjd417mQuery::getCondition($model);
            $extra = "onchange=\"return btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, "CONDITION", $model->condition, $extra, 1, $model);

            //指導計画帳票パターンコンボ
            $model->guidance_pattern = "3";
            knjCreateHidden($objForm, "GUIDANCE_PATTERN", $model->guidance_pattern);

            //項目一覧作成
            //データ取得
            $query = knjd417mQuery::getHreportGuidanceItemNameDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $cnt = 1;
            foreach ($model->pattern as $key => $val) {
                $tmp = array();
                //連番
                $tmp["ITEM_LABEL"] = $val["LABEL"];
                //項目名テキスト
                if ($model->cmd == "check") $Row[$val["COLNUM_NAME"]] = $model->field[$val["COLNUM_NAME"]];
                $tmp["ITEM_REMARK"] = knjCreateTextBox($objForm, $Row[$val["COLNUM_NAME"]], $val["COLNUM_NAME"], 32, 30, "");
                $tmp["ITEM_REMARK_COMMENT"] = getTextAreaComment(15, 0);

                $arg["data3"][] = $tmp;
                $cnt++;
            }

            //総合所見
            $Row["KIND_LABEL"] = "項目名";
            if ($model->cmd == "" || $model->cmd == "change" || $model->cmd == "clear" || $model->cmd == "level") {
                $model->field["KIND_REMARK_1"] = $savedat["001"];
            }
            $extra = "";
            $Row["KIND_REMARK"] = knjCreateTextBox($objForm, $model->field["KIND_REMARK_1"], "KIND_REMARK_1", 30, 30, $extra);
            $arg["KIND_REMARK_COMMENT"] = getTextAreaComment(10, 0);
            knjCreateHidden($objForm, "HID_KIND_CNT", 1);

            $arg["data"][] = $Row;

        } else {
            $model->guidance_pattern = '';
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
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", $model->semester);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd417mForm1.html", $arg);
    }
}

// テキストボックス文字数
function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeCmbInList(&$objForm, &$arg, $db, $query, $name, $nameId, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $maxlabellen = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        $maxlabellen = $maxlabellen < strlen($row["LABEL"]) ? strlen($row["LABEL"]) : $maxlabellen;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $nameId, $value, $opt, $extra, $size);
    return $maxlabellen;
}
?>
