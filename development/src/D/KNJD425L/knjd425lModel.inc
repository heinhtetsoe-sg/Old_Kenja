<?php

require_once('for_php7.php');

class knjd425lModel extends Model
{
    public $cmd;
    public $schregno;
    public $name;
    public $field = array();
    public $control;
    public $exp_year;      //左フレームから送られてくる年度
    public $exp_semester;  //左フレームから送られてくる学期
    public $sendPrgId;     //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限
    public $grade;         //左フレームから送られてくる年組
    public $selKindNo;     //別画面に引き継ぐKIND_NO
    public $upddate;       //右フレームで設定する更新日

    public function knjd425lModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->sendPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->selKindNo);
            unset($this->upddate);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //年組
        if (VARS::get("GRADE")) {
            //ハイフンを含むときは、HR_CLASSはセットしない
            $pos = strpos(VARS::get("GRADE"), "-");
            if ($pos === false) {
                $this->grade = VARS::get("GRADE")."-".VARS::get("HR_CLASS");
            } else {
                $this->grade = VARS::get("GRADE");
            }
        }

        $this->field = array("SUPPORTPLAN_DATE"    => VARS::post("SUPPORTPLAN_DATE"),
                             "SUPPORTPLAN_STAFFCD" => VARS::post("SUPPORTPLAN_STAFFCD")
                            );

        //選択したボタンID
        if (VARS::post("HID_SELKINDNO")) {
            $this->selKindNo = VARS::post("HID_SELKINDNO");
        }
        //更新日(別画面に引き渡す値。コンボで選択した日付)
        if (VARS::post("UPDDATE")) {
            $this->upddate = VARS::post("UPDDATE");
        }
        //新規を選択した際の日付(入力 or ポップアップで指定した日付)
        $this->recordDate = "";
        if (VARS::post("RECORD_DATE")) {
            $this->recordDate = VARS::post("RECORD_DATE");
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->selKindNo);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjd425lQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
            $this->setCmd("edit");
        } else {
            $this->setCmd("check");
        }
        return;
    }
}
