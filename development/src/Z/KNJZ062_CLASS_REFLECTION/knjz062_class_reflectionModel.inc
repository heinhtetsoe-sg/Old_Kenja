<?php

require_once('for_php7.php');

//定数定義

class knjz062_class_reflectionModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $schoolcd;  //学校コード

    //メンバ変数の初期化
    function knjz062_class_reflectionModel()
    {
    }
    function init() {   
        //プロパティファイル
        $this->getPropertiesAll();
        
        $this->field = array("BOARD_SCHOOL_KIND"  =>  VARS::post("BOARD_SCHOOL_KIND")  //教育委員会学校種別
                             );
        
        
        $this->selectdata        = VARS::post("selectdata");
        $this->cmd               = VARS::request("cmd");
        if(!$this->auth) {
            $this->auth = VARS::get("SEND_AUTH");
        }
        //V_SCHOOL_MSTから学校コードを取得
        $rtnRow = knjz062_class_reflectionQuery::getSchoolMst();
        $this->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];
        
    }
    //入力項目チェック
    function validateInput()
    {
        return true;
    }
    
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz062_class_reflectionQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
