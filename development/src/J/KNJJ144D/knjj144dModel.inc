<?php

require_once('for_php7.php');

class knjj144dModel extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $field;
    var $schregnoList = array();

    //メンバ変数の初期化
    function knjj144dModel()
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
            "GRADE_HR_CLASS"     =>     VARS::post("GRADE_HR_CLASS"),
            "GENDER"             =>     VARS::post("GENDER"),
        );

        $this->listData = array();
        foreach ($this->schregnoList as $schregno) {
            $this->listData[$schregno]["TIME_H"] = VARS::post("TIME_H_".$schregno);
            $this->listData[$schregno]["TIME_M"] = VARS::post("TIME_M_".$schregno);
            $this->listData[$schregno]["TIME_S"] = VARS::post("TIME_S_".$schregno);
            $this->listData[$schregno]["REMARK"] = VARS::post("REMARK_".$schregno);
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
        $result = knjj144dQuery::getUpdateQuery($this);
        return $result;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
