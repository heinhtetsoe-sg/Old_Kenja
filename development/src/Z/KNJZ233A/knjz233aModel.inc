<?php

require_once('for_php7.php');

class knjz233aModel extends Model {
    var $field      = array();
    var $leftField  = array();
    var $cmd;
    var $combined_subclasscd;
    var $flg;
    var $grade;
    var $coursecd;
    var $majorcd;
    var $coursecode;
    var $seq;
    var $send_flg;

    function knjz233aModel() {
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "list") {
            $this->leftField = array("FLG"          => VARS::post("FLG"),
                                     "GRADE_COURSE" => VARS::post("GRADE_COURSE")
                                     );
        }

        $this->field = array("SEQ"              =>  (VARS::post("SEQ")) ? sprintf('%02d', VARS::post("SEQ")) : "",
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),
                             "WEIGHTING"        =>  VARS::post("WEIGHTING")
                             );

        if (VARS::get("PROGRAMID")) {
            unset($this->send_flg);
            unset($this->combined_subclasscd);
            unset($this->flg);
            unset($this->grade);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->coursecode);
            unset($this->seq);
        }

        $temp = VARS::get("SEND_FLG");
        if (isset($temp)){
            $this->send_flg     = VARS::get("SEND_FLG");
            $this->combined_subclasscd = VARS::get("COMBINED_SUBCLASSCD_SEND");
            $this->flg          = VARS::get("FLG_SEND");

            list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", VARS::get("GRADE_COURSE_SEND"));
            $this->grade        = $grade;
            $this->coursecd     = $coursecd;
            $this->majorcd      = $majorcd;
            $this->coursecode   = $coursecode;

            $this->seq = VARS::get("SEQ_SEND");
        }

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $cnt = knjz233aQuery::getCopyCountQuery($this);
        if ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            $result = knjz233aQuery::getCopyQuery($this);
            $this->setMessage('MSG201','コピー件数：'.$cnt.'件');
        }
    }

    //入力項目チェック
    function validateInput() {

        //必須チェック
        if ($this->combined_subclasscd == "") {
            $this->setWarning("MSG309");
            return false;
        }
        if (trim($this->field["SEQ"]) == "") {
            $this->setWarning('MSG301', "(SEQ)");
            return false;
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->seq != $this->field["SEQ"]) {
                $this->setWarning('MSG308',"(SEQ)");
                return false;
            }
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz233aQuery::checkExists($this);
        $ret_val = $db->getOne($query);
        if (($this->cmd == "update" || $this->cmd == "delete") && !$ret_val) {
            $this->setWarning('MSG303');
            return false;
        } else if ($this->cmd == "insert" && $ret_val > 0) {
            $this->setWarning('MSG302',"(SEQ)");
            return false;
        }

        //重複チェック（読替元科目）
        if (empty($this->field["SUBCLASSCD"])){
            $this->setWarning("読替元科目がありません");
            return false;
        }
        $subclasscd = array();
        foreach ($this->field["SUBCLASSCD"] as $key => $val) {
            if ($this->field["WEIGHTING"][$key]) {
                $subclasscd[] = $val;
            }
        }
        $query = knjz233aQuery::checkDuplication($this, $subclasscd);
        $dup_cnt = $db->getOne($query);
        if ($this->cmd == "insert" && $dup_cnt > 0) {
            $this->setWarning('MSG302',"(同一パターンあり)");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        if ($this->validateInput()){
            knjz233aQuery::getInsertQuery($this);
            $this->setMessage('MSG201');
            $this->send_flg = "SEQ";
            $this->seq = $this->field["SEQ"];
        }
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()){
            knjz233aQuery::getDeleteQuery($this);
            knjz233aQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if ($this->validateInput()){
            knjz233aQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
            unset($this->send_flg);
            unset($this->combined_subclasscd);
            unset($this->seq);
            unset($this->field);
        }
    }
}
?>
