<?php

require_once('for_php7.php');

class knjd426nModel extends Model
{
    public $cmd;
    public $control;        //コントロールマスタ
    public $chkprblock;
    public $optright;

    public function knjd426nModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("SEMESTER"               =>  VARS::post("SEMESTER"),             //学期
                             "SCHOOL_KIND"            =>  VARS::post("SCHOOL_KIND"),          //学校種別
                             "HUKUSIKI_RADIO"         =>  VARS::post("HUKUSIKI_RADIO"),       //コンボ切替
                             "MOVE_DATE"              =>  VARS::post("MOVE_DATE"),            //異動対象日付
                             "PRINT_DATE"             =>  VARS::post("PRINT_DATE"),           //記載日付
                             "GHR_CD"                 =>  VARS::post("GHR_CD"),               //年組
                             "OUTPUT_PTRN"            =>  VARS::post("OUTPUT_PTRN"),          //帳票パターン
                             "FORM_YOSHIKI1"          =>  VARS::post("FORM_YOSHIKI1"),        //「準ずる教育」の様式
                             "FORM_YOSHIKI2"          =>  VARS::post("FORM_YOSHIKI2"),        //「自立活動中心用」の様式
                             );

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->optright = array();
        $this->selectdata   = VARS::post("selectdata");

        $this->chkprblock = array();
        $this->subChkprblock= array();

        //親項目
        if (is_array($this->kind_no_array)) {
            foreach ($this->kind_no_array as $kind_no) {
                $this->chkprblock[$kind_no] = VARS::post("PRINT_CHK".$kind_no);
            }
        }

        //子項目
        if (is_array($this->kind_seq_array)) {
            foreach ($this->kind_seq_array as $kind_seq) {
                $this->subChkprblock[$kind_seq] = VARS::post("PRINT_SUB_CHK".$kind_seq);
            }
        }
    }
}
