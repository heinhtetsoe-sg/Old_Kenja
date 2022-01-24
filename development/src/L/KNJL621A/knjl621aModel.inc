<?php

require_once('for_php7.php');

class knjl621aModel extends Model {
    var $cmd;
    var $year_add;

    function knjl621aModel() {}

    function init() {
        $this->entexamyear = CTRL_YEAR + 1;

        $this->cmd      = VARS::request("cmd");

        $this->field = array(
            "APPLICANTDIV"     => VARS::post("APPLICANTDIV"),    //入試制度
            "TESTDIV"          => VARS::post("TESTDIV"),         //入試区分
            "EXAMNO"           => VARS::post("EXAMNO"),          //受験番号
            "SEATNO"           => VARS::post("SEATNO"),          //座席番号
        );

        if (VARS::post("LIST_ENTEXAMYEAR")) {
            $this->list = array(
                "ENTEXAMYEAR"     => VARS::post("LIST_ENTEXAMYEAR"),    //入試年度
                "APPLICANTDIV"    => VARS::post("LIST_APPLICANTDIV"),   //入試制度
                "TESTDIV"         => VARS::post("LIST_TESTDIV"),        //入試区分
                "EXAMNO"          => VARS::post("LIST_EXAMNO"),         //受験番号
                "EXEC_TIME"       => VARS::post("LIST_EXEC_TIME"),      //日時
            );
        }

        //ajax
        if (VARS::post("AJAX_APPLICANTDIV")) {
            $this->ajaxParam = array(
                "APPLICANTDIV"    => VARS::post("AJAX_APPLICANTDIV"),   //入試制度
                "TESTDIV"         => VARS::post("AJAX_TESTDIV"),        //入試区分
                "EXAMNO"          => VARS::post("AJAX_EXAMNO"),         //受験番号
                "SEATNO"          => VARS::post("AJAX_SEATNO"),          //座席番号
            );
        }

        //座席番号重複チェック結果
        $this->duplSeatnoFlg = VARS::post("DUPL_SEATNO_FLG");
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        if ($this->cmd == "insert") {
            //必須チェック
            if ($this->field["EXAMNO"] == "") {
                $this->setWarning("MSG301", "(受験番号)");
                return;
            }
            if ($this->field["SEATNO"] == "") {
                $this->setWarning("MSG301", "(座席番号)");
                return;
            }
            //数値チェック
            if (!is_numeric($this->field["EXAMNO"])) {
                $this->setWarning("MSG909","(受験番号)");
                return false;
            }
            //文字サイズチェック
            if (strlen($this->field["EXAMNO"]) > 4) {
                $this->setWarning("MSG915","(受験番号は4バイトまでです)");
                return false;
            }
            if (strlen($this->field["SEATNO"]) > 4) {
                $this->setWarning("MSG915","(座席番号は4バイトまでです)");
                return false;
            }

            //存在チェック
            //志願者受付データ
            if (0 == $db->getOne(knjl621aQuery::checkEntexamReceptDat($this))) {
                $this->setWarning("MSG303", "(受験番号)");
                return false;
            };
            //会場番号
            if (0 == $db->getOne(knjl621aQuery::checkEntexamHallYdat($this))) {
                $this->setWarning("MSG303", "(会場番号(座席番号の上2桁))");
                return false;
            };
        }

        Query::dbCheckIn($db);

        return true;
    }

    //登録
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjl621aQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjl621aQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
