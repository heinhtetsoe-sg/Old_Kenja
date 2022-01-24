<?php

require_once('for_php7.php');

class knjd627eForm1
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
        $query = knjd627eQuery::getGrade($model);
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

        //印刷日カレンダー
        $arg["PRINT_DATE"] = View::popUpCalendar2($objForm, "PRINT_DATE", str_replace("-", "/", $model->printdate), "", "", "");

        //前期後期プルダウン（帳票ラベル向け。固定取得）
        $query = knjd627eQuery::getSemester();
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

        //対象年におけるクラス一覧
        $query = knjd627eQuery::getHrClass($model, CTRL_SEMESTER, $model->grade);
        $result = $db->query($query);
        $list = array();
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $list[] = array("label" => $row["LABEL"] , "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = " multiple style=\"width:100%\" ondblclick=\"moveLeft(this)\"";
        $arg["data"]["CLASS_LIST"] = knjCreateCombo($objForm, "CLASS_LIST", "", $list, $extra, 20);

        //通知対象クラス一覧
        $list = array();
        $extra = " multiple style=\"width:100%\" ondblclick=\"moveRight(this)\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $list, $extra, 20);

        //ボタン作成
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
        knjCreateHidden($objForm, "PRGID", $model->programID);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjd627eindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjd627eForm1.html", $arg);
    }
}
