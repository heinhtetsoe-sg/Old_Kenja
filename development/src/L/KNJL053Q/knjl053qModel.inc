<?php

require_once('for_php7.php');

class knjl053qModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examno;
    public $ObjYear;
    public $field = array();
    public $setField = array();

    public function knjl053qModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->nameMstTest = SCHOOLKIND == "J" ? "L024" : "L004";

        $this->field = array("SHOWDIV"                  =>  VARS::post("SHOWDIV"),                  //表示内容
                             "KIJUN_TEST_DIV"           =>  VARS::post("KIJUN_TEST_DIV")            //基準テスト
                             );

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->hid_receptno   = VARS::post("HID_RECEPTNO");

        $this->tmpField = array();
        if (get_count($this->subclassList) > 0) {
            foreach ($this->subclassList as $subclassCd => $subclassName) {
                $scoreName1 = "SCORE1_".$subclassCd;
                $scoreName2 = "SCORE2_".$subclassCd;
                $this->tmpField[$scoreName1] = VARS::post($scoreName1); //配列
                $this->tmpField[$scoreName2] = VARS::post($scoreName2); //配列
            }
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->setField = array();

        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            foreach ($this->subclassList as $subclassCd => $subclassName) {
                $scoreName1 = "SCORE1_".$subclassCd;
                $scoreName2 = "SCORE2_".$subclassCd;
                $this->setField[$receptno][$scoreName1] = $this->tmpField[$scoreName1][$key];
                $this->setField[$receptno][$scoreName2] = $this->tmpField[$scoreName2][$key];
            }
        }
        $result = knjl053qQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
            $this->messageFlg = 1;
        }
    }
}
?>
