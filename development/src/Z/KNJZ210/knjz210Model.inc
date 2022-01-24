<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}
class knjz210Model extends Model {
        var $field       = array();
        var $low_stock   = array();
        var $high_stock  = array();
        var $cmd;
        var $sepa;
        var $level;
        var $staffcd;   //セキュリティー用スタッフコード
        var $sec_competence;                                    //権限保持

    /**
     * メンバ変数の初期化
     */
    function knjz210Model()
    {
        $this->sepa = "";
        $this->level = "";
    }
    function init()
    {               
        //セッション変数に保存
        $this->sepa = VARS::request("sepa");

        if( VARS::post("MODIFY") ){
            $this->level = VARS::post("MODIFY");
        }else{
            $this->level = VARS::request("ASSESSLEVELCNT");
        }

        //初期化
        $this->field = array();
        $cnt = $this->level;

        if($this->sepa == 1){
        
            $this->field["ASSESSHIGH".$cnt] =  VARS::post("ASSESSHIGH".$cnt);
                
        }
        
        for($i=1;$i<=$cnt;$i++){
            $this->field["ASSESSMARK".$i] =  VARS::post("ASSESSMARK".$i);
            if($this->sepa != 2 || $i != 1){
                $this->field["ASSESSLOW".$i] =  VARS::post("ASSESSLOW".$i);
            }       
        }
        $this->cmd     = VARS::request("cmd");

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = AUTHORITY;
    }

    //入力項目チェック
    function validateInput()
    {
        $chk[] = array();

        //権限チェック
        if($this->sec_competence != DEF_UPDATABLE){
            $this->setWarning("MSG300");
            return false;
        }

        //評定区分チェック
/*              if ($this->sepa == 1 || $this->sepa == 3){
                $ii = 5;
        }else if ($this->sepa == 2){
                $ii = 10;
        }else{
                $this->setWarning("MB0104");
                return false;
        } */
        $ii = $this->level;

        //テキストエリア入力チェック
        for($i=1; $i<=$ii; $i++)
        {
            if (strlen($this->field["ASSESSMARK".$i]) >6 ) {
                $this->setWarning("MSG914","(評定記号は6バイトまでです)");
            return false;  
        }
            if($this->sepa != 2 || $i != 1){
                $ASSESSLOW  = "ASSESSLOW".($i);

                if(!isset($this->field[$ASSESSLOW])){
                   $this->setWarning("MSG301","下限");
                   return false;
                }
                //空かどうか？
                if($this->field[$ASSESSLOW] == ""){
                
                   $this->setWarning("MSG301","下限");
                   return false;
                }

                if($this->sepa == 4){
                //数字かどうか？
                   if(!is_numeric($this->field[$ASSESSLOW]) && !$this->field[$ASSESSLOW] == 0 && !$this->field[$ASSESSLOW] == "."){
                  
                       $this->setWarning("MSG907","下限");
                       return false;
                   }
                }else{
                    //数字かどうか？
                   if(!is_numeric($this->field[$ASSESSLOW]) && !$this->field[$ASSESSLOW] == 0){
                       $this->setWarning("MSG907","下限");
                       return false;
                   }
                }

                // 0 or 1 or 100 かどうか？
                if($this->field[$ASSESSLOW] < 0){
                   $this->setWarning("MSG913");
                   return false;
                } else if($this->field[$ASSESSLOW] >= 100 && ($this->sepa != 1 && $this->sepa != 2)) {
                   $this->setWarning("MSG913","下限、上限は100を超えてはいけません。");
                   return false;
                } else if($this->field[$ASSESSLOW] > 100 && $this->sepa == 2) {
                   $this->setWarning("MSG913","下限、上限は100までです。");
                   return false;
                }

                $chk[] = $this->field[$ASSESSLOW];
                //値大小チェック
                if(($this->sepa == 2 && $i >= 2) || ($this->sepa != 2 && $i >= 1)){
                    if($i > 2 && $this->sepa != 4){
                       if($this->field["ASSESSLOW2"] < 1){
                           $this->setWarning("MSG913");
                           return false;
                       }
                    }elseif($i > 2 && $this->sepa == 4){
                        if($this->field["ASSESSLOW2"] <= 0){
                           $this->setWarning("MSG913");
                           return false;
                       }
                    }
                    $ASSESSLOW_UPPER  = "ASSESSLOW".($i-1);

                    if(!($this->field[$ASSESSLOW_UPPER] < $this->field[$ASSESSLOW])){
                        $this->setWarning("MSG913");
                        return false;
                    }
                }
                    
            }
            
            if($i == $ii && $this->sepa == 1){
                if($this->field["ASSESSLOW".$i] > $this->field["ASSESSHIGH".$i]){
                    $this->setWarning("MSG913");
                    return false;
                }
    /*            if($this->field["ASSESSHIGH".$i] > 100){
                    $this->setWarning("MSG913","下限、上限は100を超えてはいけません。");
                    return false;
                } */
            }
            
            $levelcnt = knjz210Query::getLevel();
            if($i == $ii){
                if($this->sepa == 3){
                    if($this->field["ASSESSLOW".$i] > $levelcnt[0]){
                        $this->setWarning("MSG913");
                        return false;
                    }
                }
                if($this->sepa == 4){
                    if($this->field["ASSESSLOW".$i] > $levelcnt[1]){
                        $this->setWarning("MSG913");
                        return false;
                    }
                }
            }
        }
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
                if ( !isset($model->warning)){
                    
                    /**
                     * 期末評定の段階数と
                       学年末評定の段階数を変える時、
                       学年末評定の上限と帳票評定の上限も変わって再度調整のメッセージを出す
                     */
                    if($overBig==3){
                        $this->setMessage("MSG201","学年末評定の上限は変わりました、学年末評定を再度調整してください");
                    }elseif($overBig==4){
                        $this->setMessage("MSG201","帳票評定の上限は変わりました、帳票評定を再度調整してください");
                    }else{
                        $this->setMessage("MSG201");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz210Query::getInsertQuery($this);
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
