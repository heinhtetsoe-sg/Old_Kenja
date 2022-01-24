<?php

require_once('for_php7.php');

class knjl082uModel extends model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;

    function knjl082uModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->ObjYear          = CTRL_YEAR + 1;
        $this->applicantdiv     = "1"; //1:中学

        $this->auto_no_flg      = VARS::post("AUTO_NO_FLG"); //1:自動割振りする

        $this->tmp_schregno     = VARS::post("SCHREGNO");    //配列
        $this->hid_examno       = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {}

    function validateInput() {
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->schregno = array();

        //学籍番号を再配置
        $tmp = explode(",", $this->hid_examno);
        foreach ($tmp as $key => $examno) {
            if ($this->auto_no_flg == '1') {
                //全て未入力 ===> 自動割振りする
                $renban = $key + 1;
                $this->schregno[$examno] = $this->ObjYear . sprintf("%04d", $renban);
            } else {
                //全て入力済 ===> 手入力した値で登録する
                $this->schregno[$examno] = $this->tmp_schregno[$key];
            }
        }

        if ($this->validateInput()) {
            //更新
            $result = knjl082uQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        }
    }
}
?>
