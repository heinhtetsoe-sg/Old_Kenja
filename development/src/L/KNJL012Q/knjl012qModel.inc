<?php

require_once('for_php7.php');

class knjl012qModel extends Model {
    var $ObjYear;
    var $applicantdiv;
    var $cmd;
    var $fsCd;

    function knjl012qModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->fsCd);
        }

        $this->ObjYear      = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");

        $this->cmd          = VARS::request("cmd");

        //出身学校コードを取得
        if (!$this->fsCd) {
            $this->fsCd = knjl012qQuery::getNameMstA023();
        }
    }

    //入力項目チェック
    function validateInput() {
        if (!($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }

        $db = Query::dbCheckOut();
        //受験番号数取得
        $examCnt = $db->getOne(knjl012qQuery::getEntexamNoDat($this->ObjYear, $this->applicantdiv, "cnt"));
        //作成対象になる生徒件数取得
        $schCnt = $db->getOne(knjl012qQuery::getSchregCnt());
        Query::dbCheckIn($db);

        //件数チェック
        if ($examCnt < $schCnt) {
            $this->setWarning("MSG203", "\\n設定された受験番号が足りません。\\n受験番号範囲設定を確認してください。");
            return false;
        }

        return true;
    }

    //実行処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjl012qQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
