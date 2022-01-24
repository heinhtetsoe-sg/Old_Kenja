<?php

require_once('for_php7.php');

class knjd128qForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd128qindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //必須指定FLG
        $hissuUnSelectCnt = 0;

        //学期
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjd128qQuery::getSemester($model);
        $arg["SEMESTER"] = makeCmbReturn($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "BLANK");
        $hissuUnSelectCnt += $model->semester ? 0 : 1;
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //課程学科
            $extra = "onChange=\"btn_submit('edit');\"";
            $query = knjd128qQuery::getCourseMajor($model);
            $arg["COURSE_MAJOR"] = makeCmbReturn($objForm, $arg, $db, $query, $model->courseMajor, "COURSE_MAJOR", $extra, 1, "BLANK");
            $hissuUnSelectCnt += $model->courseMajor ? 0 : 1;
        }

        //テスト
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjd128qQuery::getTest($model);
        $arg["TESTCD"] = makeCmbReturn($objForm, $arg, $db, $query, $model->testCd, "TESTCD", $extra, 1, "BLANK");
        $hissuUnSelectCnt += $model->testCd ? 0 : 1;

        //学年
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjd128qQuery::getGrade($model);
        $arg["GRADE"] = makeCmbReturn($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "BLANK");
        $hissuUnSelectCnt += $model->grade ? 0 : 1;

        //年組
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjd128qQuery::getHrClass($model);
        $arg["HR_CLASS"] = makeCmbReturn($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1, "BLANK");
        $hissuUnSelectCnt += $model->hr_class ? 0 : 1;

        //科目
        $query = knjd128qQuery::getSubclass($model);
        $result = $db->query($query);
        $model->subclassArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->subclassArray[] = $row;
        }
        $result->free();
        foreach ($model->subclassArray as $key => $val) {
            $arg["TITLE"][] = $val;
            $setTitle2 = array("TOKUTEN" => "得点", "SAISI" => "再試");
            $arg["TITLE2"][] = $setTitle2;
        }
        $arg["setWidth"] = get_count($model->subclassArray) * 105;

        //一覧を取得
        $model->setList = array();
        if (!isset($model->warning) && $hissuUnSelectCnt == 0) {
            $query = knjd128qQuery::getSchregList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->setList[] = $row;
            }
            $result->free();
        }

        //一覧を表示
        $paidFlg = false;
        $model->updateKey = array();
        foreach ($model->setList as $counter => $Row) {
            $setData = array();
            $setData["ATTENDNO"] = $Row["HR_NAME"]."<BR>".$Row["ATTENDNO"]."番";
            $setData["NAME_SHOW"] = $Row["NAME"];
            list($listData, $isDisp) = makeListData($objForm, $arg, $db, $model, $Row["SCHREGNO"]);
            if ($isDisp) {
                $arg["data"][] = $setData;
                $arg["data2"][] = $listData;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $paidFlg);

        //hidden作成
        makeHidden($objForm, $model);

        if ($paidFlg) {
            $arg["jscript"] = "alert('入金済みの生徒が存在する為。更新できません。');";
        }

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjd128qForm1.html", $arg); 
    }
}
//hidden作成
function makeListData(&$objForm, &$arg, $db, &$model, $schregNo) {
    //表示データ
    $retListData["LISTDATA"] = "";
    //生徒表示/非表示
    $retDisp = false;
    //全得点取得
    $scoreQuery = knjd128qQuery::getScore($model, $schregNo);
    $result = $db->query($scoreQuery);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $scoreArray[$row["SUBCLASSCD"]]["SCORE"] = $row["SCORE"];
        $scoreArray[$row["SUBCLASSCD"]]["PASS_SCORE"] = $row["PASS_SCORE"];
        $scoreArray[$row["SUBCLASSCD"]]["VALUE_DI"] = $row["VALUE_DI"];
    }
    $result->free();

    //全追試点取得
    $suppQuery = knjd128qQuery::getSupp($model, $schregNo);
    $result = $db->query($suppQuery);
    $suppArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $suppArray[$row["SUBCLASSCD"]] = $row["SCORE_DI"] ? $row["SCORE_DI"] : $row["SCORE"];
    }
    $result->free();
    foreach ($model->subclassArray as $key => $val) {
        $getScore = $scoreArray[$val["SUBCLASSCD"]]["SCORE"];
        $perfectPassScore = $scoreArray[$val["SUBCLASSCD"]]["PASS_SCORE"];
        $valueDi = $scoreArray[$val["SUBCLASSCD"]]["VALUE_DI"];
        $perfectPassScore = $perfectPassScore ? $perfectPassScore : 50;
        $checkScore = $getScore ? $getScore : 0;
        if ($perfectPassScore > $checkScore) {
            $retDisp = true;
        }
        $setScoreVal = $valueDi ? $valueDi : $getScore;
        $setScore = "<td width=\"41px\" align=\"center\">{$setScoreVal}</td>";

        $getSupp = $suppArray[$val["SUBCLASSCD"]];
        $extra = "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\"";
        if (($perfectPassScore > $checkScore && strlen($getScore) > 0) || $valueDi == "*") {
            $getSupp = knjCreateTextBox($objForm, $getSupp, "SUPP_SCORE_{$schregNo}_{$val["SUBCLASSCD"]}", 3, 3, $extra);
            list($classCd, $schoolKind, $curriculum, $subclassCd) = preg_split("/-/", $val["SUBCLASSCD"]);
            $model->updateKey[] = array("SCHREGNO"       => $schregNo,
                                        "CLASSCD"        => $classCd,
                                        "SCHOOL_KIND"    => $schoolKind,
                                        "CURRICULUM_CD"  => $curriculum,
                                        "SUBCLASSCD"     => $subclassCd,
                                        "SUBCLASSCD_ALL" => $val["SUBCLASSCD"],
                                        "PASS_SCORE"     => $perfectPassScore
                                        );
        }
        $setSuppScore = "<td width=\"56px\" align=\"center\">{$getSupp}</td>";

        $retListData["LISTDATA"] .= $setScore.$setSuppScore;
    }
    return array($retListData, $retDisp);
}
//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $paidFlg) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $extra .= $paidFlg ? " disabled " : "";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
    knjCreateHidden($objForm, "H_HR_CLASS");
}
?>
