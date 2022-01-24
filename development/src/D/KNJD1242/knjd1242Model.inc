<?php

require_once('for_php7.php');


class knjd1242Model extends Model {
    var $field = array();
    var $data  = array();
    var $creditflg;
    var $motoCnt;
    var $cmd;
    var $term;
    var $gen_ed = "90";
    var $testcd;//講座コードをどの考査にセットするかを判断するコードの値を保持用
    var $repGetCredit;
    var $testTable;
    var $usePerfect;
    var $isJitu;
    var $absenceDiv;

    function knjd1242Model() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        $this->cmd     = VARS::request("cmd");
        for ($i = 1; $i <= 5; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        for($i=1;$i<=9;$i++) {
            $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd1242Query::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];
        //プロパティファイル
        $this->getPropertiesAll();
        //プロパティファイル
        $this->getProperties();
        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd1242Query::getSchoolMst();
        $this->isJitu = $rtnRow["JUGYOU_JISU_FLG"] == "2" ? true : false; // 1:法定授業、2:実授業
        $this->absenceDiv = "1"; // 1:年間、2:随時
    }

    function getProperties()
    {
        $posFlg1 = $posFlg2 = false;
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
        while ($line = fgets($fp,1024)) {
            $pos = strpos($line, "useTestCountflg");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                if (!$posFlg1) $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
            } else {
                $retVal = str_replace("useTestCountflg = ", "", $line);
                $retVal = str_replace("\r\n", "", $retVal);
                $retVal = str_replace("\r", "", $retVal);
                $retVal = str_replace("\n", "", $retVal);
                $this->testTable = $retVal;
                $posFlg1 = true;
            }
            $pos = strpos($line, "usePerfect");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                if (!$posFlg2) $this->usePerfect = false;
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

    //入力項目チェック
    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        /*  更新処理 */
        //学期成績データ項目名を配列にする。
        $term = array("SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_VALUE","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_VALUE","SEM3_TERM_SCORE","SEM3_TERM_VALUE","SEM3_VALUE","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");
        //初期化
        $this->fields = array();
        for ($j=0; $j < get_count($term); $j++) {
            for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    //出欠情報が入ってたら成績は空
                    if ($post_term == "-" || $post_term == "=") {
                        $this->fields[$term[$j]."_DI"][] = $post_term;
                        $this->fields[$term[$j]][]       = "";
                    } else {
                        $this->fields[$term[$j]."_DI"][] = "";
                        $this->fields[$term[$j]][]       = $post_term;
                    }
                } else {
                    $this->fields[$term[$j]][]       = $this->data[$term_name];
                    $this->fields[$term[$j]."_DI"][] = $this->attend_data[$term_name];
                }
            }
        }
        knjd1242Query::update($this);
        knjd1242Query::updateExecuted($this);
        knjd1242Query::updateExecuted2($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
?>
