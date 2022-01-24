<?php

require_once('for_php7.php');


class knjd124hModel extends Model
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
    public $useRecordChkfinDat;
    public $chikokuHyoujiFlg;
    public $useSlumpHyouka;
    public $CalcMethod;
    public $lastColumn;//CSVの最後の列の値

    public function knjd124hModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        if (VARS::get("SEND_PRGID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd     = VARS::request("cmd");
        
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::request("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::request("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        }
        for ($i = 1; $i <= 5; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        //for($i=1;$i<=3;$i++) {
        //    $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        //}
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数
        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd124hQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->useRecordChkfinDat = (strlen($this->Properties["useRecordChkfinDat"])) ? $this->Properties["useRecordChkfinDat"] : 0;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;

        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd124hQuery::getSchoolMst();
        $this->isJitu = $rtnRow["JUGYOU_JISU_FLG"] == "2" ? true : false; // 1:法定授業、2:実授業
        $this->absenceDiv = "1"; // 1:年間、2:随時

        //「評価にリンクを付けるか？」のフラグを取得
        $rtnRow = knjd124hQuery::getNameMstD048();
        $this->useSlumpHyouka = $rtnRow["NAMESPARE1"] == "1" ? true : false; // 1:評価にリンクを付ける

        //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
        $rtnRow = knjd124hQuery::getCalcMethod();
        $this->CalcMethod = !strlen($rtnRow["SCHOOL_REMARK1"]) ? "1" : $rtnRow["SCHOOL_REMARK1"];

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
        $term = array("SEM1_INTR_SCORE"
                     ,"SEM1_TERM_SCORE"
                     ,"SEM1_VALUE"
                     ,"SEM2_INTR_SCORE"
                     ,"SEM2_TERM_SCORE"
                     ,"SEM2_VALUE"
                     ,"SEM3_TERM_SCORE"
                     ,"GRAD_SCORE_HEIJOU"
                     ,"GRAD_SCORE"
                     ,"PROV_FLG"
                     ,"GRAD_VALUE"
                     ,"COMP_CREDIT"
                     ,"GET_CREDIT");
        //初期化
        $this->fields = array();
        $this->isScoreAllNull = array();
        for ($j=0; $j < get_count($term); $j++) {
            $this->isScoreAllNull[$term[$j]] = false;
            for ($i=0; $i<$this->field["COUNT"]; $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);
                //出欠情報が入ってたら成績は空
                if ($post_term == "-" || $post_term == "*") {
                    $this->fields[$term[$j]."_DI"][] = $post_term;
                    $this->fields[$term[$j]][]       = "";
                } else {
                    $this->fields[$term[$j]."_DI"][] = "";
                    $this->fields[$term[$j]][]       = $post_term;
                }
                if (strlen($post_term)) {
                    $this->isScoreAllNull[$term[$j]] = true;
                }
            }
        }
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $this->fields["NOTICE_LATE"][] = VARS::post("NOTICE_LATE"."-".$i);
            $this->fields["COMBINED_CREDIT_SUM"][] = VARS::post("COMBINED_CREDIT_SUM"."-".$i);
        }
        knjd124hQuery::update($this);
        if ($this->useRecordChkfinDat == "1") {
            knjd124hQuery::updateRecordChkfin($this, "1");
        //knjd124hQuery::updateRecordChkfin($this, "2");
        } else {
            knjd124hQuery::updateRecordSchChrTest($this);
            //knjd124hQuery::updateRecordChkfin($this, "2");
        }
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
