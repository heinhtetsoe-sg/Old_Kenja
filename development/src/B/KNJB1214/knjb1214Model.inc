<?php

require_once('for_php7.php');

class knjb1214Model extends Model {
    var $field = array();
    var $cmd;
    var $schregno;
    var $search_div;
    var $name;
    var $info;
    var $year;
    var $checked;
    var $rirekiCode;

    function knjb1214Model() {
        $this->field = array();
    }

    function init() {

        if(VARS::get("PROGRAMID")){
           unset($this->schregno);
           unset($this->search_div);
           unset($this->name);
        }

        $this->cmd      = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->year     = VARS::request("YEAR_CMB");    //対象年度コンボボックス
        $this->checked  = VARS::request("CHECKED");     //教科書チェック
        $this->rirekiCode = VARS::post("RIREKI_CODE");

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

            if ($this->cmd == "updateNext") {
                $this->cmd = "next";
            } else if ($this->cmd == "updatePrev") {
                $this->cmd = "prev";
            } else {
                $this->cmd = "main";
            }

            $this->setMessage("MSG201");
        }
    }

    //更新処理
    function update() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjb1214Query::getUpdateQuery($db, $this);
        knjb1214Query::updateTextChkFin($db, $this);

        //学籍番号セット
        $query = knjb1214Query::getSchregNo($this);
        $setSchregNo = $db->getOne($query);
        $this->schregno = $setSchregNo;

        Query::dbCheckIn($db);
    }
}
?>
