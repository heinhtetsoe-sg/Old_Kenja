<?php

require_once('for_php7.php');

class knjl328aModel extends Model
{
    public $cmd;
    public $examyear;
    public $selected_data;
    public $examType;
    public $field;

    public function knjl328aModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->examType = 1;    //固定

        $this->field = array(
                             "APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),   //受験校種
                             "PATTERN_CD"      =>  VARS::post("PATTERN_CD"),     //取扱票種類
                             "SHDIV"           =>  VARS::post("SHDIV"),          //専併区分
                             "TESTDIV"         =>  VARS::post("TESTDIV"),        //試験区分
                             "DESIREDIV"       =>  VARS::post("DESIREDIV"),      //志望コース
                             "RECEPTNO_FROM"   =>  VARS::post("RECEPTNO_FROM"),  //受験番号from
                             "RECEPTNO_TO"     =>  VARS::post("RECEPTNO_TO"),    //受験番号to
                             "TOKUTAI_SELECT"  =>  VARS::post("TOKUTAI_SELECT"), //特待生選択
                             "PREF_NAME"       =>  VARS::post("PREF_NAME"),      //都道府県
                             "ACCOUNT_NUMBER1" =>  VARS::post("ACCOUNT_NUMBER1"),//口座番号1
                             "ACCOUNT_NUMBER2" =>  VARS::post("ACCOUNT_NUMBER2"),//口座番号2
                             "ACCOUNT_NUMBER3" =>  VARS::post("ACCOUNT_NUMBER3"),//口座番号3
                             "MEMBER"          =>  VARS::post("MEMBER"),         //加入者
                             "SUBJECT"         =>  VARS::post("SUBJECT"),        //件名
                             "COMMUNICATION"   =>  VARS::post("COMMUNICATION"),  //通信欄
                             "TRANSFER_MONEY"  =>  VARS::post("TRANSFER_MONEY")  //金額
                            );
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($model->field["APPLICANTDIV"] != "") {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if ($model->field["PATTERN_CD"] != "") {
            $this->setWarning("MSG301", "( 取扱票種類 )");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["PREF_NAME"]) > 12) {
            $this->setWarning("MSG915", "( 都道府県は12バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ACCOUNT_NUMBER1"]) > 5) {
            $this->setWarning("MSG915", "( 口座番号1は5バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ACCOUNT_NUMBER2"]) > 1) {
            $this->setWarning("MSG915", "( 口座番号2は1バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ACCOUNT_NUMBER3"]) > 7) {
            $this->setWarning("MSG915", "( 口座番号3は7バイトまでです。)");
            return false;
        }
        if (strlen($this->field["MEMBER"]) > 30) {
            $this->setWarning("MSG915", "( 加入者名は30バイトまでです。)");
            return false;
        }
        if (strlen($this->field["SUBJECT"]) > 24) {
            $this->setWarning("MSG915", "( 件名は24バイトまでです。)");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 360) {
            $this->setWarning("MSG915", "( 通信欄は360バイトまでです。)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        //データの書込み処理
        $result = knjl328aQuery::getUpdateQuery($this);
        return $result;
    }
}
