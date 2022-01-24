<?php

require_once('for_php7.php');

class knje360aModel extends Model {
    var $fields = array();
    var $cmd;
    var $schoolcd;
    var $seq_list;
    var $data_select;
    var $select = array();
    var $replaceItem;

    function knje360aModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->data_select);
            unset($this->schoolcd);
            unset($this->seq_list);
            unset($this->replaceValue);
            unset($this->search_div);
            unset($this->KeepSort);
            unset($this->sort);
            unset($this->asc_or_desc);
        }

        $this->cmd          = VARS::request("cmd");
        $this->data_select  = VARS::post("DATA_SELECT");
        $this->schoolcd     = VARS::post("SCHOOLCD");
        $this->seq_list     = VARS::post("SEQ_LIST");
        $this->replaceValue = VARS::post("REP_VALUE");
        $this->search_div   = VARS::post("SEARCH_DIV");
        if (VARS::get("sort")) {
            $this->sort         = VARS::get("sort");
        }

        if (VARS::get("SEND_DATA_SELECT")) {
            $this->data_select  = VARS::get("SEND_DATA_SELECT");
        }

        if (VARS::get("SCH_CD")) {
            $this->schoolcd     = VARS::get("SCH_CD");
        }

        if (VARS::get("SEQ_LIST")) {
            $this->seq_list     = VARS::get("SEQ_LIST");
        }

        if (VARS::get("REP_ITEM")) {
            $this->replaceItem      = VARS::get("REP_ITEM");
            $this->replaceItemLabel = VARS::get("REP_ITEM_LABEL");
        }

        //初期化
        if ($this->cmd == "select") {
            unset($this->select);
        }
        //学校選択
        if ($this->cmd == "select_search") {
            $this->select["check_all"] = VARS::post("CHECKALL");
            $this->select["data_chk"]  = VARS::request("CHECKED");

            $this->select["field"] = array();
            $this->select["field"]["SEARCH_TXT"] = VARS::post("SEARCH_TXT");
            $this->select["field"]["SEARCH_NO"]  = VARS::post("SEARCH_NO");

            //学校コードを半角スペースで分割
            $this->search_no = array();
            $this->search_no = explode(' ', $this->select["field"]["SEARCH_NO"]);
        }

        //初期化
        $this->fields = array();

        //更新
        if ($this->cmd == "update") {
            $seq_array = preg_split("/,/", $this->seq_list);

            foreach ($seq_array as $key) {
                $this->fields["DECISION"][$key]     =  VARS::post("DECISION"."-".$key);
                $this->fields["PLANSTAT"][$key]     =  VARS::post("PLANSTAT"."-".$key);
                $this->fields["EXAMNO"][$key]       =  VARS::post("EXAMNO"."-".$key);
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        if (!$this->schoolcd) {
            $this->setWarning("MSG304");
            return false;
        }
        if (!$this->seq_list) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knje360aQuery::getUpdateQuery($this, $this->fields);
            $this->setMessage("MSG201");
        }
    }
}
?>
