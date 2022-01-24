<?php

require_once('for_php7.php');

class knjz525mModel extends Model
{
    public $field = array();
    public $cmd;

    public function knjz525mModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
        }

        $this->field = array("MAX_DATA_DIV"     =>  VARS::post("MAX_DATA_DIV"),
                             "SETCNT"           =>  VARS::post("SETCNT"),
                             "SHEET_PATTERN"    =>  VARS::post("SHEET_PATTERN"),
                             "STATUS_NAME"      =>  VARS::post("STATUS_NAME"),
                             "GROWUP_NAME"      =>  VARS::post("GROWUP_NAME"),
                            );

        // 最大項目数
        $this->max_item_num = 12;
        //項目数
        if (!$this->field["MAX_DATA_DIV"]) {
            $this->field["MAX_DATA_DIV"] = 0;
        }
        for ($i = 1; $i <= $this->field["MAX_DATA_DIV"]; $i++) {
            $this->field["DATA_DIV_NAME".$i]   = VARS::post("DATA_DIV_NAME".$i);
        }
        // コメント
        $this->field["COMMENTS"] = VARS::post("COMMENTS");

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->cmd == "update") {
            //テキストエリア入力チェック
            $header_array = array("STATUS_NAME", "GROWUP_NAME");
            foreach ($header_array as $key) {
                if (strlen($this->field[$key]) > 36) {
                    $this->setWarning("MSG914", "全角12文字までです。(横項目)");
                    return false;
                }
            }
            $item_array = array();
            for ($i = 1; $i <= $this->max_item_num; $i++) {
                $item_array[] = "DATA_DIV_NAME".$i;
            }
            foreach ($item_array as $index => $key) {
                if (strlen($this->field[$key]) > 90) {
                    $itemnum = $index + 1;
                    $this->setWarning("MSG914", "全角30文字までです。(項目{$itemnum})");
                    return false;
                }
            }
            //コメント入力チェック
            $header_array = array("COMMENTS");
            $comentSize = 45;
            $comentRow = 5;
            $comentByte = ($comentSize * 2);
            foreach ($header_array as $key) {
                if (strlen($this->field[$key]) > 685) {
                    $this->setWarning("MSG914", "全角{$comentSize}文字{$comentRow}行までです。(コメント)");
                    return false;
                }
                if ($this->validate_row_cnt($this->field[$key], $comentByte) > $comentRow) {
                    $this->setWarning("MSG914", "全角{$comentSize}文字{$comentRow}行までです。(コメント)");
                    return false;
                }
            }
        } elseif ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $cnt_pre    = $db->getOne(knjz525mQuery::getChallengedStatussheetItemNameDat(CTRL_YEAR-1, "count"));
            $cnt_this   = $db->getOne(knjz525mQuery::getChallengedStatussheetItemNameDat(CTRL_YEAR, "count"));
            Query::dbCheckIn($db);

            //存在チェック（前年度）
            if ($cnt_pre == 0) {
                $this->setWarning("MSG303", "( 前年度 )");
                return false;
            }

            //存在チェック（今年度）
            if ($cnt_this > 0) {
                $this->setWarning("MSG203", "今年度にデータが存在します。");
                return false;
            }
        }

        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjz525mQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除
    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        knjz525mQuery::getDeleteQuery($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        return;
    }

    //コピー処理
    public function &getCopyModel()
    {
        if ($this->validateInput()) {
            knjz525mQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
