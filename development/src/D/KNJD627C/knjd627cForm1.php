<?php

require_once('for_php7.php');

class knjd627cForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //試験年度
        $arg["YEAR"] = $model->year;

        //コントロール変更時の共通イベント
        $postback = " onchange=\"btn_submit('change', this);\"";

        //学年プルダウン（２年次以降）
        $list = array();
        $target_schoolkind = "";
        $target_gradecd = "";
        $query = knjd627cQuery::getGrade($model);
        $result = $db->query($query);
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (1 < (int)$row["GRADE_CD"]) {
                $list[] = array("label" => $row["NAME"], "value" => $row["GRADE"]);
                if ($model->grade == "") {
                    $model->grade      = $row["GRADE"];
                    $target_schoolkind = $row["SCHOOL_KIND"];
                    $target_gradecd    = (int)$row["GRADE_CD"];
                } else {
                    if ($model->grade == $row["GRADE"]) {
                        $target_schoolkind = $row["SCHOOL_KIND"];
                        $target_gradecd    = (int)$row["GRADE_CD"];
                    }
                }
            }
        }
        $result->free();
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $list, $postback, 1);

        //試験対象年次
        $list = array();
        $query = knjd627cQuery::getLowerGrade($model, $target_schoolkind, $target_gradecd);
        $result = $db->query($query);
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $list[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            if ($model->target_grade == "") {
                $model->target_grade = $row["VALUE"];
            }
        }
        $arg["TARGET_GRADE"] = knjCreateCombo($objForm, "TARGET_GRADE", $model->target_grade, $list, $postback, 1);

        //再試験テスト年度を算出
        $model->retry_test_year = (int)$model->year - ((int)$model->grade - (int)$model->target_grade);

        //前期後期プルダウン（帳票ラベル向け。固定取得）
        $query = knjd627cQuery::getSemester();
        $result = $db->query($query);
        $list = array();
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $list[] = array("label" => $row["LABEL"] , "value" => $row["VALUE"]);
            if ($model->term == "") {
                $model->term = $row["VALUE"];
            }
        }
        $result->free();
        $arg["TERM"] = knjCreateCombo($objForm, "TERM", $model->term, $list, "", 1);

        //過去に履修していた科目一覧
        $query = knjd627cQuery::getTakenSubClassList($model, $target_schoolkind);
        $result = $db->query($query);
        $list = array();
        $list[] = array("label" => "--- すべて ---", "value" => "");//全科目選択を指す
        $model->classcd      = "";
        $model->curriculumcd = "";
        $model->subclasscd   = "";
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $value = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $list[] = array(
                "label" => $row["LABEL"],
                "value" => $value
            );
            if ($model->target_subject == $value) {
                $model->classcd        = $row["CLASSCD"];
                $model->curriculumcd   = $row["CURRICULUM_CD"];
                $model->subclasscd     = $row["SUBCLASSCD"];
            }
        }
        $result->free();
        $arg["TARGET_SUBJECT"] = knjCreateCombo($objForm, "TARGET_SUBJECT", $model->target_subject, $list, $postback, 1);

        //再試験基準
        $convertToInt = " style='text-align: right;' onblur=\"this.value=toInteger(this.value);\" ";
        $arg["BORDER_SCORE"] = knjCreateTextBox($objForm, $model->borderscore, "BORDER_SCORE", 3, 3, $convertToInt);

        //対象年におけるクラス一覧
        $query = knjd627cQuery::getHrClass($model, CTRL_SEMESTER, $model->grade);
        $result = $db->query($query);
        $rightlist = array();
        $leftlist = array();
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $isMoved = false;
            if (is_array($model->selected_classes)) {
                //選択済みクラスを移動
                for ($idx =0; $idx < get_count($model->selected_classes); $idx++) {
                    if ($model->selected_classes[$idx] == $row["VALUE"]) {
                        $isMoved = true;
                        $leftlist[] = array("label" => $row["LABEL"] , "value" => $row["VALUE"]);
                        array_splice($model->selected_classes, $idx, 1);//移動し終えたクラスコードを配列より除外
                        break;
                    }
                }
            }
            if ($isMoved == false) {
                $rightlist[] = array("label" => $row["LABEL"] , "value" => $row["VALUE"]);
            }
        }
        $result->free();
        $extra = " multiple style=\"width:100%\" ondblclick=\"moveLeft(this)\"";
        $arg["data"]["CLASS_LIST"] = knjCreateCombo($objForm, "CLASS_LIST", "", $rightlist, $extra, 20);

        //通知対象クラス一覧
        $extra = " multiple style=\"width:100%\" ondblclick=\"moveRight(this)\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $leftlist, $extra, 20);

        //ボタン作成//
        $btnStyle = "style=\"height:20px;width:40px\"";
        $arg["button"]["btn_left_all"]  = knjCreateBtn($objForm, "btn_left_all", "<<", " onclick=\"moveLeft(this);\" ".$btnStyle);
        $arg["button"]["btn_left"]      = knjCreateBtn($objForm, "btn_left", "＜", " onclick=\"moveLeft(this);\" ".$btnStyle);
        $arg["button"]["btn_right"]     = knjCreateBtn($objForm, "btn_right", "＞", " onclick=\"moveRight(this);\" ".$btnStyle);
        $arg["button"]["btn_right_all"] = knjCreateBtn($objForm, "btn_right_all", ">>", " onclick=\"moveRight(this);\" ".$btnStyle);
        $arg["button"]["btn_end"]       = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //プレビュー・印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //ＣＳＶ出力ボタンを作成する
        $extra = "onclick=\"btn_submit('csv', this);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "HID_EVENT_FROM");
        knjCreateHidden($objForm, "HID_CLASS_SELECTED");
        knjCreateHidden($objForm, "HID_SCHOOLCD", $model->urlSchoolCd);
        knjCreateHidden($objForm, "HID_SCHOOLKIND", $model->selectSchoolKind);
        knjCreateHidden($objForm, "HID_YEAR", $model->year);
        knjCreateHidden($objForm, "HID_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "HID_CLASS_SCHOOLKIND", $target_schoolkind);
        knjCreateHidden($objForm, "HID_CLASSCD", $model->classcd);
        knjCreateHidden($objForm, "HID_CURRICULUM_CD", $model->curriculumcd);
        knjCreateHidden($objForm, "HID_SUBCLASSCD", $model->subclasscd);
        knjCreateHidden($objForm, "HID_RETRY_YEAR", $model->retry_test_year);
        knjCreateHidden($objForm, "HID_RETRY_SEMESTER", $model->retry_test_semester);
        knjCreateHidden($objForm, "HID_RETRY_TESTKIND", $model->retry_test_kind);
        knjCreateHidden($objForm, "HID_RETRY_TESTITEMCD", $model->retry_test_itemcd);
        knjCreateHidden($objForm, "HID_RETRY_SCORE_DIV", $model->retry_test_score_div);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", $model->programID);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjd627cindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjd627cForm1.html", $arg);
    }
}
