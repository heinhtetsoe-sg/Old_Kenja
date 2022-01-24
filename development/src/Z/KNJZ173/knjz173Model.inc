<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz173Model extends Model
{

    public $field = array();
    public $cmd;
    public $year;
    public $selKaikinCd;
    //メンバ変数の初期化

    public function knjz173Model()
    {
    }

    public function init()
    {
        $this->year = CTRL_YEAR;
        $this->cmd  = VARS::request("cmd");

        $this->mrgearry = array();
        $targetGradeCntKey = VARS::post("targetGradeCntKey") ? explode(",", VARS::post("targetGradeCntKey")) : array();
        $targetGradeCntVal = VARS::post("targetGradeCntVal") ? explode(",", VARS::post("targetGradeCntVal")) : array();

        for ($i = 0; $i < get_count($targetGradeCntKey); $i++) {
            for ($j = 1; $j <= $targetGradeCntVal[$i]; $j++) {
                if (VARS::post("G_KIND_".$targetGradeCntKey[$i]."_".$j)) {
                    $this->mrgearry[]  = VARS::post("G_KIND_".$targetGradeCntKey[$i]."_".$j);
                }
            }
        }

        //チェックボックスなので、値をarray内から取り出す。
        if (VARS::post("KAIKIN_FLG")) {
            if (is_array(VARS::post("KAIKIN_FLG"))) {
                $pickupval = VARS::post("KAIKIN_FLG")[0];
            } else {
                $pickupval = VARS::post("KAIKIN_FLG");
            }
        } else {
            $pickupval = "";
        }
        if (VARS::get("KAIKIN_CD")) {
            $this->selKaikinCd = "1";  //フラグは、form.phpの最後でクリア)
        } else {
            $this->selKaikinCd = "";
        }
        $tmp_tikoku = VARS::post("TIKOKU_CONDITION") ? VARS::post("TIKOKU_CONDITION") : (VARS::post("HID_TIKOKU_CONDITION") ? VARS::post("HID_TIKOKU_CONDITION") : "");
        $tmp_soutai = VARS::post("SOUTAI_CONDITION") ? VARS::post("SOUTAI_CONDITION") : (VARS::post("HID_SOUTAI_CONDITION") ? VARS::post("HID_SOUTAI_CONDITION") : "");
        $tmp_k_kannsan = VARS::post("KESSEKI_KANSAN") ? VARS::post("KESSEKI_KANSAN") : (VARS::post("HID_KESSEKI_KANSAN") ? VARS::post("HID_KESSEKI_KANSAN") : "");
        $this->field = array("KAIKIN_CD"            =>  $this->selKaikinCd ? VARS::get("KAIKIN_CD") : VARS::post("KAIKIN_CD"),    //皆勤コード
                             "KAIKIN_DIV"           =>  VARS::post("KAIKIN_DIV"),             //区分
                             "KAIKIN_NAME"          =>  VARS::post("KAIKIN_NAME"),            //皆勤名称
                             "REF_YEAR"             =>  VARS::post("REF_YEAR"),               //参照年数
                             "KESSEKI_CONDITION"    =>  VARS::post("KESSEKI_CONDITION"),      //欠席
                             "SET_PREFATTEND_GRADE" =>  VARS::post("SET_PREFATTEND_GRADE"),   //取得対象学年(ラジオ)
                             "LE_EXCHGTYPE"         =>  VARS::post("LE_EXCHGTYPE"),           //遅刻・早退(ラジオ)
                             "TIKOKU_CONDITION"     =>  $tmp_tikoku,       //遅刻
                             "SOUTAI_CONDITION"     =>  $tmp_soutai,       //早退
                             "KESSEKI_KANSAN"       =>  $tmp_k_kannsan,         //欠席(換算)
                             "KEKKA_JISU_CONDITION" =>  VARS::post("KEKKA_JISU_CONDITION"),   //欠課時数
                             "PRIORITY"             =>  VARS::post("PRIORITY"),               //優先順位
                             "KAIKIN_FLG"           =>  $pickupval                            //今年度使用する
                             );

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //add/update用。
/*
        //必須入力チェック
        //皆勤コード、皆勤名称、参照年数、欠席、
        if ($this->field["KAIKIN_CD"] == ""
            || $this->field["KAIKIN_NAME"] == ""
            || $this->field["REF_YEAR"] == ""
            || $this->field["KESSEKI_CONDITION"] == ""
           ) {
            $this->setWarning("MSG310");
            return false;
        }
        //遅刻・早退はパターン分けがあるので別口でチェック
        if ($this->field["LE_EXCHGTYPE"] == "1") {
            if ($this->field["KESSEKI_KANSAN"] == "") {
                $this->setWarning("MSG310");
                return false;
            }
        } else {
            if ($this->field["TIKOKU_CONDITION"] == ""
                || $this->field["SOUTAI_CONDITION"] == ""
               ) {
                $this->setWarning("MSG310");
                return false;
            }
        }
*/
        //遅刻・早退はパターン分け。選択していない方の値はクリア
        if ($this->field["LE_EXCHGTYPE"] == "1") {
            $this->field["TIKOKU_CONDITION"] = "";
            $this->field["SOUTAI_CONDITION"] = "";
        } else {
            $this->field["KESSEKI_KANSAN"] = "";
        }
        //今年度使用のチェックが外れていたら、"0"を設定
        if ($this->field["KAIKIN_FLG"] == "") {
            $this->field["KAIKIN_FLG"] = "0";
        }

        //１レコード取得
        $row = "";
        $row = knjz173Query::getRow($this);

        if ($this->cmd == "add") {
            //追加時に既に登録されていたらエラー
            if (is_array($row)) {
                $this->setWarning("MSG308", "更新するのであれば、更新ボタンを押してください。");
                return false;
            }
        }
        if ($this->cmd == "update") {
            //更新時に既に登録されていなかったらエラー
            if (!is_array($row)) {
                $this->setWarning("MSG303", "新規追加であれば、追加ボタンを押してください。");
                return false;
            }
        }
        //優先順位の一意チェック(重複不可)
        $row = "";
        $row = knjz173Query::chkUniqPriority($this);
        if (is_array($row)) {
            $this->setWarning("MSG302", "優先順位は重複しない数字を設定してください。");
            return false;
        }

        //桁数チェック
        //皆勤コード
        if (strlen($this->field["KAIKIN_CD"]) > 2 ) {
            $this->setWarning("MSG915", "(皆勤コード) 2バイトまでです。");
            return false;
        }

        //皆勤名称
        if (strlen($this->field["KAIKIN_NAME"]) > 45 ) {
            $this->setWarning("MSG915", "(皆勤名称) 15文字までです。");
            return false;
        }
        //参照年数
        if (strlen($this->field["KAIKIN_CD"]) > 2) {
            $this->setWarning("MSG915", "(参照年数) 2桁までです。");
            return false;
        }
        //欠席
        if (strlen($this->field["KESSEKI_CONDITION"]) > 3) {
            $this->setWarning("MSG915", "(欠席) 3桁までです。");
            return false;
        }
        //遅刻・早退はパターン分け
        if ($this->field["LE_EXCHGTYPE"] == "1") {
            if (strlen($this->field["KESSEKI_KANSAN"]) > 2) {
                $this->setWarning("MSG915", "(遅刻換算) 2桁までです。");
                return false;
            }
        } else {
            if (strlen($this->field["TIKOKU_CONDITION"]) > 2) {
                $this->setWarning("MSG915", "(遅刻) 2桁までです。");
                return false;
            }
            if (strlen($this->field["SOUTAI_CONDITION"]) > 2) {
                $this->setWarning("MSG915", "(早退) 2桁までです。");
                return false;
            }
        }
        /*
        //KESSEKI_KANSANがブランクの場合は1を入れておく
        if (strlen(trim($this->field["KESSEKI_KANSAN"])) == 0) {
            $this->field["KESSEKI_KANSAN"] = 1;
        }
        */
        //欠課時数
        if (strlen($this->field["KEKKA_JISU_CONDITION"]) > 3) {
            $this->setWarning("MSG915", "(欠課時数) 3桁までです。");
            return false;
        }
        //優先順位
        if (strlen($this->field["PRIORITY"]) > 2) {
            $this->setWarning("MSG915", "(優先順位) 2桁までです。");
            return false;
        }

        //数字チェック
        //参照年数
        if (!preg_match('/^[0-9]+$/', $this->field["REF_YEAR"])) {
            $this->setWarning("MSG915", "(参照年数) 数字のみ入力可能です。");
            return false;
        }

        if ($this->Properties["KNJZ173_DISPPATTERN"] == "1") {
            //勤怠（欠席・遅刻・早退・結果の合計）
            if ($this->field["REF_YEAR"] == "1" && !preg_match('/^[0-9]+$/', $this->field["KESSEKI_CONDITION"])) {
                $this->setWarning("MSG915", "(欠席・遅刻・早退・結果の合計) 数字のみ入力可能です。");
                return false;
            }
        } else {
            //欠席
            if (!preg_match('/^[0-9]+$/', $this->field["KESSEKI_CONDITION"])) {
                $this->setWarning("MSG915", "(欠席) 数字のみ入力可能です。");
                return false;
            }
            //遅刻・早退はパターン分け
            if ($this->field["LE_EXCHGTYPE"] == "1") {
                if (!preg_match('/^[0-9]+$/', $this->field["KESSEKI_KANSAN"])) {
                    $this->setWarning("MSG915", "(遅刻換算) 数字のみ入力可能です。");
                    return false;
                }
            } else {
                if (!preg_match('/^[0-9]+$/', $this->field["TIKOKU_CONDITION"])) {
                    $this->setWarning("MSG915", "(遅刻) 数字のみ入力可能です。");
                    return false;
                }
                if (!preg_match('/^[0-9]+$/', $this->field["SOUTAI_CONDITION"])) {
                    $this->setWarning("MSG915", "(早退) 数字のみ入力可能です。");
                    return false;
                }
            }
            //欠課時数
            if (!preg_match('/^[0-9]+$/', $this->field["KEKKA_JISU_CONDITION"])) {
                $this->setWarning("MSG915", "(欠課時数) 数字のみ入力可能です。");
                return false;
            }
        }
        //優先順位
        if (!preg_match('/^[0-9]+$/', $this->field["PRIORITY"])) {
            $this->setWarning("MSG915", "(優先順位) 数字のみ入力可能です。");
            return false;
        }

        return true;
    }

    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            $this->insert();
            $this->setMessage("MSG201");
        }
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    
    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        if ($this->delete()) {
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    public function &insert()
    {
        $result = knjz173Query::getInsertQuery($this);
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz173Query::getUpdateQuery($this);
        return $result;
    }

    //デリート時の内部メソッド
    public function &delete()
    {
        $result = knjz173Query::getDeleteQuery($this);
        return $result;
    }

    // 変数をクリアします
    public function &clean()
    {
        unset($this->field);
    }
}
