<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd121tModel.inc 56586 2017-10-22 12:52:35Z maeshiro $

class knjd121tModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $creditflg;
    var $motoCnt;
    var $cmd;
    var $term;
    var $gen_ed = "90";
    var $testcd;//講座コードをどの考査にセットするかを判断するコードの値を保持用 NO001Add
    var $repGetCredit;
    var $testTable;
    var $usePerfect;
    var $isJitu;
    var $absenceDiv;
    var $useRecordChkfinDat;

    /**
     * メンバ変数の初期化
     */
    function knjd121tModel()
    {
    }

    function init()
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
        if ($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else if ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }

        for($i=1;$i<=5;$i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        for($i=1;$i<=8;$i++) {
            $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数

        // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd121tQuery::getNameMst();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
        }
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->useRecordChkfinDat = (strlen($this->Properties["useRecordChkfinDat"])) ? $this->Properties["useRecordChkfinDat"] : 0;
        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd121tQuery::getSchoolMst();
        $this->isJitu = $rtnRow["JUGYOU_JISU_FLG"] == "2" ? true : false; // 1:法定授業、2:実授業
        $this->absenceDiv = "1"; // 1:年間、2:随時
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        /*  更新処理 */

        //学期成績データ項目名を配列にする。
        $term = array("SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_TERM2_SCORE","SEM1_TERM2_VALUE","SEM1_VALUE","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_VALUE","PROV_FLG","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");//Modify

        //初期化
        $this->fields = array();
        for ($j=0; $j < get_count($term); $j++) {

            for($i=0;$i<$this->field["COUNT"];$i++){

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
            }
        }
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $this->fields["NOTICE_LATE"][] = VARS::post("NOTICE_LATE"."-".$i);
            $this->fields["COMBINED_CREDIT_SUM"][] = VARS::post("COMBINED_CREDIT_SUM"."-".$i);
        }

        knjd121tQuery::update($this);
        //knjd121tQuery::updateExecuted($this);   //SCH_CHR_TESTの更新
        //knjd121tQuery::updateExecuted2($this);  //RECORD_CHKFIN_DATの更新
        if ($this->useRecordChkfinDat == "1") {
            knjd121tQuery::updateRecordChkfin($this, "1");
            knjd121tQuery::updateRecordChkfin($this, "2");
        } else {
            knjd121tQuery::updateRecordSchChrTest($this);
            knjd121tQuery::updateRecordChkfin($this, "2");
        }
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
?>
