<?php

require_once('for_php7.php');

class knjd122xForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd122xindex.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjd122xQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        //$opt_shori[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //成績入力完了チェックボックス
        $checkComp = ($model->field["CHK_COMP"] == "on" || $model->cmd == "") ? " checked" : "";
        $extra = "id=\"CHK_COMP\"" .$checkComp;
        $arg["data"]["CHK_COMP"] = knjCreateCheckBox($objForm, "CHK_COMP", "on", $extra);

        //ヘッダ出力(学年末用)チェックボックス
        $checkSem9 = ($model->field["CHK_SEM9"] == "on") ? " checked" : "";
        $extra = "id=\"CHK_SEM9\"" .$checkSem9;
        $arg["data"]["CHK_SEM9"] = knjCreateCheckBox($objForm, "CHK_SEM9", "on", $extra);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 4096000);

        /**********/
        /* コンボ */
        /**********/
        /* 取込 */
        //年度
        $query = knjd122xQuery::getSelectYear();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "IN_YEAR", $model->field["IN_YEAR"], $extra, 1);
        //学期
        $query = knjd122xQuery::getSelectSemester($model->field["IN_YEAR"]);
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "IN_SEMESTER", $model->field["IN_SEMESTER"], $extra, 1);
        //テスト種別
        $query = knjd122xQuery::getSelectTestkind($model->useTestCountflg, $model->field["IN_YEAR"], $model->field["IN_SEMESTER"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "IN_TESTKIND", $model->field["IN_TESTKIND"], $extra, 1);
        /* 出力 */
        //年度
        $query = knjd122xQuery::getSelectYear();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "OUT_YEAR", $model->field["OUT_YEAR"], $extra, 1);
        //学期
        $query = knjd122xQuery::getSelectSemester($model->field["OUT_YEAR"]);
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "OUT_SEMESTER", $model->field["OUT_SEMESTER"], $extra, 1);
        //テスト種別
        $query = knjd122xQuery::getSelectTestkind($model->useTestCountflg, $model->field["OUT_YEAR"], $model->field["OUT_SEMESTER"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "OUT_TESTKIND", $model->field["OUT_TESTKIND"], $extra, 1);

        //MAX学期
        $query = knjd122xQuery::getMaxSemester($model->field["OUT_YEAR"]);
        $model->maxSemester = $db->getOne($query);

        /**********/
        /* リスト */
        /**********/
        //科目
        $subclasscdArray = array();
        if ($model->cmd == "read" && strlen($model->selectdata["SUBCLASSCD"])) {
            $subclasscdArray = explode(",", $model->selectdata["SUBCLASSCD"]);
        }
        $opt_left_subclasscd = array();
        $opt_right_subclasscd = array();
        $result = $db->query(knjd122xQuery::getSubclassList($model->field["OUT_YEAR"], $model->field["OUT_SEMESTER"], $model->maxSemester, $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["VALUE"], $subclasscdArray)) {
                $opt_left_subclasscd[]  = array("label" => $row["VALUE"]." ".$row["LABEL"], "value" => $row["VALUE"]);
            } else {
                $opt_right_subclasscd[] = array("label" => $row["VALUE"]." ".$row["LABEL"], "value" => $row["VALUE"]);
            }
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "left_subclasscd",
                            "size"        => "8",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"moveList('right','left_subclasscd','right_subclasscd',1,'selectdataSubclasscd');\"",
                            "options"     => $opt_left_subclasscd));
        $objForm->ae(array("type"        => "select",
                            "name"        => "right_subclasscd",
                            "size"        => "8",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"moveList('left','left_subclasscd','right_subclasscd',1,'selectdataSubclasscd');\"",
                            "options"     => $opt_right_subclasscd));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_all_sub",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return moveList('sel_add_all','left_subclasscd','right_subclasscd',1,'selectdataSubclasscd');\"" ));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_sub",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return moveList('left','left_subclasscd','right_subclasscd',1,'selectdataSubclasscd');\"" ));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_sub",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return moveList('right','left_subclasscd','right_subclasscd',1,'selectdataSubclasscd');\"" ));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_all_sub",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return moveList('sel_del_all','left_subclasscd','right_subclasscd',1,'selectdataSubclasscd');\"" ));
        $arg["subclass"] = array( "LEFT_LIST"   => "対象科目",
                                  "RIGHT_LIST"  => "科目一覧",
                                  "LEFT_PART"   => $objForm->ge("left_subclasscd"),
                                  "RIGHT_PART"  => $objForm->ge("right_subclasscd"),
                                  "SEL_ADD_ALL" => $objForm->ge("sel_add_all_sub"),
                                  "SEL_ADD"     => $objForm->ge("sel_add_sub"),
                                  "SEL_DEL"     => $objForm->ge("sel_del_sub"),
                                  "SEL_DEL_ALL" => $objForm->ge("sel_del_all_sub"));
        //講座
        $opt_left_chaircd = array();
        $opt_right_chaircd = array();
        $result = $db->query(knjd122xQuery::getChairList($model->field["OUT_YEAR"], $model->field["OUT_SEMESTER"], $subclasscdArray, $model->maxSemester, $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right_chaircd[]  = array("label" => $row["VALUE"]." ".$row["LABEL"],
                                          "value" => $row["VALUE"]);
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "left_chaircd",
                            "size"        => "8",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"moveList('right','left_chaircd','right_chaircd',1,'selectdataChaircd');\"",
                            "options"     => $opt_left_chaircd));
        $objForm->ae(array("type"        => "select",
                            "name"        => "right_chaircd",
                            "size"        => "8",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"moveList('left','left_chaircd','right_chaircd',1,'selectdataChaircd');\"",
                            "options"     => $opt_right_chaircd));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return moveList('sel_add_all','left_chaircd','right_chaircd',1,'selectdataChaircd');\"" ));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return moveList('left','left_chaircd','right_chaircd',1,'selectdataChaircd');\"" ));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return moveList('right','left_chaircd','right_chaircd',1,'selectdataChaircd');\"" ));
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return moveList('sel_del_all','left_chaircd','right_chaircd',1,'selectdataChaircd');\"" ));
        $arg["chair"] = array( "LEFT_LIST"   => "対象講座",
                               "RIGHT_LIST"  => "講座一覧",
                               "LEFT_PART"   => $objForm->ge("left_chaircd"),
                               "RIGHT_PART"  => $objForm->ge("right_chaircd"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        /**************/
        /* カレンダー */
        /**************/
        //講座基準日
        if ($model->field["CHAIR_DATE"] == "") {
            $model->field["CHAIR_DATE"] = str_replace("-", "/", CTRL_DATE);
        }
        $arg["data"]["CHAIR_DATE"] = View::popUpCalendar($objForm, "CHAIR_DATE", $model->field["CHAIR_DATE"]);

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD122X");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "selectdataSubclasscd");
        knjCreateHidden($objForm, "selectdataChaircd");
        knjCreateHidden($objForm, "useTestCountflg", $model->useTestCountflg);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        
        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd122xForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "OUT_TESTKIND" || $name == "IN_TESTKIND") {
            $opt[] = array('label' => $row["VALUE"] ."：". $row["LABEL"],
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "OUT_YEAR" || $name == "IN_YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } elseif ($name == "OUT_SEMESTER" || $name == "IN_SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //読込ボタン
    $extra = "onclick=\"return btn_submit('read');\"";
    $arg["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knjd122xQuery::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　エクセル出力／ＣＳＶ取込";
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
