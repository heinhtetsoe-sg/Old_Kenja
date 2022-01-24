<?php

require_once('for_php7.php');

class knjx_ziritukatudou_selectModel extends Model
{
    public $cmd;
    public $exp_year = "";
    public $exp_semester = "";
    public $schregno = "";
    public $name = "";
    public $field = array();
    public $control;
    public $annual;        //学年(年次)
    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限
    public $send_prgid = "";


    public function knjx_ziritukatudou_selectModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->target);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::get("TARGET") != "") {
            $this->target       = VARS::get("TARGET");
        }
        if (VARS::get("SEND_PRGID") != "") {
            $this->send_prgid   = VARS::get("SEND_PRGID");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }
}
