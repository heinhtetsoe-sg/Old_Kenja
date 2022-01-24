<?php

require_once('for_php7.php');
class knjc010aForm1
{
    public function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc010aindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["YEAR"] = CTRL_YEAR;

        /* 学期 */
        $arg["SEMESTER"] = $model->control_data["学期名"][CTRL_SEMESTER];

        $arg["fep"] = $model->Properties["FEP"];

        //クラス備考
        if ($model->Properties["useAttendSemesHrRemark"] == "1" && $model->param["SEND_PRG"] == 'KNJC030A') {
            if (!isset($model->warning)) {
                $query = knjc010aQuery::getHrRemark($model);
                $model->field["HR_REMARK"] = $db->getOne($query);
            }
            $extra = "id=\"HR_REMARK\"";
            $arg["HR_REMARK"] = knjCreateTextArea($objForm, "HR_REMARK", "3", "30", "soft", $extra, $model->field["HR_REMARK"]);
            knjCreateHidden($objForm, "HR_REMARK_KETA", 30);//桁数は半角文字数文(全角10文字なら×2で20と設定)
            knjCreateHidden($objForm, "HR_REMARK_GYO", 3);
            KnjCreateHidden($objForm, "HR_REMARK_STAT", "statusarea1");
            $arg["USE_HR_REMARK"] = "1";
        }

        /* 処理日 */
        $model->param["syoribi"] = $model->param["syoribi"] ? $model->param["syoribi"] : CTRL_DATE;
        knjCreateHidden($objForm, "hid_syoribi", $model->param["syoribi"]);
        $tDate = $model->param["syoribi"];
        $tWeekArray = array("日", "月", "火", "水", "木", "金", "土");
        $tTime = strtotime($tDate);
        $tWeek = date("w", $tTime);
        $arg["TITLE_SYORI_BI"] = (str_replace("-", "/", $tDate))."({$tWeekArray[$tWeek]})";

        /* 校時 */
        $query = knjc010aQuery::getPeriod($model->param["periodcd"]);
        $tPeri = $db->getOne($query);
        $arg["TITLE_PERIOD"] = $tPeri;

        /* 出欠制御 */
        $tDate = $model->attndCntlDt;
        $tWeekArray = array("日", "月", "火", "水", "木", "金", "土");
        $tTime = strtotime($tDate);
        $tWeek = date("w", $tTime);
        $arg["TITLE_ATTEND_CTRL_DATE"] = (str_replace("-", "/", $tDate))."({$tWeekArray[$tWeek]})";

        /* ログイン職員 */
        $query = knjc010aQuery::getStaff();
        $tStaff = $db->getOne($query);
        $arg["TITLE_STAFF_NAME"] = $tStaff;

        /* 対象講座 */
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query = knjc010aQuery::getChairName($model);
            $tChairName = $db->getOne($query);
            $arg["TITLE_CHAIR_NAME"] = $tChairName;

            $query = knjc010aQuery::getSchChrList($model, $model->param["syoribi"]);
            $result = $db->query($query);
            $opt = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $value = $row["PERIODCD"].":".$row["CHAIRCD"];
                $label = $row["PERIODNAME"].":".$row["CHAIRNAME1"];
                $opt[] = array('value' => $value, 'label' => $label);
            }
            $value = $model->param["periodcd"].":".$model->param["chaircd"];
            $extra = "onChange=\"changePeriodChair(this)\"";
            $arg["TITLE_CHAIR_COMBO"] = knjCreateCombo($objForm, "TITLE_CHAIR_COMBO", $value, $opt, $extra, $size);

            $query = knjc010aQuery::getChairStaff($model, $model->param["chaircd"]);
            $tChairStaff = $db->getOne($query);
            $arg["TITLE_CHAIR_STAFF"] = "【{$tChairStaff}】";
        }

        /* 入力勤怠情報 */
        if ($model->Properties["knjc010aShowAttendInfo"] == "1") {
            if ($model->param["SEND_PRG"] == "KNJC030A") {
                knjCreateHidden($objForm, "showAttendInfo", "1");
                $arg["TITLE_UPDATE_STAFF_TITLE"]   = "時間割";
                $arg["TITLE_INPUT_CHAIR_NAME"]   = "<span id=\"INPUT_CHAIR_NAME\"></span>";
                $arg["TITLE_INPUT_CHAIR_STAFF"]  = "<span id=\"INPUT_CHAIR_STAFF\"></span>";
                $arg["TITLE_INPUT_CHAIR_CREDIT"] = "<span id=\"INPUT_CHAIR_CREDIT\"></span>";
                $arg["TITLE_INPUT_UPDATE_STAFF"] = "<span id=\"INPUT_UPDATE_STAFF\"></span>";
                $arg["TITLE_INPUT_UPDATE_TIME"] =  "<span id=\"INPUT_UPDATE_TIME\"></span>";
            }
        }

        //実施区分
        if (!isset($model->warning)) {
            $query = knjc010aQuery::getSchChrExecutediv($model);
            $model->field["TITLE_EXECUTEDIV"] = $db->getOne($query);
        }
        $query = knjc010aQuery::getExecutediv();
        $extra = "onChange = \"chgDataDisp('sendKintai')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TITLE_EXECUTEDIV"], "TITLE_EXECUTEDIV", $extra, 1, "");

        //表示制御
        $query = knjc010aQuery::getSeigyo();
        $extra = "onChange = \"chgDataDisp('sendKintai')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TITLE_DISP_SEIGYO"], "TITLE_DISP_SEIGYO", $extra, 1, "");

        //radio
        $opt_data = array(1, 2);
        $model->field["INPUT_TYPE"] = ($model->field["INPUT_TYPE"] == "") ? "1" : $model->field["INPUT_TYPE"];
        $extra = array();
        foreach ($opt_data as $key => $val) {
            array_push($extra, " id=\"INPUT_TYPE{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "INPUT_TYPE", $model->field["INPUT_TYPE"], $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //タイトル校時
        $query = knjc010aQuery::getTitlePeriod();
        $result = $db->query($query);
        $setTitle = array();
        $setTitleCnt = 0;
        $setTotalWidth = 0;
        $komaHaba = 80;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setTitle[$setTitleCnt]["TITLE_WIDTH"] = $komaHaba."px";
            $setTitle[$setTitleCnt]["PERIOD"] = $row["NAME1"];
            $setTitle[$setTitleCnt]["PERIODCD"] = $row["NAMECD2"];
            $setTitle[$setTitleCnt]["TITLE_CHAIR"] = "-";
            $setTitle[$setTitleCnt]["TITLE_ATTEND"] = "-";
            $setTitle[$setTitleCnt]["SET_PERIOD"] = $row["NAMECD2"];
            $setTitleCnt++;
            $setTotalWidth += $komaHaba;
        }
        $result->free();
        $setTotalWidth += 7 * $setTitleCnt;
        $arg["TITLE_TOTALWIDTH"] = $setTotalWidth;
        $setTitle[$setTitleCnt - 1]["TITLE_WIDTH"] = "#";
        $arg["TITLE"] = $setTitle;

        //連続校時(下)
        $query = knjc010aQuery::getRenzokuPeriodMst($model->param["periodcd"], "LOW");
        $result = $db->query($query);
        $model->periLow = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->periLow[] = $row["NAMECD2"];
        }
        $result->free();

        //連続校時(上)
        $query = knjc010aQuery::getRenzokuPeriodMst($model->param["periodcd"], "UP");
        $result = $db->query($query);
        $model->periUp = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->periUp[] = $row["NAMECD2"];
        }
        $result->free();

        if (($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") && $model->Properties["knjc010aNotUseRenzoku"] != "1") {
            $renPeri = "";
            $renChairPeri = "";
            $periList = array();
            $chairList = array();

            $query = knjc010aQuery::getSchChrList($model, $model->param["syoribi"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chairList[$row["PERIODCD"]][$row["CHAIRCD"]] = $row;
            }
            $result->free();

            // 連続講座(下)
            foreach ($model->periLow as $key => $value) {
                if ($chairList[$value][$model->param["chaircd"]]) {
                    $periList[$value] = $chairList[$value][$model->param["chaircd"]]["PERIODNAME"];
                } else {
                    break;
                }
            }
            // 連続講座(上)
            foreach ($model->periUp as $key => $value) {
                if ($chairList[$value][$model->param["chaircd"]]) {
                    $periList[$value] = $chairList[$value][$model->param["chaircd"]]["PERIODNAME"];
                } else {
                    break;
                }
            }

            // 校時で並び替え
            ksort($periList);
            $sep = "";
            foreach ($periList as $key => $value) {
                $renChairPeri .= $sep.$key;
                $renPeri .= $sep.$value;
                $sep = ",";
            }
            $renChairPeri .= $sep.$model->param["periodcd"];

            // 同じ講座が連続している校時をタイトルへ追加
            if ($renPeri) {
                $arg["TITLE_PERIOD"] .= "(".$renPeri.")";
            }

            knjCreateHidden($objForm, "renzkChairPriod", $renChairPeri);
        }

        /* 出欠リスト */
        $model->AttList = makeAttendList($objForm, $arg, $db, $model, $setTitle);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $setTitle);

        /* ボタン作成 */
        makeButton($objForm, $arg, $model);

        /* 表示生徒の最新の最終更新日取得 */
        $query = knjc010aQuery::getLastUpdate($model, $model);
        $lastUpdate = $db->getOne($query);

        //メニューから呼び出された場合
        if ($model->param["SEND_PRG"] == "" && $model->param["periodcd"] == "") {
            $jscript  = "loadwindowSchChrList('".$model->param["syoribi"]."')";
            $arg["jscript"]  = $jscript;
        }

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "changeVal", $model->changeVal);
        knjCreateHidden($objForm, "SELECT_SCHREGNO");
        knjCreateHidden($objForm, "PRAM_PERI", $model->param["periodcd"]);
        knjCreateHidden($objForm, "LASTUPDATE", $lastUpdate);

        knjCreateHidden($objForm, "SCH_CHR_EXECUTEDATE", $model->param["syoribi"]);
        knjCreateHidden($objForm, "SCH_CHR_PERIODCD", $model->param["periodcd"]);
        knjCreateHidden($objForm, "SCH_CHR_CHAIRCD", $model->param["chaircd"]);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML5($model, "knjc010aForm1.html", $arg);
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//出欠リスト作成
function makeAttendList(&$objForm, &$arg, $db, $model, $setTitle)
{
    $query = knjc010aQuery::getItiniti($model);
    $isItinit = $db->getOne($query) == "1" ? true : false;

    $query = knjc010aQuery::getAttendDiCdDat($model, $isItinit);
    $result = $db->query($query);
    $hiddenVal = "";
    $hiddenShow = "";
    $sep = "";
    $listCnt = 0;
    $retAttList = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$listCnt."')\"",
                             "SHOW" => $row["SHOW"],
                             "NAME" => $row["LABEL2"]);
        $hiddenVal .= $sep.$row["VALUE"];
        $hiddenShow .= $sep.$row["SHOW"];
        $hiddenIthiniti .= $sep.$row["ITINITI"];
        $retAttList[$row["VALUE"]] = $listCnt;
        $sep = ",";
        $listCnt++;
    }
    //備考入力
    $dataArray[] = array("VAL"  => "\"javascript:setClickValue('888')\"",
                         "SHOW" => '',
                         "NAME" => '備考入力へ');
    $hiddenVal .= $sep.'remark';
    $hiddenShow .= $sep.'';
    $hiddenIthiniti .= $sep.'888';
    $retAttList[$row["VALUE"]] = $listCnt;
    $sep = ",";

    knjCreateHidden($objForm, "SETVAL", $hiddenVal);
    knjCreateHidden($objForm, "SETSHOW", $hiddenShow);
    knjCreateHidden($objForm, "ITINITI", $hiddenIthiniti);
    $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
    $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
    foreach ($dataArray as $key => $val) {
        $setData["CLICK_NAME"] = $val["NAME"];
        if (strlen($val["SHOW"]) == 3 || strlen($val["SHOW"]) == 2) {
            $val["SHOW"] = "&nbsp;".$val["SHOW"];
        }
        $setData["CLICK_NAME_KIGOU"] = $val["SHOW"]."：";
        if (strlen($val["SHOW"]) == 0) {
            $setData["CLICK_NAME_KIGOU"] = '　'."&nbsp;&nbsp;&nbsp;".$val["SHOW"];
        }
        $setData["CLICK_VAL"] = $val["VAL"];
        $arg["menu"][] = $setData;
    }
    return $retAttList;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model, $setTitle)
{

    //タイトル行事予定は固定
    $schregArray = array();
    $meisaiCnt = 0;

    //タイトル校時
    $model->period = array();
    $query = knjc010aQuery::getSchregNo($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $schregArray[$meisaiCnt] = $row;
        $meisaiCnt++;
    }
    $result->free();

    $setSchData = array();
    $setPeriData = array();

    //タイトルをセット
    $hiddenSchreg = "";
    $schSep = "";
    //KNJC030Aの時に使用、表示時のDI保持
    $hiddenDefSchDi = "";
    $defSchDiSep = "";
    $minCredit = "";
    $maxCredit = "";
    $updStaff = "";
    $befUpdated = "";
    $attend_sdate = "";
    $attend_seme_month = array();
    $schoolMst = array();
    $chairDat = array();

    $query1  = "SELECT SEMESTER ";
    $query1 .= "  , RIGHT('00' || RTRIM(CAST(MONTH('{$model->param["syoribi"]}') AS CHAR(2))), 2) AS MONTH ";
    $query1 .= "  , RIGHT('00' || RTRIM(CAST(DAY('{$model->param["syoribi"]}') AS CHAR(2))), 2) AS DAY ";
    $query1 .= "  FROM SEMESTER_MST ";
    $query1 .= "  WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER <> '9' AND '{$model->param["syoribi"]}' BETWEEN SDATE AND EDATE ";
    $row = $db->getRow($query1, DB_FETCHMODE_ASSOC);
    $dateSemester = $row["SEMESTER"];
    $dateMonth = $row["MONTH"];
    $dateDay = $row["DAY"];
    $z010 = $db->getOne(knjc010aQuery::getZ010());

    $query = knjc010aQuery::getAttendDate($z010, $dateSemester, $dateMonth, $dateDay);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $attend_seme_month[] = $row["SEMESTER"]."-".$row["MONTH"];
        if ($z010 == 'KINDAI') {
            if ($row["MAX_APP"] == 1) { // 近大の締日1は翌月1日
                $attend_sdate = ($row["MONTH"] + 1) ."-" .$row["MAX_APP"];
            } else {
                $attend_sdate = $row["MONTH"] ."-" .$row["MAX_APP"];
            }
        } else {
            $attend_sdate = $row["MONTH"] ."-" .$row["MAX_APP"];
        }
    }

    $result->free();
    if ($attend_sdate == "") {
        $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '1' ";
        $attend_sdate = $db->getOne($query2);   //学期開始日
    } else {
        list($setMonth, $setDay) = explode("-", $attend_sdate);
        $setYear = $setMonth < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
        $query2 = "VALUES Add_days(date('".$setYear."-".$attend_sdate."'), 1)";
        $attend_sdate = $db->getOne($query2);   //次の日
    }
    $schoolMst = $db->getRow(" SELECT * FROM SCHOOL_MST WHERE YEAR = '".CTRL_YEAR."' ", DB_FETCHMODE_ASSOC);
    $chairSql = " SELECT * FROM CHAIR_DAT WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".$dateSemester."' AND CHAIRCD = '".$model->param["chaircd"]."' ";
    $chairDat = $db->getRow($chairSql, DB_FETCHMODE_ASSOC);

    // 時間割データ
    $schregNos = "('' ";
    foreach ($schregArray as $key => $val) {
        $schregNos .= ", '".$val["SCHREGNO"]."'";
    }
    $schregNos .= ")";
    $schChrInfo = array();
    $query = knjc010aQuery::getSchChrInfo($model, $schregNos);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["EXECUTED"] = $row["EXECUTED"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["SUBCLASSABBV"] = $row["SUBCLASSABBV"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_CD"] = $row["DI_CD"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["CHAIRNAME"] = $row["CHAIRNAME"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_REMARK"] = $row["DI_REMARK"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_MARK"] = $row["DI_MARK"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_NAME1"] = $row["DI_NAME1"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["ATTEND_DAT_UPDATED"] = $row["ATTEND_DAT_UPDATED"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["ATTEND_DAT_UPDATED_STAFFNAME"] = $row["ATTEND_DAT_UPDATED_STAFFNAME"];
        $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["REMARK"] = $row["REMARK"];
    }

    // 仮出欠データ
    $petition = array();
    $query = knjc010aQuery::getPetition($model, $schregNos);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_CD"] = $row["DI_CD"];
        $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_REMARK"] = $row["DI_REMARK"];
        $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_NAME1"] = $row["DI_NAME1"];
        $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_MARK"] = $row["DI_MARK"];
    }
    $petitionJsonArray = array();
    foreach ($petition as $schregno => $val) {
        $peris = array();
        foreach ($val as $peri => $cdremark) {
            $peris[] = " \"".$peri."\": \"".$cdremark["DI_CD"]."\"";
        }
        $petitionJsonArray[] = "\"".$schregno."\" : {".implode(',', $peris)."} ";
    }
    $petitionJson = "{".implode(',', $petitionJsonArray)."}";

    $setCreditOlds = array();
    $knjc030aAllInputFlg = $model->Properties["knjc030aInputSelectOnly"] != '1';
    foreach ($schregArray as $schKey => $schVal) {
        $setSchData[$schKey]["SCHREG_HR"] = $schVal["HR_NAME"]."-".$schVal["ATTENDNO"]."番";
        $setSchData[$schKey]["SCHREG_NAME"] = $schVal["NAME"];
        $knjc030aAllInputFlg = $model->param["SEND_PRG"] == "KNJC030A" && ($model->param["SEND_AUTH"] == DEF_UPDATABLE || $schVal["TR_FLG"] == "1") && $knjc030aAllInputFlg;
        //異動情報チップ
        $query = knjc010aQuery::getIdouInfo($model, $schVal["SCHREGNO"]);
        $setIdou = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $setHelpIdou = $schVal["SEX"]." 「".$schVal["NAME_KANA"]."」";
        $setNameColor = "";
        if ($setIdou["REMARK"]) {
            $setHelpIdou .= str_replace("-", "/", $setIdou["REMARK"]);
            $setNameColor = "blue";
        }

        if (!array_key_exists($schVal["COURSECD"].$schVal["MAJORCD"].$schVal["GRADE"].$schVal["COURSECODE"], $setCreditOlds)) {
            $query = knjc010aQuery::getCredit($model, $schVal["COURSECD"], $schVal["MAJORCD"], $schVal["GRADE"], $schVal["COURSECODE"], $model->param["chaircd"]);
            $setCredit = $db->getOne($query);
            $minCredit = $minCredit ? $minCredit : $setCredit;
            $maxCredit = $maxCredit ? $maxCredit : $setCredit;
            if ($minCredit > $setCredit) {
                $minCredit = $setCredit;
            }
            if ($maxCredit < $setCredit) {
                $maxCredit = $setCredit;
            }
            $setCreditOlds[$schVal["COURSECD"].$schVal["MAJORCD"].$schVal["GRADE"].$schVal["COURSECODE"]] = $setCredit;
        }

        //出欠上限
        $query = knjc010aQuery::getSyukketuJougen($model, $schVal["SCHREGNO"], $model->param["chaircd"], $attend_seme_month, $attend_sdate, $schoolMst, $chairDat);
        $setSyukketu = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $setHelpSyukketu = "欠時数={$setSyukketu["SICK"]}, 遅刻数=".$setSyukketu["LATE_INFO"];
        if ($setSyukketu["SICK"] > 0) {
            $setHelpSyukketu .= "(単位={$setSyukketu["CREDITS"]}, 欠課上限注意[履修、修得] = [{$setSyukketu["RISHU_JOGENCHI"]},{$setSyukketu["SHUTOKU_JOGENCHI"]}]";
            if ($setSyukketu["KAISU_OR_SHUSU"] == '1') {
                $setHelpSyukketu .= " 回数={$setSyukketu["KAISU_SHUSU"]}";
            } else {
                $setHelpSyukketu .= " 週数={$setSyukketu["KAISU_SHUSU"]}]";
            }

            if ($setSyukketu["COLOR"] == 'RED') {
                $setSchData[$schKey]["NAME_BGCOLOR"] = "bgColor=\"red\"";
            } elseif ($setSyukketu["COLOR"] == 'YELLOW') {
                $setSchData[$schKey]["NAME_BGCOLOR"] = "bgColor=\"yellow\"";
            }
        }
        $setHelp = $setHelpIdou."　".$setHelpSyukketu;

        $setSchData[$schKey]["viewhelp"] = "onMouseOver=\"ViewcdMousein('{$setHelp}')\" onMouseOut=\"ViewcdMouseout()\"";
        $setSchData[$schKey]["NAME_COLOR"] = $setNameColor;
        $setPeriData[$schKey]["SCHREGROWS"] = knjCreateHidden($objForm, "SCHREGROWS[]", $schVal["SCHREGNO"]);
        $renzokuPeri = getRenzokuPeri($db, $model, $schVal["SCHREGNO"], $model->param["chaircd"]);
        $setRenzokuPeri = "";
        $setRenzokuPeriSep = "";
        foreach ($renzokuPeri as $gakey => $gaval) {
            $setRenzokuPeri .= $setRenzokuPeriSep.$gakey."@".$gaval;
            $setRenzokuPeriSep = ":";
        }
        //hidden
        knjCreateHidden($objForm, "sendRenzoku_{$schVal["SCHREGNO"]}", implode(",", $renzokuPeri));
        knjCreateHidden($objForm, "renzokuPeri", $setRenzokuPeri);

        foreach ($setTitle as $key => $val) {
            $diCd = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["DI_CD"];
            $updated = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["ATTEND_DAT_UPDATED"];
            if ($befUpdated == "" || $updated > $befUpdated) {
                $updStaff = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["ATTEND_DAT_UPDATED_STAFFNAME"];
            }
            if ($updated) {
                $befUpdated = $updated;
            }

            if ($model->field["TITLE_DISP_SEIGYO"] == "1" || $model->field["TITLE_DISP_SEIGYO"] == "2") {
                $setAttend = array();
                if ($model->field["TITLE_DISP_SEIGYO"] == "1") {
                    $setAttend["LABEL"] = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["DI_MARK"];
                } elseif ($model->field["TITLE_DISP_SEIGYO"] == "2") {
                    $setAttend["LABEL"] = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["DI_NAME1"];
                }
            } elseif ($model->field["TITLE_DISP_SEIGYO"] == "3") {
                $query = knjc010aQuery::getRemark($model, $schVal["SCHREGNO"], $val["PERIODCD"]);
                $setAttend = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } elseif ($model->field["TITLE_DISP_SEIGYO"] == "4") {
                $query = knjc010aQuery::getSubclassAbbv($model, $schVal["SCHREGNO"], $val["PERIODCD"]);
                $setAttend = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $query = knjc010aQuery::getChairInfo($model, $schVal["SCHREGNO"], $val["PERIODCD"]);
                $setAttend = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
            $setId = "SCH".$schVal["SCHREGNO"]."_".$val["PERIODCD"];
            if (($diCd == '' || $diCd  === '0') && $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["EXECUTED"] != '1') {
                // 仮出欠データ読み込み
                if (is_array($petition[$schVal["SCHREGNO"]]) && array_key_exists($val["PERIODCD"], $petition[$schVal["SCHREGNO"]])) {
                    if (in_array($val["PERIODCD"], $renzokuPeri)) {
                        $diCd = $petition[$schVal["SCHREGNO"]][$val["PERIODCD"]]["DI_CD"];
                    }
                    if ($model->field["TITLE_DISP_SEIGYO"] == "1") {
                        $setAttend["LABEL"] = $petition[$schVal["SCHREGNO"]][$val["PERIODCD"]]["DI_MARK"];
                    } elseif ($model->field["TITLE_DISP_SEIGYO"] == "2") {
                        $setAttend["LABEL"] = $petition[$schVal["SCHREGNO"]][$val["PERIODCD"]]["DI_NAME1"];
                    } elseif ($model->field["TITLE_DISP_SEIGYO"] == "3") {
                        $setAttend["LABEL"] = $petition[$schVal["SCHREGNO"]][$val["PERIODCD"]]["DI_REMARK"];
                    } elseif ($model->field["TITLE_DISP_SEIGYO"] == "4") {
                        $setAttend["LABEL"] = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["SUBCLASSABBV"];
                    } elseif ($model->field["TITLE_DISP_SEIGYO"] == "5") {
                        $setAttend["LABEL"] = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["CHAIRNAME"];
                    }
                }
            }
            //KNJC030Aの時に使用、表示時のDI保持
            $hiddenDefSchDi .= $defSchDiSep.$schVal["SCHREGNO"]."_".$val["PERIODCD"]."@".$diCd;
            $defSchDiSep = ":";

            //更新用Hidden
            $setUpdId = "UPD".$schVal["SCHREGNO"]."_".$val["PERIODCD"];
            knjCreateHidden($objForm, $setUpdId, $diCd);

            //クリック時のDI保持Hidden
            $setClickDiCnt = strlen($model->AttList[$diCd]) ? $model->AttList[$diCd] : 0;
            $setClickId = "CLICK".$schVal["SCHREGNO"]."_".$val["PERIODCD"];
            knjCreateHidden($objForm, $setClickId, $setClickDiCnt);

            if (is_array($schChrInfo[$schVal["SCHREGNO"]]) && array_key_exists($val["PERIODCD"], $schChrInfo[$schVal["SCHREGNO"]])) {
                $schChrCnt = 1;
            } else {
                $schChrCnt = 0;
            }

            $hiddenSchreg .= $schSep.$setId;
            $setColor = "";
            $setClickEvent = "";
            if ($knjc030aAllInputFlg) {
                $setColor = "bgColor=\"#ccffcc\"";
                $setClickEvent = "inPutExe(this, '".$schVal["SCHREGNO"]."_".$val["PERIODCD"]."');";
            } elseif (in_array($val["PERIODCD"], $renzokuPeri)) {
                $setColor = "bgColor=\"#ccffcc\"";
                $setClickEvent = "inPutExe(this, '".$schVal["SCHREGNO"]."_".$val["PERIODCD"]."');";
            }
            $setColor = $schChrInfo[$schVal["SCHREGNO"]][$val["PERIODCD"]]["REMARK"] ? "bgColor=\"yellow\"" : $setColor;
            if ($schChrCnt == 0 || $schVal["TRANSFER_CNT"] > 0 || $schVal["TEN_TAIGAKU"] == 1) {
                $setClickEvent = "";
                $setColor = "bgColor=\"#AAAAAA\"";
                knjCreateHidden($objForm, "DISABLED".$schVal["SCHREGNO"]."_".$val["PERIODCD"]);
            }

            //備考が長い場合は、カットする。(全角6文字まで)
            $atLen = mb_strlen($setAttend["LABEL"]);
            $setAttendBef = $setAttend["LABEL"];
            $setAttendAft = "";
            $setAttend["LABEL"] = "";
            for ($atCnt = 0; $atCnt < $atLen; $atCnt++) {
                $setAttendAft .= mb_substr($setAttendBef, $atCnt, 1, "UTF-8");
                if (strlen($setAttendAft) > 18) {
                    break;
                } else {
                    $setAttend["LABEL"] .= mb_substr($setAttendBef, $atCnt, 1, "UTF-8");
                }
            }

            $setPeriData[$schKey]["PERIOD_DATA"] .= "<td width=\"{$val["TITLE_WIDTH"]}\" height=\"50px\" {$setColor} id=\"{$setId}\" onclick=\"selectRow('{$val["PERIODCD"]}', this, '{$setId}'); {$setClickEvent}\">{$setAttend["LABEL"]} </td>";
            $schSep = ",";
        }
    }

    $setCredit = "";
    if ($minCredit == $maxCredit) {
        $setCredit = $minCredit;
    } else {
        $setCredit = $minCredit."～".$maxCredit;
    }
    $arg["TITLE_CHAIR_CREDIT"] = "【{$setCredit}】";
    $arg["TITLE_UPDATE_STAFF"] = $updStaff;

    $arg["IFRAME"] = View::setIframeJs();
    $arg["period1"] = $setSchData;
    $arg["schreg_data"] = $setPeriData;
    //hidden
    knjCreateHidden($objForm, "HIDDEN_SCHREG", $hiddenSchreg);
    knjCreateHidden($objForm, "HIDDEN_CHAIRCD", $model->param["chaircd"]);
    knjCreateHidden($objForm, "BEF_ID");
    knjCreateHidden($objForm, "BEF_COLOR");
    knjCreateHidden($objForm, "defSchDi", $hiddenDefSchDi);
    knjCreateHidden($objForm, "petitionJson", $petitionJson);
    knjCreateHidden($objForm, "knjc030aAllInputFlg", $knjc030aAllInputFlg);
}

//連続授業
function getRenzokuPeri($db, $model, $schregNo, $chairCd)
{
    $retPeri = array();
    $retPeri[] = $model->param["periodcd"];
    if ($model->param["SEND_PRG"] == "KNJC030A" || $model->Properties["knjc010aNotUseRenzoku"] == "1") {
        return $retPeri;
    }
    foreach ($model->periLow as $key => $val) {
        $query = knjc010aQuery::getRenzokuPeriod($model, $schregNo, $chairCd, $val);
        $periCnt = $db->getOne($query);
        if ($periCnt > 0) {
            $retPeri[] = $val;
        } else {
            break;
        }
    }
    foreach ($model->periUp as $key => $val) {
        $query = knjc010aQuery::getRenzokuPeriod($model, $schregNo, $chairCd, $val);
        $periCnt = $db->getOne($query);
        if ($periCnt > 0) {
            $retPeri[] = $val;
        } else {
            break;
        }
    }
    return $retPeri;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    if ($model->Properties["knjc010NotUseSchedule"] != '1') {
        //時間割作成を開くボタン
        $url = REQUESTROOT."/B/KNJB3042/knjb3042index.php'";
        $param  = "?START_DATE=".$model->param["syoribi"];
        $param .= "&END_DATE=".$model->param["syoribi"];
        $param .= "&YEAR_SEME=".CTRL_YEAR."-".CTRL_SEMESTER;
        $param .= "&SEND_PRG=".$model->param["SEND_PRG"];
        $param .= "&SEND_AUTH=".$model->param["SEND_AUTH"];
        $extra  = "onclick=\"return wopenChair('".$url.", '".$param."');\"";
        $arg["btn_chair"] = knjCreateBtn($objForm, "btn_chair", "時間割作成を開く", $extra);
    }

    if ($model->Properties["KNJC010A_UseJugyouNaiyou"] == '1') {
        //授業内容登録ボタン
        $extra  = "onclick=\"return loadwindowJugyouNaiyouAdd('".$model->param["syoribi"]."');\"";
        $arg["btn_jugyou"] = knjCreateBtn($objForm, "btn_jugyou", "授業内容登録", $extra);
    }

    if (!$model->param["SEND_PRG"]) {
        // if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
        //講座選択ボタン
        $extra  = "onclick=\"return loadwindowSchChrList('".$model->param["syoribi"]."');\"";
        $arg["btn_schChrList"] = knjCreateBtn($objForm, "btn_schChrList", "講座選択ダイヤログを開く", $extra);
    }

    //全て出席ボタン
    $arg["btn_allok"] = knjCreateBtn($objForm, "btn_allok", "全て出席", " onclick=\"return setAllOk('{$model->param["periodcd"]}');\"");
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    if ($model->param["SEND_XMENU"] == "1") {
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
    } else {
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeMethod();\"");
    }
}
