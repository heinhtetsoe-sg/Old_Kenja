<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje452Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knje452Model()
    {
    }

    function init() {
        $this->assessDiv = VARS::post("ASSESS_DIV");
        $this->assessCnt["02"] = 10;
        $this->assessCnt["03"] = 5;
        $this->assessCnt["04"] = 10;

        for ($i = 1; $i <= $this->assessCnt[$this->assessDiv]; $i++) {
            $this->field["QUESTION".$i] = VARS::post("QUESTION".$i);
        }

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $pre_year = CTRL_YEAR - 1;
            $preYear_cnt = $db->getOne(knje452Query::getCopyData($pre_year, "cnt"));
            //前年度データチェック
            if ($preYear_cnt == 0) {
                $this->setWarning("MSG203", "前年度データがありません。");
                return false;  
            }
            Query::dbCheckIn($db);
        } else {
            //テキストエリア入力チェック
            for ($i = 1; $i <= $this->assessCnt[$this->assessDiv]; $i++) {
                if (strlen($this->field["QUESTION".$i]) > 90) {
                    $this->setWarning("MSG914","(設問内容は90バイトまでです：設問{$i})");
                    return false;  
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
            $this->update();
            $this->setMessage("MSG201");
        }
        return false;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        knje452Query::getInsertQuery($this);
    }

    //コピー用モデルを返します
    function &getCopyModel() {
        if ($this->validateInput()) {
            knje452Query::getCopyQuery();
            $this->setMessage("MSG201");
        }
        return false;
    }
}
?>
