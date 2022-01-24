<?php

require_once('for_php7.php');

class knja143oForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja143oForm1", "POST", "knja143oindex.php", "", "knja143oForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        if (($model->Properties["useFormNameA143O"] == "KNJA143O_3") || ($model->Properties["useFormNameA143O"] == "KNJA143O_4")) {
            $arg["set_o3"] = "1";
            $arg["noset_o3"] = "";
        } else {
            $arg["set_o3"] = "";
            $arg["noset_o3"] = "1";
        }

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knja143o');\"";
        $query = knja143oQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //校種コンボ
        $extra = "onChange=\"return btn_submit('knja143o');\"";
        $query = knja143oQuery::getSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja143o')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja143o')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //画面サイズ切替
        $arg["data"]["WIDTH"] = ($model->field["DISP"] == 1) ? "600" : "700";

        if ($model->field["DISP"] == 2) {
            //年組コンボ
            $extra = "onChange=\"return btn_submit('change');\"";
            $query = knja143oQuery::getHrClass($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //有効期限
        if (!strlen($model->field["LIMIT_DATE"])) {
            $model->field["LIMIT_DATE"] = (CTRL_YEAR + 1)."/03/31";
        }
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendar($objForm, "LIMIT_DATE", $model->field["LIMIT_DATE"]);

        if (($model->Properties["useFormNameA143O"] == "KNJA143O_3") || ($model->Properties["useFormNameA143O"] == "KNJA143O_4")) {
            //有効期限(開始日)
            if (!strlen($model->field["LIMIT_DATE_FROM"])) {
                $strtDate = $db->getRow(knja143oQuery::getSemester("1"), DB_FETCHMODE_ASSOC);
                if (is_array($strtDate)) {
                    $model->field["LIMIT_DATE_FROM"] = str_replace("-", "/", $strtDate["SDATE"]);
                } else {
                    $model->field["LIMIT_DATE_FROM"] = (CTRL_YEAR)."/04/01";
                }
            }
            $arg["data"]["LIMIT_DATE_FROM"] = View::popUpCalendar($objForm, "LIMIT_DATE_FROM", $model->field["LIMIT_DATE_FROM"]);
        } else {
            //発行日
            if (!strlen($model->field["ISSUE_DATE"])) {
                $model->field["ISSUE_DATE"] = str_replace("-", "/", CTRL_DATE);
            }
            $arg["data"]["ISSUE_DATE"] = View::popUpCalendar($objForm, "ISSUE_DATE", $model->field["ISSUE_DATE"]);

            //表/裏ラジオボタンを作成
            $opt = array(1, 2);
            $disable = 0;
            if (!$model->field["PRINT_PAGE"]) {
                $model->field["PRINT_PAGE"] = 1;
            }
            $onclick = "onclick =\"kubun();\"";
            $extra = array("id=\"PRINT_PAGE1\" ".$onclick, "id=\"PRINT_PAGE2\" ".$onclick);
            $radioArray = knjCreateRadio($objForm, "PRINT_PAGE", $model->field["PRINT_PAGE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //イメージカラーラジオボタンを作成
            $opt = array(1, 2);
            if (!$model->field["IMAGE_COLOR"]) {
                $model->field["IMAGE_COLOR"] = 1;
            }
            $onclick = "";
            $extra = array("id=\"IMAGE_COLOR1\" ".$onclick
                         , "id=\"IMAGE_COLOR2\" ".$onclick
                          );
            $radioArray = knjCreateRadio($objForm, "IMAGE_COLOR", $model->field["IMAGE_COLOR"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //裏面シールor定型文選択ラジオボタンを作成
            $opt = array(1, 2);
            $disable = 0;
            if (!$model->field["PAGE2_DIV"]) {
                $model->field["PAGE2_DIV"] = 1;
            }
            $onclick = "";
            $extra = array("id=\"PAGE2_DIV1\" ".$onclick
                         , "id=\"PAGE2_DIV2\" ".$onclick
                          );
            $radioArray = knjCreateRadio($objForm, "PAGE2_DIV", $model->field["PAGE2_DIV"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
            $row = array(array('label' => "１行",'value' => 1),
                         array('label' => "２行",'value' => 2),
                         array('label' => "３行",'value' => 3),
                         array('label' => "４行",'value' => 4),
                         array('label' => "５行",'value' => 5),
                        );

            $objForm->ae(array("type"       => "select",
                                "name"       => "POROW",
                                "size"       => "1",
                                "value"      => $model->field["POROW"],
                                "options"    => isset($row)? $row : array()));

            $arg["data"]["POROW"] = $objForm->ge("POROW");

            //開始位置（列）コンボボックスを作成する////////////////////////////////////////////////////////////////////////
            $col = array(array('label' => "１列",'value' => 1),
                         array('label' => "２列",'value' => 2),
                        );

            $objForm->ae(array("type"       => "select",
                                "name"       => "POCOL",
                                "size"       => "1",
                                "value"      => $model->field["POCOL"],
                                "options"    => isset($col) ? $col : array()));

            $arg["data"]["POCOL"] = $objForm->ge("POCOL");
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knja143oForm1.html", $arg);
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
    $query = knja143oQuery::getHrClass($model);
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
        $query = knja143oQuery::getSchList($model);
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
    if (($model->Properties["useFormNameA143O"] == "KNJA143O_3") || ($model->Properties["useFormNameA143O"] == "KNJA143O_4")) {
        knjCreateHidden($objForm, "PRGID", $model->Properties["useFormNameA143O"]);
    } else {
        knjCreateHidden($objForm, "PRGID", "KNJA143O");
    }

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
    knjCreateHidden($objForm, "useFormNameA143O", $model->Properties["useFormNameA143O"]);
}
