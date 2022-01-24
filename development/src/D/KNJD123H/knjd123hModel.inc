<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd123hModel.inc 56586 2017-10-22 12:52:35Z maeshiro $

class knjd123hModel extends Model
{
    public $field = array();
    public $data  = array();
    public $creditflg;
    public $motoCnt;
    public $cmd;
    public $term;
    public $gen_ed = "900100";        //総合学習コード
    public $testcd;//講座コードをどの考査にセットするかを判断するコードの値を保持用 NO004Add
    public $repGetCredit;
    public $usePerfect;
    public $isJitu;
    public $absenceDiv;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjd123hModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
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

        for ($i=1; $i<=5; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        for ($i=1; $i<=3; $i++) {
            $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数

        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd123hQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd123hQuery::getSchoolMst();
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
        $term = array("SEM1_INTR_SCORE","SEM1_TERM_SCORE","SEM1_VALUE","SEM2_INTR_SCORE","SEM2_TERM_SCORE","SEM2_VALUE","SEM3_TERM_SCORE","PROV_FLG","GRAD_VALUE2","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");

        //初期化
        $this->fields = array();
        for ($j=0; $j < get_count($term); $j++) {
            for ($i=0; $i<$this->field["COUNT"]; $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);

                $this->fields[$term[$j]][]       = $post_term;
            }
        }
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $this->fields["NOTICE_LATE"][] = VARS::post("NOTICE_LATE"."-".$i);
            $this->fields["COMBINED_CREDIT_SUM"][] = VARS::post("COMBINED_CREDIT_SUM"."-".$i);
        }

        knjd123hQuery::update($this);
        knjd123hQuery::updateExecuted($this);
        knjd123hQuery::updateExecuted2($this);
        $this->setMessage("MSG201");
    }
}
