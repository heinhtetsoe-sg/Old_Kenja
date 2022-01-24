<?php

require_once('for_php7.php');


class knjd127aModel extends Model
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
    public $chikokuHyoujiFlg;
    public $useRecordChkfinDat;
    public $lastColumn;//CSVの最後の列の値

    public function knjd127aModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        $this->cmd     = VARS::request("cmd");
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } elseif ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        for ($i = 1; $i <= 5; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        for ($i=1; $i<=8; $i++) {
            $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd127aQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;
        $this->useRecordChkfinDat = (strlen($this->Properties["useRecordChkfinDat"])) ? $this->Properties["useRecordChkfinDat"] : 0;

        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd127aQuery::getSchoolMst();
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
        $term = array("SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_VALUE","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_VALUE","SEM3_TERM_SCORE","SEM3_TERM_VALUE","GRAD_SCORE","PROV_FLG","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");
        //初期化
        $this->fields = array();
        $this->isScoreAllNull = array();
        for ($j=0; $j < get_count($term); $j++) {
            $this->isScoreAllNull[$term[$j]] = false;
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
        knjd127aQuery::update($this);
        if ($this->useRecordChkfinDat == "1") {
            knjd127aQuery::updateExecuted2($this, "1");
            knjd127aQuery::updateExecuted2($this, "2");
        } else {
            knjd127aQuery::updateExecuted2($this, "2");
            knjd127aQuery::updateExecuted($this);
        }
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
