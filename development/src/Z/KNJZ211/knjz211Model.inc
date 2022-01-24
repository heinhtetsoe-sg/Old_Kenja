<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjz211Model extends Model {
    var $field       = array();
    var $cmd;
    var $sepa;
    var $level;
    var $staffcd;   //セキュリティー用スタッフコード
    var $sec_competence;                                    //権限保持
    var $courseMajor;
    var $courseCode;

    /**
     * メンバ変数の初期化
     */
    function knjz211Model()
    {
        $this->sepa = "";
        $this->level = "";
    }
    function init()
    {               
        //セッション変数に保存
        $this->sepa = VARS::request("sepa");
        $this->courseMajor = VARS::post("COURSEMAJOR");
        $this->courseCode = VARS::post("COURSECODE");

        if( VARS::post("MODIFY") ){
            $this->level = VARS::post("MODIFY");
        }else{
            $this->level = VARS::request("ASSESSLEVELCNT");
        }

        //初期化
        $this->field = array();
        $cnt = $this->level;

        for ($i = 1; $i <= $cnt; $i++) {
            $this->field["ASSESSMARK".$i] =  VARS::post("ASSESSMARK".$i);
            if ($i != 1) {
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
        //権限チェック
        if($this->sec_competence != DEF_UPDATABLE){
            $this->setWarning("MSG300");
            return false;
        }
        //必須チェック
        if (!strlen($this->sepa)) {
           $this->setWarning("MSG301","評定区分");
           return false;
        }
        $sepaMax = $this->sepa == '4' ? 5 : 100;
        //テキストエリア入力チェック
        $ii = $this->level;
        for ($i = 1; $i <= $ii; $i++) {
            if (strlen($this->field["ASSESSMARK".$i]) > 6) {
                $this->setWarning("MSG914","(評定記号は6バイトまでです)");
                return false;
            }
            if ($i != 1) {
                $ASSESSLOW  = "ASSESSLOW".($i);
                //必須チェック
                if (!strlen($this->field[$ASSESSLOW])) {
                   $this->setWarning("MSG301","下限");
                   return false;
                }
                //数字チェック
                if (!is_numeric($this->field[$ASSESSLOW]) && !$this->field[$ASSESSLOW] == 0) {
                    $this->setWarning("MSG907","下限");
                    return false;
                }
                //下限、上限チェック
                if ($this->field[$ASSESSLOW] < 0) {
                   $this->setWarning("MSG913");
                   return false;
                } else if ($this->field[$ASSESSLOW] > $sepaMax) {
                   $this->setWarning("MSG913","下限、上限は{$sepaMax}までです。");
                   return false;
                }
                //値大小チェック
                if ($i >= 2) {
                    if ($i > 2) {
                       if ($this->field["ASSESSLOW2"] < 1) {
                           $this->setWarning("MSG913");
                           return false;
                       }
                    }
                    $ASSESSLOW_UPPER  = "ASSESSLOW".($i-1);
                    if (!($this->field[$ASSESSLOW_UPPER] < $this->field[$ASSESSLOW])) {
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
            $result = $this->update();
            if ($result) {
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
        $result = knjz211Query::getInsertQuery($this);
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
