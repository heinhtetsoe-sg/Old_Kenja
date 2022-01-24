<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj090_2Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjj090_2Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {               
        //プロパティファイル
        $this->getPropertiesAll();

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "SCHREGNO"         =>  VARS::post("selectdata"),       //対象生徒
                             "COMMITTEECD"      =>  VARS::post("COMMITTEECD"),      //委員会
                             "CHARGENAME"       =>  VARS::post("CHARGENAME"),       //係り名
                             "EXECUTIVECD"      =>  VARS::post("EXECUTIVECD")       //役職
                            );

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //必須項目チェック
        if(!$this->field["COMMITTEECD"] && !$this->field["CHARGENAME"]){
            $this->setWarning("MSG301","　　（委員会または係り名）");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["CHARGENAME"]) >30 ) {
            $this->setWarning("MSG914","(係り名は30バイトまでです)");
            return false;  
        }
        return true;
    }

    //更新
    function &getInsertModel()
    {
        if ($this->validateInput()) {
            $cnt = knjj090_2Query::InsertQuery($this);

            if($cnt > 0){
                $total = get_count(explode(',', $this->field["SCHREGNO"]));
                $this->setMessage("MSG201","　　（".$cnt."件／".$total."件）");
            } else {
                $this->setMessage("MSG917","データが重複しています。");
            }
        }
        return;
    }
}
?>
