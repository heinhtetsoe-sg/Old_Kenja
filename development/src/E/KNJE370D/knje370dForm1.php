<?php

require_once('for_php7.php');

class knje370dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje370dForm1", "POST", "knje370dindex.php", "", "knje370dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knje370d')\"", "id=\"DISP2\" onClick=\"return btn_submit('knje370d')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        $selectGrdFlg = "0";
        if (($model->field["DISP"] == 2 && $model->field["GRADE_HR_CLASS"] == "99999") || 
            ($model->field["DISP"] != 2 && $model->field["GRADE"] == "99")) {
            $selectGrdFlg = "1";
        }

        //画面サイズ切替
        $arg["data"]["WIDTH"] = ($model->field["DISP"] == 1) ? "550" : "700";
        if ($selectGrdFlg == "1") {
            if ($model->field["DISP"] == 2) {
                $arg["data"]["WIDTH"] = "900";
            }
        }

        if ($model->field["DISP"] == 1) {
            //学年コンボ
            $extra = "onChange=\"return btn_submit('knje370d');\"";
            $query = knje370dQuery::getGrade($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        } else if ($model->field["DISP"] == 2) {
            //年組コンボ
            $extra = "onChange=\"return btn_submit('knje370d');\"";
            $query = knje370dQuery::getHrClass($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $selectGrdFlg);

        //評定読替の項目を表示するかしないかのフラグ
        if ($model->Properties["hyoteiYomikae"] == '1') {
            $arg["hyoteiYomikae"] = '1';
        }
        if ($model->Properties["useProvFlg"] == 1) {
            $arg["data"]["HYOTEI_KARI"] = '仮';
        }
        //評定読替チェックボックス
        $extra  = ($model->field["HYOTEI"] == "on" || !$model->cmd) ? "checked" : "";
        $extra .= " id=\"HYOTEI\"";
        $arg["data"]["HYOTEI"] = knjCreateCheckBox($objForm, "HYOTEI", "on", $extra, "");

        //ボタン作成
        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJE370D");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useCurriculumcd"                 , $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat"               , $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useProvFlg"                      , $model->Properties["useProvFlg"]);
        knjCreateHidden($objForm, "gaihyouGakkaBetu"                , $model->Properties["gaihyouGakkaBetu"]);
        knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec", $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
        knjCreateHidden($objForm, "useAssessCourseMst"              , $model->Properties["useAssessCourseMst"]);
        knjCreateHidden($objForm, "useMaruA_avg"                    , $model->Properties["useMaruA_avg"]);
        knjCreateHidden($objForm, "selectGrdFlg"                    , $selectGrdFlg);
        // //まなびの記録
        // knjCreateHidden($objForm, "REMARK_MOJI", $model->remark_moji);
        // knjCreateHidden($objForm, "REMARK_GYOU", $model->remark_gyou);
        // knjCreateHidden($objForm, "HEXAM_ENTREMARK_LEARNING_DAT__REMARK", $model->Properties["HEXAM_ENTREMARK_LEARNING_DAT__REMARK"]);

        $model->field["PASS_ONLY"] = $model->field["PASS_ONLY"] ? $model->field["PASS_ONLY"] : 0;
        //チェックボックス
        $extra = "id=\"PASS_ONLY\" tabindex=\"-1\"";
        $arg["data"]["PASS_ONLY"] = knjCreateCheckBox($objForm, "PASS_ONLY", "1", $extra);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knje370dForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array ("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == "GRADE" || $name == "GRADE_HR_CLASS") {
        $setValue = "99";
        if ($name == "GRADE_HR_CLASS") {
            $setValue = "99999";
        }
        $opt[] = array('label' => "卒業生",
                       'value' => $setValue);
        if ($value == $setValue) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $selectGrdFlg) {
    //表示切替
    if ($model->field["DISP"] == 2) {
        $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
        $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
    } else {
        $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
        $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
    }

    //初期化
    $opt_left = $opt_right =array();

    if ($selectGrdFlg == "1") {
        //卒業生の場合
        if ($model->field["DISP"] == 2) {
            //年組取得
            $query = knje370dQuery::getGrdSchList($model);
            $result = $db->query($query);
            $max_len_hr_name = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //年組のMAX文字数取得
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $max_len_hr_name = ($zenkaku * 2 + $hankaku > $max_len_hr_name) ? $zenkaku * 2 + $hankaku : $max_len_hr_name;
            }
            $result->free();

            //卒業生取得
            $query = knje370dQuery::getGrdSchList($model);
            $result = $db->query($query);
            $max_len = 10;
            $max_len_attend = 5;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grd_nendo = common::DateConv1($row["GRD_DATE"]."/04/01",10);
                $zenkaku = (strlen($grd_nendo) - mb_strlen($grd_nendo)) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($grd_nendo) - $zenkaku : mb_strlen($grd_nendo);
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $grd_nendo .= "&nbsp;";

                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len_hr_name - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

                $attend_no = "";
                if ($row["ATTENDNO"] != "") {
                    $attend_no = $row["ATTENDNO"]."番";
                }
                $zenkaku = (strlen($attend_no) - mb_strlen($attend_no)) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($attend_no) - $zenkaku : mb_strlen($attend_no);
                for ($j=0; $j < ($max_len_attend - ($zenkaku * 2 + $hankaku)); $j++) $attend_no .= "&nbsp;";

                $opt_right[] = array('label' => $grd_nendo."　".$hr_name."　".$attend_no."　".$row["NAME_SHOW"],
                                     'value' => $row["SCHREGNO"]."-"."99"."999"."999");
            }
            $result->free();
        } else {
            //一覧リスト（右側）
            $opt_right[] = array('label' => "卒業生",
                                 'value' => $model->field["GRADE"]);
        }
    } else {
        //年組取得
        $query = knje370dQuery::getHrClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["DISP"] == 2) {
                //年組のMAX文字数取得
                $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
                $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
            } else {
                //一覧リスト（右側）
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //個人指定
        if ($model->field["DISP"] == 2) {
            //生徒取得
            $query = knje370dQuery::getSchList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //クラス名称調整
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $len = $zenkaku * 2 + $hankaku;
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

                $opt_right[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                     'value' => $row["VALUE"]);
            }
            $result->free();
        }
    }

    $disp = $model->field["DISP"];

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left', $disp)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', $disp)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $disp);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $disp);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $disp);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $disp);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}
?>
