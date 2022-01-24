<?php

// kanji=漢字
// $Id: knjd211Model.inc 56580 2017-10-22 12:35:29Z maeshiro $

class knjd211Model extends Model {
    var $field      = array();
    var $semes      = array();
    var $grade;
    var $exam;
    var $cmd;
    var $control;           //コントロールマスタ
    var $date;      //異動対象日付---2005.10.12Add
        
    function knjd211Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    
    function init()
    {
        //試験名称
        $this->field = array( "0101" => "SEM1_INTER_REC",
                              "0102" => "SEM1_TERM_REC",
                              "0103" => "SEM1_REC",
                              "0201" => "SEM2_INTER_REC",
                              "0202" => "SEM2_TERM_REC",
                              "0203" => "SEM2_REC",
                              "0302" => "SEM3_TERM_REC",
                              "0303" => "SEM3_REC",
                              "8003" => "GRADE_RECORD",
                              "8004" => "GRADE_ASSESS",
                              "9001" => "GRADE3_RELAASSESS_5STEP",
                              "9002" => "GRADE3_RELAASSESS_10STEP"
                     );
        //学期
        $this->semes = array( "0101" => "1",
                              "0102" => "1",
                              "0103" => "1",
                              "0201" => "2",
                              "0202" => "2",
                              "0203" => "2",
                              "0302" => "3",
                              "0303" => "3",
                              "8003" => CTRL_SEMESTER,
                              "8004" => CTRL_SEMESTER,
                              "9001" => CTRL_SEMESTER,
                              "9002" => CTRL_SEMESTER
                     );

        $this->cmd      = VARS::request("cmd");
        $this->grade    = VARS::post("GRADE");
        $this->exam     = VARS::post("EXAM");
        $this->date     = VARS::post("DATE");//2005.10.12Add
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput()
    {
        //COURSE_SEQが設定されているデータがあるかどうかをチェック ---> なければエラー
        if (!knjd211Query::ChecktoStart($this, 1)) {
            $this->setWarning("MSG305", "\\n(コースグループ設定)");
            return false;
        }
        //COURSE_SEQが１つでも設定されていないデータがあるかどうかをチェック ---> あればエラー
        if (knjd211Query::ChecktoStart($this, 2)) {
            $this->setWarning("MSG305", "\\n(コースグループ設定)");
            return false;
        }
        //存在チェック（在籍データ）
        if (!knjd211Query::ChecktoSchregno($this)) {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)(".$this->semes[$this->exam]."学期)");
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
            if (knjd211Query::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
