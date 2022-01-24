<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj091_2Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjj091_2Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {               
        //プロパティファイル
        $this->getPropertiesAll();

        $this->field = array("SCHKIND"          =>  VARS::post("SCHKIND"),          //校種
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "SCHREGNO"         =>  VARS::post("selectdata"),       //対象生徒
                             "COMMITTEECD"      =>  VARS::post("COMMITTEECD"),      //委員会
                             "COMMITTEE_FLG"    =>  VARS::post("COMMITTEE_FLG"),    //委員会区分
                             "EXECUTIVECD"      =>  VARS::post("EXECUTIVECD")       //役職
                            );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //必須項目チェック
        if(!$this->field["COMMITTEE_FLG"] || !$this->field["COMMITTEECD"] || $this->field["SEMESTER"] == ''){
            $this->setWarning("MSG301","　　（学期または委員会区分または委員会）");
            return false;
        }
        return true;
    }

    //更新
    function &getInsertModel()
    {
        if ($this->validateInput()) {
            $cnt = knjj091_2Query::InsertQuery($this);

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
