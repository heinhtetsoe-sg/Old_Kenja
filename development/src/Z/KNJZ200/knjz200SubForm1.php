<?php

require_once('for_php7.php');

class knjz200SubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjz200index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //コースキー
        if ($model->coursename != "") {
            $course_pk = explode(" ", $model->coursename);
            $model->coursecode = $course_pk[0];
            $model->coursecd   = $course_pk[1];
            $model->majorcd    = $course_pk[2];
            $model->grade      = $course_pk[3];
        }

        //単位マスタ取得
        if ($model->cmd == "replace") {
            if (isset($model->grade) && !isset($model->warning)) {
                $Row = $db->getRow(knjz200Query::getSubQuery1($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->replace["field"];
            }
        } else {
            $Row =& $model->replace["field"];
        }

        foreach ($model->replaceCheckBoxName as $key) {
            $extra  = ($model->replace["data_chk"][$key] == "1") ? "checked" : "";
            $arg["data"]["RCHECK_".$key] = knjCreateCheckBox($objForm, "RCHECK_".$key, "1", $extra, "");
        }
        $extra  = ($model->replace["check_all"] == "1") ? "checked" : "";
        $extra .= " onClick=\"return check_all();\"";
        $arg["data"]["RCHECK_ALL"] = knjCreateCheckBox($objForm, "RCHECK_ALL", "1", $extra, "");

        //単位
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CREDITS"] = knjCreateTextBox($objForm, $Row["CREDITS"], "CREDITS", 5, 2, $extra);

        //授業時数のフラグ  欠課数上限値の表示の判定に使う
        $query = knjz200Query::getJugyouJisuFlg();
        $jugyou_jisu_flg = $db->getOne($query);     //1:法定授業 2:実授業

        //実授業の場合、欠課数上限値は表示しない
        if ($jugyou_jisu_flg != '2') {
            $arg["ABSENCE_HIGH_SHOW"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["ABSENCE_HIGH_SHOW"]);
        }
        knjCreateHidden($objForm, "JUGYOU_JISU_FLG", $jugyou_jisu_flg);

        //欠時数上限値(履修)
        $extra = "onblur=\"checkDecimal(this)\"";
        $arg["data"]["ABSENCE_HIGH"] = knjCreateTextBox($objForm, $Row["ABSENCE_HIGH"], "ABSENCE_HIGH", 5, 4, $extra);

        //欠時数上限値(修得)
        $extra = "onblur=\"checkDecimal(this)\"";
        $arg["data"]["GET_ABSENCE_HIGH"] = knjCreateTextBox($objForm, $Row["GET_ABSENCE_HIGH"], "GET_ABSENCE_HIGH", 5, 4, $extra);

        //欠課数オーバー表示判定
        if ($model->control["学期数"] == "3") {
            $arg["ABSENCE_WARN_DIV"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["ABSENCE_WARN_DIV"]);
        }
        knjCreateHidden($objForm, "SEMESTER_NUM", $model->control["学期数"]);

        //欠課数オーバーの前警告
        $query = knjz200Query::getNameMst("C042", "01"); // 1:回、1以外:週間
        $namespare1 = $db->getOne($query);
        $arg["data"]["ABSENCE_WARN_KAI"] = ($namespare1 == "1") ? "回" : "週間";

        //１学期（前期）欠課数オーバー
        $arg["data"]["SEMESTERNAME1"] = $model->control["学期名"][1];
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ABSENCE_WARN"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN"], "ABSENCE_WARN", 2, 2, $extra);

        //２学期（後期）欠課数オーバー
        $arg["data"]["SEMESTERNAME2"] = $model->control["学期名"][2];
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ABSENCE_WARN2"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN2"], "ABSENCE_WARN2", 2, 2, $extra);

        //３学期欠課数オーバー
        $arg["data"]["SEMESTERNAME3"] = $model->control["学期名"][3];
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ABSENCE_WARN3"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN3"], "ABSENCE_WARN3", 2, 2, $extra);

        //必履修区分
        $query = knjz200Query::getRequireName($model);
        makeCmb($objForm, $arg, $db, $query, "REQUIRE_FLG", $Row["REQUIRE_FLG"], "", 1, 1);

        //半期認定フラグ
        $extra  = ($Row["AUTHORIZE_FLG"] == "1") ? "checked" : "";
        $arg["data"]["AUTHORIZE_FLG"] = knjCreateCheckBox($objForm, "AUTHORIZE_FLG", "1", $extra." onclick=\"Check_a('on');\"", "");
        $arg["data"]["style_a"] = ($extra != "checked") ? "" : "半期";

        //無条件履修修得フラグ
        $extra  = ($Row["COMP_UNCONDITION_FLG"] == "1") ? "checked" : "";
        $arg["data"]["COMP_UNCONDITION_FLG"] = knjCreateCheckBox($objForm, "COMP_UNCONDITION_FLG", "1", $extra." onclick=\"Check_c('on');\"", "");
        $arg["data"]["style_c"] = ($extra != "checked") ? "" : "無条件";

        //時間単位
        if ($model->Properties["useTimeUnit"] == '1') {
            $arg["useTimeUnit"] = "1";
            $extra = "onblur=\"checkDecimal(this)\"";
            $arg["data"]["TIME_UNIT"] = knjCreateTextBox($objForm, $Row["TIME_UNIT"], "TIME_UNIT", 5, 5, $extra);
        }

        //掛け率
        if ($model->Properties["useMultiplicationRate"] == '1') {
            $arg["useRate"] = "1";
            $extra = "onblur=\"checkDecimal(this)\"";
            $arg["data"]["RATE"] = knjCreateTextBox($objForm, $Row["RATE"], "RATE", 3, 3, $extra);
        }

        //学年情報
        $arg["GRADEINFO"] = CTRL_YEAR.'年度　　'.ltrim($model->grade, '0')."学年";

        //課程学科リストToリスト作成
        makeCourseList($objForm, $arg, $db, $model);

        //科目リストToリスト作成
        makeSubclassList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjz200SubForm1.html", $arg);
    }
}

//課程学科リストToリスト作成
function makeCourseList(&$objForm, &$arg, $db, $model)
{
    //一括処理選択時のコース情報
    $array = explode(",", $model->replace["selectdata_course"]);
    if ($array[0] == "") {
        $array[0] = $model->coursecd.$model->majorcd.$model->coursecode;
    }

    //学籍在籍データ件数
    $regd_cnt = $db->getOne(knjz200Query::getRegdDatCnt());
    $flg = ($regd_cnt > 0) ? "" : 1;

    //課程学科一覧取得
    $result = $db->query(knjz200Query::getCourseList($model, $flg));
    $course_left = $course_right = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $array)) {
            $course_right[] = array("label" => $row["LABEL"],
                                    "value" => $row["VALUE"]);
        } else {
            $course_left[]  = array("label" => $row["LABEL"],
                                    "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //課程学科一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('left','course','left_course','right_course',1)\"";
    $arg["data"]["RIGHT_COURSE"] = knjCreateCombo($objForm, "right_course", "", $course_right, $extra, 10);

    //対象課程学科一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('right','course','left_course','right_course',1)\"";
    $arg["data"]["LEFT_COURSE"] = knjCreateCombo($objForm, "left_course", "", $course_left, $extra, 10);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_add_all','course');\"";
    $arg["button"]["COURSE_ADD_ALL"] = knjCreateBtn($objForm, "course_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('left','course');\"";
    $arg["button"]["COURSE_ADD"] = knjCreateBtn($objForm, "course_add", "＜", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('right','course');\"";
    $arg["button"]["COURSE_DEL"] = knjCreateBtn($objForm, "course_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_del_all','course');\"";
    $arg["button"]["COURSE_DEL_ALL"] = knjCreateBtn($objForm, "course_del_all", ">>", $extra);
}

//科目リストToリスト作成
function makeSubclassList(&$objForm, &$arg, $db, $model)
{
    //一括処理選択時の科目情報
    $array = explode(",", $model->replace["selectdata_subclass"]);
    if ($array[0] == "") {
        $array[0] = $model->subclasscd;
    }

    //科目一覧取得
    $result = $db->query(knjz200Query::getSubclassList($model));
    $subclass_left = $subclass_right = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $array)) {
            $subclass_right[] = array("label" => $row["LABEL"],
                                      "value" => $row["VALUE"]);
        } else {
            $subclass_left[]  = array("label" => $row["LABEL"],
                                      "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //科目一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('left','subclass','left_subclass','right_subclass',1)\"";
    $arg["data"]["RIGHT_SUBCLASS"] = knjCreateCombo($objForm, "right_subclass", "", $subclass_right, $extra, 15);

    //対象科目一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('right','subclass','left_subclass','right_subclass',1)\"";
    $arg["data"]["LEFT_SUBCLASS"] = knjCreateCombo($objForm, "left_subclass", "", $subclass_left, $extra, 15);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_add_all','subclass');\"";
    $arg["button"]["SUBCLASS_ADD_ALL"] = knjCreateBtn($objForm, "subclass_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('left','subclass');\"";
    $arg["button"]["SUBCLASS_ADD"] = knjCreateBtn($objForm, "subclass_add", "＜", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('right','subclass');\"";
    $arg["button"]["SUBCLASS_DEL"] = knjCreateBtn($objForm, "subclass_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_del_all','subclass');\"";
    $arg["button"]["SUBCLASS_DEL_ALL"] = knjCreateBtn($objForm, "subclass_del_all", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $link = REQUESTROOT."/Z/KNJZ200/knjz200index.php?cmd=back";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"window.open('$link','_self');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata_course");
    knjCreateHidden($objForm, "selectdata_subclass");
}
