<?php

require_once('for_php7.php');

class knjd139mForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //１レコード取得
        $row = $db->getRow(knjd139mQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $row =& $model->field;
        }

        /******************/
        /* コンボボックス */
        /******************/
        //学期コンボ
        $query = knjd139mQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        if ($model->semester == '9') {
            $arg["semester9"]    = '1';
            $arg["no_semester9"] = '';
        } else {
            $arg["semester9"]    = '';
            $arg["no_semester9"] = '1';
        }

        /********************/
        /* テキストボックス */
        /********************/
        $setTextBoxArr = array();
        $setTextBoxArr[] = "TOTALSTUDYTIME";    // 総合的な学習の時間
        $setTextBoxArr[] = "ATTENDREC_REMARK";  // 出欠の備考
        $setTextBoxArr[] = "REMARK4_07_00";     // 評価（9学期のみ）
        $setTextBoxArr[] = "COMMUNICATION";     // 所見
        $outcnt = 1;
        foreach ($setTextBoxArr as $fieldName) {
            $moji = $model->getPro[$fieldName]["moji"];
            $gyou = $model->getPro[$fieldName]["gyou"];
            $extra = " id=\"{$fieldName}\" onkeyup=\"charCount(this.value, {$gyou}, ({$moji} * 2), true);\"";
            $arg[$fieldName] = knjCreateTextArea($objForm, $fieldName, $gyou, ($moji * 2), "soft", $extra, $row[$fieldName]);
            $arg[$fieldName."_COMMENT"] = "<font size=2, color=\"red\">(全角{$moji}文字X{$gyou}行まで)</font><span id=\"statusarea{$outcnt}\" style=\"color:blue\">残り文字数</span>";
            knjCreateHidden($objForm, "{$fieldName}_KETA", ($moji * 2));
            knjCreateHidden($objForm, "{$fieldName}_GYO", $gyou);
            KnjCreateHidden($objForm, "{$fieldName}_STAT", "statusarea".$outcnt);
            $outcnt++;
        }

        // 観点(凡例)
        $model->recordlist1 = array();
        $sep = "";
        $settxt = "(";
        $query = knjd139mQuery::getNameMst($model->exp_year, "D102");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $settxt .= $sep . $row["LABEL"];
            $model->recordlist1[] = $row["VALUE"];
            $sep = " ";
        }
        $settxt .= ")";
        $arg["TEXT_TITLE1"] = $settxt;
        knjCreateHidden($objForm, "HID_RECORDLIST1", implode(",", $model->recordlist1));

        // 観点
        $model->kantenArr = array();
        $query = knjd139mQuery::getKantenName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->kantenArr[$row["NAMECD2"]] = array('LABEL'    => $row["LABEL"],
                                                       'REMARK1'  => $row["REMARK1"]);
        }
        $setIndx = 1;
        foreach ($model->kantenArr as $nameCd2 => $valArr) {
            $setData = array();

            //観点（名称）
            $setData["LABEL"] = $valArr["LABEL"];

            //観点（データ）
            $extra = " class=\"REMARK1\" style=\"text-align:center\" onblur=\"checkRecord(this, '1');\" onKeyDown=\"nextGo(this, '{$setIndx}');\"";
            $setName = "REMARK1_".$nameCd2;
            $setData["REMARK1"] = knjCreateTextBox($objForm, $valArr["REMARK1"], $setName, 3, 1, $extra);

            $arg["kanten"][] = $setData;
            $setIndx++;
        }

        // 生活および行動の記録(凡例)
        $model->recordlist2 = array();
        $sep = "";
        $settxt = "(";
        $query = knjd139mQuery::getNameMst($model->exp_year, "D036");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $settxt .= $sep . $row["LABEL"];
            $model->recordlist2[] = $row["VALUE"];
            $sep = " ";
        }
        $settxt .= ")";
        $arg["TEXT_TITLE2"] = $settxt;
        knjCreateHidden($objForm, "HID_RECORDLIST2", implode(",", $model->recordlist2));

        // 生活および行動の記録
        $model->recordArr = array();
        $query = knjd139mQuery::getBehaviorSemesMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->recordArr[$row["CODE"]] = array("CODENAME"  => $row["CODENAME"],
                                                    "RECORD"    => $row["RECORD"]);
        }
        $setIndx = 1;
        foreach ($model->recordArr as $code => $valArr) {
            $setData = array();

            //行動の記録（名称）
            $setData["CODENAME"] = $valArr["CODENAME"];

            //行動の記録（データ）
            $extra = " class=\"RECORD\" style=\"text-align:center\" onblur=\"checkRecord(this, '2');\" onKeyDown=\"nextGo(this, '{$setIndx}');\"";
            $setName = "RECORD".$code;
            $setData["RECORD"] = knjCreateTextBox($objForm, $valArr["RECORD"], $setName, 3, 1, $extra);

            $arg["behav"][] = $setData;
            $setIndx++;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前後の生徒へ
        if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        } else {
            $extra = "disabled style=\"width:130px\"";
            $arg["button"]["btn_up_pre"]  = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);

        $disSansyo = ($model->schregno != '') ? '': ' disabled';
        //総合的な学習の時間ボタン
        $extra = "onclick=\"return btn_submit('totalStudy');\"";
        $arg["button"]["btn_totalStudy"] = knjCreateBtn($objForm, "btn_totalStudy", "総合学習参照", $extra.$disSansyo);

        //出欠備考参照ボタン
        $extra = "onclick=\"return btn_submit('attendRemark');\"";
        $arg["button"]["btn_attendRemark"] = knjCreateBtn($objForm, "btn_attendRemark", "出欠備考参照", $extra.$disSansyo);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent2(0);";
        } else if (get_count($model->warning) != 0 || $model->cmd == "reset") {
            $arg["next"] = "NextStudent2(1);";
        }
        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd139mForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

}
?>
