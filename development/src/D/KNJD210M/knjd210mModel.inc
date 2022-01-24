<?php

require_once('for_php7.php');


class knjd210mModel extends Model {
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

    function knjd210mModel()
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
        $cnt_sch = knjd210mQuery::ChecktoSchregno($this);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }
        //存在チェック（成績データ）
        $cnt_rec = knjd210mQuery::ChecktoRecordScore($this);
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
            //model変数（画面の学年）を保持
            $keepGrade = $this->grade;
            //処理学年
            $db = Query::dbCheckOut();
            $opt = array();
            $result = $db->query(knjd210mQuery::GetGrade($keepGrade));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["LABEL"], 
                               "value" => $row["VALUE"]);
            }
            Query::dbCheckIn($db);
            //処理学年ループ　「ALL:全学年」の対応
            foreach ($opt as $key => $val) {
                //一旦、model変数（画面の学年）に処理学年をセット
                $this->grade = $val["value"];
                if (knjd210mQuery::ExecuteQuery($this)) {
                    $this->setMessage("MSG202");
                }
            }
            //model変数（画面の学年）を戻す
            $this->grade = $keepGrade;
        }
    }
}
?>
