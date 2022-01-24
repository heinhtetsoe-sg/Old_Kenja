<?php
class knjl002vForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl002vindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg") && $model->examId) {
            $query = knjl002vQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //入試コード
        $extra = "";
        $query = knjl002vQuery::getApplicantMst($model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANT_DIV", $Row["APPLICANT_DIV"], $extra, 1);

        //志望コース
        $extra = "";
        $query = knjl002vQuery::getCourseCodeMst($model);
        makeCmb($objForm, $arg, $db, $query, "COURSE_DIV", $Row["COURSE_DIV"], $extra, 1);

        //回数
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["FREQUENCY"] = knjCreateTextBox($objForm, $Row["FREQUENCY"], "FREQUENCY", 2, 2, $extra);

        //試験名
        $extra = "";
        $arg["TOP"]["EXAM_NAME"] = knjCreateTextBox($objForm, $Row["EXAM_NAME"], "EXAM_NAME", 40, 20, $extra);

        //試験日（西暦）
        $extra = "";
        $arg["TOP"]["EXAM_DATE"] = View::popUpCalendar2($objForm, "EXAM_DATE", str_replace("-", "/", $Row["EXAM_DATE"]), "", "", $extra);

        //午前/午後コンボ
        $opt[] = array('label' => "", 'value' => "");
        $opt[] = array('label' => '1：午前', 'value' => '1');
        $opt[] = array('label' => '2：午後', 'value' => '2');
        $extra = "";
        $arg["TOP"]["AM_PM"] = knjCreateCombo($objForm, "AM_PM", $Row["AM_PM"], $opt, $extra, 1);

        //受験番号開始番号
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["EXAM_NO_BAND"] = knjCreateTextBox($objForm, $Row["EXAM_NO_BAND"], "EXAM_NO_BAND", 4, 4, $extra);

        knjCreateHidden($objForm, "UPDATED1", $Row["UPDATED1"]);

        //試験科目
        $result = $db->query(knjl002vQuery::getRowExamSubClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $subClassCd = "EXAM_SUBCLASS".$row["EXAM_SUBCLASS"];
            $extra = " id=\"".$subClassCd."\"";

            $extra .= ($row["CHECKED"]) ? " checked": "";
            $row["EXAM_SUBCLASS"] = knjCreateCheckBox($objForm, "EXAM_SUBCLASS[]", $row["EXAM_SUBCLASS"], $extra);
            $row["EXAM_SUBCLASS_NAME"] = "<label for=\"".$subClassCd."\">".$row["EXAM_SUBCLASS_NAME"]."</label>";

            $arg["data"][] = $row;
        }
        $result->free();

        /********/
        /*ボタン*/
        /********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //満点マスタボタン
        $link = REQUESTROOT."/L/KNJL002V/knjl002vindex.php?cmd=perfect";
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_perfect"] = knjCreateBtn($objForm, "btn_perfect", "満点マスタ", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl002vindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl002vForm2.html", $arg);
    }
}

/********************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
