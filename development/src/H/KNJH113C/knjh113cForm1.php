<?php

require_once('for_php7.php');

class knjh113cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["TOP"]["SEMESTER"] = $db->getOne(knjh113cQuery::getSemesterName());

        //資格コンボ
        $query = knjh113cQuery::getQualifiedMst();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SIKAKUCD", $model->sikakuCd, $extra, 1, "");

        //年組コンボ
        $query = knjh113cQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->gradeHrClass, $extra, 1, "");

        //試験日コンボ
        $query = knjh113cQuery::getTestDate($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TEST_DATE", $model->testDate, $extra, 1, "");

        //合否コンボ
        $extra = "";
        $query = knjh113cQuery::getNameMst("Z050");
        makeCmb($objForm, $arg, $db, $query, "JUDGEDIV", $model->judgeDiv, $extra, 1, "");

        //更新ALLチェック
        $chk = ($model->judgediv == "on") ? " checked" : "";
        $extra = "onclick=\"chkDataALL(this);\"";
        $arg["TOP"]["CHK_DATA_ALL"] = knjCreateCheckBox($objForm, "CHK_DATA_ALL", "on", $extra.$chk);

        //一覧表示
        $arr_schregNo = array();
        $schCnt    = 0;
        $passCnt   = 0;
        $pass1Cnt  = 0;
        $unPassCnt = 0;
        $result = $db->query(knjh113cQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $row["LIMITED_DATE"] = str_replace('-', '/', $row["LIMITED_DATE"]);

            //HIDDENに保持する用
            $arr_schregNo[] = $row["SCHREGNO"]."-".$row["TEST_CD"];

            //更新チェック
            $disJdg = ($row["PROCEDUREDIV1"] == "1") ? " disabled" : "";
            $extra = "onclick=\"bgcolorYellow(this, '{$row["SCHREGNO"]}', '{$row["TEST_CD"]}');\"";
            $row["CHK_DATA"] = knjCreateCheckBox($objForm, "CHK_DATA"."-".$row["SCHREGNO"]."-".$row["TEST_CD"], "on", $extra.$disJdg);

            //人数カウント
            $schCnt++;
            $passCnt   = ($row["PASS_CHECK"] == "0") ? $passCnt + 1: $passCnt;
            $pass1Cnt  = ($row["PASS_CHECK"] == "1") ? $pass1Cnt + 1: $pass1Cnt;
            $unPassCnt = ($row["PASS_CHECK"] == "9") ? $unPassCnt + 1: $unPassCnt;

            $arg["data"][] = $row;
        }
        $arg["SCH_COUNT"]    = $schCnt;
        $arg["PASS_COUNT"]   = $passCnt;
        $arg["PASS1_COUNT"]  = $pass1Cnt;
        $arg["UNPASS_COUNT"] = $unPassCnt;

        //ボタン作成
        //更新ボタン
        $disBtn = (0 < get_count($arr_schregNo)) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('update');\"".$disBtn;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_SCHREGNO", implode(",",$arr_schregNo));
        knjCreateHidden($objForm, "HID_SIKAKUCD");
        knjCreateHidden($objForm, "HID_GRADE_HR_CLASS");
        knjCreateHidden($objForm, "HID_TEST_DATE");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjh113cindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh113cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "TEST_DATE") {
            $row["LABEL"] = str_replace('-', '/', $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
