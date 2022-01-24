<?php

require_once('for_php7.php');


class knjb3042Model extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $control;
    public $field = array();

    public function knjb3042Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->compatible();
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
            $this->auth = AUTHORITY;
            $this->sendPrg = "";
        }
        $this->field['SCH_DIV'] = VARS::post('SCH_DIV') ? VARS::post('SCH_DIV') : $this->field['SCH_DIV'];
        $this->field['BSCSEQ'] = strlen(VARS::post('BSCSEQ')) > 0 ? VARS::post('BSCSEQ') : $this->field['BSCSEQ'];
        $this->field['PTRN_UPDATE_TITLE'] = VARS::post('ptrnUpdate_TITLE') ? VARS::post('ptrnUpdate_TITLE') : $this->field['PTRN_UPDATE_TITLE'];
        $this->field['PTRN_UPDATE_RADIO'] = VARS::post('PTRN_UPDATE_RADIO') ? VARS::post('PTRN_UPDATE_RADIO') : $this->field['PTRN_UPDATE_RADIO'];

        $this->preSeq = VARS::post("PRESEQ");
        $this->updateAddData = VARS::post("updateAddData");
        $this->updateDelData = VARS::post("updateDelData");
        $this->lineStaffInfo = VARS::post("lineStaffInfo");

        $this->operationRadio = VARS::post("OPERATION_RADIO");

        $this->grandHrClassCd = VARS::request("GRAND_HR_CLASSCD");
        $this->subclassCd = VARS::request("SUBCLASSCD");
        $this->gunCd = VARS::request("GUNCD");
        $this->staffCd = VARS::request("STAFFCD");
        $this->couseCd = VARS::request("COUSECD");
        $this->removeIds = VARS::request("REMOVE_IDS");
        $this->testCd = VARS::request("TESTCD");
        if ($this->cmd == 'update') {
            unset($this->grandHrClassCd);
            unset($this->subclassCd);
            unset($this->gunCd);
            unset($this->staffCd);
            unset($this->couseCd);
            unset($this->removeIds);
            unset($this->testCd);
        }

        $this->leftMenu = VARS::request("LEFT_MENU");
        $this->startDate = VARS::request("START_DATE");
        $this->endDate = VARS::request("END_DATE");
        $this->yearSeme = VARS::request("YEAR_SEME");
        if (VARS::request("PROGRAMID") != "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        } elseif ($this->yearSeme) {
            $list = explode('-', $this->yearSeme);
            $this->year = $list[0];
            $this->semester = $list[1];
        }
        if (VARS::get("SEND_DATE") != "") {
            $this->chairInfo["SEND_DATE"] = VARS::get("SEND_DATE");
            $this->chairInfo["SEND_PERIOD"] = VARS::get("SEND_PERIOD");
            $this->chairInfo["SEND_KOUZA"] = VARS::get("SEND_KOUZA");
            $this->chairInfo["SEND_KOMA_VAL"] = VARS::get("SEND_KOMA_VAL");
            $this->chairInfo["SEND_KOMA_EXEC"] = VARS::get("SEND_KOMA_EXEC");
            $this->chairInfo["SEND_KOMA_TEST"] = VARS::get("SEND_KOMA_TEST");
            $this->chairInfo["SEND_KOMA_COUNT_LESSON"] = VARS::get("SEND_KOMA_COUNT_LESSON");
            $this->ciField["CI_CHAIRCD"] = $this->chairInfo["SEND_KOUZA"];
        } else {
            $this->ciField["CI_CHAIRCD"] = VARS::post("CI_CHAIRCD");
            $this->chairInfo["SEND_KOUZA"] = '';
        }
        $this->ciField["CI_EXEC"] = 0;
        if (VARS::post("CI_CHAIRCD") != '') {
            $targetChairCd=VARS::post("CI_CHAIRCD");
        } elseif (VARS::get("SEND_DATE") != "") {
            $valList = explode(':', $this->chairInfo["SEND_KOMA_VAL"]);
            sort($valList);
            $targetChairCd = $this->chairInfo["SEND_KOUZA"] ? $this->chairInfo["SEND_KOUZA"] : $valList[0];
        }
        if (isset($targetChairCd)) {
            $valList = explode(':', $this->chairInfo["SEND_KOMA_VAL"]);
            $execList = explode(',', $this->chairInfo["SEND_KOMA_EXEC"]);
            $testList = explode(',', $this->chairInfo["SEND_KOMA_TEST"]);
            $countLessonList = explode(',', $this->chairInfo["SEND_KOMA_COUNT_LESSON"]);
            for ($i = 0; $i < get_count($valList); $i++) {
                if ($valList[$i] == $targetChairCd) {
                    $this->ciField["CI_EXEC"] = $execList[$i];
                    $this->ciField["CI_TEST"] = $testList[$i];
                    $this->ciField["CI_COUNT_LESSON"] = $countLessonList[$i];
                }
            }
        }

        if (VARS::get("SEND_PRG")) {
            $this->auth = VARS::get("SEND_AUTH");
            $this->sendPrg = VARS::get("SEND_PRG");
        }

        $this->ajaxParam = VARS::request("AJAX_PARAM");
        if (isset($this->ajaxParam)) {
            $this->ajaxParam = json_decode($this->ajaxParam, true);
        }

        $this->cmd = VARS::request("cmd");

        $this->visibleLine = VARS::post("visibleLine");
        if ($this->cmd == 'editCmb' || $this->cmd == '' || $this->cmd == 'editSchDiv') {
            unset($this->visibleLine);
        }

        $this->operationHistory = VARS::post("operationHistory");
        if (isset($this->operationHistory)) {
            $this->operationHistory = json_decode($this->operationHistory, true);
        }
        if ($this->cmd != 'update') {
            unset($this->operationHistory);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if ($this->Properties["KNJB3042_SchTestPattern"] == "1" && $this->field['SCH_DIV'] == '3') {
            $this->leftMenu = '5';
        }
    }

    public function compatible()
    {
        if (!function_exists("json_encode")) {
            function json_encode($param)
            {
                require_once("JSON.php");
                $json = new Services_JSON();
                return $json->encode($param);
            }
            function json_decode($param, $assoc = false)
            {
                require_once("JSON.php");
                $json = new Services_JSON($assoc ? SERVICES_JSON_LOOSE_TYPE : 0);
                return $json->decode($param);
            }
        }
    }

    public function fusemozi($staffCd)
    {
        list($simo, $fuseji) = explode(" | ", $this->Properties["showMaskStaffCd"]);
        $ume = "" ;
        for ($umecnt = 1; $umecnt <= strlen($staffCd) - (int)$simo; $umecnt++) {
            $ume .= $fuseji;
        }
        if ($fuseji) {
            return $ume.substr($staffCd, (strlen($staffCd) - (int)$simo), (int)$simo);
        } else {
            return $staffCd;
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //追加および更新
    public function &getUpdateModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (empty($this->updateAddData) && empty($this->updateDelData) && empty($this->lineStaffInfo)) {
            $this->setMessage("MSG201");
            return;
        }
        if ($this->field['SCH_DIV'] == '1') {
            $this->field['BSCSEQ'] = knjb3042Query::updatePtrnTemplate($db, $this);
            knjb3042Query::updatePtrn($db, $this);
        } else {
            knjb3042Query::update($db, $this);
        }

        if ($this->leftMenu == '1' && !empty($this->lineStaffInfo)) {
            $this->lineStaffArray = explode("|", $this->lineStaffInfo);
            if (is_array($this->lineStaffArray)) {
                $this->chairStff = array();
                foreach ($this->lineStaffArray as $key => $updData) {
                    list($setDate, $periodCd, $chairCd, $testCd, $lineCnt) = explode("_", $updData);
                    $setKey = $setDate."_".$periodCd."_".$chairCd;
                    $setStaff = $this->staffArray[$lineCnt - 1]["STAFFCD"];
                    $this->chairStff[$setKey][] = $setStaff;
                }
            }
            if ($this->field['SCH_DIV'] == '1') {
                knjb3042Query::getPtrnChairStffExistCnt($db, $this);
            } else {
                knjb3042Query::getChairStffExistCnt($db, $this);
            }
        }

        if ($this->field['SCH_DIV'] != '1') {
            try {
                // 変更通知登録
                knjb3042Query::insertOperationHistory($db, $this);
            } catch (\Throwable $th) {
                $db->rollback();
            }
        }

        knjb3042Query::updateUnitSchChrRankDat($db, $this);

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    //基本時間割 テンプレート削除
    public function &deleteBscseqModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjb3042Query::deletePtrnTemplate($db, $this);

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    // 変更通知履歴を更新
    public function &getUpdateOperationHistoryModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        knjb3042Query::updateOperationHistory($db, $this);
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        //「OK」を返却しメッセージボックスを表示する
        echo "OK";
    }

    // 試験配置表出力
    public function getDownloadCSV()
    {
        $db = Query::dbCheckOut();

        $result = $db->query(knjb3042Query::getPeriod($this, 'B004'));
        $this->periodArray = array();
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->periodArray[$row["VALUE"]]["LABEL"] = $row["LABEL"];
            $this->periodArray[$row["VALUE"]]["PERI_YOMIKAE"] = $periCnt;
            $periCnt++;
        }
        $result->free();

        $holidayList = array();
        $result = $db->query(knjb3042Query::getHolidayMst($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayList[] = $row['HOLIDAY'];
        }
        $result->free();

        $this->dateArray = array();
        //日付数
        $this->dateCntMax = 0;
        for ($dateCnt = 0; $dateCnt < 14; $dateCnt++) {
            $setDate = date("Y-m-d", strtotime($this->startDate." +{$dateCnt} day"));
            if ($setDate > str_replace("/", "-", $this->endDate)) {
                break;
            }
            list($year, $month, $day) = preg_split("/-/", $setDate);
            $timestamp = mktime(0, 0, 0, $month, $day, $year);
            $setWeek = $this->week[date('w', $timestamp)];
            $dispDate = str_replace("-", "/", $setDate)."(".$setWeek["WEEK_JP"].")";
            if ($this->Properties["notDispHoliday"] != '1' || (date('w', $timestamp) != 0 && date('w', $timestamp) != 6 && !in_array($setDate, $holidayList))) {
                $this->dateArray[] = array("RENBAN" => $this->dateCntMax, "JPN" => $dispDate, "DATE_OR_WEEK" => $setDate, "CD" => $setWeek["CD"], 'CSS' => $setWeek["CSS"], 'DATE' => $setDate);
                $this->dateCntMax++;
            }
        }

        // ＣＳＶ項目
        $header = array();
        $dateIdxList = array();
        $idx=0;
        foreach ($this->dateArray as $key => $val) {
            foreach ($this->periodArray as $periKey => $periVal) {
                $dateIdxList[$val['DATE']][$periKey] = $idx;
                $idx++;
                $header[]=$val["JPN"].' '.$periVal["LABEL"];
            }
        }

        //登録データ
        $result = $db->query(knjb3042Query::getSchChair($this));
        $schChairArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setPeriCd = $this->periodArray[$row["PERIODCD"]]["PERI_YOMIKAE"];
            if ($row["DATADIV"] == '2') {
                $schChairArray[$row["DATE_OR_WEEK"]][$setPeriCd][] = $row["CHAIRABBV"];
            }
        }
        $result->free();

        $contentsArray=array();
        foreach ($schChairArray as $key1 => $value1) {
            foreach ($value1 as $key2 => $value2) {
                $colIdx=$dateIdxList[$key1][$key2];
                $rowIdx=0;
                foreach ($value2 as $key3 => $value3) {
                    $contentsArray[$rowIdx][$colIdx]=$value3;
                    $rowIdx++;
                }
            }
            $colIdx++;
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }

        $contents  = "";
        $contents .= implode($header, ",")."\n";
        $dataFlg=false;
        for ($i = 0; $i < 30; $i++) {
            $sep='';
            $flg=false;
            $contentsText = '';
            for ($j = 0; $j < get_count($header); $j++) {
                if (isset($contentsArray[$i][$j])) {
                    $flg=true;
                }
                $contentsText.=$sep.$contentsArray[$i][$j];
                $sep=',';
            }
            if (!$flg) {
                break;
            }
            $contents.=$contentsText.','.$this->lastColumn ."\n";
            $dataFlg=true;
        }

        Query::dbCheckIn($db);
        // ＣＳＶ出力
        if ($dataFlg) {
            $filename = sprintf("%s.csv", "試験配置表出力");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
