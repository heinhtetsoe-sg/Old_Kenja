<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjz210hModel extends Model {
        var $field       = array();
        var $cmd;
        var $sepa;
        var $staffcd;   //セキュリティー用スタッフコード
        var $sec_competence;                                    //権限保持

    /**
     * メンバ変数の初期化
     */
    function knjz210hModel()
    {
        $this->sepa = "";
        $this->level = "";
    }
    function init()
    {               
        $this->sepa = VARS::request("sepa");

        //初期化
        $this->field = array();
        $cnt = ($this->sepa == 2) ? 5 : 10 ;

        for($i=1;$i<=$cnt;$i++){
            $this->field["RATE".$i] =  VARS::post("RATE".$i);
            $this->field["ASSESSLEVEL5".$i] =  VARS::post("ASSESSLEVEL5".$i);
        }
        $this->cmd     = VARS::request("cmd");

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = AUTHORITY;
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
        $result = knjz210hQuery::getInsertQuery($this);
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
