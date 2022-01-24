<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl060fModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $special_reason_div;

    /**
     * メンバ変数の初期化
     */
    public function knjl060fModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->year = CTRL_YEAR + 1;
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "TESTDIV0"     =>  VARS::post("TESTDIV0"),       //入試回数
                     );

        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->cmd = VARS::request("cmd");
    }

    public function validateInput()
    {
        $ret = knjl060fQuery::getDataCheck($this);
        if (!$ret) {
            $this->setMessage("MSG303", "\\n対象者がいません。または、得点入力が行われていません。");
            return false;
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            //1:中学
            if ($this->field["APPLICANTDIV"] == "1") {
                knjl060fQuery::getUpdateJ($this);
            }
            //2:高校
            if ($this->field["APPLICANTDIV"] == "2") {
                knjl060fQuery::getUpdateH($this);
            }
            $this->setMessage("MSG201");
        }
        return;
    }
}
