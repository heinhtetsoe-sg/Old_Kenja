<?php

require_once('for_php7.php');


class knjb3043Model extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $control;
    var $field = array();

    function knjb3043Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->compatible();
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
            $this->auth = AUTHORITY;
            $this->sendPrg = "";
        }

        $this->cmd = VARS::request("cmd");

        $this->field['SCH_DIV'] = VARS::post('SCH_DIV') ? VARS::post('SCH_DIV') : $this->field['SCH_DIV'];
        $this->field['PRECHAIRSEQ'] = strlen(VARS::post('PRECHAIRSEQ')) > 0 ? VARS::post('PRECHAIRSEQ') : $this->field['PRECHAIRSEQ'];
        $this->preChairSeq = strlen(VARS::post('PRECHAIRSEQ')) > 0 ? VARS::post('PRECHAIRSEQ') : $this->field['PRECHAIRSEQ'];

        $this->field['PRESEQ'] = strlen(VARS::post('PRESEQ')) > 0 ? VARS::post('PRESEQ') : $this->field['PRESEQ'];
        $this->preSeq = strlen(VARS::post('PRESEQ')) > 0 ? VARS::post('PRESEQ') : $this->field['PRESEQ'];

        $this->field['PTRN_UPDATE_TITLE'] = VARS::post('ptrnUpdate_TITLE') ? VARS::post('ptrnUpdate_TITLE') : $this->field['PTRN_UPDATE_TITLE'];
        $this->field['PTRN_UPDATE_RADIO'] = VARS::post('PTRN_UPDATE_RADIO') ? VARS::post('PTRN_UPDATE_RADIO') : $this->field['PTRN_UPDATE_RADIO'];

        $this->updateDataList = VARS::post("updateDataList");
        if (isset($this->updateDataList)) {
            $this->updateDataList = json_decode($this->updateDataList, true);
        }

        $this->courseCd = VARS::request("COURSECD");
        if($this->cmd == 'editSchDiv'
        || $this->cmd == 'edit'
        || $this->cmd == 'editCmb'
        || $this->cmd == 'update'){
            unset($this->courseCd);
        }

        $this->leftMenu = VARS::request("LEFT_MENU");
        $this->yearSeme = VARS::request("YEAR_SEME");

        if (VARS::request("PROGRAMID") != "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        } else if($this->yearSeme){
            $list = explode('-', $this->yearSeme);
            $this->year = $list[0];
            $this->semester = $list[1];
        }

        if (VARS::get("SEND_PRG")) {
            $this->auth = VARS::get("SEND_AUTH");
            $this->sendPrg = VARS::get("SEND_PRG");
        }

        // 表示
        $this->visibleLine = VARS::post("visibleLine");
        if ($this->cmd == '') {
            unset($this->visibleLine);
        }

        // AJAXパラメータ
        $this->ajaxParam = VARS::request("AJAX_PARAM");
        if (isset($this->ajaxParam)) {
            $this->ajaxParam = json_decode($this->ajaxParam, true);
        }

        // 単位マスタ読込みフラグ
        $this->readCreditsMstFlg = VARS::post("readCreditsMstFlg");

        // 科目展開表読込みフラグ
        $this->readPreHdatFlg = VARS::post("readPreHdatFlg");

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

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (empty($this->updateDataList)) {
            $this->setMessage("MSG201");
            return;
        }

        // 表示している展開表の種類で処理振分け
        if ($this->field['SCH_DIV'] == '1') {
            // 科目展開表
            $this->preSeq = knjb3043Query::updatePtrnPreTemplate($db, $this);
            knjb3043Query::updatePtrnPre($db, $this);
        } else {
            // // 講座展開表
            $this->preChairSeq = knjb3043Query::updatePtrnPreChairTemplate($db, $this);
            knjb3043Query::updatePtrnPreChair($db, $this);
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }

    // 科目展開表 テンプレート削除
    function &deletePreSeqModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->field['SCH_DIV'] == '1') {
            knjb3043Query::deletePtrnPreTemplate($db, $this);
        } else {
            knjb3043Query::deletePtrnPreChairTemplate($db, $this);
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
?>
