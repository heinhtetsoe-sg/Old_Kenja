<?php

require_once('for_php7.php');

class knjp744Model extends Model
{
    public $cmd;
    public $field = array();
    public $sansyouYear;
    public $selectdata;

    public function knjp744Model()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID")) {
            unset($this->sansyouYear);
            unset($this->schoolKind);
            unset($this->auto_no);
            unset($this->selectdata);
            $this->field = array();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //年度
        $this->sansyouYear = (VARS::request("SANSYOU_YEAR")) ? VARS::request("SANSYOU_YEAR") : $this->sansyouYear;
        $this->sansyouYear = ($this->sansyouYear) ? $this->sansyouYear : CTRL_YEAR;
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        //左画面から
        if (VARS::get("AUTO_NO")) {
            $this->auto_no  = VARS::get("AUTO_NO");
        }

        $this->field = array("AUTO_NO"               => strlen(VARS::post("AUTO_NO")) ? sprintf("%03d", VARS::post("AUTO_NO")) : "",
                             "AUTO_NAME"             => VARS::post("AUTO_NAME"),
                             "COLLECT_LM_CD"         => VARS::post("COLLECT_LM_CD"),
                             "INCOME_LM_CD"          => VARS::post("INCOME_LM_CD"),
                             "REQUEST_REASON"        => VARS::post("REQUEST_REASON"),
                             "REMARK"                => VARS::post("REMARK"),
                             "REQUEST_STAFF"         => VARS::post("REQUEST_STAFF"),
                             "COMMODITY_PRICE"       => VARS::post("COMMODITY_PRICE"),
                             "AUTO_INCOME_APPROVAL"  => VARS::post("AUTO_INCOME_APPROVAL"));

        list($this->field["COLLECT_L_CD"], $this->field["COLLECT_M_CD"]) = explode('-', $this->field["COLLECT_LM_CD"]);
        list($this->field["INCOME_L_CD"], $this->field["INCOME_M_CD"]) = explode('-', $this->field["INCOME_LM_CD"]);

        $this->grade        = VARS::post("GRADE");
        $this->selectdata   = VARS::post("selectdata");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //フィールド => ( 名称 / サイズ / 文字数 / 必須チェック )
        $checkArray = array();
        $checkArray["AUTO_NO"]          = array('ID',            3,    0,   1);
        $checkArray["AUTO_NAME"]        = array('名称',          0,   30,   2);
        $checkArray["COLLECT_LM_CD"]    = array('入金項目',      0,    0,   2);
        $checkArray["INCOME_LM_CD"]     = array('預り金項目',    0,    0,   2);
        $checkArray["REQUEST_REASON"]   = array('件名',          0,   40,   "");
        $checkArray["REMARK"]           = array('備考',          0,   10,   "");
        if ($this->Properties["not_select_schregno_auto_income"] != "1") {
            $checkArray["COMMODITY_PRICE"]  = array('単価',          6,    0,   2);
        }

        foreach ($checkArray as $key => $val) {
            list($label, $size, $moji, $hissu) = $val;

            //必須チェック
            if ($hissu == "1" && !strlen($this->field[$key])) {
                $this->setWarning("MSG301", "( ".$label." )");
                return false;
            }
            if (in_array($kind, array("add", "update"))) {
                //必須チェック
                if ($hissu == 2 && !strlen($this->field[$key])) {
                    $this->setWarning("MSG301", "( ".$label." )");
                    return false;
                }

                //サイズチェック
                if ($size > 0 && strlen($this->field[$key]) > $size) {
                    $this->setWarning("MSG915", "{$label}は{$size}バイトまでです。");
                    return false;
                }
                //文字数チェック
                if ($moji > 0 && $this->validate_row_cnt($this->field[$key], ($moji * 2)) > 1) {
                    $this->setWarning("MSG915", "{$label}は全角{$moji}文字までです。");
                    return false;
                }
            }
        }

        //値チェック
        if (sprintf("%03d", $this->field["AUTO_NO"]) == '000') {
            $this->setWarning("MSG901", "000は対象外です。\\n( ID )");
            return false;
        }
        if (in_array($kind, array("update", "delete"))) {
            if ($this->auto_no !=  $this->field["AUTO_NO"]) {
                $this->setWarning("MSG308", "IDは変更できません。");
                return false;
            }
        }

        //追加時の重複チェック
        $db = Query::dbCheckOut();
        $getData = $db->getOne(knjp744Query::getAutoData($this, $this->field["AUTO_NO"]));
        if ($kind == "add" && strlen($getData)) {
            $this->setWarning("MSG918");
            return false;
        }
        //追加時の入金項目重複をチェック
        if ($this->Properties["not_select_schregno_auto_income"] == "1") {
            $existsLMCnt = $db->getOne(knjp744Query::getExistsLM($this, $this->field["COLLECT_LM_CD"]));
            if ($kind == "add" && $existsLMCnt > 0) {
                $this->setWarning("MSG302", "入金項目を重複して設定することはできません。");
                return false;
            }
        }
        Query::dbCheckIn($db);

        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput($this->cmd)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjp744Query::getDeleteQuery($this, $db);
            knjp744Query::getUpdateQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->auto_no = sprintf("%03d", $this->field["AUTO_NO"]);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput($this->cmd)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjp744Query::getDeleteQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->auto_no = "";
            $this->grade = "";
            $this->field = array();
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    public function &getCopyModel()
    {
        knjp744Query::getCopyYearQuery($this);

        $this->auto_no = "";
        $this->grade = "";
        $this->field = array();
        $this->setMessage("MSG201");

        return;
    }
}
