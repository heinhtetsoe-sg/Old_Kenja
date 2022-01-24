<?php

require_once('for_php7.php');

class knjp730Model extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $schregno;
    public $setIdArr = array();
    public $slipNoArrOnly = array();

    public function knjp730Model()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }

        if (strlen(VARS::get("SCHREGNO"))) {
            $this->schregno   = VARS::get("SCHREGNO");
            $this->search_div = VARS::get("SEARCH_DIV");
        }
        $this->year = ($this->search_div == "1") ? CTRL_YEAR + 1: CTRL_YEAR;

        if (VARS::get("init") != "") {
            unset($this->schregno);
        }

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        //校種取得
        $shoolKindRow = knjp730Query::getSchoolKind($this);
        $this->schoolKind = $shoolKindRow["SCHOOL_KIND"];
        $this->grade_hr_class = $shoolKindRow["GRADE_HR_CLASS"];

        $this->cmd = VARS::request("cmd");

        $this->field = $this->slipNoArrOnly = array();
        if ($this->cmd == "update") {
            foreach ($this->setIdArr as $key => $val) {
                list($slipNo, $lcd, $mcd, $month) = explode("-", $val);

                $this->field["COLLECT_MONTH_".$val]              = VARS::post("COLLECT_MONTH_".$val);
                $this->field["CHECK-".$slipNo."-".$lcd."-".$mcd] = VARS::post("CHECK-".$slipNo."-".$lcd."-".$mcd);
                if ($lcd == "102") {
                    $this->field["PAID_LIMIT_MONTH_".$val]       = VARS::post("PAID_LIMIT_MONTH_".$val);
                }

                //一つでも値を変更していたら、COLLECT_SLIP_DATのパターンコードを"00"に変更するため
                if ($month != "TOTAL") {
                    if ($this->field["COLLECT_MONTH_".$val] != VARS::post("HID:".$val)) {
                        $this->field["CHANGE_FLG"] = "1";
                    }
                }
                $this->slipNoArrOnly[] = $slipNo;
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function validateInput()
    {
        //納期限、納入月、空チェック
        foreach ($this->slipNoArrOnly as $slipNo) {
            $setId = $slipNo."-102-102";
            //チェックありの時
            if ($this->field["CHECK-".$setId] == "1") {
                foreach ($this->monthArray as $month) {
                    if (($this->field["PAID_LIMIT_MONTH_".$setId."-".$month] != "" && $this->field["COLLECT_MONTH_".$setId."-".$month] == "")
                        || ($this->field["PAID_LIMIT_MONTH_".$setId."-".$month] == "" && $this->field["COLLECT_MONTH_".$setId."-".$month] != "")
                    ) {
                        $this->setWarning("MSG301", "(納入月または納期限)");
                        return false;
                    }
                }
            }
        }

        if (!strlen($this->schregno)) {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }

        return true;
    }

    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjp730Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
