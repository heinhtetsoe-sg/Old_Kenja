<?php

require_once('for_php7.php');

class knjc030gModel extends Model
{
    public $cmd;
    public $exp_year = "";
    public $exp_semester = "";
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hrClass;
    public $attendno;
    public $maxPeri;
    public $data;

    public function knjc030gModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
            unset($this->attendno);
            unset($this->maxPeri);
            $this->startDate =  CTRL_DATE;
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hrClass = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }

        if (VARS::request("SELECT_DATE") != "") {
            $this->startDate = VARS::request("SELECT_DATE");
        }

        if (VARS::request("MAXPERI")!="") {
            $this->maxPeri       = VARS::request("MAXPERI");
        }
        if (VARS::request("selectdate")!="") {
            $this->selectdate       = VARS::request("selectdate");
        }

        $this->data = array();
        for ($i = 0; $i < 14; $i++) {
            for ($j = 1; $j <= $this->maxPeri; $j++) {
                if (!is_null(VARS::request('syukketu_'.$i.'_'.$j))) {
                    $this->data['syukketu_'.$i.'_'.$j] = VARS::request('syukketu_'.$i.'_'.$j);
                }
            }
        }

        $this->endDate = date('Y-m-d', strtotime($this->startDate.' +2 week')-24*60*60);
        $this->year = substr($this->startDate, 0, 4);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
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
            $dayList[$day] = $i;
        }

        $result = $db->query(knjc030gQuery::getPeriod($this, 'B001'));
        $this->periodArray = array();
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->periodArray[$row["VALUE"]]["PERI_YOMIKAE"] = $periCnt;
            $periCnt++;
        }
        $result->free();

        $tempData = array();
        $result = $db->query(knjc030gQuery::selectQuery($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $dayList[$row['EXECUTEDATE']].'_'.$this->periodArray[$row['PERIODCD']]["PERI_YOMIKAE"];
            if (isset($this->data['syukketu_'.$key])) {
                if ($this->data['syukketu_'.$key] == '') {
                    knjc030gQuery::deleteAttendDat($db, $this, $row);
                } else {
                    if ($this->data['syukketu_'.$key] == 'SHUSSEKI') {
                        $row['NEW_DI_CD'] = '';
                    } else {
                        $row['NEW_DI_CD'] = $this->data['syukketu_'.$key];
                    }
                    $result2 = $db->getOne(knjc030gQuery::selectAttendDat($this, $row));
                    if ($result2 == '0') {
                        knjc030gQuery::insertAttendDat($db, $this, $row);
                    } else {
                        knjc030gQuery::updateAttendDat($db, $this, $row);
                    }
                }
            }
        }
        $result = $db->query(knjc030gQuery::selectQuery($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $executed = ($db->getOne(knjc030gQuery::selectAttendDat($this, $row)) > 0) ? '1' : '0';
            knjc030gQuery::insertUpdateSchChrHrateDat($db, $this, $row, $executed);
        }
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
    public function &getUpdateModel2()
    {
        if (!$this->validateInput()) {
            return;
        }
        $db = Query::dbCheckOut();
        knjc030gQuery::updateAttendDayDat($db, $this, VARS::request('DI_REMARK'));
        $result = $db->query(knjc030gQuery::selectQuery2($this, $this->selectdate));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['DI_CD_FLAG'] == '1') {
                knjc030gQuery::updateAttendDat2($db, $this, $row, VARS::request('DI_REMARK_'.$row['PERIODCD']));
            }
        }
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
