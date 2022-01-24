<?php

require_once('for_php7.php');


class knjd210oModel extends Model {
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
    var $tableStr;

    function knjd210oModel()
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
            $this->electdiv = ""; //初期値
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
        $cnt_sch = knjd210oQuery::ChecktoSchregno($this);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }
        //存在チェック（成績データ）
        $cnt_rec = knjd210oQuery::ChecktoRecordScore($this);
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
            if (knjd210oQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
            //★ 学期評価／学年評価の場合、以下の通り処理する。
            //-- [更新テーブル：RECORD_RANK_DAT]
            //--  ・評価を１００段階のまま処理する。(英語科特進コース)
            //--  ・評価を５段階のまま処理する。(その他)
            //-- [更新テーブル：RECORD_RANK_V_DAT]
            //--  ・評定マスタを参照し、評価を１００段階から５段階に換算し処理する。(英語科特進コース)
            //--  ・評価を５段階のまま処理する。(その他)
            $semeKindItem = $this->seme .$this->kind .$this->item;
            if ($this->kind == "99" && $semeKindItem != "99900") {
                $this->tableStr = "_v_dat"; //RECORD_RANK_V_DATも作成
                if (knjd210oQuery::ExecuteQuery($this)) {
                    $this->setMessage("MSG202");
                }
            }
        }
    }
}
?>
