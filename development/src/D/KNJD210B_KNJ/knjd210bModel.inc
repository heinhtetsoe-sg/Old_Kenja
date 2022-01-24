<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd210bModel.inc,v 1.5 2008/07/09 07:31:36 nakamoto Exp $

class knjd210bModel extends Model {
    var $grade;
    var $exam;
    var $seme;
    var $kind;
    var $item;
    var $seme_sch;
    var $cmd;
    var $control;
    var $chairdate;
    var $electdiv;
    var $schoolName;
    var $patarndiv;
        
    function knjd210bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    
    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->grade    = VARS::post("GRADE");
        $this->exam     = VARS::post("EXAM");
        $test = explode("-", VARS::post("EXAM"));
        $this->seme     = VARS::post("SEMESTER");
        $this->kind     = $test[0];
        $this->item     = $test[1];
        $this->seme_sch = ($this->seme != "9") ? $this->seme : CTRL_SEMESTER;
        $this->chairdate = VARS::post("CHAIRDATE");
        //メニュー起動時
        if (VARS::get("PROGRAMID")) {
            $this->electdiv = "1"; //初期値
        } else {
            $this->electdiv = VARS::post("ELECTDIV");
        }
        //学校を判断する
        $rtnRow = knjd210bquery::getNameMst();
        $this->schoolName       = $rtnRow["NAME1"];
        //「3/5/全科の順位」（1:総合点で生成 2:平均点で生成）
        $this->patarndiv = VARS::post("PATARN_DIV");
        if ($this->schoolName == "HOUSEI" || $this->schoolName == "jisyukan") $this->patarndiv = "1";
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput()
    {
        //存在チェック（在籍データ）
        $cnt_sch = knjd210bQuery::ChecktoSchregno($this);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }
        //存在チェック（成績データ）
        $cnt_rec = knjd210bQuery::ChecktoRecordScore($this);
        if (0 < $cnt_rec) {
        } else {
            $this->setWarning("MSG303", "\\n(成績データ)");
            return false;
        }

        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //事前処理チェック
        if ($this->validateInput()){
            if (knjd210bQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
