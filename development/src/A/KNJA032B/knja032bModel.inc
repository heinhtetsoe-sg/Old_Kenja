<?php

require_once('for_php7.php');

class knja032bModel extends Model {
    var $cmd;
    var $control_data = array();        //コントロールのデータを保持
    var $sec_competence;                //権限保持
    var $sort;//ソート
    var $schoolName;
    var $recordTableDiv;
    var $secondarySchoolDiv;

    var $class_cmb;

    //メンバ変数の初期化
    function knja032bModel()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
        $this->sort["ATTENDNO"] = 1;
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        if (VARS::post("CLASS_CMB")) {
            $this->class_cmb = VARS::post("CLASS_CMB");
        }
        //チェックした学籍番号
        $this->checked  = VARS::post("CHECKED");
        //処理
        $this->trans    = VARS::post("TRANS");
        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck(STAFFCD, "KNJA032B");
        // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
        $rtnRow = knja032bQuery::getNameMst();
        $this->schoolName           = $rtnRow["NAME1"];     // 学校区分
        $this->recordTableDiv       = $rtnRow["NAMESPARE1"];// 1:RECORD_SCORE_DAT, null:RECORD_DAT
        $this->secondarySchoolDiv   = $rtnRow["NAMESPARE2"];// 1:中高一貫

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
    /**
     * 全体表示用モデルを返します
    */
    function &getMainModel()
    {
        //ソート用 番号、席次
        $sort = array("ATTENDNO","TARGET");
        //ソートが押下
        if (trim(VARS::request("sort")) != ""){
            foreach($sort as $field){
                if (VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0) {
                    //昇順⇔降順
                    $this->sort[$field] = (int)$this->sort[$field] *(-1);
                }elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                }else{
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //権限チェック
        if($this->sec_competence != DEF_UPDATABLE){
            $this->setWarning("MSG300");
            return false;
        }
        if (!is_array($this->checked)){
            $this->warning = "チェックボックスを選択して下さい。";
            return false;
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
        //データの書込み処理
        $result = knja032bQuery::getUpdateQuery($this);
        return $result;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
