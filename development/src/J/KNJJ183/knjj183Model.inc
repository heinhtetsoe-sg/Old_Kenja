<?php

require_once('for_php7.php');


class knjj183Model extends Model {
    var $exp_year;          //左画面よりgetで取得
    var $exp_semester;      //左画面よりgetで取得
    var $schregno;          //左画面よりgetで取得
    var $name;              //左画面よりgetで取得
    var $grade;             //左画面よりgetで取得
    var $schoolkind;
    var $field    = array();
    var $cmd;

    function knjj183Model(){

    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array("GUARD_NAME"        => VARS::post("GUARD_NAME"),      //会員氏名
                             "GUARD_KANA"        => VARS::post("GUARD_KANA"),      //会員ふりがな
                             "GUARD_ZIPCD"       => VARS::post("GUARD_ZIPCD"),     //〒
                             "GUARD_ADDR1"       => VARS::post("GUARD_ADDR1"),     //住所
                             "GUARD_ADDR2"       => VARS::post("GUARD_ADDR2"),     //マンション等名
                             "GUARD_TELNO"       => VARS::post("GUARD_TELNO"),     //電話番号
                             "GUARD_TELNO2"      => VARS::post("GUARD_TELNO2"),    //電話番号２
                             "BRANCHCD"          => VARS::post("BRANCHCD"),        //支部コード
                             "BRANCH_POSITION"   => VARS::post("BRANCH_POSITION"), //役員
                             "RESIDENTCD"        => VARS::post("RESIDENTCD"),      //通学
                             "SEND_NAME"         => VARS::post("SEND_NAME"),       //送付先名

                             "UPDATED"           => VARS::post("UPDATED"));

        $this->cmd            = VARS::request("cmd");

        //左フレームから取得
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade      = VARS::get("GRADE");
            $db = Query::dbCheckOut();
            $query = knjj183Query::getSchoolkind($this->grade);
            $this->schoolkind = $db->getOne($query);
            Query::dbCheckIn($db);
        }
        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //追加、更新、削除時にリストから選択しているかをチェック
        if ($this->cmd == "update" || $this->cmd == "delete" || $this->cmd == "add") {
            if($this->schregno == "") {
                $this->setWarning("MSG308");
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd =="add") {
            //必須項目
            if (trim($this->field["GUARD_NAME"]) == "") {
                $this->setWarning("MSG310","会員氏名");
                return false;
            }
            //文字数制限
            if (strlen($this->field["GUARD_NAME"]) >120) { 
                $this->setWarning("MSG915","(会員氏名は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["GUARD_KANA"]) >240) {
                $this->setWarning("MSG915","(会員ふりがなは240バイトまでです。)");
                return false;
            } elseif (strlen($this->field["GUARD_ADDR1"]) >150) {
                $this->setWarning("MSG915","(住所は150バイトまでです。)");
                return false;
            } elseif (strlen($this->field["GUARD_ADDR2"]) >150) {
                $this->setWarning("MSG915","(マンション等名は150バイトまでです。)");
                return false;
            } elseif (strlen($this->field["GUARD_TELNO"]) >14) {
                $this->setWarning("MSG915","(電話番号は14バイトまでです。)");
                return false;
            } elseif (strlen($this->field["GUARD_TELNO2"]) >14) {
                $this->setWarning("MSG915","(電話番号２は14バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SEND_NAME"]) >120) {
                $this->setWarning("MSG915","(送付先名は120バイトまでです。)");
                return false;
            }
            //１レコード取得
            $db = Query::dbCheckOut();
            $row = $db->getRow(knjj183Query::getBranchMst($this->schregno, $this->schoolkind, ""),DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("既にデータが存在します。");
                return false;
            }
        }

        return true;
    }

    //アップデート時の内部メソッド
    function &getUpdateModel() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        $db = Query::dbCheckOut();

        $dataRow = $db->getRow(knjj183Query::getBranchMst($this->schregno, $this->schoolkind, ""), DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        if (is_array($dataRow)) {
            $this->cmd = "update";
            //入力チェック
            if ($this->validateInput()) {
                knjj183Query::getUpdateModel($this);
                $this->setMessage("MSG201");
                return;
            }
        } else {
            $this->cmd = "add";
            if($this->validateInput()) {
                knjj183Query::getInsertQuery($this);
                $this->setMessage("MSG201");
                return;
            }
        }
    }
    //削除用のモデルを返します
    function &getDeleteModel() {
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        knjj183Query::getDeleteQuery($this);
        unset($this->schregno);
        $this->setMessage("MSG201");
        return;
    }

     //変数をクリアします
    function clean(){}
}
?>
