<?php

require_once('for_php7.php');


class knjd128f_2Model extends Model
{
    public $field = array();
    public $data  = array();
    public $creditflg;
    public $cmd;
    public $term;
    public $gen_ed = "90";
    public $testcd;//講座コードをどの考査にセットするかを判断するコードの値を保持用
    public $repGetCredit;
    public $testTable;
    public $usePerfect;
    public $isJitu;
    public $absenceDiv;
    public $prgid;
    public $auth;
    public $useRecordChkfinDat;
    public $chikokuHyoujiFlg;
    public $lastColumn;//CSVの最後の列の値

    public function knjd128f_2Model()
    {
    }

    public function init()
    {
        common::GetControlMaster_Fnc($this->control);
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        if (VARS::get("prgid")) {
            $this->prgid = VARS::get("prgid");
        }
        if (VARS::get("auth")) {
            $this->auth = VARS::get("auth");
        }

        $this->cmd     = VARS::request("cmd");
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        $this->field["CHAIRCD"] = VARS::request("CHAIRCD");
        if ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        }
        $this->field["TESTCD"] = VARS::request("TESTCD");
        $this->field["RECORD_DIV"] = (substr($this->field["TESTCD"], 1, 2) == "99") ? "2" : "1";
        for ($i = 1; $i <= 1; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数
        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd128f_2Query::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->useRecordChkfinDat = (strlen($this->Properties["useRecordChkfinDat"])) ? $this->Properties["useRecordChkfinDat"] : 0;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;

        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd128f_2Query::getSchoolMst();
        $this->isJitu = $rtnRow["JUGYOU_JISU_FLG"] == "2" ? true : false; // 1:法定授業、2:実授業
        $this->absenceDiv = "1"; // 1:年間、2:随時

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        /*  更新処理 */
        //学期成績データ項目名を配列にする。
        $term = array("SLUMP","TST_SCORE","REMARK");
        //初期化
        $this->fields = array();
        $this->isScoreAllNull = array();
        for ($j=0; $j < get_count($term); $j++) {
            $this->isScoreAllNull[$term[$j]] = false;
            for ($i=0; $i<$this->field["COUNTER"]; $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);
                //不振チェックボックス
                if ($term[$j] == "SLUMP" || $term[$j] == "REMARK") {
                    $this->fields[$term[$j]][] = $post_term;
                } elseif (isset($post_term)) {
                    //出欠情報が入ってたら成績は空
                    if ($post_term == "-" || $post_term == "=") {
                        $this->fields[$term[$j]."_DI"][] = $post_term;
                        $this->fields[$term[$j]][]       = "";
                    } else {
                        $this->fields[$term[$j]."_DI"][] = "";
                        $this->fields[$term[$j]][]       = $post_term;
                    }
                    if (strlen($post_term)) {
                        $this->isScoreAllNull[$term[$j]] = true;
                    }
                } else {
                    $this->fields[$term[$j]][]       = $this->data[$term_name];
                    $this->fields[$term[$j]."_DI"][] = $this->attend_data[$term_name];
                    if (strlen($this->data[$term_name])) {
                        $this->isScoreAllNull[$term[$j]] = true;
                    }
                }
            }
        }
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
        }
        knjd128f_2Query::updateSlump($this);
        //knjd128f_2Query::update($this);
        //if ($this->useRecordChkfinDat == "1" || $this->field["RECORD_DIV"] == "2") {
        //    knjd128f_2Query::updateRecordChkfin($this, $this->field["RECORD_DIV"]);
        //} else {
        //    knjd128f_2Query::updateRecordSchChrTest($this);
        //}
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
