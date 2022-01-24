<?php

require_once('for_php7.php');

class knjz204Model extends Model {
    var $field      = array();
    var $cmd;
    var $patternCd = "";           //primary

    function knjz204Model() {
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            $this->patternCd = "";
        }
        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("PATTERNCD"          =>  VARS::post("PATTERNCD"),
                             "PATTERNCDNAME"      =>  VARS::post("PATTERNCDNAME"),
                             "BASEDATE"           =>  VARS::post("BASEDATE"),
                             "ASSESSLEVELCNT"     =>  VARS::post("ASSESSLEVELCNT"),
                             "ASSESSMARK"         =>  VARS::post("ASSESSMARK"),
                             "RATE"               =>  VARS::post("RATE")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("PATTERNCD")!="") {
            $this->patternCd = VARS::get("PATTERNCD");
        }

        $this->fields = array();
        for ($i = 0; $i < $this->field["ASSESSLEVELCNT"]; $i++) {
            //記号
            $assessmark = "ASSESSMARK"."-".$i;
            $this->fields["ASSESSMARK"][]  = VARS::post($assessmark);
            //率
            $rate  = "RATE"."-".$i;
            $this->fields["RATE"][]  = VARS::post($rate);
        }
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $cnt  = knjz204Query::getCopyCountQuery();
        $cnt2 = knjz204Query::getCopyCount2();
        if ($cnt2 != '0') {
            $this->setMessage('すでにデータがあります。');
        } else if ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            $result = knjz204Query::getCopyDelMstQuery();
            $result = knjz204Query::getCopyDelDatQuery();
            $result = knjz204Query::getCopyClassQuery1();
            $result = knjz204Query::getCopyClassQuery2();
            $this->setMessage('MSG201','コピー件数：'.$cnt.'件');
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["PATTERNCD"]) == "") {
            $this->setWarning("MSG301","(パターンコード)");
            return false;
        }
        if (strlen($this->field["PATTERNCDNAME"]) == 0) {
            $this->setWarning("MSG301","(パターン名称)");
            return false;
        }
        if (strlen($this->field["BASEDATE"]) == 0) {
            $this->setWarning("MSG301","(基準日)");
            return false;
        }
        if (strlen($this->field["ASSESSLEVELCNT"]) == 0) {
            $this->setWarning("MSG301","(段階値)");
            return false;
        }
        //長さチェック
        if ($this->field["PATTERNCDNAME"] > 30) {
            $this->setWarning("MSG914","(パターン名称)");
            return false;
        }
        //リストより選択しているか
        if (($this->patternCd != $this->field["PATTERNCD"]) && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }
        //重複チェック
        $checkPcd  = knjz204Query::checkPcd($this->field["PATTERNCD"]);
        if ($checkPcd && $this->cmd == "add") {
            //元の値をセット
            $this->field["ASSESSLEVELCNT"] = $this->assesslevelCnt;
            $this->setWarning("MSG302","(パターンコード)");
            return false;
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
            $this->patternCd = $this->field["PATTERNCD"];
        }
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->setMessage("MSG201");
            $this->patternCd = $this->field["PATTERNCD"];
        }
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //リストより選択しているか
        if ($this->patternCd != $this->field["PATTERNCD"]) {
            $this->setWarning("MSG308");
            return false;
        }
        knjz204Query::getDeleteQuery($this->field);
        $this->setMessage("MSG201");

        $this->patternCd = "";
        $this->field = array();
    }

    //インサート
    function &insert() {
        $result = knjz204Query::getInsertQuery($this->field, $this);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update() {
        knjz204Query::getDeleteQuery($this->field);
        $result = knjz204Query::getInsertQuery($this->field, $this);

        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
