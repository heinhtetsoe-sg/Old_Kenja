<?php

require_once('for_php7.php');

class knjb3043Form1
{
    function main(&$model)
    {
        set_time_limit(600);

        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb3043index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
 
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

$GLOBALS['global_time_start'] = microtime();

        // [共通] 教科選択時処理（科目取得）
        if($model->cmd == "getSubclass"){
            $response = array();
            // 送信パラメタ
            //   $model->year;
            //   $model->semester;
            //   $model->ajaxParam['COURSECD'];
            $model->courseCd = $model->ajaxParam['COURSECD'];
            // 科目リスト(コースIDが設定されている場合のみ処理)
            if ($model->courseCd) {
                $query = knjb3043Query::getSubclassCmb($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $response[] = $row;
                }
                $result->free();
            }
            echo json_encode($response);
            die();
        }

        // [科目展開表] コース名取得（レイアウト編集(縦)）
        if($model->cmd == "getLayoutCourse"){
            $response = array();
            // 送信パラメタ
            //   $model->year;
            $query = knjb3043Query::getCourseName($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }

        // [講座展開表] 講座情報取得（設定講座一覧のコンボボックス変更時）
        if($model->cmd == "getChair"){
            // 送信パラメタ
            //   $model->year;
            //   $model->semester;
            //   $model->ajaxParam['HRCLASSCD'];
            //   $model->ajaxParam['SUBCLASSCD'];
            //   $model->ajaxParam['GUNCD'];
            //   $model->ajaxParam['STAFFCD'];
            //   $model->ajaxParam['COURSECD'];
            $response = array();
            $query = knjb3043Query::getChairCmb($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }

        // [講座展開表] コース情報取得（レイアウト編集(縦)のコンボボックス変更時）
        if($model->cmd == "getLayoutCourseChair"){
            // 送信パラメタ
            //   $model->year;
            //   $model->semester;
            //   ajaxParam['LEFT_MENU']
            $response = array();
            if ($model->ajaxParam['LEFT_MENU'] == '1') {
                $query = knjb3043Query::getPreChairTitle1($model);
            } else if ($model->ajaxParam['LEFT_MENU'] == '2') {
                $query = knjb3043Query::getPreChairTitle2($model);
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }

        // [講座展開表] 背景色変更(講座人数オーバー)
        if ($model->cmd == 'getBackColorChairCapaOver') {
            $response = array();
            $query = knjb3043Query::getChairStdCapaOver($model, $dayKey);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // [講座展開表] 背景色変更(指定科目・講座 科目変更時)
        if ($model->cmd == 'getBackColorChair') {
            $response = array();
            $query = knjb3043Query::getChairCmb($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // [講座展開表] 背景色変更(同一名簿取得)
        if ($model->cmd == 'getBackColorChairSameMeibo') {
            $response = array();
            $query = knjb3043Query::getChairStdSameMeibo($model, $dayKey);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // [講座展開表] 背景色変更(指定生徒の受講講座 年組変更時(HRクラス名簿取得))
        if ($model->cmd == 'getBackColorHrClassStdMeibo') {
            $response = array();
            $query = knjb3043Query::getHrClassStdMeibo($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // [講座展開表] 背景色変更(指定生徒の受講講座)
        if ($model->cmd == 'getBackColorStdChair') {
            $response = array();
            $query = knjb3043Query::getStdChair($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }

        // [講座展開表] 重複人数チェック
        if ($model->cmd == "getStdDupCnt") {
            $response = array();

            //学期情報
            $query = knjb3043Query::getSemesterInfo($model);
            $model->semesterInfo = array();
            $model->semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $chairList = $model->ajaxParam['CHAIRCD'];
            $query = knjb3043Query::getStdDupliCheck($model, $chairList, 'STD_CNT');
            $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
            $response['STDCNT'] = $row['STDCNT'];


            $chairList = $model->ajaxParam['CHAIRCD'];
            $query = knjb3043Query::getStdDupliCheck($model, $chairList, 'CHAIR_LIST');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response['DUPCHAIR'][$row['CHAIRCD']] = $row['STDCNT'];
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // [講座展開表] 未配置人数チェック
        if ($model->cmd == "getStdUnPlacedCnt") {
            $response = array();

            $query = knjb3043Query::getStdUnPlacedCheck($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // コースID
                $courseId = $row['COURSECD'].'-'.$row['MAJORCD'].'-'.$row['GRADE'].'-'.$row['COURSECODE'];
                // 科目コード
                $subclassCd = $row['CLASSCD'].'-'.$row['SCHOOL_KIND'].'-'.$row['CURRICULUM_CD'].'-'.$row['SUBCLASSCD'];

                $response[$courseId][$subclassCd] = $row["STDCNT"];
            }
            $result->free();
            echo json_encode($response);
            die();
        }
        // [講座展開表] 施設講座キャパ超チェック
        if ($model->cmd == "getFacCapOverCnt") {
            $response = array();

            $chairList = $model->ajaxParam['CHAIRCD'];
            $query = knjb3043Query::getFacCapOverCheck($model, $chairList);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response['FACCAPOVER'][$row["CHAIRCD"]] = $row;
            }
            $result->free();
            echo json_encode($response);
            die();
        }

        // [講座展開表] 「反映」押下時のコース取得
        if ($model->cmd == 'getCourseChair') {

            $response = array();

            //学期情報
            $query = knjb3043Query::getSemesterInfo($model);
            $model->semesterInfo = array();
            $model->semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $chairList = $model->ajaxParam['CHAIRCD'];
            $model->leftMenu = $model->ajaxParam['MENUDIV'];
            $model->courseCd = $model->ajaxParam['COURSECD'];

            $query = knjb3043Query::getCourseChair($model, $chairList);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // 講座毎にコースを設定
                $response[$row['CHAIRCD']][] = $row;
            }

            echo json_encode($response);
            die();
        }


        //年度
        $arg["YEAR"] = CTRL_YEAR;

        if ($model->cmd == '' || $model->cmd == 'editSchDiv') {
            $arg['showMain'] = '';
            $arg['showDummy'] = '1';
        } else {
            $arg['showMain'] = '1';
            $arg['showDummy'] = '';
        }

        if ($model->Properties["useSchool_KindField"] == "1") {
            $arg['useSchool_KindField'] = '1';
            $query = knjb3043Query::getSchoolKind($model);
            $extra = "onchange=\"staffCmbChage()\"";
            makeCmb($objForm, $arg, $db, $query, $val, "schoolKind_STAFF", $extra, 1, "BLANK");
            $extra = "onchange=\"hrSubclassCmbChage()\"";
            makeCmb($objForm, $arg, $db, $query, $val, "schoolKind_HrSubClass", $extra, 1, "BLANK");
        }

        // 年学期
        $query = knjb3043Query::getYearSemester();
        $extra = "onChange=\"btn_submit('editSchDiv')\"";
        makeCmb($objForm, $arg, $db, $query, $model->yearSeme, "YEAR_SEME", $extra, 1, "");

        //学期情報
        $query = knjb3043Query::getSemesterInfo($model);
        $model->semesterInfo = array();
        $model->semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);


        // 展開表種別(科目・講座)
        $opt = array(1, 2);
        $model->field['SCH_DIV'] = ($model->field['SCH_DIV'] == "") ? '2' : $model->field['SCH_DIV'];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SCH_DIV{$val}\" onClick=\"btn_submit('editSchDiv')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SCH_DIV", $model->field['SCH_DIV'], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        // 展開表の処理振分
        if ($model->field["SCH_DIV"] == '1') {
            // 科目展開表
            createSubclassView($objForm, $arg, $db, $model);
        } else {
            // 講座展開表
            createChairView($objForm, $arg, $db, $model);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        //hidden作成
        makeHidden($objForm, $model);
        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        // 展開表の処理振分
        if ($model->field["SCH_DIV"] == '1') {
            // 科目展開表
            View::toHTML5($model, "knjb3043Form1Subclass.html", $arg);
        } else {
            View::toHTML5($model, "knjb3043Form1.html", $arg);
        }

    }
}

/**
 * 科目の展開表作成
 */
function createSubclassView(&$objForm, &$arg, $db, $model) {

    $arg['schPtrn'] = '1';

    // 科目展開表 テンプレートタイトル
    $query = knjb3043Query::getPtrnPreHdat($model);
    $opt = array();
    $opt[] = array("label" => "(新規)", "value" => "0");
    $weekName = array('日', '月', '火', '水', '木', '金', '土');
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $dateTime = explode('.', $row["UPDATED"]);
        list($setDate, $setTime) = explode(' ', $dateTime[0]);
        list($year, $month, $day) = explode('-', $setDate);
        $timestamp = mktime(0, 0, 0, $month, $day, $year);
        $setWeek = $weekName[date('w', $timestamp)];

        $dispDate = str_replace("-", "/", $setDate)."({$setWeek}) {$setTime}";
        $row["PRESEQ"] = sprintf('%02d', $row["PRESEQ"]);

        $opt[] = array('label' => "{$row["PRESEQ"]} {$dispDate} {$row["TITLE"]}",
                        'value' => $row["PRESEQ"]);
    }
    $model->preSeq = strlen($model->preSeq) > 0 ? $model->preSeq : $opt[0]["value"];
    $extra = "id=\"PRESEQ\" onChange=\"btn_submit('editSchDiv')\"";

    $arg['PRESEQ'] = knjCreateCombo($objForm, 'PRESEQ', $model->preSeq, $opt, $extra, $size);
    //ゴミ箱
    if ($model->auth == DEF_UPDATABLE) {
        $arg["TRASH_BOX"] .= "<div id=\"TRASH_BOX\" ondragover=\"f_dragover(event,this)\" ondragleave=\"f_dragleave(event,this)\" ondrop=\"f_dropTrash(event, this)\">ゴミ箱：枠内にドロップして下さい</div>";
    }
    // 行タイトル
    // 画面左側の切り替え(教育課程、年組)
    $model->leftMenuArray = array("教育課程", "教育課程(年組)");
    $opt = array();
    foreach ($model->leftMenuArray as $lmKey => $lmVal) {
        $opt[] = array('label' => $lmVal, 'value' => $lmKey + 1);
    }
    $model->leftMenu = $model->leftMenu ? $model->leftMenu : "1";
    $extra = "onchange=\"return btn_submit('editCmb');\"";
    $arg["LEFT_MENU"] = knjCreateCombo($objForm, "LEFT_MENU", $model->leftMenu, $opt, $extra, 1);


    /**************/
    /* メイン処理 */
    /**************/
    if ($arg['showMain']) {
        // 行タイトル(コース名の一覧を取得)
        $query = knjb3043Query::getCourseName($model);
        $result = $db->query($query);
        $courseArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseArray[] = $row;
        }
        $result->free();

        // メインループ
        cellSubclassLoopMain($objForm, $arg, $db, $model, $courseArray);
    }

    /**
     * 画面右 設定科目一覧
     */
    // コース名
    $query = knjb3043Query::getCourseNameCmb($model);
    $extra = "onChange=\"selectCourse('getSubclass')\"";
    makeCmb($objForm, $arg, $db, $query, $model->courseCd, "COURSECD", $extra, 1, "BLANK");

    // 科目リスト(コースIDが設定されている場合のみ処理)
    $selectList = array();
    // HTMLフォームロード時にAJAXにて取得するので、ココでは取得しない
    $extra = "multiple style=\"width:300px\" ondblclick=\"setSubclass();\"";
    $arg["list"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", '', $selectList, $extra, 20);

    // レイアウト編集(縦)
    //校種コンボ
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "-- 全て --", 'value' => "ALL");
    $query = knjb3043Query::getSchoolKind($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "onchange=\"return layoutSchoolKind();\"";
    $arg["LAYOUT_SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $opt[0]["value"], $opt, $extra, 1);

}

/**
 * 科目の展開表作成(科目の読込み)
 */
function cellSubclassLoopMain(&$objForm, &$arg, $db, &$model, $lineHeadArray) {

    if ($model->readCreditsMstFlg) {
        // 単位マスタのテーブルからデータ取得
        $query = knjb3043Query::getCreditsMstList($model);
    } else {
        // 科目展開表のテーブルからデータ取得
        $query = knjb3043Query::getSubclassList($model);
    }
    $result = $db->query($query);
    $subclassArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        // 行ヘッダキー
        $lineKey = $row['COURSECD'].'-'.$row['MAJORCD'].'-'.$row['GRADE'].'-'.$row['COURSECODE'];
        $lineKey2 = '';
        if ($model->leftMenu == "2") {
            $lineKey2 = $row['GRADE'].'-'.$row['HR_CLASS'];
        }
        $subclassArray[$lineKey][$lineKey2][] = $row;
    }
    $result->free();

    // セル情報作成
    cellSubclassLoopMainParts($objForm, $arg, $db, $model, $lineHeadArray, $subclassArray);

}

/**
 * 科目の展開表作成
 *   科目情報の設定
 */
function cellSubclassLoopMainParts(&$objForm, &$arg, $db, &$model, $lineHeadArray, $subclassArray) {

    $subclassView = array();
    $subclassMaxCnt = 0;

    // 単位数(列)の最大値を取得
    $query = knjb3043Query::getCreditsMax($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $subclassMaxCnt = $row['CREDITS_MAX'];
    // 科目(列)最大件数 + 空白列(5列)
    $subclassMaxCnt = $subclassMaxCnt + 5;

    /**************************
     * 科目の列を単位数で展開
     * 科目の列の最大を取得する
     **************************/
    // コース ループ(縦列)
    $lineHeadArrayCnt = get_count($lineHeadArray);
    for ($lineLoop = 0; $lineLoop < $lineHeadArrayCnt; $lineLoop++) {

        $lineHeader = $lineHeadArray[$lineLoop];
        // コースID
        $lineKey = $lineHeader['COURSECD'].'-'.$lineHeader['MAJORCD'].'-'.$lineHeader['GRADE'].'-'.$lineHeader['COURSECODE'];
        $lineKey2 = '';
        if ($model->leftMenu == "2") {
            $lineKey2 = $lineHeader['GRADE'].'-'.$lineHeader['HR_CLASS'];
        }

        if (isset($subclassArray[$lineKey][$lineKey2])) {
            $subclassList = $subclassArray[$lineKey][$lineKey2];
            $subclassListCnt = get_count($subclassList);

            for ($i=0; $i < $subclassListCnt; $i++) { 
                $subclass = $subclassList[$i];

                // 単位数分の科目を作成する。
                // ※科目展開表テーブルから取得した場合
                //   [ORDER]毎に登録されているので、[CREDITS]は'1'固定で取得している
                for ($credits = 0; $credits < $subclass['CREDITS']; $credits++) { 
                    $subclassView[$lineKey][$lineKey2][] = $subclass;
                } 
            }
        }
    }

    // 初期表示行初期化
    $model->visibleLine = "";
    // コース ループ(縦列)
    $lineHeadArrayCnt = get_count($lineHeadArray);
    for ($lineLoop = 0; $lineLoop < $lineHeadArrayCnt; $lineLoop++) {
        if ($lineLoop > 0) {
            $model->visibleLine .= ",";
        }
        $model->visibleLine .= $lineLoop;

        $lineHeader = $lineHeadArray[$lineLoop];

        // コースID
        $lineKey = $lineHeader['COURSECD'].'-'.$lineHeader['MAJORCD'].'-'.$lineHeader['GRADE'].'-'.$lineHeader['COURSECODE'];
        // 第2キー
        $lineKey2 = '';
        if ($model->leftMenu == "2") {
            // HRクラス
            $lineKey2 = $lineHeader['GRADE'].'-'.$lineHeader['HR_CLASS'];
            // コース名
            $lineLabel = $lineHeader['GRADE_NAME1'].'  '.$lineHeader['HR_NAME'].'<BR>';
        } else {
            // コース名
            $lineLabel = $lineHeader['GRADE_NAME1'].'<BR>';
        }
        $lineLabel .= '('.$lineHeader['COURSECD'].$lineHeader['MAJORCD'].')'.$lineHeader['COURSENAME'].$lineHeader['MAJORNAME'].'<BR>';
        $lineLabel .= '('.$lineHeader['COURSECODE'].')'.$lineHeader['COURSECODENAME'];

        $arg["data"][] = array(
                             'LINE_KEY'=>$lineKey
                             ,'LINE_KEY2'=>$lineKey2
                             ,'LINE_NO'=>$lineLoop
                             ,'TITLE_NAME'=>$lineLabel
                        );

        // 行データ初期化
        $setData2 = array();
        // 科目ループ(横列)
        $subclassListCnt = get_count($subclassView[$lineKey][$lineKey2]);
        for ($itemCnt = 0; $itemCnt < $subclassListCnt; $itemCnt++) {
            $subclass = $subclassView[$lineKey][$lineKey2][$itemCnt];

            // 科目情報
            $val['classcd'] = $subclass['CLASSCD'];
            $val['school_kind'] = $subclass['SCHOOL_KIND'];
            $val['curriculum_cd'] = $subclass['CURRICULUM_CD'];
            $val['subclasscd'] = $subclass['SUBCLASSCD'];
            $val['credits'] = $subclass['CREDITS'];
            // $val['subclassname'] = $subclass['SUBCLASSNAME'];
            $val['subclassname'] = $subclass['SUBCLASSABBV'];
            $values = $val;

            // セルの表示文字成形
            $dispChair  = $subclass['CLASSCD']."-".$subclass['SCHOOL_KIND']."-".$subclass['CURRICULUM_CD']."-".$subclass['SUBCLASSCD']."<BR>";
            $dispChair .= $subclass['SUBCLASSABBV'];

            // セル出力
            $cellDisp = "<td id=\"KOMA_{$lineLoop}_{$itemCnt}\" style=\"cursor:move;\" ";
            $cellDisp .= " draggable=\"true\" class=\"targetbox\" line-key=\"{$lineKey}\" line-key2=\"{$lineKey2}\" ";
            $cellDisp .= " data-val='".json_encode($values)."' >{$dispChair}</td>\n";

            $setData2[] = $cellDisp;
        }

        // 列の最大に満たない場合、空白セルを追加
        for ($i = $subclassListCnt; $i < $subclassMaxCnt; $i++) { 
            $dispChair = "";
            $cellDisp = "<td id=\"KOMA_{$lineLoop}_{$i}\" style=\"cursor:move;\" draggable=\"true\" class=\"targetbox\" line-key=\"{$lineKey}\" line-key2=\"{$lineKey2}\" data-val='' >{$dispChair}</td>\n";
            $setData2[] = $cellDisp;
        }

        $arg["data2"][]["LISTDATA"] = join('', $setData2);
    }
    $arg["setWidth"] = ((int)$subclassMaxCnt) * 110;

    // 列タイトル作成
    for ($i=0; $i < $subclassMaxCnt; $i++) { 
        $setTitle = array();
        $setTitle['RENBAN'] = $i + 1;
        $arg["TITLE"][] = $setTitle;
    }
}

/**
 * 講座の展開表作成
 */
function createChairView(&$objForm, &$arg, $db, $model) {
    
    $arg['schPtrn'] = '2';

    // 科目展開表 テンプレートタイトル
    $query = knjb3043Query::getPtrnPreHdat($model);
    $opt = array();
    $weekName = array('日', '月', '火', '水', '木', '金', '土');
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $dateTime = explode('.', $row["UPDATED"]);
        list($setDate, $setTime) = explode(' ', $dateTime[0]);
        list($year, $month, $day) = explode('-', $setDate);
        $timestamp = mktime(0, 0, 0, $month, $day, $year);
        $setWeek = $weekName[date('w', $timestamp)];

        $dispDate = str_replace("-", "/", $setDate)."({$setWeek}) {$setTime}";
        $row["PRESEQ"] = sprintf('%02d', $row["PRESEQ"]);

        $opt[] = array('label' => "{$row["PRESEQ"]} {$dispDate} {$row["TITLE"]}",
                        'value' => $row["PRESEQ"]);
    }
    $model->preSeq = strlen($model->preSeq) > 0 ? $model->preSeq : $opt[0]["value"];
    $arg['PRESEQ'] = knjCreateCombo($objForm, 'PRESEQ', $model->preSeq, $opt, $extra, $size);

    // 講座展開表 テンプレートタイトル
    $query = knjb3043Query::getPtrnPreChairHdat($model);
    $opt = array();
    $opt[] = array("label" => "(新規)", "value" => "0");
    $weekName = array('日', '月', '火', '水', '木', '金', '土');
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $dateTime = explode('.', $row["UPDATED"]);
        list($setDate, $setTime) = explode(' ', $dateTime[0]);
        list($year, $month, $day) = explode('-', $setDate);
        $timestamp = mktime(0, 0, 0, $month, $day, $year);
        $setWeek = $weekName[date('w', $timestamp)];

        $dispDate = str_replace("-", "/", $setDate)."({$setWeek}) {$setTime}";
        $row["PRECHAIRSEQ"] = sprintf('%02d', $row["PRECHAIRSEQ"]);

        $opt[] = array('label' => "{$row["PRECHAIRSEQ"]} {$dispDate} {$row["TITLE"]}",
                        'value' => $row["PRECHAIRSEQ"]);
    }
    $model->preChairSeq = strlen($model->preChairSeq) > 0 ? $model->preChairSeq : $opt[0]["value"];
    $extra = "id=\"PRECHAIRSEQ\" onChange=\"btn_submit('editSchDiv')\"";

    $arg['PRECHAIRSEQ'] = knjCreateCombo($objForm, 'PRECHAIRSEQ', $model->preChairSeq, $opt, $extra, $size);
    //ゴミ箱
    if ($model->auth == DEF_UPDATABLE) {
        $arg["TRASH_BOX"] .= "<div id=\"TRASH_BOX\" ondragover=\"f_dragover(event,this)\" ondragleave=\"f_dragleave(event,this)\" ondrop=\"f_dropTrash(event, this)\">ゴミ箱：枠内にドロップして下さい</div>";
    }
    // 行タイトル
    // 画面左側の切り替え
    $model->leftMenuArray = array("教育課程", "教育課程(年組)", "教職員", "年組", "科目", "講座");
    $opt = array();
    foreach ($model->leftMenuArray as $lmKey => $lmVal) {
        // 表示の制限
        if (in_array(($lmKey + 1) , [1, 2, 3, 4])) {
            $opt[] = array('label' => $lmVal, 'value' => $lmKey + 1);
        }
    }
    $model->leftMenu = $model->leftMenu ? $model->leftMenu : "2";
    $extra = "onchange=\"return btn_submit('editCmb');\"";
    $arg["LEFT_MENU"] = knjCreateCombo($objForm, "LEFT_MENU", $model->leftMenu, $opt, $extra, 1);

    /**************/
    /* メイン処理 */
    /**************/
    if ($arg['showMain']) {
        if ($model->leftMenu == '1') {
            // 行タイトル(教育課程の一覧を取得)
            $query = knjb3043Query::getPreChairTitle1($model);
        } else if ($model->leftMenu == '2') {
            // 行タイトル(教育課程(年組)の一覧を取得)
            $query = knjb3043Query::getPreChairTitle2($model);
        } else if ($model->leftMenu == '3') {
            // 行タイトル(教職員の一覧を取得)
            // $query = knjb3043Query::getPreChairTitle3($model);
            $query = knjb3043Query::getPreChairTitle3($model);
        } else if ($model->leftMenu == '4') {
            // 行タイトル(年組の一覧を取得)
            $query = knjb3043Query::getPreChairTitle4($model);
            // $query = knjb3043Query::getPreChairTitle1($model);
        } else if ($model->leftMenu == '5') {
            // 行タイトル(科目の一覧を取得)
            // $query = knjb3043Query::getPreChairTitle5($model);
            $query = knjb3043Query::getPreChairTitle1($model);
        } else if ($model->leftMenu == '6') {
            // 行タイトル(講座の一覧を取得)
            // $query = knjb3043Query::getPreChairTitle6($model);
            $query = knjb3043Query::getPreChairTitle1($model);
        }
        $result = $db->query($query);
        $lineHeadArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lineHeadArray[] = $row;
        }
        $result->free();

        // メインループ
        cellChairLoopMain($objForm, $arg, $db, $model, $lineHeadArray);
    }

    //操作区分ラジオ
    $opt = array(1, 2, 3);
    $model->operationRadio = ($model->operationRadio == "") ? "1" : $model->operationRadio;
    $extra = array();
    foreach ($opt as $key => $val) {
        array_push($extra, " id=\"OPERATION_RADIO{$val}\" ");
    }
    $radioArray = knjCreateRadio($objForm, "OPERATION_RADIO", $model->operationRadio, $extra, $opt, get_count($opt));
    foreach ($radioArray as $key => $val) $arg[$key] = $val;

    /**
     * 画面右 設定科目一覧
     */
    // 年組
    $query = knjb3043Query::getHrClassCmb($model);
    $extra = "onChange=\"getChairInfo();\"";
    makeCmb($objForm, $arg, $db, $query, $model->courseCd, "HRCLASSCD", $extra, 1, "BLANK");

    $extra = "onChange=\"layoutSchoolKind();\"";
    makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "LAYOUT_HR_CLASS", $extra, 1, "BLANK");

    // 科目リスト(コースIDが設定されている場合のみ処理)
    $query = knjb3043Query::getSubclassCmb($model);
    $extra = "onChange=\"getChairInfo();\"";
    makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "BLANK");

    $extra = "onChange=\"layoutSchoolKind();\"";
    makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "LAYOUT_SUBCLASS", $extra, 1, "BLANK");

    // 群
    $query = knjb3043Query::getGunCmb($model);
    $extra = "onChange=\"getChairInfo();\"";
    makeCmb($objForm, $arg, $db, $query, $model->courseCd, "GUNCD", $extra, 1, "BLANK");

    // 職員
    $query = knjb3043Query::getStaffCmb($model);
    $extra = "onChange=\"getChairInfo();\"";
    makeCmb($objForm, $arg, $db, $query, $model->courseCd, "STAFFCD", $extra, 1, "BLANK");

    // コース名
    $query = knjb3043Query::getCourseNameCmb($model);
    $extra = "onChange=\"getChairInfo();\"";
    makeCmb($objForm, $arg, $db, $query, $model->courseCd, "COURSECD", $extra, 1, "BLANK");


    // 科目リスト(コースIDが設定されている場合のみ処理)
    $selectList = array();
    // HTMLフォームロード時にAJAXにて取得するので、ココでは取得しない
    $extra = "multiple style=\"width:300px\" ondblclick=\"setChairInfo();\"";
    $arg["list"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", '', $selectList, $extra, 20);

    // レイアウト編集(縦)
    //校種コンボ
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "-- 全て --", 'value' => "");
    $query = knjb3043Query::getSchoolKind($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "onchange=\"return layoutSchoolKind();\"";
    $arg["LAYOUT_SCHOOL_KIND"] = knjCreateCombo($objForm, "LAYOUT_SCHOOL_KIND", $opt[0]["value"], $opt, $extra, 1);
}


function cellChairLoopMain(&$objForm, &$arg, $db, &$model, $lineHeadArray) {

    if ($model->readPreHdatFlg) {
        // 科目展開表のテーブルから講座データ取得
        $query = knjb3043Query::getPtrnPreDat($model);
    } else {
        // 講座展開表のテーブルから講座データ取得
        $query = knjb3043Query::getPtrnPreChairDat($model);
    }

    $result = $db->query($query);
    $subclassArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if ($model->leftMenu == '1') {
            // 行ヘッダキー(コースID)
            $lineKey = $row['COURSECD'].'-'.$row['MAJORCD'].'-'.$row['GRADE'].'-'.$row['COURSECODE'];
            // 行ヘッダキー2(HRクラス)
            $lineKey2 = '';
        } else if ($model->leftMenu == '2') {
            // 行ヘッダキー(コースID)
            $lineKey = $row['COURSECD'].'-'.$row['MAJORCD'].'-'.$row['GRADE'].'-'.$row['COURSECODE'];
            // 行ヘッダキー2(HRクラス)
            $lineKey2 = $row['GRADE'].'-'.$row['HR_CLASS'];
        } else if ($model->leftMenu == '3') {
            // 行ヘッダキー(職員番号)
            $lineKey = $row['STAFFCD'];
            // 行ヘッダキー2(なし)
            $lineKey2 = '';
        } else if ($model->leftMenu == '4') {
            // 行ヘッダキー(HRクラス)
            $lineKey = $row['GRADE'].'-'.$row['HR_CLASS'];
            // 行ヘッダキー2(なし)
            $lineKey2 = '';
        } else {
            // 行ヘッダキー(コースID)
            $lineKey = $row['COURSECD'].'-'.$row['MAJORCD'].'-'.$row['GRADE'].'-'.$row['COURSECODE'];
            // 行ヘッダキー2(HRクラス)
            $lineKey2 = $row['GRADE'].'-'.$row['HR_CLASS'];
        }
        // 列ヘッダキー(横列:PRE_ORDER)
        $preOrder = $row['PRE_ORDER'];

        $subclassArray[$lineKey][$lineKey2][$preOrder][] = $row;

        $orderArray[$preOrder][] = $row;
    }
    $result->free();

    ///////////////////////////////////////////////////////
    // 画面下 重複人数／未配置人数の取得
    // 重複人数チェック処理
    $dupChairArray = checkStdDup($objForm, $arg, $db, $model, $orderArray);
    // 未配置人数チェック処理
    $unPlacedArray = checkStdUnPlaced($objForm, $arg, $db, $model, $orderArray);
    // 施設講座キャパオーバーチェック処理
    $capOverFacArray = checkFacCapOver($objForm, $arg, $db, $model, $orderArray);

    ///////////////////////////////////////////////////////
    // セル情報作成
    cellChairLoopMainParts($objForm, $arg, $db, $model, $lineHeadArray, $subclassArray, $dupChairArray, $unPlacedArray, $capOverFacArray);

}

/**
 * 講座の展開表作成
 *   講座情報の設定
 */
function cellChairLoopMainParts(&$objForm, &$arg, $db, &$model, $lineHeadArray, $subclassArray, $dupChairArray, $unPlacedArray, $capOverFacArray) {

    $subclassView = array();
    $subclassMaxCnt = 0;

    // 単位数(列)の最大値を取得
    $query = knjb3043Query::getCreditsMax($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $subclassMaxCnt = $row['CREDITS_MAX'];
    // 科目(列)最大件数 + 空白列(5列)
    $subclassMaxCnt = $subclassMaxCnt + 5;
    // 校時の件数から最大列幅取得(曜日数)
    $query = knjb3043Query::getPeriodCnt($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $periodMaxCnt = (int)$row['CNT'] * 7;
    // 単位マスタの最大単位数と校時の列数で大きい方を列数にする
    if ($subclassMaxCnt < $periodMaxCnt) {
        $subclassMaxCnt = $periodMaxCnt;
    }

    // 初期表示行初期化
    $model->visibleLine = "";
    // コース ループ(縦列)
    $lineHeadArrayCnt = get_count($lineHeadArray);
    for ($lineLoop = 0; $lineLoop < $lineHeadArrayCnt; $lineLoop++) {
        if ($lineLoop > 0) {
            $model->visibleLine .= ",";
        }
        $model->visibleLine .= $lineLoop;

        $lineHeader = $lineHeadArray[$lineLoop];

        if ($model->leftMenu == '1') {
            // コースID
            $lineKey = $lineHeader['COURSECD'].'-'.$lineHeader['MAJORCD'].'-'.$lineHeader['GRADE'].'-'.$lineHeader['COURSECODE'];
            // HRクラス
            $lineKey2 = '';
            // コース名
            $lineLabel = $lineHeader['GRADE_NAME1'].'<BR>';
            $lineLabel .= '('.$lineHeader['COURSECD'].$lineHeader['MAJORCD'].')'.$lineHeader['COURSENAME'].$lineHeader['MAJORNAME'].'<BR>';
            $lineLabel .= '('.$lineHeader['COURSECODE'].')'.$lineHeader['COURSECODENAME'];
        } else if ($model->leftMenu == '2') {
            // コースID
            $lineKey = $lineHeader['COURSECD'].'-'.$lineHeader['MAJORCD'].'-'.$lineHeader['GRADE'].'-'.$lineHeader['COURSECODE'];
            // HRクラス
            $lineKey2 = $lineHeader['GRADE'].'-'.$lineHeader['HR_CLASS'];
            // コース名
            $lineLabel = $lineHeader['GRADE_NAME1'].'  '.$lineHeader['HR_NAME'].'<BR>';
            $lineLabel .= '('.$lineHeader['COURSECD'].$lineHeader['MAJORCD'].')'.$lineHeader['COURSENAME'].$lineHeader['MAJORNAME'].'<BR>';
            $lineLabel .= '('.$lineHeader['COURSECODE'].')'.$lineHeader['COURSECODENAME'];
        } else if ($model->leftMenu == '3') {
            // 行ヘッダキー(職員番号)
            $lineKey = $lineHeader['STAFFCD'];
            // 行ヘッダキー2(なし)
            $lineKey2 = '';
            // 職員名
            $lineLabel = $lineHeader['STAFFCD'].'<BR>'.$lineHeader['STAFFNAME'].'<BR>';
        } else if ($model->leftMenu == '4') {
            // 行ヘッダキー(HRクラス)
            $lineKey = $lineHeader['GRADE'].'-'.$lineHeader['HR_CLASS'];
            // 行ヘッダキー2(なし)
            $lineKey2 = '';
            // コース名
            $lineLabel = $lineHeader['GRADE_NAME1'].'  '.$lineHeader['HR_NAME'].'<BR>';
        } else {
            // コースID
            $lineKey = $lineHeader['COURSECD'].'-'.$lineHeader['MAJORCD'].'-'.$lineHeader['GRADE'].'-'.$lineHeader['COURSECODE'];
            // HRクラス
            $lineKey2 = $lineHeader['GRADE'].'-'.$lineHeader['HR_CLASS'];
            // コース名
            $lineLabel = $lineHeader['GRADE_NAME1'].'  '.$lineHeader['HR_NAME'].'<BR>';
            $lineLabel .= '('.$lineHeader['COURSECD'].$lineHeader['MAJORCD'].')'.$lineHeader['COURSENAME'].$lineHeader['MAJORNAME'].'<BR>';
            $lineLabel .= '('.$lineHeader['COURSECODE'].')'.$lineHeader['COURSECODENAME'];
        }

        $arg["data"][] = array(
                              'LINE_KEY'=>$lineKey
                             ,'LINE_KEY2'=>$lineKey2
                             ,'LINE_NO'=>$lineLoop
                             ,'TITLE_NAME'=>$lineLabel
                        );

        // 行データ初期化
        $setData2 = array();

        // 講座情報一覧
        $chairInfoList = $subclassArray[$lineKey][$lineKey2];
        // 列ループ
        for ($i=0; $i < $subclassMaxCnt; $i++) { 
            // PRE_ORDERへの存在判定
            if (isset($chairInfoList[$i])) {

                $chairInfoCnt = get_count($chairInfoList[$i]);

                // 受講生未配置フラグ
                $isUnPlaced = false;
                // 受講生重複フラグ
                $isDupStd = false;
                // 施設重複フラグ
                $isOverFac = false;

                $values = array();
                for ($j=0; $j < $chairInfoCnt; $j++) { 

                    $chairInfo = $chairInfoList[$i][$j];
                    // 科目情報
                    $val['classCd'] = $chairInfo['CLASSCD'];
                    $val['schoolKind'] = $chairInfo['SCHOOL_KIND'];
                    $val['curriculumCd'] = $chairInfo['CURRICULUM_CD'];
                    $val['subclassCd'] = $chairInfo['SUBCLASSCD'];
                    // 講座情報
                    $val['chairCd'] = $chairInfo['CHAIRCD'];
                    $val['chairName'] = $chairInfo['CHAIRABBV'];

                    $values[] = $val;


                    // コースID
                    $courseId = $chairInfo['COURSECD'].'-'.$chairInfo['MAJORCD'].'-'.$chairInfo['GRADE'].'-'.$chairInfo['COURSECODE'];
                    // 科目コード
                    $subclassCd = $chairInfo['CLASSCD'].'-'.$chairInfo['SCHOOL_KIND'].'-'.$chairInfo['CURRICULUM_CD'].'-'.$chairInfo['SUBCLASSCD'];
                    // 受講生未配置講座「未」の表示
                    if (isset($unPlacedArray[$courseId][$subclassCd])) {
                        $isUnPlaced = true;
                    }
                    // 受講生重複講座「重」の表示
                    if (isset($dupChairArray[$i][$chairInfo['CHAIRCD']])) {
                        $isDupStd = true;
                    }
                    // 施設講座キャパオーバー「施」の表示
                    if (isset($capOverFacArray[$i][$chairInfo['CHAIRCD']])) {
                        $isOverFac = true;
                    }
                }

                $dispChair = "";
                // ２件以上登録されている場合
                if (get_count($values) > 1) {
                    $dispChair = get_count($values)."件のデータ".'<br/>';
                } else {
                    // セルの表示文字成形
                    $dispChair  = $values[0]['chairCd']."<br/>";
                    $dispChair .= $values[0]['chairName']."<br/>";
                }
                // 受講生未配置講座「未」の表示
                if ($isUnPlaced) {
                    $dispChair .= "未";
                }
                // 受講生重複講座「重」の表示
                if ($isDupStd) {
                    $dispChair .= "重";
                }
                // 施設キャパ超「施」の表示
                if ($isOverFac) {
                    $dispChair .= "施";
                }

                // セルスタイル設定
                $addClass = "";
                if (get_count($values) > 1) {
                    // 複数講座表示（ 〇件のデータ ）
                    $addClass .= " hukusuu_box ";
                }
                // セル出力
                $cellDisp = "<td id=\"KOMA_{$lineLoop}_{$i}\" style=\"cursor:move;\" ";
                $cellDisp .= " draggable=\"true\" class=\"targetbox {$addClass}\" ";
                $cellDisp .= " line-key=\"{$lineKey}\" line-key2=\"{$lineKey2}\" order=\"{$i}\" ";
                if ($model->readPreHdatFlg) {
                    $cellDisp .= " data-update=\"1\" ";
                }
                $cellDisp .= " data-val='".json_encode($values)."' >{$dispChair}</td>\n";
                $setData2[] = $cellDisp;

            } else {
                // 存在しない場合は空白を追加
                $dispChair = "";

                // $cellDisp = "<td id=\"KOMA_{$lineLoop}_{$i}\" style=\"cursor:move;\" draggable=\"true\" class=\"targetbox\" line-key=\"{$lineKey}\" line-key2=\"{$lineKey2}\" order=\"{$i}\" data-val='' >{$dispChair}</td>\n";

                $cellDisp = "<td id=\"KOMA_{$lineLoop}_{$i}\" style=\"cursor:move;\" ";
                $cellDisp .= " draggable=\"true\" class=\"targetbox\" ";
                $cellDisp .= " line-key=\"{$lineKey}\" line-key2=\"{$lineKey2}\" order=\"{$i}\" ";
                if ($model->readPreHdatFlg) {
                    $cellDisp .= " data-update=\"1\" ";
                }
                $cellDisp .= " data-val='' >{$dispChair}</td>\n";

                $setData2[] = $cellDisp;
            }
        }
        $arg["data2"][]["LISTDATA"] = join('', $setData2);
    }
    $arg["setWidth"] = ((int)$subclassMaxCnt) * 110;

    // 列タイトル作成
    for ($i=0; $i < $subclassMaxCnt; $i++) { 
        $setTitle = array();
        $setTitle['RENBAN'] = $i + 1;
        $arg["TITLE"][] = $setTitle;
    }

}


// 講座の重複人数チェック
function checkStdDup(&$objForm, &$arg, $db, $model, $orderArray) {

    // 単位数(列)の最大値を取得
    $query = knjb3043Query::getCreditsMax($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $subclassMaxCnt = $row['CREDITS_MAX'];
    // 科目(列)最大件数 + 空白列(5列)
    $subclassMaxCnt = $subclassMaxCnt + 5;
    // 校時の件数から最大列幅取得(曜日数)
    $query = knjb3043Query::getPeriodCnt($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $periodMaxCnt = (int)$row['CNT'] * 7;
    // 単位マスタの最大単位数と校時の列数で大きい方を列数にする
    if ($subclassMaxCnt < $periodMaxCnt) {
        $subclassMaxCnt = $periodMaxCnt;
    }

    $stdDupChairArray = array();

    for ($i=0; $i < $subclassMaxCnt; $i++) { 
        $stdDup = array();
        $stdDup['ORDER'] = $i;

        if (isset($orderArray[$i])) {

            $chairList = array();
            for ($j=0; $j < get_count($orderArray[$i]); $j++) { 
                $chairInfo = $orderArray[$i][$j];
                $chairList[] = $chairInfo["CHAIRCD"];
            }

            // 講座毎に受講生重複人数を取得
            $query = knjb3043Query::getStdDupliCheck($model, $chairList, 'CHAIR_LIST');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $stdDupChairArray[$i][$row['CHAIRCD']] = $row['STDCNT'];
            }

            // 列(ORDER)毎の受講生重複人数を取得
            $query = knjb3043Query::getStdDupliCheck($model, $chairList, 'STD_CNT');
            $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
            $stdCnt = $row['STDCNT'];

            $stdDup['STDCNT'] = $row['STDCNT'];
        } else {
            $stdDup['STDCNT'] = "";
        }
        $arg["FOOTER"]["STDDUP"][] = $stdDup;
    }

    return $stdDupChairArray;
}

// 講座の未配置人数チェック
function checkStdUnPlaced(&$objForm, &$arg, $db, $model, $orderArray) {

    // 単位数(列)の最大値を取得
    $query = knjb3043Query::getCreditsMax($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $subclassMaxCnt = $row['CREDITS_MAX'];
    // 科目(列)最大件数 + 空白列(5列)
    $subclassMaxCnt = $subclassMaxCnt + 5;
    // 校時の件数から最大列幅取得(曜日数)
    $query = knjb3043Query::getPeriodCnt($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $periodMaxCnt = (int)$row['CNT'] * 7;
    // 単位マスタの最大単位数と校時の列数で大きい方を列数にする
    if ($subclassMaxCnt < $periodMaxCnt) {
        $subclassMaxCnt = $periodMaxCnt;
    }

    $unPlacedArray = array();

    $query = knjb3043Query::getStdUnPlacedCheck($model, $chairList);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        // コースID
        $courseId = $row['COURSECD'].'-'.$row['MAJORCD'].'-'.$row['GRADE'].'-'.$row['COURSECODE'];
        // 科目コード
        $subclassCd = $row['CLASSCD'].'-'.$row['SCHOOL_KIND'].'-'.$row['CURRICULUM_CD'].'-'.$row['SUBCLASSCD'];

        $unPlacedArray[$courseId][$subclassCd] = $row["STDCNT"];
    }

    for ($i=0; $i < $subclassMaxCnt; $i++) { 
        $stdunPlaced = array();
        $stdunPlaced['ORDER'] = $i;
        if (isset($orderArray[$i])) {
            $stdCnt = 0;
            for ($j=0; $j < get_count($orderArray[$i]); $j++) { 
                $chairInfo = $orderArray[$i][$j];

                // コースID
                $courseId = $chairInfo['COURSECD'].'-'.$chairInfo['MAJORCD'].'-'.$chairInfo['GRADE'].'-'.$chairInfo['COURSECODE'];
                // 科目コード
                $subclassCd = $chairInfo['CLASSCD'].'-'.$chairInfo['SCHOOL_KIND'].'-'.$chairInfo['CURRICULUM_CD'].'-'.$chairInfo['SUBCLASSCD'];

                if (isset($unPlacedArray[$courseId][$subclassCd])) {
                    $stdCnt += $unPlacedArray[$courseId][$subclassCd];
                }
            }
            $stdunPlaced['STDCNT'] = $stdCnt;
        } else {
            $stdunPlaced['STDCNT'] = "";
        }
        $arg["FOOTER"]["UNPLACED"][] = $stdunPlaced;
    }

    return $unPlacedArray;
}

// 施設の重複チェック
function checkFacCapOver(&$objForm, &$arg, $db, $model, $orderArray) {

    // 単位数(列)の最大値を取得
    $query = knjb3043Query::getCreditsMax($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $subclassMaxCnt = $row['CREDITS_MAX'];
    // 科目(列)最大件数 + 空白列(5列)
    $subclassMaxCnt = $subclassMaxCnt + 5;
    // 校時の件数から最大列幅取得(曜日数)
    $query = knjb3043Query::getPeriodCnt($model);
    $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
    $periodMaxCnt = (int)$row['CNT'] * 7;
    // 単位マスタの最大単位数と校時の列数で大きい方を列数にする
    if ($subclassMaxCnt < $periodMaxCnt) {
        $subclassMaxCnt = $periodMaxCnt;
    }

    $facDupChairArray = array();

    for ($i=0; $i < $subclassMaxCnt; $i++) { 
        $facDup = array();
        $facDup['ORDER'] = $i;

        if (isset($orderArray[$i])) {
            $chairList = array();
            for ($j=0; $j < get_count($orderArray[$i]); $j++) { 
                $chairInfo = $orderArray[$i][$j];
                $chairList[] = $chairInfo["CHAIRCD"];
            }
            // 講座毎に施設キャパオーバーを取得
            $query = knjb3043Query::getFacCapOverCheck($model, $chairList);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $facDupChairArray[$i][$row['CHAIRCD']] = $row;
            }
        }
    }
    return $facDupChairArray;
}


//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    // 制限付き表示用
    if ($model->auth != DEF_UPDATABLE) {
        $disabled = " disabled ";
    }
    // 読込
    $extra = "onclick=\"return yomikomiTimeCheck();\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    // 更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // 終了
    if ($model->sendPrg) {
        $extra = "onclick=\"window.opener.btn_submit('main'); btn_close();\"";
    } else {
        $extra = "onclick=\"return btn_close();\"";
    }
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

    ///////////////////////////////////////////////////////
    // 科目展開表 テンプレート削除
    $disabled = " disabled ";
    $extra = "onclick=\"return btn_submit('preSeqDelete');\" {$disabled} ";
    $arg["button"]["btn_preseq_delete"] = knjCreateBtn($objForm, "btn_preseq_delete", "削 除", $extra);

    // 単位マスタ読込
    $extra = "onclick=\"return yomikomiCreditsMstCheck();\"";
    $arg["button"]["btn_creditMstread"] = knjCreateBtn($objForm, "btn_creditMstread", "単位マスタ読込", $extra);

    // 反映(科目展開表 設定科目一覧)
    $extra = "onclick=\"setSubclass();\" ";
    $arg["button"]["btn_set"] = knjCreateBtn($objForm, "btn_set", "反 映", $extra);

    //レイアウト編集(縦)
    if ($model->leftMenu == "1") {
        $callBoxName = "layoutCourseBox";
        $callLayoutIni = "layoutCourseBoxInitFunc";
    } else {
        $callBoxName = "layoutCourseBox";
        $callLayoutIni = "layoutCourseBoxInitFunc";
    }
    $disabled = " ";
    $extra = "onclick=\"showDialog('{$callBoxName}', 'レイアウト編集(縦)', {$callLayoutIni});\" {$disabled} ";
    $arg["button"]["btn_layout"] = knjCreateBtn($objForm, "btn_layout", "レイアウト編集(縦)", $extra);

    ///////////////////////////////////////////////////////
    // 講座展開表 テンプレート削除
    $disabled = " disabled ";
    $extra = "onclick=\"return btn_submit('preChairSeqDelete');\" {$disabled} ";
    $arg["button"]["btn_preChairSeqDelete"] = knjCreateBtn($objForm, "btn_preChairSeqDelete", "削 除", $extra);

    // 科目展開表読込
    $extra = "onclick=\"return yomikomiPreHdatCheck();\"";
    $arg["button"]["btn_preHdatRead"] = knjCreateBtn($objForm, "btn_preHdatRead", "科目展開表読込", $extra);

    // 反映(講座展開表 設定講座一覧)
    $extra = "onclick=\"setChairInfo();\" ";
    $arg["button"]["btn_setChair"] = knjCreateBtn($objForm, "btn_setChair", "反 映", $extra);

    //色分け表示設定
    $disabled = " ";
    $extra = "onclick=\"showDialog('backColorChangeBox','色分け表示設定',backColorInitFunc);\" {$disabled} ";
    $arg["button"]["btn_backColor"] = knjCreateBtn($objForm, "btn_backColor", "色分け表示設定", $extra);

    //レイアウト編集(縦)
    if ($model->leftMenu == "1") {
        $callBoxName = "layoutCourseBox";
        $callLayoutIni = "layoutCourseBoxInitFunc";
    } else {
        $callBoxName = "layoutCourseBox";
        $callLayoutIni = "layoutCourseBoxInitFunc";
    }
    $disabled = " ";
    $extra = "onclick=\"showDialog('{$callBoxName}', 'レイアウト編集(縦)', {$callLayoutIni});\" {$disabled} ";
    $arg["button"]["btn_layoutChair"] = knjCreateBtn($objForm, "btn_layoutChair", "レイアウト編集(縦)", $extra);

    //列で移動/コピー/入替/削除
    $extra = "onclick=\"showDialog('copyMoveBox','列で移動/コピー/入替/削除',copyMoveBoxInitFunc);\" {$disabled} ";
    $arg["button"]["btn_moveCopy"] = knjCreateBtn($objForm, "btn_moveCopy", "列で移動/コピー/入替/削除", $extra);

    //重複講座名簿
    $extra  = " onClick=\" btn_overlapMeibo('".REQUESTROOT."/B/KNJB3043_CHAIR_STD_SELECT/knjb3043_chair_std_selectindex.php'";
    $extra .= ", '?SEND_PRGRID=KNJB3043";
    $extra .= "&YEAR=".$model->year."";
    $extra .= "&SEMESTER=".$model->semester."";
    $extra .= "&START_DATE=".$model->semesterInfo["SDATE"]."";
    $extra .= "&PRESEQ=".$model->preChairSeq."";
    $extra .= "&SUBWIN=SUBWIN2";
    $extra .= "');\"";
    $extra .= " {$disabled} ";
    $arg["button"]["btn_chairStd"] = knjCreateBtn($objForm, "btn_chairStd", "重複名簿等の移動", $extra);

}

// hidden作成
function makeHidden(&$objForm, $model) {

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "updateDataList");
    knjCreateHidden($objForm, "selectTD");
    knjCreateHidden($objForm, "startTD");
    knjCreateHidden($objForm, "selectStartTD");
    knjCreateHidden($objForm, "visibleLine", $model->visibleLine);
    knjCreateHidden($objForm, "semesterStartDate", $model->semesterInfo["SDATE"]);
    knjCreateHidden($objForm, "semesterEndDate", $model->semesterInfo["EDATE"]);
    knjCreateHidden($objForm, "AUTHORITY", $model->auth);

    knjCreateHidden($objForm, "REQROOT", REQUESTROOT);

    ///////////////////////////////////////////////////////
    // 科目展開表
    knjCreateHidden($objForm, "readCreditsMstFlg");     // 単位マスタ読込フラグ

    ///////////////////////////////////////////////////////
    // 講座展開表
    knjCreateHidden($objForm, "readPreHdatFlg");        // 科目展開表読込フラグ
}


// makeCmb
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
        if ($value === $row["VALUE"]) $value_flg = true;
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

?>
