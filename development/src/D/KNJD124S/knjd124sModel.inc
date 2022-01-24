<?php

require_once('for_php7.php');


class knjd124sModel extends Model
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
    public $D073Namespare1;
    public $lastColumn;//CSVの最後の列の値

    public function knjd124sModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->cmd     = VARS::request("cmd");
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
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
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数
        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd124sQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->useRecordChkfinDat = (strlen($this->Properties["useRecordChkfinDat"])) ? $this->Properties["useRecordChkfinDat"] : 0;

        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd124sQuery::getSchoolMst();
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
        $term = array("SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_VALUE","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_VALUE","SEM3_TERM_SCORE","SEM3_VALUE","GRAD_SCORE","PROV_VALUE","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");
        //初期化
        $this->fields = array();
        $this->isScoreAllNull = array();
        for ($j=0; $j < get_count($term); $j++) {
            $this->isScoreAllNull[$term[$j]] = false;
            for ($i=0; $i<$this->field["COUNT"]; $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);
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
            }
        }
        //仮評定フラグ対応
        $this->D073Namespare1 = "";
        if ($this->Properties["useProvFlg"] == '1') {
            //レコード削除フラグ
            if ($this->isScoreAllNull["PROV_VALUE"]) {
                $this->isScoreAllNull["GRAD_VALUE"] = true;
            }
            //学年成績から換算した評定を「XXX評定」として扱うフラグ取得・・・仮評定(1)、本評定(NULL)
            $rtnRow = knjd124sQuery::getNameMstD073();
            $this->D073Namespare1 = $rtnRow["NAMESPARE1"];
        }
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $this->fields["NOTICE_LATE"][] = VARS::post("NOTICE_LATE"."-".$i);
            $this->fields["COMBINED_CREDIT_SUM"][] = VARS::post("COMBINED_CREDIT_SUM"."-".$i);
            //仮評定フラグ対応
            if ($this->Properties["useProvFlg"] == '1') {
                //仮評定と学年評定を１つにしたい・・・仮評定があれば学年評定にセット
                if (strlen($this->fields["PROV_VALUE"][$i])) {
                    $this->fields["GRAD_VALUE"][$i]     = $this->fields["PROV_VALUE"][$i];
                }
                if (strlen($this->fields["PROV_VALUE_DI"][$i])) {
                    $this->fields["GRAD_VALUE_DI"][$i]  = $this->fields["PROV_VALUE_DI"][$i];
                }
                //仮評定があれば、仮評定フラグを１としたい・・・
                $this->fields["PROV_FLG"][$i] = "";
                if (strlen($this->fields["PROV_VALUE"][$i])) {
                    $this->fields["PROV_FLG"][$i] = "1";
                }
                if (strlen($this->fields["PROV_VALUE_DI"][$i])) {
                    $this->fields["PROV_FLG"][$i] = "1";
                }
                if ($this->fields["GRAD_SCORE"][$i] != "" && $this->fields["GRAD_VALUE"][$i] == "" && $this->D073Namespare1 == "1") {
                    $this->fields["PROV_FLG"][$i] = "1";
                }
            }
        }

        knjd124sQuery::update($this);
        if ($this->useRecordChkfinDat == "1") {
            knjd124sQuery::updateExecuted2($this, "1");
            knjd124sQuery::updateExecuted2($this, "2");
        } else {
            knjd124sQuery::updateExecuted($this);
            knjd124sQuery::updateExecuted2($this, "2");
        }
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
