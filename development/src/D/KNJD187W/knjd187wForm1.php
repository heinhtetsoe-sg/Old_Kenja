<?php

require_once('for_php7.php');

class knjd187wForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //試験年度
        $arg["YEAR"] = $model->year;

        //コントロール変更時の共通イベント
        $postback = " onchange=\"btn_submit('change');\"";

        //学年プルダウン
        $list = array();
        $target_schoolkind = "";
        $target_gradecd = "";
        $query = knjd187wQuery::getGrade($model);
        $result = $db->query($query);
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $list[] = array("label" => $row["NAME"], "value" => $row["GRADE"]);
            if ($model->grade == "") {
                $model->grade = $row["GRADE"];
            }
        }
        $result->free();
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $list, $postback, 1);

        //対象クラス一覧
        $query = knjd187wQuery::getHrClass($model, CTRL_SEMESTER, $model->grade);
        $result = $db->query($query);
        $list = array();
        while (is_null($result) == false && $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $list[] = array("label" => $row["LABEL"] , "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = " multiple style=\"width:100%\" ondblclick=\"moveLeft(this)\"";
        $arg["data"]["CLASS_LIST"] = knjCreateCombo($objForm, "CLASS_LIST", "", $list, $extra, 20);

        //出力対象一覧
        $list = array();
        $extra = " multiple style=\"width:100%\" ondblclick=\"moveRight(this)\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $list, $extra, 20);

        //出力条件
        $extra = " min='1' style='text-align : right;' onblur=\"checkInputNum(this)\" ";
        $arg["LOWER_SCORE"] = knjCreateTextBox($objForm, $model->lowerScore, "LOWER_SCORE", 3, 3, $extra);
        $arg["AVARAGE_SCORE"] = knjCreateTextBox($objForm, $model->avarageScore, "AVARAGE_SCORE", 3, 3, $extra);

        //ボタン作成//
        $btnStyle = "style=\"height:20px;width:40px\"";
        $arg["button"]["btn_left_all"]       = knjCreateBtn($objForm, "btn_left_all", "<<", " onclick=\"moveLeft(this);\" ".$btnStyle);
        $arg["button"]["btn_left"]           = knjCreateBtn($objForm, "btn_left", "＜", " onclick=\"moveLeft(this);\" ".$btnStyle);
        $arg["button"]["btn_right"]          = knjCreateBtn($objForm, "btn_right", "＞", " onclick=\"moveRight(this);\" ".$btnStyle);
        $arg["button"]["btn_right_all"]      = knjCreateBtn($objForm, "btn_right_all", ">>", " onclick=\"moveRight(this);\" ".$btnStyle);
        $arg["button"]["btn_end"]            = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //プレビュー・印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);
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
        $arg["start"]  = $objForm->get_start("main", "POST", "knjd187windex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjd187wForm1.html", $arg);
    }
}
