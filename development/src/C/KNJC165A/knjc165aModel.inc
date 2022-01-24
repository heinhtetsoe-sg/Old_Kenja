<?php

require_once('for_php7.php');

class knjc165aModel extends Model {

    var $field = array();
    var $monthArray = array();
    var $cmd;

    //備考
    var $remark_moji;
    var $remark_gyou;

    /**
     * メンバ変数の初期化
     */
    function knjc165aModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        
        $this->getPropertiesAll(); //プロパティファイル
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "MONTH"            =>  VARS::post("MONTH"),            //対象月
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "REMARK"           =>  VARS::post("REMARK"),           //備考
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");
        $this->getFieldSize();

        $this->cmd     = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //必須入力チェック
        if ($this->field["YEAR"] == "") {
            $this->setWarning("MSG304", "( 年度 )");
            return false;
        }
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }
        if ($this->field["MONTH"] == "") {
            $this->setWarning("MSG304", "( 対象月 )");
            return false;
        }
        if ($this->field["GRADE_HR_CLASS"] == "") {
            $this->setWarning("MSG304", "( 年組 )");
            return false;
        }

        //桁数チェック
        if (strlen($this->field["REMARK"]) > 90) {
            $this->setWarning("MSG915","( 備考は90バイトまでです )");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjc165aQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
    
    function getFieldSize() {
        //備考
        $this->remark_moji = 15; //デフォルトの値
        $this->remark_gyou = 1; //デフォルトの値
    }
}
//var_dump($_REQUEST);
?>
