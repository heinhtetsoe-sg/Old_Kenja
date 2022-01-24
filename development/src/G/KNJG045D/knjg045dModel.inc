<?php

require_once('for_php7.php');

class knjg045dModel extends Model {

    var $field  = array();  //画面データ格納
    var $cmd;               //コマンド
    var $diaryDate;         //日付
    var $control;           //コントロールマスタ

    function knjg045dModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->diaryDate  = VARS::post("DIARY_DATE");
        if (($this->cmd == 'shutcho' || $this->cmd == 'kyuka') && VARS::get("DIARY_DATE")) {
            $this->diaryDate  = VARS::get("DIARY_DATE");
        }
        if ($this->diaryDate == "") $this->diaryDate  = str_replace("-","/",CTRL_DATE);
        $this->field = array("WEATHER"          =>  VARS::post("WEATHER"),          //天気1
                             "WEATHER2"         =>  VARS::post("WEATHER2"),         //天気2
                             "NEWS"             =>  VARS::post("NEWS"),             //行事
                             "RAIKOU"           =>  VARS::post("RAIKOU"),           //来校者
                             "MATTER"           =>  VARS::post("MATTER"),           //用件
                             "REMARK"           =>  VARS::post("REMARK"),           //その他
                             "STAFFCD1"         =>  VARS::post("STAFFCD1"),         //出張者 コード
                             "STAFFNAME_SHOW1"  =>  VARS::post("STAFFNAME_SHOW1"),  //出張者 名称
                             "STAFFCD2"         =>  VARS::post("STAFFCD2"),         //休暇等 コード
                             "STAFFNAME_SHOW2"  =>  VARS::post("STAFFNAME_SHOW2"),  //休暇等 名称
                            );
                            
//        $this->data["STAFFCD"]  =  VARS::request("STAFFCD");
//        for ($i = 0; $i < get_count($this->data["STAFFCD"]); $i++) {
//            $this->fields["CHECK"][]            = VARS::post("CHECK"."_".$i);
        for ($i = 1; $i <= 20; $i++) {
            $this->fields["STAFF"][]        = VARS::post("STAFF"."_".$i);
            $this->fields["REMARK2"][]      = VARS::post("REMARK2"."_".$i);
            $this->fields["REMARK3"][]      = VARS::post("REMARK3"."_".$i);
            $this->fields["REMARK4"][]      = VARS::post("REMARK4"."_".$i);
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->validate_row_cnt($this->field["NEWS"], (93 * 2)) > 8) {
            $this->setWarning("MSG915","(記事は8行までです)");
            return false;
        }
        if (strlen($this->field["NEWS"]) > 1500) {
            $this->setWarning("MSG915","(記事は1500バイトまでです)");
            return false;
        }

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
            if ($this->cmd == "update") {
                if ($this->validateInput()) {
                    knjg045dQuery::getDeleteQuery($this);
                    knjg045dQuery::getInsertQuery($this);
                }
            } else if ($this->cmd == "delete") {
                knjg045dQuery::getDeleteQuery($this, "delete");
            }
            $this->setMessage("MSG201");
    }

    // 編集完了用のモデルを返します
    function &getUpdateDetailSeqModel()
    {
        $this->setDiaryDate =  VARS::post("setDiaryDate");
        $this->setStaffDiv  =  VARS::post("setStaffDiv");
//        $this->setcounter =  VARS::post("setcounter");
        //入力チェック
        for ($i = 1; $i <= 20; $i++) {
            $this->fields["STAFF"][$i]          = VARS::post("STAFF"."_".$i);
            $this->fields["REMARK2"][$i]        = VARS::post("REMARK2"."_".$i);
            $this->fields["REMARK3"][$i]        = VARS::post("REMARK3"."_".$i);
            $this->fields["REMARK4"][$i]        = VARS::post("REMARK4"."_".$i);
        }

        knjg045dQuery::getUpdateDetailSeq($this);
        //$this->setMessage("MSG201");

        if ($this->setStaffDiv === '1') {
            $this->cmd = 'shutcho-A';
        } else if ($this->setStaffDiv === '2') {
            $this->cmd = 'kyuka-A';
        }
    }

}
?>
