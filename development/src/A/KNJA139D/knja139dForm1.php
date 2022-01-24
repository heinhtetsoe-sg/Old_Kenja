<?php

require_once('for_php7.php');

class knja139dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knja139dindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = (AUTHORITY != DEF_UPDATABLE) ? "disabled" : "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //学期コンボ(9:学期末のみ取得)
        $query = knja139dQuery::getSemester("9");
        $extra = "onChange=\"return btn_submit('main')\"";
        $model->field["SEMESTER"] = ($model->field["SEMESTER"] != "") ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knja139dQuery::getGrade($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //種別コンボ
        $query = knja139dQuery::getA042("cnt");
        $a042cnt = $db->getOne($query);
        if ($a042cnt == 0) {
            $opt = array();
            $opt[] = array('label' => '総合的な学習の時間：活動', 'value' => '01');
            $opt[] = array('label' => '総合的な学習の時間：評価', 'value' => '02');

            $model->field["DATA_DIV"] = ($model->field["DATA_DIV"]) ? $model->field["DATA_DIV"] : $opt[0]["value"];
            $extra = "onChange=\"return btn_submit('main')\"";
            $arg["DATA_DIV"] = knjCreateCombo($objForm, "DATA_DIV", $model->field["DATA_DIV"], $opt, $extra, 1);
        } else {
            $query = knja139dQuery::getA042();
            $extra = "onChange=\"return btn_submit('main')\"";
            makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field["DATA_DIV"], $extra, 1);
        }

        //データ数
        $query = knja139dQuery::getDataCnt($model);
        $model->dataCnt = $db->getOne($query);
        if ($model->cmd != "readCnt") {
            $model->field["DATA_CNT"] = $model->dataCnt;
        }
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["DATA_CNT"] = knjCreateTextBox($objForm, $model->field["DATA_CNT"], "DATA_CNT", 3, 3, $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('readCnt');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

        //パターン数
        $model->pattern_cd = array();
        for ($i = 1; $i <= $model->field["DATA_CNT"]; $i++) {
            $model->pattern_cd[] = $i;
        }

        //定型文一覧表示
        for ($i = 0; $i < get_count($model->pattern_cd); $i++) {
            //データ存在チェック
            $query = knja139dQuery::getHtrainremarkTempDat($model, $model->pattern_cd[$i], "cnt");
            $cnt = $db->getOne($query);

            if ($cnt > 0) {
                $query = knja139dQuery::getHtrainremarkTempDat($model, $model->pattern_cd[$i], "list");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //定型文テキストエリア
                    $value = (!isset($model->warning)) ? $row["REMARK"] : $model->fields[$i]["REMARK"];
                    $extra = "";
                    $row["REMARK"] = KnjCreateTextArea($objForm, "REMARK_".$i, "3", "111", "soft", $extra, $value);
                    //パターンコード（全角で表示）
                    $row["PATTERN_CD_SHOW"] = mb_convert_kana($model->pattern_cd[$i], "R");

                    $arg["data"][] = $row;
                }
            } else {
                $tmp = array();
                //定型文テキストエリア
                $value = (!isset($model->warning)) ? $tmp["REMARK"] : $model->fields[$i]["REMARK"];
                $extra = "";
                $tmp["REMARK"] = KnjCreateTextArea($objForm, "REMARK_".$i, 3, 111, "soft", $extra, $value);
                //パターンコード（全角で表示）
                $tmp["PATTERN_CD_SHOW"] = mb_convert_kana($model->pattern_cd[$i], "R");

                $arg["data"][] = $tmp;
            }
        }

        //ボタン作成
        //更新ボタン
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


        //学期コース毎定型文取得
        $query = knja139dQuery::getHtrainremarkTempSemesCourseDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $course = $row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
            if (!in_array($course, $model->field["COURSE_SELECTED"])) {
                $model->field["COURSE_SELECTED"][] = $course;
            }
            $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            if (!in_array($course, $model->field["SUBCLASS_SELECTED"])) {
                $model->field["SUBCLASS_SELECTED"][] = $subclass;
            }
        }
        $result->free();

        //コース一覧取得
        $courseList = array();
        $courseSelect = array();
        $query = knja139dQuery::getCouse($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["VALUE"], $model->field["COURSE_SELECTED"])) {
                $courseSelect[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            } else {
                $courseList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
        }
        $result->free();
        //コースコンボボックス作成
        $extra = "style=\"width:100%;\" ondblclick=\"moveCourse('right', '');\"";
        $arg["CATEGORY_COURSE_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_COURSE_SELECTED", "", $courseSelect, $extra, 10);
        $extra = "style=\"width:100%;\" ondblclick=\"moveCourse('left', '');\"";
        $arg["CATEGORY_COURSE_LIST"] = knjCreateCombo($objForm, "CATEGORY_COURSE_LIST", "", $courseList, $extra, 10);
        knjCreateHidden($objForm, "COURSE_SELECTED");

        //科目一覧取得
        $subclassList = array();
        $subclassSelect = array();
        $query = knja139dQuery::getSubclass($model, '90');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["VALUE"], $model->field["SUBCLASS_SELECTED"])) {
                $subclassSelect[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            } else {
                $subclassList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
        }
        $result->free();
        //科目コンボボックス作成
        $extra = "";
        $extra = "style=\"width:100%;\" ondblclick=\"moveSubclass('right', '');\"";
        $arg["CATEGORY_SUBCLASS_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SUBCLASS_SELECTED", "", $subclassSelect, $extra, 10);
        $extra = "style=\"width:100%;\" ondblclick=\"moveSubclass('left', '');\"";
        $arg["CATEGORY_SUBCLASS_LIST"] = knjCreateCombo($objForm, "CATEGORY_SUBCLASS_LIST", "", $subclassList, $extra, 10);
        knjCreateHidden($objForm, "SUBCLASS_SELECTED");


        //コース全対象移動ボタンを作成する(対象 → 一覧)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveCourse('right', 'ALL');\"";
        $arg["button"]["btn_course_rights"] = knjCreateBtn($objForm, "btn_course_rights", ">>", $extra);
        //コース全対象移動ボタンを作成する(一覧 → 対象)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveCourse('left', 'ALL');\"";
        $arg["button"]["btn_course_lefts"] = knjCreateBtn($objForm, "btn_course_lefts", "<<", $extra);
        //コース選択対象移動ボタンを作成する(対象 → 一覧)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveCourse('right', '');\"";
        $arg["button"]["btn_course_right"] = knjCreateBtn($objForm, "btn_course_right", "＞", $extra);
        //コース選択対象移動ボタンを作成する(一覧 → 対象)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveCourse('left', '');\"";
        $arg["button"]["btn_course_left"] = knjCreateBtn($objForm, "btn_course_left", "＜", $extra);

        //科目全対象移動ボタンを作成する(対象 → 一覧)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveSubclass('right', 'ALL');\"";
        $arg["button"]["btn_subclass_rights"] = knjCreateBtn($objForm, "btn_subclass_rights", ">>", $extra);
        //科目全対象移動ボタンを作成する(一覧 → 対象)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveSubclass('left', 'ALL');\"";
        $arg["button"]["btn_subclass_lefts"] = knjCreateBtn($objForm, "btn_subclass_lefts", "<<", $extra);
        //科目選択対象移動ボタンを作成する(対象 → 一覧)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveSubclass('right', '');\"";
        $arg["button"]["btn_subclass_right"] = knjCreateBtn($objForm, "btn_subclass_right", "＞", $extra);
        //科目選択対象移動ボタンを作成する(一覧 → 対象)
        $extra = "style=\"height:20px;width:40px\" onclick=\"moveSubclass('left', '');\"";
        $arg["button"]["btn_subclass_left"] = knjCreateBtn($objForm, "btn_subclass_left", "＜", $extra);


        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja139dForm1.html", $arg);
    }
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

?>
