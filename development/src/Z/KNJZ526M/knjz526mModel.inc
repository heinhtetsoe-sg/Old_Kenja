<?php

require_once('for_php7.php');

class knjz526mModel extends Model {
    var $field = array();
    var $cmd;

    function knjz526mModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");

        $this->kindNo = VARS::post("KIND_NO");

        $this->field = array(
              "DUMMY"            => "DUMMY"
            , "DATA_CNT"         =>  VARS::post("DATA_CNT")
            , "SETCNT"           =>  VARS::post("SETCNT")
            , "MAX_DATA_CNT"     =>  VARS::post("MAX_DATA_CNT")
        );

        //横項目設定
        $this->pattern = array();
        $this->pattern["01"][] = array("COLNUM_NAME" => "STATUS_NAME1", "LABEL" => "項目名（左）");
        $this->pattern["01"][] = array("COLNUM_NAME" => "STATUS_NAME2", "LABEL" => "項目名（右）");

        $this->pattern["03"][] = array("COLNUM_NAME" => "STATUS_NAME1", "LABEL" => "項目名（左）");
        $this->pattern["03"][] = array("COLNUM_NAME" => "STATUS_NAME2", "LABEL" => "項目名（中央）");
        $this->pattern["03"][] = array("COLNUM_NAME" => "STATUS_NAME3", "LABEL" => "項目名（右）");

        $this->pattern["04"][] = array("COLNUM_NAME" => "STATUS_NAME1", "LABEL" => "横項目1");
        // 横項目値取得
        $statusList = $this->pattern[$this->kindNo];
        for ($i=0; $i < get_count($statusList); $i++) { 
            $colnum = $statusList[$i];
            $this->field[$colnum["COLNUM_NAME"]] = VARS::post($colnum["COLNUM_NAME"]);
        }

        //縦項目値取得
        for ($i = 1; $i <= $this->field["SETCNT"]; $i++) {
            $this->field["KIND_NAME".$i] = VARS::post("KIND_NAME".$i);
        }


    }

    //入力項目チェック
    function validateInput() {
        if ($this->cmd == "update") {
            // 横項目 入力チェック
            $statusList = $this->pattern[$this->kindNo];
            for ($i=0; $i < get_count($statusList); $i++) { 
                $colnum = $statusList[$i];
                if (strlen($this->field[$colnum["COLNUM_NAME"]]) > 36 ) {
                    $this->setWarning("MSG914", $colnum["LABEL"]."は全角12文字までです。");
                    return false;
                }
            }

            //縦項目 入力チェック
            for ($i = 1; $i <= $this->field["SETCNT"]; $i++) {
                if (strlen($this->field["KIND_NAME".$i]) > 36 ) {
                    $this->setWarning("MSG914", "縦項目".$i."は全角12文字までです。");
                    return false;
                }
            }

        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $preRow  = $db->getRow(knjz526mQuery::getChallengedSupportplanKindNameDat(CTRL_YEAR-1, ""));
            $thisRow = $db->getRow(knjz526mQuery::getChallengedSupportplanKindNameDat(CTRL_YEAR, ""));
            Query::dbCheckIn($db);

            //存在チェック（前年度）
            if (!$preRow) {
                $this->setWarning("MSG303", "前年度のデータが存在しません。");
                return false;
            }
            //存在チェック（今年度）
            if ($thisRow) {
                $this->setWarning("MSG203", "今年度にデータが存在します。");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz526mQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除
    function &getDeleteModel() {
        knjz526mQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            knjz526mQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
