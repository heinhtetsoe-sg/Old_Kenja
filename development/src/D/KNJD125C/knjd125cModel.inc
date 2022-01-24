<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125cModel.inc 56581 2017-10-22 12:37:16Z maeshiro $

class knjd125cModel extends Model
{
    public $field = array();
    public $data  = array();
    public $passdata  = array();
    public $creditflg;
    public $cmd;
    public $term;
    public $gen_ed = "90";
    public $testcd;
    public $repGetCredit;
    public $usePerfect;
    public $isJitu;
    public $absenceDiv;
    public $lastColumn;//CSVの最後の列の値

    public function knjd125cModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        if ($this->cmd != "subclasscd" && VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }

        $this->cmd     = VARS::request("cmd");

        for ($i = 1; $i <= 4; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        $this->getProperties();
        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd125cQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];
        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd125cQuery::getSchoolMst();
        $this->isJitu = $rtnRow["JUGYOU_JISU_FLG"] == "2" ? true : false; // 1:法定授業、2:実授業
        $this->absenceDiv = "1"; // 1:年間、2:随時

        $this->getPropertiesAll();
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function getProperties()
    {
        $posFlg2 = false;
        $retVal = "";
        
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp, 1024)) {
            $pos = strpos($line, "usePerfect");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                if (!$posFlg2) {
                    $this->usePerfect = false;
                }
            } else {
                $retVal = str_replace("usePerfect = ", "", $line);
                $retVal = str_replace("\r\n", "", $retVal);
                $retVal = str_replace("\r", "", $retVal);
                $retVal = str_replace("\n", "", $retVal);
                $this->usePerfect = $retVal;
                $posFlg2 = true;
            }
        }
        fclose($fp);

        return $retVal;
    }

    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        $term = array("SEM1_INTR_SCORE1","SEM1_INTR_SCORE2","SEM1_INTR_SCORE3","SEM1_TERM_SCORE1","SEM1_TERM_SCORE2","SEM1_TERM_SCORE3","SEM1_SCORE","SEM1_VALUE","SEM2_INTR_SCORE1","SEM2_INTR_SCORE2","SEM2_INTR_SCORE3","SEM2_TERM_SCORE1","SEM2_TERM_SCORE2","SEM2_TERM_SCORE3","SEM2_SCORE","SEM2_VALUE","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");
        $term2 = array(
            "SCORE1111",
            "SCORE1112",
            "SCORE1113",
            "SCORE1211",
            "SCORE1212",
            "SCORE1213",
            "SCORE1900",
            "VALUE1900",
            "SCORE2111",
            "SCORE2112",
            "SCORE2113",
            "SCORE2211",
            "SCORE2212",
            "SCORE2213",
            "SCORE2900",
            "VALUE2900",
            "VALUE9900",
            "COMP_CREDIT9900",
            "GET_CREDIT9900"
        );

        $this->fields = array();
        $this->fields2 = array();
        $this->fields3 = array();
        $this->isScoreAllNull = array();
        for ($j = 0; $j < get_count($term); $j++) {
            $this->isScoreAllNull[$term2[$j]] = false;
            for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$term[$j]][]   = $post_term;
                    $this->fields2[$term2[$j]][] = $post_term;
                    $this->fields3[$term2[$j]][] = "";
                    if (strlen($post_term)) {
                        $this->isScoreAllNull[$term2[$j]] = true;
                    }
                } else {
                    $this->fields[$term[$j]][]   = $this->data[$term_name];
                    $this->fields2[$term2[$j]][] = $this->data[$term_name];
                    $this->fields3[$term2[$j]][] = $this->passdata[$term_name];
                    if (strlen($this->data[$term_name])) {
                        $this->isScoreAllNull[$term2[$j]] = true;
                    }
                    if (strlen($this->passdata[$term_name])) {
                        $this->isScoreAllNull[$term2[$j]] = true;
                    }
                }
            }
        }

        knjd125cQuery::updateRecord($this);
        knjd125cQuery::updateExecuted($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
