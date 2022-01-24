<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjz220uModel extends Model {
        var $field       = array();
        var $cmd;
        var $level;

    /**
     * メンバ変数の初期化
     */
    function knjz220uModel() {
        $this->level = "";
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->level = VARS::post("ASSESSLEVELCNT");

        //初期化
        $this->field = array();
        for ($i = 0; $i < $this->level; $i++) {
            $assesslebel = $this->level - $i;
            $this->field["ASSESSRATE".$assesslebel] = VARS::post("ASSESSRATE".$assesslebel);
        }

    }

    //入力項目チェック
    function validateInput() {
        if ($this->level == "") {
            $this->setWarning("MSG301","評定段階数");
            return false;
        }
        $check100 = "";
        for ($i = 0; $i < $this->level; $i++) {
            $assesslebel = $this->level - $i;
            //評定１以外のチェック
            if ($assesslebel != "1") {
                //空欄チェック
                if ($this->field["ASSESSRATE".$assesslebel] == "") {
                    $this->setWarning("MSG301","評定".$assesslebel."　基準値");
                    return false;
                }
                //ゼロ時
                if ($this->field["ASSESSRATE".$assesslebel] == "0"){
                    $this->setWarning("MSG901","評定".$assesslebel."　基準値");
                    return false;
                }
                //基準値合計
                $check100 += $this->field["ASSESSRATE".$assesslebel];
            }
        }
        //合計が100か
        if ($check100 != "100") {
            $this->setWarning("基準値の合計は100にして下さい\\n（評定１は除く）");
            return false;
        }

        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
            return true;
        }
        return false;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        $result = knjz220uQuery::getInsertQuery($this);
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &getCopyModel() {
        $result = knjz220uQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return true;
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
