<?php

require_once('for_php7.php');

class knjz182rForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz182rindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //学年コンボ
        $query = knjz182rQuery::getGrade();
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "BLANK");

        if ($model->div === '3') {
            //コースコンボ
            $arg["CourseMajor"] = "1";
            $arg["SET_DIV_NAME"] = 'コース';
            $extra = "onChange=\"return btn_submit('change');\"";
            $query = knjz182rQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, $model->course, "COURSE", $extra, 1, "BLANK");
            list($coursecd, $majorcd, $coursecode) = explode("-", $model->course);
            $model->coursecd        = $coursecd;
            $model->groupcd         = $majorcd;
            $model->coursecode      = $coursecode;
            $model->hr_class        = '000';
        } else {
            //コースグループコンボ
            $arg["CourseGroup"] = "1";
            $arg["SET_DIV_NAME"] = 'コースグループ';
            $query = knjz182rQuery::getGroupCd($model);
            $extra = "onChange=\"btn_submit('change')\";";
            makeCmb($objForm, $arg, $db, $query, $model->groupcd, "GROUP_CD", $extra, 1, "BLANK");
            $model->coursecd        = '0';
            $model->coursecode      = '0000';
            $model->hr_class        = '000';
        }

        //評定段階数の取得
        $assesslevelcnt = $db->getOne(knjz182rQuery::getAssessLevelCnt($model)); //評定段階数の初期値
        if ($model->cmd == 'level') {
            $assesslevelcnt = $model->field2["ASSESSLEVELCNT"];
        }
        $model->setAssesslevelCount = ($assesslevelcnt > 0) ? $assesslevelcnt : "5";
        //評定段階数
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $model->setAssesslevelCount, "ASSESSLEVELCNT", 2, 2, $extra);

        //段階リスト作成
        makeAssessList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) {   //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz182rindex.php?cmd=up_list&shori=add','left_frame');";
        }

        View::toHTML($model, "knjz182rForm2.html", $arg); 
    }
}

//リスト作成
function makeAssessList(&$objForm, &$arg, $db, $model) {
    //警告メッセージを表示しない場合
    if (!isset($model->warning)) {
        $query = knjz182rQuery::getRow($model);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
    } else {
        //初期化
        $model->field2 = array();
        $Row =& $model->field2;
    }

    for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
        //段階値
        $setRow["RANK_LEVEL"] = $i;

        //記号
        $extra = " STYLE=\"text-align:right;\"";
        $setRow["RANK_MARK"] = knjCreateTextBox($objForm, $Row["RANK_MARK_".$i], "RANK_MARK_".$i, 3, 3, $extra);

        //下限
        if ($i == 1) {
            if (!$Row["RANK_LOW_".$i]) {
                $Row["RANK_LOW_".$i] = 1;
            }
            $extra = " STYLE=\"text-align:right;\"";
        } else {
            $extra = " STYLE=\"text-align:right;\" onblur=\"isNumbJyougen(this, ".$i.");\"";
        }
        $setRow["RANK_LOW"] = knjCreateTextBox($objForm, $Row["RANK_LOW_".$i], "RANK_LOW_".$i, 3, 3, $extra);

        //上限
        if ($i == $model->setAssesslevelCount) {
            $extra = " STYLE=\"text-align:right;\"";
            $setRow["RANK_HIGH"] = knjCreateTextBox($objForm, $Row["RANK_HIGH_".$i], "RANK_HIGH_".$i, 3, 3, $extra);
        } else {
            $setRow["RANK_HIGH"]  = "<span id=\"RANK_HIGH_".$i."\">";
            $setRow["RANK_HIGH"] .= $Row["RANK_HIGH_".$i];
            $setRow["RANK_HIGH"] .= "</span>";
            knjCreateHidden($objForm, "RANK_HIGH_".$i, $Row["RANK_HIGH_".$i]);
        }

        $arg["data"][] = $setRow;
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //確定ボタン
    $extra = "onclick=\"return level({$model->setAssesslevelCount});\" ";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\" ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\" ";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\" ";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\" ";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
