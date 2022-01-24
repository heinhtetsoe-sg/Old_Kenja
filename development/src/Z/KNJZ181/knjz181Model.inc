<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}
class knjz181Model extends Model {
        var $field       = array();
        var $year;
        var $copyYear;
        var $grade;
        var $mockSubclassCd;
        var $cmd;
        var $level;

    /**
     * メンバ変数の初期化
     */
    function knjz181Model() {
        $this->sepa = "";
        $this->level = "";
    }

    function init() {

        $this->cmd              = VARS::request("cmd");
        $this->year             = VARS::post("YEAR");
        $this->copyYear         = VARS::post("COPY_YEAR");
        $this->grade            = VARS::post("GRADE");
        $this->mockSubclassCd   = VARS::post("MOCK_SUBCLASS_CD");

        if ($this->cmd == "changeYear") {
            $this->copyYear         = "";
            $this->grade            = "";
            $this->mockSubclassCd   = "";
        }

        $this->level = VARS::post("ASSESSLEVELCNT");

        //初期化
        $this->field = array();

        $this->field["ASSESSHIGH1"] =  VARS::post("ASSESSHIGH1");

        for ($i = 1; $i <= $this->level; $i++) {
            $this->field["ASSESSMARK".$i] = VARS::post("ASSESSMARK".$i);
            $this->field["ASSESSLOW".$i]  = VARS::post("ASSESSLOW".$i);
        }

    }

    //入力項目チェック
    function validateInput()
    {

        if ($this->field["ASSESSHIGH1"] ) {
        }

        if (!isset($this->field["ASSESSHIGH1"])) {
            $this->setWarning("MSG301","上限");
            return false;
        }

        if ($this->field["ASSESSHIGH1"] == "") {
            $this->setWarning("MSG301","上限");
            return false;
        }

        //数字かどうか？
        if(!is_numeric($this->field["ASSESSHIGH1"]) && !$this->field["ASSESSHIGH1"] == 0){
            $this->setWarning("MSG907","上限");
            return false;
        }

        //テキストエリア入力チェック
        for ($i = 1; $i <= $ii; $i++) {
            if (strlen($this->field["ASSESSMARK".$i]) > 6) {
                $this->setWarning("MSG914","(評定記号は6バイトまでです)");
                return false;  
            }

            $ASSESSLOW  = "ASSESSLOW".$i;

            if (!isset($this->field[$ASSESSLOW])) {
                $this->setWarning("MSG301","下限");
                return false;
            }

            if ($this->field[$ASSESSLOW] == "") {
                $this->setWarning("MSG301","下限");
                return false;
            }

            //数字かどうか？
            if(!is_numeric($this->field[$ASSESSLOW]) && !$this->field[$ASSESSLOW] == 0){
                $this->setWarning("MSG907","下限");
                return false;
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
            $this->update();
            $this->setMessage("MSG201");
            return true;
        }
        return false;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz181Query::getInsertQuery($this);
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &getCopyModel()
    {
        $result = knjz181Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return true;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
