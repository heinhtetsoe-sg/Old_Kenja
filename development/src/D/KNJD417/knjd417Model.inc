<?php

require_once('for_php7.php');

class knjd417Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjd417Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "" || VARS::request("cmd") == "clear") {
            unset($this->compositiontype);
            unset($this->compositionname);
            unset($this->compositionname_inflg);
            unset($this->compcnt);
            unset($this->semester);
            unset($this->gakubu_school_kind);
            unset($this->condition);
            unset($this->guidance_pattern);
            $this->field = array();
        }

        if (VARS::request("cmd") != "clear") {
            if (VARS::post("COMPOSITION_TYPE") != "") {
                $this->compositiontype = VARS::post("COMPOSITION_TYPE");
            }
            $typechk = explode("-", $this->compositiontype);

            $this->compositionname = VARS::post("COMPOSITION_NAME");
            if (VARS::post("COMPOSITIONNAME_FLG") !== "" && VARS::post("COMPOSITIONNAME_FLG") !== null) {
                $this->compositionname_inflg = VARS::post("COMPOSITIONNAME_FLG");
            }
            if (VARS::post("COMPCNT") != ""){
                $this->compcnt = VARS::post("COMPCNT");
            } else if (get_count($typechk[0]) > 0 && $typechk[1] == "2") {
                $this->compcnt = 3;
            }

            if ($this->compcnt > 0) {
                $this->field = array();
                for ($i = 1; $i <= $this->compcnt; $i++) {
                    $this->field["ASSESSLEVEL_INFLG_".$i] =  VARS::post("ASSESSLEVEL_INFLG_".$i);
                    $this->field["ASSESSLEVEL_".$i] =  VARS::post("ASSESSLEVEL_".$i);
                    $this->field["ASSESSMARK_".$i] =  VARS::post("ASSESSMARK_".$i);
                }
            } else {
                $this->field = array();
            }
        } else {
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        if ($this->Properties["useGradeKindCompGroupSemester"] == "1") {
            if (VARS::post("SEMESTER") != "") {
                $this->semester = VARS::request("SEMESTER");
            }
        } else {
            $this->semester = "9";
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

        if ($this->guidance_pattern) {
            $this->field = array();
            if ($this->pattern) {
                foreach ($this->pattern as $key => $val) {
                    $this->field["ITEM_REMARK".$key] =  VARS::post("ITEM_REMARK".$key);
                }
            }
        }
    }

    //入力項目チェック
    function validateInput() {

        if ($this->cmd == "update") {
            //必須チェック
            if ($this->compositionname == "") {
                $this->setWarning("MSG304", "　　　（ 構成項目名 ）");
                return false;
            }

            $typechk = explode("-", $this->compositiontype);

            //画面下部を表示する場合にチェック
            if ($typechk[1] !== "" && $typechk[1] != '3') {
                if ($this->compcnt == "") {
                    $this->setWarning("MSG304", "　　　（ 項目数 ）");
                    return false;
                }
            }

            //テキストエリア入力チェック
            for ($i = 1; $i <= $this->compcnt; $i++) {
                if (strlen($this->field["ASSESSLEVEL_".$i]) > 30 ) {
                    $this->setWarning("MSG914","(登録文字列は30バイトまでです)");
                    return false;
                }
            }
            if ($typechk[1] == '3') {
                $cnt = 1;
                $remark = "";
                foreach ($this->pattern as $key => $val) {
                    if (strlen($this->field["ITEM_REMARK".$key]) > 30 ) {
                        $this->setWarning("MSG914","(項目".$cnt."は30バイトまでです)");
                        return false;
                    }
                    $remark .= $this->field["ITEM_REMARK".$key];
                    $cnt++;
                }
                if (strlen($remark) == 0 ) {
                    $this->setWarning("MSG301","値を入力してください。");
                    return false;
                }
            }
        } else if ($this->cmd == "delete") {
            //必須チェック
            if ($this->compositionname == "") {
                $this->setWarning("MSG304", "　　　（ 構成項目名 ）");
                return false;
            }
        } else if ($this->cmd == "copy") {
            $db = Query::dbCheckOut();
            $cnt_preHdat    = $db->getOne(knjd417Query::chkCopyData(CTRL_YEAR-1, "check", "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            $cnt_pre        = $db->getOne(knjd417Query::chkCopyData(CTRL_YEAR-1, "check", "HREPORT_GUIDANCE_KIND_NAME_DAT"));
            $cnt_thisHdat   = $db->getOne(knjd417Query::chkCopyData(CTRL_YEAR,   "check", "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            $cnt_this       = $db->getOne(knjd417Query::chkCopyData(CTRL_YEAR,   "check", "HREPORT_GUIDANCE_KIND_NAME_DAT"));
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
            knjd417Query::getInsertQuery($this);
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
            knjd417Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            //前年度のデータ取得
            $result = $db->query(knjd417Query::chkCopyData(CTRL_YEAR-1, "list", "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjd417Query::getCopyQuery($row, "HREPORT_GUIDANCE_KIND_NAME_HDAT"));
            }
            $result = $db->query(knjd417Query::chkCopyData(CTRL_YEAR-1, "list", "HREPORT_GUIDANCE_KIND_NAME_DAT"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー
                $db->query(knjd417Query::getCopyQuery($row, "HREPORT_GUIDANCE_KIND_NAME_DAT"));
            }
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
