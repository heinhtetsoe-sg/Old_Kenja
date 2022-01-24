<?php

require_once('for_php7.php');

class knjb1218Model extends Model {
    var $field = array();
    var $cmd;
    var $schregno;
    var $search_div;
    var $name;
    var $info;
    var $checked;

    function knjb1218Model() {
        $this->field = array();
    }

    function init() {
        if(VARS::get("PROGRAMID")) {
           unset($this->schregno);
           unset($this->name);
           unset($this->search_div);
        }

        $this->cmd      = VARS::request("cmd");
        $this->checked  = VARS::request("CHECKED");     //教科書チェック

        $this->div1_num_cnt     = VARS::request("DIV1_NUM_CNT");
        $this->div2_num_cnt     = VARS::request("DIV2_NUM_CNT");
        $this->div3_num_cnt     = VARS::request("DIV3_NUM_CNT");
        $this->total_num_cnt    = VARS::request("TOTAL_NUM_CNT");

        $this->div1_price_cnt   = VARS::request("DIV1_PRICE_CNT");
        $this->div2_price_cnt   = VARS::request("DIV2_PRICE_CNT");
        $this->div3_price_cnt   = VARS::request("DIV3_PRICE_CNT");
        $this->total_price_cnt  = VARS::request("TOTAL_PRICE_CNT");

        if (VARS::get("SCHREGNO")) {
            $this->schregno     = VARS::get("SCHREGNO");
            $this->search_div   = VARS::get("SEARCH_DIV");
            $this->name         = VARS::get("NAME");
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力チェック
    function validateInput() {
        //同一教科書の有無チェック
        $textbook_array = array();
        $array = (is_array($this->checked)) ? explode(",", implode(',',$this->checked)) : "";
        if ($array) {
            for ($i = 0; $i < get_count($array); $i++){
                list($classcd, $school_kind, $curriculum_cd, $subclasscd, $textbookcd) = explode('-', $array[$i]);

                if (in_array($textbookcd, $textbook_array)) {
                    $this->setWarning("MSG302","同一の教科書が選択されています。");
                    $this->cmd = "main";
                    return false;
                }
                $textbook_array[] = $textbookcd;
            }
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //更新処理
    function update() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjb1218Query::getUpdateQuery($db, $this);
        knjb1218Query::updateTextChkFin($db, $this);

        Query::dbCheckIn($db);
    }
}
?>
