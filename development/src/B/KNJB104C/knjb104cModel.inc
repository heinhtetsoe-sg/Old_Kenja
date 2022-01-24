<?php

require_once('for_php7.php');

class knjb104cModel extends Model
{
    public $cmd;
    public $gyou;
    public $retu;
    public $grade;
    public $kousa;
    public $executeDate;
    public $periodCd;
    public $hrgradeclass;
    public $exp_year;
    public $exp_semester;
    public $exp_grade;
    public $exp_hrclass;
    public $exp_executedate;
    public $exp_faccd;
    public $exp_chaircd;
    public $exp_facname;
    public $exp_chairname;
    public $exp_subclassname;
    public $exp_subclassnameDisp;
    public $exp_hrname;
    public $exp_hrnameDisp;
    public $exp_periodcd;
    public $exp_testkindcd;
    public $exp_testitemcd;
    public $setData;

    public function knjb104cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
            $this->gyou = 7;
            $this->retu = 7;
            unset($this->grade);
            unset($this->kousa);
            unset($this->executeDate);
            unset($this->periodCd);
            unset($this->hrgradeclass);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->exp_grade);
            unset($this->exp_hrclass);
            unset($this->exp_executedate);
            unset($this->exp_faccd);
            unset($this->exp_chaircd);
            unset($this->exp_facname);
            unset($this->exp_chairname);
            unset($this->exp_subclassname);
            unset($this->exp_subclassnameDisp);
            unset($this->exp_hrname);
            unset($this->exp_hrnameDisp);
            unset($this->exp_periodcd);
            unset($this->exp_testkindcd);
            unset($this->exp_testitemcd);
            unset($this->setData);
        } else {
            if (VARS::request("cmd") == 'list') {
                $this->semester = VARS::request("SEMESTER");
                $this->grade = VARS::request("GRADE");
                $this->kousa = VARS::request("KOUSA");
                $this->executeDate = VARS::request("EXECUTEDATE");
                $this->periodCd = VARS::request("PERIODCD");
                $this->hrgradeclass = VARS::request("GRADE_HR_CLASS");
            }
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::request("exp_year") != "") {
            $this->exp_year = VARS::request("exp_year");
            $this->exp_semester = VARS::request("exp_semester");
            $this->exp_grade = VARS::request("exp_grade");
            $this->exp_hrclass = VARS::request("exp_hrclass");
            $this->exp_executedate = VARS::request("exp_executedate");
            $this->exp_faccd = VARS::request("exp_faccd");
            $this->exp_chaircd = VARS::request("exp_chaircd");
            $this->exp_facname = VARS::request("exp_facname");
            $this->exp_chairname = VARS::request("exp_chairname");
            $this->exp_subclassname = VARS::request("exp_subclassname");
            $this->exp_subclassnameDisp = VARS::request("exp_subclassname");
            $this->exp_hrname = VARS::request("exp_hrname");
            $this->exp_hrnameDisp = str_replace('<br>', '&nbsp;&nbsp;&nbsp;', VARS::request("exp_hrname"));
            $this->exp_periodcd = VARS::request("exp_periodcd");
            $this->exp_testkindcd = VARS::request("exp_testkindcd");
            $this->exp_testitemcd = VARS::request("exp_testitemcd");
            if ($this->cmd != 'update') {
                $this->gyou = 7;
                $this->retu = 7;
            }
            $hrNameList = explode('<br>', $this->exp_hrname);
            list($this->hrgradeclass, $damy) = explode(':', $hrNameList[0]);
        }

        $this->setData = array();
        if (VARS::request("setData") != "") {
            $this->setData = explode(',', VARS::request("setData"));
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjb104cQuery::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
