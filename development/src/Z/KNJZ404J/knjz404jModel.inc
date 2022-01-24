<?php

require_once('for_php7.php');


class knjz404jModel extends Model {
    var $fields      = array();
    var $cmd;
    var $term;
    var $term2;
    var $control;
    var $check;
    var $checkGrddiv;
    var $chaircd;
    var $defFlg;

    function knjz404jModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->grade);
            unset($this->taisyouYear);
            unset($this->sansyouYear);
            unset($this->subclassCd);
            unset($this->data);
            unset($this->cmd);
        }
        if (!isset($this->taisyouYear)) {
            $this->taisyouYear = CTRL_YEAR;
        }
        if (VARS::request("TAISYOU_YEAR") != "") {
            $this->taisyouYear = VARS::request("TAISYOU_YEAR");
            unset($this->subclassCd);
        }
        if (VARS::request("GRADE") != "") {
            $this->grade = VARS::request("GRADE");
            unset($this->subclassCd);
        }

        if (VARS::request("SANSYOU_YEAR") != "") {
            $this->sansyouYear = VARS::request("SANSYOU_YEAR");
        }
        if (VARS::request("SUBCLASSCD") != "") {
            $this->subclassCd = VARS::request("SUBCLASSCD");
        }

        //段階取得
        $this->dankai = knjz404jQuery::getDankai();

        for ($i=1;$i<=$this->dankai;$i++) {
            for ($j=1;$j<=10;$j++) {
                $this->data['JVIEW_'.$i.'_'.$j] = VARS::request('JVIEW_'.$i.'_'.$j);
            }
        }

        $this->cmd   = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->taisyouYear == "") {
            $this->setWarning("MSG301", "(対象年度)");
            return false;
        }
        if ($this->grade == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if ($this->subclassCd == "") {
            $this->setWarning("MSG301", "(科目)");
            return false;
        }
        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz404jQuery::deleteQuery($this);
            knjz404jQuery::insertQuery($this);
            $this->message = "データを更新しました。";
        }
    }
    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz404jQuery::deleteQuery($this);
            $this->message = "データを削除しました。";
        }
    }

    //コピーボタン押し時の処理
    function &getInsertCopyModel() {
        //「コピーボタン」押し時に,コピー元のデータが存在するかをチェック
        $count = knjz404jQuery::copySansyouCount($this);
        if ($count == 0) {
            $this->warning="参照年度にデータが存在しません。";
            return false;
        }
        //対象年度にデータが存在するかどうかチェック
        $count = knjz404jQuery::copyTaisyouCount($this);
        if ($count > 0) {
            $this->warning="対象年度に既にデータが存在します。。";
            return false;
        }
        knjz404jQuery::copyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
