<?php

require_once('for_php7.php');

class knjj144cModel extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $field;
    var $schregnoList = array();

    //メンバ変数の初期化
    function knjj144cModel()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->semester);
            unset($this->field);
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        $this->field = array(
            "GRADE"                    =>     VARS::post("GRADE"),
            "HR_CLASS"                 =>     VARS::post("HR_CLASS"),
            "GENDER"                   =>     VARS::post("GENDER"),
            "IGNORE_TIME_REGISTRATION" =>     VARS::post("IGNORE_TIME_REGISTRATION"),
        );

        $this->listData = array();
        foreach ($this->schregnoList as $schregno) {
            $this->listData[$schregno]["ATTEND_CD"] = VARS::post("ATTEND_CD_".$schregno);
            $this->listData[$schregno]["REMARK"] = VARS::post("REMARK_".$schregno);
        }

        if ($this->cmd == "checkAttendCd") {
            $this->ajaxAttendCd = VARS::post("AJAX_ATTEND_CD");
        }
    }

    //入力項目チェック
    function validateInput()
    {

        foreach ($this->schregnoList as $schregno) {
            //文字数チェック
            if (strlen($this->listData[$schregno]["REMARK"]) > 60 ) {
                $this->setWarning("MSG915", "(備考) 60バイトまで");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjj144cQuery::getUpdateQuery($this);
        return $result;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
