<?php

require_once('for_php7.php');

class knjb3042SchTestPattern
{
    public function main(&$model)
    {
        set_time_limit(600);

        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb3042index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $GLOBALS['global_time_start'] = microtime();

        if ($model->cmd == "getChair") {
            //講座コンボ
            $response = makeChairList($objForm, $arg, $db, $model, "returnGe");

            echo $response;
            die();
        }
        if ($model->cmd == 'getChairData') {
            $query = knjb3042Query::getChairDataAjax($model);
            $result = $db->query($query);
            $response = null;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response['kamoku'] = array('SUBCLASSNAME'=>$row["SUBCLASSNAME"],'SUBCLASSCD'=>$row["SUBCLASSCD"]);
                $response['gun'] = array('CHAIRNAME'=>$row["GROUPNAME"],'GROUPCD'=>$row["GROUPCD"]);
                break;
            }

            echo json_encode($response);
            die();
        }
        if ($model->cmd == "getMeiboParam") {
            $meiboParam = json_decode($model->ajaxParam['AJAX_MEIBO_PARAM'], true);
            $response = getMeiboExec($db, $model, $meiboParam);
            echo json_encode($response);
            die();
        }
        if ($model->cmd == "getMeiboAndFacParam") {
            $meiboFacParam = json_decode($model->ajaxParam['AJAX_MEIBO_FAC_PARAM'], true);
            $response = checkMeiboAndFac($db, $model, $meiboFacParam);
            echo json_encode($response);
            die();
        }
        if ($model->cmd == 'getGunCode') {
            $response = array();
            $GunParam = json_decode($model->ajaxParam['AJAX_GUN_PARAM'], true);
            for ($i = 0; $i < get_count($GunParam); $i++) {
                $query = knjb3042Query::getGunCodeAjax($model, $GunParam[$i]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $response[] = array('Kouza'=>$GunParam[$i],'GunCode'=>$row["GROUPCD"],'GunName'=>$row["CHAIRNAME"]);
                    break;
                }
            }
            echo json_encode($response);
            die();
        }

        if ($model->cmd == 'getFacilityKouzaList' || $model->cmd == 'getTestFacilityKouzaList') {
            $response = array();
            $KouzaParam = json_decode($model->ajaxParam['AJAX_KOUZA_PARAM'], true);
            for ($i = 0; $i < get_count($KouzaParam); $i++) {
                $query = knjb3042Query::getFacilityKouzaKamokuAjax($model, $KouzaParam[$i]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $response[] = array('Kouza' => $KouzaParam[$i],
                                        'KouzaName' => $row["CHAIRNAME"],
                                        'GunCode' => $row["GROUPCD"],
                                        'GunName' => $row["GROUPNAME"],
                                        'Kamoku' => $row["SUBCLASSCD"],
                                        'KamokuName' => $row["SUBCLASSNAME"]);
                }
            }
            echo json_encode($response);
            die();
        }

        if ($model->cmd == 'getFacilitySelect' || $model->cmd == 'getTestFacilitySelect') {
            $response = array();
            $KouzaSelect = $model->ajaxParam['AJAX_KOUZA_SELECT'];

            $query = knjb3042Query::getKouzaFacilityAjax($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = array('Faccd' => $row["FACCD"],
                                    'FacilityName' => $row["FACCD"].":".$row["FACILITYNAME"]);
            }
            echo json_encode($response);
            die();
        }

        if ($model->cmd == 'getFacility' || $model->cmd == 'getTestFacility') {
            $response = "";
            $query = knjb3042Query::getFacilityAjax($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response .= "<option value=\"{$row["FACCD"]}\">{$row["FACCD"]} : {$row["FACILITYNAME"]}</option>";
            }
            echo $response;
            die();
        }

        if ($model->cmd == 'getLessonModeList') {
            $query = knjb3042Query::getLessonModeAjax();
            $extra = " id=\"DLOG_LESSONMODE\"";
            $response = makeCmb($objForm, $arg, $db, $query, $value, "DLOG_LESSONMODE", $extra, 1, 'BLANK', '1');
            echo $response;
            die();
        }

        if ($model->cmd == 'getLayoutStaffChair' || $model->cmd == 'getLayoutHrSubclass') {
            if ($model->cmd == 'getLayoutStaffChair') {
                $query = knjb3042Query::getLayoutStaffChair($model);
                $selectedName = "category_staffchair_selected";
                $upperName = "StaffChair";
            } elseif ($model->cmd == 'getLayoutHrSubclass') {
                $query = knjb3042Query::getLayoutHrSubclass($model);
                $selectedName = "category_hrsubclass_selected";
                $upperName = "HrSubclass";
            }
            $selectList = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->leftMenu == '1') {
                    $selectList[] = array('label' => $model->fusemozi($row["VALUE"]).'：'.$row["LABEL"], 'value' => $row["VALUE"]);
                } else {
                    $selectList[] = array('label' => $row["VALUE"].'：'.$row["LABEL"], 'value' => $row["VALUE"]);
                }
            }
            $result->free();
            if ($model->ajaxParam["LAYOUT_LIST_NAME"] == $selectedName) {
                $side = "left";
            } else {
                $side = "right";
            }
            $extra = "id=\"{$model->ajaxParam["LAYOUT_LIST_NAME"]}\" multiple style=\"width:300px; height:350px\" ondblclick=\"layoutMove('{$side}', '{$upperName}');\"";
            $response = knjCreateCombo($objForm, $model->ajaxParam["LAYOUT_LIST_NAME"], '', $selectList, $extra, 20);

            echo $response;
            die();
        }
        if ($model->cmd == 'getincludingChairParam') {
            $response = array();
            $includingList = json_decode($model->ajaxParam['AJAX_INCLUDING_CHAIR_PARAM'], true);

            $query = knjb3042Query::includingChairAJAX($model, $includingList['targetDay'], $includingList['period'], $includingList['list']);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            echo json_encode($response);
            die();
        }
        if ($model->cmd == 'dipliStdViewParam') {
            $response = array();
            $includingList = json_decode($model->ajaxParam['AJAX_DIPLISTDVIEW_PARAM'], true);

            $query = knjb3042Query::dipliStdViewAJAX($model, $includingList['targetDay'], $includingList['period'], $includingList['list']);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            echo json_encode($response);
            die();
        }
        // 背景色変更ダイアログ－講座取得
        if ($model->cmd == 'getBackColorChair') {
            $response = array();
            $query = knjb3042Query::getChair($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = array('label' => $row["LABEL"], 'value' => $row["CHAIRCD"]);
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // 背景色変更ダイアログ－講座人数オーバー取得
        if ($model->cmd == 'getBackColorChairCapaOver') {
            $response = array();
            for ($i=0; $i < get_count($model->ajaxParam['UPDDATE']); $i++) {
                $dayKey = $model->ajaxParam['UPDDATE'][$i];
                $query = knjb3042Query::getChairStdCapaOver($model, $dayKey);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $response[$dayKey][] = $row["CHAIRCD"];
                }
                $result->free();
            }
            echo json_encode($response);
            die();
        }
        // 背景色変更ダイアログ－同一名簿取得
        if ($model->cmd == 'getBackColorChairSameMeibo') {
            $response = array();
            for ($i=0; $i < get_count($model->ajaxParam['UPDDATE']); $i++) {
                $dayKey = $model->ajaxParam['UPDDATE'][$i];
                $query = knjb3042Query::getChairStdSameMeibo($model, $dayKey);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $response[$dayKey][] = $row["CHAIRCD"];
                }
                $result->free();
            }
            echo json_encode($response);
            die();
        }
        // 背景色変更ダイアログ－HRクラス名簿取得
        if ($model->cmd == 'getBackColorHrClassStdMeibo') {
            $response = array();
            $query = knjb3042Query::getHrClassStdMeibo($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // 背景色変更ダイアログ－生徒の受講講座一覧取得
        if ($model->cmd == 'getBackColorStdChair') {
            $response = array();
            for ($i=0; $i < get_count($model->ajaxParam['UPDDATE']); $i++) {
                $dayKey = $model->ajaxParam['UPDDATE'][$i];
                $query = knjb3042Query::getStdChair($model, $dayKey);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $response[$dayKey][] = $row["CHAIRCD"];
                }
                $result->free();
            }
            echo json_encode($response);
            die();
        }

        if ($model->cmd == 'selectOperationHistory') {
            $response = array();
            makeOpeLogList($objForm, $arg, $db, $model);
            if ($arg['OpeLog']) {
                $response = $arg['OpeLog'];
            }
            echo json_encode($response);
            die();
        }


        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //曜日
        $model->week = array();
        $model->week[] = array("CD" => "1", "WEEK_JP" => "日", 'CSS' => 'week_niti');
        $model->week[] = array("CD" => "2", "WEEK_JP" => "月", 'CSS' => 'week_getu');
        $model->week[] = array("CD" => "3", "WEEK_JP" => "火", 'CSS' => 'week_ka');
        $model->week[] = array("CD" => "4", "WEEK_JP" => "水", 'CSS' => 'week_sui');
        $model->week[] = array("CD" => "5", "WEEK_JP" => "木", 'CSS' => 'week_moku');
        $model->week[] = array("CD" => "6", "WEEK_JP" => "金", 'CSS' => 'week_kin');
        $model->week[] = array("CD" => "7", "WEEK_JP" => "土", 'CSS' => 'week_do');

        //曜日基本時間割用
        $model->weekPtrn = array();
        $model->weekPtrn[] = array("RENBAN" => "0", "CD" => "2", "DATE_OR_WEEK" => "2", "WEEK_JP" => "月", 'CSS' => 'week_getu');
        $model->weekPtrn[] = array("RENBAN" => "1", "CD" => "3", "DATE_OR_WEEK" => "3", "WEEK_JP" => "火", 'CSS' => 'week_ka');
        $model->weekPtrn[] = array("RENBAN" => "2", "CD" => "4", "DATE_OR_WEEK" => "4", "WEEK_JP" => "水", 'CSS' => 'week_sui');
        $model->weekPtrn[] = array("RENBAN" => "3", "CD" => "5", "DATE_OR_WEEK" => "5", "WEEK_JP" => "木", 'CSS' => 'week_moku');
        $model->weekPtrn[] = array("RENBAN" => "4", "CD" => "6", "DATE_OR_WEEK" => "6", "WEEK_JP" => "金", 'CSS' => 'week_kin');
        $model->weekPtrn[] = array("RENBAN" => "5", "CD" => "7", "DATE_OR_WEEK" => "7", "WEEK_JP" => "土", 'CSS' => 'week_do');
        $model->weekPtrn[] = array("RENBAN" => "6", "CD" => "1", "DATE_OR_WEEK" => "1", "WEEK_JP" => "日", 'CSS' => 'week_niti');

        if ($model->cmd != '' && $model->cmd != 'editSchDiv') {
            $arg['showMain'] = '1';
            $arg['showDummy'] = '';
        } else {
            $arg['showMain'] = '';
            $arg['showDummy'] = '1';
        }

        if ($model->Properties["useSchool_KindField"] == "1") {
            $arg['useSchool_KindField'] = '1';
            $query = knjb3042Query::getSchoolKind($model);
            $extra = "onchange=\"staffCmbChage()\"";
            makeCmb($objForm, $arg, $db, $query, $val, "schoolKind_STAFF", $extra, 1, "BLANK");
            $extra = "onchange=\"hrSubclassCmbChage()\"";
            makeCmb($objForm, $arg, $db, $query, $val, "schoolKind_HrSubClass", $extra, 1, "BLANK");
        }

        //時間割種別
        $opt = array(1, 2, 3);
        $model->field['SCH_DIV'] = ($model->field['SCH_DIV'] == "") ? '2' : $model->field['SCH_DIV'];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SCH_DIV{$val}\" onClick=\"btn_submit('editSchDiv')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SCH_DIV", $model->field['SCH_DIV'], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        if ($model->field["SCH_DIV"] == '1') {
            $arg['schPtrn'] = '1';
        } else {
            $arg['schChair'] = '1';
        }

        //年学期
        $query = knjb3042Query::getYearSemester();
        $extra = "onChange=\"btn_submit('editSchDiv')\"";
        makeCmb($objForm, $arg, $db, $query, $model->yearSeme, "YEAR_SEME", $extra, 1, "");

        //学期情報
        $query = knjb3042Query::getSemesterInfo($model);
        $model->semesterInfo = array();
        $model->semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //開始日付
        $model->startDate = $model->startDate ? $model->startDate : CTRL_DATE;
        if (str_replace("/", "-", $model->startDate) < $model->semesterInfo["SDATE"] || str_replace("/", "-", $model->startDate) > $model->semesterInfo["EDATE"]) {
            $model->startDate = $model->semesterInfo["SDATE"];
        }
        $param = "extra=dateChange(f.document.forms[0][\\'START_DATE\\'].value);";
        $arg["START_DATE"] = View::popUpCalendar($objForm, "START_DATE", str_replace("-", "/", $model->startDate), $param);
        //終了日付
        $defaultTime = date('Y-m-d', strtotime($model->startDate.' +1 week')-24*60*60);
        $endTime = date('Y-m-d', strtotime($model->startDate.' +2 week')-24*60*60);
        $model->endDate = empty($model->endDate) ? $defaultTime : $model->endDate;
        if (str_replace("/", "-", $model->endDate) < str_replace("/", "-", $model->startDate)) {
            $model->endDate = $endTime;
        }
        if (str_replace("/", "-", $model->endDate) > $endTime) {
            $model->endDate = $endTime;
        }
        if (str_replace("/", "-", $model->endDate) > $model->semesterInfo["EDATE"]) {
            $model->endDate = $model->semesterInfo["EDATE"];
        }
        $arg["END_DATE"] = View::popUpCalendar($objForm, "END_DATE", str_replace("-", "/", $model->endDate));

        //タイトル
        $query = knjb3042Query::getSchPtrnHdat($model);
        $opt = array();
        $opt[] = array("label" => "(新規)", "value" => "0");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateTime = explode('.', $row["UPDATED"]);
            list($setDate, $setTime) = explode(' ', $dateTime[0]);
            $setWeek = $model->week[date('w', $row["UPDATED"])];
            $dispDate = str_replace("-", "/", $setDate)."({$setWeek["WEEK_JP"]}) {$setTime}";
            $row["BSCSEQ"] = sprintf('%02d', $row["BSCSEQ"]);

            $opt[] = array('label' => "{$row["BSCSEQ"]} {$dispDate} {$row["TITLE"]}",
                           'value' => $row["BSCSEQ"]);
        }
        $model->field['BSCSEQ'] = strlen($model->field['BSCSEQ']) > 0 ? $model->field['BSCSEQ'] : $opt[0]["value"];
        $extra = "id=\"BSCSEQ\" onChange=\"btn_submit('editSchDiv')\"";
        $arg['BSCSEQ'] = knjCreateCombo($objForm, 'BSCSEQ', $model->field['BSCSEQ'], $opt, $extra, $size);

        //画面左側の切り替え(職員、年組)
        $arg["LEFT_MENU"] = 'テスト考査'.knjCreateHidden($objForm, "LEFT_MENU", $model->leftMenu);

        //左のタイトル
        $arg["LEFT_TITLE"] = 'No';

        //ゴミ箱
        if ($model->auth == DEF_UPDATABLE) {
            $arg["TRASH_BOX"] .= "<div id=\"TRASH_BOX\" ondragover=\"f_dragover(event,this)\" ondragleave=\"f_dragleave(event,this)\" ondrop=\"f_dropTrash(event, this)\">ゴミ箱：枠内にドロップして下さい</div>";
        }

        //校時
        $namecd = ($model->Properties["KNJB3042_SchTestPattern"] == "1" && $model->field['SCH_DIV'] == '3') ? 'B004' : 'B001';
        $query = knjb3042Query::getPeriod($model, $namecd);
        $result = $db->query($query);
        /*
        * periodArray
        * LABEL:校時名称
        * PERI_YOMIKAE:連番(校時コードにアルファベットがある為)
        */
        $model->periodArray = array();
        $model->maxPeri = 0;
        $model->periYomikae = array();
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->periodArray[$row["VALUE"]]["LABEL"] = $row["LABEL"];
            $model->periodArray[$row["VALUE"]]["PERI_YOMIKAE"] = $periCnt;
            $model->maxPeri = $periCnt;
            $model->periYomikae[$periCnt] = $row["VALUE"];
            $periCnt++;
        }
        $result->free();

        if ($model->field['SCH_DIV'] == '1') {
            //曜日数
            $model->dateCntMax = 7;
            for ($dateCnt = 0; $dateCnt < 7; $dateCnt++) {
                //hidden
                knjCreateHidden($objForm, "DATEDISP".$dateCnt, $model->weekPtrn[$dateCnt]['WEEK_JP']);
                knjCreateHidden($objForm, "UPDDATE".$dateCnt, $model->weekPtrn[$dateCnt]['CD']);
            }
            //hidden
            knjCreateHidden($objForm, "DATECNT_MAX", $model->dateCntMax);
        } else {
            $holidayList = array();
            $result = $db->query(knjb3042Query::getHolidayMst($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $holidayList[] = $row['HOLIDAY'];
            }
            $result->free();

            $model->dateArray = array();
            //日付数
            $model->dateCntMax = 0;
            for ($dateCnt = 0; $dateCnt < 14; $dateCnt++) {
                $setDate = date("Y-m-d", strtotime($model->startDate." +{$dateCnt} day"));
                if ($setDate > str_replace("/", "-", $model->endDate)) {
                    break;
                }
                list($year, $month, $day) = preg_split("/-/", $setDate);
                $timestamp = mktime(0, 0, 0, $month, $day, $year);
                $setWeek = $model->week[date('w', $timestamp)];
                $dispDate = str_replace("-", "/", $setDate)."(".$setWeek["WEEK_JP"].")";
                if ($model->Properties["notDispHoliday"] != '1' || (date('w', $timestamp) != 0 && date('w', $timestamp) != 6 && !in_array($setDate, $holidayList))) {
                    $model->dateArray[] = array("RENBAN" => $model->dateCntMax, "JPN" => $dispDate, "DATE_OR_WEEK" => $setDate, "CD" => $setWeek["CD"], 'CSS' => $setWeek["CSS"], 'DATE' => $setDate);

                    //hidden
                    knjCreateHidden($objForm, "DATEDISP".$model->dateCntMax, $dispDate);
                    knjCreateHidden($objForm, "UPDDATE".$model->dateCntMax, $setDate);

                    $model->dateCntMax++;
                }
            }
            //hidden
            knjCreateHidden($objForm, "DATECNT_MAX", $model->dateCntMax);
        }

        if ($model->field["SCH_DIV"] == '1') {
            $AllDate = array();
            foreach ($model->weekPtrn as $key => $val) {
                $setTitle = array();
                $setTitle['COL_SPAN'] = get_count($model->periodArray);
                $setTitle['WEEK_NAME'] = $val['WEEK_JP'];
                $setTitle['CSS'] = $val['CSS'];
                $arg["TITLE"][] = $setTitle;
                $AllDate[] = $val['CD'];

                foreach ($model->periodArray as $periKey => $periVal) {
                    $setTitle2 = array();
                    $setTitle2["PERIOD"] = $periVal["LABEL"];
                    $setTitle2["PERIID"] = $val["RENBAN"]."_".$periKey;
                    $setTitle2["CSS"] = $val['CSS'];
                    $arg["TITLE2"][] = $setTitle2;
                }
            }
        } else {
            $AllDate = array();
            foreach ($model->dateArray as $key => $val) {
                $setTitle = array();
                $setTitle["COL_SPAN"] = get_count($model->periodArray);
                $setTitle["WEEK_NAME"] = $val["JPN"];
                $setTitle['CSS'] = $val['CSS'];
                $arg["TITLE"][] = $setTitle;
                $AllDate[] = $val['DATE'];

                foreach ($model->periodArray as $periKey => $periVal) {
                    $setTitle2 = array();
                    $setTitle2["PERIOD"] = $periVal["LABEL"];
                    $setTitle2["PERIID"] = $val["RENBAN"]."_".$periKey;
                    $setTitle2["CSS"] = $val['CSS'];
                    $arg["TITLE2"][] = $setTitle2;
                }
            }
        }

        //最大校時
        knjCreateHidden($objForm, "MAX_PERIOD", $model->maxPeri);
        knjCreateHidden($objForm, "ALL_DATE", implode(',', $AllDate));

        if ($model->leftMenu == '1') {
            setStaffDispData($objForm, $arg, $db, $model);
        } elseif ($model->leftMenu == '2') {
            setHrClassDispData($objForm, $arg, $db, $model);
        } elseif ($model->leftMenu == '3') {
            setSubclassDispData($objForm, $arg, $db, $model);
        } elseif ($model->leftMenu == '4') {
            setChairDispData($objForm, $arg, $db, $model);
        } elseif ($model->leftMenu == '5') {
            setSchTestPattern($objForm, $arg, $db, $model);
        }

        if ($model->Properties["KNJB3042_SchTestPattern"] == "1" && $model->field['SCH_DIV'] == '3' && ($model->cmd != '' && $model->cmd != 'editSchDiv')) {
            //講座の生徒数合計
            $result = $db->query(knjb3042Query::chairStdSum($model));
            $chairStdSum = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chairStdSum[$row['EXECUTEDATE']][$row['PERIODCD']] = $row['CNT'];
            }
            $result->free();

            $cnt = 0;
            foreach ($model->dateArray as $key => $val) {
                foreach ($model->periodArray as $periKey => $periVal) {
                    $zyuugoukei = isset($model->dipliObjCnt2[$val["DATE"].'_'.$model->periodArray[$periKey]["PERI_YOMIKAE"]]) ? $model->dipliObjCnt2[$val["DATE"].'_'.$model->periodArray[$periKey]["PERI_YOMIKAE"]] : 0;
                    $sougoukei = isset($chairStdSum[$val["DATE"]][$periKey]) ? $chairStdSum[$val["DATE"]][$periKey] : 0;
                    $arg["HEADERRROW"][] = array('ID'=>$cnt.'_'.$model->periodArray[$periKey]["PERI_YOMIKAE"], 'ZYUUGOUKEI' => $zyuugoukei, 'SOUGOUKEI' => $sougoukei, 'CSS' => $val['CSS']);
                }
                $cnt++;
            }
        }

        diffmicrotime(microtime());
        // 1Day  ： 3.44424  秒 25.011545 秒 41.834615 秒 43.157709 秒 SQLの分離で1/10くらい
        // 1Day  ： 3.82898  秒 26.012721 秒 39.438676 秒 40.679743 秒 isset
        // 1Day  ： 3.155157 秒 24.881925 秒 38.312434 秒 39.522275 秒 文字連結を配列化してJOIN
        // 1Day  ： 3.383963 秒 27.555323 秒 29.565569 秒 31.09326  秒 SQLの取得を一回で

        // 1Week ：10.643922 秒 163.62494 秒 237.319444 秒
        $outputChairList = array();
        if (isset($model->chairList2)) {
            foreach ($model->chairList2 as $key => $value) {
                for ($i = 0; $i < get_count($value); $i++) {
                    if (!isset($outputChairList[$value[$i]])) {
                        $outputChairList[$value[$i]] = array();
                    }
                    $outputChairList[$value[$i]][] = $key;
                }
            }
        }
        //講座CD:[1,5,10]この講座の割当られる行番号(職員だったら講座担当の行番号)
        knjCreateHidden($objForm, "chairList", json_encode($outputChairList));

        //年組
        $query = knjb3042Query::getHrName($model);
        $extra = "onChange=\"selectSubclass('getChair')\"";
        makeCmb($objForm, $arg, $db, $query, $model->grandHrClassCd, "GRAND_HR_CLASSCD", $extra, 1, "BLANK");

        //科目
        $query = knjb3042Query::getSubclass($model, false);
        $extra = "onChange=\"selectSubclass('getChair')\"";
        makeCmb($objForm, $arg, $db, $query, $model->subclassCd, "SUBCLASSCD", $extra, 1, "BLANK");

        //群
        $query = knjb3042Query::getGun($model);
        $extra = "onChange=\"selectSubclass('getChair')\"";
        makeCmb($objForm, $arg, $db, $query, $model->gunCd, "GUNCD", $extra, 1, "BLANK");

        //職員
        $query = knjb3042Query::getStaff2($model);
        $extra = "onChange=\"selectSubclass('getChair')\"";
        makeCmb($objForm, $arg, $db, $query, $model->staffCd, "STAFFCD", $extra, 1, "BLANK");

        //コース名
        $query = knjb3042Query::getCouseName($model, (($db->getOne(knjb3042Query::getRegdDatCnt()) > 0) ? '' : 1), SCHOOLKIND);
        $extra = "onChange=\"selectSubclass('getChair')\"";
        makeCmb($objForm, $arg, $db, $query, $model->couseCd, "COUSECD", $extra, 1, "BLANK");

        //通常、テスト区別ラジオ
        $opt = array(1, 2);
        $model->testRadio = ($model->testRadio == "") ? "1" : $model->testRadio;
        $extra = array(" id=\"TEST_RADIO1\" onclick=\"$('#testCdBox').hide()\" "," id=\"TEST_RADIO2\"  onclick=\"$('#testCdBox').show()\"");
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"TEST_RADIO{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "TEST_RADIO", $model->testRadio, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //テストコンボ
        $query = knjb3042Query::getTestCombo($model);
        $extra = " id=\"TESTCD\"";
        makeCmb($objForm, $arg, $db, $query, $model->testCd, "TESTCD", $extra, 1, '');

        //テストダイアログコンボ
        $query = knjb3042Query::getTestCombo($model);
        $extra = " id=\"DLOG_TESTCD\"";
        makeCmb($objForm, $arg, $db, $query, $value, "DLOG_TESTCD", $extra, 1, '');

        //テスト日付校時移動またはコピーコンボ
        $query = knjb3042Query::getTestCombo($model);
        $extra = " id=\"MOVECOPY_TESTCD\"";
        makeCmb($objForm, $arg, $db, $query, $value, "MOVECOPY_TESTCD", $extra, 1, '');

        //操作区分ラジオ
        $opt = array(1, 2, 3, 4, 5);
        if ($model->leftMenu == '1') {
            $arg["showShokuin"] = "1";
        } else {
            if ($model->operationRadio == '3' || $model->operationRadio == '4') {
                $model->operationRadio = '1';
            }
        }

        $setOpeMongon = $model->field['SCH_DIV'] == '1' ? "曜日・校時" : "日付・校時";
        $arg["OPERATION_NAME"] = $setOpeMongon;

        $model->operationRadio = ($model->operationRadio == "") ? "1" : $model->operationRadio;
        $extra = array();
        foreach ($opt as $key => $val) {
            if (($val == 2 || $val == 4) && ($model->auth != DEF_UPDATABLE)) {
                array_push($extra, " id=\"OPERATION_RADIO{$val}\" disabled ");
            } else {
                array_push($extra, " id=\"OPERATION_RADIO{$val}\" ");
            }
        }
        $radioArray = knjCreateRadio($objForm, "OPERATION_RADIO", $model->operationRadio, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //講座リスト
        makeChairList($objForm, $arg, $db, $model);

        //変更通知リスト
        makeOpeLogList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjb3042SchTestPattern.html", $arg);
        /*
        diffmicrotime(microtime());
        */
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //制限付き表示用
    if ($model->auth != DEF_UPDATABLE) {
        $disabled = " disabled ";
    }
    //読込
    $extra = "onclick=\"return yomikomiTimeCheck();\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //確認
//    $extra = "onclick=\"return getContent('kakunin');\"";
//    $arg["button"]["btn_kakunin"] = knjCreateBtn($objForm, "btn_kakunin", "確 認", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    if ($model->sendPrg) {
        $extra = "onclick=\"window.opener.btn_submit('main'); btn_close();\"";
    } else {
        $extra = "onclick=\"return btn_close();\"";
    }
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
    //ヘルプ
    $extra = "onclick=\"showDialog('helpBox','ヘルプ',helpInitFunc);\"";
    $arg["button"]["btn_help"] = knjCreateBtn($objForm, "btn_help", "ヘルプ", $extra);
    //色分け表示設定
    $extra = "onclick=\"showDialog('backColorChangeBox','色分け表示設定',backColorInitFunc);\" {$disabled} ";
    $arg["button"]["btn_backColor"] = knjCreateBtn($objForm, "btn_backColor", "色分け表示設定", $extra);

    //操作履歴表示
    $extra = "onclick=\"showDialog('operationHistoryBox','変更通知履歴',operationHistoryBoxInitFunc);\" {$disabled} ";
    $arg["button"]["btn_opeHistory"] = knjCreateBtn($objForm, "btn_opeHistory", "変更通知履歴", $extra);
    //操作履歴クリア
    $extra = "onclick=\"deleteAllOperationHistory();\" {$disabled} ";
    $arg["button"]["btn_delOpeHistory"] = knjCreateBtn($objForm, "btn_opeHistory", "変更通知クリア", $extra);
    //操作履歴更新
    $extra = "onclick=\"updateOperationHistory();\" ";
    $arg["button"]["btn_updOpeHistory"] = knjCreateBtn($objForm, "btn_opeHistory", "更 新", $extra);

    //反映
    $extra = "onclick=\"setChair();\" {$disabled} ";
    $arg["button"]["btn_set"] = knjCreateBtn($objForm, "btn_set", "反 映", $extra);
    //日付校時で移動/コピー/入替/削除
    $extra = "onclick=\"showDialog('copyMoveBox','日付校時で移動/コピー/入替/削除',copyMoveBoxInitFunc);\" {$disabled} ";
    $arg["button"]["btn_moveCopy"] = knjCreateBtn($objForm, "btn_moveCopy", "日付校時で移動/コピー/入替/削除", $extra);
    //レイアウト編集(縦)
    if ($model->leftMenu == "1" || $model->leftMenu == "4") {
        $callBoxName = "layoutStaffChairBox";
        $callLayoutIni = "layoutStaffChairBoxInitFunc";
    } else {
        $callBoxName = "layoutHrSubclassBox";
        $callLayoutIni = "layoutHrSubclassBoxInitFunc";
    }
    $extra = "onclick=\"showDialog('{$callBoxName}', 'レイアウト編集(縦)', {$callLayoutIni});\" {$disabled} ";
    $arg["button"]["btn_layout"] = knjCreateBtn($objForm, "btn_layout", "レイアウト編集(縦)", $extra);
    //重複講座名簿
    $extra  = " onClick=\" btn_overlapMeibo('".REQUESTROOT."/B/KNJB3042_CHAIR_STD_SELECT/knjb3042_chair_std_selectindex.php'";
    $extra .= ", '?SEND_PRGRID=KNJB3042";
    $extra .= "&YEAR=".$model->year."";
    $extra .= "&SEMESTER=".$model->semester."";
    $extra .= "&START_DATE=".$model->startDate."";
    $extra .= "&SCH_PTRN=".$model->field['SCH_DIV']."";
    $extra .= "&BSCSEQ=".$model->field['BSCSEQ']."";
    $extra .= "&SUBWIN=SUBWIN2";
    $extra .= "');\"";
    $extra .= " {$disabled} ";
    $arg["button"]["btn_chairStd"] = knjCreateBtn($objForm, "btn_chairStd", "重複名簿等の移動", $extra);
    //基本時間割 テンプレート削除
    if ($model->field['SCH_DIV'] == '1' && $model->field['BSCSEQ'] > 0) {
        $extra = "onclick=\"return btn_submit('bscseqDelete');\" {$disabled} ";
        $arg["button"]["btn_bscseq_delete"] = knjCreateBtn($objForm, "btn_bscseq_delete", "削 除", $extra);
    }

    //重複生徒一覧
    $extra = "onclick=\"showDialog('dipliStdViewBox','重複生徒一覧',dipliStdViewFunc);\"";
    $arg["button"]["btn_dipliStdView"] = knjCreateBtn($objForm, "btn_dipliStdView", "重複生徒一覧", $extra);

    //試験配置表出力
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "試験配置表出力", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "updateAddData");
    knjCreateHidden($objForm, "updateDelData");
    knjCreateHidden($objForm, "lineStaffInfo");
    knjCreateHidden($objForm, "startTD");
    knjCreateHidden($objForm, "selectStartTD");
    knjCreateHidden($objForm, "selectEndTD");
    knjCreateHidden($objForm, "visibleLine", $model->visibleLine);
    knjCreateHidden($objForm, "semesterStartDate", $model->semesterInfo["SDATE"]);
    knjCreateHidden($objForm, "semesterEndDate", $model->semesterInfo["EDATE"]);
    knjCreateHidden($objForm, "operationHistory");
    knjCreateHidden($objForm, "AUTHORITY", $model->auth);
    knjCreateHidden($objForm, "KNJB3042_SchTestPattern", $model->Properties["KNJB3042_SchTestPattern"]);
    knjCreateHidden($objForm, "notDispHoliday", $model->Properties["notDispHoliday"]);
}

//講座一覧リストToリスト作成
function makeChairList(&$objForm, &$arg, $db, $model, $div = "")
{
    $selectList = array();
    $query = knjb3042Query::getChair($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $selectList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result->free();

    //出力対象作成
    if ($div == "returnGe") {
        $extra = "multiple style=\"width:300px\" ondblclick=\"setChair();\"";
        return knjCreateCombo($objForm, "CATEGORY_SELECTED", '', $selectList, $extra, 20);
    } else {
        $extra = "multiple style=\"width:300px\" ondblclick=\"setChair();\"";
        $arg["list"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", '', $selectList, $extra, 20);
    }
}

// 変更通知リスト作成
function makeOpeLogList(&$objForm, &$arg, $db, $model, $div = "")
{
    $opeLogList = array();

    $extra = ' onchange="opeLogChange(this);" ';
    $query = knjb3042Query::selectOpelog($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row['CHK'] = knjCreateCheckBox($objForm, "opelogChk", $row['SEQ'], "");
        $row['REGIST_DATE'] = str_replace('-', '/', $row['REGIST_DATE']);
        $checked = $row['CANCEL_FLG'] == '1' ? ' checked ' : '';
        $row['CANCEL_FLG'] = knjCreateCheckBox($objForm, "opelogCancel", $row['CANCEL_FLG'], $extra.$checked);
        $row['STAFFNAME'] = $row['STAFFCD'].'<br>'.$row['STAFFNAME_SHOW'];
        $row['NOTICE_MESSAGE'] = knjCreateTextBox($objForm, $row['NOTICE_MESSAGE'], "opelogMessage", 90, 600, $extra);
        $opeLogList[] = $row;
    }
    $result->free();
    $arg["OpeLog"] = $opeLogList;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $retFlg = "")
{
    $opt = array();
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
    $result->free();

    if ($name == "YEAR_SEME") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR."-".CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    if ($retFlg) {
        return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}
function getMeiboExec(&$db, &$model, $meiboParam)
{
    $response = array();
    for ($i = 0; $i < get_count($meiboParam); $i++) {
        $responseParts = array();
        $targetDayArray = preg_split("/-/", $meiboParam[$i]['targetDay']);
        $targetDay = sprintf('%04d-%02d-%02d', $targetDayArray[0], $targetDayArray[1], $targetDayArray[2]);

        $dupChair = array();
        if (get_count($meiboParam[$i]['list'])>0) {
            $query = knjb3042Query::getMeiboZyuuhukuPHP($model, $targetDay, $meiboParam[$i]['list']);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dupChair[$row['CHAIRCD']] = 1;
            }
            $result->free();
        }
        for ($chairCnt = 0; $chairCnt < get_count($meiboParam[$i]['list']); $chairCnt++) {
            $responseParts[$meiboParam[$i]['list'][$chairCnt]] = 0;
            //重複のある講座のフラグを立てる
            if (isset($dupChair[$meiboParam[$i]['list'][$chairCnt]])) {
                $responseParts[$meiboParam[$i]['list'][$chairCnt]] = 1;
            }
        }
        $response[] = array('targetDay'=>$meiboParam[$i]['targetDay'],'id'=>$meiboParam[$i]['id'],'kouzi'=>$meiboParam[$i]['kouzi'],'list'=>$responseParts);
    }
    return $response;
}
function checkMeiboAndFac(&$db, &$model, $meiboFacParam)
{
    $response = array();
    for ($i = 0; $i < get_count($meiboFacParam); $i++) {
        $responseParts = array();
        $targetDayArray = preg_split("/-/", $meiboFacParam[$i]['targetDay']);
        $targetDay = sprintf('%04d-%02d-%02d', $targetDayArray[0], $targetDayArray[1], $targetDayArray[2]);

        $dupChair = array();
        if (get_count($meiboFacParam[$i]['list'])>0) {
            $query = knjb3042Query::getMeiboZyuuhukuPHP($model, $targetDay, $meiboFacParam[$i]['list']);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dupChair[$row['CHAIRCD']] = $row['CHAIRCNT'];
            }
            $result->free();
        }
        for ($chairCnt = 0; $chairCnt < get_count($meiboFacParam[$i]['list']); $chairCnt++) {
            $responseParts[$meiboFacParam[$i]['list'][$chairCnt]] = 0;
            //重複のある講座のフラグを立てる
            if (isset($dupChair[$meiboFacParam[$i]['list'][$chairCnt]])) {
                $responseParts[$meiboFacParam[$i]['list'][$chairCnt]] = 1;
            }
        }
        $responseParts2 = array();
        $dupChair = array();
        if (get_count($meiboFacParam[$i]['listHeaderNum'])>0) {
            $query = knjb3042Query::getMeiboZyuuhukuPHP($model, $targetDay, $meiboFacParam[$i]['listHeaderNum']);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dupChair[$row['CHAIRCD']] = $row['CHAIRCNT'];
            }
            $result->free();
        }
        for ($chairCnt = 0; $chairCnt < get_count($meiboFacParam[$i]['listHeaderNum']); $chairCnt++) {
            $responseParts2[$meiboFacParam[$i]['listHeaderNum'][$chairCnt]] = 0;
            //重複のある講座のフラグを立てる
            if (isset($dupChair[$meiboFacParam[$i]['listHeaderNum'][$chairCnt]])) {
                $responseParts2[$meiboFacParam[$i]['listHeaderNum'][$chairCnt]] += $dupChair[$meiboFacParam[$i]['listHeaderNum'][$chairCnt]];
            }
        }
        $sougouSum = array();
        if (get_count($meiboFacParam[$i]['listHeaderNum'])>0) {
            $sougouSum = $db->getOne(knjb3042Query::chairStdSumAJAX($model, $targetDay, $meiboFacParam[$i]['listHeaderNum']));
        }

        //施設の収容講座数をチェック
        $idArray = explode("_", $meiboFacParam[$i]["id"]);
        $renban = $idArray[1];
        $kouzi = $meiboFacParam[$i]['kouzi'];

        if (get_count($meiboFacParam[$i]['faccd']) > 0) {
            $query = knjb3042Query::getChrFacility($model, $meiboFacParam[$i]['faccd']);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $usedFacCount = $meiboFacParam[$i]["usedFacility"][$renban. "_" .$kouzi. "_" .$row["FACCD"]];
                if ($row["CHR_CAPACITY"] !== null && $row["CHR_CAPACITY"] < $usedFacCount) {
                    //収容講座数を超過している施設にフラグを立てる
                    $responseParts[$renban . "_" . $kouzi . "_" . $row["FACCD"]] = 1;
                }
            }
        }

        $response[] = array('targetDay'=>$meiboFacParam[$i]['targetDay'],'id'=>$meiboFacParam[$i]['id'],'kouzi'=>$kouzi,'list'=>$responseParts,'list2'=>$responseParts2,'sougou'=>$sougouSum);
    }
    return $response;
}

function diffmicrotime($a, $msg = "")
{
    $b = $GLOBALS['global_time_start'];
    list($am, $at) = explode(' ', $a);
    list($bm, $bt) = explode(' ', $b);
//    echo $msg. (((float)$am-(float)$bm) + ((float)$at-(float)$bt)).' 秒';
}

//左メニュー職員
function setStaffDispData(&$objForm, &$arg, $db, &$model)
{
    $arg['isSection'] = "1";

    $visibleList = array();
    //職員
    $query = knjb3042Query::getStaff($model);
    $result = $db->query($query);
    $leftMenuArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row['LoopKey'] = $row['STAFFCD'];
        $row['TitleName'] = $row['STAFFNAME'];
        $leftMenuArray[] = $row;
        $visibleList[$row['LoopKey']] = false;
        if ($row['CHARGECLASSCD'] == '1') {
            $visibleList[$row['LoopKey']] = true;
        }
    }
    $result->free();

    $model->staffArray = $leftMenuArray;

    //制限付き権限ユーザーの左メニュー 職員
    if ($model->auth != DEF_UPDATABLE) {
        $visibleList = array();

        $query = knjb3042Query::getStaffLimitUser($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['LoopKey'] = $row['STAFFCD'];
            $visibleList[$row['LoopKey']] = true;
        }
        $result->free();
    }

    //職員講座
    $query = knjb3042Query::getStaffChair($model);
    $result = $db->query($query);
    $toCellActiveList = array();
    $toChairList2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //正担任、副担任のフラグ。1は正担任
        if ($row['CHARGEDIV'] == 1) {
            $setKey = $row["STAFFCD"];
            $toCellActiveList[$row["CHAIRCD"]][$setKey] = "1";
            $toChairList2[$setKey][] = $row["CHAIRCD"];
        }
    }
    $result->free();

    //講座変更(SCH_STF_DAT)データ
    if ($model->field['SCH_DIV'] == '1') {
        $query = knjb3042Query::getPtrnStaff($model);
    } else {
        $query = knjb3042Query::getSchStaff($model);
    }
    $result = $db->query($query);
    $schStaffArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setPeriCd = $model->periodArray[$row["PERIODCD"]]["PERI_YOMIKAE"];
        $schStaffArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]][$row["STAFFCD"]] = "1";
    }
    $result->free();

    cellLoopMain($objForm, $arg, $db, $model, $leftMenuArray, $toCellActiveList, $toChairList2, $schStaffArray, $visibleList);
}

//左メニュー年組
function setHrClassDispData(&$objForm, &$arg, $db, &$model)
{
    $visibleList = array();
    //年組
    $query = knjb3042Query::getHrClassSql($model);
    $result = $db->query($query);
    $leftMenuArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row['LoopKey'] = $row['GRADE'].$row['HR_CLASS'];
        $row['TitleName'] = $row['HR_NAME'];
        $leftMenuArray[] = $row;
        $visibleList[$row['LoopKey']] = true;
    }
    $result->free();

    //制限付き権限ユーザーの左メニュー 年組
    if ($model->auth != DEF_UPDATABLE) {
        $query = knjb3042Query::getHrClassSqlLimitUser($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['LoopKey'] = $row['GRADE'].$row['HR_CLASS'];
            $visibleList[$row['LoopKey']] = true;
        }
        $result->free();
    }

    //年組講座
    $query = knjb3042Query::getChairClassSql($model);
    $result = $db->query($query);
    $toCellActiveList = array();
    $toChairList2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setKey = $row["TRGTGRADE"].$row["TRGTCLASS"];
        $toCellActiveList[$row["CHAIRCD"]][$setKey] = "1";
        $toChairList2[$setKey][] = $row["CHAIRCD"];
    }
    $result->free();

    cellLoopMain($objForm, $arg, $db, $model, $leftMenuArray, $toCellActiveList, $toChairList2, null, $visibleList);
}
//左メニュー科目
function setSubclassDispData(&$objForm, &$arg, $db, &$model)
{
    $visibleList = array();
    //科目
    $query = knjb3042Query::getSubclass($model, true);
    $result = $db->query($query);
    $leftMenuArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row['LoopKey'] = $row['VALUE'];
        $row['TitleName'] = $row['SUBCLASSNAME'];
        $leftMenuArray[] = $row;
        $visibleList[$row['LoopKey']] = true;
    }
    $result->free();

    //制限付き権限ユーザーの左メニュー 科目
    if ($model->auth != DEF_UPDATABLE) {
        $query = knjb3042Query::getSubclassLimitUser($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['LoopKey'] = $row['VALUE'];
            $visibleList[$row['LoopKey']] = $row;
        }
        $result->free();
    }

    //科目-講座
    $query = knjb3042Query::getChairSubclass($model);
    $result = $db->query($query);
    $toCellActiveList = array();
    $toChairList2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setKey = $row["SUBCLASSCD"];
        $toCellActiveList[$row["CHAIRCD"]][$setKey] = "1";
        $toChairList2[$setKey][] = $row["CHAIRCD"];
    }
    $result->free();

    cellLoopMain($objForm, $arg, $db, $model, $leftMenuArray, $toCellActiveList, $toChairList2, null, $visibleList);
}

//左メニュー講座
function setChairDispData(&$objForm, &$arg, $db, &$model)
{
    $visibleList = array();
    //講座
    $query = knjb3042Query::getChair2($model, true);
    $result = $db->query($query);
    $leftMenuArray = array();
    $toCellActiveList = array();
    $toChairList2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row['LoopKey'] = $row['CHAIRCD'];
        $row['TitleName'] = $row['CHAIRNAME'];
        $leftMenuArray[] = $row;

        $setKey = $row["CHAIRCD"];
        $toCellActiveList[$row["CHAIRCD"]][$setKey] = "1";
        $toChairList2[$setKey][] = $row["CHAIRCD"];
        $visibleList[$row['LoopKey']] = true;
    }
    $result->free();

    //制限付き権限ユーザーの左メニュー 講座
    if ($model->auth != DEF_UPDATABLE) {
        $query = knjb3042Query::getChairLimitUser($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['LoopKey'] = $row['CHAIRCD'];
            $visibleList[$row['LoopKey']] = true;
        }
        $result->free();
    }

    cellLoopMain($objForm, $arg, $db, $model, $leftMenuArray, $toCellActiveList, $toChairList2, null, $visibleList);
}
//テスト考査
function setSchTestPattern(&$objForm, &$arg, $db, &$model)
{
    $arg['isSection'] = "1";

    $visibleList = array();

    for ($i = 0; $i < 31; $i++) {
        $row['LoopKey'] = $i;
        $row['TitleName'] = $i;
        $leftMenuArray[] = $row;
        $visibleList[$row['LoopKey']] = true;
    }

    $toCellActiveList = array();
    $toChairList2 = array();
    $schStaffArray = array();

    cellLoopMain($objForm, $arg, $db, $model, $leftMenuArray, $toCellActiveList, $toChairList2, $schStaffArray, $visibleList);
}
function cellLoopMain(&$objForm, &$arg, $db, &$model, $leftMenuArray, $toCellActiveList, $toChairList2, $schStaffArray = null, $visibleList = null)
{
    if ($model->cmd == '' || $model->cmd == 'editSchDiv') {
        $leftMenuLoopCnt = get_count($leftMenuArray);
        if ($model->auth == DEF_UPDATABLE) {
            $model->visibleLine = '0';
            $setSep = ",";
            for ($lineCnt = 1; $lineCnt <= $leftMenuLoopCnt; $lineCnt++) {
                $LoopKey = $leftMenuArray[$lineCnt - 1]['LoopKey'];
                if ($visibleList[$LoopKey]) {
                    $model->visibleLine .= $setSep.($lineCnt - 1);
                }
            }
        } else {
            for ($lineCnt = 1; $lineCnt <= $leftMenuLoopCnt; $lineCnt++) {
                $LoopKey = $leftMenuArray[$lineCnt - 1]['LoopKey'];
                if ($visibleList[$LoopKey]) {
                    $model->visibleLine .= $setSep.($lineCnt - 1);
                    $setSep = ",";
                }
            }
        }
        return true;
    }

    //hidden
    knjCreateHidden($objForm, "MAX_LINE", get_count($leftMenuArray));

    //施設情報
    if ($model->field['SCH_DIV'] == '1') {
        $query = knjb3042Query::getPtrnFacDat($model);
    } else {
        $query = knjb3042Query::getSchFacDat($model);
    }
    $result = $db->query($query);
    $schFacArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setPeriCd = $model->periodArray[$row["PERIODCD"]]["PERI_YOMIKAE"];
        $sep = "";
        if ($schFacArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]]) {
            $sep = ":";
        }
        $schFacArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]] .= $sep.$row["FACCD"];
    }
    $result->free();

    //試験会場
    $schTestFacArray = array();
    if ($model->Properties["useTestFacility"] == "1") {
        if ($model->field['SCH_DIV'] == '1') {
            $query = knjb3042Query::getPtrnTestFacDat($model);
        } else {
            $query = knjb3042Query::getSchTestFacDat($model);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setPeriCd = $model->periodArray[$row["PERIODCD"]]["PERI_YOMIKAE"];
            $sep = "";
            if ($schTestFacArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]]) {
                $sep = ":";
            }
            $schTestFacArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]] .= $sep.$row["FACCD"];
        }
        $result->free();
    }

    //集計フラグ/授業形態情報
    if ($model->field['SCH_DIV'] == '1') {
        $query = knjb3042Query::getPtrnCountFlgDat($model);
    } else {
        $query = knjb3042Query::getSchCountFlgDat($model);
    }
    $result = $db->query($query);
    $schCountFlgArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setPeriCd = $model->periodArray[$row["PERIODCD"]]["PERI_YOMIKAE"];
        $sep = "";
        if ($schCountFlgArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]]) {
            $sep = ":";
        }
        $schCountFlgArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]] .= $sep.$row["GRADEHR"].'/'.$row["COUNTFLG"].'-'.$row["LESSON_MODE"];
    }
    $result->free();

    //登録データ
    if ($model->field['SCH_DIV'] == '1') {
        $query = knjb3042Query::getSchPtrn($model);
    } else {
        $query = knjb3042Query::getSchChair($model);
    }

    $result = $db->query($query);
    $schChairArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setPeriCd = $model->periodArray[$row["PERIODCD"]]["PERI_YOMIKAE"];
        $schChairArray[$row["DATE_OR_WEEK"]][$setPeriCd][$row["CHAIRCD"]] = $row;
    }
    $result->free();

    //連続授業
    $query = knjb3042Query::getChair2($model);
    $result = $db->query($query);
    $chairDataList = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $chairDataList[$row["CHAIRCD"]]=array('FRAMECNT'=> $row['FRAMECNT'],'CHAIRNAME' =>$row['CHAIRNAME']);
    }
    $result->free();

    $weekTitleArray = array();
    if ($model->field['SCH_DIV'] == '1') {
        $weekTitleArray = $model->weekPtrn;
    } else {
        $weekTitleArray = $model->dateArray;
    }

    $schChairArrayHumei = $schChairArray;
    cellLoopMainParts(
        $objForm,
        $arg,
        $db,
        $model,
        $leftMenuArray,
        $toCellActiveList,
        $toChairList2,
        $schStaffArray,
        $visibleList,
        $schFacArray,
        $schTestFacArray,
        $schCountFlgArray,
        $schChairArray,
        $chairDataList,
        $weekTitleArray,
        $schChairArrayHumei
    );

    $arg["setWidth"] = get_count($weekTitleArray) * get_count($model->periodArray) * 50;
}


function cellLoopMainParts(
    &$objForm,
    &$arg,
    $db,
    &$model,
    $leftMenuArray,
    $toCellActiveList,
    $toChairList2,
    $schStaffArray,
    $visibleList,
    $schFacArray,
    $schTestFacArray,
    $schCountFlgArray,
    $schChairArray,
    $chairDataList,
    $weekTitleArray,
    &$schChairArrayHumei
) {

    //画面用
    $schChairDisp = array();
    //受講者重複チェック用
    $dipliObj = array();
    $dipliObjTest = array();

    // 通常講座
    $TuujoCd = "";
    // 不明行
    $fumeiCd = $leftMenuArray[0]['LoopKey'];
    if ($model->field['SCH_DIV'] == '3') {
        $TuujoCd = $leftMenuArray[0]['LoopKey'];
        $fumeiCd = $leftMenuArray[1]['LoopKey'];
    }


    $leftStaff = array();
    foreach ($leftMenuArray as $value) {
        $leftStaff[$value["LoopKey"]] = $value["LoopKey"];
    }

    //時間割講座を画面表示の対象位置に振分け
    //登録されている講座分ループ
    foreach ($schChairArray as $schDateArray) {
        foreach ($schDateArray as $schPeriArray) {
            foreach ($schPeriArray as $schChairVal) {
                $syoriBi = str_replace("/", "-", $schChairVal["DATE_OR_WEEK"]);
                $syoriPeri = $model->periodArray[$schChairVal["PERIODCD"]]["PERI_YOMIKAE"];
                $chairCd = $schChairVal["CHAIRCD"];

                $isDisp = false;

                // 考査の場合、通常の講座は通常分へまとめる
                $isTuujo = ($model->field['SCH_DIV'] == '3' && $schChairVal["DATADIV"] != '2');
                if ($isTuujo) {
                    $isDisp = true;
                    $schChairDisp[$syoriBi][$syoriPeri][$TuujoCd][] = $schChairVal;
                } elseif ($model->Properties["KNJB3042_SchTestPattern"] == "1" && $model->field['SCH_DIV'] == '3') {
                    $isDisp = true;
                    if (get_count($schChairDisp[$syoriBi][$syoriPeri]) == 0) {
                        $schChairDisp[$syoriBi][$syoriPeri][] =array();
                    }
                    $schChairDisp[$syoriBi][$syoriPeri][] =array();
                    $schChairDisp[$syoriBi][$syoriPeri][get_count($schChairDisp[$syoriBi][$syoriPeri]) - 1][] = $schChairVal;
                    if ($syoriPeri != '') {
                        $dipliObjTest[$syoriBi][$syoriPeri][] = $chairCd;
                    }
                } else {
                    //講座が処理対象か判定
                    if (isset($schStaffArray)) {
                        if (isset($schStaffArray[$syoriBi][$syoriPeri][$chairCd])) {
                            foreach ($schStaffArray[$syoriBi][$syoriPeri][$chairCd] as $LoopKey => $dateVal) {
                                if (isset($leftStaff[$LoopKey])) {
                                    $isDisp = true;
                                    $schChairDisp[$syoriBi][$syoriPeri][$LoopKey][] = $schChairVal;
                                }
                            }
                        } else {
                            foreach ((array)$toCellActiveList[$chairCd] as $LoopKey => $dateVal) {
                                if (isset($leftStaff[$LoopKey])) {
                                    $isDisp = true;
                                    $schChairDisp[$syoriBi][$syoriPeri][$LoopKey][] = $schChairVal;
                                }
                            }
                        }
                    } else {
                        foreach ((array)$toCellActiveList[$chairCd] as $LoopKey => $dateVal) {
                            if (isset($leftStaff[$LoopKey])) {
                                $isDisp = true;
                                $schChairDisp[$syoriBi][$syoriPeri][$LoopKey][] = $schChairVal;
                            }
                        }
                    }
                    //対象行があった場合は、不明行用から抜く
                    if ($isDisp) {
                        unset($schChairArrayHumei[$syoriBi][$syoriPeri][$chairCd]);
                    } else {
                        if (isset($chairDataList[$chairCd])) {
                            //対象行がない場合は、不明行へ追加
                            $schChairDisp[$syoriBi][$syoriPeri][$fumeiCd][] = $schChairVal;
                        }
                    }
                }
                //受講者重複チェック用
                $dipliObj[$syoriBi][$syoriPeri][] = $chairCd;
            }
        }
    }

    //受講生重複チェック
    $dipliObj2 = getDipliObj2($db, $model, $dipliObj);
    $model->dipliObjCnt2 = getDipliObj2($db, $model, $dipliObjTest);

    //日付ループ
    $numArray = array();
    if ($model->field['SCH_DIV'] == '2' || $model->field['SCH_DIV'] == '3') {
        //講座毎の受講人数チェック
        foreach ($weekTitleArray as $dateKey => $dateVal) {
            $syoriBi = $dateVal["DATE_OR_WEEK"];
            $query = knjb3042Query::getZyukouNumAll($model, $syoriBi);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $numArray[$syoriBi][$row["CHAIRCD"]] = true;
            }
            $result->free();
        }
    }

    $isMainLoop = isset($schChairArrayHumei);
    $linkingObj = array();

    if ($isMainLoop) {
        $model->chairList2 = array();
    }
    if ($isMainLoop && (VARS::request("PROGRAMID") != '' || $model->cmd == '' || $model->cmd == 'editCmb' || $model->cmd == 'editSchDiv')) {
        $model->visibleLine = "";
        $setSep = "";
        if ($model->leftMenu != '4') {
            $model->visibleLine = '0';
            $setSep = ",";
        }
    }

    //縦タイトルループ
    $leftMenuLoopCnt = get_count($leftMenuArray);
    //縦タイトルループ
    for ($lineCnt = 1; $lineCnt <= $leftMenuLoopCnt; $lineCnt++) {
        $LoopKey = $leftMenuArray[$lineCnt - 1]['LoopKey'];
        if ($model->Properties["KNJB3042_SchTestPattern"] == "1" && $model->field['SCH_DIV'] == '3') {
            $arg["data"][] = array('LINE_CNT'=>$lineCnt,'KEY_NAME'=>$LoopKey,'TITLE_NAME'=>$leftMenuArray[$lineCnt - 1]['TitleName']);
        } elseif ($model->leftMenu == '1') {
            $arg["data"][] = array('LINE_CNT'=>$lineCnt,'KEY_NAME'=>$LoopKey
                    , 'TITLE_NAME' => $model->fusemozi($LoopKey)."<BR>".$leftMenuArray[$lineCnt - 1]['TitleName']
                    , 'SECTION' => $leftMenuArray[$lineCnt - 1]['SECTIONNAME']);
        } else {
            $arg["data"][] = array('LINE_CNT'=>$lineCnt,'KEY_NAME'=>$LoopKey,'TITLE_NAME'=>$LoopKey."<BR>".$leftMenuArray[$lineCnt - 1]['TitleName']);
        }
        $setData2 = array();
        if ($isMainLoop) {
            if (isset($toChairList2[$LoopKey])) {
                $model->chairList2[$lineCnt] = $toChairList2[$LoopKey];
            }
        }

        $isVisible = true;
        // 制限付き権限ユーザーの表示・非表示
        if ($model->auth != DEF_UPDATABLE) {
            $isVisible = false;
            if ($visibleList[$LoopKey]) {
                $isVisible = true;
            }
        } else {
            if (!$visibleList[$LoopKey]) {
                $isVisible = false;
            }
        }

        if (
            $isVisible && $isMainLoop
            && (VARS::request("PROGRAMID") != '' || $model->cmd == '' || $model->cmd == 'editCmb' || $model->cmd == 'editSchDiv')
        ) {
            $model->visibleLine .= $setSep.($lineCnt - 1);
            $setSep = ",";
        }
        //日付ループ
        foreach ($weekTitleArray as $dateKey => $dateVal) {
            $syoriBi = $dateVal["DATE_OR_WEEK"];
            $setSyoriBi = $model->field['SCH_DIV'] == '1' ? $dateVal["RENBAN"] : $dateVal["DATE_OR_WEEK"];
            //校時ループ
            foreach ($model->periodArray as $periKey => $periVal) {
                $setChairVal = "";
                $chairValSep = "";
                $syoriPeri = $periVal["PERI_YOMIKAE"];
                $setIdName = $dateVal["RENBAN"]."_".$syoriPeri."_".$lineCnt;
                $lineDisp = "";
                $dataTestSep = '';
                //時間割に現在の日付校時があるか
                if (isset($schChairDisp[$syoriBi][$syoriPeri][$LoopKey])) {
                    $dispCnt = 0;
                    $dataDef= '';
                    $dispChair = '';
                    $dataText = '';
                    $dataTest = '';
                    $dataExec = '';
                    $dataZyukou = '';
                    $dataDepli = '';
                    $dataChrFaclityOver = '';
                    $testBoxClass = '';
                    $dataFac = '';
                    $dataTestFac = '';
                    $dataCountLesson = '';
                    //現在の日付校時に登録されている講座ループ
                    foreach ($schChairDisp[$syoriBi][$syoriPeri][$LoopKey] as $schChairVal) {
                        $chairCd = $schChairVal["CHAIRCD"];
                        if ($dipliObj2[$dateVal["DATE_OR_WEEK"].'_'.$syoriPeri.'_'.$chairCd] == 1) {
                            $dataDepli='重';
                        }
                        if ($schFacArray[$dateVal["DATE_OR_WEEK"]][$syoriPeri][$chairCd]) {
                            $faccd = $schFacArray[$dateVal["DATE_OR_WEEK"]][$syoriPeri][$chairCd];
                            if (checkChrFacilityOver($db, $model, $dateVal["DATE_OR_WEEK"], $syoriPeri, $faccd)) {
                                $dataChrFaclityOver='施';
                            }
                        }
                        $dispCnt++;
                        if ($dispCnt > 1) {
                            $dispChair = $dispCnt."件のデータ".'<br>'.$dataDepli.$dataChrFaclityOver;
                        } else {
                            $dispChair = $chairCd."<BR>".$schChairVal["CHAIRABBV"].'<br>'.$dataDepli.$dataChrFaclityOver;
                        }
                        $dataText .= $chairCd."<BR>".$schChairVal["CHAIRABBV"].',';
                        $setChairVal .= $chairValSep.$chairCd;
                        $chairValSep = ":";
                        $dataTest .= $dataTestSep.(($schChairVal["DATADIV"] == '2') ? $schChairVal["TESTKINDCD"].$schChairVal["TESTITEMCD"] : '0');
                        $dataExec .= $dataTestSep.$schChairVal['EXECUTED'];
                        $dataZyukou .= $dataTestSep.(isset($numArray[$syoriBi][$chairCd]) ? 'zyukou_box' : '0');
                        $setFac = isset($schFacArray[$syoriBi][$syoriPeri][$chairCd]) ? $schFacArray[$syoriBi][$syoriPeri][$chairCd] : "0";
                        $setTestFac = isset($schTestFacArray[$syoriBi][$syoriPeri][$chairCd]) ? $schTestFacArray[$syoriBi][$syoriPeri][$chairCd] : "0";
                        $setCountLesson = isset($schCountFlgArray[$syoriBi][$syoriPeri][$chairCd]) ? $schCountFlgArray[$syoriBi][$syoriPeri][$chairCd] : "00-000/1-00";
                        $dataFac .= $dataTestSep.$setFac;
                        $dataTestFac .= $dataTestSep.$setTestFac;
                        $dataDef .= $dataTestSep.$setSyoriBi.'_'.$syoriPeri.'_'.$chairCd.'_'.(($schChairVal["DATADIV"]=='2')?$schChairVal["TESTKINDCD"].$schChairVal["TESTITEMCD"]:'0').'_'.$lineCnt.'_'.$setFac.'_'.$setTestFac.'_'.$setCountLesson;
                        $dataCountLesson .= $dataTestSep.$setCountLesson;

                        $dataTestSep = ',';
                        if ($schChairVal["DATADIV"] == '2') {
                            $testBoxClass='test_box';
                        }
                    }

                    $linkingText = '';
                    if (isset($linkingObj['Linking_'.$setIdName])) {
                        foreach ($linkingObj['Linking_'.$setIdName] as $chairCd => $value2) {
                            $linkingText .= $value2.'/';
                        }
                        $linkingText = trim($linkingText, '/');
                    }
                    $addClass = makeAddClass($dataExec, $dataZyukou, $dispChair, $testBoxClass);
                    $dataText = rtrim($dataText, ',');
                    //$dispChair = $dataCountLesson;
                    $lineDisp = "<td id=\"KOMA_{$setIdName}\" style=\"cursor:move;\" draggable=\"true\" class=\"targetbox {$addClass}\" data-zyukou=\"{$dataZyukou}\" data-text=\"{$dataText}\" data-val=\"{$setChairVal}\" data-def=\"{$dataDef}\" data-test=\"{$dataTest}\" data-exec=\"{$dataExec}\" data-linking=\"{$linkingText}\" data-selectfacility=\"{$dataFac}\" data-selecttestfacility=\"{$dataTestFac}\" data-count-lesson=\"{$dataCountLesson}\" data-dirty=\"0\" data-syoribi=\"{$syoriBi}\" data-period=\"{$syoriPeri}\">{$dispChair}</td>\n";
                } else {
                    $dispChair = "";
                    $lineDisp = "<td id=\"KOMA_{$setIdName}\" style=\"cursor:move;\" draggable=\"true\" class=\"targetbox\" data-zyukou=\"\" data-text=\"\" data-val=\"\" data-def=\"\" data-test=\"\" data-exec=\"\" data-linking=\"\" data-selectfacility=\"\" data-selecttestfacility=\"\" data-count-lesson=\"\" data-dirty=\"0\" data-syoribi=\"\" data-period=\"\">{$dispChair}</td>\n";
                }
                $setData2[] = $lineDisp;
            }
        }
        $arg["data2"][]["LISTDATA"] = join('', $setData2);
    }
}

//色変えのCSSクラス生成
function makeAddClass($dataExec, $dataZyukou, $dispChair, $testBoxClass)
{
    $dataExecList = explode(',', $dataExec);
    $execClass = '';
    if ($dataExec != '' && get_count($dataExecList) == 1) {
        if ($dataExecList[0] == "SYUKKETSU") {
            $execClass = 'syukketu';
        } elseif ($dataExecList[0] == "MI_SYUKKETSU") {
            $execClass = 'no_syukketu';
        } elseif ($dataExecList[0] == "ITIBU_SYUKKETSU") {
            $execClass = 'itibu_syukketu';
        }
    }

    if (strpos($dispChair, '件のデータ') !== false) {
        $addClass = 'hukusuu_box';
    } else {
        if (strpos($dataZyukou, 'zyukou_box') !== false) {
            $addClass = 'zyukou_box ' . $execClass;
        } else {
            $addClass = $testBoxClass. ' ' . $execClass;
        }
    }
    return $addClass;
}

function getDipliObj2(&$db, &$model, $dipliObj)
{

//3秒
    diffmicrotime(microtime(), ' ');

    // 日付 → 校時
    foreach ($dipliObj as $targetDay => $value) {
        foreach ($value as $kouzi => $chairCdList) {
            $kouziId = $model->periYomikae[$kouzi];
            //受講講座が2件以上ある生徒の講座番号を取得
            $query = knjb3042Query::getKouzaZyuuhukuPHP($model, $targetDay, $kouziId);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dipliObj2[$targetDay.'_'.$kouzi.'_'.$row['CHAIRCD']] = 1;
            }
            $result->free();
        }
    }
    diffmicrotime(microtime(), ' ');
    return $dipliObj2;
}

function getDipliObj2Test(&$db, &$model, $dipliObj)
{
    diffmicrotime(microtime(), ' ');

    // 日付 → 校時
    foreach ($dipliObj as $targetDay => $value) {
        foreach ($value as $kouzi => $chairCdList) {
            $kouziId = $model->periYomikae[$kouzi];
            //受講講座が2件以上ある生徒の講座番号を取得
            $query = knjb3042Query::getKouzaZyuuhukuPHPTest($model, $targetDay, $kouziId, $chairCdList);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dipliObjCnt2[$targetDay.'_'.$kouzi] += $row['CHAIRCNT'];
            }
            $result->free();
        }
    }
    diffmicrotime(microtime(), ' ');
    return $dipliObjCnt2;
}


function checkChrFacilityOver(&$db, &$model, $dateOrWeek, $kouzi, $faccd)
{
    // 日付 → 校時
    $kouziId = $model->periYomikae[$kouzi];
    //施設の収容講座数が超えている講座を取得
    if ($model->field["SCH_DIV"] == "1") {
        $query = knjb3042Query::getChrFacilityOverPtrn($model, $dateOrWeek, $kouziId, $faccd);
    } else {
        $query = knjb3042Query::getChrFacilityOverFac($model, $dateOrWeek, $kouziId, $faccd);
    }
    $result = $db->query($query);
    $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
    if ($row) {
        return true;
    }
    $result->free();

    return false;
}
