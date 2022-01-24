<?php

require_once('for_php7.php');

class knjl410_1Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl410_1Form1", "POST", "knjl410_1index.php", "", "knjl410_1Form1");

        //セキュリティーチェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //イベント参加者データ取得
        $info = $db->getRow(knjl410_1Query::getRecruitDat($model), DB_FETCHMODE_ASSOC);

        //ヘッダー情報（年度、管理番号、氏名）
        $arg["HEADER_INFO"] = (CTRL_YEAR + 1).'年度　　'.$info["RECRUIT_NO"].'：'.$info["NAME"];

        //編集用データ取得
        if (isset($model->recruit_no) && !isset($model->warning)) {
            $Row = $db->getRow(knjl410_1Query::getRecruitVisitDat($model, "1"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //以下、入力項目

        //1.RECRUIT_VISIT_DAT

        //登録日付
        $value = ($Row["TOUROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOUROKU_DATE"]);
        $arg["data1"]["TOUROKU_DATE"] = View::popUpCalendar($objForm, "TOUROKU_DATE", $value);

        //確定日付
        $value = ($Row["KAKUTEI_DATE"] == "") ? "" : str_replace("-", "/", $Row["KAKUTEI_DATE"]);
        $arg["data1"]["KAKUTEI_DATE"] = View::popUpCalendar($objForm, "KAKUTEI_DATE", $value);

        //希望コース
        $query = knjl410_1Query::getHopeCourse();
        makeCmb($objForm, $arg, $db, $query, "HOPE_COURSE", $Row["HOPE_COURSE"], "", 1, "BLANK");

        //受験種別
        $query = knjl410_1Query::getTestdiv();
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $Row["TESTDIV"], "", 1, "BLANK");

        //担当・・・登録データがない時、ログイン者
        $Row["STAFFCD"] = ($Row["STAFFCD"] == "") ? STAFFCD : $Row["STAFFCD"];
        $query = knjl410_1Query::getStaffMst($model);
        makeCmb($objForm, $arg, $db, $query, "STAFFCD", $Row["STAFFCD"], "", 1, "BLANK");

        //備考
        $extra = "onkeyup =\"charCount(this.value, 3, (20 * 2), true);\" oncontextmenu =\"charCount(this.value, 3, (20 * 2), true);\"";
        $arg["data1"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", "3", "40", "wrap", $extra, $Row["REMARK1"]);

        //特待生
        $query = knjl410_1Query::getJudgeKind();
        makeCmb($objForm, $arg, $db, $query, "JUDGE_KIND", $Row["JUDGE_KIND"], "", 1, "BLANK");

        //志望校
        $query = knjl410_1Query::getNameMst('L015');
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_DIV", $Row["SCHOOL_DIV"], "", 1, "BLANK");

        //志望校名
        $extra = "";
        $arg["data1"]["SCHOOL_NAME"] = knjCreateTextBox($objForm, $Row["SCHOOL_NAME"], "SCHOOL_NAME", 51, 50, $extra);

        //2.RECRUIT_VISIT_SCORE_DAT
        //入力確認チェック
        $extra = strlen($Row["SCORE_CHK"]) ? "checked" : "";
        $arg["data1"]["SCORE_CHK"] = knjCreateCheckBox($objForm, "SCORE_CHK", "1", $extra);
        //教科・科目を取得
        $nameL008 = array();
        $query = knjl410_1Query::getNameMstL008();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameL008[$row["VALUE"]] = $row["LABEL"];

            knjCreateHidden($objForm, "FLG3"."_".$row["VALUE"]."_S", $row["FLG3"]);
            knjCreateHidden($objForm, "FLG5"."_".$row["VALUE"]."_S", $row["FLG5"]);
            knjCreateHidden($objForm, "FLG9"."_".$row["VALUE"]."_S", "1");
        }
        $result->free();
        //学期名一覧
        $sem_name = array("1" => "1学期（前期）", "2" => "2学期（後期）");
        //通知票評定
        for ($sem = 1; $sem <= 2; $sem++) {
            //学期名
            $arg["data1"]["SEMESTER"."_S_{$sem}"] = $sem_name[$sem];
            //教科・科目
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
            for ($s = 1; $s <= 9; $s++) {
                $sub = sprintf("%02d", $s);
                //教科・科目名
                $arg["data1"]["SUBCLASSNAME{$sub}"."_S_{$sem}"] = $nameL008[$sub];
                $name = "SUBCLASSCD{$sub}"."_S_{$sem}";
                $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 2, 2, $extra);
            }
            //合計
            $name3 = "TOTAL3"."_S_{$sem}";
            $name5 = "TOTAL5"."_S_{$sem}";
            $name9 = "TOTAL9"."_S_{$sem}";
            $arg["data1"][$name3] = knjCreateTextBox($objForm, $Row[$name3], $name3, 2, 2, $extra);
            $arg["data1"][$name5] = knjCreateTextBox($objForm, $Row[$name5], $name5, 2, 2, $extra);
            $arg["data1"][$name9] = knjCreateTextBox($objForm, $Row[$name9], $name9, 2, 2, $extra);
            //選択チェック
            $name = "SELECT_DIV"."_S_{$sem}";
            $extra = strlen($Row[$name]) ? "checked" : "";
            $arg["data1"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra);

            //算出ボタン
            $extra = "onclick=\"keisanScore('{$sem}');\"";
            $arg["button"]["btn_keisanScore{$sem}"] = knjCreateBtn($objForm, "btn_keisanScore{$sem}", "算出", $extra);
        }

        //3.RECRUIT_VISIT_MOCK_DAT
        //入力確認チェック
        $extra = strlen($Row["MOCK_CHK"]) ? "checked" : "";
        $arg["data1"]["MOCK_CHK"] = knjCreateCheckBox($objForm, "MOCK_CHK", "1", $extra);
        //模試偏差値
        for ($i = 4; $i <= 12; $i++) {
            $mon = sprintf("%02d", $i);
            //月名
            $arg["data1"]["MONTH"."_M_{$mon}"] = $i . "月";
            //教科・科目
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value);\"";
            for ($s = 1; $s <= 5; $s++) {
                $sub = sprintf("%02d", $s);
                $name = "SUBCLASSCD{$sub}"."_M_{$mon}";
                $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 5, 5, $extra);
            }
            //平均
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value);\"";
            $name3 = "AVG3"."_M_{$mon}";
            $name5 = "AVG5"."_M_{$mon}";
            $arg["data1"][$name3] = knjCreateTextBox($objForm, $Row[$name3], $name3, 5, 5, $extra);
            $arg["data1"][$name5] = knjCreateTextBox($objForm, $Row[$name5], $name5, 5, 5, $extra);

            //算出ボタン
            $extra = "onclick=\"keisanMock('{$mon}');\"";
            $arg["button"]["btn_keisanMock{$mon}"] = knjCreateBtn($objForm, "btn_keisanMock{$mon}", "算出", $extra);

            //模試名コンボ
            $extra = "onchange=\"disCompanyText(this);\"";
            $name = "COMPANYCD"."_M_{$mon}";
            $query = knjl410_1Query::getNameMstL406();
            makeCmb($objForm, $arg, $db, $query, $name, $Row[$name], $extra, 1, "BLANK");
            //模試名テキスト
            $extra = ($Row[$name] == "00009999") ? "" : "disabled";
            $name = "COMPANY_TEXT"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 21, 40, $extra);
        }
        for ($i = 99; $i <= 99; $i++) {
            $mon = sprintf("%02d", $i);
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value);\"";
            //TOP2（同一月不可）
            //TOP1
            $name = "TOP1_AVG3"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 5, 5, $extra);
            //$arg["data1"][$name] = $Row[$name];
            //knjCreateHidden($objForm, $name, $Row[$name]);
            $name = "TOP1_AVG5"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 5, 5, $extra);
            //$arg["data1"][$name] = $Row[$name];
            //knjCreateHidden($objForm, $name, $Row[$name]);
            $extra = "onchange=\"disCompanyText(this);\"";
            $name = "TOP1_COMPANYCD"."_M_{$mon}";
            $query = knjl410_1Query::getNameMstL406();
            makeCmb($objForm, $arg, $db, $query, $name, $Row[$name], $extra, 1, "BLANK");
            //模試名テキスト
            $extra = ($Row[$name] == "00009999") ? "" : "disabled";
            $name = "TOP1_COMPANY_TEXT"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 21, 40, $extra);
            //$arg["data1"][$name] = $Row[$name];
            //knjCreateHidden($objForm, $name, $Row[$name]);
            //TOP2
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value);\"";
            $name = "TOP2_AVG3"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 5, 5, $extra);
            //$arg["data1"][$name] = $Row[$name];
            //knjCreateHidden($objForm, $name, $Row[$name]);
            $name = "TOP2_AVG5"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 5, 5, $extra);
            //$arg["data1"][$name] = $Row[$name];
            //knjCreateHidden($objForm, $name, $Row[$name]);
            $extra = "onchange=\"disCompanyText(this);\"";
            $name = "TOP2_COMPANYCD"."_M_{$mon}";
            $query = knjl410_1Query::getNameMstL406();
            makeCmb($objForm, $arg, $db, $query, $name, $Row[$name], $extra, 1, "BLANK");
            //模試名テキスト
            $extra = ($Row[$name] == "00009999") ? "" : "disabled";
            $name = "TOP2_COMPANY_TEXT"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 21, 40, $extra);
            //$arg["data1"][$name] = $Row[$name];
            //knjCreateHidden($objForm, $name, $Row[$name]);
            //TOP2の偏差値平均
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value);\"";
            $name = "TOP_AVG"."_M_{$mon}";
            $arg["data1"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 5, 5, $extra);
            //$arg["data1"][$name] = $Row[$name];
            //knjCreateHidden($objForm, $name, $Row[$name]);
        }
        //TOP2算出ボタン
        $extra = "onclick=\"calcMock();\"";
        //$extra = "";
        $arg["button"]["btn_calcMock"] = knjCreateBtn($objForm, "btn_calcMock", "TOP2算出", $extra);

        //4.RECRUIT_VISIT_ACTIVE_DAT
        //諸活動情報
        $total = 0;
        foreach ($model->actArray as $actKey => $actRow) {
            $i = $actRow["VALUE"];
            $div = "1";
            $seq = sprintf("%03d", $i);
            //諸活動001～
            $name = "LABEL"."_A_{$div}_{$seq}";
            $arg["data1"][$name] = $actRow["LABEL"];
            //選択チェック
            $name = "REMARK1"."_A_{$div}_{$seq}";
            $extra = strlen($Row[$name]) ? "checked" : "";
            $arg["data1"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra);
            //合計
            $point = strlen($actRow["POINT"]) ? (int) $actRow["POINT"] : 0;
            if (strlen($Row[$name])) {
                $total += $point;
                $arg["data1"]["TOTAL_POINT"."_A_{$div}"] = $total;
            }
        }
        for ($i = 1; $i <= 2; $i++) {
            $div = "2";
            $seq = sprintf("%03d", $i);
            //001:中学時代の部活動 002:学校外での諸活動
            $name = "REMARK1"."_A_{$div}_{$seq}";
            $extra = "onkeyup =\"charCount(this.value, 8, (20 * 2), true);\" oncontextmenu =\"charCount(this.value, 8, (20 * 2), true);\"";
            $arg["data1"][$name] = knjCreateTextArea($objForm, $name, "8", "40", "wrap", $extra, $Row[$name]);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl410_1Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data1"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = " onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"closeMethod();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
