<?php

require_once('for_php7.php');

class knjz210jModel extends Model {
        var $field       = array();
        var $low_stock   = array();
        var $high_stock  = array();
        var $cmd;
        var $sepa;
        var $level;

    function knjz210jModel() {}

    function init() {
        $this->cmd  = VARS::request("cmd");
        
        if ($this->cmd == 'update') {
            $this->updData = array();
            $setScore= 100;
            for ($scoreCnt=1; $scoreCnt < 102; $scoreCnt++) {
                $this->updData[$scoreCnt]["SCORE"] = VARS::post("SCORE".$setScore);
                $dataCnt = 1;
                foreach ($this->getTitle as $key => $val) {
                    $this->updData[$scoreCnt]["JVIEW".$dataCnt] = VARS::post("JVIEW".$dataCnt."_".$setScore);
                    $dataCnt++;
                }
                $setScore--;
            }
        }
    }

    /* 入力項目チェック */
    function validateInput() {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $this->setWarning("MSG300");
            return false;
        }
        return true;
    }

    /* 更新 */
    function &getUpdateModel() {

        //入力チェック
        if ($this->validateInput()) {
            knjz210jQuery::getInsertQuery($this);
        }
        $this->setMessage("MSG201");
        return false;
    }

    /* コピー */
    function &getCopyModel() {

        knjz210jQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return false;
    }

    /* 削除 */
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjz210jQuery::getDeleteQuery();
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
        return false;
    }

    //変数をクリアします
    function clean() {}
}
?>
