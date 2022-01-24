<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl030fModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    public function knjl030fModel()
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

        $this->cmd = VARS::request("cmd");
    }

    //更新処理
    public function &getUpdateModel()
    {
        //1:中学
        if ($this->field["APPLICANTDIV"] == "1") {
            knjl030fQuery::getUpdateJ($this);
        }
        //2:高校
        if ($this->field["APPLICANTDIV"] == "2") {
            knjl030fQuery::getUpdateH($this);
        }
        $this->setMessage("MSG201");
        return;
    }
}
