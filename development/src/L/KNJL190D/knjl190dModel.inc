<?php

require_once('for_php7.php');

class knjl190dModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $testdiv;
    public $desirediv;
    public $noticeDate;
    public $transferDate1;

    public function knjl190dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = VARS::post("YEAR");
        $this->applicantdiv = "1";  //固定値

        $this->testdiv       = VARS::post("TESTDIV");
        $this->desirediv     = VARS::post("DESIREDIV");
        $this->noticeDate    = VARS::post("NOTICE_DATE");
        $this->transferDate1 = VARS::post("TRANSFER_DATE1");

        $this->field = array("SEND_TO"              => VARS::post("SEND_TO"),           //送付先
                             "FORM_KIND"            => VARS::post("FORM_KIND"),         //通知種別
                             "TENNYUGAKU_DATE"      => VARS::post("TENNYUGAKU_DATE"),   //転入学日
                             "TENSEKI_DATE"         => VARS::post("TENSEKI_DATE"),      //転籍許可日
                             "SPORT_CERTIF"         => VARS::post("SPORT_CERTIF"),      //日本スポーツ振興センター加入証明書
                             "MONTH"                => VARS::post("MONTH"),             //時候の挨拶(月)
                             "GREET"                => VARS::post("GREET")              //時候の挨拶
                            );
    }
}
