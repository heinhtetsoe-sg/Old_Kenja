<?php

require_once('for_php7.php');

class knjl014rModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $control;       //コントロールマスタ

    public function knjl014rModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->year = CTRL_YEAR + 1;

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"      =>  VARS::post("TESTDIV"),
                             "BEFORE_PAGE"  =>  VARS::post("BEFORE_PAGE"),
                            );

        $examno_array = explode(',', $this->data["EXAMNO"]);
        foreach ($examno_array as $key => $examno) {
            $this->data["MIJUKEN".$examno]     = VARS::post("MIJUKEN".$examno);
        }

        $this->cmd = VARS::request("cmd");
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {

        $db = Query::dbCheckOut();
        $examno_array = explode(',', $this->data["EXAMNO"]);
        foreach ($examno_array as $key => $examno) {
            //更新
            knjl014rQuery::getUpdateQuery($db, $this, $examno, $this->data["MIJUKEN".$examno]);
        }

        $this->cmd = "knjl014r";
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
}
