<?php

require_once('for_php7.php');

class knjl413Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjl413Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->field);
            unset($this->selectdata);
        }

        $this->field = array("EVENT_CLASS_CD"   =>  VARS::post("EVENT_CLASS_CD"),   //分類
                             "EVENT_CD"         =>  VARS::post("EVENT_CD"),         //イベント
                             "SEND_CD_PRGID"    =>  VARS::post("SEND_CD_PRGID"),    //送付名
                             "REPRINT"          =>  VARS::post("REPRINT"),      //再印刷
                             "SEND_DATE"        =>  VARS::post("SEND_DATE"),    //送付日付
                             "SEND_METHOD"      =>  VARS::post("SEND_METHOD"),  //送付方法
                             "PRINT_GUARDNAME"  =>  VARS::post("PRINT_GUARDNAME"),  //保護者氏名
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->field["SEND_CD_PRGID"]) == "") {
            $this->setWarning("MSG301","(送付名)");
            return false;
        }
        if (trim($this->field["SEND_DATE"]) == "") {
            $this->setWarning("MSG301","(送付日付)");
            return false;
        }

        return true;
    }

    //追加用のモデルを返します
    function &getInsertModel() {

        $selectdata = ($this->selectdata) ? explode(',', $this->selectdata) : array();

        //入力チェック
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $cnt = $db->getOne(knjl413Query::getRecruitSendHistDat($this->field, "cnt"));
            $max = $cnt + 1;
            Query::dbCheckIn($db);

            if ($this->field["REPRINT"]) {
                knjl413Query::getInsertSendHitsQuery($this->field, $this, $recruit_no, $max);
            }

            for ($i = 0; $i < get_count($selectdata); $i++) {

                if (!$this->field["REPRINT"]) {
                    continue;
                } else {
                    $this->insert($selectdata[$i], $max);
                }
            }
        }
        return;
    }

    //追加時の内部メソッド
    function &insert($recruit_no, $max) {
        knjl413Query::getInsertSendHitsStdQuery($this->field, $this, $recruit_no, $max);
        return;
    }
}
?>
