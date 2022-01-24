<?php

require_once('for_php7.php');

//定数定義
class knjp982Model extends Model
{
    public $field = array();
    public $cmd;
    public $levy_lmcd;      //会計科目+項目コード
    public $levy_scd;       //費目小分類コード
    public $year;

    //メンバ変数の初期化
    public function knjp982Model()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->auth);
            unset($this->levy_lmcd);
            unset($this->levy_scd);
        }

        $this->year = CTRL_YEAR;
        $this->cmd = VARS::request("cmd");
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        if ($this->cmd == "change") {
            unset($this->levy_lmcd);
            unset($this->levy_scd);
        }

        if (VARS::get("LEVY_L_M_CD")) {
            $this->levy_lmcd = VARS::get("LEVY_L_M_CD");
        }
        if (VARS::get("LEVY_S_CD")) {
            $this->levy_scd = sprintf("%03d", VARS::get("LEVY_S_CD"));
        }

        $this->field = array("YEAR"           => $this->year,
                             "LEVY_L_M_CD"   => VARS::post("LEVY_L_M_CD"),   //会計科目・項目コード
                             "LEVY_S_CD"     => VARS::post("LEVY_S_CD"),     //会計細目コード
                             "LEVY_S_NAME"   => VARS::post("LEVY_S_NAME"),   //会計細目名称
                             "LEVY_S_ABBV"   => VARS::post("LEVY_S_ABBV"),   //会計細目略称
                             "REPAY_DIV"     => VARS::post("REPAY_DIV"),     //返金可・不可
                             "BENEFIT"       => VARS::post("BENEFIT"),       //給付対象
                             "MAX_BENEFIT"   => VARS::post("MAX_BENEFIT"),   //給付上限額
                             "REMARK"        => VARS::post("REMARK"),        //備考
                             "KIND_CD"       => VARS::post("KIND_CD"),       //種別
                             "KIND_NAME"     => VARS::post("KIND_NAME"),     //種別名称
                             "DEPOSIT_CD"    => VARS::post("DEPOSIT_CD")     //内訳費目コード
                             );

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if (strlen($this->field["LEVY_L_M_CD"]) == 0) {
            $this->setWarning("MSG301", "(会計科目・項目コード)");
            return false;
        }
        if (strlen($this->field["LEVY_S_CD"]) == 0) {
            $this->setWarning("MSG301", "(会計細目コード)");
            return false;
        }
        if ($kind == "insert" || $kind == "update") {
            if (strlen($this->field["LEVY_S_NAME"]) == 0) {
                $this->setWarning("MSG301", "(会計細目名称)");
                return false;
            }
        }

        if (($kind=="update" || $kind=="delete") && $this->levy_scd != $this->field["LEVY_S_CD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }

        //入力項目の１レコード取得
        $row = knjp982Query::getRow($this, 2);
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["LEVY_S_NAME"]) > 90) {
            $this->setWarning("MSG915", "(会計細目名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["LEVY_S_ABBV"]) > 90) {
            $this->setWarning("MSG915", "(会計細目略称は90バイトまでです。)");
            return false;
        }

        //給付上限額
        if ($model->Properties["useBenefit"] == "1" && strlen($this->field["MAX_BENEFIT"]) > 8) {
            $this->setWarning("MSG915", "(給付上限額は8バイトまでです。)");
            return false;
        }

        if (strlen($this->field["REMARK"]) > 60) {
            $this->setWarning("MSG915", "(備考は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["KIND_CD"]) > 3) {
            $this->setWarning("MSG915", "(種別は3バイトまでです。)");
            return false;
        }
        if (strlen($this->field["KIND_NAME"]) > 90) {
            $this->setWarning("MSG915", "(種別名称は90バイトまでです。)");
            return false;
        }
        if (strlen($this->field["DEPOSIT_CD"]) > 3) {
            $this->setWarning("MSG915", "(内訳費目コードは3バイトまでです。)");
            return false;
        }

        return true;
    }

    //追加処理
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjp982Query::getInsertQuery($this->field, $this);
            $this->levy_lmcd = $this->field["LEVY_L_M_CD"];
            $this->levy_scd = sprintf("%03d", $this->field["LEVY_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjp982Query::getUpdateQuery($this->field, $this);
            $this->levy_lmcd = $this->field["LEVY_L_M_CD"];
            $this->levy_scd = sprintf("%03d", $this->field["LEVY_S_CD"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjp982Query::getDeleteQuery($this->field, $this);
            $this->levy_lmcd = "";
            $this->levy_scd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
