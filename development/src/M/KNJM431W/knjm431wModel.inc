<?php

require_once('for_php7.php');

class knjm431wModel extends Model
{

        public $field = array();
        public $clear = array();
        public $cmd;
        public $schregno;
        public $sec_competence;
        public $detail_sdate;
        public $detail_edate;
        public $org_detail_sdate;
        public $org_detail_edate;
        public $detaildiv;
        public $org_detaildiv;
        public $def_detaildiv;
        public $dtclick = false;

    public function knjm431wModel()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "right_list") {
            $this->clear = array();
            $this->sendField = array();
            $this->schregno = "";
        }

        $this->field = array(
                             "COMMENT"       =>  VARS::post("COMMENT"),
                             "TEST_DATE"     =>  VARS::post("TEST_DATE"),
                             "SCORE"         =>  VARS::post("SCORE")
                             );

        $this->schregno = VARS::request("SCHREGNO");
        $this->sendFlg = false;
        if ($this->cmd == "edit") {
            $this->sendField = array();
            $this->sendField["YEAR"]           = VARS::get("SEND_YEAR");
            $this->sendField["SEMESTER"]       = VARS::get("SEND_SEMESTER");

            $this->sendField["SEND_TESTTYPE"]            = VARS::get("SEND_TESTTYPE");
            $this->sendField["SEND_SUBCLASS"]            = VARS::get("SEND_SUBCLASS");
            $this->sendField["SEND_SCHREGNO"]            = VARS::get("SEND_SCHREGNO");
            $this->schregno = $this->sendField["SEND_SCHREGNO"];

            if (VARS::get("SEND_TESTKINDCD")) {
                $this->sendField["TESTKINDCD"]     = VARS::get("SEND_TESTKINDCD");
                $this->sendField["TESTITEMCD"]     = VARS::get("SEND_TESTITEMCD");
                $this->sendField["SCORE_DIV"]      = VARS::get("SEND_SCORE_DIV");
            }
            if (VARS::get("SEND_CLASSCD")) {
                $this->sendField["CLASSCD"]        = VARS::get("SEND_CLASSCD");
                $this->sendField["SCHOOL_KIND"]    = VARS::get("SEND_SCHOOL_KIND");
                $this->sendField["CURRICULUM_CD"]  = VARS::get("SEND_CURRICULUM_CD");
                $this->sendField["SUBCLASSCD"]     = VARS::get("SEND_SUBCLASSCD");
            }

            $this->sendField["SCHREGNO"]       = VARS::get("SEND_SCHREGNO");
            $this->sendField["SEQ"]            = VARS::get("SEND_SEQ");

            $this->sendFlg = true;
        } elseif (VARS::get("SEND_YEAR")) {
            $this->sendField = array();
            $this->sendField["YEAR"]           = VARS::get("SEND_YEAR");
            $this->sendField["SEMESTER"]       = VARS::get("SEND_SEMESTER");

            $this->sendField["SEND_TESTTYPE"]            = VARS::get("SEND_TESTTYPE");
            $this->sendField["SEND_SUBCLASS"]            = VARS::get("SEND_SUBCLASS");
            $this->sendField["SEND_SCHREGNO"]            = VARS::get("SEND_SCHREGNO");
            $this->schregno = $this->sendField["SEND_SCHREGNO"];

            $sendTesttype = $this->sendField["SEND_TESTTYPE"];

            if ($sendTesttype) {
                $this->sendField["TESTKINDCD"]     = substr($sendTesttype, 0, 2);
            }
            if ($sendTesttype) {
                $this->sendField["TESTITEMCD"]     = substr($sendTesttype, 2, 2);
            }
            if ($sendTesttype) {
                $this->sendField["SCORE_DIV"]      = substr($sendTesttype, 4, 2);
            }
            $spl_subclass = explode("-", $this->sendField["SEND_SUBCLASS"]);
            if ($spl_subclass[0]) {
                $this->sendField["CLASSCD"]        = $spl_subclass[0];
            }
            if ($spl_subclass[1]) {
                $this->sendField["SCHOOL_KIND"]    = $spl_subclass[1];
            }
            if ($spl_subclass[2]) {
                $this->sendField["CURRICULUM_CD"]  = $spl_subclass[2];
            }
            if ($spl_subclass[3]) {
                $this->sendField["SUBCLASSCD"]     = $spl_subclass[3];
            }
            $this->sendField["SCHREGNO"]       = VARS::get("SEND_SCHREGNO");
            $this->sendField["SEQ"]            = VARS::get("SEND_SEQ");

            $this->sendFlg = true;
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

        //入力項目チェック
    public function validateInput($kind)
    {
        if ($this->schregno == "") {
            $this->schregno."==";
            $this->setWarning("MSG304", "(生徒を選択してから行ってください)");
            return false;
        }

        //キーは画面左側に移動したので、チェック不要。
        //if ($kind=="update" || $kind=="delete") {
        //    $checkTest = $this->sendField["SEMESTER"]."-".$this->sendField["TESTKINDCD"]."-".$this->sendField["TESTITEMCD"]."-".$this->sendField["SCORE_DIV"];
        //    $checkSubclass = $this->sendField["CLASSCD"]."-".$this->sendField["SCHOOL_KIND"]."-".$this->sendField["CURRICULUM_CD"]."-".$this->sendField["SUBCLASSCD"];

        //    if ($checkTest != $this->field["TESTCD"] ||
        //        $checkSubclass != $this->field["SUBCLASSCD"]
        //    ) {
        //        $this->setWarning("MSG308","キー値が変更されています。");
        //        return false;
        //    }
        //}

        if ($kind=="update" || $kind=="insert") {
            $s_date_f = common::DateConv1($this->field["TEST_DATE"], 12);
            if ($s_date_f != CTRL_YEAR) {
                $this->setWarning("MB0001", "(試験日付は今年度以外を選択してはいけません。)");
                return false;
            }

            $db = Query::dbCheckOut();
            if ($this->Properties["knjm431wUseGakkiHyouka"] == "1" && $this->sendField["TESTKINDCD"] == "99" && $this->sendField["TESTITEMCD"] == "00" && $this->sendField["SCORE_DIV"] == "08") {
                // 受験条件をチェックしない
            } else {
                $query = knjm431wQuery::getRepPass($this);

                $passFlg = $db->getOne($query);

                if ($passFlg != "1") {
                    $this->setWarning("MSG203", "受験条件を満たしていません。");
                    return false;
                }
            }

            Query::dbCheckIn($db);
        }
        return true;
    }

    public function &getInsertModel()
    {
        if ($this->validateInput("insert")) {
            $this->insert();
        }
        return;
    }

        //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

        //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }

        //インサート時の内部メソッド
    public function &insert()
    {
        $result = knjm431wQuery::getInsertQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

        //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjm431wQuery::getUpdateQuery($this);
        if ( !isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

        //デリート時の内部メソッド
    public function &delete()
    {
        $result = knjm431wQuery::getDeleteQuery($this);
        if ( !isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        $this->field = array();
        return $result;
    }
}
