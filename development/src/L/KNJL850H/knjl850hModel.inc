<?php
class knjl850hModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;
    var $print;

    function knjl850hModel(){}

    function init() {
        //$this->getPropertiesAll();
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"          =>  VARS::post("APPLICANTDIV"),          //学校種別
                             "TESTDIV"               =>  VARS::post("TESTDIV"),               //入試区分
                             "OUTPUT"                =>  VARS::post("OUTPUT"),                //帳票種類
                             "NUMBER"                =>  VARS::post("NUMBER"),                //No.
                             "YOTEIKIKAN"            =>  VARS::post("YOTEIKIKAN"),            //連絡予定期間
                             "YOTEIBI"               =>  VARS::post("YOTEIBI"),               //不合格通知予定日
                             "DENWABANGO"            =>  VARS::post("DENWABANGO"),            //連絡電話番号
                            );

    }
    
    
    //入力項目チェック
    function validateInput() {
    
    $checkStrLen["NUMBER"]         = array("LEN" => 4,   "MESAGE" => "No.");
    $checkStrLen["YOTEIKIKAN"]     = array("LEN" => 80,  "MESAGE" => "連絡予定期間");
    $checkStrLen["YOTEIBI"]        = array("LEN" => 20,  "MESAGE" => "不合格通知予定日");
    $checkStrLen["DENWABANGO"]     = array("LEN" => 30,  "MESAGE" => "連絡電話番号");
    foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
    return true;
    }
    
    
    function &getCheckModel() {
      
      if($this->validateInput()){
        $this->print = on;
      }
    
      return;
    }
}
?>
