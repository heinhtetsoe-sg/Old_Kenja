<?php

require_once('for_php7.php');

class knjl327bModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl327bModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),              //入試区分
                             "TSUCHI_DATE"  =>  VARS::post("TSUCHI_DATE"),          //通知日付
                             "GOUKAKU"      =>  VARS::post("GOUKAKU"),              //1:正規合格、2:単願切換合格
                             "EARLY_PASSEXAMCOURSECD" => VARS::post("EARLY_PASSEXAMCOURSECD"),  //単願切換合格コース
                             "EARLY_S_DATE" =>  VARS::post("EARLY_S_DATE"),         //早期入学手続期間(自)
                             "EARLY_S_TIME" =>  VARS::post("EARLY_S_TIME"),         //早期入学手続時間(自)
                             "EARLY_E_DATE" =>  VARS::post("EARLY_E_DATE"),         //早期入学手続期間(至)
                             "EARLY_E_TIME" =>  VARS::post("EARLY_E_TIME"),         //早期入学手続時間(至)
                             "NOUNYU_DATE"  =>  VARS::post("NOUNYU_DATE"),          //納入期限
                             "FORM"         =>  VARS::post("FORM"),                 //帳票出力
                             "OUTPUT"       =>  VARS::post("OUTPUT"),               //（通知）出力範囲
                             "EXAMNO_S"     =>  VARS::post("EXAMNO_S"),             //（通知）開始受験番号
                             "EXAMNO_E"     =>  VARS::post("EXAMNO_E"),             //（通知）終了受験番号
                             "DBNAME"       =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
