<?php

require_once('for_php7.php');
class knjb3042ChairInfo
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjb3042index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["useTestFacility"] == "1") {
            $arg['useTestFacilityFlg'] = '1';
        }

        //講座コンボ
        $query = knjb3042Query::getCiChairDat($model);
        $extra = "onchange=\"return btn_submit('chairInfo');\"";
        makeCmb($objForm, $arg, $db, $query, $model->ciField["CI_CHAIRCD"], "CI_CHAIRCD", $extra, 1);

        $result = $db->query($query);
        list($gradeHr, $countLesson) = explode('/', $model->chairInfo["SEND_KOMA_COUNT_LESSON"]);
        list($sendCountFlg, $sendLesson) = explode('-', $countLesson);

        $datCountFlg = $sendCountFlg;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['VALUE'] == $model->ciField['CI_CHAIRCD']) {
                $arg['data']['CI_CHAIRCD_DISP'] = $row['LABEL'];
                if ($model->chairInfo["SEND_KOMA_COUNT_LESSON"] == '00-000/1-00') {
                    $datCountFlg = $row['COUNTFLG'];
                }
                break;
            }
        }

        //日付
        if ($model->field['SCH_DIV'] == '1') {
            foreach ($model->weekPtrn as $weekKey => $weekVal) {
                if ($weekVal['CD'] == $model->chairInfo["SEND_DATE"]) {
                    $dispDate = "({$weekVal['WEEK_JP']})";
                    break;
                }
            }
            $arg["data"]["SCH_DIV_FLAG_1"] = true;
        } else {
            list($year, $month, $day) = preg_split("/-/", $model->chairInfo["SEND_DATE"]);
            $timestamp = mktime(0, 0, 0, $month, $day, $year);
            $setWeek = $model->week[date('w', $timestamp)];
            $dispDate = str_replace("-", "/", $model->chairInfo["SEND_DATE"])."(".$setWeek["WEEK_JP"].")";
            $arg["data"]["SCH_DIV_FLAG_2"] = true;
        }
        $arg["data"]["CI_SYORIBI"] = $dispDate;

        //校時
        $arg["data"]["CI_PERIOD"] = $model->periodArray[$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]]["LABEL"];

        //科目
        $query = knjb3042Query::getCiSubclass($model);
        $arg["data"]["CI_SUBCLASS"] = $db->getOne($query);

        //講座情報
        $query = knjb3042Query::getChairInfo($model);
        $rowChairInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->chairInfo["GROUPCD"] = $rowChairInfo["GROUPCD"];
        $model->chairInfo["GROUPNAME"] = $rowChairInfo["GROUPNAME"];
        $model->chairInfo["FRAMECNT"] = $rowChairInfo["FRAMECNT"];
        $model->chairInfo["LESSONCNT"] = $rowChairInfo["LESSONCNT"];
        //群
        $arg["data"]["CI_GUN"] = $model->chairInfo["GROUPCD"] > 0 ? $model->chairInfo["GROUPCD"].":".$model->chairInfo["GROUPNAME"] : "";

        //連続授業・週授業回数
        $setLessonCnt = $model->chairInfo["FRAMECNT"] > 0 ? "連".$model->chairInfo["FRAMECNT"] : "";
        $setFrameCnt = $model->chairInfo["LESSONCNT"] > 0 ? "週".$model->chairInfo["LESSONCNT"] : "";
        $arg["data"]["CI_LESSON"] = $setLessonCnt != "" && $setFrameCnt != "" ? $setLessonCnt.",".$setFrameCnt : $setLessonCnt.$setFrameCnt;

        //講座クラス
        $query = knjb3042Query::getChairClass($model);
        $result = $db->query($query);
        $model->chairClass = array();
        $model->chairClassCL = array();
        $setChrClass = "";
        $setHrNameArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->chairClass[] = $row["VALUE"];
            $setHrNameArray[$row["VALUE"]] = $row["LABEL"];
            $model->chairClassCL[$row["VALUE"]] = $row["VALUE"].':'.$row["LABEL"].':';
            $setChrClass .= $sep.$row["LABEL"];
            $sep = ",";
        }
        $result->free();
        $arg["data"]["CI_CHRCLASS"] = $setChrClass;

        //時間割担当
        if ($model->field['SCH_DIV'] == '1') {
            $query = knjb3042Query::getCiPtrnStaff($model);
        } else {
            $query = knjb3042Query::getCiSchStaff($model);
        }
        $result = $db->query($query);
        $schStaff = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schStaff[] = $model->fusemozi($row["VALUE"]).':'.$row["LABEL"];
        }
        $result->free();
        $arg["data"]["CI_SCHSTAFF"] = implode(",", $schStaff);

        //講座担当
        $query = knjb3042Query::getCiChairStaff($model);
        $result = $db->query($query);
        $chairStaff = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chairStaff[] = $model->fusemozi($row["VALUE"]).':'.$row["LABEL"];
        }
        $result->free();
        $arg["data"]["CI_CHAIRSTAFF"] = implode(",", $chairStaff);

        //施設
        if ($model->field['SCH_DIV'] == '1') {
            $query = knjb3042Query::getCiPtrnFacility($model);
        } else {
            $query = knjb3042Query::getCiFacility($model);
        }
        $result = $db->query($query);
        $facilyty = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $facilyty[] = $row["LABEL"];
        }
        $result->free();
        $arg["data"]["CI_FACILYTY"] = implode(",", $facilyty);

        $testFacilyty = array();
        if ($model->Properties["useTestFacility"] == "1") {
            //試験会場
            if ($model->field['SCH_DIV'] == '1') {
                $query = knjb3042Query::getCiPtrnTestFacility($model);
            } else {
                $query = knjb3042Query::getCiTestFacility($model);
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $testFacilyty[] = $row["LABEL"];
            }
            $result->free();
        }
        $arg["data"]["CI_TESTFACILYTY"] = implode(",", $testFacilyty);

        if ($model->field['SCH_DIV'] == '2') {
            if ($model->ciField["CI_TEST"] == '0') {
                $setDataDiv = "通常時間割で編集";
            } else {
                $setDataDiv = "定期考査で編集";
            }
            $arg["data"]["CI_DATADIV"] = $setDataDiv;
        }

        //実施区分
        $setExecuted = "未";
        if ($rowCiSchChair["EXECUTED"] == "1") {
            $setExecuted = "済";
        }
        $arg["data"]["CI_EXECUTED"] = $setExecuted;

        //集計フラグ/授業形態パラメーター用
        if ($model->field['SCH_DIV'] == '1') {
            $query = knjb3042Query::getPtrnCountFlgDatOne($model);
        } else {
            $query = knjb3042Query::getSchCountFlgDatOne($model);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->chairClassCL[$row["GRADEHR"]] = $row['GRADEHR'].':'.$setHrNameArray[$row['GRADEHR']].':'.$row['COUNTFLG'].':'.$row['LESSON_MODE'];
        }
        $result->free();

        //集計フラグ、授業形態
        $countLessonObj = array();
        if ($model->ciField["CI_COUNT_LESSON"] != '') {
            $countLessonList = explode(':', $model->ciField["CI_COUNT_LESSON"]);
            for ($i = 0; $i < get_count($countLessonList); $i++) {
                $countLessonListParts = explode('/', $countLessonList[$i]);
                list($countFlg, $lessonMode) = explode('-', $countLessonListParts[1]);
                $countLessonObj[] = array('countFlg' => $countFlg, 'lessonMode' => $lessonMode);
            }
        }
        $countFlgCheck = null;
        $lessonModeCheck = null;
        for ($i = 0; $i < get_count($countLessonObj); $i++) {
            if (!isset($countFlgCheck)) {
                $countFlgCheck = $countLessonObj[$i]['countFlg'];
            } elseif ($countFlgCheck != $countLessonObj[$i]['countFlg']) {
                $countFlgCheck = 2;
            }
            if (!isset($lessonModeCheck)) {
                $lessonModeCheck = $countLessonObj[$i]['lessonMode'];
            } elseif ($lessonModeCheck != $countLessonObj[$i]['lessonMode']) {
                $lessonModeCheck = -1;
            }
        }
        if ($countFlgCheck == 0) {
            $arg["data"]["CI_COUNTFLG"] = '集計しない';
        } elseif ($countFlgCheck == 1) {
            $arg["data"]["CI_COUNTFLG"] = '集計する';
        } elseif ($countFlgCheck == 2) {
            $arg["data"]["CI_COUNTFLG"] = '一部集計';
        }

        if ($model->chairInfo["SEND_KOMA_COUNT_LESSON"] == '00-000/1-00') {
            $arg["data"]["CI_COUNTFLG"] = $datCountFlg == '1' ? '集計する' : '集計しない';
        }

        if ($model->field['SCH_DIV'] == '2') {
            if ($lessonModeCheck == -1) {
                $arg["data"]["CI_LESSON_MODE"] = '混在';
            } else {
                $result = $db->query(knjb3042Query::getLessonModeAjax());
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row['VALUE'] == $lessonModeCheck) {
                        $arg["data"]["CI_LESSON_MODE"] = $row['LABEL'];
                        break;
                    }
                }
            }
        }

        //出欠済み
        $setExecInfo = "";
        if ($model->ciField["CI_EXEC"] == "SYUKKETSU") {
            $setExecInfo = "出欠済み";
        } elseif ($model->ciField["CI_EXEC"] == "MI_SYUKKETSU") {
            $setExecInfo = "未出欠";
        } elseif ($model->ciField["CI_EXEC"] == "ITIBU_SYUKKETSU") {
            $setExecInfo = "一部出欠済み";
        }
        $arg["data"]["CI_EXEC_DISP"] = $setExecInfo;

        if ($model->ciField["CI_TEST"] != '0') {
            $result = $db->query(knjb3042Query::getTestCombo($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row['VALUE']==$model->ciField["CI_TEST"]) {
                    $arg["data"]["CI_TEST_DISP"] = $row['LABEL'] .'　'. (($row['COUNTFLG'] == 1)?'集計あり':'集計なし');
                    break;
                }
            }
        }

        $arg['data']['HIDUKEKOUZI_DIALOG']="<a href=\"javascript:window.parent.showDialog('hidukeKouziBox','日付校時編集',window.parent.hidukekouziInitFunc)\">／</a>";
        //施設
        $arg['data']['FACILYTY_DIALOG']="<a href=\"javascript:window.parent.showDialog('facilityBox','施設編集',window.parent.facilityInitFunc)\">／</a>";
        //試験会場
        $arg['data']['TESTFACILYTY_DIALOG']="<a href=\"javascript:window.parent.showDialog('testFacilityBox','試験会場編集',window.parent.testFacilityInitFunc)\">／</a>";
        //集計フラグ/授業形態
        $setGradeHr = implode(",", $model->chairClassCL);
        $arg['data']['COUNTLESSON_DIALOG']="<a href=\"javascript:window.parent.showDialog('countLessonBox','集計フラグ/授業形態編集',window.parent.countLessonInitFunc('{$setGradeHr}', '{$datCountFlg}'))\">／</a>";
        //時間割種別選択
        $arg['data']['TEST_DIALOG'] = "<a href=\"javascript:window.parent.showDialog('testSelectBox','時間割種別選択',window.parent.testInitFunc)\">／</a>";

        if ($model->Properties["KNJB3042_SchTestPattern"] == "1" && $model->field['SCH_DIV'] != '3' && $model->ciField["CI_TEST"] != '0') {
            $arg['data']['HIDUKEKOUZI_DIALOG'] = '';
            $arg['data']['FACILYTY_DIALOG'] = '';
            $arg['data']['COUNTLESSON_DIALOG'] = '';
            $arg['data']['TEST_DIALOG'] = '';
        }

        if ($model->ciField["CI_TEST"] != '0') {
            $arg['data']['COUNTLESSON_DIALOG'] = '';
        }
        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "combo") {
            $arg["reload"] = "window.open('knjb3042index.php?cmd=edit','right_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3042ChairInfo.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($name == "CI_CHAIRCD") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

    $result->free();
}
