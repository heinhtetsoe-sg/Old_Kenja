<?php

require_once('for_php7.php');

class knjc020gModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $startDate;
    public $endDate;
    public $chairCd;

    public function knjc020gModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->startDate);
            unset($this->endDate);
            unset($this->chairCd);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        $this->startDate = CTRL_DATE;

        if (VARS::request("START_DATE") != "") {
            $this->startDate = VARS::request("START_DATE");
            $this->year = substr($this->startDate, 0, 4);
        }
        $this->chairCd = VARS::request("CHAIRCD");

        if ($this->cmd == 'today') {
            $this->startDate = CTRL_DATE;
        }
        if ($this->cmd == 'before') {
            $this->startDate = date('Y-m-d', strtotime($this->startDate.' -2 week'));
        }
        if ($this->cmd == 'next') {
            $this->startDate = date('Y-m-d', strtotime($this->startDate.' +2 week'));
        }

        $this->endDate = date('Y-m-d', strtotime($this->startDate.' +2 week')-24*60*60);

        $this->schregnos = array();
        if (VARS::request("schregnos") != "") {
            $this->schregnos = explode(',', VARS::request("schregnos"));
        }
        $this->dayPeriods = array();
        if (VARS::request("dayPeriods") != "") {
            $this->dayPeriods = explode(',', VARS::request("dayPeriods"));
        }

        $this->flgdata = array();
        for ($i = 0; $i < get_count($this->dayPeriods); $i++) {
            if (VARS::request('UPDATE_CHECK_'.$i) == '1') {
                $this->flgdata['UPDATE_CHECK_'.$i] = true;
            }
        }
        $this->data = array();
        for ($i = 0; $i < get_count($this->dayPeriods); $i++) {
            for ($j = 0; $j < get_count($this->schregnos); $j++) {
                if (!is_null(VARS::request('syukketu_'.$i.'_'.$j))) {
                    $this->data['syukketu_'.$i.'_'.$j] = VARS::request('syukketu_'.$i.'_'.$j);
                }
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < 14; $i++) {
            $day = date('Y-m-d', strtotime($this->startDate . ' +' . $i . ' day'));
            $dayList[$i] = $day;
        }

        $result = $db->query(knjc020gQuery::getPeriod($this, 'B001'));
        $this->periodArray = array();
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->periodArray[$row["VALUE"]]["PERI_YOMIKAE"] = $periCnt;
            $this->periYomikae[$periCnt] = $row["VALUE"];
            $periCnt++;
        }
        $result->free();

        $row['CHAIRCD'] = $this->chairCd;

        for ($i = 0; $i < get_count($this->dayPeriods); $i++) {
            if (!$this->flgdata['UPDATE_CHECK_'.$i]) {
                continue;
            }
            list($row['EXECUTEDATE'], $row['PERIODCD']) = explode('_', $this->dayPeriods[$i]);
            for ($j = 0; $j < get_count($this->schregnos); $j++) {
                if (isset($this->data['syukketu_'.$i.'_'.$j])) {
                    $row['SCHREGNO'] = $this->schregnos[$j];
                    $row['NEW_DI_CD'] = $this->data['syukketu_'.$i.'_'.$j];
                    $delFlag = false;
                    if ($row['NEW_DI_CD'] == '') {
                        knjc020gQuery::deleteAttendChairStfDat($db, $this, $row);
                        $delFlag = true;
                    } else {
                        if ($row['NEW_DI_CD'] == 'SHUSSEKI') {
                            $row['NEW_DI_CD'] = '';
                        }
                        $result2 = $db->getOne(knjc020gQuery::selectAttendChairStfDat($this, $row));
                        if ($result2 == '0') {
                            knjc020gQuery::insertAttendChairStfDat($db, $this, $row);
                        } else {
                            knjc020gQuery::updateAttendChairStfDat($db, $this, $row);
                        }
                    }
                    $result3 = $db->getOne(knjc020gQuery::selectAttendDatCheck1($this, $row));
                    if ($result3 > '0') {
                        if ($delFlag) {
                            knjc020gQuery::deleteAttendDat($db, $this, $row);
                        } else {
                            knjc020gQuery::updateAttendDat($db, $this, $row);
                        }
                    } else {
                        $result4 = $db->getOne(knjc020gQuery::selectAttendDatCheck2($this, $row));
                        if ($result4 == '0') {
                            knjc020gQuery::deleteAttendDat($db, $this, $row);
                            if (!$delFlag) {
                                knjc020gQuery::insertAttendDat($db, $this, $row);
                            }
                        }
                    }
                }
            }
        }

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
