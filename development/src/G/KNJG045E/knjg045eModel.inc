<?php

require_once('for_php7.php');

class knjg045eModel extends Model {

    var $field  = array();  //画面データ格納
    var $cmd;               //コマンド
    var $diaryDate;         //日付
    var $control;           //コントロールマスタ
    var $data;              //チェックエラー時用のデータ

    function knjg045eModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->diaryDate  = VARS::post("DIARY_DATE");
        if ($this->diaryDate == "") $this->diaryDate  = str_replace("-","/",CTRL_DATE);
        $this->field = array("SCHKIND"      => VARS::post("SCHKIND"),
                             "WEATHER"      => VARS::post("WEATHER"),
                             "WEATHER2"     => VARS::post("WEATHER2"),
                             "TEMPERATURE"  => VARS::post("TEMPERATURE"),
                             "REMARK"       => VARS::post("REMARK"),
                             "DATA_MAX_CNT" => VARS::post("DATA_MAX_CNT")
                            );

        for ($i = 0; $i < $this->field["DATA_MAX_CNT"]; $i++) {
            $this->data[$i]["REMARK1"] = VARS::post("REMARK1".$i);
        }

        //講座存在チェック
        $this->existsData = true;
        if ($this->cmd == "main") {
            //DB接続
            $db = Query::dbCheckOut();
            $query = knjg045eQuery::selectQuery($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row) $this->setWarning("講座がありません");
            $this->existsData = false;

            //DB切断
            Query::dbCheckIn($db);
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //数値、桁チェック
        if ($this->field["TEMPERATURE"] != "" && !preg_match('/^(-?)([1-9]\d?|0)(\.\d)?$/', $this->field["TEMPERATURE"])) {
            $this->setWarning("MSG901", "(気温は-99.9～99.9の範囲の半角数字で入力して下さい)");
            return false;
        }

        for ($i = 0; $i < $this->field["DATA_MAX_CNT"]; $i++) {
            //文字数チェック
            if ($this->validate_row_cnt($this->data[$i]["REMARK1"], (40 * 2)) > 2) {
                $this->setWarning("MSG915","(授業コメントは2行までです)");
                return false;
            }
        }
        //文字数チェック
        if ($this->validate_row_cnt($this->field["REMARK"], (100 * 2)) > 5) {
            $this->setWarning("MSG915","(今日の出来事は5行までです)");
            return false;
        }

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
            if ($this->cmd == "update") {
                if ($this->validateInput()) {
                    knjg045eQuery::getDeleteQuery($this);
                    knjg045eQuery::getInsertQuery($this);

                }
            }
            $this->setMessage("MSG201");
    }
}
?>
