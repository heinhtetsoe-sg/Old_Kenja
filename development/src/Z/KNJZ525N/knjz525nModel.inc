<?php

require_once('for_php7.php');

class knjz525nModel extends Model {
    var $field = array();
    var $cmd;

    function knjz525nModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
        }

        $this->field = array("MAX_DATA_DIV"     =>  VARS::post("MAX_DATA_DIV"),
                             "setcnt"           =>  VARS::post("setcnt"),
                             "WINDOW_PTRN"      =>  VARS::post("WINDOW_PTRN"),
                             "STATUS_NAME"      =>  VARS::post("STATUS_NAME"),
                             "GROWUP_NAME"      =>  VARS::post("GROWUP_NAME"),
                            );

        $this->max_item_num = 20;

        for ($i = 1; $i <= $this->max_item_num; $i++) {
            $this->field["DATA_DIV_NAME".$i]   = VARS::post("DATA_DIV_NAME".$i);
        }

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "update") {
            //テキストエリア入力チェック
            $header_array = array("STATUS_NAME", "GROWUP_NAME");
            foreach ($header_array as $key) {
                if (strlen($this->field[$key]) > 90 ) {
                    $this->setWarning("MSG914","全角30文字までです。(横項目)");
                    return false;
                }
            }
            $item_array = array();
            for ($i = 1; $i <= $this->max_item_num; $i++) $item_array[] = "DATA_DIV_NAME".$i;
            foreach ($item_array as $index => $key) {
                if (strlen($this->field[$key]) > 150 ) {
                    $itemnum = $index + 1;
                    $this->setWarning("MSG914","全角50文字までです。(項目{$itemnum})");
                    return false;
                }
            }

        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $cnt_pre    = $db->getOne(knjz525nQuery::getChallengedAssessmentStatusGrowupDat($this, CTRL_YEAR-1, "cnt"));
            $cnt_this   = $db->getOne(knjz525nQuery::getChallengedAssessmentStatusGrowupDat($this, CTRL_YEAR, "cnt"));
            Query::dbCheckIn($db);

            //存在チェック（前年度）
            if ($cnt_pre == 0) {
                $this->setWarning("MSG303","( 前年度 )");
                return false;
            }

            //存在チェック（今年度）
            if ($cnt_this > 0) {
                $this->setWarning("MSG203","今年度にデータが存在します。");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz525nQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        knjz525nQuery::getDeleteQuery($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            knjz525nQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
