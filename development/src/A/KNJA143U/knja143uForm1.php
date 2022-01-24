<?php

require_once('for_php7.php');

class knja143uForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja143uForm1", "POST", "knja143uindex.php", "", "knja143uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knja143u');\"";
        $query = knja143uQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //校種コンボ
        $extra = "onChange=\"return btn_submit('knja143u');\"";
        $query = knja143uQuery::getSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //学年コンボ
        if (strpos($model->Properties["useFormNameA143U"], "KNJA143U_5") !== false) {
            $extra = "onChange=\"return btn_submit('knja143u');\"";
            $query = knja143uQuery::getGrade($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        }

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja143u')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja143u')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //プロパティに"KNJA143U_3"の文字列を含んでいるなら、表示フラグを設定
        if (strpos($model->Properties["useFormNameA143U"], "KNJA143U_3") !== false) {
            $arg["DISP_SELECT_FORMKIND"] = "1";
            $model->toinFlg = true;
        } else {
            $arg["DISP_SELECT_FORMKIND"] = "";
            $model->toinFlg = false;
        }

        //プロパティに"KNJA143U_6"の文字列を含んでいるなら、表示フラグを設定
        if (strpos($model->Properties["useFormNameA143U"], "KNJA143U_6") !== false) {
            $arg["DISP_SELECT_FORMKIND2"] = "1";
        } else {
            $arg["DISP_SELECT_FORMKIND2"] = "";
        }

        knjCreateHidden($objForm, "toinFlg", $model->toinFlg ? "1": "");

        $opt = array(1, 2);
        $model->field["FROM_KIND"] = ($model->field["FROM_KIND"] == "") ? "1" : $model->field["FROM_KIND"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"FROM_KIND{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "FROM_KIND", $model->field["FROM_KIND"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //画面サイズ切替
        $arg["data"]["WIDTH"] = ($model->field["DISP"] == 1) ? "550" : "700";

        if ($model->field["DISP"] == 2) {
            //年組コンボ
            $extra = "onChange=\"return btn_submit('change');\"";
            $query = knja143uQuery::getHrClass($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //次年度チェックボックス
        if (strpos($model->Properties["useFormNameA143U"], "KNJA143U_8") !== false) {
            $arg["show_next_year_grade"] = "1";
            $extra = "id=\"NEXT_YEAR_GRADE_FLG\"";
            $extra .= ($model->field["NEXT_YEAR_GRADE_FLG"] == "1") ? " checked" : "";
            $arg["data"]["NEXT_YEAR_GRADE_FLG"] = knjCreateCheckBox($objForm, "NEXT_YEAR_GRADE_FLG", "1", $extra);
        }

        //発行日
        if (!$model->toinFlg) {
            $arg["show_issue_date"] = "1";
        }
        if (!strlen($model->field["ISSUE_DATE"])) {
            if ($model->toinFlg) {
                $model->field["ISSUE_DATE"] = CTRL_YEAR."/04/01";
            } else {
                $model->field["ISSUE_DATE"] = str_replace("-", "/", CTRL_DATE);
            }
        }
        $arg["data"]["ISSUE_DATE"] = View::popUpCalendar($objForm, "ISSUE_DATE", $model->field["ISSUE_DATE"]);

        //有効期限
        if ($model->toinFlg) {
            $arg["LIMIT_DATE_TITLE"] = "有効年度";
            $value = ($model->field["LIMIT_DATE"] != "") ? $model->field["LIMIT_DATE"]: CTRL_YEAR;
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["LIMIT_DATE"] = knjCreateTextBox($objForm, $value, "LIMIT_DATE", 4, 4, $extra);
        } else {
            $arg["LIMIT_DATE_TITLE"] = "有効期限";
            if (strpos($model->Properties["useFormNameA143U"], "KNJA143U_5") !== false) {
                //GRADE_CDに応じて、加算年度をセットする
                $plusYear = $db->getOne(knja143uQuery::getPlusYear($model));
                $model->field["LIMIT_DATE"] = (CTRL_YEAR +(int)$plusYear)."/03/31";
            } elseif (!strlen($model->field["LIMIT_DATE"])) {
                $model->field["LIMIT_DATE"] = (CTRL_YEAR + 1)."/03/31";
            }
            $arg["data"]["LIMIT_DATE"] = View::popUpCalendar($objForm, "LIMIT_DATE", $model->field["LIMIT_DATE"]);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knja143uForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank != "") {
        $opt[] = array("label" => "", "value" => "");
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
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

    //年組取得
    $query = knja143uQuery::getHrClass($model);
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
        $selectleft = ($model->selectleft != "") ? explode(",", $model->selectleft) : array();
        $selectleftval = ($model->selectleftval != "") ? explode(",", $model->selectleftval) : array();

        //生徒取得
        $query = knja143uQuery::getSchList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //クラス名称調整
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
                $hr_name .= "&nbsp;";
            }

            if ($model->cmd == 'change') {
                if (!in_array($row["VALUE"], $selectleft)) {
                    $opt_right[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                         'value' => $row["VALUE"]);
                }
            } else {
                $opt_right[] = array('label' => $hr_name."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //左リストで選択されたものを再セット
        if ($model->cmd == 'change') {
            for ($i = 0; $i < get_count($selectleft); $i++) {
                $opt_left[] = array("label" => $selectleftval[$i],
                                    "value" => $selectleft[$i]);
            }
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    //PRGIDについては、javascriptの帳票出力時に再設定しているので注意。
    if (strpos($model->Properties["useFormNameA143U"], "KNJA143U_3") !== false) {
        knjCreateHidden($objForm, "PRGID", "KNJA143U_3");
    } elseif (strpos($model->Properties["useFormNameA143U"], "KNJA143U_4") !== false) {
        knjCreateHidden($objForm, "PRGID", "KNJA143U_4");
    } elseif (strpos($model->Properties["useFormNameA143U"], "KNJA143U_5") !== false) {
        knjCreateHidden($objForm, "PRGID", "KNJA143U_5");
    } elseif (strpos($model->Properties["useFormNameA143U"], "KNJA143U_6") !== false) {
        knjCreateHidden($objForm, "PRGID", "KNJA143U_6");
    } elseif (strpos($model->Properties["useFormNameA143U"], "KNJA143U_8") !== false) {
        knjCreateHidden($objForm, "PRGID", "KNJA143U_8");
    } else {
        knjCreateHidden($objForm, "PRGID", "KNJA143U");
    }
    knjCreateHidden($objForm, "USEFORMNAMEA143U", $model->Properties["useFormNameA143U"]);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}
