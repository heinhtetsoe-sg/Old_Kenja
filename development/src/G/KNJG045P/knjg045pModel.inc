<?php

require_once('for_php7.php');


class knjg045pModel extends Model {

    var $field  = array();  //画面データ格納
    var $cmd;               //コマンド
    var $diaryDate;         //日付
    var $schoolKind;        //校種
    var $control;           //コントロールマスタ

    function knjg045pModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        if ($this->cmd != 'read') {
            $this->weather  = VARS::post("WEATHER");
            $this->diaryDate  = VARS::post("DIARY_DATE");
            if ($this->diaryDate == "") $this->diaryDate  = str_replace("-","/",CTRL_DATE);
            $this->schoolKind  = "P";
            $this->field = array("WEATHER"                      =>  VARS::post("WEATHER"),
                                 "LESSON1"                      =>  VARS::post("LESSON1"),
                                 "LESSON2"                      =>  VARS::post("LESSON2"),
                                 "LESSON3"                      =>  VARS::post("LESSON3"),
                                 "LESSON4"                      =>  VARS::post("LESSON4"),
                                 "LESSON5"                      =>  VARS::post("LESSON5"),
                                 "LESSON6"                      =>  VARS::post("LESSON6"),
                                 "IMPORTANT_MATTER"             =>  VARS::post("IMPORTANT_MATTER"),
                                 "GUEST"                        =>  VARS::post("GUEST"),
                                 "REPORT"                       =>  VARS::post("REPORT"),
                                 "RECEIVE_OFFICIAL_DOCUMENTS"   =>  VARS::post("RECEIVE_OFFICIAL_DOCUMENTS"),
                                 "SENDING_OFFICIAL_DOCUMENTS"   =>  VARS::post("SENDING_OFFICIAL_DOCUMENTS"),
                                 "BUSINESS_TRIP"                =>  VARS::post("BUSINESS_TRIP"),
                                 "VACATION"                     =>  VARS::post("VACATION"),
                                 "SPECIAL_LEAVE"                =>  VARS::post("SPECIAL_LEAVE"),
                                 "ABSENCE"                      =>  VARS::post("ABSENCE"),
                                 "LATE_EARLY"                   =>  VARS::post("LATE_EARLY"),
                                 "REMARK"                       =>  VARS::post("REMARK"),
                         );
            $this->check_field = array("IMPORTANT_MATTER"             =>  array("MOJI" => 22, "GYOU" => 10, "MSG" => "重要事項"),
                                       "GUEST"                        =>  array("MOJI" => 22, "GYOU" => 10, "MSG" => "来校者・氏名・用件"),
                                       "REPORT"                       =>  array("MOJI" => 47, "GYOU" => 10, "MSG" => "記事"),
                                       "RECEIVE_OFFICIAL_DOCUMENTS"   =>  array("MOJI" => 22, "GYOU" => 10, "MSG" => "収受公文書"),
                                       "SENDING_OFFICIAL_DOCUMENTS"   =>  array("MOJI" => 22, "GYOU" => 10, "MSG" => "発送公文書"),
                                       "BUSINESS_TRIP"                =>  array("MOJI" => 22, "GYOU" => 7, "MSG" => "出張"),
                                       "VACATION"                     =>  array("MOJI" => 22, "GYOU" => 7, "MSG" => "休暇"),
                                       "SPECIAL_LEAVE"                =>  array("MOJI" => 22, "GYOU" => 7, "MSG" => "慶弔・産休等"),
                                       "ABSENCE"                      =>  array("MOJI" => 22, "GYOU" => 5, "MSG" => "欠勤"),
                                       "LATE_EARLY"                   =>  array("MOJI" => 22, "GYOU" => 5, "MSG" => "遅刻・早退"),
                               );
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        foreach ($this->check_field as $cKey => $cVal) {
            if ($this->validate_row_cnt($this->field[$cKey], ($cVal["MOJI"] * 2)) > $cVal["GYOU"]) {
                $this->setWarning("MSG915","({$cVal["MSG"]}は{$cVal["GYOU"]}行までです)");
                return false;
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
            if ($this->cmd == "update") {
                if ($this->validateInput()) {
                    $this->delete();
                    $this->insert();
                }
            } else if ($this->cmd == "delete") {
                $this->delete();
            }
            $this->setMessage("MSG201");
    }

    /**
     * インサート時の内部メソッド
     */
    function &insert()
    {
        knjg045pQuery::getInsertQuery($this);
    }

    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        knjg045pQuery::getDeleteQuery($this);
    }

}
?>
