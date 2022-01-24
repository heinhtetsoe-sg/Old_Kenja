<?php

require_once('for_php7.php');


class knjd210sModel extends Model {
    var $grade;
    var $school_kind;
    var $exam;
    var $seme;
    var $kind;
    var $item;
    var $sdiv;
    var $seme_sch;
    var $cmd;
    var $control;
    var $chairdate;
    var $subclasscd;
    var $electdiv;
    var $tableStr;
    var $z010;

    function knjd210sModel()
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
        $this->sdiv     = $test[2];
        $this->seme_sch = ($this->seme != "9") ? $this->seme : CTRL_SEMESTER;
        $this->chairdate = VARS::post("CHAIRDATE");
        $this->subclasscd = VARS::post("SUBCLASSCD");
        //更新テーブル _dat:record_rank_dat _v_dat:record_rank_v_dat _sdiv_dat:record_rank_sdiv_dat
        $this->tableStr = "_sdiv_dat";
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
        //FI対応
        if ($this->Properties["useFi_Hrclass"] == '1') {
            //存在チェック（在籍データ）
            $cnt_sch = knjd210sQuery::ChecktoSchregno($this, "FI");
            if (0 < $cnt_sch) {
            } else {
                $this->setWarning("MSG303", "\\n(FI在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
                return false;
            }
        }
        //存在チェック（在籍データ）
        $cnt_sch = knjd210sQuery::ChecktoSchregno($this);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }
        //存在チェック（成績データ）
        $cnt_rec = knjd210sQuery::ChecktoRecordScore($this);
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
            knjd210sQuery::executeRireki($this);
            if (knjd210sQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
                //FI対応
                if ($this->Properties["useFi_Hrclass"] == '1') {
                    //複式クラス序列テーブル
                    $this->tableStr = "_fi_sdiv_dat";
                    if (knjd210sQuery::ExecuteQuery($this)) {
                        $this->setMessage("MSG202");
                    }
                } else {
                    //累計用序列テーブル
                    //実行条件・・・処理学年：中高、処理種別の小分類が'08'（つまり、ＸＸ得点）
                    if (($this->school_kind == "J" || $this->school_kind == "H") && $this->sdiv == "08") {
                        $this->tableStr = "_ruikei_sdiv_dat";
                        if (knjd210sQuery::ExecuteQuery($this)) {
                            $this->setMessage("MSG202");
                        }
                    }
                }
            }
        }
    }
}
?>
