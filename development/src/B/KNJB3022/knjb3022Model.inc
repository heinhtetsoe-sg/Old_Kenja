<?php

require_once('for_php7.php');


class knjb3022Model extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $control;
    var $field = array();

    function knjb3022Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->compatible();
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
        }

        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        if (VARS::request("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        $this->staffCd = VARS::post('STAFFCD');
        $this->staffClass = VARS::post('STAFF_CLASS');
        $this->bscSeq = strlen(VARS::post('BSCSEQ')) > 0 ? VARS::post('BSCSEQ') : $this->bscSeq;
        $this->field['PTRN_UPDATE_TITLE'] = VARS::post('ptrnUpdate_TITLE') ? VARS::post('ptrnUpdate_TITLE') : $this->field['PTRN_UPDATE_TITLE'];
        $this->field['PTRN_UPDATE_RADIO'] = VARS::post('PTRN_UPDATE_RADIO') ? VARS::post('PTRN_UPDATE_RADIO') : $this->field['PTRN_UPDATE_RADIO'];

        $this->field['GRADE_SELECTED'] = VARS::request("GRADE_SELECTED");
        $this->field['GRAND_HR_CLASSCD_SELECTED'] = VARS::request("GRAND_HR_CLASSCD_SELECTED");
        $this->field['GRAND_HR_CLASSCD'] = VARS::request("GRAND_HR_CLASSCD");
        $this->field['CLASSCD'] = VARS::post('CLASSCD');
        $this->field['SCHOOL_KIND'] = VARS::post('CLASSCD');
        
        $this->operationRadio = VARS::post("OPERATION_RADIO");

        $this->updateAddData = VARS::request("updateAddData");
        if (isset($this->updateAddData)) {
            $this->updateAddData = json_decode($this->updateAddData, true);
        }
        $this->updateDelData = VARS::request("updateDelData");
        if (isset($this->updateDelData)) {
            $this->updateDelData = json_decode($this->updateDelData, true);
        }

        if($this->cmd == 'update'){
            unset($this->grandHrClassCd);
        }

        $this->ajaxParam = VARS::request("AJAX_PARAM");
        if (isset($this->ajaxParam)) {
            $this->ajaxParam = json_decode($this->ajaxParam, true);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function compatible() {
        if(!function_exists("json_encode")) {
            function json_encode($param) {
                require_once("JSON.php");
                $json = new Services_JSON();
                return $json->encode($param);
            }
            function json_decode($param, $assoc = false) {
                require_once("JSON.php");
                $json = new Services_JSON($assoc ? SERVICES_JSON_LOOSE_TYPE : 0);
                return $json->decode($param);
            }
        } 
    }

    function fusemozi($staffCd){
        list($simo, $fuseji) = explode(" | ", $this->Properties["showMaskStaffCd"]);
        $ume = "" ;
        for ($umecnt = 1; $umecnt <= strlen($staffCd) - (int)$simo; $umecnt++) {
            $ume .= $fuseji;
        }
        if ($fuseji) {
            return $ume.substr($staffCd, (strlen($staffCd) - (int)$simo), (int)$simo);
        } else {
            return $staffCd;
        }
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // ヘッダレコード編集
        $bscSeq = knjb3022Query::updatePtrnTemplate($db, $this);
        $this->bscSeq = $bscSeq;

        if (!empty($this->updateAddData) || !empty($this->updateDelData)) {
            knjb3022Query::update($db, $this);
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

}
?>
