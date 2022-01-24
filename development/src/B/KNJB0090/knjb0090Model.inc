<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb0090Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $ctrl_year_semester;//現在年度・学期
    public $from_year_semester;//From年度・学期
    public $to_year_semester;  //To年度・学期
    public $from_seq;          //FromSEQ
    public $to_seq;            //To年度のMAX+1
    public $to_title;          //ToTITLE
    public $stf_chk;           //時間割担当

    /**
     * メンバ変数の初期化
    */
    public function knjb0090Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd    = VARS::request("cmd");

        //現在年度・学期
        $this->ctrl_year_semester = CTRL_YEAR . CTRL_SEMESTER;

        //From
        $this->from_year_semester   = VARS::post("FROM_YEAR_SEMESTER");
        $this->from_seq             = VARS::post("FROM_SEQ");

        //To
        $this->to_year_semester = VARS::post("TO_YEAR_SEMESTER");
        $this->to_title         = VARS::post("TO_TITLE");
        $this->to_seq           = VARS::post("TO_SEQ");//To年度のMAX+1

        //時間割担当
        $this->stf_chk  = VARS::post("STF_CHK");
    }

    public function &getMainModel()
    {
    }

    //入力項目チェック
    public function validateInput()
    {
        /*****From*****/
        //年度・学期
        if ($this->from_year_semester == "") {
            $this->setWarning("MSG310", "( Fromの年度・学期 )");
            return false;
        }
        //基本時間割一覧
        if ($this->from_seq == "") {
            $this->setWarning("MSG310", "( 基本時間割一覧 )");
            return false;
        }
        /*****To*****/
        //年度・学期
        if ($this->to_year_semester == "") {
            $this->setWarning("MSG310", "( Toの年度・学期 )");
            return false;
        }
        //タイトル
        if ($this->to_title == "") {
            $this->setWarning("MSG301", "( タイトル )");
            return false;
        }
        //文字数チェック
        if (strlen($this->to_title) > 45) {
            $this->setWarning("MSG915", "( タイトル ) 45バイトまでです。");
            return false;
        }

        //一致する講座の存在チェック
        $chair_cnt = knjb0090Query::getExists($this);
        if ($chair_cnt == 0) {
            $this->setWarning("\\nコピーできません。\\n\\nコピー後の時間割データ件数がゼロ件になる(一致する講座が無い)為、\\nコピーを許可していません。");
            return false;
        }

        return true;
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }
    }

    public function &insert()
    {
        $seq_cnt = knjb0090Query::getInsertQuery($this);
        $this->setMessage("MSG201", "( " .$this->to_seq ."：" .$this->to_title ."、件数：" .$seq_cnt ."件 )");
    }
}
