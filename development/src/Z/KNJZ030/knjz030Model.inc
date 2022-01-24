<?php

require_once('for_php7.php');

class knjz030Model extends Model
{
    public $cmd = "";
    public $staffcd = "";
    public $semescount = "";
    public $SEMESTER = array();
    public $SEMESTERNAME = array();
    public $SDATE = array();
    public $EDATE = array();
    public $year = "";

    public function knjz030Model()
    {
    }
    
    public function init()
    {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        
        if (VARS::request("year") != "") {
            $this->year = VARS::request("year");
        } else {
            $this->year = CTRL_YEAR;
        }

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            for ($i = 1; $i <= $this->semescount; $i++) {
                $this->SEMESTER[$i]             =  VARS::post("SEMESTER".$i);
                $this->SEMESTERNAME[$i]         =  VARS::post("SEMESTERNAME".$i);
                $this->SDATE[$i]                =  VARS::post("SDATE".$i);
                $this->EDATE[$i]                =  VARS::post("EDATE".$i);
            }
        }

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck(STAFFCD, "knjz030");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    
    //入力項目チェック
    public function validateInput()
    {
        for ($i = 1; $i <= $this->semescount; $i++) {
            //学期名称チェック
            if (!($this->SEMESTERNAME[$i])) {
                $this->setWarning("MSG903", "項目：学期名称");
                return false;
            }
            //学期名称文字数チェック
            if (strlen($this->SEMESTERNAME[$i]) > 15) {
                $this->setWarning("MSG915", "学期名称 15バイトまで");
                return false;
            }
            
            //日付チェック
            if ($this->SEMESTER[$i] != 9) {
                $j=$i+1;

                if ($this->SDATE[$i] >= $this->EDATE[$i]) {
                    $this->setWarning("MSG916", $this->SEMESTER[$i].
                                        "学期の開始日付と終了日付が正しくありません。");
                    return false;
                } elseif ($this->SEMESTER["$j"] != "9" && ($this->SDATE["$j"] <= $this->EDATE[$i])) {
                    $this->setWarning("MSG916", $this->SEMESTER[$i]."学期と".
                                                $this->SEMESTER["$j"]."学期の日付の前後関係が正しくありません。");
                    return false;
                }
                
                //日付が対象年度内かチェック
                if (strtotime($this->SDATE[$i]) < strtotime($this->year."/04/01")
                    || strtotime($this->EDATE[$i]) > strtotime(((int)$this->year+1)."/03/31")) {
                    $this->setWarning("MSG916", "学期コード ".$this->SEMESTER[$i]." が対象年度内の日付ではありません。");
                    return false;
                }
            }
        }

        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $maxsemester=(int)$this->semescount-1;   //max学期

            for ($i = 1; $i <= $this->semescount; $i++) {
                $this->semester = $this->SEMESTER[$i];
                $this->semestername = $this->SEMESTERNAME[$i];

                if ($this->semester == "9") {
                    $this->sdate = $this->SDATE["1"];
                    $this->edate = $this->EDATE[$maxsemester];
                } else {
                    $this->sdate = $this->SDATE[$i];
                    $this->edate = $this->EDATE[$i];
                }

                knjz030Query::getUpdateQuery($this);
            }

            $this->setMessage("MSG201", "登録された年度、学期は再ログイン時に有効になります。");
        }
    }
}
