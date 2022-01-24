<?php

require_once('for_php7.php');


class knjd212cModel extends Model
{
    public $grade;
    public $school_kind;
    public $seme;
    public $kind;
    public $item;
    public $sdiv;
    public $seme_sch;
    public $cmd;
    public $control;
    public $subclasscd;
    public $tableStr;
    public $calcDeviationFlg;

    public function knjd212cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->grade    = VARS::post("GRADE");
        $this->school_kind = VARS::post("SCHOOL_KIND"); //学校種別
        $this->semeester     = VARS::post("SEMESTER");
        $this->seme_sch = ($this->semeester != "9") ? $this->semeester : CTRL_SEMESTER;
        $this->subclasscd = VARS::post("SUBCLASSCD");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function &getMainModel()
    {
    }

    //事前処理チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();

        //指定学期・指定科目の算出設定が事前にされているかチェック
        $query = knjd212cQuery::getAdminCtrlCalcExists($this);
        $result = $db->query($query);
        $existsSubclass = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $existsSubclass[$row["SUBCLASSCD"]] = $row["SUBCLASSCD"];
        }
        $result->free();

        $errorFlg = false;
        $errorMsg = "算出設定がされていない科目があります。";
        if ($this->subclasscd != '000000') {
            if (!array_key_exists($this->subclasscd, $existsSubclass) && !isset($existsSubclass["00-{$this->school_kind}-00-000000"])) {
                $errorMsg .= "\\n".$this->subclassArray[$this->subclasscd];
                $errorFlg = true;
            }
        } else {
            foreach ($this->subclassArray as $subclass => $subclassName) {
                if (!array_key_exists($subclass, $existsSubclass) && !isset($existsSubclass["00-{$this->school_kind}-00-000000"])) {
                    $errorMsg .= "\\n".$subclassName;
                    $errorFlg = true;
                }
            }
        }

        if ($errorFlg) {
            $this->setMessage($errorMsg);
            return false;
        }

        //割合チェックとセット
        if ($this->semeester != '9') {
            $query = knjd212cQuery::getRateSemes($this);
            $result = $db->query($query);

            $this->rateSemes = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclass = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                $this->rateSemes[$subclass] = $row["RATE"];
            }
            $result->free();

            $errorFlg = false;
            $errorMsg = "割合未設定科目があります。";
            if ($this->subclasscd != '000000') {
                if (!array_key_exists($this->subclasscd, $this->rateSemes)) {
                    $errorMsg .= "\\n".$this->subclassArray[$this->subclasscd];
                    $errorFlg = true;
                }
            } else {
                foreach ($this->subclassArray as $subclass => $subclassName) {
                    if (!array_key_exists($subclass, $this->rateSemes)) {
                        $errorMsg .= "\\n".$subclassName;
                        $errorFlg = true;
                    }
                }
            }

            if ($errorFlg) {
                $this->setMessage($errorMsg);
                return false;
            }
        }

        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //事前処理チェック
        if ($this->validateInput()) {
            knjd212cQuery::updateScore($this);
            knjd212cQuery::executeRireki($this);
            $this->setMessage("MSG202");
        }
    }
}
?>
