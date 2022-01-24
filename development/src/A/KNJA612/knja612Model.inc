<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja612Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $scoreList  = array();

    /**
     * メンバ変数の初期化
     */
    public function knja612Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"), //年組
                             "DATA_DIV"       =>  VARS::post("DATA_DIV"),       //文言種別
                             "PERFECT"        =>  VARS::post("PERFECT"),        //満点
                             "DBNAME"         =>  VARS::post("DBNAME")          //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        if (VARS::post("SCHREGNOS") != "") {
            $schregnoList = explode(',', VARS::post("SCHREGNOS"));
            $this->scoreList = null;
            foreach ($schregnoList as $schregno) {
                $this->scoreList[$schregno] = VARS::post("SCHREGNO-".$schregno);
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function updateScore()
    {
        //入力チェック
        if ($this->validateInput() == false) {
            unset($scoreList);
            return false;
        }

        //登録・更新実行
        knja612Query::modifyHtrainremarkScoreDat($this);

        //更新完了通知
        $this->setMessage("MSG201");
    }

    //入力チェック
    public function validateInput()
    {
        //各行の下限値入力チェック
        $scoreListKeys = array_keys($this->scoreList);
        for ($i = 0; $i < get_count($scoreListKeys); $i++) {
            if ($this->scoreList[$scoreListKeys[$i]] != "") {
                $rowNum = $i + 1;

                //半角数字チェック
                if (preg_match('/^[0-9]+$/', $this->scoreList[$scoreListKeys[$i]]) == false) {
                    $this->setWarning("MSG907", "( 得点 {$rowNum}行目 )");
                    return false;
                }

                //入力範囲チェック
                if (($this->scoreList[$scoreListKeys[$i]] < 0) || ($this->field["PERFECT"] < $this->scoreList[$scoreListKeys[$i]])) {
                    $this->setWarning("MSG914", "( 得点 {$rowNum}行目 )\\n最小：0 ～ 最大：{$this->field["PERFECT"]}");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
