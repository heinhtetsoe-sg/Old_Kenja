<?php

require_once('for_php7.php');

class knjd187sForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //コントロール変更時の共通イベント
        $postback = " onchange=\"btn_submit('change', this);\"";

        //学期ラベル
        $arg["SEMISTER_NAME"] = CTRL_SEMESTERNAME;

        //対象年におけるクラス一覧
        $query = knjd187sQuery::selectClassListInYear($model, CTRL_SEMESTER);
        $result = $db->query($query);
        $list = array();
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grade   = $row["GRADE"];
            $hrclass = $row["HRCLASS"];
            $value = "{$grade}{$model->seperator}{$hrclass }";
            $list[] = array("label" => $row["LABEL"] , "value" => $value);
            if ($model->targetClass == "") {
                $model->targetClass = $value;
                $model->grade       = $grade;
                $model->hrclass     = $hrclass;
            }
        }
        $result->free();
        $arg["TARGET_CLASS"] = knjCreateCombo($objForm, "TARGET_CLASS", $model->targetClass, $list, $postback, 1);

        //通知対象学生一覧
        $list = array();
        $extra = " multiple style=\"width:100%;\" ondblclick=\"moveLeft(this)\"";
        $gradeAndHrCls = explode("-", $model->targetClass);
        $query = knjd187sQuery::selectStudentOfClsOnYr($model, CTRL_SEMESTER);
        $result = $db->query($query);
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $regNo   = substr("00000000".$row["SCHREGNO"], -8);
            $attndNo = substr("000".$row["ATTENDNO"], -3);
            $list[] = array(
                "value" => $row["SCHREGNO"],
                "label" => "{$regNo}　{$attndNo}番　{$row["NAME"]}"
            );
        }
        $result->free();
        $arg["data"]["STUDENTS_LIST"] = knjCreateCombo($objForm, "STUDENTS_LIST", "", $list, $extra, 20);

        //出力対象一覧
        $list = array();
        $extra = " multiple style=\"width:100%\" ondblclick=\"moveRight(this)\"";
        $arg["data"]["STUDENTS_SELECTED"] = knjCreateCombo($objForm, "STUDENTS_SELECTED", "", $list, $extra, 20);

        //ボタン作成//
        $btnStyle = "style=\"height:20px;width:40px\"";
        $arg["button"]["btn_left_all"]  = knjCreateBtn($objForm, "btn_left_all", "<<", " onclick=\"moveLeft(this);\" ".$btnStyle);
        $arg["button"]["btn_left"]      = knjCreateBtn($objForm, "btn_left", "＜", " onclick=\"moveLeft(this);\" ".$btnStyle);
        $arg["button"]["btn_right"]     = knjCreateBtn($objForm, "btn_right", "＞", " onclick=\"moveRight(this);\" ".$btnStyle);
        $arg["button"]["btn_right_all"] = knjCreateBtn($objForm, "btn_right_all", ">>", " onclick=\"moveRight(this);\" ".$btnStyle);
        $arg["button"]["btn_end"]       = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //プレビュー・印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "HID_EVENT_FROM");
        knjCreateHidden($objForm, "HID_YEAR", $model->year);
        knjCreateHidden($objForm, "HID_SCHOOLCD", $model->urlSchoolCd);
        knjCreateHidden($objForm, "HID_SCHOOLKIND", $model->selectSchoolKind);
        knjCreateHidden($objForm, "HID_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "HID_GRADE", $model->grade);
        knjCreateHidden($objForm, "HID_HR_CLASS", $model->hrclass);
        knjCreateHidden($objForm, "PRGID", $model->programID);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjd187sindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjd187sForm1.html", $arg);
    }
}
