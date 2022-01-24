<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125eModel.inc 56581 2017-10-22 12:37:16Z maeshiro $

class knjd125eModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $creditflg;
    var $cmd;
    var $show_all;          //処理済表示フラグ
    var $repGetCredit;
    var $isJitu;
    var $absenceDiv;

    function knjd125eModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->show_all      = VARS::post("shw_flg");
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        $this->field["TESTITEM"] = VARS::post("TESTITEM");
        $this->field["CHAIRCD"] = VARS::post("CHAIRCD");

        $this->cmd     = VARS::request("cmd");
        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd125eQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];
        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd125eQuery::getSchoolMst();
        $this->isJitu = $rtnRow["JUGYOU_JISU_FLG"] == "2" ? true : false; // 1:法定授業、2:実授業
        $this->absenceDiv = "1"; // 1:年間、2:随時
    }

    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        $seme_kind = $this->field["SEMESTER"] .substr($this->field["TESTITEM"],0,2);
        $term = array("SCORE".$seme_kind."_1", "SCORE".$seme_kind."_2");
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "chk_box"."-".$i;
            $post_term = VARS::post($term_name);
            if (isset($post_term)) {
                $this->fields["chk_box"][]   = $post_term;
                $this->fields["SCHREGNO"][]             = $this->data["SCHREGNO"][$i];
                $this->fields["NOTICE_LATE"][]          = $this->data["NOTICE_LATE"][$i];
                $this->fields["COMBINED_CREDIT_SUM"][]  = $this->data["COMBINED_CREDIT_SUM"][$i];
                for ($j = 0; $j < get_count($term); $j++) {
                    $term_name = $term[$j]."-".$i;
                    $post_term = VARS::post($term_name);
                    if (isset($post_term)) {
                        $this->fields[$term[$j]][]   = $post_term;
                    }
                }
            }
        }
//var_dump($this->fields);
        knjd125eQuery::updateSuppExaDat($this);
        knjd125eQuery::updateRecordScoreDat($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
?>
