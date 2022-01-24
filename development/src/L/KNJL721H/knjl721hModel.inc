<?php

require_once('for_php7.php');

class knjl721hModel extends model
{
    public $cmd;
    public $examYear;
    public $applicantdiv;
    public $testdiv;
    public $s_examno;
    public $e_examno;
    // var $field = array();
    public $hid_examno;
    public $hid_kyokaseq;
    public $hid_5kyokaseq;
    public $hid_3kyokaseq;
    public $recordarry;

    public function knjl721hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->examYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->s_examno);
            unset($this->e_examno);
        }

        $this->cmd          = VARS::request("cmd");
        $this->examYear     = CTRL_YEAR + 1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->s_examno     = strlen(VARS::post("S_EXAMNO")) ? VARS::post("S_EXAMNO") : VARS::post("HID_S_EXAMNO");
        $this->e_examno     = strlen(VARS::post("E_EXAMNO")) ? VARS::post("E_EXAMNO") : VARS::post("HID_E_EXAMNO");
        $this->hid_examno   = VARS::post("HID_EXAMNO"); //"EXAMNO"-"COUNT"で取得
        $this->hid_kyokaseq  = VARS::post("HID_KYOKASEQ"); //教科コード配列
        $this->hid_5kyokaseq = VARS::post("HID_5KYOKASEQ"); //5教科コード配列
        $this->hid_3kyokaseq = VARS::post("HID_3KYOKASEQ"); //3教科コード配列
    }

    //受験番号をセット
    public function &getMainModel()
    {
        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            knjl721hQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //データチェック
        if (!$this->hid_examno) {
            $this->setWarning("MSG303");
            return false;
        }

        $this->recordarry = array();
        $examno_array = array();
        $tmp = explode(",", $this->hid_examno);
        $kyoka_array = (!$this->hid_kyokaseq) ? array() : explode(",", $this->hid_kyokaseq);
        $kyoka5_array = (!$this->hid_5kyokaseq) ? array() : explode(",", $this->hid_5kyokaseq);
        $kyoka3_array = (!$this->hid_3kyokaseq) ? array() : explode(",", $this->hid_3kyokaseq);
        foreach ($tmp as $key => $examno_count) {
            $examno_array = explode("-", $examno_count);
            $addwk = array();
            $total5 = 0;
            foreach ($kyoka5_array as $kyokaseq) {
                $total5 += (int)VARS::post("KYOKA_{$kyokaseq}-".$examno_array[1]);
            }
            $total3 = 0;
            foreach ($kyoka3_array as $kyokaseq) {
                $total3 += (int)VARS::post("KYOKA_{$kyokaseq}-".$examno_array[1]);
            }
            $total_all = 0;
            foreach ($kyoka_array as $kyokaseq) {
                $addwk["kyoka_{$kyokaseq}"] = VARS::post("KYOKA_{$kyokaseq}-".$examno_array[1]);
                $total_all += (int)VARS::post("KYOKA_{$kyokaseq}-".$examno_array[1]);
            }
            $addwk["total5"] = $total5;
            $addwk["total3"] = $total3;
            $addwk["total_all"] = $total_all;
            $addwk["kodo"] = VARS::post("KODO-".$examno_array[1]);
            $addwk["kesseki"] = VARS::post("KESSEKI-".$examno_array[1]);
            $addwk["remark"] = VARS::post("REMARK-".$examno_array[1]);
            $this->recordarry[$examno_array[1]] = $addwk;
        }

        return true;
    }

    public function clean()
    {
    }
}
