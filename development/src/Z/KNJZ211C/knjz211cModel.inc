<?php

require_once('for_php7.php');

class knjz211cModel extends Model {
    var $field = array();
    var $cmd;
    var $schoolkind;
    var $pattern_cd;
    var $send_prgid;
    var $send_grade;
    var $send_subclass;

    function knjz211cModel() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->schoolkind);
            unset($this->pattern_cd);
            $this->field = array();

            unset($this->send_prgid);
            unset($this->send_grade);
            unset($this->send_subclass);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),
                             "PATTERN_CD"           =>  strlen(VARS::post("PATTERN_CD")) ? sprintf("%03d", VARS::post("PATTERN_CD")) : "",
                             "PATTERN_NAME"         =>  VARS::post("PATTERN_NAME"),
                             "PERFECT"              =>  VARS::post("PERFECT"),
                             "ASSESSLEVEL_CNT"      =>  VARS::post("ASSESSLEVEL_CNT")
                            );

        if (VARS::post("ASSESSLEVEL_CNT") > 0) {
            for ($i = 1; $i <= VARS::post("ASSESSLEVEL_CNT"); $i++) {
                $this->field["ASSESSMARK-".$i]      = VARS::post("ASSESSMARK-".$i);
                $this->field["ASSESSLOW-".$i]       = VARS::post("ASSESSLOW-".$i);
                $this->field["ASSESSHIGH-".$i]      = VARS::post("ASSESSHIGH-".$i);
                $this->field["ASSESS_SHOW1-".$i]    = VARS::post("ASSESS_SHOW1-".$i);
                $this->field["ASSESS_SHOW2-".$i]    = VARS::post("ASSESS_SHOW2-".$i);
            }
        }

        $this->cmd = VARS::request("cmd");

        //list→edit
        if (VARS::request("SCHOOL_KIND") != "") {
            $this->schoolkind = VARS::request("SCHOOL_KIND");
        }
        if (VARS::get("PATTERN_CD")) {
            $this->pattern_cd = VARS::get("PATTERN_CD");
        }

        //プログラムからコール
        if (VARS::get("SEND_PRGID")) {
            $this->send_prgid = VARS::get("SEND_PRGID");
        }
        if (VARS::get("SEND_GRADE")) {
            $this->send_grade = VARS::get("SEND_GRADE");
        }
        if (VARS::get("SEND_SUBCLASS")) {
            $this->send_subclass = VARS::get("SEND_SUBCLASS");
        }
    }

    //入力項目チェック
    function validateInput($cmd) {
        //チェック項目一覧
        //フィールド => サイズ, 必須チェック, サイズチェック, 項目名
        $checkArray = array("SCHOOL_KIND"       => array(1,   "HISSU",  "",       "校種"),
                            "PATTERN_CD"        => array(3,   "HISSU",  "KETA",   "パターンコード"),
                            "PATTERN_NAME"      => array(45,  "HISSU",  "KETA",   "名称"),
                            "PERFECT"           => array(3,   "HISSU",  "KETA",   "満点"),
                            "ASSESSLEVEL_CNT"   => array(3,   "HISSU",  "KETA",   "段階数"),
                            "ASSESSMARK"        => array(6,   "",       "KETA2",  "記号"),
                            "ASSESSLOW"         => array(3,   "HISSU",  "KETA2",  "下限値"),
                            "ASSESS_SHOW1"      => array(15,  "",       "KETA2",  "表示1"),
                            "ASSESS_SHOW2"      => array(15,  "",       "KETA2",  "表示2")
                           );


        if ($cmd == "update" || $cmd == "add") {
            //チェック
            $errMsgHissu = $errMsgKeta = array();
            foreach ($checkArray as $field => $val) {
                list ($size, $hissu, $keta, $label) = $val;
                //必須チェック
                if ($hissu) {
                    if ($field == "ASSESSLOW") {
                        for ($i = 1; $i <= $this->field["ASSESSLEVEL_CNT"]; $i++) {
                            if ($this->field[$field."-".$i] == "") {
                                $errMsgHissu[] = $label.$i;
                            }
                        }
                    } else {
                        if ($this->field[$field] == "") {
                            $errMsgHissu[] = $label;
                        }
                    }
                }
                //文字数チェック
                if ($keta == "KETA2") {
                    for ($i = 1; $i <= $this->field["ASSESSLEVEL_CNT"]; $i++) {
                        if (strlen($this->field[$field."-".$i]) > $size) {
                            $errMsgKeta[] = $label."-".$i."は".$size."バイトまで";
                        }
                    }
                } else if ($keta) {
                    if (strlen($this->field[$field]) > $size) {
                        $errMsgKeta[] = $label."は".$size."バイトまで";
                    }
                }
            }
            if (get_count($errMsgHissu) > 0) {
                $this->setWarning("MSG301","( ".implode(',', $errMsgHissu)." )");
                return false;
            }
            if (get_count($errMsgKeta) > 0) {
                $addMsg = (get_count($errMsgKeta) > 10) ? "..." : "";
                array_splice($errMsgKeta, 10);
                $this->setWarning("MSG915",implode('\n', $errMsgKeta).$addMsg);
                return false;
            }
        }

        if ($cmd == "update" || $cmd == "delete") {
            //リストより選択しているか
            if ($this->schoolkind == "" || $this->pattern_cd == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //必須項目が変更されていないか
            if ($this->field["SCHOOL_KIND"] != $this->schoolkind) {
                $this->setWarning("MSG308","( 校種 )");
                return false;
            }
            if ($this->field["PATTERN_CD"] != $this->pattern_cd) {
                $this->setWarning("MSG308","( パターンコード )");
                return false;
            }
        }

        if ($cmd == "add") {
            //存在チェック
            $db = Query::dbCheckOut();
            $cnt = $db->getOne(knjz211cQuery::checkExistsYmst(CTRL_YEAR, $this->field["SCHOOL_KIND"], $this->field["PATTERN_CD"]));
            Query::dbCheckIn($db);
            if ($cnt > 0) {
                $this->setWarning("MSG302","( パターンコード )");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput($this->cmd)) {
            $this->update();
            $this->setMessage("MSG201");
            $this->pattern_cd = $this->field["PATTERN_CD"];
            $this->field = array();
            return;
        }
    }
    function &update() {
        knjz211cQuery::getUpdateQuery($this, $this->field);
        return;
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
            $this->setMessage("MSG201");
            $this->field = array();
            unset($this->pattern_cd);
            return;
        }
    }
    function &delete() {
        knjz211cQuery::getDeleteQuery($this, $this->field["SCHOOL_KIND"], $this->field["PATTERN_CD"]);
        return;
    }

    //前年度コピー
    function &getCopyModel() {
        //存在チェック
        $db = Query::dbCheckOut();
        $preCnt     = $db->getOne(knjz211cQuery::checkExistsYmst((CTRL_YEAR - 1), $this->schoolkind));
        Query::dbCheckIn($db);
        if ($preCnt == 0) {
            $this->setWarning("MSG203","前年度にデータがありません。");
            return false;
        }

        //今年度のデータ削除
        knjz211cQuery::getDeleteQuery($this, $this->schoolkind);

        //コピー
        knjz211cQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        $this->field = array();
        return;
    }
}
?>
