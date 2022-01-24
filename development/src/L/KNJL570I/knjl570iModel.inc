<?php
class knjl570iModel extends model
{
    public $cmd;
    public $entexamyear;
    public $applicantdiv;
    public $majorcd;
    public $majorcdList;
    public $getDefTokutai;

    public function knjl570iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->entexamyear);
            unset($this->applicantdiv);
        }

        $this->cmd = VARS::request("cmd");
        if (VARS::request("year") != "") {
            $this->entexamyear = VARS::request("year");
        }
        $this->entexamyear = ($this->entexamyear == "") ? CTRL_YEAR + 1 : $this->entexamyear;
        $this->applicantdiv = "2"; // '2' 固定
        $this->majorcd = VARS::post("MAJORCD") != "" ? VARS::post("MAJORCD") : "1";
        $this->getDefTokutai = VARS::post("GET_DEFTOKUTAI");

        $this->majorcdList   = array();
        $this->majorcdList[] = array("label" => "1:普通科", "value" => "1");
        $this->majorcdList[] = array("label" => "2:工業科", "value" => "2");

        $this->field = array("MAJORCD"        =>  VARS::post("MAJORCD"),          //学科
                             "TESTDIV"        =>  VARS::post("TESTDIV"),          //入試区分
                             "MAJOR_COURCECD" =>  VARS::post("MAJOR_COURCECD"),   //コース
                             "RECEPTNO"       =>  VARS::post("RECEPTNO")          //受験番号
                             );
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();

        $hid_testdiv = VARS::post("HID_TESTDIV");
        $hid_receptno = VARS::post("HID_RECEPTNO");
        $receptnoArray = explode(",", $hid_receptno);
        $errFlg = false;
        foreach ($receptnoArray as $key => $receptno) {
            $this->updField[$receptno]["JUDGE_COURCE"]    = VARS::post("JUDGE_COURCE-{$receptno}");
            $this->updField[$receptno]["HONORDIV"]        = VARS::post("HONORDIV-{$receptno}");
            $this->updField[$receptno]["HONOR_REASONDIV"] = VARS::post("HONOR_REASONDIV-{$receptno}");

            //特待コードと特待理由は「両方セット」or「両方null」の状態のみ取り得る
            if ($this->updField[$receptno]["HONORDIV"] xor $this->updField[$receptno]["HONOR_REASONDIV"]) {
                $errItemMsg = ($this->updField[$receptno]["HONORDIV"] == "") ? "特待コード" : "特待理由コード";
                $this->setWarning("MSG301", "({$errItemMsg})\\n\\n受験番号 : {$receptno}");
                $errFlg = true;
            }
        }

        if (!$errFlg) {
            //更新
            knjl570iQuery::getUpdateQuery($this, $hid_testdiv);
            $this->setMessage("MSG201");
        }
    }
}
