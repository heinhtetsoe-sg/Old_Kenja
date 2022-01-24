<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh090_2Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $grade;
    public $hr_class;

    /**
     * メンバ変数の初期化
     */
    public function knjh090_2Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "selectdata"         =>  VARS::post("selectdata"),     //対象生徒
                             "DETAIL_SDATE"         =>  VARS::post("DETAIL_SDATE"), //登録日付
                             "DETAIL_DIV"           =>  VARS::post("DETAIL_DIV"),   //詳細区分
                             "DETAILCD"             =>  VARS::post("DETAILCD"),     //詳細種類
                             "CONTENT"              =>  VARS::post("CONTENT"),      //詳細内容
                             "REMARK"               =>  VARS::post("REMARK")        //備考
                        );

        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS")) {
            $this->hr_class = VARS::get("HR_CLASS");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須項目チェック
        if (!$this->field["DETAIL_SDATE"] && !$this->field["DETAIL_DIV"]) {
            $this->setWarning("MSG301", "　　（登録日付または詳細区分）");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["CONTENT"]) > 900) {
            $this->setWarning("MSG915", "(詳細内容は900バイトまでです)");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 60) {
            $this->setWarning("MSG915", "(備考は60バイトまでです)");
            return false;
        }

        return true;
    }

    //更新
    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            $cnt = knjh090_2Query::insertQuery($this);
            $this->setMessage("MSG201", "（".$cnt."件）");
        }
        return;
    }
}
