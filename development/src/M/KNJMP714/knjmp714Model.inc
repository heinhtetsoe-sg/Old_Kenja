<?php

require_once('for_php7.php');


class knjmp714Model extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $hr_class;
    var $data_cnt;
    var $control;
    var $z010name1;

    function knjmp714Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("SEND_SCHREGNO")) {
            $this->sendSchregNo = VARS::get("SEND_SCHREGNO");
            $this->sendSlipNo   = VARS::get("SEND_SLIP_NO");
        } else {
            $this->year = VARS::post("YEAR");
            $this->semester = VARS::post("SEMESTER");
            $this->dataDiv = VARS::post("DATADIV");
            $this->seikyuuDisp = VARS::post("SEIKYUU_DISP");
            $this->hr_class = VARS::post("HR_CLASS");

            if (VARS::request("PROGRAMID") != "") {
                $this->year = CTRL_YEAR;
                $this->semester = CTRL_SEMESTER;
            }
            unset($this->sendSchregNo);
            unset($this->sendSlipNo);
        }

        $this->cmd   = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            //DB接続
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            foreach ($this->setList as $counter => $Row) {
                $setKey = $Row["SCHREGNO"].":".$Row["SLIP_NO"];
                $updCheck = VARS::post("UPDATE_CHK_{$setKey}");
                if ($updCheck == "") {
                    continue;
                }
                $groupCd = VARS::post("GROPCD_{$setKey}");
                $jugyouLM = VARS::post("JUGYOU_{$setKey}");
                $collectCnt = VARS::post("COLLECT_CNT_{$setKey}") ? VARS::post("COLLECT_CNT_{$setKey}") : 0;

                knjmp714Query::getInsertQuery($db, $this, $Row["SCHREGNO"], $Row["SLIP_NO"], $groupCd, $jugyouLM, $collectCnt);
            } //foreach
            $this->setMessage("MSG201");

            $db->commit();
            //DB切断
            Query::dbCheckIn($db);
        }
    }

}
?>
