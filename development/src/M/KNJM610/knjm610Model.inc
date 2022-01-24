<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm610Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjm610Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"          =>  VARS::post("YEAR"),        //年度
                             "MONEY"         =>  VARS::post("MONEY")        //受講料
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->update();
        $this->setMessage("MSG201");
    }

    // 編集完了用のモデルを返します
    function &getDeleteModel()
    {
        $this->delete();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        $result = knjm610Query::getDeleteQuery($this);
        $result = knjm610Query::getUpdateQuery($this);
        return $result;
    }

    function &delete()
    {
        $result = knjm610Query::getDeleteQuery($this);
        return $result;
    }

}
?>
