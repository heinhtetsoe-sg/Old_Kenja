<?php

require_once('for_php7.php');

class knjd417mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjd417mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "" || VARS::request("cmd") == "clear") {
            unset($this->KindNo);
            unset($this->kindName);
            unset($this->kindNameFlg);
            unset($this->kindCnt);
            unset($this->semester);
            unset($this->gakubu_school_kind);
            unset($this->condition);
            unset($this->guidance_pattern);
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");
        $this->field = array();

        if (VARS::request("cmd") != "clear") {
            if (VARS::post("KIND_NO") != "") {
                $this->KindNo = VARS::post("KIND_NO");
            }
            $this->kindType = VARS::post("KIND_TYPE");
            $this->kindName = VARS::post("KIND_NAME");
            if (VARS::post("KIND_NAME_FLG") != "") {
                $this->kindNameFlg = VARS::post("KIND_NAME_FLG");
            }
            $this->kindCnt = VARS::post("HID_KIND_CNT");
            if ($this->kindCnt > 0) {
                $this->field = array();
                for ($i = 1; $i <= $this->kindCnt; $i++) {
                    $this->field["KIND_REMARK_".$i] =  VARS::post("KIND_REMARK_".$i);
                }
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->semester = "9";
        if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
            if (VARS::post("SEMESTER") != "") {
                $this->semester = VARS::post("SEMESTER");
            }
        }
        if (VARS::post("GAKUBU_SCHOOL_KIND") != "") {
            $this->gakubu_school_kind = VARS::post("GAKUBU_SCHOOL_KIND");
        }
        if (VARS::post("CONDITION") != "") {
            $this->condition = VARS::post("CONDITION");
        }
        if (VARS::post("GUIDANCE_PATTERN") != "") {
            $this->guidance_pattern = VARS::post("GUIDANCE_PATTERN");
        }

        $this->pattern = array();
        $this->pattern["1"] = array("COLNUM_NAME" => "ITEM_REMARK1", "LABEL" => "項目名（左）");
        $this->pattern["2"] = array("COLNUM_NAME" => "ITEM_REMARK2", "LABEL" => "項目名（中央）");
        $this->pattern["3"] = array("COLNUM_NAME" => "ITEM_REMARK3", "LABEL" => "項目名（右）");
        if ($this->guidance_pattern) {
            foreach ($this->pattern as $key => $val) {
                $this->field["ITEM_REMARK".$key] =  VARS::post("ITEM_REMARK".$key);
            }
        }
    }

    //入力項目チェック
    function validateInput() {

        if ($this->cmd == "update") {
            //必須チェック
            if ($this->kindName == "") {
                $this->setWarning("MSG304", "　　　（ ブロック名 ）");
                return false;
            }
            //構成名入力
            if (strlen($this->kindName) > 45 ) {
                $this->setWarning("MSG914","(ブロック名は全角15文字までです)");
                return false;
            }

            //項目数を表示する場合にチェック
            if ($this->kindType == "1") {
                if ($this->kindCnt == "") {
                    $this->setWarning("MSG301", "　　　（ 目標数 ）");
                    return false;
                }
            }
            //テキストエリア入力チェック
            for ($i = 1; $i <= $this->kindCnt; $i++) {
                if (strlen($this->field["KIND_REMARK_".$i]) > 30 ) {
                    $this->setWarning("MSG914","(登録文字列は全角10文字までです)");
                    return false;
                }
            }
            if ($this->kindType == "3") {
                $remark = "";
                foreach ($this->pattern as $key => $val) {
                    if (strlen($this->field[$val["COLNUM_NAME"]]) > 45 ) {
                        $this->setWarning("MSG914","(".$val["LABEL"]."は全角15文字までです)");
                        return false;
                    }
                    $remark .= $this->field[$val["COLNUM_NAME"]];
                }
                if (strlen($remark) == 0 ) {
                    $this->setWarning("MSG301","値を入力してください。");
                    return false;
                }
            }
        } else if ($this->cmd == "delete") {
            //必須チェック
            if ($this->kindName == "") {
                $this->setWarning("MSG304", "　　　（ 構成項目名 ）");
                return false;
            }
        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $cnt_preHdat    = $db->getOne(knjd417mQuery::chkCopyData(CTRL_YEAR-1, "check", "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            $cnt_pre        = $db->getOne(knjd417mQuery::chkCopyData(CTRL_YEAR-1, "check", "HREPORT_GUIDANCE_KIND_NAME_DAT"));
            $cnt_thisHdat   = $db->getOne(knjd417mQuery::chkCopyData(CTRL_YEAR,   "check", "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            $cnt_this       = $db->getOne(knjd417mQuery::chkCopyData(CTRL_YEAR,   "check", "HREPORT_GUIDANCE_KIND_NAME_DAT"));
            Query::dbCheckIn($db);

            //存在チェック（前年度）
            if ($cnt_pre == 0 && $cnt_preHdat == 0) {
                $this->setWarning("MSG917","前年度のデータが存在しません。");
                return false;
            }

            //存在チェック（今年度）
            if ($cnt_this > 0 || $cnt_thisHdat > 0) {
                $this->setWarning("MSG917","今年度にデータが存在します。");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd417mQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->setCmd("main");
        } else {
            $this->setCmd("check");
        }
        return;
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd417mQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $lastYear = CTRL_YEAR - 1;

            $result = $db->query(knjd417mQuery::chkCopyData($lastYear, "list", "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjd417mQuery::getCopyQuery($row, "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            }
            $result = $db->query(knjd417mQuery::chkCopyData($lastYear, "list", "HREPORT_GUIDANCE_KIND_NAME_DAT"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjd417mQuery::getCopyQuery($row, "HREPORT_GUIDANCE_KIND_NAME_DAT"));
            }

            $db->query(knjd417mQuery::getDeleteItemNameQuery("HREPORT_GUIDANCE_ITEM_NAME_DAT"));
            $result = $db->query(knjd417mQuery::chkCopyData($lastYear, "list", "HREPORT_GUIDANCE_ITEM_NAME_DAT"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjd417mQuery::getCopyItemNameQuery($row, "HREPORT_GUIDANCE_ITEM_NAME_DAT"));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
