<?php

require_once('for_php7.php');

class knjb3022Form1
{
    function main(&$model)
    {
        set_time_limit(600);

        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb3022index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

$GLOBALS['global_time_start'] = microtime();

        // 学級選択時処理（HRクラス取得）
        if($model->cmd == "getHrClass"){
            $response = array();
            // 送信パラメタ
            //   $model->year;
            //   $model->ajaxParam['GRADE'];
            $query = knjb3022Query::getHrName($model, $model->ajaxParam['GRADE']);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = array(
                    'VALUE' => $row["VALUE"],
                    'LABEL' => $row["LABEL"]
                );
            }
            echo json_encode($response);
            die();
        }

        // クラス選択時処理（教科取得）
        if($model->cmd == "getClass"){
            $response[] = array('VALUE' => '', 'LABEL' => '');
            // 送信パラメタ
            //   $model->year;
            //   $model->staffCd;
            //   $model->staffClass;
            //   $model->ajaxParam['HR_CLASS'];     //-- 配列
            $query = knjb3022Query::getClass($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = array(
                    'VALUE' => $row["VALUE"],
                    'LABEL' => $row["LABEL"]
                );
            }

            // 選択されているHRクラスの担任or副担任へ登録されている場合はHR教科も追加で設定
            $query = knjb3022Query::getHrClassTr($model);
            $result = $db->getOne($query);
            if ($result) {
                $query = knjb3022Query::getClassHr($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $response[] = array(
                        'VALUE' => $row["VALUE"],
                        'LABEL' => $row["LABEL"]
                    );
                }
            }
            echo json_encode($response);
            die();
        }

        // 教科選択時処理（科目取得）
        if($model->cmd == "getSubclass"){
            $response = array();
            // 送信パラメタ
            //   $model->year;
            //   $model->ajaxParam['HR_CLASS'];     //-- 配列
            //   $model->ajaxParam['CLASS_CD'];
            //   $model->ajaxParam['SCHOOL_KIND'];
            $query = knjb3022Query::getSubclass($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = array(
                    'VALUE' => $row["VALUE"],
                    'LABEL' => $row["LABEL"]
                );
            }
            echo json_encode($response);
            die();
        }

        // 反映時処理（科目を受講するHRクラス取得）
        if($model->cmd == "getCreditHrClass"){
            $response = array();
            // 送信パラメタ
            //   $model->year;
            //   $model->ajaxParam['CLASSCD'];
            //   $model->ajaxParam['SCHOOL_KIND'];
            //   $model->ajaxParam['CURRICULUM_CD'];
            //   $model->ajaxParam['SUBCLASSCD'];
            $query = knjb3022Query::getCreditSubclass($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $response[] = array(
                    'GRADE' => $row["GRADE"],
                    'HR_CLASS' => $row["HR_CLASS"]
                );
            }
            echo json_encode($response);
            die();
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //曜日基本時間割用
        $model->weekPtrn = array();
        $model->weekPtrn[] = array("RENBAN" => "0", "CD" => "2", "DATE_OR_WEEK" => "2", "WEEK_JP" => "月", 'CSS' => 'week_getu');
        $model->weekPtrn[] = array("RENBAN" => "1", "CD" => "3", "DATE_OR_WEEK" => "3", "WEEK_JP" => "火", 'CSS' => 'week_ka');
        $model->weekPtrn[] = array("RENBAN" => "2", "CD" => "4", "DATE_OR_WEEK" => "4", "WEEK_JP" => "水", 'CSS' => 'week_sui');
        $model->weekPtrn[] = array("RENBAN" => "3", "CD" => "5", "DATE_OR_WEEK" => "5", "WEEK_JP" => "木", 'CSS' => 'week_moku');
        $model->weekPtrn[] = array("RENBAN" => "4", "CD" => "6", "DATE_OR_WEEK" => "6", "WEEK_JP" => "金", 'CSS' => 'week_kin');
        $model->weekPtrn[] = array("RENBAN" => "5", "CD" => "7", "DATE_OR_WEEK" => "7", "WEEK_JP" => "土", 'CSS' => 'week_do');
        $model->weekPtrn[] = array("RENBAN" => "6", "CD" => "1", "DATE_OR_WEEK" => "1", "WEEK_JP" => "日", 'CSS' => 'week_niti');

        if ($model->cmd != '' && $model->cmd != 'editStaff') {
            $arg['showMain'] = '1';
            $arg['showDummy'] = '';
        } else {
            $arg['showMain'] = '';
            $arg['showDummy'] = '1';
        }

        if ($model->Properties["useSchool_KindField"] == "1") {
            $arg['useSchool_KindField'] = '1';
            $query = knjb3022Query::getSchoolKind($model);
            $extra = "onchange=\"staffCmbChage()\"";
            makeCmb($objForm, $arg, $db, $query, $val, "schoolKind_STAFF", $extra, 1, "BLANK");
            $extra = "onchange=\"hrSubclassCmbChage()\"";
            makeCmb($objForm, $arg, $db, $query, $val, "schoolKind_HrSubClass", $extra, 1, "BLANK");
        }

        // 年度取得
        $query = knjb3022Query::getYear($model);
        $extra = "onChange=\"btn_submit('editStaff')\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1, "");

        //職員
        $query = knjb3022Query::getStaff($model);
        $extra = "onChange=\"btn_submit('editStaff')\"";
        makeCmb($objForm, $arg, $db, $query, $model->staffCd, "STAFFCD", $extra, 1, "BLANK");

        // 科目別基本時間割
        $weekName = array('日', '月', '火', '水', '木', '金', '土');
        $query = knjb3022Query::getPtrnSubclassHdat($model);
        $opt = array();
        $opt[] = array("label" => "(新規)", "value" => "0");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateTime = explode('.', $row["UPDATED"]);
            list($setDate, $setTime) = explode(' ', $dateTime[0]);
            list($year, $month, $day) = explode('-', $setDate);
            $timestamp = mktime(0, 0, 0, $month, $day, $year);
            $setWeek = $weekName[date('w', $timestamp)];
            $dispDate = str_replace("-", "/", $setDate)."({$setWeek}) {$setTime}";
            $row["BSCSEQ"] = sprintf('%02d', $row["SEQ"]);

            $opt[] = array('label' => "{$row["BSCSEQ"]} {$dispDate} {$row["TITLE"]}",
                           'value' => $row["SEQ"]);
        }

        $model->bscSeq = strlen($model->bscSeq) > 0 ? $model->bscSeq : $opt[0]["value"];
        $extra = "id=\"BSCSEQ\" onChange=\"btn_submit('editStaff')\"";
        $arg['BSCSEQ'] = knjCreateCombo($objForm, 'BSCSEQ', $model->bscSeq, $opt, $extra, $size);

        // 学年(選択用)
        $query = knjb3022Query::getSelectGrade($model);
        $extra = "style=\"width:100%\" onChange=\"selectGrade('getHrClass'); \"";
        makeCmb($objForm, $arg, $db, $query, $model->field['GRADE_SELECTED'], "GRADE_SELECTED", $extra, 1, "");
        // 左側年組(選択済)
        $extra = "style=\"width:100%;height:100%;\" multiple ondblclick=\"layoutMove('GRAND_HR_CLASSCD_SELECTED', 'GRAND_HR_CLASSCD', '');\"";
        $arg["GRAND_HR_CLASSCD_SELECTED"] = knjCreateCombo($objForm, "GRAND_HR_CLASSCD_SELECTED", "", array(), $extra, 15);
        // 右側年組(選択用)
        $query = knjb3022Query::getHrName($model, $model->field['GRADE_SELECTED']);
        $extra = "style=\"width:100%;height:100%;\" multiple ondblclick=\"layoutMove('GRAND_HR_CLASSCD', 'GRAND_HR_CLASSCD_SELECTED', '');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field['GRAND_HR_CLASSCD'], "GRAND_HR_CLASSCD", $extra, 15, "");

        // 教科
        $model->staffClass = true;
        $query = knjb3022Query::getClass($model);
        // 職員の資格教科が 0件 の場合は全ての教科を表示
        $result = $db->getOne($query);
        if (!$result) {
            $model->staffClass = false;
        }
        $extra = "style=\"width:100%;\" onChange=\"selectClass('getSubclass')\"";
        makeCmb($objForm, $arg, $db, "", $model->classCd, "CLASSCD", $extra, 1, "BLANK");

        // 科目
        // 初期表示では空値を表示(教科選択時に取得する)
        // $query = knjb3022Query::getSubclass($model);
        $extra = "style=\"width:100%;\" ondblclick=\"setSubClass();\"";
        makeCmb($objForm, $arg, $db, "", $model->subclassCd, "SUBCLASSCD", $extra, 10, "");

        //ゴミ箱
        $arg["TRASH_BOX"] .= "<div id=\"TRASH_BOX\" ondragover=\"f_dragover(event,this)\" ondragleave=\"f_dragleave(event,this)\" ondrop=\"f_dropTrash(event, this)\">ゴミ箱：枠内にドロップして下さい</div>";

        //校時
        $query = knjb3022Query::getPeriod($model);
        $result = $db->query($query);
        $model->periYomikae = array();
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $period = array();
            $period["PERI_NAME"] = $row["LABEL"];
            $period["PERI_ID"] = $row["VALUE"];
            $arg["PERIOD"][] = $period;
            $model->periYomikae[$periCnt] = $row["VALUE"];
            $periCnt++;
        }
        $result->free();

        //最大曜日数・最大校時数
        $model->dateCntMax = 7;
        knjCreateHidden($objForm, "DATECNT_MAX", $model->dateCntMax);
        knjCreateHidden($objForm, "PERIODCNT_MAX", get_count($model->periYomikae));

        $AllDate = array();
        foreach ($model->weekPtrn as $key => $val) {
            $setTitle = array();
            $setTitle['WEEK_NAME'] = $val['WEEK_JP'];
            $setTitle['CSS'] = $val['CSS'];
            $arg["TITLE"][] = $setTitle;
        }

        // メインループ
        cellLoopMain($objForm, $arg, $db, $model, $model->weekPtrn, $arg["PERIOD"]);

// diffmicrotime(microtime());

        //操作区分ラジオ
        $opt = array(1, 2, 3);
        $arg["OPERATION_NAME"] = "曜日・校時";
        $model->operationRadio = ($model->operationRadio == "") ? "1" : $model->operationRadio;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OPERATION_RADIO{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OPERATION_RADIO", $model->operationRadio, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjb3022Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //読込
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //確認
    $extra = "onclick=\"return getContent('kakunin');\"";
    $arg["button"]["btn_kakunin"] = knjCreateBtn($objForm, "btn_kakunin", "確 認", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"return btn_close();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

    //反映
    $extra = "onclick=\"setSubClass();\"";
    $arg["button"]["btn_set"] = knjCreateBtn($objForm, "btn_set", "反 映", $extra);
    //曜日で移動/コピー
    $extra = "onclick=\"showDialog('copyMoveBox','曜日で移動/コピー',copyMoveBoxInitFunc);\"";
    $arg["button"]["btn_moveCopy"] = knjCreateBtn($objForm, "btn_moveCopy", "曜日で移動/コピー", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "staffClass", $model->staffClass);
    knjCreateHidden($objForm, "updateAddData");
    knjCreateHidden($objForm, "updateDelData");
    knjCreateHidden($objForm, "startTD");
    knjCreateHidden($objForm, "selectTD");
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $retFlg = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($query) {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                'value' => $row["VALUE"]);
            if ($value === $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    if ($retFlg) {
        return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}

function diffmicrotime($a, $msg="")
{
    $b = $GLOBALS['global_time_start'];
    list($am, $at) = explode(' ', $a);
    list($bm, $bt) = explode(' ', $b);
    echo $msg. (((float)$am-(float)$bm) + ((float)$at-(float)$bt)).' 秒';
}

function cellLoopMain(&$objForm, &$arg, $db, &$model, $weekArray, $periodArray)
{
    // 科目別基本時間割 登録データ取得
    $query = knjb3022Query::getPtrnSubclassDat($model);
    $result = $db->query($query);
    $schSubclassArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $schSubclassArray[$row['WEEK_CD']][$row['PERIODCD']][] = $row;
    }
    $result->free();

    // セル情報作成
    cellLoopMainParts($objForm, $arg, $db, $model, $weekArray, $periodArray, $schSubclassArray);

    $arg["setWidth"] = get_count($weekArray) * 150;
}

function cellLoopMainParts(&$objForm, &$arg, $db, &$model, $weekArray, $periodArray, $schSubclassArray) {
    // 校時 タイトルループ
    $periodLoopCnt = get_count($periodArray);
    for ($lineCnt = 1; $lineCnt <= $periodLoopCnt; $lineCnt++) {
        $setData2 = array();

        // 曜日 タイトルループ
        $weekLoopCnt = get_count($weekArray);
        for ($weekCnt = 0; $weekCnt < $weekLoopCnt; $weekCnt++) {
            $setIdName = $weekCnt."_".$lineCnt;
            //曜日の読替
            $weekCd = $model->weekPtrn[$weekCnt]['CD'];
            $periodCd = $model->periYomikae[$lineCnt];

            // 時間割に現在の日付校時があるか
            if (isset($schSubclassArray[$weekCd][$periodCd])) {
                $dispChair = "";
                $hrClassName = "";
                $values = array();
                $valCnt = 1;
                // 登録されている曜日校時へ科目を追加する
                foreach ($schSubclassArray[$weekCd][$periodCd] as $value) {
                    $val['week'] = $weekCnt;
                    $val['period'] = $lineCnt;
                    $val['grade'] = $value['GRADE'];
                    // HRクラス情報
                    $val['hrclasscd'] = $value['HR_CLASS'];
                    $val['hrclassname'] = $value['HR_NAME'];
                    // 科目情報
                    $val['classcd'] = $value['CLASSCD'];
                    $val['school_kind'] = $value['SCHOOL_KIND'];
                    $val['curriculum_cd'] = $value['CURRICULUM_CD'];
                    $val['subclasscd'] = $value['SUBCLASSCD'];
                    $val['subclassname'] = $value['SUBCLASSNAME'];
                    $values[] = $val;

                    // セルの表示文字成形
                    if ($valCnt == 1) {
                        $hrClassName .= $val['hrclassname'];
                    }
                    if ($valCnt == 2) {
                        $hrClassName .= " *";
                    }
                    $dispChair  = $hrClassName ."<br/>";
                    $dispChair .= $val['subclassname'];
                    $valCnt++;
                }
                $lineDisp = "<td id=\"KOMA_{$setIdName}\" style=\"cursor:move;\" draggable=\"true\" class=\"targetbox\" data-val='".json_encode($values)."' >{$dispChair}</td>\n";
            } else {
                $dispChair = "";
                $lineDisp = "<td id=\"KOMA_{$setIdName}\" style=\"cursor:move;\" draggable=\"true\" class=\"targetbox\" data-val='' >{$dispChair}</td>\n";
            }
            $setData2[] = $lineDisp;
        }
        $arg["data2"][]["LISTDATA"] = join('', $setData2);
    }
}

//色変えのCSSクラス生成
function makeAddClass($dataExec) {
    $addClass = '';
    return $addClass;
}

?>
