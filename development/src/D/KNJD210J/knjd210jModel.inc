<?php

require_once('for_php7.php');


class knjd210jModel extends Model {
    var $grade;
    var $exam;
    var $seme;
    var $kind;
    var $item;
    var $seme_sch;
    var $cmd;
    var $control;
    var $chairdate;
    var $subclasscd;
    var $electdiv;
    var $tableStr;

    function knjd210jModel()
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
        $this->subclasscd = VARS::post("SUBCLASSCD");
        //メニュー起動時
        if (VARS::get("PROGRAMID")) {
            $this->electdiv = "1"; //初期値
        } else {
            $this->electdiv = VARS::post("ELECTDIV");
        }
        //更新テーブル _dat:record_rank_dat _v_dat:record_rank_v_dat
        $this->tableStr = "_dat";
//echo "更新テーブル：record_rank" .$this->tableStr;
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput()
    {
        //存在チェック（在籍データ）
        $cnt_sch = knjd210jQuery::ChecktoSchregno($this);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }
        //存在チェック（成績データ）
        $cnt_rec = knjd210jQuery::ChecktoRecordScore($this);
        if (0 < $cnt_rec) {
        } else {
            $this->setWarning("MSG303", "\\n(成績データ)");
            return false;
        }
        //存在チェック（コースグループデータ）
        $cnt_rec = knjd210jQuery::checkCourseGroup($this);
        if (0 < $cnt_rec) {
            $this->setWarning("MSG305", "\\n【コースグループデータ】\\nコースが全てコースグループに設定されているか確認して下さい。");
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
            knjd210jQuery::executeRireki($this);
            if (knjd210jQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
                $this->tableStr = "_conv_dat"; //換算値用序列テーブル・・・得点を100点満点に換算した値で序列
                if (knjd210jQuery::ExecuteQuery($this)) {
                    $this->setMessage("MSG202");
                }
            }
        }
    }
}
?>
