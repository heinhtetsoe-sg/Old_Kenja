<?php

require_once('for_php7.php');

class knjd416Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd416index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["HREPORT_GUIDANCE_KIND_NAME_HDAT_schoolOnly"] == '1') {
            $arg["buttonHyouji"] = '';
        } else {
            $arg["buttonHyouji"] = '1';
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //前年度からコピー後 画面タイプを初期表示
        if($model->cmd == 'copy_after'){
            $model->type = "0";
        }

        //画面タイプ
        $model->type = ($model->type == NULL) ? "0" : $model->type;

        //前年度データ件数
        $pre_year = CTRL_YEAR - 1;
        $preYear_cnt = $db->getOne(knjd416Query::getCopyData($pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        //今年度データ件数
        $this_year = CTRL_YEAR;
        $thisYear_cnt = $db->getOne(knjd416Query::getCopyData($this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //学年コンボ
        $query = knjd416Query::getGrade($model);
        $extra = "onChange=\"return btn_submit('edit', $model->type, '');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //校種取得
        $model->schoolKind = $db->getOne(knjd416Query::getSchoolKind($model->field["GRADE"]));

        //年組コンボ
        $query = knjd416Query::getHrClass($model);
        $extra = "onChange=\"return btn_submit('edit', $model->type, '');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //生徒コンボ
        $query = knjd416Query::getSchreg($model);
        $extra = "onChange=\"return btn_submit('edit', $model->type, '');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHREGNO", $model->field["SCHREGNO"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model->type, $db);

        //表示・非表示設定
        setOutPut($model->type, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "type");
        knjCreateHidden($objForm, "typeOld");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd416Form1.html", $arg);

    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    $opt_right = $opt_left = $kindNo = array();

    //対象指導一覧取得(左リスト)
    $query = knjd416Query::getHreportList($model, "left");

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        $kindNo[] = $row["LABEL"];
    }

    //指導一覧取得(右リスト)
    $query = knjd416Query::getHreportList($model, "right");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["LABEL"], $kindNo)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }

    //対象リスト
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["LEFT_SELECT"] = knjCreateCombo($objForm, "LEFT_SELECT", "", $opt_left, $extra, 20);

    //一覧リスト
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["RIGHT_SELECT"] = knjCreateCombo($objForm, "RIGHT_SELECT", "", $opt_right, $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$type, $db) {

    $query = knjd416Query::getHreportList($model, "count");
    $dataCnt = $db->getOne($query);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update', $type, $dataCnt);\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //学年別ボタン
    $extra = "onclick=\"return btn_submit('gakunen', $type, $dataCnt);\"";
    $arg["button"]["btn_gakunen"] = knjCreateBtn($objForm, "btn_gakunen", "学年別", $extra);
    //個人別ボタン
    $extra = "onclick=\"return btn_submit('kojin', $type, $dataCnt);\"";
    $arg["button"]["btn_kojin"] = knjCreateBtn($objForm, "btn_kojin", "個人別", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('back', $type, $dataCnt);\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻る", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//表示・非表示設定
function setOutPut(&$type, &$arg) {
    if($type == "0" || $type == ""){
        //基本
        $arg["data"]["GAKUNEN"]     = 0;    //学年コンボ       非表示
        $arg["data"]["KOJIN"]       = 0;    //年組・生徒コンボ 非表示
        $arg["data"]["BTN_GAKUNEN"] = 1;    //学年別ボタン       表示
        $arg["data"]["BTN_KOJIN"]   = 1;    //個人別ボタン       表示
        $arg["data"]["BACK"]        = 0;    //戻るボタン       非表示

    }else if ($type == "1"){
        //学年別
        $arg["data"]["GAKUNEN"]     = 1;    //学年コンボ         表示
        $arg["data"]["KOJIN"]       = 0;    //年組・生徒コンボ 非表示
        $arg["data"]["BTN_GAKUNEN"] = 0;    //学年別ボタン     非表示
        $arg["data"]["BTN_KOJIN"]   = 1;    //個人別ボタン       表示
        $arg["data"]["BACK"]        = 1;    //戻るボタン         表示

    }else if ($type == "2"){
        //個人別
        $arg["data"]["GAKUNEN"]     = 1;    //学年コンボ         表示
        $arg["data"]["KOJIN"]       = 1;    //年組・生徒コンボ   表示
        $arg["data"]["BTN_GAKUNEN"] = 0;    //学年別ボタン     非表示
        $arg["data"]["BTN_KOJIN"]   = 0;    //個人別ボタン     非表示
        $arg["data"]["BACK"]        = 1;    //戻るボタン         表示

    }
}
?>
