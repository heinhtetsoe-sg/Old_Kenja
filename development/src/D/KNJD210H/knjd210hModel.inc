<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd210hModel extends Model {
        var $field       = array();
        var $cmd;
        var $seme;
        var $grad;
        var $staffcd;   //セキュリティー用スタッフコード
        var $sec_competence;                                    //権限保持

    /**
     * メンバ変数の初期化
     */
    function knjd210hModel()
    {
        $this->sepa = "";
        $this->level = "";
    }
    function init()
    {               
        $this->seme = VARS::post("seme");
        $this->grad = VARS::post("grad");

        $this->cmd     = VARS::request("cmd");

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = AUTHORITY;
        $this->getPropertiesAll(); //プロパティファイル
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }
        
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $overBig = $this->update();
            if($overBig){
                $this->setMessage("MSG201");
                return true;
            }
        }
        return false;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjd210hQuery::getInsertQuery($this, 1);
        if ($this->seme == "2" && $this->grad == "03") 
            $result = knjd210hQuery::getInsertQuery($this, 2);
        return $result;
    }
    
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
