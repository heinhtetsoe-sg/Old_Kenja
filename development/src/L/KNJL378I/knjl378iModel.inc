<?php

class knjl378iModel extends Model
{
    public $schoolKind;
    public $cmd;
    public $field = array();
    public $print;

    /**
     * メンバ変数の初期化
     */
    public function knjl378iModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->schoolKind = VARS::get("URL_SCHOOLKIND");
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        
        $this->field = array(
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "DBNAME"           =>  VARS::post("DBNAME"),            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //入試制度
        if (VARS::post("APPLICANTDIV")) {
            $this->field["APPLICANTDIV"] = VARS::post("APPLICANTDIV");
        }
        //入試区分
        if (VARS::post("TESTDIV")) {
            $this->field["TESTDIV"] = VARS::post("TESTDIV");
        }
    }
    
    public function &getUpdateModel()
    {
        $this->examArr = knjl378iQuery::getApplicantBaseSelectQuery($this->field["APPLICANTDIV"], $this->field["TESTDIV"]);
        $capaTotalCnt = knjl378iQuery::getCapaTotalCnt($this->field["APPLICANTDIV"], $this->field["TESTDIV"]);

        if (count($this->examArr) > $capaTotalCnt) { //対象受験者全員に対して受験班を割り振れない場合エラー
            $this->setWarning("MSG203", "受験者数が受験班の設定人数を超えています。\\n受験班設定を確認してください。");
        } else {
            $tmp = knjl378iQuery::update($this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->examArr);
            $this->setMessage("MSG201");
            $this->print = $tmp;
        }
        return;
    }
}
